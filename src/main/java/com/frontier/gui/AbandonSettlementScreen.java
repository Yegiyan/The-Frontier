package com.frontier.gui;

import java.awt.Color;

import com.frontier.PlayerData;
import com.frontier.network.FrontierPackets;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class AbandonSettlementScreen extends Screen 
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
    
    private ButtonWidget createButton;
    private ButtonWidget electButton;

    public AbandonSettlementScreen() 
    {
        super(Text.literal("Abandon Settlement Screen"));
    }
    
	@Override
    protected void init() 
    {
		backgroundPosX = ((this.width - BACKGROUND_WIDTH) / 2) + UI_OFFSET_X;
        backgroundPosY = ((this.height - BACKGROUND_HEIGHT) / 2) + UI_OFFSET_Y;
        
        titleText = ((MutableText) Text.literal("Abandon Settlement")).formatted(Formatting.BOLD).formatted(Formatting.UNDERLINE);
        tip1Text = ((MutableText) Text.literal("* Settlers will elect someone if you don't"));
        tip2Text = ((MutableText) Text.literal("* You will lose your leader privileges"));
        tip3Text = ((MutableText) Text.literal("* This CANNOT be undone"));

        this.createButton = ButtonWidget.builder(Text.literal("Abandon"), button -> 
        {        	
        	PlayerEntity player = MinecraftClient.getInstance().player;
        	PlayerData playerData = PlayerData.players.get(player.getUuid());
        	
        	PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
    	    passedData.writeString(playerData.getFaction());
    	    ClientPlayNetworking.send(FrontierPackets.ABANDON_SETTLEMENT_ID, passedData);
        	
            MinecraftClient.getInstance().setScreen(null);
        }).dimensions(backgroundPosX + 173, backgroundPosY + 91, 60, 20).build();
        
        this.electButton = ButtonWidget.builder(Text.literal("Elect"), button -> 
        {        	
        	// for when we can choose a successor, hello future hike ;D - 11/12/23
            MinecraftClient.getInstance().setScreen(null);
        }).dimensions(backgroundPosX + 8, backgroundPosY + 91, 60, 20).tooltip(Tooltip.of(Text.literal("Not implemented yet"))).build();
        electButton.active = false;
        
        addDrawableChild(createButton);
        addDrawableChild(electButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) 
    {
        this.renderBackground(context);
        context.drawTexture(BACKGROUND_TEXTURE, backgroundPosX, backgroundPosY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);

        context.drawText(this.textRenderer, titleText, (backgroundPosX + 68), (backgroundPosY + 10), new Color(255, 255, 255).getRGB(), true);
        context.drawText(this.textRenderer, tip1Text, (backgroundPosX + 10), (backgroundPosY + 35), new Color(80, 80, 80).getRGB(), false);
        context.drawText(this.textRenderer, tip2Text, (backgroundPosX + 10), (backgroundPosY + 50), new Color(80, 80, 80).getRGB(), false);
        context.drawText(this.textRenderer, tip3Text, (backgroundPosX + 10), (backgroundPosY + 65), new Color(80, 80, 80).getRGB(), false);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean shouldPause() 
    {
    	return false;
    }
}