package com.frontier.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.frontier.util.HostileMobMixinGoal;

import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.mob.MobEntity;

@Mixin(EndermiteEntity.class)
public class EndermiteEntityMixin
{
	@Inject(method = "<init>", at = @At("TAIL"))
	private void makeEndermiteTargetSettler(CallbackInfo ci)
	{
		HostileMobMixinGoal.addTargetGoal((MobEntity) (Object) this);
	}
}