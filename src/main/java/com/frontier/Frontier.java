package com.frontier;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.frontier.entities.ArchitectEntity;
import com.frontier.entities.NomadEntity;
import com.frontier.entities.SettlerEntity;
import com.frontier.events.RequestNomads;
import com.frontier.gui.PlayerCardScreen;
import com.frontier.network.FrontierPackets;
import com.frontier.regions.RegionManager;
import com.frontier.renderers.RegionMapRenderer;
import com.frontier.renderers.SettlerEntityRenderer;
import com.frontier.settlements.Settlement;
import com.frontier.settlements.SettlementManager;
import com.frontier.structures.Structure;
import com.frontier.structures.StructureManager;
import com.frontier.util.FrontierCommands;
import com.frontier.util.FrontierKeyBindings;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class Frontier implements ModInitializer 
{
	public static String MOD_ID = "frontier";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	
	public static final EntityType<NomadEntity> NOMAD_ENTITY = Registry.register(Registries.ENTITY_TYPE, new Identifier("settlers", "nomad"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, NomadEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).trackRangeBlocks(128).build());
	
	public static final EntityType<ArchitectEntity> ARCHITECT_ENTITY = Registry.register(Registries.ENTITY_TYPE, new Identifier("settlers", "architect"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, ArchitectEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).trackRangeBlocks(128).build());
	
	public void registerEntities()
	{
		EntityRendererRegistry.register(NOMAD_ENTITY,(EntityRendererFactory.Context context) -> new SettlerEntityRenderer(context));
		FabricDefaultAttributeRegistry.register(NOMAD_ENTITY, SettlerEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 20).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2));
		
		EntityRendererRegistry.register(ARCHITECT_ENTITY,(EntityRendererFactory.Context context) -> new SettlerEntityRenderer(context));
		FabricDefaultAttributeRegistry.register(ARCHITECT_ENTITY, SettlerEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 20).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1));
	}
	
	// intervals in seconds
	private static final int playerRepInterval = 5;
	private static final int borderDrawInterval = 1;
	private static int repTickCounter = 0;
	private static int borderTickCounter = 0;
	
	// create building ui
	// show dimensions of structure to player when they try to manually place structure location
	// test warehouse construction/upgrading/repairing with architect
	
	@Override
	public void onInitialize()
	{
		registerEntities();
		registerCommands();
		
		manageKeyBindings();
		
		manageRegionData();
		manageEntityData();
		managePlayerData();
		manageSettlementData();
		
		FrontierPackets.apply();
		RegionManager.registerCallback();
		SettlementManager.registerCallback();
		RequestNomads.registerCallback();
		
		updateWorldEvents();
	}
	
	public static void updateWorldEvents()
	{
		StructureManager structureManager = new StructureManager();
		structureManager.register();
		
	    ServerTickEvents.END_SERVER_TICK.register(server ->
	    {
	        repTickCounter++;
	        if (repTickCounter >= (20 * playerRepInterval))
	        {
	            repTickCounter = 0;
	            SettlementManager.updatePlayerReputations(server);
	        }

	        borderTickCounter++;
	        if (borderTickCounter >= (20 * borderDrawInterval))
	        {
	            borderTickCounter = 0;
	            SettlementManager.drawSettlementBorder(server);
	        }
	    });
	}
	
	public void manageEntityData()
	{
		ServerLifecycleEvents.SERVER_STARTED.register(server -> 
		{
	        ServerWorld world = server.getWorld(World.OVERWORLD);
	        if (world != null && !world.isClient)
	            SettlerEntity.loadData(world);
	    });
	}
	
	public void managePlayerData()
	{
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
		{
		    ServerPlayerEntity player = handler.player;
		    UUID playerUUID = player.getUuid();
		    PlayerData playerData = PlayerData.map.getOrDefault(playerUUID, new PlayerData(playerUUID, player.getEntityName(), "N/A", "Adventurer", 0, server));
		    if (!PlayerData.map.containsKey(playerUUID))
		    {
		    	PlayerData.map.put(playerUUID, playerData);
		    	playerData.saveData();
		    }
		});
		
		ServerLifecycleEvents.SERVER_STARTED.register(server ->
		{
			PlayerData.loadData(server);
	    });
		
		ServerLifecycleEvents.SERVER_STOPPING.register(server ->
        {
        	PlayerData.reset();
        });
	}
	
	private void manageSettlementData()
	{
        ServerLifecycleEvents.SERVER_STARTED.register(server ->
        {
        	SettlementManager.loadSettlements(server);
        	for (Settlement settlement : SettlementManager.getSettlements().values())
            	for (Structure structure : settlement.getStructures())
            		structure.resume(server.getOverworld());
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server ->
        {
        	SettlementManager.saveSettlements(server);
        	SettlementManager.reset();
        });
    }
	
	private void manageRegionData()
	{
        ServerLifecycleEvents.SERVER_STARTED.register(server ->
        {
            RegionManager.setWorldSeed(server.getOverworld().getSeed());
            ServerWorld world = server.getWorld(World.OVERWORLD);
            if (world != null && !world.isClient)
            	RegionManager.loadRegionData(world);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server ->
        {
            ServerWorld world = server.getWorld(World.OVERWORLD);
            if (world != null && !world.isClient)
                RegionManager.saveRegionData(world);
            RegionManager.reset();
        });
    }
	
	public void manageKeyBindings()
	{
		FrontierKeyBindings.register();
        KeyBindingHelper.registerKeyBinding(FrontierKeyBindings.playerCardKey);
        KeyBindingHelper.registerKeyBinding(FrontierKeyBindings.toggleRegionsKey);
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> 
        {
            while (FrontierKeyBindings.playerCardKey.wasPressed())
                client.setScreen(new PlayerCardScreen());
            
            while (FrontierKeyBindings.toggleRegionsKey.wasPressed())
                RegionMapRenderer.isRenderingEnabled = !RegionMapRenderer.isRenderingEnabled;
        });
	}
	
	public void registerCommands()
	{
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
		{
	        LiteralArgumentBuilder<ServerCommandSource> getRenownCommand = CommandManager.literal("getFrontierRenown")
	                .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
	                .executes(context -> FrontierCommands.getRenown(context, GameProfileArgumentType.getProfileArgument(context, "player").iterator().next().getName())));
	        dispatcher.register(getRenownCommand);
	    });

	    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
	    {
	        LiteralArgumentBuilder<ServerCommandSource> setRenownCommand = CommandManager.literal("setFrontierRenown")
	                .requires(source -> source.hasPermissionLevel(2))
	                .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
	                .then(CommandManager.argument("renown", IntegerArgumentType.integer())
	                        .executes(context -> FrontierCommands.setRenown(context, GameProfileArgumentType.getProfileArgument(context, "player").iterator().next().getName(), 
	                                IntegerArgumentType.getInteger(context, "renown")))));
	        dispatcher.register(setRenownCommand);
	    });
	    
	    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
	    {
	        LiteralArgumentBuilder<ServerCommandSource> command = CommandManager.literal("getFrontierFaction")
	                .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
	                .executes(context -> FrontierCommands.getFaction(context, GameProfileArgumentType.getProfileArgument(context, "player").iterator().next().getName())));
	        dispatcher.register(command);
	    });
	    
	    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
	    {
	        LiteralArgumentBuilder<ServerCommandSource> command = CommandManager.literal("setFrontierFaction")
	                .requires(source -> source.hasPermissionLevel(2))
	                .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
	                .then(CommandManager.argument("faction", StringArgumentType.word())
	                        .executes(context -> FrontierCommands.setFaction(context, GameProfileArgumentType.getProfileArgument(context, "player").iterator().next().getName(), 
	                        		StringArgumentType.getString(context, "faction")))));
	        dispatcher.register(command);
	    });
	    
	    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
	    {
	        LiteralArgumentBuilder<ServerCommandSource> command = CommandManager.literal("getFrontierProfession")
	                .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
	                .executes(context -> FrontierCommands.getProfession(context, GameProfileArgumentType.getProfileArgument(context, "player").iterator().next().getName())));
	        dispatcher.register(command);
	    });
	    
	    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
	    {
	        LiteralArgumentBuilder<ServerCommandSource> command = CommandManager.literal("setFrontierProfession")
	                .requires(source -> source.hasPermissionLevel(2))
	                .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
	                .then(CommandManager.argument("profession", StringArgumentType.word())
	                        .executes(context -> FrontierCommands.setProfession(context, GameProfileArgumentType.getProfileArgument(context, "player").iterator().next().getName(), 
	                        		StringArgumentType.getString(context, "profession")))));
	        dispatcher.register(command);
	    });
	}
	
	public static void sendMessage(ServerPlayerEntity player, String message, Formatting color)
	{
        if (player != null && message != null && color != null)
            player.sendMessage(Text.literal(message).styled(style -> style.withColor(color)), false);
    }
}