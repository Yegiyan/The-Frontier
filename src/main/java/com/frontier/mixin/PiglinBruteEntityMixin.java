package com.frontier.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.frontier.util.HostileMobMixinGoal;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinBruteEntity;

@Mixin(PiglinBruteEntity.class)
public class PiglinBruteEntityMixin
{
	@Inject(method = "<init>", at = @At("TAIL"))
	private void makePiglinBruteTargetSettler(CallbackInfo ci)
	{
		HostileMobMixinGoal.addTargetGoal((MobEntity) (Object) this);
	}
}