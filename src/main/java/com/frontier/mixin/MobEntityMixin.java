package com.frontier.mixin;

import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.ZombieEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.frontier.entities.SettlerEntity;

@Mixin(ZombieEntity.class)
public class MobEntityMixin
{
	@Inject(method = "initGoals", at = @At("TAIL"))
	private void makeZombieTargetSettler(CallbackInfo ci)
	{
		((MobEntityAccessor) this).getTargetSelector().add(1,
				new ActiveTargetGoal<>((ZombieEntity) (Object) this, SettlerEntity.class, true));
	}
}