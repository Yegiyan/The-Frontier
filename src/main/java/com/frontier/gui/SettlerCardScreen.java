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
    public static final int BACKGROUND_WIDTH = 244;
    public static final int BACKGROUND_HEIGHT = 205;

    private static final Identifier SLOT_TEXTURE = new Identifier("minecraft", "textures/gui/container/generic_54.png");
    private static final Identifier PROFESSION_TEXTURE = new Identifier("minecraft", "textures/item/writable_book.png");
    private static final Identifier NOTIFICATION_TEXTURE = new Identifier("minecraft", "textures/gui/unseen_notification.png");
    
    private static final Identifier BARS_TEXTURE = new Identifier("minecraft", "textures/gui/bars.png");
    
    private static final Identifier HEALTH_TEXTURE = new Identifier("minecraft", "textures/gui/icons.png");
    private static final Identifier HUNGER_TEXTURE = new Identifier("minecraft", "textures/gui/icons.png");
    private static final Identifier MORALE_TEXTURE = new Identifier("minecraft", "textures/gui/icons.png");
    private static final Identifier SKILL_TEXTURE = new Identifier("minecraft", "textures/gui/icons.png");

    private int backgroundPosX;
    private int backgroundPosY;

    List<TextureElement> textures = new ArrayList<>();
    List<TextureElement> barTextures = new ArrayList<>();

    private ButtonWidget cardButton;
    private ButtonWidget taskButton;
    private boolean toggleMenu = false;

    MinecraftProfileTexture portrait;
    Identifier skinTexture;

    private Text nameText;
    private String notificationText;
    private Text professionText;
    
    private Text healthText;
    private Text hungerText;
    private Text moraleText;
    private Text skillText;

    private SettlerEntity settler;
    private SimpleInventory inventory;

    public SettlerCardScreen(SettlerEntity settler)
    {
        super(Text.literal("Settler Card Screen"));
        this.settler = settler;
        this.inventory = settler.getInventory();
        this.notificationText = "Notification!";
    }

    @Override
    protected void init()
    {
        PlayerEntity player = MinecraftClient.getInstance().player;
        PlayerData playerData = PlayerData.map.get(player.getUuid());

        backgroundPosX = ((this.width - BACKGROUND_WIDTH) / 2) + UI_OFFSET_X;
        backgroundPosY = ((this.height - BACKGROUND_HEIGHT) / 2) + UI_OFFSET_Y;

        textures.add(new TextureElement(PROFESSION_TEXTURE, (backgroundPosX + 212), (backgroundPosY + 9), 12, 12, "Profession", 1.0f));
        textures.add(new TextureElement(NOTIFICATION_TEXTURE, (backgroundPosX + 191), (backgroundPosY + 86), 12, 12, notificationText, 2.0f));
        
        barTextures.add(new TextureElement(HEALTH_TEXTURE, (backgroundPosX + 10), (backgroundPosY + 25), 9, 9, 53, 0, 256, 256, "Health", 1.0f));
        barTextures.add(new TextureElement(HUNGER_TEXTURE, (backgroundPosX + 10), (backgroundPosY + 36), 9, 8, 53, 28, 256, 256, "Hunger", 1.0f));
        barTextures.add(new TextureElement(MORALE_TEXTURE, (backgroundPosX + 10), (backgroundPosY + 45), 9, 9, 161, 0, 256, 256, "Morale", 1.0f));
        barTextures.add(new TextureElement(SKILL_TEXTURE, (backgroundPosX + 10), (backgroundPosY + 55), 9, 9, 89, 0, 256, 256, "Skill", 1.0f));

        if (player != null && playerData != null)
        {
            this.portrait = MinecraftClient.getInstance().getSkinProvider().getTextures(player.getGameProfile()).get(MinecraftProfileTexture.Type.SKIN);

            if (portrait != null)
                skinTexture = new Identifier(portrait.getUrl());
            else 
                skinTexture = new Identifier("minecraft", "textures/entity/player/wide/steve.png");
        }

        nameText = ((MutableText) Text.literal(settler.getSettlerName()));
        professionText = ((MutableText) Text.literal(settler.getSettlerProfession()));

        healthText = Text.literal(String.format("%.0f", (settler.getHealth() / 20.0f) * 100));
        hungerText = Text.literal(String.valueOf(settler.getSettlerHunger()));
        moraleText = Text.literal(String.valueOf(settler.getSettlerMorale()));
        skillText = Text.literal(String.valueOf(settler.getSettlerSkill()));

        this.cardButton = ButtonWidget.builder(Text.literal("INFORMATION"), button -> { toggleMenu = false; }).dimensions(backgroundPosX + 0, backgroundPosY - 25, 80, 20).build();
        this.taskButton = ButtonWidget.builder(Text.literal("TASK LIST"), button -> { toggleMenu = true; }).dimensions(backgroundPosX + 156, backgroundPosY - 25, 80, 20).build();

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

            int inventoryStartX = backgroundPosX + 11;
            int inventoryStartY = backgroundPosY + 73;
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
            
            for (TextureElement element : barTextures)
            {
                element.drawRect(context);
                if (element.isMouseOver(mouseX, mouseY))
                    renderTooltip(context, element.getToolTip(), mouseX, mouseY);
            }

            context.drawText(this.textRenderer, nameText, (backgroundPosX + 25), (backgroundPosY + 10), new Color(255, 255, 255).getRGB(), true);
            drawTextR(context, this.textRenderer, professionText, (backgroundPosX + 203), (backgroundPosY + 10), new Color(255, 255, 255).getRGB(), true);

            context.drawText(this.textRenderer, healthText, (backgroundPosX + 212), (backgroundPosY + 26), new Color(75, 75, 75).getRGB(), false);
            context.drawText(this.textRenderer, hungerText, (backgroundPosX + 212), (backgroundPosY + 36), new Color(75, 75, 75).getRGB(), false);
            context.drawText(this.textRenderer, moraleText, (backgroundPosX + 212), (backgroundPosY + 46), new Color(75, 75, 75).getRGB(), false);
            context.drawText(this.textRenderer, skillText, (backgroundPosX + 212), (backgroundPosY + 56), new Color(75, 75, 75).getRGB(), false);
            
            drawBar(context, backgroundPosX + 25, backgroundPosY + 27, settler.getHealth() / 20.0f, 4);
            drawBar(context, backgroundPosX + 25, backgroundPosY + 37, settler.getSettlerHunger() / 100.0f, 12);
            drawBar(context, backgroundPosX + 25, backgroundPosY + 47, settler.getSettlerMorale() / 100.0f, 8);
            drawBar(context, backgroundPosX + 25, backgroundPosY + 57, settler.getSettlerSkill() / 100.0f, 6);
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
