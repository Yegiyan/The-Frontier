package com.frontier.gui.util;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class TextureElement 
{
    Identifier textureID;
    int x;
    int y;
    int width;
    int height;
    String tooltip;

    public TextureElement(Identifier textureID, int x, int y, int width, int height, String tooltip) 
    {
        this.textureID = textureID;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.tooltip = tooltip;
    }
    
    public TextureElement(Identifier textureID, int x, int y, int width, int height) 
    {
        this.textureID = textureID;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.tooltip = null;
    }

    public void draw(DrawContext context) 
    {
        context.drawTexture(textureID, x, y, 0, 0, width, height, width, height);
    }

    public boolean isMouseOver(int mouseX, int mouseY) 
    {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
    
    public void setToolTip(String tooltip) {
    	this.tooltip = tooltip;
    }
    
    public String getToolTip() {
    	return this.tooltip;
    }
}