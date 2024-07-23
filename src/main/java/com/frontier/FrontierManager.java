package com.frontier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.frontier.entities.settler.SettlerEntity;
import com.frontier.gui.StructureScreen;
import com.frontier.network.FrontierPackets;
import com.frontier.gui.PlayerCardScreen;
import com.frontier.regions.RegionManager;
import com.frontier.register.FrontierKeyBindings;
import com.frontier.renderers.RegionMapRenderer;
import com.frontier.settlements.Settlement;
import com.frontier.settlements.SettlementManager;
import com.frontier.structures.Structure;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class FrontierManager
{
	public static void entityData()
	{
		ServerLifecycleEvents.SERVER_STARTED.register(server -> 
		{
	        ServerWorld world = server.getWorld(World.OVERWORLD);
	        if (world != null && !world.isClient)
	            SettlerEntity.loadEntityData(world);
	    });
	}
	
	public static void playerData()
	{
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
		{
		    ServerPlayerEntity player = handler.player;
		    UUID playerUUID = player.getUuid();
		    PlayerData playerData = PlayerData.players.getOrDefault(playerUUID, new PlayerData(playerUUID, player.getEntityName(), "N/A", "Adventurer", 0, server));
		    if (!PlayerData.players.containsKey(playerUUID))
		    {
		    	PlayerData.players.put(playerUUID, playerData);
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
	
	public static void settlementData()
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
	
	public static void regionData()
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
	
	public static void keyBindings()
	{
		FrontierKeyBindings.register();
        KeyBindingHelper.registerKeyBinding(FrontierKeyBindings.keyCharacterSheet);
        KeyBindingHelper.registerKeyBinding(FrontierKeyBindings.keyToggleRegions);
        KeyBindingHelper.registerKeyBinding(FrontierKeyBindings.keyToggleConstructionGUI);
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> 
        {
            while (FrontierKeyBindings.keyCharacterSheet.wasPressed())
                client.setScreen(new PlayerCardScreen());
            
            while (FrontierKeyBindings.keyToggleConstructionGUI.wasPressed())
            	ClientPlayNetworking.send(FrontierPackets.SETTLEMENT_RESOURCES_REQUEST_ID, new PacketByteBuf(Unpooled.buffer()));
            
            while (FrontierKeyBindings.keyToggleRegions.wasPressed())
                RegionMapRenderer.isRenderingEnabled = !RegionMapRenderer.isRenderingEnabled;
        });
	}
}