package com.frontier.goals.nomad;

import java.util.Random;

import com.frontier.Frontier;
import com.frontier.entities.NomadEntity;
import com.frontier.entities.SettlerEntity;

import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;

public class MoveTowardsDespawnGoal extends Goal 
{
    private final NomadEntity nomad;
    private final double speed;
    private Vec3d lastTickPosition;
    private int stuckTicks;
    private BlockPos despawnPos;

    public MoveTowardsDespawnGoal(NomadEntity nomad, double speed) 
    {
        this.nomad = nomad;
        this.speed = speed;
        this.lastTickPosition = nomad.getPos();
        this.stuckTicks = 0;
    }

    @Override
    public boolean canStart() 
    {
    	ServerWorld world = (ServerWorld) this.nomad.getEntityWorld();
        return this.nomad instanceof SettlerEntity && world.isNight();
    }

    @Override
    public void start() 
    {
        ServerWorld world = (ServerWorld) this.nomad.getEntityWorld();
        this.nomad.getNavigation().startMovingTo(getDespawnPos(world).getX(), getDespawnPos(world).getY(), getDespawnPos(world).getZ(), this.speed);
    }

    @Override
    public boolean shouldContinue() 
    {
    	return isInDespawnRange();
    }

    @Override
    public void tick() 
    {
        updateStuckTicks();

        if (this.stuckTicks >= 60 && !isInDespawnRange())
            teleportTowardsDespawn();

        this.lastTickPosition = this.nomad.getPos();
        
        if (isInDespawnRange())
        	this.nomad.remove(RemovalReason.DISCARDED);
    }

    private boolean isInDespawnRange() 
    {
        ServerWorld world = (ServerWorld) this.nomad.getEntityWorld();
        BlockPos despawnPos = getDespawnPos(world);
        return despawnPos.isWithinDistance(this.nomad.getPos(), 10D);
    }

    private void updateStuckTicks() 
    {
        if (this.nomad.getPos().equals(this.lastTickPosition))
            this.stuckTicks++;
        else 
            this.stuckTicks = 0;
    }

    private void teleportTowardsDespawn() 
    {
    	Frontier.LOGGER.info("Nomad stuck at (" + (int)this.nomad.getPos().x + ", " + (int)this.nomad.getPos().y + ", " + (int)this.nomad.getPos().z + "), teleporting!");

        BlockPos despawnPos = getDespawnPos((ServerWorld) this.nomad.getEntityWorld());
        Vec3d directionToDespawn = new Vec3d(despawnPos.getX() - this.nomad.getX(), despawnPos.getY() - this.nomad.getY(), despawnPos.getZ() - this.nomad.getZ()).normalize();
        BlockPos targetPos = new BlockPos((int)(this.nomad.getX() + directionToDespawn.x * 5), 0, (int)(this.nomad.getZ() + directionToDespawn.z * 5));

        int surfaceY = this.nomad.getWorld().getTopY(Heightmap.Type.WORLD_SURFACE, targetPos.getX(), targetPos.getZ());
        targetPos = new BlockPos(targetPos.getX(), surfaceY, targetPos.getZ());

        this.nomad.updatePosition(targetPos.getX(), targetPos.getY(), targetPos.getZ());
        this.stuckTicks = 0;
    }

    private BlockPos getDespawnPos(ServerWorld world) 
    {
        if (despawnPos != null)
            return despawnPos;

        Random rand = new Random();
        int dist = 128;

        double angle = rand.nextDouble() * Math.PI * 2;
        int x = this.nomad.getBellPosition().getX() + (int)(Math.cos(angle) * dist);
        int z = this.nomad.getBellPosition().getZ() + (int)(Math.sin(angle) * dist);
        int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);

        despawnPos = new BlockPos(x, y, z);
        return despawnPos;
    }
}

