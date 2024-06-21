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
    	System.out.println("Construction of Warehouse started at " + position);
    	System.out.println("Resource requirements: " + resourceRequirements);
    }

    @Override
    protected void onUpgrade()
    {
        System.out.println("Upgrading Town Hall to tier " + tier + " at " + position);
        System.out.println("New resource requirements: " + resourceRequirements);
    }
}