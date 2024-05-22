package com.frontier.goals.nomad;

import com.frontier.entities.NomadEntity;
import com.frontier.entities.SettlerEntity;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;

public class MoveTowardsBellGoal extends Goal 
{
    private final NomadEntity nomad;
    private final double speed;
    private Vec3d lastTickPosition;
    private int stuckTicks;

    public MoveTowardsBellGoal(NomadEntity nomad, double speed) 
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
        return this.nomad instanceof SettlerEntity && world.isDay() && this.nomad.getBellPosition() != null;
    }

    @Override
    public void start() 
    {
    	this.nomad.getNavigation().startMovingTo(this.nomad.getBellPosition().getX(), this.nomad.getBellPosition().getY(), this.nomad.getBellPosition().getZ(), this.speed);
    }

    @Override
    public boolean shouldContinue() 
    {
        return isInBellRange();
    }

    @Override
    public void tick() 
    {
        updateStuckTicks();

        if (this.stuckTicks >= 60 && !isInBellRange())
            teleportTowardsBell();

        this.lastTickPosition = this.nomad.getPos();
    }
    
    private boolean isInBellRange() 
    {
        BlockPos bellPos = this.nomad.getBellPosition();
        return bellPos != null && bellPos.isWithinDistance(this.nomad.getPos(), 10D);
    }

    private void updateStuckTicks() 
    {
        if (this.nomad.getPos().equals(this.lastTickPosition))
            this.stuckTicks++;
        else 
            this.stuckTicks = 0;
    }

    private void teleportTowardsBell() 
    {
        System.out.println("Nomad stuck at (" + (int)this.nomad.getPos().x + ", " + (int)this.nomad.getPos().y + ", " + (int)this.nomad.getPos().z + "), teleporting!");

        BlockPos bellPos = this.nomad.getBellPosition();
        Vec3d directionToBell = new Vec3d(bellPos.getX() - this.nomad.getX(), bellPos.getY() - this.nomad.getY(), bellPos.getZ() - this.nomad.getZ()).normalize();
        BlockPos targetPos = new BlockPos((int)(this.nomad.getX() + directionToBell.x * 5), 0, (int)(this.nomad.getZ() + directionToBell.z * 5));

        // ensuring it teleports to the highest surface block
        int surfaceY = this.nomad.getWorld().getTopY(Heightmap.Type.WORLD_SURFACE, targetPos.getX(), targetPos.getZ());
        targetPos = new BlockPos(targetPos.getX(), surfaceY, targetPos.getZ());

        this.nomad.updatePosition(targetPos.getX(), targetPos.getY(), targetPos.getZ());
        this.stuckTicks = 0;
    }
}
