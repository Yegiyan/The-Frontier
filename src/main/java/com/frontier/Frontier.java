package com.frontier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.frontier.events.RequestNomads;
import com.frontier.network.FrontierPackets;
import com.frontier.regions.RegionManager;
import com.frontier.register.FrontierCommands;
import com.frontier.register.FrontierEntities;
import com.frontier.settlements.SettlementManager;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Frontier implements ModInitializer
{
	public static String MOD_ID = "frontier";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	
	// create date system
	// create 'deaths' list for settlements for cemetary
	// fully develop architect NPC
	
	// create building ui
	// show dimensions of structure to player when they try to manually place structure location
	// test warehouse construction/upgrading/repairing with architect
	
	@Override
	public void onInitialize()
	{
		FrontierEntities.register();
		FrontierCommands.register();
		
		FrontierManager.keyBindings();
		FrontierManager.regionData();
		FrontierManager.entityData();
		FrontierManager.playerData();
		FrontierManager.settlementData();
		
		RegionManager.registerCallback();
		SettlementManager.registerCallback();
		RequestNomads.registerCallback();
		
		FrontierPackets.apply();
		
		FrontierUpdate.worldEvents();
	}
	
	public static void sendMessage(ServerPlayerEntity player, String message, Formatting color)
	{
        if (player != null && message != null && color != null)
            player.sendMessage(Text.literal(message).styled(style -> style.withColor(color)), false);
        else
        	LOGGER.error("Frontier - null message value!");
    }
}