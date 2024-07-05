package com.frontier.gui.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class TextWrapper
{
	public static void render(DrawContext context, TextRenderer textRenderer, Text text, int x, int y, int color, int maxWidth)
	{
		List<MutableText> lines = wrapText(text, textRenderer, maxWidth);

		// render each line
		int lineHeight = textRenderer.fontHeight + 2; // line height with some padding
		for (int i = 0; i < lines.size(); i++)
			context.drawText(textRenderer, lines.get(i), x, y + (i * lineHeight), color, true);
	}

	public static List<MutableText> wrapText(Text text, TextRenderer textRenderer, int maxWidth)
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
}