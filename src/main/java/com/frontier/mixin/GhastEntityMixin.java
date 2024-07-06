package com.frontier.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.frontier.util.HostileMobMixinGoal;

import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.MobEntity;

@Mixin(GhastEntity.class)
public class GhastEntityMixin
{
	@Inject(method = "<init>", at = @At("TAIL"))
	private void makeGhastTargetSettler(CallbackInfo ci)
	{
		HostileMobMixinGoal.addTargetGoal((MobEntity) (Object) this);
	}
}