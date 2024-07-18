package com.frontier.goals;

import java.util.EnumSet;

import com.frontier.entities.settler.SettlerEntity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;

public class SelfDefenseGoal extends TrackTargetGoal 
{
    private static final double SPEED = .45D;			 // increase to .6D for military settlers?
    private static final double ATTACK_DISTANCE = 1.75D; // increase this to 2.25D for military settlers
    private static final int ATTACK_TIME = 5;

    private final SettlerEntity settler;
    private LivingEntity attacker;
    private long lastAttackedTime;

    public SelfDefenseGoal(SettlerEntity settler) 
    {
        super(settler, false);
        this.settler = settler;
        setControls(EnumSet.of(Goal.Control.TARGET));
    }

    @Override
    public boolean canStart() 
    {
        this.attacker = this.settler.getAttacker();
        if (this.attacker != null && this.attacker.isAlive()) 
        {
            this.lastAttackedTime = this.settler.getWorld().getTime();
            return true;
        }
        return false;
    }

    @Override
    public void start() 
    {
        this.mob.setTarget(this.attacker);
        super.start();
    }

    @Override
    public void tick() 
    {
        super.tick();
        
        moveTowardsAttacker();
        attackIfInRange();
        forgiveIfTimeExpired();
    }

    private void moveTowardsAttacker() 
    {
        this.settler.getNavigation().startMovingTo(this.attacker, SPEED);
    }

    private void attackIfInRange() 
    {
        if (this.settler.distanceTo(this.attacker) <= ATTACK_DISTANCE)
            this.settler.tryAttack(this.attacker);
    }

    private void forgiveIfTimeExpired() 
    {
        if (this.settler.getWorld().getTime() - this.lastAttackedTime > (20 * ATTACK_TIME))
            this.settler.setTarget(null);
    }
}
