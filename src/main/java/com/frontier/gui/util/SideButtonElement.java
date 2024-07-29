package com.frontier.gui.util;

import com.frontier.gui.PlayerCardScreen.Tab;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class SideButtonElement
{
	private static final Identifier SIDE_BUTTON_TEXTURE = new Identifier("minecraft", "textures/gui/recipe_book.png");
	private static final int SIZE = 25;
	private static final int UNFOCUSED_X = 153;
	private static final int FOCUSED_X = 188;
	
	private int x, y;
	private boolean focused;
	private boolean active;
	private String tooltip;
	private Identifier icon;
	private float scaleX;
	private float scaleY;
	private Tab tab;

	public SideButtonElement(int x, int y, String tooltip, Tab tab, float scaleX, float scaleY)
	{
		this(x, y, tooltip, null, tab, scaleX, scaleY);
	}

	public SideButtonElement(int x, int y, String tooltip, Identifier icon, Tab tab, float scaleX, float scaleY)
	{
		this.x = x;
		this.y = y;
		this.tooltip = tooltip;
		this.icon = icon;
		this.focused = false;
		this.active = true;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.tab = tab;
	}

	public void render(DrawContext context, TextRenderer textRenderer)
	{
		int textureX = focused ? FOCUSED_X : UNFOCUSED_X;
		if (!active)
			textureX = UNFOCUSED_X;

		context.getMatrices().push();
		context.getMatrices().translate(x, y, 0);
		context.getMatrices().scale(scaleX, scaleY, 1);

		if (focused)
			context.drawTexture(SIDE_BUTTON_TEXTURE, 0, 0, textureX, 2, 35, 26, 256, 256);
		else
			context.drawTexture(SIDE_BUTTON_TEXTURE, 3, 0, textureX, 2, 35, 26, 256, 256);

		context.getMatrices().pop();

		if (this.icon != null)
		{
			context.getMatrices().push();
			context.getMatrices().translate(x + (int) (9 * scaleX), y + (int) (5 * scaleY), 0);

			if (focused)
			{
				RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f); // normal texture
				context.drawTexture(this.icon, 0, 0, 0, 0, 16, 16, 16, 16);
			}
			else
			{
				RenderSystem.setShaderColor(0.6f, 0.6f, 0.6f, 1.0f); // darken color
				context.drawTexture(this.icon, 3, 0, 0, 0, 16, 16, 16, 16);
			}

			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f); // reset color
			context.getMatrices().pop();
		}
		else
		{
			if (focused)
				context.drawText(textRenderer, tooltip, x + (int) (10 + 3 * scaleX), y + (int) (9 * scaleY), 0xFFFFFF, true);
			else
				context.drawText(textRenderer, tooltip, x + (int) (10 * scaleX), y + (int) (9 * scaleY), 0xFFFFFF, true);
		}
	}

	public boolean isMouseOver(int mouseX, int mouseY)
	{
		int scaledSizeX = (int) ((SIZE * scaleX) + 10);
		int scaledSizeY = (int) (SIZE * scaleY);
		return mouseX >= x && mouseX <= x + scaledSizeX && mouseY >= y && mouseY <= y + scaledSizeY;
	}
	
	public Tab getTab() {
        return tab;
    }

    public void setTab(Tab tab) {
        this.tab = tab;
    }
	
	public void toggleFocus() {
		this.focused = !this.focused;
	}

	public void setFocused(boolean focused) {
		this.focused = focused;
	}

	public boolean isFocused() {
		return focused;
	}

	public String getToolTip() {
		return this.tooltip;
	}
	
	public void setToolTip(String tooltip) {
		this.tooltip = tooltip;
	}

	public Identifier getIcon() {
		return icon;
	}
	
	public boolean isActive() {
		return this.active;
	}
	
	public void setActive(boolean isActive) {
		this.active = isActive;
	}
}