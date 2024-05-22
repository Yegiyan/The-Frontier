package com.frontier.structures;

import net.minecraft.util.math.BlockPos;

public class TownHall extends Structure
{
    public TownHall(String name, String faction, BlockPos position)
    {
        super(name, faction, StructureType.CORE, position);
    }

    @Override
    protected void onConstruction()
    {
    	System.out.println("Construction of Town Hall started at " + position);
    }

    @Override
    protected void onUpgrade()
    {
    	System.out.println("Upgrading Town Hall to tier " + tier + " at " + position);
    }
}