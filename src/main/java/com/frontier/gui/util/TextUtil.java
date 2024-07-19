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
	
	public static void drawText(DrawContext context, TextRenderer textRenderer, Text text, int x, int y)
	{
		context.drawText(textRenderer, text, x, y, Color.WHITE.getRGB(), true);
	}
	
	public static void renderTooltip(DrawContext context, TextRenderer textRenderer, String text, int mouseX, int mouseY)
	{
		if (text != null)
			context.drawTooltip(textRenderer, Text.literal(text), mouseX, mouseY);
	}
}