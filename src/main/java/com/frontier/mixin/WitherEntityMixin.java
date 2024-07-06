package com.frontier.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.frontier.util.HostileMobMixinGoal;

import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.MobEntity;

@Mixin(WitherEntity.class)
public class WitherEntityMixin
{
	@Inject(method = "<init>", at = @At("TAIL"))
	private void makeWitherTargetSettler(CallbackInfo ci)
	{
		HostileMobMixinGoal.addTargetGoal((MobEntity) (Object) this);
	}
}