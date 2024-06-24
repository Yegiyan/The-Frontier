package com.frontier.structures;

import java.util.Arrays;

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
	protected void update()
	{
		System.out.println(getName() + " of " + getFaction() + " is updating!");
	}

    @Override
    protected void onConstruction()
    {
    	System.out.println("Constructed Town Hall at " + position);
    	setActive(true);
    }

    @Override
    protected void onUpgrade()
    {
        System.out.println("Upgraded Town Hall to tier " + tier + " at " + position);
    }
    
    @Override
	protected void onRemove()
	{
		setActive(false);
	}
    
    public void printStructureSize()
    {
        int[] size = getStructureSize();
        System.out.println("Structure size: " + Arrays.toString(size));
    }
}