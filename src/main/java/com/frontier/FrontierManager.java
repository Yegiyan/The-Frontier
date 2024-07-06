package com.frontier;

import java.util.UUID;

import com.frontier.entities.SettlerEntity;
import com.frontier.gui.PlayerCardScreen;
import com.frontier.regions.RegionManager;
import com.frontier.register.FrontierKeyBindings;
import com.frontier.renderers.RegionMapRenderer;
import com.frontier.settlements.Settlement;
import com.frontier.settlements.SettlementManager;
import com.frontier.structures.Structure;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class FrontierManager
{
	public static void manageEntityData()
	{
		ServerLifecycleEvents.SERVER_STARTED.register(server -> 
		{
	        ServerWorld world = server.getWorld(World.OVERWORLD);
	        if (world != null && !world.isClient)
	            SettlerEntity.loadData(world);
	    });
	}
	
	public static void managePlayerData()
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
	
	public static void manageSettlementData()
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
	
	public static void manageRegionData()
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
	
	public static void manageKeyBindings()
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
}