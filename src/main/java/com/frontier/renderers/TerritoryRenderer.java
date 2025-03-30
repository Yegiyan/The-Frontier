package com.frontier.renderers;

import java.util.Set;

import com.frontier.PlayerData;
import com.frontier.settlements.SettlementManager;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ChunkPos;

public class TerritoryRenderer
{
	public static void register()
	{
		WorldRenderEvents.LAST.register(context ->
		{
			MatrixStack matrixStack = context.matrixStack();
			MinecraftClient client = MinecraftClient.getInstance();
			Camera camera = context.camera();

			if (client.player != null)
			{
				PlayerData playerData = PlayerData.players.get(client.player.getUuid());
				if (playerData != null)
				{
					if (playerData.getProfession().equals("Leader") && SettlementManager.getSettlement(playerData.getFaction()) != null && SettlementManager.getSettlement(playerData.getFaction()).isLeaderHoldingClock(client.getServer()))
					{
						Set<ChunkPos> territory = SettlementManager.getSettlement(playerData.getFaction()).getTerritory();
						ForcefieldRenderer.drawForcefield(client, matrixStack, client.player, territory, 3, camera);
					}
				}
			}
		});
	}
}