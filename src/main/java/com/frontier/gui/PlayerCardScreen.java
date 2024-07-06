package com.frontier.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.frontier.Frontier;
import com.frontier.PlayerData;
import com.frontier.gui.util.TextureElement;
import com.frontier.regions.RegionManager;
import com.frontier.settlements.SettlementManager;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class PlayerCardScreen extends Screen
{
	public static final int UI_OFFSET_X = 0;
    public static final int UI_OFFSET_Y = 70;
    
	private static final Identifier BACKGROUND_TEXTURE = new Identifier("minecraft", "textures/gui/demo_background.png");
	public static final int BACKGROUND_WIDTH = 256;
    public static final int BACKGROUND_HEIGHT = 330;
    
	private static final Identifier FACTION_TEXTURE = new Identifier("minecraft", "textures/mob_effect/resistance.png");
	private static final Identifier RENOWN_TEXTURE = new Identifier("minecraft", "textures/mob_effect/hero_of_the_village.png");
	private static final Identifier REGION_TEXTURE = new Identifier("minecraft", "textures/mob_effect/darkness.png");
	private static final Identifier TERRITORY_TEXTURE = new Identifier("minecraft", "textures/item/mojang_banner_pattern.png");
	
	private static final Identifier SEPARATOR_TEXTURE = new Identifier("minecraft", "textures/gui/header_separator.png");
	
	int ORANGE = 0xF74B03;
	
	List<TextureElement> textures = new ArrayList<>();
	
	private int backgroundPosX;
    private int backgroundPosY;
	
    private Text nameText;
    private Text factionText;
    private Text professionText;
    private Text renownText;
    private Text regionText;
    private Text territoryText;
    
    private ButtonWidget cardButton;
    private ButtonWidget bountyButton;
    private boolean toggleMenu = false;
    
    private static Identifier professionTexture;
    MinecraftProfileTexture portrait;
    Identifier skinTexture;

    public PlayerCardScreen() 
    {
        super(Text.literal("Player Card Screen"));
    }
    
	@Override
    protected void init() 
    {
		PlayerEntity player = MinecraftClient.getInstance().player;
    	PlayerData playerData = PlayerData.players.get(player.getUuid());
    	
    	switch (playerData.getProfession())
    	{
    		case "Adventurer":
    			professionTexture = new Identifier("minecraft", "textures/mob_effect/speed.png");
    			break;
    		case "Denizen":
    			professionTexture = new Identifier("minecraft", "textures/item/totem_of_undying.png");
    			break;
    		case "Merchant":
    			professionTexture = new Identifier("minecraft", "textures/item/bundle.png");
    			break;
    		case "Commander":
    			professionTexture = new Identifier("minecraft", "textures/mob_effect/strength.png");
    			break;
    		case "Leader":
    			professionTexture = new Identifier("minecraft", "textures/item/bell.png");
    			break;
    		case "Outlaw":
    			professionTexture = new Identifier("minecraft", "textures/item/spyglass.png");
    			break;
    		case "Maverick":
    			professionTexture = new Identifier("minecraft", "textures/mob_effect/bad_omen.png");
    			break;
    		default:
    			Frontier.LOGGER.error("PlayerCardScreen() - No player profession found!");
    			professionTexture = new Identifier("minecraft", "textures/item/barrier.png");
    			break;
    	}
    	
    	backgroundPosX = ((this.width - BACKGROUND_WIDTH) / 2) + UI_OFFSET_X;
        backgroundPosY = ((this.height - BACKGROUND_HEIGHT) / 2) + UI_OFFSET_Y;

        textures.add(new TextureElement(REGION_TEXTURE, backgroundPosX + 12, (backgroundPosY + 35), 10, 10, "Current Region (" + RegionManager.getPlayerDirection(player.getBlockPos()) + ")" + RegionManager.getRegionWild(player.getBlockPos()), 1.0f));
        textures.add(new TextureElement(TERRITORY_TEXTURE, (backgroundPosX + 12), (backgroundPosY + 55), 12, 12, "Current Territory", 1.0f));
        textures.add(new TextureElement(professionTexture, (backgroundPosX + 225), (backgroundPosY + 14), 12, 12, "Profession", 1.0f));
        textures.add(new TextureElement(FACTION_TEXTURE, (backgroundPosX + 225), (backgroundPosY + 35), 12, 12, "Faction", 1.0f));
        textures.add(new TextureElement(RENOWN_TEXTURE, (backgroundPosX + 225), (backgroundPosY + 55), 12, 12, "Renown", 1.0f));
        
        if (player != null && playerData != null)
        {
            this.portrait = MinecraftClient.getInstance().getSkinProvider().getTextures(player.getGameProfile()).get(MinecraftProfileTexture.Type.SKIN);
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = MinecraftClient.getInstance().getSkinProvider().getTextures(player.getGameProfile());
            
            if (textures.containsKey(MinecraftProfileTexture.Type.SKIN))
            {
                MinecraftProfileTexture texture = textures.get(MinecraftProfileTexture.Type.SKIN);
                skinTexture = MinecraftClient.getInstance().getSkinProvider().loadSkin(texture, MinecraftProfileTexture.Type.SKIN);
            }
            else
                skinTexture = DefaultSkinHelper.getTexture(player.getUuid());
            
            if (player.getWorld().getRegistryKey().equals(World.OVERWORLD))
            	regionText = ((MutableText) Text.literal(RegionManager.getPlayerRegion(player.getBlockPos())));
            else if (player.getWorld().getRegistryKey().equals(World.NETHER))
            	regionText = ((MutableText) Text.literal("§kNetherworld"));
            else if (player.getWorld().getRegistryKey().equals(World.END))
            	regionText = ((MutableText) Text.literal("The End"));
            else
            	regionText = ((MutableText) Text.literal("Unknown Region"));
            
            String territory = SettlementManager.getSettlementTerritory(player.getBlockPos());
            if (SettlementManager.settlementExists(territory))
            {
            	int playerReputation = playerData.getReputation(territory);
            	
            	if (playerData.getProfession().equals("Leader") && playerData.getFaction().equals(territory))
            		territoryText = Text.literal(SettlementManager.getSettlementTerritory(player.getBlockPos())).formatted(Formatting.BLUE);
            	
            	else if (playerReputation >= -10 && playerReputation <= 10)
                	territoryText = Text.literal(SettlementManager.getSettlementTerritory(player.getBlockPos())).formatted(Formatting.WHITE);
            	
                else if (playerReputation >= 11 && playerReputation <= 50)
                	territoryText = Text.literal(SettlementManager.getSettlementTerritory(player.getBlockPos())).formatted(Formatting.GREEN);
                else if (playerReputation >= 51 && playerReputation <= 99)
                	territoryText = Text.literal(SettlementManager.getSettlementTerritory(player.getBlockPos())).formatted(Formatting.DARK_AQUA);
                else if (playerReputation == 100)
                	territoryText = Text.literal(SettlementManager.getSettlementTerritory(player.getBlockPos())).formatted(Formatting.BLUE);
            	
                else if (playerReputation >= -11 && playerReputation <= -50)
                	territoryText = Text.literal(SettlementManager.getSettlementTerritory(player.getBlockPos())).formatted(Formatting.YELLOW);
                else if (playerReputation >= -51 && playerReputation <= -99)
                	territoryText = Text.literal(SettlementManager.getSettlementTerritory(player.getBlockPos())).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(ORANGE)));
                else if (playerReputation == -100)
                	territoryText = Text.literal(SettlementManager.getSettlementTerritory(player.getBlockPos())).formatted(Formatting.RED);
            }
            else
            	territoryText = ((MutableText) Text.literal(SettlementManager.getSettlementTerritory(player.getBlockPos())).formatted(Formatting.WHITE));
        	
        	nameText = ((MutableText) Text.literal(playerData.getName()));
            factionText = ((MutableText)Text.literal(playerData.getFaction()));
            professionText = ((MutableText) Text.literal(playerData.getProfession()));
            renownText = ((MutableText) Text.literal(playerData.getRenownAsString()));

            this.cardButton = ButtonWidget.builder(Text.literal("PLAYER CARD"), button ->
            {        	
            	toggleMenu = false;
            }).dimensions(backgroundPosX + 0, backgroundPosY - 25, 80, 20).build();
            
            this.bountyButton = ButtonWidget.builder(Text.literal("BOUNTIES"), button ->
            {        	
            	toggleMenu = true;
            }).dimensions(backgroundPosX + 168, backgroundPosY - 25, 80, 20).build();
            
            addDrawableChild(cardButton);
            addDrawableChild(bountyButton);
        }  
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) 
    {
        this.renderBackground(context);
        context.drawTexture(BACKGROUND_TEXTURE, backgroundPosX, backgroundPosY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
        
        if (toggleMenu)
        {
        	cardButton.active = true;
        	bountyButton.active = false;
        }
        
        else
        {
        	cardButton.active = false;
        	bountyButton.active = true;
        	
        	if (skinTexture != null)
                context.drawTexture(skinTexture, (backgroundPosX + 13), (backgroundPosY + 15), 8, 8, 8, 8, 64, 64);
        	
        	for (TextureElement element : textures) 
        	{
                element.draw(context);
                if (element.isMouseOver(mouseX, mouseY))
                    renderTooltip(context, element.getToolTip(), mouseX, mouseY);
            }
        	
        	context.drawText(this.textRenderer, nameText, (backgroundPosX + 30), (backgroundPosY + 15), new Color(255, 255, 255).getRGB(), true);
        	context.drawText(this.textRenderer, regionText, (backgroundPosX + 30), (backgroundPosY + 36), new Color(255, 255, 255).getRGB(), true);
        	context.drawText(this.textRenderer, territoryText, (backgroundPosX + 30), (backgroundPosY + 57), new Color(255, 255, 255).getRGB(), true);

        	drawTextR(context, this.textRenderer, professionText, (backgroundPosX + 215), (backgroundPosY + 15), new Color(255, 255, 255).getRGB(), true);
        	drawTextR(context, this.textRenderer, factionText, (backgroundPosX + 215), (backgroundPosY + 36), new Color(255, 255, 255).getRGB(), true);
        	drawTextR(context, this.textRenderer, renownText, (backgroundPosX + 215), (backgroundPosY + 57), new Color(255, 255, 255).getRGB(), true);   
        	
        	context.drawTexture(SEPARATOR_TEXTURE, (backgroundPosX + 12), (backgroundPosY + 80), 0, 0, 225, 2, 32, 2);
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
    
    @Override
    public boolean shouldPause() 
    {
    	return false;
    }
}