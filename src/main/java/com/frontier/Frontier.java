package com.frontier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.frontier.events.RequestNomads;
import com.frontier.network.FrontierPackets;
import com.frontier.regions.RegionManager;
import com.frontier.register.FrontierCommands;
import com.frontier.register.FrontierEntities;
import com.frontier.settlements.SettlementManager;
import com.frontier.structures.StructureManager;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Frontier implements ModInitializer
{
	public static String MOD_ID = "frontier";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	
	// intervals in seconds
	private static final int playerRepInterval = 5;
	private static final int borderDrawInterval = 1;
	private static int repTickCounter = 0;
	private static int borderTickCounter = 0;
	
	// fully develop architect NPC
	
	// create building ui
	// show dimensions of structure to player when they try to manually place structure location
	// test warehouse construction/upgrading/repairing with architect
	
	@Override
	public void onInitialize()
	{
		FrontierEntities.register();
		FrontierCommands.register();
		
		FrontierManager.manageKeyBindings();
		FrontierManager.manageRegionData();
		FrontierManager.manageEntityData();
		FrontierManager.managePlayerData();
		FrontierManager.manageSettlementData();
		
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
	
	public static void sendMessage(ServerPlayerEntity player, String message, Formatting color)
	{
        if (player != null && message != null && color != null)
            player.sendMessage(Text.literal(message).styled(style -> style.withColor(color)), false);
    }
}