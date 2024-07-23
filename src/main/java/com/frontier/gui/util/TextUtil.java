package com.frontier.gui.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class TextUtil
{
	public enum TextAlign { LEFT, CENTER, RIGHT; }
    
    public static void drawText(DrawContext context, TextRenderer textRenderer, Text text, int x, int y)
    {
        context.drawText(textRenderer, text, x, y, Color.WHITE.getRGB(), true);
    }
    
    public static void drawText(DrawContext context, TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow, boolean wrap, int maxWidth, TextAlign align)
    {
        if (wrap)
        {
            List<MutableText> lines = wrapText(text, textRenderer, maxWidth);
            int lineHeight = textRenderer.fontHeight + 1; // line height with some padding
            for (int i = 0; i < lines.size(); i++)
            {
                int drawX = x;
                if (align == TextAlign.CENTER)
                    drawX = x - textRenderer.getWidth(lines.get(i)) / 2;
                else if (align == TextAlign.RIGHT)
                    drawX = x - textRenderer.getWidth(lines.get(i));
                context.drawText(textRenderer, lines.get(i), drawX, y + (i * lineHeight), color, shadow);
            }
        }
        else
        {
            int drawX = x;
            if (align == TextAlign.CENTER)
                drawX = x - textRenderer.getWidth(text) / 2;
            else if (align == TextAlign.RIGHT)
                drawX = x - textRenderer.getWidth(text);
            context.drawText(textRenderer, text, drawX, y, color, shadow);
        }
    }

    private static List<MutableText> wrapText(Text text, TextRenderer textRenderer, int maxWidth)
    {
        List<MutableText> lines = new ArrayList<>();
        String[] words = text.getString().split(" ");
        StringBuilder line = new StringBuilder();
        MutableText currentLineText = Text.literal("");

        for (String word : words)
        {
            if (textRenderer.getWidth(line.toString() + word) <= maxWidth)
            {
                line.append(word).append(" ");
                currentLineText = Text.literal(line.toString()).setStyle(text.getStyle());
            }
            else
            {
                lines.add(currentLineText);
                line = new StringBuilder(word).append(" ");
                currentLineText = Text.literal(line.toString()).setStyle(text.getStyle());
            }
        }
        if (line.length() > 0)
            lines.add(currentLineText);

        return lines;
    }
    
    public static void renderTooltip(DrawContext context, TextRenderer textRenderer, String text, int mouseX, int mouseY)
    {
        if (text != null)
            context.drawTooltip(textRenderer, Text.literal(text), mouseX, mouseY);
    }
}
