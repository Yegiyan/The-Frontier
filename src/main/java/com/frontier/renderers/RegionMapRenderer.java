package com.frontier.renderers;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import com.frontier.regions.Region;
import com.frontier.regions.RegionManager;
import com.frontier.regions.Zone;

import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.map.MapState;

public class RegionMapRenderer extends MapRenderer 
{
    public static int minX, minZ, maxX, maxZ;
    public static boolean isRenderingEnabled = false;
    
    public RegionMapRenderer(TextureManager textureManager)
    {
        super(textureManager);
    }

    @Override
    public void draw(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int id, MapState state, boolean hidePlayerIcons, int light)
    {
        renderRegions(matrices, vertexConsumers, id, state, light);
    }

	private void renderRegions(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int id, MapState state, int light)
     {
    	if (!isRenderingEnabled)
    		return;
    	
    	List<Region> regionsToRender = new ArrayList<>(RegionManager.regions);
    	VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());
    	
        for (Region region : regionsToRender)
        {
            for (Zone zone : new ArrayList<>(region.getZones()))
            {
                int mapX1 = Math.max(worldToMapCoord(zone.getX1(), state.scale), minX);
                int mapZ1 = Math.max(worldToMapCoord(zone.getZ1(), state.scale), minZ);
                int mapX2 = Math.min(worldToMapCoord(zone.getX2(), state.scale), maxX);
                int mapZ2 = Math.min(worldToMapCoord(zone.getZ2(), state.scale), maxZ);

                if (mapX1 <= mapX2 && mapZ1 <= mapZ2)
                {
                	drawRegion(matrices, vertexConsumer, state, mapX1, mapZ1, mapX2, mapZ2, light, zone.getColor());
                }
            }
        }
    }

    private void drawRegion(MatrixStack matrices, VertexConsumer vertexConsumer, MapState state, int x1, int z1, int x2, int z2, int light, byte color)
    {
        // calculate scaled map boundaries
        int scaledMinX = worldToMapCoord(minX, state.scale);
        int scaledMinZ = worldToMapCoord(minZ, state.scale);
        int scaledMaxX = worldToMapCoord(maxX, state.scale);
        int scaledMaxZ = worldToMapCoord(maxZ, state.scale);

        // clamp coordinates to scaled map boundaries
        x1 = Math.max(scaledMinX, Math.min(x1, scaledMaxX));
        z1 = Math.max(scaledMinZ, Math.min(z1, scaledMaxZ));
        x2 = Math.max(scaledMinX, Math.min(x2, scaledMaxX));
        z2 = Math.max(scaledMinZ, Math.min(z2, scaledMaxZ));

        // convert byte color to RGB values
        int r = (color >> 5) & 0x7;
        int g = (color >> 2) & 0x7;
        int b = color & 0x3;

        // scale RGB up to 255 range
        r = (r * 255) / 7;
        g = (g * 255) / 7;
        b = (b * 255) / 3;

        drawLine(matrices, vertexConsumer, state, x1, z1, x2, z1, light, r, g, b); // top
        drawLine(matrices, vertexConsumer, state, x2, z1, x2, z2, light, r, g, b); // right
        drawLine(matrices, vertexConsumer, state, x2, z2, x1, z2, light, r, g, b); // bottom
        drawLine(matrices, vertexConsumer, state, x1, z2, x1, z1, light, r, g, b); // left
        
        //drawLine(matrices, vertexConsumer, state, x1, z1, x2, z2, light, r, g, b); // top-left  to bottom-right \
        drawLine(matrices, vertexConsumer, state, x2, z1, x1, z2, light, r, g, b);   // top-right to  bottom-left /
        
        /*
        int numLines = 0;
        for (int i = 1; i < numLines; i++)
        {
        	int z = z1 + (z2 - z1) * i / numLines;
            drawLine(matrices, vertexConsumer, state, x1, z, x2, z, light, r, g, b);
        }
        */
    }

    private void drawLine(MatrixStack matrices, VertexConsumer vertexConsumer, MapState state, int x1, int z1, int x2, int z2, int light,  int r, int g, int b)
    {
        int scale = getMapScale(state.scale);
        float startX = mapToWorldCoord(x1 * scale, state.centerX, state.scale);
        float startZ = mapToWorldCoord(z1 * scale, state.centerZ, state.scale);
        float endX = mapToWorldCoord(x2 * scale, state.centerX, state.scale);
        float endZ = mapToWorldCoord(z2 * scale, state.centerZ, state.scale);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        vertexConsumer.vertex(matrix, startX, startZ, 0).color(r, g, b, 255).light(light).normal(1, 1, 1).next();
        vertexConsumer.vertex(matrix, endX, endZ, 0).color(r, g, b, 255).light(light).normal(1, 1, 1).next();
    }

    private int worldToMapCoord(int worldCoordinate, int scale)
    {
        return worldCoordinate >> scale;
    }

    private float mapToWorldCoord(int mapCoord, int mapCenterCoord, int zoomLevel)
    {
        int scale = (int) Math.pow(2, zoomLevel);
        return (mapCoord - mapCenterCoord) / (float) scale + (64 / scale);
    }
    
    public byte rgbToByte(int r, int g, int b)
    {
        r = Math.min(Math.max(0, r), 255);
        g = Math.min(Math.max(0, g), 255);
        b = Math.min(Math.max(0, b), 255);
        
        // scale down values to fit desired number of bits for each color
        // for example, for red: 3 bits can represent values 0-7 (2^3 = 8 possible values)
        r = r >> 5; // shift off 5 bits, keeping the 3 most significant bits
        g = g >> 5; // shift off 5 bits, keeping the 3 most significant bits
        b = b >> 6; // shift off 6 bits, keeping the 2 most significant bits
        
        // combine the bits: RRRGGGBB
        byte colorByte = (byte)((r << 5) | (g << 2) | b);
        return colorByte;
    }
    
    private int getMapScale(int scale) {
        return (int) Math.pow(2, scale);
    }
    
    @SuppressWarnings("unused")
	private void drawDitheredRegions(MapState state, int x1, int z1, int x2, int z2, byte color) // too much hassle, despite looking the best
    {
        int scale = getMapScale(state.scale);
        
        int startX = (int) mapToWorldCoord(x1 * scale, state.centerX, state.scale);
        int startZ = (int) mapToWorldCoord(z1 * scale, state.centerZ, state.scale);
        int endX = (int) mapToWorldCoord(x2 * scale, state.centerX, state.scale);
        int endZ = (int) mapToWorldCoord(z2 * scale, state.centerZ, state.scale);

        for (int x = startX; x <= endX; x++)
        {
            for (int z = startZ; z <= endZ; z++)
            {
                if (x >= 0 && x < 128 && z >= 0 && z < 128)
                {
                    int index = x + z * 128;
                    if (index < state.colors.length && (x + z) % 2 == 0) 
                        state.colors[index] = color;
                }
            }
        }

        state.markDirty();
    }
}