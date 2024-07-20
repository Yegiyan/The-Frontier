package com.frontier.gui.util;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class TextureElement 
{
	private Identifier textureID;
    private int x;
    private int y;
    private int width;
    private int height;
    private int rectX;
    private int rectY;
    private int rectWidth;
    private int rectHeight;
    private String tooltip;
    private float scale;

    public TextureElement(Identifier textureID, int x, int y, int width, int height, int rectX, int rectY, int rectWidth, int rectHeight, String tooltip, float scale) 
    {
        this.textureID = textureID;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rectX = rectX;
        this.rectY = rectY;
        this.rectWidth = rectWidth;
        this.rectHeight = rectHeight;
        this.tooltip = tooltip;
        this.scale = scale;
    }
    
    public TextureElement(Identifier textureID, int x, int y, int width, int height, String tooltip, float scale) 
    {
        this.textureID = textureID;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.tooltip = tooltip;
        this.scale = scale;
    }
    
    public TextureElement(Identifier textureID, int x, int y, int width, int height, float scale) 
    {
        this.textureID = textureID;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.scale = scale;
    }

    public void draw(DrawContext context) 
    {
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(scale, scale, 1);
        context.drawTexture(textureID, 0, 0, 0, 0, width, height, width, height);
        context.getMatrices().pop();
    }
    
    public void drawRect(DrawContext context) 
    {
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(scale, scale, 1);
        context.drawTexture(textureID, 0, 0, rectX, rectY, width, height, rectWidth, rectHeight);
        context.getMatrices().pop();
    }
    
    public boolean isMouseOver(int mouseX, int mouseY) 
    {
        int scaledWidth = (int) (width * scale);
        int scaledHeight = (int) (height * scale);
        return mouseX >= x && mouseX < x + scaledWidth && mouseY >= y && mouseY < y + scaledHeight;
    }
    
    public void setToolTip(String tooltip) {
    	this.tooltip = tooltip;
    }
    
    public String getToolTip() {
    	return this.tooltip;
    }

	public Identifier getTexture() {
		return textureID;
	}
}