package com.frontier.structures;

import com.frontier.settlements.Settlement;
import com.frontier.settlements.SettlementManager;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public class StructureUpdate
{
	private static final int SECONDS = 2; // update interval in seconds
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
            checkStructuresForDamage(server);
        }
    }

    private void checkStructuresForDamage(MinecraftServer server)
    {
        for (Settlement settlement : SettlementManager.getSettlements().values())
        {
            ServerWorld world = server.getOverworld();
            for (Structure structure : settlement.getStructures())
            {
            	System.out.println(structure.getFurnaceOutputContents(world));
            	
            	if (structure.isDamaged(world))
            	{
            		structure.requiresRepair = true;
            		System.out.println("Structure '" + structure.getName() + "' in settlement '" + settlement.getName() + "' is damaged!");
            	}
            	else
            		structure.requiresRepair = false;
            }
                
        }
    }
}