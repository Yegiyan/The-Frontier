package com.frontier.renderers;

import java.util.Set;

import org.joml.Matrix4f;

import com.frontier.Frontier;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ForcefieldRenderer
{
	private static final Identifier FORCEFIELD_TEXTURE = new Identifier("minecraft", "textures/misc/forcefield.png");
	private static final float TEXTURE_SCROLL_SPEED = 200.0f;
	private static final int VISIBLE_RANGE = 128;
	//private static final int BORDER_HEIGHT = 3;

	private static final float R = 130;
	private static final float G = 195;
	private static final float B = 255;
	private static final float A = 1.0f;

	public static void drawForcefield(MinecraftClient client, MatrixStack matrixStack, PlayerEntity leader, Set<ChunkPos> territory, int yLevelRange, Camera camera)
	{
		BlockPos leaderPos = leader.getBlockPos();
		Frustum frustum = new Frustum(matrixStack.peek().getPositionMatrix(), RenderSystem.getProjectionMatrix());
	    frustum.setPosition(camera.getPos().x, camera.getPos().y, camera.getPos().z);

		int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
		for (ChunkPos chunkPos : territory)
		{
			minX = Math.min(minX, chunkPos.getStartX());
			minZ = Math.min(minZ, chunkPos.getStartZ());
			maxX = Math.max(maxX, chunkPos.getEndX());
			maxZ = Math.max(maxZ, chunkPos.getEndZ());
		}

		int leaderY = leaderPos.getY();
		int[] yLevels = new int[yLevelRange * 2 + 1];
		for (int i = 0; i <= yLevelRange * 2; i++)
			yLevels[i] = leaderY - yLevelRange + i;

		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderTexture(0, FORCEFIELD_TEXTURE);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.setShaderColor(convertRGB(R), convertRGB(G), convertRGB(B), A);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		long gameTime = client.world.getTime();
		float textureOffset = (gameTime % 1000) / TEXTURE_SCROLL_SPEED;

		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

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
						if (frustum.isVisible(new Box(pos)))
						{
							Direction direction = getDirection(minX, maxX, minZ, maxZ, x, z);
							addForcefieldVertex(buffer, matrixStack, client, pos, direction, textureOffset, 0.0f, 0.0f, camera);
							if ((x == minX || x == maxX) && (z == minZ || z == maxZ))
								addCornerVertex(buffer, matrixStack, client, pos, x == minX, z == minZ, textureOffset, 0.0f, 0.0f, camera);
						}
					}
				}
			}
		}

		tessellator.draw();

		RenderSystem.disableBlend();
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
	}

	private static void addForcefieldVertex(BufferBuilder buffer, MatrixStack matrixStack, MinecraftClient client, BlockPos pos, Direction direction, float textureOffset, float textureOffsetX, float textureOffsetY, Camera camera)
	{
		Vec3d cameraPos = camera.getPos();

	    matrixStack.push();
	    matrixStack.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);
	    Matrix4f matrix = matrixStack.peek().getPositionMatrix();

		switch (direction)
		{
			case NORTH ->
			{
				addQuadVertices(buffer, matrix, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, -textureOffset, textureOffsetX, textureOffsetY, true);
				addQuadVertices(buffer, matrix, 0, 1, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0, -textureOffset, textureOffsetX, textureOffsetY, true);
			}
			case SOUTH ->
			{
				addQuadVertices(buffer, matrix, 0, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, textureOffset, textureOffsetX, textureOffsetY, false);
				addQuadVertices(buffer, matrix, 0, 1, 1, 1, 1, 1, 1, 0, 1, 0, 0, 1, textureOffset, textureOffsetX, textureOffsetY, false);
			}
			case WEST ->
			{
				addQuadVertices(buffer, matrix, 0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0, textureOffset, textureOffsetX, textureOffsetY, false);
				addQuadVertices(buffer, matrix, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, textureOffset, textureOffsetX, textureOffsetY, false);
			}
			case EAST ->
			{
				addQuadVertices(buffer, matrix, 1, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0, -textureOffset, textureOffsetX, textureOffsetY, true);
				addQuadVertices(buffer, matrix, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 0, -textureOffset, textureOffsetX, textureOffsetY, true);
			}
			default ->
			{
				Frontier.LOGGER.error("TerritoryRenderer - quad direction not found!");
			}
		}

		matrixStack.pop();
	}

	private static void addCornerVertex(BufferBuilder buffer, MatrixStack matrixStack, MinecraftClient client, BlockPos pos, boolean isMinX, boolean isMinZ, float textureOffset, float textureOffsetX, float textureOffsetY, Camera camera)
	{
        Direction direction = (isMinX ? (isMinZ ? Direction.NORTH : Direction.SOUTH) : (isMinZ ? Direction.NORTH : Direction.SOUTH));
        addForcefieldVertex(buffer, matrixStack, client, pos, direction, textureOffset, textureOffsetX, textureOffsetY, camera);
    }

	private static void addQuadVertices(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float textureOffset, float textureOffsetX, float textureOffsetY, boolean flipTexture)
	{
		if (flipTexture)
		{
			buffer.vertex(matrix, x1, y1, z1).texture((textureOffsetX + 1 - textureOffset), (textureOffsetY + 1 - textureOffset)).next();
			buffer.vertex(matrix, x2, y2, z2).texture((textureOffsetX + 0 - textureOffset), (textureOffsetY + 1 - textureOffset)).next();
			buffer.vertex(matrix, x3, y3, z3).texture((textureOffsetX + 0 - textureOffset), (textureOffsetY + 0 - textureOffset)).next();
			buffer.vertex(matrix, x4, y4, z4).texture((textureOffsetX + 1 - textureOffset), (textureOffsetY + 0 - textureOffset)).next();
		}
		else
		{
			buffer.vertex(matrix, x1, y1, z1).texture((textureOffsetX + 0 + textureOffset), (textureOffsetY + 0 + textureOffset)).next();
			buffer.vertex(matrix, x2, y2, z2).texture((textureOffsetX + 1 + textureOffset), (textureOffsetY + 0 + textureOffset)).next();
			buffer.vertex(matrix, x3, y3, z3).texture((textureOffsetX + 1 + textureOffset), (textureOffsetY + 1 + textureOffset)).next();
			buffer.vertex(matrix, x4, y4, z4).texture((textureOffsetX + 0 + textureOffset), (textureOffsetY + 1 + textureOffset)).next();
		}
	}
	
	public static void drawForcefieldAtPosition(MinecraftClient client, MatrixStack matrixStack, BlockPos position, int sizeX, int sizeY, int sizeZ, Camera camera)
	{
	    int minX = position.getX();
	    int minY = position.getY();
	    int minZ = position.getZ();
	    int maxX = minX + sizeX;
	    int maxY = minY + sizeY;
	    int maxZ = minZ + sizeZ;

	    // set up rendering
	    RenderSystem.setShader(GameRenderer::getPositionTexProgram);
	    RenderSystem.setShaderTexture(0, FORCEFIELD_TEXTURE);
	    RenderSystem.enableBlend();
	    RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
	    RenderSystem.setShaderColor(convertRGB(R), convertRGB(G), convertRGB(B), A);

	    // disable face culling to render inside faces
	    RenderSystem.disableCull();

	    Tessellator tessellator = Tessellator.getInstance();
	    BufferBuilder buffer = tessellator.getBuffer();
	    long gameTime = client.world.getTime();
	    float textureOffset = (gameTime % 1000) / TEXTURE_SCROLL_SPEED;

	    buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

	    // create frustum for culling
	    Frustum frustum = new Frustum(matrixStack.peek().getPositionMatrix(), RenderSystem.getProjectionMatrix());
	    frustum.setPosition(camera.getPos().x, camera.getPos().y, camera.getPos().z);

	    // draw six faces of box
	    drawFace(buffer, matrixStack, minX, minY, minZ, maxX, maxY, maxZ, Direction.WEST, textureOffset, camera, frustum);
	    drawFace(buffer, matrixStack, minX, minY, minZ, maxX, maxY, maxZ, Direction.EAST, textureOffset, camera, frustum);
	    drawFace(buffer, matrixStack, minX, minY, minZ, maxX, maxY, maxZ, Direction.DOWN, textureOffset, camera, frustum);
	    drawFace(buffer, matrixStack, minX, minY, minZ, maxX, maxY, maxZ, Direction.UP, textureOffset, camera, frustum);
	    drawFace(buffer, matrixStack, minX, minY, minZ, maxX, maxY, maxZ, Direction.NORTH, textureOffset, camera, frustum);
	    drawFace(buffer, matrixStack, minX, minY, minZ, maxX, maxY, maxZ, Direction.SOUTH, textureOffset, camera, frustum);

	    tessellator.draw();

	    // re-enable face culling after rendering
	    RenderSystem.enableCull();

	    RenderSystem.disableBlend();
	    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
	}


	private static void drawFace(BufferBuilder buffer, MatrixStack matrixStack, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Direction direction, float textureOffset, Camera camera, Frustum frustum)
	{
	    float x1, y1, z1; // first  vertex
	    float x2, y2, z2; // second vertex
	    float x3, y3, z3; // third  vertex
	    float x4, y4, z4; // fourth vertex

	    switch (direction)
	    {
	        case DOWN: // bottom face
	            x1 = minX; y1 = minY; z1 = minZ;
	            x2 = maxX; y2 = minY; z2 = minZ;
	            x3 = maxX; y3 = minY; z3 = maxZ;
	            x4 = minX; y4 = minY; z4 = maxZ;
	            break;
	        case UP: // top face
	            x1 = minX; y1 = maxY; z1 = minZ;
	            x2 = minX; y2 = maxY; z2 = maxZ;
	            x3 = maxX; y3 = maxY; z3 = maxZ;
	            x4 = maxX; y4 = maxY; z4 = minZ;
	            break;
	        case NORTH: // front face
	            x1 = minX; y1 = minY; z1 = minZ;
	            x2 = minX; y2 = maxY; z2 = minZ;
	            x3 = maxX; y3 = maxY; z3 = minZ;
	            x4 = maxX; y4 = minY; z4 = minZ;
	            break;
	        case SOUTH: // back face
	            x1 = maxX; y1 = minY; z1 = maxZ;
	            x2 = maxX; y2 = maxY; z2 = maxZ;
	            x3 = minX; y3 = maxY; z3 = maxZ;
	            x4 = minX; y4 = minY; z4 = maxZ;
	            break;
	        case WEST: // left face
	            x1 = minX; y1 = minY; z1 = maxZ;
	            x2 = minX; y2 = maxY; z2 = maxZ;
	            x3 = minX; y3 = maxY; z3 = minZ;
	            x4 = minX; y4 = minY; z4 = minZ;
	            break;
	        case EAST: // right face
	            x1 = maxX; y1 = minY; z1 = minZ;
	            x2 = maxX; y2 = maxY; z2 = minZ;
	            x3 = maxX; y3 = maxY; z3 = maxZ;
	            x4 = maxX; y4 = minY; z4 = maxZ;
	            break;
	        default:
	            return;
	    }

	    // create bounding box for frustum culling
	    Box faceBox = new Box
	    (
	    		Math.min(x1, x3), Math.min(y1, y3), Math.min(z1, z3),
	            Math.max(x1, x3), Math.max(y1, y3), Math.max(z1, z3)
	    );

	    if (!frustum.isVisible(faceBox))
	    	return;

	    Vec3d cameraPos = camera.getPos();

	    matrixStack.push();
	    matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
	    Matrix4f matrix = matrixStack.peek().getPositionMatrix();

	    // add vertices to buffer
	    buffer.vertex(matrix, x1, y1, z1).texture(0.0f, 0.0f).next();
	    buffer.vertex(matrix, x2, y2, z2).texture(0.0f, 1.0f).next();
	    buffer.vertex(matrix, x3, y3, z3).texture(1.0f, 1.0f).next();
	    buffer.vertex(matrix, x4, y4, z4).texture(1.0f, 0.0f).next();

	    matrixStack.pop();
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

	private static float convertRGB(float color)
	{
		return color / 255.0f;
	}
}