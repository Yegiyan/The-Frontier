package com.frontier.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.frontier.Frontier;
import com.frontier.settlements.Settlement;
import com.frontier.settlements.SettlementManager;

import net.minecraft.block.BellBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
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

	// ignore these properties when comparing block states for repair
	private static final Set<String> IGNORED_PROPERTIES = new HashSet<>();
	static
	{
		IGNORED_PROPERTIES.add("facing");
		IGNORED_PROPERTIES.add("open");
		IGNORED_PROPERTIES.add("powered");
		IGNORED_PROPERTIES.add("lit");
		IGNORED_PROPERTIES.add("enabled");
	}

	public StructureRepairManager(Structure structure)
	{
		this.structure = structure;
		this.requiresRepair = false;
	}

	// checks if the structure is damaged by comparing block states, ignores certain properties for specific block types
	public boolean isDamaged(ServerWorld world)
	{
		final boolean[] isDamaged = { false };

		StructureSerializer.processStructure(structure, world, (blockPos, expectedState) ->
		{
			// skip checking air blocks and dirt path blocks
			if (expectedState.isOf(Blocks.AIR) || expectedState.isOf(Blocks.DIRT_PATH))
				return;

			BlockState currentState = world.getBlockState(blockPos);

			// first, check if block types match
			if (!currentState.getBlock().equals(expectedState.getBlock()))
			{
				isDamaged[0] = true;
				return;
			}

			// for certain block types, we need special property comparison
			if (expectedState.getBlock() instanceof FenceGateBlock || expectedState.getBlock() instanceof DoorBlock || expectedState.getBlock() instanceof BellBlock || expectedState.isOf(Blocks.FURNACE))
			{

				if (!areRelevantPropertiesEqual(currentState, expectedState))
					isDamaged[0] = true;
			}
			
			// for other blocks, do a direct state comparison
			else if (!currentState.equals(expectedState))
				isDamaged[0] = true;
		});

		return isDamaged[0];
	}

	// compare block states, ignoring certain properties defined in IGNORED_PROPERTIES
	private boolean areRelevantPropertiesEqual(BlockState currentState, BlockState expectedState)
	{
		for (Property<?> property : expectedState.getProperties())
		{
			if (IGNORED_PROPERTIES.contains(property.getName()))
				continue;

			if (!currentState.contains(property) || !currentState.get(property).equals(expectedState.get(property)))
				return false;
		}
		return true;
	}

	// detects blocks that need to be repaired, including fence gates and doors
	private Map<BlockPos, BlockState> detectMissingBlocks(ServerWorld world)
	{
		Map<BlockPos, BlockState> missingBlocks = new HashMap<>();

		StructureSerializer.processStructure(structure, world, (blockPos, expectedState) ->
		{
			// ignore air blocks & dirt path
			if (expectedState.isOf(Blocks.AIR) || expectedState.isOf(Blocks.DIRT_PATH))
				return;

			BlockState currentState = world.getBlockState(blockPos);

			// check if block types match
			if (!currentState.getBlock().equals(expectedState.getBlock()))
			{
				missingBlocks.put(blockPos, expectedState);
				return;
			}

			if (expectedState.getBlock() instanceof FenceGateBlock || expectedState.getBlock() instanceof DoorBlock|| expectedState.getBlock() instanceof BellBlock || expectedState.isOf(Blocks.FURNACE))
			{

				if (!areRelevantPropertiesEqual(currentState, expectedState))
				{
					// create new state with the existing directional properties but correct other properties
					BlockState newState = preserveDirectionalProperties(currentState, expectedState);
					missingBlocks.put(blockPos, newState);
				}
			}
			
			// for regular blocks
			else if (!currentState.equals(expectedState))
				missingBlocks.put(blockPos, expectedState);
		});

		return missingBlocks;
	}

	// creates a blockstate that preserves directional properties from the current state but takes all other properties from the expected state
	private BlockState preserveDirectionalProperties(BlockState currentState, BlockState expectedState)
	{
		BlockState newState = expectedState;

		// copy directional properties from current state if they exist
		for (Property<?> property : currentState.getProperties())
			if (IGNORED_PROPERTIES.contains(property.getName()) && expectedState.contains(property))
				newState = copyProperty(currentState, newState, property);

		return newState;
	}


	private <T extends Comparable<T>> BlockState copyProperty(BlockState from, BlockState to, Property<T> property)
	{
		return to.with((Property<T>) property, from.get((Property<T>) property));
	}

	// checks if repair can be performed based on available resources
	public boolean canRepair(ServerWorld world)
	{
		if (!requiresRepair)
			return false;

		// step 1: detect missing blocks
		Map<BlockPos, BlockState> missingBlocks = detectMissingBlocks(world);

		if (missingBlocks.isEmpty())
		{
			requiresRepair = false;
			return true;
		}

		// step 2: check for required blocks in settlement storage
		Map<Block, Integer> requiredBlocks = calculateRequiredBlocks(missingBlocks);
		Map<Item, Integer> availableItems = getAvailableResources(world);

		// check if we have all required resources
		for (Map.Entry<Block, Integer> entry : requiredBlocks.entrySet())
		{
			Block requiredBlock = entry.getKey();
			int requiredCount = entry.getValue();

			// find matching item for this block
			boolean resourceFound = false;

			for (Map.Entry<Item, Integer> itemEntry : availableItems.entrySet())
			{
				Item item = itemEntry.getKey();

				// check if item is a BlockItem that can place our required block
				if (item instanceof BlockItem && ((BlockItem) item).getBlock().equals(requiredBlock))
				{
					int availableCount = itemEntry.getValue();
					if (availableCount < requiredCount)
					{
						Frontier.LOGGER.info("Not enough resources for repair: have " + availableCount + " of " + item.getTranslationKey() + ", need " + requiredCount);
						return false;
					}
					resourceFound = true;
					break;
				}
			}

			if (!resourceFound)
			{
				// Frontier.LOGGER.info("Missing resources for repair: " + requiredBlock.getTranslationKey());
				return false;
			}
		}

		return true;
	}

	// calculates blocks required for repair based on missing blocks
	private Map<Block, Integer> calculateRequiredBlocks(Map<BlockPos, BlockState> missingBlocks)
	{
		Map<Block, Integer> requiredBlocks = new HashMap<>();

		for (BlockState state : missingBlocks.values())
		{
			Block block = state.getBlock();
			requiredBlocks.put(block, requiredBlocks.getOrDefault(block, 0) + 1);
		}

		return requiredBlocks;
	}

	// gets all available resources from storage containers in the settlement
	private Map<Item, Integer> getAvailableResources(ServerWorld world)
	{
		Map<Item, Integer> availableResources = new HashMap<>();
		Settlement settlement = SettlementManager.getSettlement(structure.getFaction());

		if (settlement == null)
		{
			return availableResources;
		}

		// check all townhalls and warehouses
		for (Structure structureInSettlement : settlement.getStructures())
		{
			if (structureInSettlement.getType().equals(StructureType.TOWNHALL) || structureInSettlement.getType().equals(StructureType.WAREHOUSE))
			{

				StructureInventoryManager inventoryManager = structureInSettlement.getInventoryManager();

				// get all inventories
				addResourcesFromInventory(availableResources, inventoryManager.getChestContents(world));
				addResourcesFromInventory(availableResources, inventoryManager.getBarrelContents(world));

				// add furnace outputs
				for (ItemStack item : inventoryManager.getFurnaceOutputContents(world).values())
				{
					Item itemType = item.getItem();
					availableResources.put(itemType, availableResources.getOrDefault(itemType, 0) + item.getCount());
				}
			}
		}

		return availableResources;
	}

	private void addResourcesFromInventory(Map<Item, Integer> resources, Map<BlockPos, List<ItemStack>> containers)
	{
		for (List<ItemStack> items : containers.values())
		{
			for (ItemStack item : items)
			{
				Item itemType = item.getItem();
				resources.put(itemType, resources.getOrDefault(itemType, 0) + item.getCount());
			}
		}
	}

	// initiates structure repair, consuming resources and setting up repair queue
	public void repairStructure(ServerWorld world)
	{
		if (!canRepair(world))
		{
			//Frontier.LOGGER.info("Cannot repair " + structure.getName() + ": Insufficient resources or no damage detected");
			return;
		}

		Map<BlockPos, BlockState> missingBlocks = detectMissingBlocks(world);
		if (missingBlocks.isEmpty())
		{
			requiresRepair = false;
			return;
		}

		// calculate resources needed for repair
		Map<Block, Integer> requiredBlocks = calculateRequiredBlocks(missingBlocks);

		// consume resources from storage
		if (!consumeResources(world, requiredBlocks))
		{
			//Frontier.LOGGER.error("Failed to consume resources for repair of " + structure.getName());
			return;
		}

		// Set up repair queue
		repairQueue.clear();
		repairQueue.addAll(missingBlocks.entrySet());
		repairTicksElapsed = 0;
		requiresRepair = true;

		//Frontier.LOGGER.info("Started repair of " + structure.getName() + " - blocks to repair: " + repairQueue.size());
	}

	// consumes resources required for repair from settlement storage
	private boolean consumeResources(ServerWorld world, Map<Block, Integer> requiredBlocks)
	{
		Settlement settlement = SettlementManager.getSettlement(structure.getFaction());
		if (settlement == null)
			return false;

		// track structures with storage
		List<Structure> storageStructures = new ArrayList<>();
		for (Structure settlementStructure : settlement.getStructures())
		{
			if (settlementStructure.getType().equals(StructureType.TOWNHALL) || settlementStructure.getType().equals(StructureType.WAREHOUSE))
			{
				storageStructures.add(settlementStructure);
			}
		}

		// for each required block type
		for (Map.Entry<Block, Integer> entry : requiredBlocks.entrySet())
		{
			Block requiredBlock = entry.getKey();
			int requiredCount = entry.getValue();

			if (!consumeSpecificResource(world, storageStructures, requiredBlock, requiredCount))
			{
				//Frontier.LOGGER.error("Failed to consume required resource: " + requiredBlock.getTranslationKey());
				return false;
			}
		}

		return true;
	}

	// consumes a specific resource type from storage containers
	private boolean consumeSpecificResource(ServerWorld world, List<Structure> structures, Block requiredBlock, int requiredCount)
	{
		int remainingToConsume = requiredCount;

		// try each storage structure
		for (Structure structure : structures)
		{
			if (remainingToConsume <= 0)
				break;

			StructureInventoryManager inventoryManager = structure.getInventoryManager();

			// try chests first
			Map<BlockPos, List<ItemStack>> chestContents = new HashMap<>(inventoryManager.getChestContents(world));
			remainingToConsume = consumeFromContainers(world, chestContents, requiredBlock, remainingToConsume);

			if (remainingToConsume <= 0)
				continue;

			// try barrels next
			Map<BlockPos, List<ItemStack>> barrelContents = new HashMap<>(inventoryManager.getBarrelContents(world));
			remainingToConsume = consumeFromContainers(world, barrelContents, requiredBlock, remainingToConsume);

			if (remainingToConsume <= 0)
				continue;

			// try furnace outputs last
			Map<BlockPos, ItemStack> furnaceOutputs = new HashMap<>(inventoryManager.getFurnaceOutputContents(world));
			remainingToConsume = consumeFromFurnaces(world, furnaceOutputs, requiredBlock, remainingToConsume);
		}

		return remainingToConsume <= 0;
	}

	
	// consumes resources from container inventories (chests, barrels)
	private int consumeFromContainers(ServerWorld world, Map<BlockPos, List<ItemStack>> containers, Block requiredBlock, int requiredCount)
	{
		for (Map.Entry<BlockPos, List<ItemStack>> entry : containers.entrySet())
		{
			BlockPos containerPos = entry.getKey();
			List<ItemStack> items = entry.getValue();

			Iterator<ItemStack> iterator = items.iterator();
			while (iterator.hasNext() && requiredCount > 0)
			{
				ItemStack stack = iterator.next();

				// check if this item can place our required block
				if (stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock().equals(requiredBlock))
				{

					int toConsume = Math.min(stack.getCount(), requiredCount);
					stack.decrement(toConsume);
					requiredCount -= toConsume;

					// update the inventory in the world
					if (world.getBlockEntity(containerPos) instanceof net.minecraft.inventory.Inventory)
					{
						net.minecraft.inventory.Inventory inventory = (net.minecraft.inventory.Inventory) world.getBlockEntity(containerPos);
						for (int i = 0; i < inventory.size(); i++)
						{
							ItemStack invStack = inventory.getStack(i);
							if (invStack.getItem() == stack.getItem())
							{
								if (stack.isEmpty())
									inventory.setStack(i, ItemStack.EMPTY);
								else if (invStack.getCount() > stack.getCount())
									invStack.setCount(stack.getCount());
								break;
							}
						}
					}

					if (stack.isEmpty())
						iterator.remove();

					if (requiredCount <= 0)
						break;
				}
			}

			if (requiredCount <= 0)
				break;
		}

		return requiredCount;
	}

	// consumes resources from furnace outputs
	private int consumeFromFurnaces(ServerWorld world, Map<BlockPos, ItemStack> furnaces, Block requiredBlock, int requiredCount)
	{
		for (Iterator<Map.Entry<BlockPos, ItemStack>> it = furnaces.entrySet().iterator(); it.hasNext() && requiredCount > 0;)
		{

			Map.Entry<BlockPos, ItemStack> entry = it.next();
			BlockPos furnacePos = entry.getKey();
			ItemStack stack = entry.getValue();

			// check if this item can place our required block
			if (stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock().equals(requiredBlock))
			{

				int toConsume = Math.min(stack.getCount(), requiredCount);
				stack.decrement(toConsume);
				requiredCount -= toConsume;

				// update the furnace in the world
				if (world.getBlockEntity(furnacePos) instanceof net.minecraft.inventory.Inventory)
				{
					net.minecraft.inventory.Inventory inventory = (net.minecraft.inventory.Inventory) world.getBlockEntity(furnacePos);
					ItemStack outputSlot = inventory.getStack(2); // furnace output slot 2

					if (outputSlot.getItem() == stack.getItem())
					{
						if (stack.isEmpty())
						{
							inventory.setStack(2, ItemStack.EMPTY);
						}
						else
						{
							outputSlot.setCount(stack.getCount());
						}
					}
				}

				if (stack.isEmpty())
					it.remove();

				if (requiredCount <= 0)
					break;
			}
		}

		return requiredCount;
	}

	public void processTick(ServerWorld world)
	{
		if (!requiresRepair || repairQueue.isEmpty())
			return;

		repairTicksElapsed++;

		if (repairTicksElapsed >= BLOCK_REPAIR_TICKS)
		{
			repairTicksElapsed = 0;

			if (!repairQueue.isEmpty())
			{
				Map.Entry<BlockPos, BlockState> entry = repairQueue.poll();
				world.setBlockState(entry.getKey(), entry.getValue());

				// log repair progress
				//if (repairQueue.size() % 5 == 0 || repairQueue.isEmpty())
					//Frontier.LOGGER.info(structure.getName() + " repair progress: " + (repairQueue.isEmpty() ? "Complete" : repairQueue.size() + " blocks remaining"));

				if (repairQueue.isEmpty())
				{
					requiresRepair = false;
					//Frontier.LOGGER.info(structure.getName() + " repair complete!");
					SettlementManager.saveSettlements(world.getServer());
				}
			}
		}
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