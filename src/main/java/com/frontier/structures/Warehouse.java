package com.frontier.structures;

import com.frontier.Frontier;
import com.frontier.settlements.SettlementManager;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class Warehouse extends Structure
{
    public Warehouse(String name, String faction, BlockPos position, Direction facing)
    {
        super(name, faction, StructureType.WAREHOUSE, position, facing);
    }
    
    @Override
	protected void update(ServerWorld world)
	{
    	// getInventoryManager().getStructureInventory(world)
		//Frontier.LOGGER.info(getName() + " of " + getFaction() + " is updating!");
	}

    @Override
    protected void onConstruction(ServerWorld world)
    {
    	this.setName("Warehouse");
    	setActive(true);
    	SettlementManager.saveSettlements(world.getServer());
    	Frontier.LOGGER.info("Constructed " + this.getName() + " at (" + position + ")");
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