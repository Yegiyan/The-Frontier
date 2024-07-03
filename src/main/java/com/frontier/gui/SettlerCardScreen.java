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
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SettlerCardScreen extends Screen
{
    public static final int UI_OFFSET_X = 0;
    public static final int UI_OFFSET_Y = 40;

    private static final Identifier BACKGROUND_TEXTURE = new Identifier("minecraft", "textures/gui/demo_background.png");
    public static final int BACKGROUND_WIDTH = 256;
    public static final int BACKGROUND_HEIGHT = 250;

    private static final Identifier NAMEPLATE_TEXTURE = new Identifier("minecraft", "textures/gui/social_interactions.png");
    
    private static final Identifier SLOT_TEXTURE = new Identifier("minecraft", "textures/gui/container/generic_54.png");
    private static final Identifier PROFESSION_TEXTURE = new Identifier("minecraft", "textures/item/writable_book.png");
    private static final Identifier NOTIFICATION_TEXTURE = new Identifier("minecraft", "textures/gui/unseen_notification.png");
    
    private static final Identifier SEPARATOR_TEXTURE = new Identifier("minecraft", "textures/gui/header_separator.png");
    
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
        backgroundPosX = ((this.width - BACKGROUND_WIDTH) / 2) + UI_OFFSET_X;
        backgroundPosY = ((this.height - BACKGROUND_HEIGHT) / 2) + UI_OFFSET_Y;

        barTextures.add(new TextureElement(NAMEPLATE_TEXTURE, (backgroundPosX + 5), (backgroundPosY + 8), 238, 36, 0, 0, 256, 256, null, 1.0f));
        
        textures.add(new TextureElement(PROFESSION_TEXTURE, (backgroundPosX + 216), (backgroundPosY + 20), 12, 12, "Profession", 1.0f));
        textures.add(new TextureElement(NOTIFICATION_TEXTURE, (backgroundPosX + 197), (backgroundPosY + 114), 12, 12, notificationText, 2.0f));
        
        barTextures.add(new TextureElement(HEALTH_TEXTURE, (backgroundPosX + 14), (backgroundPosY + 48), 9, 9, 53, 0, 256, 256, "Health (" + settler.getHealth() + " / 20)", 1.0f));
        barTextures.add(new TextureElement(HUNGER_TEXTURE, (backgroundPosX + 14), (backgroundPosY + 59), 9, 8, 53, 28, 256, 256, "Hunger", 1.0f));
        barTextures.add(new TextureElement(MORALE_TEXTURE, (backgroundPosX + 14), (backgroundPosY + 68), 9, 9, 161, 0, 256, 256, "Morale", 1.0f));
        barTextures.add(new TextureElement(SKILL_TEXTURE, (backgroundPosX + 14), (backgroundPosY + 78), 9, 9, 89, 0, 256, 256, "Skill", 1.0f));

        boolean isMale = settler.getSettlerGender().equals("Male");
        Identifier[] textures = settler.getTextures(isMale);
        int textureIndex = settler.getSettlerTexture();

        if (textures != null && textures.length > 0)
            skinTexture = textures[textureIndex];
        else
            skinTexture = DefaultSkinHelper.getTexture(settler.getUuid());

        nameText = ((MutableText) Text.literal(settler.getSettlerName()));
        professionText = ((MutableText) Text.literal(settler.getSettlerProfession()));

        healthText = Text.literal(String.format("%.0f", (settler.getHealth() / 20.0f) * 100));
        hungerText = Text.literal(String.valueOf(settler.getSettlerHunger()));
        moraleText = Text.literal(String.valueOf(settler.getSettlerMorale()));
        skillText = Text.literal(String.valueOf(settler.getSettlerSkill()));

        this.cardButton = ButtonWidget.builder(Text.literal("INFORMATION"), button -> { toggleMenu = false; }).dimensions(backgroundPosX + 0, backgroundPosY - 25, 80, 20).build();
        this.taskButton = ButtonWidget.builder(Text.literal("TASK LIST"), button -> { toggleMenu = true; }).dimensions(backgroundPosX + 168, backgroundPosY - 25, 80, 20).build();

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
            int inventoryStartY = backgroundPosY + 102;
            for (int i = 0; i < inventory.size(); i++)
            {
                ItemStack stack = inventory.getStack(i);
                int slotX = inventoryStartX + (i % 9) * 18;
                int slotY = inventoryStartY + (i / 9) * 18;
                drawSlot(stack, slotX, slotY, context);
            }
            
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
            
            context.drawTexture(skinTexture, (backgroundPosX + 20), (backgroundPosY + 21), 8, 8, 8, 8, 64, 64);

            context.drawText(this.textRenderer, nameText, (backgroundPosX + 35), (backgroundPosY + 22), new Color(255, 255, 255).getRGB(), true);
            drawTextR(context, this.textRenderer, professionText, (backgroundPosX + 207), (backgroundPosY + 22), new Color(255, 255, 255).getRGB(), true);

            context.drawText(this.textRenderer, healthText, (backgroundPosX + 218), (backgroundPosY + 49), new Color(65, 65, 65).getRGB(), false);
            context.drawText(this.textRenderer, hungerText, (backgroundPosX + 218), (backgroundPosY + 59), new Color(65, 65, 65).getRGB(), false);
            context.drawText(this.textRenderer, moraleText, (backgroundPosX + 218), (backgroundPosY + 69), new Color(65, 65, 65).getRGB(), false);
            context.drawText(this.textRenderer, skillText, (backgroundPosX + 218), (backgroundPosY + 79), new Color(65, 65, 65).getRGB(), false);
            
            context.drawTexture(SEPARATOR_TEXTURE, (backgroundPosX + 10), (backgroundPosY + 93), 0, 0, 225, 2, 32, 2);
            
            drawBar(context, backgroundPosX + 29, backgroundPosY + 50, settler.getHealth() / 20.0f, 4);
            drawBar(context, backgroundPosX + 29, backgroundPosY + 60, settler.getSettlerHunger() / 100.0f, 12);
            drawBar(context, backgroundPosX + 29, backgroundPosY + 70, settler.getSettlerMorale() / 100.0f, 8);
            drawBar(context, backgroundPosX + 29, backgroundPosY + 80, settler.getSettlerSkill() / 100.0f, 6);
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