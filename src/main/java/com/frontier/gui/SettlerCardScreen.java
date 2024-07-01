package com.frontier.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.frontier.PlayerData;
import com.frontier.entities.SettlerEntity;
import com.frontier.gui.util.TextureElement;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SettlerCardScreen extends Screen
{
	public static final int UI_OFFSET_X = 0;
    public static final int UI_OFFSET_Y = 20;
	
	private static final Identifier BACKGROUND_TEXTURE = new Identifier("minecraft", "textures/gui/demo_background.png");
	public static final int BACKGROUND_WIDTH = 186;
    public static final int BACKGROUND_HEIGHT = 205;
    
    private static final Identifier SLOT_TEXTURE = new Identifier("minecraft", "textures/gui/container/generic_54.png");
    private static final Identifier PROFESSION_TEXTURE = new Identifier("minecraft", "textures/item/writable_book.png");
	private static final Identifier FACTION_TEXTURE = new Identifier("minecraft", "textures/mob_effect/resistance.png");
    
    private int backgroundPosX;
    private int backgroundPosY;
    
    List<TextureElement> textures = new ArrayList<>();
    
    private ButtonWidget cardButton;
    private ButtonWidget taskButton;
    private boolean toggleMenu = false;
    
    MinecraftProfileTexture portrait;
    Identifier skinTexture;
    
    private Text nameText;
    private Text factionText;
    private Text professionText;
    
    private SettlerEntity settler;
    private SimpleInventory inventory;
    
    public SettlerCardScreen(SettlerEntity settler)
    {
        super(Text.literal("Settler Card Screen"));
        this.settler = settler;
        this.inventory = settler.getInventory();
    }
    
	@Override
    protected void init()
    {
    	PlayerEntity player = MinecraftClient.getInstance().player;
    	PlayerData playerData = PlayerData.map.get(player.getUuid());
    	
    	backgroundPosX = ((this.width - BACKGROUND_WIDTH) / 2) + UI_OFFSET_X;
        backgroundPosY = ((this.height - BACKGROUND_HEIGHT) / 2) + UI_OFFSET_Y;
        
        textures.add(new TextureElement(PROFESSION_TEXTURE, (backgroundPosX + 160), (backgroundPosY + 8), 12, 12, "Profession"));
        textures.add(new TextureElement(FACTION_TEXTURE, (backgroundPosX + 160), (backgroundPosY + 30), 12, 12, "Faction"));
        
        if (player != null && playerData != null)
        {
        	this.portrait = MinecraftClient.getInstance().getSkinProvider().getTextures(player.getGameProfile()).get(MinecraftProfileTexture.Type.SKIN);
            
            if (portrait != null)
                skinTexture = new Identifier(portrait.getUrl());
            else 
                skinTexture = new Identifier("minecraft", "textures/entity/player/wide/steve.png");
        }
        
        nameText = ((MutableText) Text.literal(settler.getSettlerName()));
        factionText = ((MutableText)Text.literal(settler.getSettlerFaction()));
        professionText = ((MutableText) Text.literal(settler.getSettlerProfession()));

        this.cardButton = ButtonWidget.builder(Text.literal("INFORMATION"), button -> { toggleMenu = false; }).dimensions(backgroundPosX + 0, backgroundPosY - 25, 80, 20).build();
        this.taskButton = ButtonWidget.builder(Text.literal("TASK LIST"), button -> { toggleMenu = true; }).dimensions(backgroundPosX + 100, backgroundPosY - 25, 80, 20).build();
        
        addDrawableChild(cardButton);
        addDrawableChild(taskButton);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) 
    {
        this.renderBackground(context);
        context.drawTexture(BACKGROUND_TEXTURE, backgroundPosX, backgroundPosY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
        
        if (toggleMenu)
        {
        	cardButton.active = true;
        	taskButton.active = false;
        }
        
        else
        {
        	cardButton.active = false;
        	taskButton.active = true;
        	
        	int inventoryStartX = backgroundPosX + 10;
            int inventoryStartY = backgroundPosY + 70;
            for (int i = 0; i < inventory.size(); i++)
            {
                ItemStack stack = inventory.getStack(i);
                int slotX = inventoryStartX + (i % 9) * 18;
                int slotY = inventoryStartY + (i / 9) * 18;
                drawSlot(stack, slotX, slotY, context);
            }
        	
        	context.drawTexture(skinTexture, (backgroundPosX + 10), (backgroundPosY + 10), 8, 8, 8, 8, 64, 64);
        	for (TextureElement element : textures)
        	{
                element.draw(context);
                if (element.isMouseOver(mouseX, mouseY))
                    renderTooltip(context, element.getToolTip(), mouseX, mouseY);
            }
        	
        	context.drawText(this.textRenderer, nameText, (backgroundPosX + 25), (backgroundPosY + 10), new Color(255, 255, 255).getRGB(), true);
        	drawTextR(context, this.textRenderer, professionText, (backgroundPosX + 155), (backgroundPosY + 10), new Color(255, 255, 255).getRGB(), true);
        	drawTextR(context, this.textRenderer, factionText, (backgroundPosX + 155), (backgroundPosY + 32), new Color(255, 255, 255).getRGB(), true);
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void renderTooltip(DrawContext context, String text, int mouseX, int mouseY) 
    {
    	if (text != null)
    		context.drawTooltip(textRenderer, Text.literal(text), mouseX, mouseY);
    }

    private void drawSlot(ItemStack stack, int x, int y, DrawContext context)
    {
        context.drawTexture(SLOT_TEXTURE, x - 1, y - 1, 25, 35, 18, 18);
        if (!stack.isEmpty())
        {
            context.drawItem(stack, x, y);
            context.drawItemInSlot(textRenderer, stack, x, y);
        }
    }

    // align text from right side
    private void drawTextR(DrawContext context, TextRenderer textRenderer, Text text, int rightEdgeX, int y, int color, boolean shadow) 
    {
        int textWidth = textRenderer.getWidth(text);
        int x = rightEdgeX - textWidth;
        context.drawText(textRenderer, text, x, y, color, shadow);
    }
    
    @Override
    public boolean shouldPause() {
    	return false;
    }
}