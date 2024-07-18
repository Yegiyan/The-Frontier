package com.frontier.structures;

import com.frontier.Frontier;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class Warehouse extends Structure
{
    public Warehouse(String name, String faction, BlockPos position, Direction facing)
    {
        super(name, faction, StructureType.GOVERNING, position, facing);
        //setMaxTier(2);
    }
    
    @Override
	protected void update()
	{
		//Frontier.LOGGER.info(getName() + " of " + getFaction() + " is updating!");
	}

    @Override
    protected void onConstruction()
    {
    	Frontier.LOGGER.info("Constructed Warehouse at " + position);
    	setActive(true);
    }

    @Override
    protected void onUpgrade()
    {
    	Frontier.LOGGER.info("Upgraded Warehouse to tier " + tier + " at " + position);
    }

	@Override
	protected void onRemove()
	{
		setActive(false);
	}
}