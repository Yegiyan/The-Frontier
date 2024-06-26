package com.frontier.structures;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class Warehouse extends Structure
{
    public Warehouse(String name, String faction, BlockPos position, Direction facing)
    {
        super(name, faction, StructureType.CORE, position, facing);
        //setMaxTier(2);
    }
    
    @Override
	protected void update()
	{
		//System.out.println(getName() + " of " + getFaction() + " is updating!");
	}

    @Override
    protected void onConstruction()
    {
    	System.out.println("Constructed Warehouse at " + position);
    	setActive(true);
    }

    @Override
    protected void onUpgrade()
    {
        System.out.println("Upgraded Warehouse to tier " + tier + " at " + position);
    }

	@Override
	protected void onRemove()
	{
		setActive(false);
	}
}