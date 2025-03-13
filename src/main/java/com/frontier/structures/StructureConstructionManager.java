package com.frontier.structures;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.frontier.Frontier;
import com.frontier.settlements.SettlementManager;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

public class StructureConstructionManager
{
	private final Structure structure;

	private boolean canConstruct;
	private boolean isConstructing;
	private boolean isClearing;

	private Queue<BlockPos> airBlocksQueue = new LinkedList<>();
	private Queue<BlockPos> nonAirBlocksQueue = new LinkedList<>();
	private Queue<BlockPos> clearingQueue = new LinkedList<>();
	private Map<BlockPos, BlockState> constructionMap = new HashMap<>();

	private static final int BLOCK_PLACE_TICKS = 1;
	private static final int BLOCK_CLEAR_TICKS = 1;
	private int constructionTicksElapsed = 0;

	private static final Set<Block> GROUND_BLOCKS = Set.of(Blocks.GRASS_BLOCK, Blocks.SAND, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.STONE);
	private static final Set<Block> INVALID_BLOCKS = Set.of(Blocks.WATER, Blocks.LAVA);

	public StructureConstructionManager(Structure structure)
	{
		this.structure = structure;
		this.canConstruct = true;
		this.isConstructing = false;
		this.isClearing = true;
	}

	public void constructStructure(ServerWorld world)
	{
	    this.isConstructing = true;
	    
	    BlockPos originalPos = structure.getPosition();
	    BlockPos adjustedPos = adjustToGround(world, originalPos);
	    
	    if (!originalPos.equals(adjustedPos))
	        structure.setPosition(adjustedPos);

	    if (canConstruct)
	    {
	        prepareClearingQueue(world);
	        prepareConstructionQueue(world);
	    }
	    
	    else
	    {
	        Frontier.LOGGER.warn("Cannot construct structure at position: " + adjustedPos + " (canConstruct=false)");
	        this.isConstructing = false;
	    }
	}

	public void spawnStructure(ServerWorld world)
	{
		this.isConstructing = true;
		BlockPos adjustedPos = adjustToGround(world, structure.getPosition());
		structure.setPosition(adjustedPos);

		if (canConstruct)
		{
			StructureSerializer.processStructure(structure, world, (blockPos, blockState) -> world.setBlockState(blockPos, blockState));
			isConstructing = false;
			structure.setConstructed(true);
			structure.onConstruction(world);
		}
		
		SettlementManager.saveSettlements(world.getServer());
	}

	private BlockPos adjustToGround(ServerWorld world, BlockPos originalPosition)
	{
		int x = originalPosition.getX();
		int z = originalPosition.getZ();
		int groundY = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
		BlockPos groundPos = originalPosition;

		while (groundY > world.getBottomY())
		{
			BlockPos pos = new BlockPos(x, groundY, z);
			Block block = world.getBlockState(pos).getBlock();
			if (GROUND_BLOCKS.contains(block))
			{
				groundPos = pos.up();
				break; // return position just above the ground block
			}
			
			else if (INVALID_BLOCKS.contains(block))
			{
				this.canConstruct = false;
				return originalPosition; // return original position if an invalid block is found
			}
			groundY--;
		}

		// check if more than 2/3rds of the blocks one level below ground level are air
		int airBlockCount = 0;
		int totalBlockCount = 0;
		for (int dx = -structure.getLength() / 2; dx <= structure.getLength() / 2; dx++)
		{
			for (int dz = -structure.getWidth() / 2; dz <= structure.getWidth() / 2; dz++)
			{
				BlockPos checkPos = groundPos.add(dx, -1, dz); // check one level below ground position
				totalBlockCount++;
				if (world.getBlockState(checkPos).isAir())
					airBlockCount++;
			}
		}

		if (airBlockCount > (2.0 / 3.0) * totalBlockCount)
		{
			this.canConstruct = false;
			return originalPosition; // too many air blocks, invalid position
		}

		this.canConstruct = true;
		return groundPos;
	}

	public void prepareClearingQueue(ServerWorld world)
	{
		clearingQueue.clear();
		StructureSerializer.processStructure(structure, world, (blockPos, blockState) -> clearingQueue.add(blockPos));
	}

	public void prepareConstructionQueue(ServerWorld world)
	{
		airBlocksQueue.clear();
		nonAirBlocksQueue.clear();
		constructionMap.clear();
		
		StructureSerializer.processStructure(structure, world, (blockPos, blockState) ->
		{
			if (blockState.isOf(Blocks.AIR))
				airBlocksQueue.add(blockPos);
			else
				nonAirBlocksQueue.add(blockPos);
			constructionMap.put(blockPos, blockState);
		});
	}

	public void processTick(ServerWorld world) 
	{
		if (!isConstructing) return;
		
		constructionTicksElapsed++;
		if (isClearing)
		{
			// clear air blocks instantly
			while (!clearingQueue.isEmpty() && world.getBlockState(clearingQueue.peek()).isAir())
			{
				BlockPos pos = clearingQueue.poll();
				world.setBlockState(pos, Blocks.AIR.getDefaultState());
			}

			// clear non-air blocks
			if (constructionTicksElapsed >= BLOCK_CLEAR_TICKS)
			{
				constructionTicksElapsed = 0;
				if (!clearingQueue.isEmpty())
				{
					BlockPos pos = clearingQueue.poll();
					world.breakBlock(pos, true);
				}
				
				else
				{
					isClearing = false;
					constructionTicksElapsed = 0; // reset tick counter for construction phase
				}
			}
		}
		else
		{
			// place air blocks instantly
			while (!airBlocksQueue.isEmpty() && constructionMap.get(airBlocksQueue.peek()).isAir())
			{
				BlockPos pos = airBlocksQueue.poll();
				BlockState state = constructionMap.get(pos);
				world.setBlockState(pos, state);
			}

			// place non-air blocks
			if (constructionTicksElapsed >= BLOCK_PLACE_TICKS)
			{
				constructionTicksElapsed = 0;
				if (!nonAirBlocksQueue.isEmpty())
				{
					BlockPos pos = nonAirBlocksQueue.poll();
					BlockState state = constructionMap.get(pos);
					world.setBlockState(pos, state);
				}
				
				else if (airBlocksQueue.isEmpty())
				{
					this.isConstructing = false;
					structure.setConstructed(true);
					structure.onConstruction(world);
					SettlementManager.saveSettlements(world.getServer());
				}
			}
		}
	}

    public boolean canConstruct() {
        return canConstruct;
    }
    
    public boolean isConstructing() {
        return isConstructing;
    }
    
    public void setConstructing(boolean isConstructing) {
        this.isConstructing = isConstructing;
    }
    
    public boolean isClearing() {
        return isClearing;
    }
    
    public void setClearing(boolean isClearing) {
        this.isClearing = isClearing;
    }
    
    public Queue<BlockPos> getAirBlocksQueue() {
        return airBlocksQueue;
    }
    
    public void setAirBlocksQueue(Queue<BlockPos> airBlocksQueue) {
        this.airBlocksQueue = airBlocksQueue;
    }
    
    public Queue<BlockPos> getNonAirBlocksQueue() {
        return nonAirBlocksQueue;
    }
    
    public void setNonAirBlocksQueue(Queue<BlockPos> nonAirBlocksQueue) {
        this.nonAirBlocksQueue = nonAirBlocksQueue;
    }
    
    public Queue<BlockPos> getClearingQueue() {
        return clearingQueue;
    }
    
    public void setClearingQueue(Queue<BlockPos> clearingQueue) {
        this.clearingQueue = clearingQueue;
    }
    
    public Map<BlockPos, BlockState> getConstructionMap() {
        return constructionMap;
    }
    
    public void setConstructionMap(Map<BlockPos, BlockState> constructionMap) {
        this.constructionMap = constructionMap;
    }
    
    public int getConstructionTicksElapsed() {
        return constructionTicksElapsed;
    }
    
    public void setConstructionTicksElapsed(int constructionTicksElapsed) {
        this.constructionTicksElapsed = constructionTicksElapsed;
    }
}