package com.frontier.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.frontier.util.HostileMobMixinGoal;

import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;

@Mixin(CreeperEntity.class)
public class CreeperEntityMixin
{
	@Inject(method = "<init>", at = @At("TAIL"))
	private void makeCreeperTargetSettler(CallbackInfo ci)
	{
		HostileMobMixinGoal.addTargetGoal((MobEntity) (Object) this);
	}
}