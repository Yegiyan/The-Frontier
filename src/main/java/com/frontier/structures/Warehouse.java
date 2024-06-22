package com.frontier.structures;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class Warehouse extends Structure
{
    public Warehouse(String name, String faction, BlockPos position, Direction facing)
    {
        super(name, faction, StructureType.CORE, position, facing);
    }

    @Override
    protected void onConstruction()
    {
    	System.out.println("Constructed Warehouse at " + position);
    }

    @Override
    protected void onUpgrade()
    {
        System.out.println("Upgraded Warehouse to tier " + tier + " at " + position);
    }

	@Override
	protected void onUpdate()
	{
		
	}
}