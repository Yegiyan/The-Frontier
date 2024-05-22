package com.frontier.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.frontier.renderers.RegionMapRenderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.map.MapState;

@Mixin(MapRenderer.class)
public class MapRendererMixin 
{
	@Inject(at = @At("HEAD"), method = "draw", cancellable = true)
	private void draw(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int mapId, MapState mapState, boolean bl, int light, CallbackInfo info) 
	{
		try (RegionMapRenderer customRenderer = new RegionMapRenderer(MinecraftClient.getInstance().getTextureManager()))
		{
			customRenderer.draw(matrices, vertexConsumers, mapId, mapState, bl, light);
		}
    }
}