package com.frontier;

import com.frontier.settlements.SettlementManager;
import com.frontier.structures.StructureManager;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class FrontierUpdate
{
	private static final int INTERVAL = 5;
	private static int tickCounter = 0;
	
	public static void worldEvents()
	{
		StructureManager structureManager = new StructureManager();
		structureManager.register();
		
	    ServerTickEvents.END_SERVER_TICK.register(server ->
	    {
	    	tickCounter++;
	        if (tickCounter >= (20 * INTERVAL))
	        {
	        	tickCounter = 0;
	        	
	        	SettlementManager.updatePlayerReputations(server);
	        }
	    });
	}
}