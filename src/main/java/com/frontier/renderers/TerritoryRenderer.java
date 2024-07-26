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
					drawTerritoryEdge(client, matrixStack, client.player, playerData, territory);
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

		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderTexture(0, FORCEFIELD_TEXTURE);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.setShaderColor(convertRGB(R), convertRGB(G), convertRGB(B), A);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		long gameTime = client.world.getTime();
		float textureOffset = (gameTime % 1000) / SCROLL_SPEED;

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
						Direction direction = getDirection(minX, maxX, minZ, maxZ, x, z);
						drawForcefield(matrixStack, buffer, tessellator, client, pos, direction, textureOffset, 0.0f, 0.0f);
						if ((x == minX || x == maxX) && (z == minZ || z == maxZ))
						{
							drawCorner(matrixStack, buffer, tessellator, client, pos, x == minX, z == minZ, textureOffset, 0.0f, 0.0f);
						}
					}
				}
			}
		}

		RenderSystem.disableBlend();
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
	}

	private static Direction getDirection(int minX, int maxX, int minZ, int maxZ, int x, int z)
	{
		if (x == minX)
			return Direction.WEST;
		if (x == maxX)
			return Direction.EAST;
		if (z == minZ)
			return Direction.NORTH;
		if (z == maxZ)
			return Direction.SOUTH;
		return Direction.NORTH;
	}

	private static void drawCorner(MatrixStack matrixStack, BufferBuilder buffer, Tessellator tessellator, MinecraftClient client, BlockPos pos, boolean isMinX, boolean isMinZ, float textureOffset, float textureOffsetX, float textureOffsetY)
	{
		Direction direction = (isMinX ? (isMinZ ? Direction.NORTH : Direction.SOUTH) : (isMinZ ? Direction.NORTH : Direction.SOUTH));
		drawForcefield(matrixStack, buffer, tessellator, client, pos, direction, textureOffset, textureOffsetX, textureOffsetY);
	}

	public static void drawForcefield(MatrixStack matrixStack, BufferBuilder buffer, Tessellator tessellator, MinecraftClient client, BlockPos pos, Direction direction, float textureOffset, float textureOffsetX, float textureOffsetY)
	{
		double interpolatedX = client.player.prevX + (client.player.getX() - client.player.prevX) * client.getTickDelta();
		double interpolatedY = client.player.prevY + (client.player.getY() - client.player.prevY) * client.getTickDelta();
		double interpolatedZ = client.player.prevZ + (client.player.getZ() - client.player.prevZ) * client.getTickDelta();

		matrixStack.push();
		matrixStack.translate(pos.getX() - interpolatedX, pos.getY() - interpolatedY, pos.getZ() - interpolatedZ);
		Matrix4f matrix = matrixStack.peek().getPositionMatrix();

		float size = 1f;
		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

		switch (direction)
		{
		case NORTH ->
		{
			drawQuad(buffer, matrix, 0, 0, 0, size, 0, 0, size, size, 0, 0, size, 0, -textureOffset, textureOffsetX, textureOffsetY, true);
			drawQuad(buffer, matrix, 0, size, 0, size, size, 0, size, 0, 0, 0, 0, 0, -textureOffset, textureOffsetX, textureOffsetY, true);
		}
		case SOUTH ->
		{
			drawQuad(buffer, matrix, 0, 0, size, size, 0, size, size, size, size, 0, size, size, textureOffset, textureOffsetX, textureOffsetY, false);
			drawQuad(buffer, matrix, 0, size, size, size, size, size, size, 0, size, 0, 0, size, textureOffset, textureOffsetX, textureOffsetY, false);
		}
		case WEST ->
		{
			drawQuad(buffer, matrix, 0, 0, 0, 0, 0, size, 0, size, size, 0, size, 0, textureOffset, textureOffsetX, textureOffsetY, false);
			drawQuad(buffer, matrix, 0, size, 0, 0, size, size, 0, 0, size, 0, 0, 0, textureOffset, textureOffsetX, textureOffsetY, false);
		}
		case EAST ->
		{
			drawQuad(buffer, matrix, size, 0, 0, size, 0, size, size, size, size, size, size, 0, -textureOffset, textureOffsetX, textureOffsetY, true);
			drawQuad(buffer, matrix, size, size, 0, size, size, size, size, 0, size, size, 0, 0, -textureOffset, textureOffsetX, textureOffsetY, true);
		}
		default -> Frontier.LOGGER.error("TerritoryRenderer - quad direction not found!");
		}

		tessellator.draw();
		matrixStack.pop();
	}

	private static void drawQuad(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float textureOffset, float textureOffsetX, float textureOffsetY, boolean flipTexture)
	{
		if (flipTexture)
		{
			buffer.vertex(matrix, x1, y1, z1).texture(textureOffsetX + 1 - textureOffset, textureOffsetY + 1 - textureOffset).next();
			buffer.vertex(matrix, x2, y2, z2).texture(textureOffsetX + 0 - textureOffset, textureOffsetY + 1 - textureOffset).next();
			buffer.vertex(matrix, x3, y3, z3).texture(textureOffsetX + 0 - textureOffset, textureOffsetY + 0 - textureOffset).next();
			buffer.vertex(matrix, x4, y4, z4).texture(textureOffsetX + 1 - textureOffset, textureOffsetY + 0 - textureOffset).next();
		}
		else
		{
			buffer.vertex(matrix, x1, y1, z1).texture(textureOffsetX + 0 + textureOffset, textureOffsetY + 0 + textureOffset).next();
			buffer.vertex(matrix, x2, y2, z2).texture(textureOffsetX + 1 + textureOffset, textureOffsetY + 0 + textureOffset).next();
			buffer.vertex(matrix, x3, y3, z3).texture(textureOffsetX + 1 + textureOffset, textureOffsetY + 1 + textureOffset).next();
			buffer.vertex(matrix, x4, y4, z4).texture(textureOffsetX + 0 + textureOffset, textureOffsetY + 1 + textureOffset).next();
		}
	}

	public static float convertRGB(float color)
	{
		return color / 255.0f;
	}
}
