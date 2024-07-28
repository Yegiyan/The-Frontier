package com.frontier.gui;

import java.awt.Color;

import com.frontier.gui.util.TextUtil;
import com.frontier.gui.util.TextUtil.TextAlign;
import com.frontier.network.FrontierPacketsServer;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class CreateSettlementScreen extends Screen 
{
	public static final int UI_OFFSET_X = 0;
    public static final int UI_OFFSET_Y = 65;
    
	private static final Identifier BACKGROUND_TEXTURE = new Identifier("minecraft", "textures/gui/demo_background.png");
	public static final int BACKGROUND_WIDTH = 270;
    public static final int BACKGROUND_HEIGHT = 345;
    
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
        
        titleText = Text.literal("Create a New Settlement").formatted(Formatting.BOLD).formatted(Formatting.UNDERLINE);
        factionNameText = Text.literal("Faction Name: ").formatted(Formatting.WHITE);
        tip1Text = Text.literal("Word will spread about your new settlement, inviting nomads who seek work and a new home.");
        tip2Text = Text.literal("Designate your own custom structure as a town hall by placing this blueprint in an item frame above a door. Hire an architect to buy more blueprints and build, upgrade, and repair structures for you.");
        tip3Text = Text.literal("A 128x128 border will be designated as your factions territory from THIS spot! Territory can be expanded by building watchtowers.");
        
        nameField = new TextFieldWidget(this.textRenderer, backgroundPosX + 130, backgroundPosY + 36, 95, 16, Text.literal("Faction Name"));
        nameField.setMaxLength(MAX_NAME_LENGTH);

        this.createButton = ButtonWidget.builder(Text.literal("Create"), button -> 
        {        	
        	String factionName = nameField.getText().trim();
        	
        	PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
    	    passedData.writeString(factionName);
    	    ClientPlayNetworking.send(FrontierPacketsServer.CREATE_SETTLEMENT_ID, passedData);
        	
            MinecraftClient.getInstance().setScreen(null);
        }).dimensions(backgroundPosX + 105, backgroundPosY + 187, 60, 20).build();
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

        context.drawText(this.textRenderer, titleText, (backgroundPosX + 65), (backgroundPosY + 15), new Color(255, 255, 255).getRGB(), true);
        context.drawText(this.textRenderer, factionNameText, (backgroundPosX + 50), (backgroundPosY + 40), new Color(255, 255, 255).getRGB(), true);
        
        TextUtil.drawText(context, textRenderer, tip1Text, backgroundPosX + 132, backgroundPosY + 65, new Color(255, 255, 255).getRGB(), true, true, 235, TextAlign.CENTER);
        TextUtil.drawText(context, textRenderer, tip2Text, backgroundPosX + 132, backgroundPosY + 90, new Color(255, 255, 255).getRGB(), true, true, 240, TextAlign.CENTER);
        TextUtil.drawText(context, textRenderer, tip3Text, backgroundPosX + 132, backgroundPosY + 146, new Color(255, 255, 255).getRGB(), true, true, 240, TextAlign.CENTER);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean shouldPause() 
    {
    	return false;
    }
}