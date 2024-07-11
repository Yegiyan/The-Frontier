package com.frontier.gui;

import java.awt.Color;

import com.frontier.network.FrontierPackets;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class CreateSettlementScreen extends Screen 
{
	public static final int UI_OFFSET_X = 0;
    public static final int UI_OFFSET_Y = 0;
    
	private static final Identifier BACKGROUND_TEXTURE = new Identifier("minecraft", "textures/gui/demo_background.png");
	public static final int BACKGROUND_WIDTH = 248;
    public static final int BACKGROUND_HEIGHT = 180;
    
    public static final int MAX_NAME_LENGTH = 14;
    
    private int backgroundPosX;
    private int backgroundPosY;
	
    private Text titleText;
    private Text factionNameText;
    private Text tip1Text;
    private Text tip2Text;
    private Text tip3Text;
    
    private TextFieldWidget nameField;
    private ButtonWidget createButton;

    public CreateSettlementScreen() 
    {
        super(Text.literal("Create Settlement Screen"));
    }
    
	@Override
    protected void init() 
    {
		backgroundPosX = ((this.width - BACKGROUND_WIDTH) / 2) + UI_OFFSET_X;
        backgroundPosY = ((this.height - BACKGROUND_HEIGHT) / 2) + UI_OFFSET_Y;
        
        titleText = ((MutableText) Text.literal("Create a New Settlement")).formatted(Formatting.BOLD).formatted(Formatting.UNDERLINE);
        factionNameText = Text.literal("Faction Name: ").formatted(Formatting.WHITE);
        tip1Text = ((MutableText) Text.literal("* Spawns a Townhall"));
        tip2Text = ((MutableText) Text.literal("* Faction Radius: 128x128"));
        tip3Text = ((MutableText) Text.literal("* Watchtowers Expand Radius"));
        
        nameField = new TextFieldWidget(this.textRenderer, backgroundPosX + 90, backgroundPosY + 36, 140, 16, Text.literal("Faction Name"));
        nameField.setMaxLength(MAX_NAME_LENGTH);

        this.createButton = ButtonWidget.builder(Text.literal("Create"), button -> 
        {        	
        	String factionName = nameField.getText().trim();
        	
        	PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
    	    passedData.writeString(factionName);
    	    ClientPlayNetworking.send(FrontierPackets.CREATE_SETTLEMENT_ID, passedData);
        	
            MinecraftClient.getInstance().setScreen(null);
        }).dimensions(backgroundPosX + 173, backgroundPosY + 91, 60, 20).build();
        createButton.active = false;
        
        nameField.setChangedListener((text) -> 
        {
        	if (text.isBlank() || text.equalsIgnoreCase("N/A") || text.length() < 3 || !text.matches("[a-zA-Z ]+"))
        	    createButton.active = false;
        	else
        		createButton.active = true;
        });
        
        addDrawableChild(nameField);
        addDrawableChild(createButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) 
    {
        this.renderBackground(context);
        context.drawTexture(BACKGROUND_TEXTURE, backgroundPosX, backgroundPosY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);

        context.drawText(this.textRenderer, titleText, (backgroundPosX + 52), (backgroundPosY + 10), new Color(255, 255, 255).getRGB(), true);
        context.drawText(this.textRenderer, factionNameText, (backgroundPosX + 15), (backgroundPosY + 40), new Color(255, 255, 255).getRGB(), true);
        
        context.drawText(this.textRenderer, tip1Text, (backgroundPosX + 10), (backgroundPosY + 70), new Color(100, 100, 100).getRGB(), false);
        context.drawText(this.textRenderer, tip2Text, (backgroundPosX + 10), (backgroundPosY + 85), new Color(100, 100, 100).getRGB(), false);
        context.drawText(this.textRenderer, tip3Text, (backgroundPosX + 10), (backgroundPosY + 100), new Color(100, 100, 100).getRGB(), false);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean shouldPause() 
    {
    	return false;
    }
}