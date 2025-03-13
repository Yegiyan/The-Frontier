package com.frontier.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.frontier.Frontier;
import com.frontier.settlements.Settlement;
import com.frontier.settlements.SettlementManager;

import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;

public class StructureRepairManager
{
	private final Structure structure;

	private boolean requiresRepair;
	private Queue<Map.Entry<BlockPos, BlockState>> repairQueue = new LinkedList<>();

	private static final int BLOCK_REPAIR_TICKS = 20;
	private int repairTicksElapsed = 0;

	public StructureRepairManager(Structure structure)
	{
		this.structure = structure;
		this.requiresRepair = false;
	}

	public boolean isDamaged(ServerWorld world)
	{
		final boolean[] isDamaged = { false };
		StructureSerializer.processStructure(structure, world, (blockPos, expectedState) ->
		{
			if (expectedState.isOf(Blocks.AIR)) // ignore air blocks
				return;

			BlockState currentState = world.getBlockState(blockPos);

			// check furnace block type and ignore active state property
			if (expectedState.isOf(Blocks.FURNACE))
			{
				if (currentState.isOf(Blocks.FURNACE))
					return;
				else
				{
					isDamaged[0] = true;
					return;
				}
			}

			// ignore direction of bells
			if (expectedState.getBlock() instanceof BellBlock && currentState.getBlock() instanceof BellBlock)
			{
				// check all properties except 'facing' for bells
				for (Property<?> property : expectedState.getProperties())
				{
					if (!property.getName().equals("facing") && !currentState.get(property).equals(expectedState.get(property)))
					{
						isDamaged[0] = true;
						break;
					}
				}
			}
			else if (!currentState.equals(expectedState))
				isDamaged[0] = true;
		});
		return isDamaged[0];
	}

	public boolean canRepair(ServerWorld world)
	{
	    if (!requiresRepair)
	        return false;

	    // step 1: detect missing blocks
	    Map<BlockPos, BlockState> missingBlocks = detectMissingBlocks(world);

	    if (missingBlocks.isEmpty())
	        return true;

	    // step 2: check chests and furnaces for required blocks
	    Settlement settlement = SettlementManager.getSettlement(structure.getFaction());
	    
	    // create a map to track all available resources across all containers
	    Map<String, Integer> availableResources = new HashMap<>();

	    // collect resources from all valid structures
	    for (Structure structureInSettlement : settlement.getStructures())
	    {
	        if (structureInSettlement.getType().equals(StructureType.TOWNHALL) || structureInSettlement.getType().equals(StructureType.WAREHOUSE))
	        {
	            StructureInventoryManager inventoryManager = structureInSettlement.getInventoryManager();
	            Map<BlockPos, List<ItemStack>> chestContents = inventoryManager.getChestContents(world);
	            Map<BlockPos, List<ItemStack>> barrelContents = inventoryManager.getBarrelContents(world);
	            Map<BlockPos, ItemStack> furnaceOutputs = inventoryManager.getFurnaceOutputContents(world);

	            // aggregate resources from chests
	            for (List<ItemStack> items : chestContents.values())
	            {
	                for (ItemStack item : items)
	                {
	                    String itemName = item.getItem().getTranslationKey();
	                    availableResources.put(itemName, availableResources.getOrDefault(itemName, 0) + item.getCount());
	                }
	            }

	            // aggregate resources from barrels
	            for (List<ItemStack> items : barrelContents.values())
	            {
	                for (ItemStack item : items)
	                {
	                    String itemName = item.getItem().getTranslationKey();
	                    availableResources.put(itemName, availableResources.getOrDefault(itemName, 0) + item.getCount());
	                }
	            }

	            // aggregate resources from furnace outputs
	            for (ItemStack item : furnaceOutputs.values())
	            {
	                String itemName = item.getItem().getTranslationKey();
	                availableResources.put(itemName, availableResources.getOrDefault(itemName, 0) + item.getCount());
	            }
	        }
	    }

	    // create a map to track required resources
	    Map<String, Integer> requiredResources = new HashMap<>();
	    
	    // calculate total resources needed
	    for (Map.Entry<BlockPos, BlockState> entry : missingBlocks.entrySet())
	    {
	        String blockName = entry.getValue().getBlock().getTranslationKey();
	        requiredResources.put(blockName, requiredResources.getOrDefault(blockName, 0) + 1);
	    }

	    // check if there are enough resources
	    for (Map.Entry<String, Integer> resource : requiredResources.entrySet())
	    {
	        String blockName = resource.getKey();
	        int requiredCount = resource.getValue();

	        if (!availableResources.containsKey(blockName) || availableResources.get(blockName) < requiredCount)
	            return false; // not enough resources
	    }

	    return true; // all required resources available
	}

	public void repairStructure(ServerWorld world)
	{
	    if (!canRepair(world))
	    {
	        Frontier.LOGGER.info("Cannot repair " + structure.getName() + ": Not enough resources!");
	        return;
	    }

	    Map<BlockPos, BlockState> missingBlocks = detectMissingBlocks(world);
	    Settlement settlement = SettlementManager.getSettlement(structure.getFaction());
	    
	    Map<String, Integer> requiredResources = new HashMap<>();
	    
	    // calculate total resources needed
	    for (Map.Entry<BlockPos, BlockState> entry : missingBlocks.entrySet())
	    {
	        String blockName = entry.getValue().getBlock().getTranslationKey();
	        requiredResources.put(blockName, requiredResources.getOrDefault(blockName, 0) + 1);
	    }
	    
	    // track which containers to check and in what order
	    List<Structure> structuresToCheck = new ArrayList<>();
	    
	    // prioritize townhall and warehouse structures
	    for (Structure settlementStructure : settlement.getStructures())
	        if (settlementStructure.getType().equals(StructureType.TOWNHALL) || settlementStructure.getType().equals(StructureType.WAREHOUSE))
	            structuresToCheck.add(settlementStructure);
	    
	    // remove resources from containers
	    for (String blockName : requiredResources.keySet())
	    {
	        int remainingToRemove = requiredResources.get(blockName);
	        
	        // try to remove from each structure until we've removed enough
	        for (Structure structureInSettlement : structuresToCheck)
	        {
	            if (remainingToRemove <= 0)
	            	break;
	            
	            StructureInventoryManager inventoryManager = structureInSettlement.getInventoryManager();
	            Map<BlockPos, List<ItemStack>> chestContents = inventoryManager.getChestContents(world);
	            
	            // remove from chests
	            remainingToRemove = removeResourcesFromChests(chestContents, blockName, remainingToRemove);
	            
	            if (remainingToRemove <= 0)
	            	continue;
	            
	            // remove from barrels if we still need more
	            Map<BlockPos, List<ItemStack>> barrelContents = inventoryManager.getBarrelContents(world);
	            remainingToRemove = removeResourcesFromBarrels(barrelContents, blockName, remainingToRemove);
	            
	            if (remainingToRemove <= 0)
	            	continue;
	            
	            // remove from furnace outputs if we still need more
	            Map<BlockPos, ItemStack> furnaceOutputs = inventoryManager.getFurnaceOutputContents(world);
	            remainingToRemove = removeResourcesFromFurnaceOutputs(furnaceOutputs, blockName, remainingToRemove);
	        }
	        
	        if (remainingToRemove > 0)
	        {
	            Frontier.LOGGER.info("Cannot repair " + structure.getName() + ": Not enough of " + blockName + " found!");
	            return;
	        }
	    }

	    repairQueue.addAll(missingBlocks.entrySet());
	    repairTicksElapsed = 0;
	}

	public void processTick(ServerWorld world)
	{
		if (!requiresRepair || repairQueue.isEmpty()) return;
		
		repairTicksElapsed++;

		if (repairTicksElapsed >= BLOCK_REPAIR_TICKS)
		{
			repairTicksElapsed = 0;

			if (!repairQueue.isEmpty())
			{
				Map.Entry<BlockPos, BlockState> entry = repairQueue.poll();
				world.setBlockState(entry.getKey(), entry.getValue());

				if (repairQueue.isEmpty())
				{
					requiresRepair = false;
					Frontier.LOGGER.info(structure.getName() + " repaired!");
					SettlementManager.saveSettlements(world.getServer());
				}
			}
		}
	}

	private Map<BlockPos, BlockState> detectMissingBlocks(ServerWorld world)
	{
		Map<BlockPos, BlockState> missingBlocks = new HashMap<>();

		StructureSerializer.processStructure(structure, world, (blockPos, expectedState) ->
		{
			BlockState currentState = world.getBlockState(blockPos);
			if (!currentState.equals(expectedState))
				missingBlocks.put(blockPos, expectedState);
		});

		return missingBlocks;
	}

	private int removeResourcesFromChests(Map<BlockPos, List<ItemStack>> inventory, String blockName, int requiredCount)
	{
		for (List<ItemStack> items : inventory.values())
		{
			Iterator<ItemStack> iterator = items.iterator();
			while (iterator.hasNext())
			{
				ItemStack item = iterator.next();
				if (item.getItem().getTranslationKey().equals(blockName))
				{
					int availableCount = item.getCount();
					int toRemove = Math.min(availableCount, requiredCount);
					item.decrement(toRemove);
					requiredCount -= toRemove;

					if (item.isEmpty())
						iterator.remove();

					if (requiredCount <= 0)
						return 0; // all required resources removed
				}
			}
		}
		return requiredCount; // return remaining count
	}

	private int removeResourcesFromBarrels(Map<BlockPos, List<ItemStack>> inventory, String blockName, int requiredCount)
	{
		for (List<ItemStack> items : inventory.values())
		{
			Iterator<ItemStack> iterator = items.iterator();
			while (iterator.hasNext())
			{
				ItemStack item = iterator.next();
				if (item.getItem().getTranslationKey().equals(blockName))
				{
					int availableCount = item.getCount();
					int toRemove = Math.min(availableCount, requiredCount);
					item.decrement(toRemove);
					requiredCount -= toRemove;

					if (item.isEmpty())
						iterator.remove();

					if (requiredCount <= 0)
						return 0; // all required resources removed
				}
			}
		}
		return requiredCount; // return remaining count
	}

	private int removeResourcesFromFurnaceOutputs(Map<BlockPos, ItemStack> furnaceOutputs, String blockName, int requiredCount)
	{
		for (Iterator<Map.Entry<BlockPos, ItemStack>> it = furnaceOutputs.entrySet().iterator(); it.hasNext();)
		{
			Map.Entry<BlockPos, ItemStack> entry = it.next();
			ItemStack item = entry.getValue();
			if (item.getItem().getTranslationKey().equals(blockName))
			{
				int availableCount = item.getCount();
				int toRemove = Math.min(availableCount, requiredCount);
				item.decrement(toRemove);
				requiredCount -= toRemove;

				if (item.isEmpty())
					it.remove();

				if (requiredCount <= 0)
					return 0; // all required resources removed
			}
		}
		return requiredCount; // return remaining count
	}
    
    public boolean requiresRepair() {
        return requiresRepair;
    }
    
    public void setRepair(boolean requiresRepair) {
        this.requiresRepair = requiresRepair;
    }
    
    public Queue<Map.Entry<BlockPos, BlockState>> getRepairQueue() {
        return repairQueue;
    }
    
    public void setRepairQueue(Queue<Map.Entry<BlockPos, BlockState>> repairQueue) {
        this.repairQueue = repairQueue;
    }
    
    public int getRepairTicksElapsed() {
        return repairTicksElapsed;
    }
    
    public void setRepairTicksElapsed(int repairTicksElapsed) {
        this.repairTicksElapsed = repairTicksElapsed;
    }
}