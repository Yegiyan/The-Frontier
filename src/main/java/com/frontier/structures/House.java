package com.frontier.structures;

import com.frontier.Frontier;
import com.frontier.settlements.SettlementManager;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class House extends Structure
{
    public House(String name, String faction, BlockPos position, Direction facing)
    {
        super(name, faction, StructureType.CORE, position, facing);
        //setMaxTier(2);
    }
    
    @Override
	protected void update(ServerWorld world)
	{
		//Frontier.LOGGER.info(getName() + " of " + getFaction() + " is updating!");
	}

    @Override
    protected void onConstruction(ServerWorld world)
    {
    	Frontier.LOGGER.info("Constructed House at " + position);
    	setActive(true);
    	SettlementManager.saveSettlements(world.getServer());
    }

    @Override
    protected void onUpgrade()
    {
    	Frontier.LOGGER.info("Upgraded House to tier " + tier + " at " + position);
    }

	@Override
	protected void onRemove()
	{
		setActive(false);
	}
}