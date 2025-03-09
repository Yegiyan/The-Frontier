package com.frontier.structures;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Optional;

public class BlockStateHelper
{

	public static BlockPos rotatePos(BlockPos originalPos, Direction facing)
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
				throw new IllegalArgumentException("Invalid facing direction: " + facing);
		}
	}

	@SuppressWarnings("unchecked")
	public static BlockState rotateBlockState(BlockState state, Direction facing)
	{
		for (Property<?> property : state.getProperties())
		{
			if (property.getName().equals("facing") && property.getType() == Direction.class)
			{
				Direction currentFacing = state.get((Property<Direction>) property);
				Direction newFacing = rotateDirection(currentFacing, facing);
				state = state.with((Property<Direction>) property, newFacing);
			}
			else if (property.getName().equals("rotation") && property.getType() == Integer.class)
			{
				int currentRotation = state.get((Property<Integer>) property);
				int newRotation = adjustRotation(currentRotation, facing);
				state = state.with((Property<Integer>) property, newRotation);
			}
		}
		return state;
	}

	public static Direction rotateDirection(Direction original, Direction facing)
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
	
	public static BlockPos rotateAroundCenter(BlockPos pos, Direction facing, int centerX, int centerZ)
	{
		int relX = pos.getX() - centerX;
		int relZ = pos.getZ() - centerZ;
		int rotatedX, rotatedZ;

		switch (facing)
		{
			case SOUTH: // 180 degree rotation
				rotatedX = -relX;
				rotatedZ = -relZ;
				break;
			case WEST: // 270 degree rotation (clockwise)
				rotatedX = relZ;
				rotatedZ = -relX;
				rotatedZ += 1;
				rotatedX -= 1;
				break;
			case EAST: // 90 degree rotation (clockwise)
				rotatedX = -relZ;
				rotatedZ = relX;
				rotatedZ += 1;
				rotatedX -= 1;
				break;
			case NORTH:
			default:
				// no rotation needed for NORTH
				rotatedX = relX;
				rotatedZ = relZ;
				break;
		}

		return new BlockPos(rotatedX + centerX, pos.getY() - 1, rotatedZ + centerZ);
	}

	public static int adjustRotation(int original, Direction facing)
	{
		switch (facing)
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

	public static <T extends Comparable<T>> BlockState applyProperty(BlockState state, Property<T> property, String value)
	{
		Optional<T> optional = property.parse(value);
		if (optional.isPresent())
			return state.with(property, optional.get());
		return state;
	}
}