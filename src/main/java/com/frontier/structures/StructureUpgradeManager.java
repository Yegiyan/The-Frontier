package com.frontier.structures;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import com.frontier.Frontier;
import com.frontier.settlements.SettlementManager;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class StructureUpgradeManager
{
	private final Structure structure;

	private boolean isUpgrading;
	private Queue<BlockPos> upgradeQueue = new LinkedList<>();
	private Map<BlockPos, BlockState> upgradeMap = new HashMap<>();

	private static final int BLOCK_PLACE_TICKS = 1;
	private int upgradeTicksElapsed = 0;

	public StructureUpgradeManager(Structure structure)
	{
		this.structure = structure;
		this.isUpgrading = false;
	}

	public void upgradeStructure(ServerWorld world)
	{
		if (structure.getTier() >= structure.getMaxTier())
		{
			Frontier.LOGGER.info(structure.getName() + " in " + structure.getFaction() + " is already at max tier!");
			return;
		}

		int nextTier = structure.getTier() + 1;
		upgradeMap = StructureSerializer.loadStructure(structure.getName(), nextTier, structure.getPosition(),
				structure.getFacing());

		StructureSerializer.processStructure(structure, world, (blockPos, blockState) ->
		{
			if (upgradeMap.containsKey(blockPos))
			{
				Block currentBlock = blockState.getBlock();

				// skip chests & furnaces
				if (!(currentBlock instanceof ChestBlock) && !(currentBlock instanceof FurnaceBlock))
					upgradeQueue.add(blockPos);
			}
		});

		structure.setTier(nextTier);
		structure.loadResourceRequirements();
		isUpgrading = true;
		registerUpgradeTick(world);
	}

	public void upgrade(ServerWorld world)
	{
		if (structure.isConstructed() && upgradeAvailable())
		{
			Frontier.LOGGER.info("Upgrading!");
			upgradeStructure(world);
		}
	}

	protected void registerUpgradeTick(ServerWorld world)
	{
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getOverworld() == world && isUpgrading)
			{
				upgradeTicksElapsed++;

				// process air blocks instantly
				while (!upgradeQueue.isEmpty() && world.getBlockState(upgradeQueue.peek()).isAir())
				{
					BlockPos pos = upgradeQueue.poll();
					BlockState newState = upgradeMap.get(pos);
					world.setBlockState(pos, newState);
				}

				// process non-air blocks
				if (upgradeTicksElapsed >= BLOCK_PLACE_TICKS)
				{
					upgradeTicksElapsed = 0;
					if (!upgradeQueue.isEmpty())
					{
						BlockPos pos = upgradeQueue.poll();
						BlockState newState = upgradeMap.get(pos);
						world.setBlockState(pos, newState);
					}
					else
					{
						isUpgrading = false;
						structure.onUpgrade();
						SettlementManager.saveSettlements(world.getServer());
					}
				}
			}
		});
	}
    
    public boolean upgradeAvailable()
    {
        return structure.getTier() < structure.getMaxTier();
    }
    
    // getters and setters for serialization
    public boolean isUpgrading() {
        return isUpgrading;
    }
    
    public void setUpgrading(boolean isUpgrading) {
        this.isUpgrading = isUpgrading;
    }
    
    public Queue<BlockPos> getUpgradeQueue() {
        return upgradeQueue;
    }
    
    public void setUpgradeQueue(Queue<BlockPos> upgradeQueue) {
        this.upgradeQueue = upgradeQueue;
    }
    
    public Map<BlockPos, BlockState> getUpgradeMap() {
        return upgradeMap;
    }
    
    public void setUpgradeMap(Map<BlockPos, BlockState> upgradeMap) {
        this.upgradeMap = upgradeMap;
    }
    
    public int getUpgradeTicksElapsed() {
        return upgradeTicksElapsed;
    }
    
    public void setUpgradeTicksElapsed(int upgradeTicksElapsed) {
        this.upgradeTicksElapsed = upgradeTicksElapsed;
    }
}