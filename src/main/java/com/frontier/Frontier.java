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
	
	// custom placement GUI for each blueprint?
	// ability to choose structure material type?
	
	// 0. don't allow building other structures until a townhall is active
	// 1. hit confirm
	// 2. send required info across the network
	// 3. put structure into queue
	// 4. alert architect of queued buildings
	// 5. architect checks if we have required materials
	// 6. if we don't, alert the player
	// 7. if we do, architect grabs required materials and goes to placement position
	// 8. architect begins placing blocks, looking at the position of the block they're placing as well as walking near it
	// 9. once constructed, architect goes to townhall
	// 10. architect checks for other queued buildings
	// 11. if not he can go idle and wait for STEP 1
	// 12. if we do, we go to STEP 5
	
	// architect:
	// create build   goal
	// create repair  goal
	// create upgrade goal
	// create idle    goal
	// create choose 'random' build location goal
	
	// implement custom structure activation with blueprints/item frames

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