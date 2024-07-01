package com.frontier.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.frontier.PlayerData;
import com.frontier.entities.SettlerEntity;
import com.frontier.gui.util.TextureElement;
import com.frontier.network.FrontierPackets;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class HireSettlerScreen extends Screen
{
	public static final int UI_OFFSET_X = 0;
    public static final int UI_OFFSET_Y = 0;
    
	private static final Identifier BACKGROUND_TEXTURE = new Identifier("minecraft", "textures/gui/demo_background.png");
	public static final int BACKGROUND_WIDTH = 248;
    public static final int BACKGROUND_HEIGHT = 180;
    
    private int backgroundPosX;
    private int backgroundPosY;
    
    List<TextureElement> textures = new ArrayList<>();
	
    private static Identifier expertiseTexture;
    private String expertise;
    
    private Text titleText;
    private Text tip1Text;
    private Text tip2Text;
    private Text tip3Text;
    private Text tip4Text;
    
    private ButtonWidget hireButton;
    
    private SettlerEntity settler;
    
    public HireSettlerScreen(SettlerEntity settler)
    {
        super(Text.literal("Hire a Settler Screen"));
        this.settler = settler;
        this.expertise = settler.getSettlerExpertise();
        
        switch (expertise)
        {
        	case "CORE":
        		expertiseTexture = new Identifier("minecraft", "textures/item/writable_book.png");
        		break;
        	case "GATHERING":
        		expertiseTexture = new Identifier("minecraft", "textures/item/bundle_filled.png");
        		break;
        	case "TRADING":
        		expertiseTexture = new Identifier("minecraft", "textures/item/raw_gold.png");
        		break;
        	case "CRAFTING":
        		expertiseTexture = new Identifier("minecraft", "textures/item/cauldron.png");
        		break;
        	case "RANCHING":
        		expertiseTexture = new Identifier("minecraft", "textures/item/egg.png");
        		break;
        	case "FIGHTING":
        		expertiseTexture = new Identifier("minecraft", "textures/item/chainmail_helmet.png");
        		break;
        	default:
        		System.err.println("No settler expertise found!");
        		break;
        }
    }
    
    @Override
    protected void init() 
    {
    	backgroundPosX = ((this.width - BACKGROUND_WIDTH) / 2) + UI_OFFSET_X;
        backgroundPosY = ((this.height - BACKGROUND_HEIGHT) / 2) + UI_OFFSET_Y;
        
        textures.add(new TextureElement(expertiseTexture, (backgroundPosX + 160), (backgroundPosY + 8), 12, 12, "Expertise in " + expertise));
        titleText = ((MutableText) Text.literal("Recruit Architect to Begin")).formatted(Formatting.BOLD).formatted(Formatting.UNDERLINE);
        tip1Text = ((MutableText) Text.literal("* Can have two architects per settlement"));
        tip2Text = ((MutableText) Text.literal("* Townhall is the architect's home"));
        tip3Text = ((MutableText) Text.literal("* Right-click settlers for options"));
        tip4Text = ((MutableText) Text.literal("* Rename settlers with nametags"));

        this.hireButton = ButtonWidget.builder(Text.literal("Recruit"), button ->
        {
        	PlayerEntity player = MinecraftClient.getInstance().player;
        	PlayerData playerData = PlayerData.map.get(player.getUuid());
        	if (player != null && playerData != null && settler != null)
        	{
            	PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
            	passedData.writeString(playerData.getFaction());
            	passedData.writeString(settler.getSettlerName());
            	passedData.writeString(settler.getSettlerGender());
            	passedData.writeString(settler.getSettlerExpertise());
            	passedData.writeBlockPos(settler.getBlockPos());
        	    ClientPlayNetworking.send(FrontierPackets.HIRE_ARCHITECT_ID, passedData);
                MinecraftClient.getInstance().setScreen(null);
        	}
        }).dimensions(backgroundPosX + 173, backgroundPosY + 91, 60, 20).build();
        //hireButton.active = false;
        addDrawableChild(hireButton);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) 
    {
        this.renderBackground(context);
        context.drawTexture(BACKGROUND_TEXTURE, backgroundPosX, backgroundPosY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);

        context.drawText(this.textRenderer, titleText, (backgroundPosX + 47), (backgroundPosY + 10), new Color(255, 255, 255).getRGB(), true);
        
        context.drawText(this.textRenderer, tip1Text, (backgroundPosX + 10), (backgroundPosY + 35), new Color(100, 100, 100).getRGB(), false);
        context.drawText(this.textRenderer, tip2Text, (backgroundPosX + 10), (backgroundPosY + 50), new Color(100, 100, 100).getRGB(), false);
        context.drawText(this.textRenderer, tip3Text, (backgroundPosX + 10), (backgroundPosY + 65), new Color(100, 100, 100).getRGB(), false);
        context.drawText(this.textRenderer, tip4Text, (backgroundPosX + 10), (backgroundPosY + 80), new Color(100, 100, 100).getRGB(), false);
        
        for (TextureElement element : textures)
    	{
            element.draw(context);
            if (element.isMouseOver(mouseX, mouseY))
                renderTooltip(context, element.getToolTip(), mouseX, mouseY);
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void renderTooltip(DrawContext context, String text, int mouseX, int mouseY) 
    {
    	if (text != null)
    		context.drawTooltip(textRenderer, Text.literal(text), mouseX, mouseY);
    }
    
    @Override
    public boolean shouldPause() 
    {
    	return false;
    }
}