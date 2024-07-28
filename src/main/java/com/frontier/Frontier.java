package com.frontier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.frontier.calendar.FrontierCalendarManager;
import com.frontier.events.RequestNomads;
import com.frontier.items.FrontierItemGroup;
import com.frontier.items.FrontierItems;
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

	// show tier 0 dimensions of structure to player when they try to manually place
	
	// architect:
	// create idle    goal
	// create build   goal
	// create repair  goal
	// create upgrade goal
	// create choose build location goal
	
	// allow custom building activation

    @Override
    public void onInitialize()
    {
    	FrontierCommands.register();
        FrontierEntities.register();
        FrontierItemGroup.register();
        FrontierManager.registerEvents();
        FrontierPacketsServer.registerHandlers();

        RegionManager.registerCallback();
        SettlementManager.registerCallback();
        RequestNomads.registerCallback();

        FrontierCalendarManager.initialize();
        FrontierItems.initialize();
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