package com.frontier.structures;

import com.frontier.settlements.Settlement;
import com.frontier.settlements.SettlementManager;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public class StructureManager
{
	private static final int SECONDS = 5; // update interval in seconds
    private int tickCounter = 0;

    public void register()
    {
        ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);
    }

    private void onServerTick(MinecraftServer server)
    {
        tickCounter++;
        if (tickCounter >= 20 * SECONDS)
        {
            tickCounter = 0;
            updateActivities(server);
        }
    }

    private void updateActivities(MinecraftServer server)
    {
        for (Settlement settlement : SettlementManager.getSettlements().values())
        {
            ServerWorld world = server.getOverworld();
            for (Structure structure : settlement.getStructures())
            {
            	if (structure.isDamaged(world) && !structure.isUpgrading() && !structure.isConstructing())
            		structure.requiresRepair = true;
            	else
            		structure.requiresRepair = false;
            	
            	structure.update(server.getOverworld());
            }
            
        }
    }
}