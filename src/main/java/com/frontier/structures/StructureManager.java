package com.frontier.structures;

import com.frontier.Frontier;
import com.frontier.settlements.Settlement;
import com.frontier.settlements.SettlementManager;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public class StructureManager
{
	private static final int SECONDS = 1; // update interval in seconds
	private int tickCounter = 0;

	public void register()
	{
		ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);
		Frontier.LOGGER.info("StructureManager registered for tick events");
	}

	private void onServerTick(MinecraftServer server)
	{
		tickCounter++;
		if (tickCounter >= 1 * SECONDS)
		{
			tickCounter = 0;
			updateActivities(server);
		}
	}

	private void updateActivities(MinecraftServer server)
	{
		ServerWorld world = server.getOverworld();
		int totalSettlements = SettlementManager.getSettlements().size();
		
		Frontier.LOGGER.debug("StructureManager updating " + totalSettlements + " settlements");
		
		for (Settlement settlement : SettlementManager.getSettlements().values())
		{
			// auto flag damaged structures for repair
			for (Structure structure : settlement.getStructures())
			{
				if (structure.isDamaged(world) && !structure.isUpgrading() && !structure.isConstructing() && !structure.requiresRepair())
				{
					Frontier.LOGGER.info("Structure " + structure.getName() + " is damaged, flagging for repair");
					structure.getRepairManager().setRepair(true);
				}
			}
			
			// process construction ticks
			for (Structure structure : settlement.getStructures())
			{
				if (structure.isConstructing())
				{
					structure.getConstructionManager().processTick(world);
				}
			}
			
			// process upgrade ticks
			for (Structure structure : settlement.getStructures())
			{
				if (structure.isUpgrading())
				{
					structure.getUpgradeManager().processTick(world);
				}
			}
			
			// process repair ticks and auto-repair if resources are available
			for (Structure structure : settlement.getStructures())
			{
				if (structure.requiresRepair() && !structure.isUpgrading() && !structure.isConstructing())
				{
					// start repairs if resources are available and repair isn't already in progress
					if (structure.getRepairManager().getRepairQueue().isEmpty() && structure.getRepairManager().canRepair(world))
					{
						Frontier.LOGGER.info("Starting repairs on structure: " + structure.getName());
						structure.getRepairManager().repairStructure(world);
					}
					
					// process ongoing repairs
					structure.getRepairManager().processTick(world);
				}
			}

			// update all structures
			for (Structure structure : settlement.getStructures())
				structure.update(world);
		}
	}
}