package com.frontier.gui;

import java.awt.Color;

import com.frontier.PlayerData;
import com.frontier.entities.SettlerEntity;
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

public class HireArchitectScreen extends Screen
{
	public static final int UI_OFFSET_X = 0;
    public static final int UI_OFFSET_Y = 0;
    
	private static final Identifier BACKGROUND_TEXTURE = new Identifier("minecraft", "textures/gui/demo_background.png");
	public static final int BACKGROUND_WIDTH = 248;
    public static final int BACKGROUND_HEIGHT = 180;
    
    private int backgroundPosX;
    private int backgroundPosY;
	
    private Text titleText;
    private Text tip1Text;
    private Text tip2Text;
    private Text tip3Text;
    private Text tip4Text;
    
    private ButtonWidget recruitButton;
    
    private SettlerEntity settler;
    
    public HireArchitectScreen(SettlerEntity settler)
    {
        super(Text.literal("Hire an Architect Screen"));
        this.settler = settler;
    }
    
    @Override
    protected void init() 
    {
    	backgroundPosX = ((this.width - BACKGROUND_WIDTH) / 2) + UI_OFFSET_X;
        backgroundPosY = ((this.height - BACKGROUND_HEIGHT) / 2) + UI_OFFSET_Y;
        
        titleText = ((MutableText) Text.literal("Recruit Architect to Begin")).formatted(Formatting.BOLD).formatted(Formatting.UNDERLINE);
        tip1Text = ((MutableText) Text.literal("* Can have two architects per settlement"));
        tip2Text = ((MutableText) Text.literal("* Townhall is the architect's home"));
        tip3Text = ((MutableText) Text.literal("* Right-click settlers for options"));
        tip4Text = ((MutableText) Text.literal("* Rename settlers with nametags"));

        this.recruitButton = ButtonWidget.builder(Text.literal("Recruit"), button ->
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
            	passedData.writeInt(settler.getSettlerMorale());
            	passedData.writeInt(settler.getSettlerSkill());
            	passedData.writeBlockPos(settler.getBlockPos());
        	    ClientPlayNetworking.send(FrontierPackets.HIRE_ARCHITECT_ID, passedData);
                MinecraftClient.getInstance().setScreen(null);
        	}
        }).dimensions(backgroundPosX + 173, backgroundPosY + 91, 60, 20).build();
        //recruitButton.active = false;
        addDrawableChild(recruitButton);
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
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean shouldPause() 
    {
    	return false;
    }
}