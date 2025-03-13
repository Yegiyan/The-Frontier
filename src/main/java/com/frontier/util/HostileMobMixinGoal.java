package com.frontier.util;

import com.frontier.entities.settler.SettlerEntity;
import com.frontier.mixin.MobEntityAccessor;

import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;

public class HostileMobMixinGoal
{
	// hostile mobs that won't target frontier entities at the moment: breeze, bogged, enderman, phantom, shulker, piglin brute, hoglin, and zoglin
	// make sure other future custom entities (similar to SettlerEntity) are also targeted if appropriate
	
	public static void addTargetGoal(MobEntity mobEntity)
	{
		GoalSelector targetSelector = ((MobEntityAccessor) mobEntity).getTargetSelector();
		targetSelector.add(1, new ActiveTargetGoal<>(mobEntity, SettlerEntity.class, true));
	}
}