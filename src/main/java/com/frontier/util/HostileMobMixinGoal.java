package com.frontier.util;

import com.frontier.entities.SettlerEntity;
import com.frontier.mixin.MobEntityAccessor;

import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;

public class HostileMobMixinGoal
{
	// hostile mobs that won't target frontier entities: breeze, enderman, phantom, shulker, piglin brute, hoglin, and zoglin
	
	public static void addTargetGoal(MobEntity mobEntity)
	{
		GoalSelector targetSelector = ((MobEntityAccessor) mobEntity).getTargetSelector();
		targetSelector.add(1, new ActiveTargetGoal<>(mobEntity, SettlerEntity.class, true));
	}
}