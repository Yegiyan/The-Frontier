package com.frontier.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.frontier.entities.SettlerEntity;
import com.frontier.gui.util.TextureElement;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class HireSettlerScreen extends Screen
{
	public static final int UI_OFFSET_X = 0;
    public static final int UI_OFFSET_Y = 40;
    
	private static final Identifier BACKGROUND_TEXTURE = new Identifier("minecraft", "textures/gui/demo_background.png");
	public static final int BACKGROUND_WIDTH = 256;
    public static final int BACKGROUND_HEIGHT = 250;
    
    private static final Identifier NAMEPLATE_TEXTURE = new Identifier("minecraft", "textures/gui/social_interactions.png");
    private static final Identifier SEPARATOR_TEXTURE = new Identifier("minecraft", "textures/gui/header_separator.png");
    
    private static final Identifier BARS_TEXTURE = new Identifier("minecraft", "textures/gui/bars.png");
    
    private static final Identifier HEALTH_TEXTURE = new Identifier("minecraft", "textures/gui/icons.png");
    private static final Identifier SKILL_TEXTURE = new Identifier("minecraft", "textures/gui/icons.png");

    List<TextureElement> textures = new ArrayList<>();
    List<TextureElement> barTextures = new ArrayList<>();
    
    private int backgroundPosX;
    private int backgroundPosY;
    
    public enum Page { MAIN, GOVERNING, HARVESTING, CRAFTING, RANCHING, TRADING, MILITARY }
    private Page page;
    
    private ButtonWidget governingButton;
    private ButtonWidget militiaButton;
    private ButtonWidget harvestingButton;
    private ButtonWidget craftingButton;
    private ButtonWidget ranchingButton;
    private ButtonWidget tradingButton;
    
    // governing hires
    private ButtonWidget hireArchitectButton;
    
    // military hires
    private Text militaryTitle;
    private Text militaryText;
    private ButtonWidget hireArcherButton;
    private ButtonWidget hireKnightButton;
    private ButtonWidget hireClericButton;
    
    MinecraftProfileTexture portrait;
    Identifier skinTexture;
    
    private static Identifier expertiseTexture;
    private String expertise;
    
    private Text nameText;
    private Text expertiseText;
    
    private Text healthText;
    private Text skillText;
    
    private SettlerEntity settler;
    
    public HireSettlerScreen(SettlerEntity settler)
    {
        super(Text.literal("Hire a Settler Screen"));
        this.settler = settler;
        this.expertise = settler.getSettlerExpertise();
        this.page = Page.MAIN;
        
        switch (expertise)
        {
            case "GOVERNING":
                expertiseTexture = new Identifier("minecraft", "textures/item/writable_book.png");
                break;
            case "HARVESTING":
                expertiseTexture = new Identifier("minecraft", "textures/item/bucket.png");
                break;
            case "TRADING":
                expertiseTexture = new Identifier("minecraft", "textures/item/bundle_filled.png");
                break;
            case "CRAFTING":
                expertiseTexture = new Identifier("minecraft", "textures/item/brewing_stand.png");
                break;
            case "RANCHING":
                expertiseTexture = new Identifier("minecraft", "textures/item/lead.png");
                break;
            case "MILITARY":
                expertiseTexture = new Identifier("minecraft", "textures/item/chainmail_chestplate.png");
                break;
            default:
                System.err.println("HireSettlerScreen() - Invalid settler expertise!");
                break;
        }
    }

    @Override
    protected void init() 
    {
        backgroundPosX = ((this.width - BACKGROUND_WIDTH) / 2) + UI_OFFSET_X;
        backgroundPosY = ((this.height - BACKGROUND_HEIGHT) / 2) + UI_OFFSET_Y;

        barTextures.add(new TextureElement(NAMEPLATE_TEXTURE, (backgroundPosX + 5), (backgroundPosY + 8), 238, 36, 0, 0, 256, 256, null, 1.0f));
        
        textures.add(new TextureElement(expertiseTexture, (backgroundPosX + 214), (backgroundPosY + 18), 12, 12, "Expertise", 1.2f));
        
        barTextures.add(new TextureElement(HEALTH_TEXTURE, (backgroundPosX + 14), (backgroundPosY + 52), 9, 9, 53, 0, 256, 256, "Health (" + settler.getHealth() + " / 20)", 1.0f));
        barTextures.add(new TextureElement(SKILL_TEXTURE, (backgroundPosX + 14), (backgroundPosY + 70), 9, 9, 89, 0, 256, 256, "Skill (in field of expertise)", 1.0f));
        
        boolean isMale = settler.getSettlerGender().equals("Male");
        Identifier[] textures = settler.getTextures(isMale);
        int textureIndex = settler.getSettlerTexture();

        if (textures != null && textures.length > 0)
            skinTexture = textures[textureIndex];
        else
            skinTexture = DefaultSkinHelper.getTexture(settler.getUuid());
        
        nameText = ((MutableText) Text.literal(settler.getSettlerName()));
        expertiseText = ((MutableText) Text.literal(settler.getSettlerExpertise()));
        String transformedExpertise = expertise.substring(0, 1).toUpperCase() + expertise.substring(1).toLowerCase();
        expertiseText = (MutableText) Text.literal(transformedExpertise);

        healthText = Text.literal(String.format("%.0f", (settler.getHealth() / 20.0f) * 100));
        skillText = Text.literal(String.valueOf(settler.getSettlerSkill()));
        
        militaryTitle = Text.literal("Military");
        militaryText = Text.literal("Military Description");
        
        updateButtons();
    }

    private void updateButtons()
    {
        this.clearChildren();
        
        if (page == Page.MAIN)
        {
            this.governingButton = ButtonWidget.builder(Text.literal("Governing"), button -> { page = Page.GOVERNING; updateButtons(); }).dimensions(backgroundPosX + 10, backgroundPosY + 100, 65, 20).build();
            this.militiaButton = ButtonWidget.builder(Text.literal("Military"), button -> { page = Page.MILITARY; updateButtons(); }).dimensions(backgroundPosX + 10, backgroundPosY + 130, 65, 20).build();
            this.harvestingButton = ButtonWidget.builder(Text.literal("Harvesting"), button -> { page = Page.HARVESTING; updateButtons(); }).dimensions(backgroundPosX + 90, backgroundPosY + 100, 65, 20).build();
            this.craftingButton = ButtonWidget.builder(Text.literal("Crafting"), button -> { page = Page.CRAFTING; updateButtons(); }).dimensions(backgroundPosX + 90, backgroundPosY + 130, 65, 20).build();
            this.ranchingButton = ButtonWidget.builder(Text.literal("Ranching"), button -> { page = Page.RANCHING; updateButtons(); }).dimensions(backgroundPosX + 170, backgroundPosY + 100, 65, 20).build();
            this.tradingButton = ButtonWidget.builder(Text.literal("Trading"), button -> { page = Page.TRADING; updateButtons(); }).dimensions(backgroundPosX + 170, backgroundPosY + 130, 65, 20).build();
            
            addDrawableChild(governingButton);
            addDrawableChild(militiaButton);
            addDrawableChild(harvestingButton);
            addDrawableChild(craftingButton);
            addDrawableChild(ranchingButton);
            addDrawableChild(tradingButton);
        }
        
        else if (page == Page.GOVERNING)
        {
            this.hireArchitectButton = ButtonWidget.builder(Text.literal("Architect"), button -> { /* hire architect */ }).dimensions(backgroundPosX + 10, backgroundPosY + 100, 65, 20).build();

            addDrawableChild(hireArchitectButton);
        }
        
        else if (page == Page.MILITARY)
        {
            this.hireArcherButton = ButtonWidget.builder(Text.literal("Archer"), button -> { /* hire archer */ }).dimensions(backgroundPosX + 10, backgroundPosY + 100, 65, 20).build();
            this.hireKnightButton = ButtonWidget.builder(Text.literal("Knight"), button -> { /* hire knight */ }).dimensions(backgroundPosX + 90, backgroundPosY + 100, 65, 20).build();
            this.hireClericButton = ButtonWidget.builder(Text.literal("Cleric"), button -> { /* hire cleric */ }).dimensions(backgroundPosX + 170, backgroundPosY + 100, 65, 20).build();
            
            addDrawableChild(hireArcherButton);
            addDrawableChild(hireKnightButton);
            addDrawableChild(hireClericButton);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) 
    {
        this.renderBackground(context);
        context.drawTexture(BACKGROUND_TEXTURE, backgroundPosX, backgroundPosY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
        
        if (page == Page.MAIN)
        {
            for (TextureElement element : barTextures)
            {
                element.drawRect(context);
                if (element.isMouseOver(mouseX, mouseY))
                    renderTooltip(context, element.getToolTip(), mouseX, mouseY);
            }
            
            for (TextureElement element : textures)
            {
                element.draw(context);
                if (element.isMouseOver(mouseX, mouseY))
                    renderTooltip(context, element.getToolTip(), mouseX, mouseY);
            }
            
            context.drawTexture(skinTexture, (backgroundPosX + 20), (backgroundPosY + 22), 8, 8, 8, 8, 64, 64);

            context.drawText(this.textRenderer, nameText, (backgroundPosX + 35), (backgroundPosY + 22), new Color(255, 255, 255).getRGB(), true);
            drawTextR(context, this.textRenderer, expertiseText, (backgroundPosX + 207), (backgroundPosY + 22), new Color(255, 255, 255).getRGB(), true);

            context.drawText(this.textRenderer, healthText, (backgroundPosX + 218), (backgroundPosY + 52), new Color(65, 65, 65).getRGB(), false);
            context.drawText(this.textRenderer, skillText, (backgroundPosX + 218), (backgroundPosY + 70), new Color(65, 65, 65).getRGB(), false);
            
            context.drawTexture(SEPARATOR_TEXTURE, (backgroundPosX + 10), (backgroundPosY + 87), 0, 0, 225, 2, 32, 2);
            
            drawBar(context, backgroundPosX + 29, backgroundPosY + 53, settler.getHealth() / 20.0f, 4);
            drawBar(context, backgroundPosX + 29, backgroundPosY + 71, settler.getSettlerSkill() / 100.0f, 6);
        }
        
        else if (page == Page.GOVERNING)
        {
            // Render governing page
        }
        
        else if (page == Page.MILITARY)
        {
        	context.drawText(this.textRenderer, militaryTitle, (backgroundPosX + 35), (backgroundPosY + 22), new Color(255, 255, 255).getRGB(), true);
        	context.drawText(this.textRenderer, militaryText, (backgroundPosX + 35), (backgroundPosY + 35), new Color(255, 255, 255).getRGB(), true);
        }
        
        else if (page == Page.HARVESTING)
        {
            // Render harvesting page
        }
        
        else if (page == Page.CRAFTING)
        {
            // Render crafting page
        }
        
        else if (page == Page.RANCHING)
        {
            // Render ranching page
        }
        
        else if (page == Page.TRADING)
        {
            // Render trading page
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void renderTooltip(DrawContext context, String text, int mouseX, int mouseY) 
    {
    	if (text != null)
    		context.drawTooltip(textRenderer, Text.literal(text), mouseX, mouseY);
    }
    
    // align text from right side
    private void drawTextR(DrawContext context, TextRenderer textRenderer, Text text, int rightEdgeX, int y, int color, boolean shadow) 
    {
        int textWidth = textRenderer.getWidth(text);
        int x = rightEdgeX - textWidth;
        context.drawText(textRenderer, text, x, y, color, shadow);
    }

    private void drawBar(DrawContext context, int x, int y, float value, int barIndex)
    {
        int barHeight = 5;
        int barWidth = (int) (182 * value);
        
        context.drawTexture(BARS_TEXTURE, x, y, 0, 5 * barIndex, 182, barHeight);
        context.drawTexture(BARS_TEXTURE, x, y, 0, 5 * (barIndex + 1), barWidth, barHeight);
    }
    
    @Override
    public boolean shouldPause() 
    {
    	return false;
    }
}