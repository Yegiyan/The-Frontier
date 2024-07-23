package com.frontier.structures;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

import com.frontier.Frontier;
import com.frontier.settlements.Settlement;
import com.frontier.settlements.SettlementManager;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BellBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;

public abstract class Structure
{
	public enum StructureType { CORE, GATHERING, LABORING, CRAFTING, RANCHING, MILITARY, VISTING, MISC }
	
	protected String name;
	protected String faction;
	protected StructureType type;
	protected BlockPos position;
	protected Direction facing;
	protected int tier;
	protected int maxTier;
	protected UUID uuid;
	
	protected boolean isActive;
	protected boolean canConstruct;
	protected boolean isConstructed;
	protected boolean isConstructing;
	protected boolean requiresRepair;
	protected boolean isUpgrading;
	protected boolean isClearing;

	protected Map<String, Integer> resourceRequirements;
	
	private Queue<BlockPos> airBlocksQueue = new LinkedList<>();
	private Queue<BlockPos> nonAirBlocksQueue = new LinkedList<>();
	private Queue<BlockPos> clearingQueue = new LinkedList<>();
	private Queue<BlockPos> upgradeQueue = new LinkedList<>();
	
	private Queue<Map.Entry<BlockPos, BlockState>> repairQueue = new LinkedList<>();
	private Map<BlockPos, BlockState> upgradeMap = new HashMap<>();
	private Map<BlockPos, BlockState> constructionMap = new HashMap<>();
    
	private static final Set<Block> GROUND_BLOCKS = Set.of(
	        Blocks.GRASS_BLOCK, 
	        Blocks.SAND, 
	        Blocks.DIRT, 
	        Blocks.COARSE_DIRT, 
	        Blocks.STONE
	    );
	
	private static final Set<Block> INVALID_BLOCKS = Set.of(
	        Blocks.WATER,
	        Blocks.LAVA
	    );
	
	private static final int BLOCK_PLACE_TICKS = 1;
    private static final int BLOCK_CLEAR_TICKS = 1;
    private static final int BLOCK_REPAIR_TICKS = 20;
	
    private int constructionTicksElapsed = 0;
	private int upgradeTicksElapsed = 0;
	private int repairTicksElapsed = 0;
	
	protected abstract void onConstruction();
	protected abstract void onUpgrade();
	protected abstract void onRemove();
	protected abstract void update(ServerWorld world);

	public Structure(String name, String faction, StructureType type, BlockPos position, Direction facing)
	{
		this.name = name;
		this.faction = faction;
		this.type = type;
		this.position = position;
		this.facing = facing;
		this.tier = 0;
		this.maxTier = 0;
		this.uuid = UUID.randomUUID();
		this.isActive = false;
		this.canConstruct = true;
		this.isConstructed = false;
		this.isConstructing = false;
		this.requiresRepair = false;
		this.isUpgrading = false;
		this.isClearing = true;
		this.resourceRequirements = new HashMap<>();
		loadResourceRequirements();
	}
	
	// when the architect does shit, they'll call:
	// - constructStructure(world)
	// - upgradeStructure(world)
	// - repairStructure(world)
	
	public void resume(ServerWorld world)
	{
		if (isClearing)
			prepareClearingQueue(world);
		
	    if (isConstructing)
	    {
	    	prepareConstructionQueue(world);
	    	registerConstructionTick(world);
	    }
	    
	    if (isUpgrading)
	        registerUpgradeTick(world);
	    
	    if (requiresRepair)
	    	repairStructure(world);
	}
	
	public void spawnStructure(ServerWorld world)
	{
		this.isConstructing = true;
		this.position = adjustToGround(world, this.position);
		if (canConstruct)
		{
			processStructure(world, (blockPos, blockState) -> world.setBlockState(blockPos, blockState));
			isConstructing = false;
			isConstructed = true;
			onConstruction();
		}
	}
	
	public void constructStructure(ServerWorld world)
	{
		this.isConstructing = true;
		this.position = adjustToGround(world, this.position);
		if (canConstruct)
		{
			prepareClearingQueue(world);     // prepare queue of blocks to be cleared
	        prepareConstructionQueue(world); // prepare queue of blocks to be placed
	        registerConstructionTick(world); // register tick event
		}
	}
	
	public void upgradeStructure(ServerWorld world)
	{
	    if (tier >= maxTier)
	    {
	    	Frontier.LOGGER.info(getName() + " in " + getFaction() + " is already at max tier!");
	    	return;
	    }

	    int nextTier = tier + 1;
	    upgradeMap = loadStructure(nextTier);

	    processStructure(world, (blockPos, blockState) ->
	    {
	        if (upgradeMap.containsKey(blockPos))
	        {
	            Block currentBlock = blockState.getBlock();

	            // skip chests & furnaces
	            if (!(currentBlock instanceof ChestBlock) && !(currentBlock instanceof FurnaceBlock))
	                upgradeQueue.add(blockPos);
	        }
	    });

	    tier = nextTier;
	    loadResourceRequirements();
	    isUpgrading = true;
	    registerUpgradeTick(world);
	}

	public void upgrade(ServerWorld world)
	{
		if (isConstructed && upgradeAvailable())
		{
			Frontier.LOGGER.info("Upgrading!");
			upgradeStructure(world);
		}	
	}
	
	// works GOOD ENOUGH for now
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
		for (int dx = -getLength() / 2; dx <= getLength() / 2; dx++)
		{
			for (int dz = -getWidth() / 2; dz <= getWidth() / 2; dz++)
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

	private void prepareClearingQueue(ServerWorld world)
	{
		processStructure(world, (blockPos, blockState) -> clearingQueue.add(blockPos));
	}

	private void prepareConstructionQueue(ServerWorld world)
	{
		processStructure(world, (blockPos, blockState) ->
		{
			if (blockState.isOf(Blocks.AIR))
				airBlocksQueue.add(blockPos);
			else
				nonAirBlocksQueue.add(blockPos);
			constructionMap.put(blockPos, blockState);
		});
	}

	private void registerConstructionTick(ServerWorld world)
	{
		ServerTickEvents.END_SERVER_TICK.register(server ->
		{
			if (server.getOverworld() == world && isConstructing)
			{
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
							this.isConstructed = true;
							onConstruction();
						}
					}
				}
			}
		});
	}
    
	private void registerUpgradeTick(ServerWorld world)
	{
		ServerTickEvents.END_SERVER_TICK.register(server ->
		{
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
						onUpgrade();
					}
				}
			}
		});
	}
    
    protected void loadResourceRequirements()
	{
		String path = String.format("data/frontier/structures/settlement/%s_%d.nbt", name.toLowerCase(), tier);
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path))
		{
			if (inputStream != null)
			{
				NbtCompound tag = NbtIo.readCompressed(inputStream);
				NbtList blocksList = tag.getList("blocks", 10);
				NbtList paletteList = tag.getList("palette", 10);

				Map<Integer, String> paletteMap = new HashMap<>();
				for (int i = 0; i < paletteList.size(); i++)
				{
					NbtCompound paletteEntry = paletteList.getCompound(i);
					String blockName = paletteEntry.getString("Name");
					paletteMap.put(i, blockName);
				}

				for (int i = 0; i < blocksList.size(); i++)
				{
					NbtCompound blockEntry = blocksList.getCompound(i);
					int state = blockEntry.getInt("state");
					String blockName = paletteMap.get(state);
					resourceRequirements.put(blockName, resourceRequirements.getOrDefault(blockName, 0) + 1);
				}
			}
			else
				Frontier.LOGGER.error("NBT file not found: " + path);
		}
		catch (IOException e) { e.printStackTrace(); }
	}
	
    private Map<BlockPos, BlockState> loadStructure(int tier)
    {
        Map<BlockPos, BlockState> structureMap = new HashMap<>();
        String path = String.format("data/frontier/structures/settlement/%s_%d.nbt", name.toLowerCase(), tier);

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path))
        {
            if (inputStream != null)
            {
                NbtCompound tag = NbtIo.readCompressed(inputStream);
                NbtList blocksList = tag.getList("blocks", 10);
                NbtList paletteList = tag.getList("palette", 10);

                Map<Integer, BlockState> paletteMap = buildPaletteMap(paletteList);

                for (int i = 0; i < blocksList.size(); i++)
                {
                    NbtCompound blockEntry = blocksList.getCompound(i);
                    int state = blockEntry.getInt("state");
                    BlockState blockState = paletteMap.get(state);

                    NbtList posList = blockEntry.getList("pos", 3);
                    int x = posList.getInt(0);
                    int y = posList.getInt(1);
                    int z = posList.getInt(2);

                    BlockPos originalPos = new BlockPos(x - 7, y - 1, z - 10);
                    BlockPos rotatedPos = rotatePos(originalPos);
                    BlockPos blockPos = position.add(rotatedPos);
                    BlockState rotatedState = rotateBlockState(blockState);

                    structureMap.put(blockPos, rotatedState);
                }
            }
            else
            {
            	Frontier.LOGGER.error("NBT file not found: " + path);
            }
        }
        catch (IOException e) { e.printStackTrace(); }
        return structureMap;
    }
    
	public boolean isDamaged(ServerWorld world)
	{
	    final boolean[] isDamaged = { false };
	    processStructure(world, (blockPos, expectedState) ->
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

	private void processStructure(ServerWorld world, BiConsumer<BlockPos, BlockState> blockProcessor)
	{
		String path = String.format("data/frontier/structures/settlement/%s_%d.nbt", name.toLowerCase(), tier);
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path))
		{
			if (inputStream != null)
			{
				NbtCompound tag = NbtIo.readCompressed(inputStream);
				NbtList blocksList = tag.getList("blocks", 10);
				NbtList paletteList = tag.getList("palette", 10);

				Map<Integer, BlockState> paletteMap = buildPaletteMap(paletteList);

				for (int i = 0; i < blocksList.size(); i++)
				{
					NbtCompound blockEntry = blocksList.getCompound(i);
					int state = blockEntry.getInt("state");
					BlockState blockState = paletteMap.get(state);

					NbtList posList = blockEntry.getList("pos", 3);
					int x = posList.getInt(0);
					int y = posList.getInt(1);
					int z = posList.getInt(2);

					BlockPos originalPos = new BlockPos(x - 7, y - 1, z - 10);
					BlockPos rotatedPos = rotatePos(originalPos);
					BlockPos blockPos = position.add(rotatedPos);
					BlockState rotatedState = rotateBlockState(blockState);

					blockProcessor.accept(blockPos, rotatedState);
				}
			}
			else
				Frontier.LOGGER.error("NBT file not found: " + path);
		}
		catch (IOException e) { e.printStackTrace(); }
	}

	private Map<Integer, BlockState> buildPaletteMap(NbtList paletteList)
	{
		Map<Integer, BlockState> paletteMap = new HashMap<>();
		for (int i = 0; i < paletteList.size(); i++)
		{
			NbtCompound paletteEntry = paletteList.getCompound(i);
			String blockName = paletteEntry.getString("Name");
			Block block = Registries.BLOCK.get(new Identifier(blockName));
			BlockState blockState = block.getDefaultState();
			if (paletteEntry.contains("Properties"))
			{
				NbtCompound properties = paletteEntry.getCompound("Properties");
				for (String key : properties.getKeys())
				{
					Property<?> property = block.getStateManager().getProperty(key);
					if (property != null)
					{
						String value = properties.getString(key);
						blockState = applyProperty(blockState, property, value);
					}
				}
			}
			paletteMap.put(i, blockState);
		}
		return paletteMap;
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
		Settlement settlement = SettlementManager.getSettlement(faction);
		Map<String, Integer> availableResources = new HashMap<>();

		for (Structure structure : settlement.getStructures())
		{
			if (structure.getName().equals("townhall") || structure.getName().equals("warehouse"))
			{
				Map<BlockPos, List<ItemStack>> chestContents = structure.getChestContents(world);
				Map<BlockPos, ItemStack> furnaceOutputs = structure.getFurnaceOutputContents(world);

				// aggregate resources from chests
				for (List<ItemStack> items : chestContents.values())
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

		// check if available resources can cover missing blocks
		for (Map.Entry<BlockPos, BlockState> entry : missingBlocks.entrySet())
		{
			String blockName = entry.getValue().getBlock().getTranslationKey();
			int requiredCount = 1; // each missing block counts as 1

			if (!availableResources.containsKey(blockName) || availableResources.get(blockName) < requiredCount)
				return false; // not enough resources

			// deduct resources
			availableResources.put(blockName, availableResources.get(blockName) - requiredCount);
		}

		return true; // all required resources available
	}

	public void repairStructure(ServerWorld world)
	{
		if (!canRepair(world))
		{
			Frontier.LOGGER.info("Cannot repair " + this.getName() + ": Not enough resources!");
			return;
		}

		Map<BlockPos, BlockState> missingBlocks = detectMissingBlocks(world);
		Settlement settlement = SettlementManager.getSettlement(faction);

		// remove required resources from chests & furnaces
		for (Structure structure : settlement.getStructures())
		{
			if (structure.getName().equals("townhall") || structure.getName().equals("warehouse"))
			{
				Map<BlockPos, List<ItemStack>> chestContents = structure.getChestContents(world);
				Map<BlockPos, List<ItemStack>> barrelContents = structure.getBarrelContents(world);
				Map<BlockPos, ItemStack> furnaceOutputs = structure.getFurnaceOutputContents(world);

				for (Map.Entry<BlockPos, BlockState> entry : missingBlocks.entrySet())
				{
					String blockName = entry.getValue().getBlock().getTranslationKey();
					int requiredCount = 1; // each missing block counts as 1

					requiredCount = removeResourcesFromChests(chestContents, blockName, requiredCount);
					requiredCount = removeResourcesFromBarrels(barrelContents, blockName, requiredCount);
					requiredCount = removeResourcesFromFurnaceOutputs(furnaceOutputs, blockName, requiredCount);

					if (requiredCount > 0)
					{
						Frontier.LOGGER.info("Cannot repair " + this.getName() + ": Not enough resources in chests, barrels, or furnaces!");
						return;
					}
				}
			}
		}

		// initialize repair queue
		repairQueue.addAll(missingBlocks.entrySet());
		repairTicksElapsed = 0;

		// register repair tick event
		ServerTickEvents.END_SERVER_TICK.register(server ->
		{
			if (server.getOverworld() == world)
				repairTick(world);
		});
	}

	private void repairTick(ServerWorld world)
	{
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
					Frontier.LOGGER.info(getName() + " repaired!");
				}
			}
		}
	}
	
	private Map<BlockPos, BlockState> detectMissingBlocks(ServerWorld world)
	{
		Map<BlockPos, BlockState> missingBlocks = new HashMap<>();

		processStructure(world, (blockPos, expectedState) ->
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
        processStructure(world, (blockPos, blockState) ->
        {
            if (blockState.isOf(Blocks.CHEST))
                chestPositions.add(blockPos);
        });
        return chestPositions;
    }
    
    public List<BlockPos> findBarrels(ServerWorld world)
    {
        List<BlockPos> barrelPositions = new ArrayList<>();
        processStructure(world, (blockPos, blockState) ->
        {
            if (blockState.isOf(Blocks.BARREL))
                barrelPositions.add(blockPos);
        });
        return barrelPositions;
    }

    
    public List<BlockPos> findFurnaces(ServerWorld world)
    {
        List<BlockPos> furnacePositions = new ArrayList<>();
        processStructure(world, (blockPos, blockState) ->
        {
            if (blockState.isOf(Blocks.FURNACE))
                furnacePositions.add(blockPos);
        });
        return furnacePositions;
    }
	
	private BlockPos rotatePos(BlockPos originalPos)
	{
		switch (facing)
		{
			case NORTH:
				return originalPos;
			case SOUTH:
				return new BlockPos(-originalPos.getX(), originalPos.getY(), -originalPos.getZ());
			case WEST:
				return new BlockPos(originalPos.getZ(), originalPos.getY(), -originalPos.getX());
			case EAST:
				return new BlockPos(-originalPos.getZ(), originalPos.getY(), originalPos.getX());
			default:
				throw new IllegalArgumentException("Invalid facing direction: " + this.facing);
		}
	}

	@SuppressWarnings("unchecked")
	private BlockState rotateBlockState(BlockState state)
	{
		for (Property<?> property : state.getProperties())
		{
			if (property.getName().equals("facing") && property.getType() == Direction.class)
			{
				Direction currentFacing = state.get((Property<Direction>) property);
				Direction newFacing = rotateDirection(currentFacing);
				state = state.with((Property<Direction>) property, newFacing);
			}
			else if (property.getName().equals("rotation") && property.getType() == Integer.class)
			{
				int currentRotation = state.get((Property<Integer>) property);
				int newRotation = adjustRotation(currentRotation);
				state = state.with((Property<Integer>) property, newRotation);
			}
		}
		return state;
	}

	private Direction rotateDirection(Direction original)
	{
		switch (facing)
		{
			case NORTH:
				return original;
			case SOUTH:
				return original.getOpposite();
			case WEST:
				return original.rotateYCounterclockwise();
			case EAST:
				return original.rotateYClockwise();
			default:
				return original;
		}
	}

	private int adjustRotation(int original)
	{
		switch (this.facing)
		{
			case NORTH:
				return original;
			case SOUTH:
				return (original + 2) % 4;
			case WEST:
				return (original + 3) % 4;
			case EAST:
				return (original + 1) % 4;
			default:
				return original;
		}
	}
	
	private <T extends Comparable<T>> BlockState applyProperty(BlockState state, Property<T> property, String value)
	{
		return property.parse(value).map(parsedValue -> state.with(property, parsedValue)).orElse(state);
	}

    public boolean upgradeAvailable()
    {
		if (this.tier < this.maxTier)
			return true;
		return false;
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
    
	public int[] getStructureSize()
	{
		String path = String.format("data/frontier/structures/settlement/%s_%d.nbt", name.toLowerCase(), tier);
		int[] size = new int[3]; // [length, width, height]

		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path))
		{
			if (inputStream != null)
			{
				NbtCompound tag = NbtIo.readCompressed(inputStream);
				NbtList sizeList = tag.getList("size", 3);

				size[0] = sizeList.getInt(0); // length
				size[1] = sizeList.getInt(1); // width
				size[2] = sizeList.getInt(2); // height
			}
			else
				Frontier.LOGGER.error("Structure - NBT file not found: " + path);
		}
		catch (IOException e) { e.printStackTrace(); }
		return size;
	}
	
	public int getLength() {
        return getStructureSize()[0];
    }

    public int getWidth() {
        return getStructureSize()[1];
    }

    public int getHeight() {
        return getStructureSize()[2];
    }
    
    public UUID getLeader() {
		return SettlementManager.getSettlement(faction).getLeader();
	}
    
    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFaction() {
		return faction;
	}

	public void setFaction(String playerFaction) {
		this.faction = playerFaction;
	}

	public StructureType getType() {
		return type;
	}

	public void setType(StructureType type) {
		this.type = type;
	}
	
	public void setType(String type)
	{
        try { this.type = StructureType.valueOf(type.toUpperCase()); }
        catch (IllegalArgumentException e) { Frontier.LOGGER.error("Invalid structure type: " + type + "!"); }
    }

	public BlockPos getPosition() {
		return position;
	}

	public void setPosition(BlockPos position) {
		this.position = position;
	}
	
	public Direction getFacing() {
		return facing;
	}
	
	public int getTier() {
		return tier;
	}
	
	public void setTier(int tier) {
		this.tier = tier;
		loadResourceRequirements();
	}
	
	public int getMaxTier() {
		return maxTier;
	}
	
	public void setMaxTier(int maxTier) {
		this.maxTier = maxTier;
	}

	public UUID getUUID() {
		return uuid;
	}
	
	public void setUUID(UUID uuid) {
	    this.uuid = uuid;
	}
	
	public boolean isActive() {
		return isActive;
	}
	
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	public boolean canConstruct() {
		return canConstruct;
	}
	public boolean isConstructed() {
		return isConstructed;
	}
	
	public void setConstructed(boolean isConstructed) {
		this.isConstructed = isConstructed;
	}
	
	public boolean isConstructing() {
		return isConstructing;
	}
	
	public void setConstructing(boolean isConstructing) {
		this.isConstructing = isConstructing;
	}
	
	public boolean requiresRepair() {
		return requiresRepair;
	}
	
	public void setRepair(boolean requiresRepair) {
		this.requiresRepair = requiresRepair;
	}
	
	public boolean isUpgrading() {
		return isUpgrading;
	}
	
	public void setUpgrading(boolean isUpgrading) {
		this.isUpgrading = isUpgrading;
	}
	
	public boolean isClearing() {
		return isClearing;
	}
	
	public void setClearing(boolean isClearing) {
		this.isClearing = isClearing;
	}
	
	public int getConstructionTicksElapsed() {
		return constructionTicksElapsed;
	}
	
	public void setConstructionTicksElapsed(int constructionTicksElapsed) {
		this.constructionTicksElapsed = constructionTicksElapsed;
	}
	
	public int getUpgradeTicksElapsed() {
		return upgradeTicksElapsed;
	}
	
	public void setUpgradeTicksElapsed(int upgradeTicksElapsed) {
		this.upgradeTicksElapsed = upgradeTicksElapsed;
	}
	
	public int getRepairTicksElapsed() {
		return repairTicksElapsed;
	}
	
	public void setRepairTicksElapsed(int repairTicksElapsed) {
		this.repairTicksElapsed = repairTicksElapsed;
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
	
	public Queue<BlockPos> getUpgradeQueue() {
		return upgradeQueue;
	}
	
	public void setUpgradeQueue(Queue<BlockPos> upgradeQueue) {
		this.upgradeQueue = upgradeQueue;
	}
	
	public Queue<Map.Entry<BlockPos, BlockState>> getRepairQueue() {
		return repairQueue;
	}
	
	public void setRepairQueue(Queue<Map.Entry<BlockPos, BlockState>> repairQueue) {
		this.repairQueue = repairQueue;
	}
	
	public Map<BlockPos, BlockState> getConstructionMap() {
		return constructionMap;
	}
	
	public void setConstructionMap(Map<BlockPos, BlockState> constructionMap) {
		this.constructionMap = constructionMap;
	}
	
	public Map<BlockPos, BlockState> getUpgradeMap() {
		return upgradeMap;
	}
	
	public void setUpgradeMap(Map<BlockPos, BlockState> upgradeMap) {
		this.upgradeMap = upgradeMap;
	}
}