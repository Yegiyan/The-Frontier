package com.frontier.renderers;

import java.util.Set;

import org.joml.Matrix4f;

import com.frontier.Frontier;
import com.frontier.PlayerData;
import com.frontier.settlements.SettlementManager;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;

public class TerritoryRenderer
{
	private static final Identifier FORCEFIELD_TEXTURE = new Identifier("minecraft", "textures/misc/forcefield.png");

	private static final int VISIBLE_RANGE = 24;
	private static final float SCROLL_SPEED = 200.0f;
	private static final float R = 130;
	private static final float G = 195;
	private static final float B = 255;
	private static final float A = 1.0f;

	public static void register()
	{
		WorldRenderEvents.LAST.register(context ->
		{
			MatrixStack matrixStack = context.matrixStack();
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player != null)
			{
				PlayerData playerData = PlayerData.players.get(client.player.getUuid());
				if (playerData.getProfession().equals("Leader") && SettlementManager.getSettlement(playerData.getFaction()) != null)
				{
					Set<ChunkPos> territory = SettlementManager.getSettlement(playerData.getFaction()).getTerritory();
					TerritoryRenderer.drawTerritoryEdge(client, matrixStack, client.player, playerData,  territory);
				}
			}
		});
	}

	public static void drawTerritoryEdge(MinecraftClient client, MatrixStack matrixStack, PlayerEntity leader, PlayerData leaderData, Set<ChunkPos> territory)
	{
		BlockPos leaderPos = leader.getBlockPos();

		int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
		for (ChunkPos chunkPos : territory)
		{
			minX = Math.min(minX, chunkPos.getStartX());
			minZ = Math.min(minZ, chunkPos.getStartZ());
			maxX = Math.max(maxX, chunkPos.getEndX());
			maxZ = Math.max(maxZ, chunkPos.getEndZ());
		}

		int[] yLevels = { leaderPos.getY() - 3, leaderPos.getY() - 2, leaderPos.getY() - 1, leaderPos.getY(), leaderPos.getY() + 1, leaderPos.getY() + 2, leaderPos.getY() + 3 };

		for (int y : yLevels)
		{
			for (int x = minX; x <= maxX; x++)
			{
				for (int z = minZ; z <= maxZ; z++)
				{
					boolean isEdge = x == minX || x == maxX || z == minZ || z == maxZ;
					boolean isWithinRange = Math.abs(leaderPos.getX() - x) <= VISIBLE_RANGE && Math.abs(leaderPos.getZ() - z) <= VISIBLE_RANGE;
					if (isEdge && isWithinRange)
					{
						BlockPos pos = new BlockPos(x, y, z);
						Direction direction = Direction.NORTH;

						if (x == minX)
							direction = Direction.WEST;
						else if (x == maxX)
							direction = Direction.EAST;
						else if (z == minZ)
							direction = Direction.NORTH;
						else if (z == maxZ)
							direction = Direction.SOUTH;

						TerritoryRenderer.drawForcefield(matrixStack, client, pos, direction); // Red color with 50% opacity

						// draw extra texture to connect corners
						if ((x == minX || x == maxX) && (z == minZ || z == maxZ))
							drawCorner(matrixStack, client, pos, x == minX, z == minZ);
					}
				}
			}
		}
	}

	private static void drawCorner(MatrixStack matrixStack, MinecraftClient client, BlockPos pos, boolean isMinX, boolean isMinZ)
	{
		Direction direction = null;

		if (isMinX && isMinZ)
			direction = Direction.NORTH; // Top-left corner if facing North and West.
		else if (isMinX && !isMinZ)
			direction = Direction.SOUTH; // Bottom-left corner if facing South and West.
		else if (!isMinX && isMinZ)
			direction = Direction.NORTH; // Top-right corner if facing North and East.
		else if (!isMinX && !isMinZ)
			direction = Direction.SOUTH; // Bottom-right corner if facing South and East.

		TerritoryRenderer.drawForcefield(matrixStack, client, pos, direction);
	}

	public static void drawForcefield(MatrixStack matrixStack, MinecraftClient client, BlockPos pos, Direction direction)
	{
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderTexture(0, FORCEFIELD_TEXTURE);

		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

		RenderSystem.setShaderColor(convertRGB(R), convertRGB(G), convertRGB(B), A);

		matrixStack.push();
		matrixStack.translate(pos.getX() - client.player.getX(), pos.getY() - client.player.getY(), pos.getZ() - client.player.getZ());
		Matrix4f matrix = matrixStack.peek().getPositionMatrix();

		float size = 1f;

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

		long gameTime = client.world.getTime();
		switch (direction)
		{
		case NORTH:
			drawQuad(buffer, matrix, 0, 0, 0, size, 0, 0, size, size, 0, 0, size, 0, gameTime, true);
			drawQuad(buffer, matrix, 0, size, 0, size, size, 0, size, 0, 0, 0, 0, 0, gameTime, true);
			break;
		case SOUTH:
			drawQuad(buffer, matrix, 0, 0, size, size, 0, size, size, size, size, 0, size, size, gameTime, false);
			drawQuad(buffer, matrix, 0, size, size, size, size, size, size, 0, size, 0, 0, size, gameTime, false);
			break;
		case WEST:
			drawQuad(buffer, matrix, 0, 0, 0, 0, 0, size, 0, size, size, 0, size, 0, gameTime, false);
			drawQuad(buffer, matrix, 0, size, 0, 0, size, size, 0, 0, size, 0, 0, 0, gameTime, false);
			break;
		case EAST:
			drawQuad(buffer, matrix, size, 0, 0, size, 0, size, size, size, size, size, size, 0, gameTime, true);
			drawQuad(buffer, matrix, size, size, 0, size, size, size, size, 0, size, size, 0, 0, gameTime, true);
			break;
		default:
			Frontier.LOGGER.error("TerritoryRenderer - quad direction not found!");
			break;
		}

		tessellator.draw();
		matrixStack.pop();

		RenderSystem.disableBlend();
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f); // reset to default color (else hand & held items become RGB'd)
	}

	private static void drawQuad(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4,
			float time, boolean reverse)
	{
		float textureOffset = (time % 1000) / SCROLL_SPEED;
		if (reverse)
			textureOffset = -textureOffset;

		buffer.vertex(matrix, x1, y1, z1).texture(0 + textureOffset, 0 + textureOffset).next();
		buffer.vertex(matrix, x2, y2, z2).texture(1 + textureOffset, 0 + textureOffset).next();
		buffer.vertex(matrix, x3, y3, z3).texture(1 + textureOffset, 1 + textureOffset).next();
		buffer.vertex(matrix, x4, y4, z4).texture(0 + textureOffset, 1 + textureOffset).next();
	}

	public static float convertRGB(float color)
	{
		return color / 255.0f;
	}
}