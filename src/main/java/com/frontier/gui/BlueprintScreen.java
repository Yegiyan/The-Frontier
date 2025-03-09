package com.frontier.gui;

import java.awt.Color;

import com.frontier.PlayerData;
import com.frontier.blueprint.BlueprintState;
import com.frontier.network.FrontierPacketsServer;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class BlueprintScreen extends Screen 
{
	public static final int UI_OFFSET_X = 0;
    public static final int UI_OFFSET_Y = 15;
    
	private static final Identifier BACKGROUND_TEXTURE = new Identifier("minecraft", "textures/gui/demo_background.png");
	public static final int BACKGROUND_WIDTH = 310;
    public static final int BACKGROUND_HEIGHT = 180;
    
    private int backgroundPosX;
    private int backgroundPosY;
	
    private Text titleText;
    private Text tip1Text;
    private Text tip2Text;
    private Text tip3Text;
    
    private ButtonWidget confirmButton;
    private ButtonWidget inspectButton;
    private ButtonWidget cancelButton;

    BlueprintState blueprintState;
    
    public BlueprintScreen(BlueprintState blueprintState)
    {
        super(Text.literal("Blueprint Screen"));
        this.blueprintState = blueprintState;
    }
    
	@Override
    protected void init() 
    {
		backgroundPosX = ((this.width - BACKGROUND_WIDTH) / 2) + UI_OFFSET_X;
        backgroundPosY = ((this.height - BACKGROUND_HEIGHT) / 2) + UI_OFFSET_Y;
        
        titleText = Text.literal("Confirm Blueprint Placement").formatted(Formatting.BOLD).formatted(Formatting.UNDERLINE);
        tip1Text = Text.literal("Blue bounding box is the tier 0 size of the structure");
        tip2Text = Text.literal("Inspecting will allow you to view your placement");
        tip3Text = Text.literal("Right click the blueprint to come back to this menu");

        this.confirmButton = ButtonWidget.builder(Text.literal("Confirm"), button ->
        {
            PlayerEntity player = MinecraftClient.getInstance().player;
            PlayerData playerData = PlayerData.players.get(player.getUuid());

            PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
            passedData.writeString(playerData.getFaction());
            passedData.writeString(blueprintState.getName());
            passedData.writeBlockPos(blueprintState.getPlacementPos());
            passedData.writeEnumConstant(blueprintState.getFacing());
            ClientPlayNetworking.send(FrontierPacketsServer.BLUEPRINT_PLACEMENT_ID, passedData);
            
            blueprintState.reset();
            MinecraftClient.getInstance().setScreen(null);
        }).dimensions(backgroundPosX + 232, backgroundPosY + 91, 60, 20).build();
        
        this.inspectButton = ButtonWidget.builder(Text.literal("Inspect"), button ->
        {
        	blueprintState.setInspecting(true);
            MinecraftClient.getInstance().setScreen(null);
        }).dimensions(backgroundPosX + 120, backgroundPosY + 91, 60, 20).build();
        
        this.cancelButton = ButtonWidget.builder(Text.literal("Cancel"), button ->
        {
        	blueprintState.reset();
            MinecraftClient.getInstance().setScreen(null);
        }).dimensions(backgroundPosX + 8, backgroundPosY + 91, 60, 20).build();
        
        addDrawableChild(confirmButton);
        addDrawableChild(inspectButton);
        addDrawableChild(cancelButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) 
    {
        this.renderBackground(context);
        context.drawTexture(BACKGROUND_TEXTURE, backgroundPosX, backgroundPosY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);

        context.drawText(this.textRenderer, titleText, (backgroundPosX + 68), (backgroundPosY + 10), new Color(255, 255, 255).getRGB(), true);
        context.drawText(this.textRenderer, tip1Text, (backgroundPosX + 16), (backgroundPosY + 35), new Color(80, 80, 80).getRGB(), false);
        context.drawText(this.textRenderer, tip2Text, (backgroundPosX + 31), (backgroundPosY + 50), new Color(80, 80, 80).getRGB(), false);
        context.drawText(this.textRenderer, tip3Text, (backgroundPosX + 23), (backgroundPosY + 65), new Color(80, 80, 80).getRGB(), false);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean shouldPause() 
    {
    	return false;
    }
}