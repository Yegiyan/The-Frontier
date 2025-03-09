package com.frontier.structures;

import com.frontier.Frontier;
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

			// auto flag damaged structures for repair
			for (Structure structure : settlement.getStructures())
				if (structure.isDamaged(world) && !structure.isUpgrading() && !structure.isConstructing())
					structure.getRepairManager().setRepair(true);
			
			// auto repairs if resources are available
			for (Structure structure : settlement.getStructures())
			{
				if (structure.requiresRepair() && !structure.isUpgrading() && !structure.isConstructing())
				{
					Frontier.LOGGER.info("Attempting to repair structure: " + structure.getName());
					if (structure.getRepairManager().canRepair(world))
					{
						Frontier.LOGGER.info("Repairing structure: " + structure.getName());
						structure.getRepairManager().repairStructure(world);
					}
					else
					{
						Frontier.LOGGER.info("Cannot repair structure yet: " + structure.getName() + " (insufficient resources)");
					}
				}
			}

			// update all structures
			for (Structure structure : settlement.getStructures())
				structure.update(world);
		}
	}

	// add more global structure management methods here
	// for example, to find structures of certain types across settlements or to apply global effects to structures
}