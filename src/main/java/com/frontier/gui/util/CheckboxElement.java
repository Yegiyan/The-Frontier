package com.frontier.gui.util;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class CheckboxElement
{
	private static final Identifier CHECKBOX_TEXTURE = new Identifier("minecraft", "textures/gui/checkbox.png");
	private static final int SIZE = 20;
	
	private int x, y;
	private boolean checked;
	private String label;
	private float scale;

	public CheckboxElement(int x, int y, String label, float scale)
	{
		this.x = x;
		this.y = y;
		this.label = label;
		this.checked = false;
		this.scale = scale;
	}

	public void render(DrawContext context, TextRenderer textRenderer)
	{
		int textureY = checked ? SIZE : 0;
		int scaledSize = (int) (SIZE * scale);
		context.getMatrices().push();
		context.getMatrices().translate(x, y, 0);
		context.getMatrices().scale(scale, scale, 1);
		context.drawTexture(CHECKBOX_TEXTURE, 0, 0, 0, textureY, SIZE, SIZE, 64, 64);
		context.getMatrices().pop();
		context.drawText(textRenderer, label, x + scaledSize + 4, y + (scaledSize - SIZE) / 2 + 5, 0xFFFFFF, true);
	}

	public boolean isMouseOver(int mouseX, int mouseY)
	{
		int scaledSize = (int) (SIZE * scale);
		return mouseX >= x && mouseX <= x + scaledSize && mouseY >= y && mouseY <= y + scaledSize;
	}

	public void toggle() {
		this.checked = !this.checked;
	}
	
	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public boolean isChecked() {
		return checked;
	}

	public String getLabel() {
		return label;
	}
}