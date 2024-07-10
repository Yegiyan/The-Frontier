package com.frontier.gui.util;

import java.awt.Color;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class TextUtil
{
	public static void drawTextR(DrawContext context, TextRenderer textRenderer, Text text, int rightEdgeX, int y, int color, boolean shadow)
    {
        int textWidth = textRenderer.getWidth(text);
        int x = rightEdgeX - textWidth;
        context.drawText(textRenderer, text, x, y, color, shadow);
    }
	
	public static void drawTextR(DrawContext context, TextRenderer textRenderer, Text text, int rightEdgeX, int y, boolean shadow)
	{
		int textWidth = textRenderer.getWidth(text);
		int x = rightEdgeX - textWidth;
		context.drawText(textRenderer, text, x, y, Color.WHITE.getRGB(), shadow);
	}
}