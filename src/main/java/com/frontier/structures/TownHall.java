package com.frontier.structures;

import java.util.Arrays;

import com.frontier.Frontier;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class TownHall extends Structure
{
    public TownHall(String name, String faction, BlockPos position, Direction facing)
    {
        super(name, faction, StructureType.GOVERNING, position, facing);
        setMaxTier(2);
    }
    
    @Override
	protected void update()
	{
		//Frontier.LOGGER.info(getName() + " of " + getFaction() + " is updating!");
	}

    @Override
    protected void onConstruction()
    {
    	Frontier.LOGGER.info("Constructed Town Hall at " + position);
    	setActive(true);
    }

    @Override
    protected void onUpgrade()
    {
    	Frontier.LOGGER.info("Upgraded Town Hall to tier " + tier + " at " + position);
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