package com.frontier.goals.architect;

import java.util.EnumSet;
import java.util.Random;

import com.frontier.entities.settler.ArchitectEntity;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class IdleInTownHallGoal extends Goal
{
	private final ArchitectEntity architect;
	private final double speed;
	private int idleTimer;
	private Random random = new Random();
	private BlockPos currentTarget;
	private int interactionCooldown = 0;

	public IdleInTownHallGoal(ArchitectEntity architect, double speed)
	{
		this.architect = architect;
		this.speed = speed;
		this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
	}

	@Override
	public boolean canStart()
	{
		return this.architect.getState() == ArchitectState.IDLE_IN_TOWNHALL;
	}

	@Override
	public void start()
	{
		this.idleTimer = 0;
		this.currentTarget = null;
	}

	@Override
	public boolean shouldContinue()
	{
		return this.architect.getState() == ArchitectState.IDLE_IN_TOWNHALL;
	}

	@Override
	public void tick()
	{
		idleTimer++;

		// if not moving or we've reached our current target
		if (currentTarget == null || this.architect.getNavigation().isIdle())
		{
			// every 3-5 seconds, choose a new random location to wander to
			if (idleTimer % (60 + random.nextInt(40)) == 0)
				wanderRandomly();
		}

		if (interactionCooldown > 0)
			interactionCooldown--;

		// interact with blocks (chests, crafting tables, furnaces) occasionally
		if (interactionCooldown == 0 && idleTimer % 80 == 0)
			interactWithNearbyBlocks();
	}

	private void wanderRandomly()
	{
		// get random position in a 6x6 area around the architect
		int offsetX = random.nextInt(7) - 3; // -3 to 3
		int offsetZ = random.nextInt(7) - 3; // -3 to 3

		BlockPos entityPos = this.architect.getBlockPos();
		currentTarget = entityPos.add(offsetX, 0, offsetZ);

		// start moving to the new location
		this.architect.getNavigation().startMovingTo(currentTarget.getX(), currentTarget.getY(), currentTarget.getZ(), this.speed);
	}

	private void interactWithNearbyBlocks()
	{
		if (!(this.architect.getEntityWorld() instanceof ServerWorld))
			return;

		ServerWorld world = (ServerWorld) this.architect.getEntityWorld();
		BlockPos entityPos = this.architect.getBlockPos();

		// look for interesting blocks nearby
		for (int x = -3; x <= 3; x++)
		{
			for (int y = -1; y <= 1; y++)
			{
				for (int z = -3; z <= 3; z++)
				{
					BlockPos checkPos = entityPos.add(x, y, z);
					BlockState state = world.getBlockState(checkPos);

					// check for blocks the architect might interact with
					if (state.getBlock() instanceof ChestBlock || state.getBlock() instanceof CraftingTableBlock || state.getBlock() instanceof FurnaceBlock)
					{

						// move toward the block
						this.architect.getNavigation().startMovingTo(checkPos.getX(), checkPos.getY(), checkPos.getZ(), this.speed);

						// look at the block
						this.architect.getLookControl().lookAt(checkPos.getX() + 0.5, checkPos.getY() + 0.5, checkPos.getZ() + 0.5);

						// spawn some particles to show interaction
						world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, checkPos.getX() + 0.5, checkPos.getY() + 1.0, checkPos.getZ() + 0.5, 5, 0.25, 0.25, 0.25, 0.0);

						// set a cooldown before interacting again
						interactionCooldown = 60 + random.nextInt(40); // 3-5 seconds
					}
				}
			}
		}
	}
}