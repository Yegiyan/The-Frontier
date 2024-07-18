package com.frontier.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.frontier.entities.settler.SettlerEntity;

import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.MobEntity;

@Mixin(EnderDragonEntity.class)
public class EnderDragonEntityMixin
{
	@Inject(method = "<init>", at = @At("TAIL"))
	private void makeEnderDragonTargetSettler(CallbackInfo ci)
	{
		((MobEntityAccessor) this).getTargetSelector().add(1,
				new ActiveTargetGoal<>((MobEntity) (Object) this, SettlerEntity.class, true));
	}
}