package com.frontier.structures;

import java.util.Arrays;

import com.frontier.Frontier;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class TownHall extends Structure
{
    public TownHall(String name, String faction, BlockPos position, Direction facing)
    {
        super(name, faction, StructureType.CORE, position, facing);
        setMaxTier(2);
    }
    
    @Override
	protected void update(ServerWorld world)
	{
		//Frontier.LOGGER.info(getName() + " inventory: " + getStructureInventory(world));
	}

    @Override
    protected void onConstruction()
    {
    	Frontier.LOGGER.info("Constructed TownHall at " + position);
    	setActive(true);
    }

    @Override
    protected void onUpgrade()
    {
    	Frontier.LOGGER.info("Upgraded TownHall to tier " + tier + " at " + position);
    }
    
    @Override
	protected void onRemove()
	{
		setActive(false);
	}
    
    public void printStructureSize()
    {
        int[] size = getStructureSize();
        Frontier.LOGGER.info("Structure size: " + Arrays.toString(size));
    }
}