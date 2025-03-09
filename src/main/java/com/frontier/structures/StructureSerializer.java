package com.frontier.structures;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.frontier.Frontier;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class StructureSerializer
{

	public static void processStructure(Structure structure, ServerWorld world, BiConsumer<BlockPos, BlockState> blockProcessor)
	{
	    String path = String.format("data/frontier/structures/settlement/%s_%d.nbt", structure.getName().toLowerCase(), structure.getTier());
	    try (InputStream inputStream = StructureSerializer.class.getClassLoader().getResourceAsStream(path))
	    {
	        if (inputStream != null)
	        {
	            NbtCompound tag = NbtIo.readCompressed(inputStream);
	            NbtList blocksList = tag.getList("blocks", 10);
	            NbtList paletteList = tag.getList("palette", 10);

	            // get structure's dimensions for proper centering
	            int[] structureSize = getStructureSize(structure.getName(), structure.getTier());
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

	                BlockPos blockPos = structure.getPosition().add(x, y, z);
	                BlockPos rotatedPos = BlockStateHelper.rotateAroundCenter
	                (
	                    new BlockPos(x, y, z),
	                    structure.getFacing(),
	                    structureSize[0] / 2,
	                    structureSize[2] / 2
	                );
	                
	                blockPos = structure.getPosition().add(rotatedPos);
	                BlockState rotatedState = BlockStateHelper.rotateBlockState(blockState, structure.getFacing());

	                blockProcessor.accept(blockPos, rotatedState);
	            }
	        }
	        else
	            Frontier.LOGGER.error("NBT file not found: " + path);
	    }
	    catch (IOException e) { e.printStackTrace(); }
	}

	public static Map<BlockPos, BlockState> loadStructure(String name, int tier, BlockPos position, Direction facing)
	{
	    Map<BlockPos, BlockState> structureMap = new HashMap<>();
	    String path = String.format("data/frontier/structures/settlement/%s_%d.nbt", name.toLowerCase(), tier);

	    try (InputStream inputStream = StructureSerializer.class.getClassLoader().getResourceAsStream(path))
	    {
	        if (inputStream != null)
	        {
	            NbtCompound tag = NbtIo.readCompressed(inputStream);
	            NbtList blocksList = tag.getList("blocks", 10);
	            NbtList paletteList = tag.getList("palette", 10);
	            
	            // get structure dimensions for proper centering
	            int[] structureSize = getStructureSize(name, tier);

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

	                BlockPos rotatedPos = BlockStateHelper.rotateAroundCenter
	                (
	                    new BlockPos(x, y, z),
	                    facing,
	                    structureSize[0] / 2,
	                    structureSize[2] / 2
	                );
	                
	                BlockPos blockPos = position.add(rotatedPos);
	                BlockState rotatedState = BlockStateHelper.rotateBlockState(blockState, facing);

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

	public static Map<Integer, BlockState> buildPaletteMap(NbtList paletteList)
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
						blockState = BlockStateHelper.applyProperty(blockState, property, value);
					}
				}
			}
			paletteMap.put(i, blockState);
		}
		return paletteMap;
	}

	public static int[] getStructureSize(String name, int tier)
	{
		String path = String.format("data/frontier/structures/settlement/%s_%d.nbt", name.toLowerCase(), tier);
		int[] size = new int[3]; // [length, width, height]

		try (InputStream inputStream = StructureSerializer.class.getClassLoader().getResourceAsStream(path))
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
}