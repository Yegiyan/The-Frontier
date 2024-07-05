package com.frontier.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.frontier.entities.SettlerEntity;
import com.frontier.gui.util.TextWrapper;
import com.frontier.gui.util.TextureElement;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class HireSettlerScreen extends Screen
{
	public static final int UI_OFFSET_X = 0;
    public static final int UI_OFFSET_Y = 60;
    
	private static final Identifier BACKGROUND_TEXTURE = new Identifier("minecraft", "textures/gui/demo_background.png");
	public static final int BACKGROUND_WIDTH = 256;
    public static final int BACKGROUND_HEIGHT = 285;
    
    private static final Identifier NAMEPLATE_TEXTURE = new Identifier("minecraft", "textures/gui/social_interactions.png");
    private static final Identifier SEPARATOR_TEXTURE = new Identifier("minecraft", "textures/gui/header_separator.png");
    
    private static final Identifier BARS_TEXTURE = new Identifier("minecraft", "textures/gui/bars.png");
    
    private static final Identifier HEALTH_TEXTURE = new Identifier("minecraft", "textures/gui/icons.png");
    private static final Identifier HUNGER_TEXTURE = new Identifier("minecraft", "textures/gui/icons.png");
    private static final Identifier MORALE_TEXTURE = new Identifier("minecraft", "textures/gui/icons.png");
    private static final Identifier SKILL_TEXTURE = new Identifier("minecraft", "textures/gui/icons.png");
    
    private static final Identifier HANGINGSIGN_TEXTURE = new Identifier("minecraft", "textures/gui/hanging_signs/spruce.png");
    private static final Identifier EMERALD_TEXTURE = new Identifier("minecraft", "textures/item/emerald.png");
    
    private static final Identifier ARCHER_TEXTURE = new Identifier("minecraft", "textures/item/bow_pulling_2.png");
    private static final Identifier KNIGHT_TEXTURE = new Identifier("minecraft", "textures/item/iron_sword.png");
    private static final Identifier CLERIC_TEXTURE = new Identifier("minecraft", "textures/mob_effect/health_boost.png");

    List<TextureElement> mainTextures = new ArrayList<>();
    List<TextureElement> barTextures = new ArrayList<>();
    
    private int backgroundPosX;
    private int backgroundPosY;
    
    public enum Page { MAIN, GOVERNING, HARVESTING, CRAFTING, RANCHING, TRADING, MILITARY }
    private Page page;
    
    public enum Hire
    {
        NONE(0), ARCHITECT(3), COURIER(6), PRIEST(6),
        ARCHER(6), KNIGHT(6), CLERIC(10),
        FARMER(4), MINER(8), LUMBERJACK(4), FISHERMAN(5),
        ALCHEMIST(12), BLACKSMITH(8), FLETCHER(6), MASON(10), CARPENTER(8), CARTOGRAPHER(10),
        BEEKEEPER(8), POULTRYMAN(6), COWHAND(4), SWINEHERD(4), SHEPHERD(8), STABLEHAND(14),
        BAKER(6), COOK(8), ARCANIST(10), LEATHERWORKER(8), MERCHANT(10);
    	
        private int value;
        Hire(int value) { this.value = value; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
        public MutableText getText() { return Text.literal(String.valueOf(value)); }
    }
    private Hire hire;
    
    private ButtonWidget backButton;
    private ButtonWidget governingButton;
    private ButtonWidget militiaButton;
    private ButtonWidget harvestingButton;
    private ButtonWidget craftingButton;
    private ButtonWidget ranchingButton;
    private ButtonWidget tradingButton;
    
    private ButtonWidget hireButton;
    private Text priceText;
    
    // governing hires
    private ButtonWidget hireArchitectButton;
    
    // military hires
    List<TextureElement> militaryTextures = new ArrayList<>();
    private Text militaryTitle;
    private Text militaryText1;
    private Text militaryText2;
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
    private Text hungerText;
    private Text moraleText;
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

        nameText = Text.literal(settler.getSettlerName());
        expertiseText = Text.literal(settler.getSettlerExpertise());
        String transformedExpertise = expertise.substring(0, 1).toUpperCase() + expertise.substring(1).toLowerCase();
        expertiseText = Text.literal(transformedExpertise);

        healthText = Text.literal(String.format("%.0f", (settler.getHealth() / 20.0f) * 100));
        hungerText = Text.literal(String.valueOf(settler.getSettlerHunger()));
        moraleText = Text.literal(String.valueOf(settler.getSettlerMorale()));
        skillText = Text.literal(String.valueOf(settler.getSettlerSkill()));
        
        militaryTitle = Text.literal("Hiring Cost");
        militaryText1 = Text.literal("Military units will guard, patrol, and defend your settlement and merchants.");
        militaryText2 = Text.literal("They will also follow their leader and enact raids on your enemies. Make sure they are armed!");
        
        priceText = Text.literal("0");
        
        updateButtons();
    }

    private void updateButtons()
    {
        this.clearChildren();
        backButton = ButtonWidget.builder(Text.literal("Information"), button -> { page = Page.MAIN; updateButtons(); }).dimensions(backgroundPosX + 0, backgroundPosY - 25, 80, 20).build();
        backButton.active = true;
        
        if (page == Page.MAIN)
        {
        	hire = Hire.NONE;
        			
        	barTextures.add(new TextureElement(NAMEPLATE_TEXTURE, (backgroundPosX + 5), (backgroundPosY + 8), 238, 36, 0, 0, 256, 256, null, 1.0f));
            
            mainTextures.add(new TextureElement(expertiseTexture, (backgroundPosX + 214), (backgroundPosY + 18), 12, 12, "Expertise", 1.2f));
            
            militaryTextures.add(new TextureElement(ARCHER_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 120), 16, 16, "Ranged fighter that wears leather armor", 1.0f));
            militaryTextures.add(new TextureElement(KNIGHT_TEXTURE, (backgroundPosX + 87), (backgroundPosY + 120), 16, 16, "Melee fighter that wears leather or heavy armor", 1.0f));
            militaryTextures.add(new TextureElement(CLERIC_TEXTURE, (backgroundPosX + 167), (backgroundPosY + 120), 16, 16, "Armored medic that heals allies in and out of combat", 1.0f));      
            
            barTextures.add(new TextureElement(HEALTH_TEXTURE, (backgroundPosX + 14), (backgroundPosY + 51), 9, 9, 53, 0, 256, 256, "Health (" + (int) settler.getHealth() + " / 20)", 1.0f));
            barTextures.add(new TextureElement(HUNGER_TEXTURE, (backgroundPosX + 14), (backgroundPosY + 64), 9, 8, 53, 28, 256, 256, "Hunger", 1.0f));
            barTextures.add(new TextureElement(MORALE_TEXTURE, (backgroundPosX + 14), (backgroundPosY + 75), 9, 9, 161, 0, 256, 256, "Morale", 1.0f));
            barTextures.add(new TextureElement(SKILL_TEXTURE, (backgroundPosX + 14), (backgroundPosY + 87), 9, 9, 89, 0, 256, 256, "Skill (in field of expertise)", 1.0f));
            
            boolean isMale = settler.getSettlerGender().equals("Male");
            Identifier[] textures = settler.getTextures(isMale);
            int textureIndex = settler.getSettlerTexture();

            if (textures != null && textures.length > 0)
                skinTexture = textures[textureIndex];
            else
                skinTexture = DefaultSkinHelper.getTexture(settler.getUuid());
            
            backButton.active = false;
            governingButton = ButtonWidget.builder(Text.literal("Governing"), button -> { page = Page.GOVERNING; updateButtons(); }).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
            militiaButton = ButtonWidget.builder(Text.literal("Military"), button -> { page = Page.MILITARY; updateButtons(); }).dimensions(backgroundPosX + 10, backgroundPosY + 155, 65, 20).build();
            harvestingButton = ButtonWidget.builder(Text.literal("Harvesting"), button -> { page = Page.HARVESTING; updateButtons(); }).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
            craftingButton = ButtonWidget.builder(Text.literal("Crafting"), button -> { page = Page.CRAFTING; updateButtons(); }).dimensions(backgroundPosX + 90, backgroundPosY + 155, 65, 20).build();
            ranchingButton = ButtonWidget.builder(Text.literal("Ranching"), button -> { page = Page.RANCHING; updateButtons(); }).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
            tradingButton = ButtonWidget.builder(Text.literal("Trading"), button -> { page = Page.TRADING; updateButtons(); }).dimensions(backgroundPosX + 170, backgroundPosY + 155, 65, 20).build();
            
            addDrawableChild(governingButton);
            addDrawableChild(militiaButton);
            addDrawableChild(harvestingButton);
            addDrawableChild(craftingButton);
            addDrawableChild(ranchingButton);
            addDrawableChild(tradingButton);
        }
        
        else if (page == Page.GOVERNING)
        {
            hireArchitectButton = ButtonWidget.builder(Text.literal("Architect"), button -> { /* hire architect */ }).dimensions(backgroundPosX + 10, backgroundPosY + 100, 65, 20).build();

            addDrawableChild(hireArchitectButton);
        }
        
        else if (page == Page.MILITARY)
        {
        	hireButton = ButtonWidget.builder(Text.literal("Hire"), button -> { /* hire nomad */ }).dimensions(backgroundPosX + 180, backgroundPosY + 70, 45, 20).build();
        	
            hireArcherButton = ButtonWidget.builder(Text.literal("Archer"), button -> { hire = Hire.ARCHER; priceText = Hire.ARCHER.getText(); }).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
            hireKnightButton = ButtonWidget.builder(Text.literal("Knight"), button -> { hire = Hire.KNIGHT; priceText = Hire.KNIGHT.getText(); }).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
            hireClericButton = ButtonWidget.builder(Text.literal("Cleric"), button -> { hire = Hire.CLERIC; priceText = Hire.CLERIC.getText(); }).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();

            addDrawableChild(hireButton);
            addDrawableChild(hireArcherButton);
            addDrawableChild(hireKnightButton);
            addDrawableChild(hireClericButton);
        }
        
        if (hire == Hire.NONE)
        	priceText = Text.literal("-");
        
        addDrawableChild(backButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) 
    {
        this.renderBackground(context);
        context.drawTexture(BACKGROUND_TEXTURE, backgroundPosX, backgroundPosY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
        super.render(context, mouseX, mouseY, delta);
        
        if (page == Page.MAIN)
        {
        	context.drawTexture(SEPARATOR_TEXTURE, (backgroundPosX + 10), (backgroundPosY + 108), 0, 0, 225, 2, 32, 2);
        	
            for (TextureElement element : barTextures)
            {
                element.drawRect(context);
                if (element.isMouseOver(mouseX, mouseY))
                    renderTooltip(context, element.getToolTip(), mouseX, mouseY);
            }
            
            for (TextureElement element : mainTextures)
            {
                element.draw(context);
                if (element.isMouseOver(mouseX, mouseY))
                    renderTooltip(context, element.getToolTip(), mouseX, mouseY);
            }
            
            context.drawTexture(skinTexture, (backgroundPosX + 20), (backgroundPosY + 22), 8, 8, 8, 8, 64, 64);

            context.drawText(this.textRenderer, nameText, (backgroundPosX + 35), (backgroundPosY + 22), new Color(255, 255, 255).getRGB(), true);
            drawTextR(context, this.textRenderer, expertiseText, (backgroundPosX + 207), (backgroundPosY + 22), new Color(70, 150, 25).getRGB(), true);

            context.drawText(this.textRenderer, healthText, (backgroundPosX + 218), (backgroundPosY + 52), new Color(65, 65, 65).getRGB(), false);
            context.drawText(this.textRenderer, hungerText, (backgroundPosX + 218), (backgroundPosY + 64), new Color(65, 65, 65).getRGB(), false);
            context.drawText(this.textRenderer, moraleText, (backgroundPosX + 218), (backgroundPosY + 76), new Color(65, 65, 65).getRGB(), false);
            context.drawText(this.textRenderer, skillText, (backgroundPosX + 218), (backgroundPosY + 88), new Color(65, 65, 65).getRGB(), false);
            
            drawBar(context, backgroundPosX + 29, backgroundPosY + 53, settler.getHealth() / 20.0f, 4);
            drawBar(context, backgroundPosX + 29, backgroundPosY + 65, settler.getSettlerHunger() / 100.0f, 12);
            drawBar(context, backgroundPosX + 29, backgroundPosY + 77, settler.getSettlerMorale() / 100.0f, 8);
            drawBar(context, backgroundPosX + 29, backgroundPosY + 89, settler.getSettlerSkill() / 100.0f, 6);
        }
        
        else if (page == Page.GOVERNING)
        {
            // render governing page
        }
        
        else if (page == Page.MILITARY)
        {
        	hireButton.active = (hire != Hire.NONE);
        	hireArcherButton.active = (hire != Hire.ARCHER);
        	hireKnightButton.active = (hire != Hire.KNIGHT);
        	hireClericButton.active = (hire != Hire.CLERIC);
        	
        	if (hireArcherButton.isMouseOver(mouseX, mouseY)) priceText = Hire.ARCHER.getText().formatted(Formatting.GRAY);
        	else if (hireKnightButton.isMouseOver(mouseX, mouseY)) priceText = Hire.KNIGHT.getText().formatted(Formatting.GRAY);
        	else if (hireClericButton.isMouseOver(mouseX, mouseY)) priceText = Hire.CLERIC.getText().formatted(Formatting.GRAY);
        	else priceText = hire.getText();
        	
        	context.drawText(this.textRenderer, militaryTitle, (backgroundPosX + 175), (backgroundPosY + 16), new Color(255, 255, 255).getRGB(), true);
            TextWrapper.render(context, this.textRenderer, militaryText1, backgroundPosX + 14, backgroundPosY + 16, new Color(255, 255, 255).getRGB(), 135);
            TextWrapper.render(context, this.textRenderer, militaryText2, backgroundPosX + 14, backgroundPosY + 55, new Color(255, 255, 255).getRGB(), 135);
            context.drawTexture(SEPARATOR_TEXTURE, (backgroundPosX + 10), (backgroundPosY + 109), 0, 0, 225, 2, 32, 2);
            
            context.drawTexture(HANGINGSIGN_TEXTURE, (backgroundPosX + 175), (backgroundPosY + 28), 0, 0, 54, 32, 54, 32);
            context.drawTexture(EMERALD_TEXTURE, (backgroundPosX + 207), (backgroundPosY + 43), 0, 0, 14, 14, 14, 14);
            
            for (TextureElement element : militaryTextures)
            {
                element.draw(context);
                if (element.isMouseOver(mouseX, mouseY))
                    renderTooltip(context, element.getToolTip(), mouseX, mouseY);
            }
            
            TextWrapper.render(context, this.textRenderer, priceText, backgroundPosX + 192, backgroundPosY + 46, new Color(255, 255, 255).getRGB(), 225);
        }
        
        else if (page == Page.HARVESTING)
        {
            // render harvesting page
        }
        
        else if (page == Page.CRAFTING)
        {
            // render crafting page
        }
        
        else if (page == Page.RANCHING)
        {
            // render ranching page
        }
        
        else if (page == Page.TRADING)
        {
            // render trading page
        }
    }
    
    private void renderTooltip(DrawContext context, String text, int mouseX, int mouseY) 
    {
    	if (text != null)
    		context.drawTooltip(this.textRenderer, Text.literal(text), mouseX, mouseY);
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