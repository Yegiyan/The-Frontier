package com.frontier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.frontier.calendar.FrontierCalendarManager;
import com.frontier.events.RequestNomads;
import com.frontier.network.FrontierPacketsServer;
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
	
	// optimize territory border rendering
	// display name above npc heads
	// display their current goal below name(?)
	// create blueprint items (warehouse & house)
	// show tier 0 dimensions of structure to player when they try to manually place
	
	// architect:
	// create idle    goal
	// create build   goal
	// create repair  goal
	// create upgrade goal
	
	// allow custom building activation

    @Override
    public void onInitialize()
    {
    	FrontierCalendarManager.initialize();
    	
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

        FrontierPacketsServer.registerServerPacketHandlers();

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