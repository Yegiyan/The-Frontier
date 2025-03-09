package com.frontier.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class StructureInventoryManager
{
	private final Structure structure;

	public StructureInventoryManager(Structure structure)
	{
		this.structure = structure;
	}

	public List<ItemStack> getStructureContents(ServerWorld world)
	{
		List<ItemStack> contents = new ArrayList<>();

		Map<BlockPos, List<ItemStack>> chestContents = getChestContents(world);
		for (List<ItemStack> itemList : chestContents.values())
			contents.addAll(itemList);

		Map<BlockPos, List<ItemStack>> barrelContents = getBarrelContents(world);
		for (List<ItemStack> itemList : barrelContents.values())
			contents.addAll(itemList);

		Map<BlockPos, ItemStack> furnaceOutputs = getFurnaceOutputContents(world);
		contents.addAll(furnaceOutputs.values());

		return contents;
	}

	public Map<BlockPos, List<ItemStack>> getChestContents(ServerWorld world)
	{
		List<BlockPos> chestPositions = findChests(world);
		Map<BlockPos, List<ItemStack>> chestContents = new HashMap<>();
		for (BlockPos chestPos : chestPositions)
		{
			BlockEntity blockEntity = world.getBlockEntity(chestPos);
			if (blockEntity instanceof ChestBlockEntity)
			{
				Inventory inventory = (Inventory) blockEntity;
				List<ItemStack> items = new ArrayList<>();
				for (int i = 0; i < inventory.size(); i++)
				{
					ItemStack itemStack = inventory.getStack(i);
					if (!itemStack.isEmpty())
						items.add(itemStack);
				}
				chestContents.put(chestPos, items);
			}
		}
		return chestContents;
	}

	public Map<BlockPos, List<ItemStack>> getBarrelContents(ServerWorld world)
	{
		List<BlockPos> barrelPositions = findBarrels(world);
		Map<BlockPos, List<ItemStack>> barrelContents = new HashMap<>();
		for (BlockPos barrelPos : barrelPositions)
		{
			BlockEntity blockEntity = world.getBlockEntity(barrelPos);
			if (blockEntity instanceof BarrelBlockEntity)
			{
				Inventory inventory = (Inventory) blockEntity;
				List<ItemStack> items = new ArrayList<>();
				for (int i = 0; i < inventory.size(); i++)
				{
					ItemStack itemStack = inventory.getStack(i);
					if (!itemStack.isEmpty())
						items.add(itemStack);
				}
				barrelContents.put(barrelPos, items);
			}
		}
		return barrelContents;
	}

	public Map<BlockPos, ItemStack> getFurnaceOutputContents(ServerWorld world)
	{
		List<BlockPos> furnacePositions = findFurnaces(world);
		Map<BlockPos, ItemStack> furnaceOutputs = new HashMap<>();
		for (BlockPos furnacePos : furnacePositions)
		{
			BlockEntity blockEntity = world.getBlockEntity(furnacePos);
			if (blockEntity instanceof FurnaceBlockEntity)
			{
				Inventory inventory = (Inventory) blockEntity;
				ItemStack outputStack = inventory.getStack(2); // output for furnace (slot 2)
				if (!outputStack.isEmpty())
					furnaceOutputs.put(furnacePos, outputStack);
			}
		}
		return furnaceOutputs;
	}

	public List<BlockPos> findChests(ServerWorld world)
	{
		List<BlockPos> chestPositions = new ArrayList<>();
		StructureSerializer.processStructure(structure, world, (blockPos, blockState) ->
		{
			if (blockState.isOf(Blocks.CHEST))
				chestPositions.add(blockPos);
		});
		return chestPositions;
	}

	public List<BlockPos> findBarrels(ServerWorld world)
	{
		List<BlockPos> barrelPositions = new ArrayList<>();
		StructureSerializer.processStructure(structure, world, (blockPos, blockState) ->
		{
			if (blockState.isOf(Blocks.BARREL))
				barrelPositions.add(blockPos);
		});
		return barrelPositions;
	}

	public List<BlockPos> findFurnaces(ServerWorld world)
	{
		List<BlockPos> furnacePositions = new ArrayList<>();
		StructureSerializer.processStructure(structure, world, (blockPos, blockState) ->
		{
			if (blockState.isOf(Blocks.FURNACE))
				furnacePositions.add(blockPos);
		});
		return furnacePositions;
	}

	public List<ItemStack> getStructureInventory(ServerWorld world)
	{
		Map<Item, ItemStack> itemMap = new HashMap<>();

		Map<BlockPos, List<ItemStack>> chestContents = getChestContents(world);
		for (List<ItemStack> itemList : chestContents.values())
		{
			for (ItemStack itemStack : itemList)
			{
				itemMap.merge(itemStack.getItem(), itemStack.copy(), (existing, newStack) ->
				{
					existing.increment(newStack.getCount());
					return existing;
				});
			}
		}

		Map<BlockPos, List<ItemStack>> barrelContents = getBarrelContents(world);
		for (List<ItemStack> itemList : barrelContents.values())
		{
			for (ItemStack itemStack : itemList)
			{
				itemMap.merge(itemStack.getItem(), itemStack.copy(), (existing, newStack) ->
				{
					existing.increment(newStack.getCount());
					return existing;
				});
			}
		}

		Map<BlockPos, ItemStack> furnaceOutputs = getFurnaceOutputContents(world);
		for (ItemStack itemStack : furnaceOutputs.values())
		{
			itemMap.merge(itemStack.getItem(), itemStack.copy(), (existing, newStack) ->
			{
				existing.increment(newStack.getCount());
				return existing;
			});
		}

		return new ArrayList<>(itemMap.values());
	}
}