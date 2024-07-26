package com.frontier;

import com.frontier.settlements.SettlementManager;
import com.frontier.structures.StructureManager;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class FrontierUpdate
{
	// intervals in seconds
	private static final int playerRepInterval = 5;
	private static int repTickCounter = 0;
	
	public static void worldEvents()
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
	    });
	}
}
