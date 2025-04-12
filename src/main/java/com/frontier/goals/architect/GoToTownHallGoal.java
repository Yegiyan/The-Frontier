package com.frontier.goals.architect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.frontier.Frontier;
import com.frontier.entities.settler.ArchitectEntity;
import com.frontier.settlements.Settlement;
import com.frontier.settlements.SettlementManager;
import com.frontier.structures.Structure;
import com.frontier.structures.StructureType;

import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;

public class GoToTownHallGoal extends Goal
{
	private final ArchitectEntity architect;
	private final double speed;
	private BlockPos townHallCenter;
	private Structure townHall;
	private Vec3d lastTickPosition;
	private int stuckTicks;
	private int pathfindingAttempts;
	private boolean isNavigatingToEntrance;
	private BlockPos entrancePos;

	private final Map<BlockPos, Integer> openedDoors = new HashMap<>();
	private final int DOOR_CLOSE_TICKS = 40; // 40 ticks = 2 seconds

	private final double DISTANCE = 1D; // how close to consider "arrived"
	private final int DOOR_CLOSE_DISTANCE = 3;
	private final int MAX_PATHFINDING_ATTEMPTS = 3;

	public GoToTownHallGoal(ArchitectEntity architect, double speed)
	{
		this.architect = architect;
		this.speed = speed;
		this.lastTickPosition = architect.getPos();
		this.stuckTicks = 0;
		this.pathfindingAttempts = 0;
		this.isNavigatingToEntrance = false;
	}

	@Override
	public boolean canStart()
	{
		ArchitectState currentState = this.architect.getState();
		if (currentState != ArchitectState.GOING_TO_TOWNHALL)
			return false;

		if (!(this.architect.getEntityWorld() instanceof ServerWorld))
			return false;

		String faction = this.architect.getSettlerFaction();
		if (faction == null)
			return false;

		Settlement settlement = SettlementManager.getSettlement(faction);
		if (settlement == null)
			return false;

		this.townHall = null;
		for (Structure structure : settlement.getStructures())
		{
			if (structure.getType() == StructureType.TOWNHALL)
			{
				this.townHall = structure;
				break;
			}
		}

		if (this.townHall == null)
			return false;

		this.townHallCenter = this.townHall.getCenterGroundBlockPos();
		return this.townHallCenter != null;
	}

	@Override
	public void start()
	{
		navigateToTownHall();
		this.openedDoors.clear();
	}
	
	@Override
	public boolean shouldContinue()
	{
		if (this.architect.getState() != ArchitectState.GOING_TO_TOWNHALL)
			return false;
		
		if (isAtTownHallCenter())
		{
			this.architect.setState(ArchitectState.IDLE_IN_TOWNHALL);
			return false;
		}

		// if we've reached the entrance, now head to the center
		if (this.isNavigatingToEntrance && this.entrancePos != null)
		{
			double distToEntrance = this.architect.getPos().squaredDistanceTo(Vec3d.ofCenter(this.entrancePos));
			if (distToEntrance < 2.0)
				navigateToTownHall();
		}

		return !this.architect.getNavigation().isIdle();
	}

	@Override
	public void tick()
	{
		handleDoorsInPath();
		checkToCloseDoors();
		updateStuckTicks();

		// if stuck for 5 ticks, try to recover
		if (this.stuckTicks >= 5)
		{
			if (this.pathfindingAttempts < MAX_PATHFINDING_ATTEMPTS)
				retryPathfinding();
			
			// if we've tried multiple path attempts and still stuck, teleport
			else if (!isAtTownHallCenter())
				teleportTowardsTownHall();
		}

		this.lastTickPosition = this.architect.getPos();
	}
	
	@Override
	public void stop()
	{
		closeAllDoors();
	}

	private void navigateToTownHall()
	{
		this.isNavigatingToEntrance = false;
		this.architect.getNavigation().startMovingTo(this.townHallCenter.getX(), this.townHallCenter.getY(), this.townHallCenter.getZ(), this.speed);
	}

	private void retryPathfinding()
	{
		//Frontier.LOGGER.info("Architect stuck, retrying pathfinding (attempt " + (pathfindingAttempts + 1) + ")");

		this.pathfindingAttempts++;
		this.stuckTicks = 0;

		// if we were trying to go to the entrance and got stuck, try going straight to center instead
		if (this.isNavigatingToEntrance)
			navigateToTownHall();
		
		// if we were already going to center and got stuck, try a slightly different target position
		else
		{
			// offset the target position slightly
			int offsetX = (this.pathfindingAttempts % 2 == 0) ? 2 : -2;
			int offsetZ = (this.pathfindingAttempts % 3 == 0) ? 2 : -2;

			BlockPos newTarget = this.townHallCenter.add(offsetX, 0, offsetZ);
			this.architect.getNavigation().startMovingTo(newTarget.getX(), newTarget.getY(), newTarget.getZ(), this.speed);
		}
	}

	private void handleDoorsInPath()
	{
		ServerWorld world = (ServerWorld) this.architect.getEntityWorld();
		EntityNavigation navigation = this.architect.getNavigation();
		Path path = navigation.getCurrentPath();

		// if no path or at destination, nothing to do
		if (path == null || path.isFinished())
			return;

		BlockPos entityPos = this.architect.getBlockPos();

		// look for doors in a larger area around the architect
		for (int xOffset = -2; xOffset <= 2; xOffset++)
		{
			for (int zOffset = -2; zOffset <= 2; zOffset++)
			{
				// skip diagonal checks that are too far away
				if (Math.abs(xOffset) == 2 && Math.abs(zOffset) == 2)
					continue;

				// check at eye level and below
				for (int yOffset = 0; yOffset <= 1; yOffset++)
				{
					BlockPos checkPos = entityPos.add(xOffset, yOffset, zOffset);

					// check if this position contains a door/gate
					BlockState state = world.getBlockState(checkPos);
					boolean isDoor = state.getBlock() instanceof DoorBlock && !state.get(Properties.OPEN);
					boolean isGate = state.getBlock() instanceof FenceGateBlock && !state.get(FenceGateBlock.OPEN);

					if (isDoor || isGate)
					{
						// check if the door is in our path or nearby
						boolean shouldOpen = false;

						// check if it's in front of us in our movement direction
						Vec3d moveDir = getMoveDirection();
						Vec3d doorDir = new Vec3d(checkPos.getX() - entityPos.getX(), 0, checkPos.getZ() - entityPos.getZ()).normalize();

						double dotProduct = moveDir.dotProduct(doorDir);

						// check if door is in front in our direction
						if (dotProduct > 0.5)
							shouldOpen = true;

						// check if door is in our navigation path
						if (!shouldOpen && path != null)
						{
							for (int i = path.getCurrentNodeIndex(); i < Math.min(path.getCurrentNodeIndex() + 5, path.getLength()); i++)
							{
								BlockPos pathNode = path.getNodePos(i);
								if (pathNode.getManhattanDistance(checkPos) <= 2)
								{
									shouldOpen = true;
									break;
								}
							}
						}

						if (shouldOpen)
						{
							if (openDoorIfNeeded(checkPos))
							{
								// recalculate path when we open a door
								if (this.isNavigatingToEntrance && this.entrancePos != null)
									this.architect.getNavigation().startMovingTo(this.entrancePos.getX(), this.entrancePos.getY(), this.entrancePos.getZ(), this.speed);
								else
									this.architect.getNavigation().startMovingTo(this.townHallCenter.getX(), this.townHallCenter.getY(), this.townHallCenter.getZ(), this.speed);
							}
						}
					}
				}
			}
		}
	}

	private boolean openDoorIfNeeded(BlockPos pos)
	{
		ServerWorld world = (ServerWorld) this.architect.getEntityWorld();
		BlockState state = world.getBlockState(pos);

		try
		{
			if (state.getBlock() instanceof DoorBlock)
			{
				if (!state.get(Properties.OPEN))
				{
					BlockState newState = state.with(Properties.OPEN, true);
					world.setBlockState(pos, newState, 10);

					// handle double-height doors
					if (state.contains(Properties.DOUBLE_BLOCK_HALF))
					{
						if (state.get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER)
						{
							BlockState upperState = world.getBlockState(pos.up());
							if (upperState.getBlock() instanceof DoorBlock)
								world.setBlockState(pos.up(), upperState.with(Properties.OPEN, true), 10);
						}
						else
						{
							BlockState lowerState = world.getBlockState(pos.down());
							if (lowerState.getBlock() instanceof DoorBlock)
								world.setBlockState(pos.down(), lowerState.with(Properties.OPEN, true), 10);
						}
					}

					// play door open sound
					world.syncWorldEvent(null, 1005, pos, 0);

					// record the current tick when the door was opened
					openedDoors.put(pos, architect.age);
					return true;
				}
			}
			else if (state.getBlock() instanceof FenceGateBlock)
			{
				if (!state.get(FenceGateBlock.OPEN))
				{
					world.setBlockState(pos, state.with(FenceGateBlock.OPEN, true), 10);

					// play gate open sound
					world.syncWorldEvent(null, 1008, pos, 0);

					// record the current tick when the gate was opened
					openedDoors.put(pos, architect.age);
					return true;
				}
			}
		}
		catch (Exception e)
		{
			Frontier.LOGGER.error("Error opening door at " + pos + ": " + e.getMessage());
		}
		return false;
	}

	private void checkToCloseDoors()
	{
		BlockPos entityPos = this.architect.getBlockPos();
		ServerWorld world = (ServerWorld) this.architect.getEntityWorld();

		Set<BlockPos> doorsToRemove = new HashSet<>();

		// check each door we've opened
		for (Map.Entry<BlockPos, Integer> entry : openedDoors.entrySet())
		{
			BlockPos doorPos = entry.getKey();
			int openedAtTick = entry.getValue();
			int ticksOpen = this.architect.age - openedAtTick;

			// close the door if either:
			// 1. the architect has moved far enough away
			// 2. the door has been open for the specified amount of time
			boolean farEnoughAway = doorPos.getManhattanDistance(entityPos) >= DOOR_CLOSE_DISTANCE;
			boolean openTooLong = ticksOpen >= DOOR_CLOSE_TICKS;

			if (farEnoughAway || openTooLong)
			{
				// check if the door still exists and is open
				BlockState state = world.getBlockState(doorPos);

				if (state.getBlock() instanceof DoorBlock)
				{
					if (state.get(Properties.OPEN))
						closeDoor(doorPos);
				}
				else if (state.getBlock() instanceof FenceGateBlock)
				{
					if (state.get(FenceGateBlock.OPEN))
						closeGate(doorPos);
				}

				doorsToRemove.add(doorPos);
			}
		}

		// remove closed doors from our tracking set
		for (BlockPos pos : doorsToRemove)
		{
			openedDoors.remove(pos);
		}
	}

	private void closeDoor(BlockPos pos)
	{
		ServerWorld world = (ServerWorld) this.architect.getEntityWorld();
		BlockState state = world.getBlockState(pos);

		try
		{
			// set closed state
			BlockState newState = state.with(Properties.OPEN, false);
			world.setBlockState(pos, newState, 10);

			// handle double-height doors
			if (state.contains(Properties.DOUBLE_BLOCK_HALF))
			{
				if (state.get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER)
				{
					BlockState upperState = world.getBlockState(pos.up());
					if (upperState.getBlock() instanceof DoorBlock)
					{
						world.setBlockState(pos.up(), upperState.with(Properties.OPEN, false), 10);
					}
				}
				else
				{
					BlockState lowerState = world.getBlockState(pos.down());
					if (lowerState.getBlock() instanceof DoorBlock)
					{
						world.setBlockState(pos.down(), lowerState.with(Properties.OPEN, false), 10);
					}
				}
			}

			// play door close sound
			world.syncWorldEvent(null, 1006, pos, 0);
		}
		catch (Exception e)
		{
			Frontier.LOGGER.error("Error closing door at " + pos + ": " + e.getMessage());
		}
	}

	private void closeGate(BlockPos pos)
	{
		ServerWorld world = (ServerWorld) this.architect.getEntityWorld();
		BlockState state = world.getBlockState(pos);

		try
		{
			world.setBlockState(pos, state.with(FenceGateBlock.OPEN, false), 10);

			// play gate close sound
			world.syncWorldEvent(null, 1014, pos, 0);
		}
		catch (Exception e)
		{
			Frontier.LOGGER.error("Error closing gate at " + pos + ": " + e.getMessage());
		}
	}

	private void closeAllDoors()
	{
		for (BlockPos doorPos : new HashSet<>(openedDoors.keySet()))
		{
			ServerWorld world = (ServerWorld) this.architect.getEntityWorld();
			BlockState state = world.getBlockState(doorPos);

			if (state.getBlock() instanceof DoorBlock)
			{
				if (state.get(Properties.OPEN))
					closeDoor(doorPos);
			}
			else if (state.getBlock() instanceof FenceGateBlock)
			{
				if (state.get(FenceGateBlock.OPEN))
					closeGate(doorPos);
			}
		}

		openedDoors.clear();
	}

	private Vec3d getMoveDirection()
	{
		Vec3d moveDir;

		// first try to get direction from velocity
		moveDir = this.architect.getVelocity().normalize();

		// if velocity is too small, use navigation target
		if (moveDir.length() < 0.05)
		{
			if (this.architect.getNavigation().getTargetPos() != null)
			{
				BlockPos targetPos = this.architect.getNavigation().getTargetPos();
				BlockPos entityPos = this.architect.getBlockPos();

				moveDir = new Vec3d(targetPos.getX() - entityPos.getX(), 0, targetPos.getZ() - entityPos.getZ()).normalize();
			}
			else if (townHallCenter != null)
			{
				// fall back to town hall direction
				BlockPos entityPos = this.architect.getBlockPos();
				moveDir = new Vec3d(townHallCenter.getX() - entityPos.getX(), 0, townHallCenter.getZ() - entityPos.getZ()).normalize();
			}
			else
			{
				// last resort: look direction
				moveDir = this.architect.getRotationVector().normalize();
			}
		}

		return moveDir;
	}

	private boolean isAtTownHallCenter()
	{
		if (this.townHallCenter == null)
			return false;
		
		double distanceSquared = this.architect.getPos().squaredDistanceTo(this.townHallCenter.getX(), this.townHallCenter.getY(), this.townHallCenter.getZ());
		return distanceSquared <= DISTANCE * DISTANCE;
	}

	private void updateStuckTicks()
	{
		Vec3d currentPos = this.architect.getPos();
		if (currentPos.squaredDistanceTo(this.lastTickPosition) < 0.0001)
			this.stuckTicks++;
		else
			this.stuckTicks = 0;
	}

	private void teleportTowardsTownHall()
	{
		Frontier.LOGGER.info("Architect stuck at (" + (int) this.architect.getPos().x + ", " + (int) this.architect.getPos().y + ", " + (int) this.architect.getPos().z + "), teleporting!");

		double squaredDistance = Math.pow(this.townHallCenter.getX() - this.architect.getX(), 2) + Math.pow(this.townHallCenter.getY() - this.architect.getY(), 2) + Math.pow(this.townHallCenter.getZ() - this.architect.getZ(), 2);
		BlockPos targetPos;

		// if within 32 blocks, teleport directly to town hall
		if (squaredDistance <= 32.0 * 32.0)
			targetPos = this.townHallCenter;
		else
		{
			Vec3d directionToTownHall = new Vec3d(this.townHallCenter.getX() - this.architect.getX(), this.townHallCenter.getY() - this.architect.getY(), this.townHallCenter.getZ() - this.architect.getZ()).normalize();
			targetPos = new BlockPos((int) (this.architect.getX() + directionToTownHall.x * 5), 0, (int) (this.architect.getZ() + directionToTownHall.z * 5));

			// ensuring it teleports to the highest surface block
			int surfaceY = this.architect.getWorld().getTopY(Heightmap.Type.WORLD_SURFACE, targetPos.getX(), targetPos.getZ());
			targetPos = new BlockPos(targetPos.getX(), surfaceY, targetPos.getZ());
		}

		this.architect.updatePosition(targetPos.getX(), targetPos.getY(), targetPos.getZ());
		this.stuckTicks = 0;

		// start new path from the teleport position
		this.architect.getNavigation().startMovingTo(this.townHallCenter.getX(), this.townHallCenter.getY(), this.townHallCenter.getZ(), this.speed);
	}
}