package com.frontier.structures;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import net.minecraft.block.BellBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.inventory.Inventory;
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

public abstract class Structure
{
	protected String name;
	protected String faction;
	protected BlockPos position;
	protected boolean isConstructed;
	protected boolean requiresRepair;
	protected Direction facing;
	protected int tier;
	protected UUID uuid;

	protected Map<String, Integer> resourceRequirements;

	protected StructureType type;

	public enum StructureType { CORE, GATHERER, TRADER, CRAFTER, OUTSIDER, RANCHER, MILITIA, MISC }

	protected abstract void onConstruction();
	protected abstract void onUpgrade();

	public Structure(String name, String faction, StructureType type, BlockPos position, Direction facing)
	{
		this.name = name;
		this.faction = faction;
		this.type = type;
		this.position = position;
		this.facing = facing;
		this.isConstructed = false;
		this.tier = 0;
		this.resourceRequirements = new HashMap<>();
		loadResourceRequirements();
	}

	public void startConstruction(ServerWorld world)
	{
		this.isConstructed = true;
		this.uuid = UUID.randomUUID();
		onConstruction();
		spawnStructure(world);
	}

	public void upgrade()
	{
		if (isConstructed)
		{
			tier++;
			loadResourceRequirements();
			onUpgrade();
		}
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
				System.err.println("NBT file not found: " + path);
		}
		catch (IOException e) { e.printStackTrace(); }
	}

	protected void spawnStructure(ServerWorld world)
	{
		processStructure(world, (blockPos, blockState) -> world.setBlockState(blockPos, blockState));
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
	            // check all properties except facing
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
				System.err.println("NBT file not found: " + path);
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
                ItemStack outputStack = inventory.getStack(2); // output for furnace should be slot 2
                if (!outputStack.isEmpty())
                    furnaceOutputs.put(furnacePos, outputStack);
            }
        }
        return furnaceOutputs;
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

	public BlockPos getPosition() {
		return position;
	}

	public void setPosition(BlockPos position) {
		this.position = position;
	}
	
	public Direction getFacing() {
		return facing;
	}

	public boolean isConstructed() {
		return isConstructed;
	}

	public int getTier() {
		return tier;
	}
	
	public void setTier(int tier) {
		this.tier = tier;
		loadResourceRequirements();
	}

	public UUID getUUID() {
		return uuid;
	}
	
	public void setUUID(UUID uuid) {
	    this.uuid = uuid;
	}
}