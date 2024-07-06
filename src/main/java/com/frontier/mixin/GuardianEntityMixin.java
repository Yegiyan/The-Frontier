package com.frontier.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.frontier.util.HostileMobMixinGoal;

import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.MobEntity;

@Mixin(GuardianEntity.class)
public class GuardianEntityMixin
{
	@Inject(method = "<init>", at = @At("TAIL"))
	private void makeGuardianTargetSettler(CallbackInfo ci)
	{
		HostileMobMixinGoal.addTargetGoal((MobEntity) (Object) this);
	}
}