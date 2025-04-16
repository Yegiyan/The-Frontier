package com.frontier.goals.architect;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import com.frontier.Frontier;
import com.frontier.entities.settler.ArchitectEntity;
import com.frontier.settlements.Settlement;
import com.frontier.settlements.SettlementManager;
import com.frontier.structures.Structure;
import com.frontier.structures.TownHall;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class IdleInTownHallGoal extends Goal
{
	private final ArchitectEntity architect;
	private final double speed;
	
	private BlockPos targetPos;
	private Path path;
	
	private BlockPos lookTarget = null;
	private int lookTimer = 0;
	private int cooldown = 0;
	
	private final List<BlockPos> functionalBlocks = new ArrayList<>();
	private boolean isScanningForBlocks = true;
	private Box townHallBounds = null;
	private TownHall townHall = null;
	
	private final Random random = new Random();

	public IdleInTownHallGoal(ArchitectEntity architect, double speed)
	{
		this.architect = architect;
		this.speed = speed;
		this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
	}

	@Override
	public boolean canStart()
	{
		return architect.getState() == ArchitectState.IDLE_IN_TOWNHALL;
	}

	@Override
	public void start()
	{
		findTownHall();
		cooldown = 0;
	}

	private void findTownHall()
	{
		World world = architect.getWorld();
		
		if (world.isClient)
			return;

		String faction = architect.getSettlerFaction();
		Settlement settlement = SettlementManager.getSettlement(faction);

		if (settlement == null)
			return;

		// find town hall structure
		for (Structure structure : settlement.getStructures())
		{
			if (structure instanceof TownHall && structure.isConstructed())
			{
				townHall = (TownHall) structure;

				// calculate bounds of town hall
				int[] size = townHall.getStructureSize();
				BlockPos pos = townHall.getPosition();
				BlockPos maxPos = pos.add(size[0], size[2], size[1]);

				townHallBounds = new Box(pos, maxPos);
				isScanningForBlocks = true;
				
				return;
			}
		}
		Frontier.LOGGER.warn("No town hall found for settlement: " + faction + "!");
	}

	private void scanForFunctionalBlocks()
	{
		if (townHall == null || townHallBounds == null)
			return;

		functionalBlocks.clear();
		ServerWorld world = (ServerWorld) architect.getWorld();

		// get min and max coordinates of town hall
		int minX = (int) townHallBounds.minX;
		int minY = (int) townHallBounds.minY;
		int minZ = (int) townHallBounds.minZ;
		int maxX = (int) townHallBounds.maxX;
		int maxY = (int) townHallBounds.maxY;
		int maxZ = (int) townHallBounds.maxZ;

		// scan area for functional blocks
		for (int x = minX; x <= maxX; x++)
		{
			for (int y = minY; y <= maxY; y++)
			{
				for (int z = minZ; z <= maxZ; z++)
				{
					BlockPos pos = new BlockPos(x, y, z);
					BlockState state = world.getBlockState(pos);
                    
                    if (state.getBlock() == Blocks.CHEST || 
                        state.getBlock() == Blocks.TRAPPED_CHEST ||
                        state.getBlock() == Blocks.BARREL ||
                        state.getBlock() == Blocks.FURNACE ||
                        state.getBlock() == Blocks.BLAST_FURNACE ||
                        state.getBlock() == Blocks.SMOKER ||
                        state.getBlock() == Blocks.CRAFTING_TABLE ||
                        state.getBlock() == Blocks.SMITHING_TABLE ||
                        state.getBlock() == Blocks.FLETCHING_TABLE ||
                        state.getBlock() == Blocks.CARTOGRAPHY_TABLE ||
                        state.getBlock() == Blocks.LOOM ||
                        state.getBlock() == Blocks.STONECUTTER ||
                        state.getBlock() == Blocks.GRINDSTONE ||
                        state.getBlock() == Blocks.ANVIL ||
                        state.getBlock() == Blocks.BREWING_STAND)
                    {
                        functionalBlocks.add(pos);
                    }
                }
            }
        }

		isScanningForBlocks = false;
	}

	@Override
	public void tick()
	{
		if (isScanningForBlocks)
			scanForFunctionalBlocks();

		if (lookTarget != null && lookTimer > 0)
		{
			lookAtBlock();
			lookTimer--;

			if (lookTimer <= 0)
				lookTarget = null;
			return;
		}

		if (cooldown > 0)
		{
			cooldown--;
			return;
		}

		// if we have no target or reached the target, pick a new one
		if (targetPos == null || architect.getNavigation().isIdle())
		{
			// either pick functional block to look at or random position to move to
			if (!functionalBlocks.isEmpty() && random.nextFloat() < 0.3f)
			{
				// 30% chance to look at a functional block
				lookTarget = functionalBlocks.get(random.nextInt(functionalBlocks.size()));
				lookTimer = 100 + random.nextInt(100); // look for 5 - 10 seconds
				lookAtBlock();
			}
			else
				pickNewTarget(); // else, pick random position within town hall to move to

			cooldown = 20 + random.nextInt(60); // 1-4 seconds
		}
	}

	private void lookAtBlock()
	{
		if (lookTarget == null)
			return;

		// look at center of block
		Vec3d targetVec = new Vec3d(lookTarget.getX() + 0.5, lookTarget.getY() + 0.5, lookTarget.getZ() + 0.5);
		architect.getLookControl().lookAt(targetVec.x, targetVec.y, targetVec.z);
	}

	private void pickNewTarget()
	{
		if (townHallBounds == null)
			return;

		// pick random position within town hall bounds
		int tries = 0;
		while (tries < 50)
		{
			double x = townHallBounds.minX + random.nextDouble() * (townHallBounds.maxX - townHallBounds.minX);
			double y = townHallBounds.minY;
			double z = townHallBounds.minZ + random.nextDouble() * (townHallBounds.maxZ - townHallBounds.minZ);

			BlockPos pos = new BlockPos((int) x, (int) y, (int) z);

			// find the first non-air block from the bottom
			World world = architect.getWorld();
			while (y <= townHallBounds.maxY)
			{
				pos = new BlockPos((int) x, (int) y, (int) z);
				if (!world.getBlockState(pos).isAir() && world.getBlockState(pos.up()).isAir())
				{
					pos = pos.up();
					break;
				}
				y++;
			}

			path = architect.getNavigation().findPathTo(pos, 0);
			if (path != null)
			{
				targetPos = pos;
				architect.getNavigation().startMovingAlong(path, speed);
				return;
			}

			tries++;
		}
	}

	@Override
	public boolean shouldContinue()
	{
		return architect.getState() == ArchitectState.IDLE_IN_TOWNHALL && townHall != null;
	}

	@Override
	public void stop()
	{
		architect.getNavigation().stop();
		targetPos = null;
		lookTarget = null;
		lookTimer = 0;
	}
}