package com.frontier.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.frontier.PlayerData;
import com.frontier.gui.util.TextureElement;
import com.frontier.regions.RegionManager;
import com.frontier.settlements.SettlementManager;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class PlayerCardScreen extends Screen
{
	private static final int UI_OFFSET_X = 0;
	private static final int UI_OFFSET_Y = 70;
	
	private static final Identifier BACKGROUND_TEXTURE = new Identifier("minecraft", "textures/gui/demo_background.png");
	private static final int BACKGROUND_WIDTH = 256;
	private static final int BACKGROUND_HEIGHT = 330;

	private static final Identifier FACTION_TEXTURE = new Identifier("minecraft", "textures/mob_effect/resistance.png");
	private static final Identifier RENOWN_TEXTURE = new Identifier("minecraft", "textures/mob_effect/hero_of_the_village.png");
	private static final Identifier REGION_TEXTURE = new Identifier("minecraft", "textures/mob_effect/darkness.png");
	private static final Identifier TERRITORY_TEXTURE = new Identifier("minecraft", "textures/item/mojang_banner_pattern.png");
	private static final Identifier SEPARATOR_TEXTURE = new Identifier("minecraft", "textures/gui/header_separator.png");

	private static final int ORANGE = 0xF74B03;
	private static final Map<String, Identifier> PROFESSION_TEXTURES = Map.of(
			"Adventurer", new Identifier("minecraft", "textures/mob_effect/speed.png"),
			"Denizen", new Identifier("minecraft", "textures/item/totem_of_undying.png"), 
			"Merchant", new Identifier("minecraft", "textures/item/bundle.png"), 
			"Commander", new Identifier("minecraft", "textures/mob_effect/strength.png"), 
			"Leader", new Identifier("minecraft", "textures/item/bell.png"), 
			"Outlaw", new Identifier("minecraft", "textures/item/spyglass.png"), 
			"Maverick", new Identifier("minecraft", "textures/mob_effect/bad_omen.png"));

	private List<TextureElement> textures = new ArrayList<>();
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
	private Identifier skinTexture;

	public PlayerCardScreen()
	{
		super(Text.literal("Player Card Screen"));
	}

	@Override
	protected void init()
	{
		PlayerEntity player = MinecraftClient.getInstance().player;
		PlayerData playerData = PlayerData.players.get(player.getUuid());

		professionTexture = PROFESSION_TEXTURES.getOrDefault(playerData.getProfession(), new Identifier("minecraft", "textures/item/barrier.png"));

		backgroundPosX = (this.width - BACKGROUND_WIDTH) / 2 + UI_OFFSET_X;
		backgroundPosY = (this.height - BACKGROUND_HEIGHT) / 2 + UI_OFFSET_Y;

		if (player.getWorld().getRegistryKey().equals(World.OVERWORLD))
			textures.add(new TextureElement(REGION_TEXTURE, backgroundPosX + 12, backgroundPosY + 35, 10, 10, "Current Region (" + RegionManager.getPlayerDirection(player.getBlockPos()) + ")" + RegionManager.getRegionWild(player.getBlockPos()), 1.0f));
		else if (player.getWorld().getRegistryKey().equals(World.NETHER))
			textures.add(new TextureElement(REGION_TEXTURE, backgroundPosX + 12, backgroundPosY + 35, 10, 10, "Current Region" + RegionManager.getRegionWild(player.getBlockPos()), 1.0f));
		else if (player.getWorld().getRegistryKey().equals(World.END))
			textures.add(new TextureElement(REGION_TEXTURE, backgroundPosX + 12, backgroundPosY + 35, 10, 10, "Current Region" + RegionManager.getRegionWild(player.getBlockPos()), 1.0f));
		else
			textures.add(new TextureElement(REGION_TEXTURE, backgroundPosX + 12, backgroundPosY + 35, 10, 10, "Current Region" + RegionManager.getRegionWild(player.getBlockPos()), 1.0f));
		
		textures.add(new TextureElement(TERRITORY_TEXTURE, backgroundPosX + 12, backgroundPosY + 55, 12, 12, "Current Territory", 1.0f));
		textures.add(new TextureElement(professionTexture, backgroundPosX + 225, backgroundPosY + 14, 12, 12, "Profession", 1.0f));
		textures.add(new TextureElement(FACTION_TEXTURE, backgroundPosX + 225, backgroundPosY + 35, 12, 12, "Faction", 1.0f));
		textures.add(new TextureElement(RENOWN_TEXTURE, backgroundPosX + 225, backgroundPosY + 55, 12, 12, "Renown", 1.0f));

		if (player != null && playerData != null)
		{
			setSkinTexture(player);
			setRegionText(player);
			setTerritoryText(player, playerData);

			nameText = Text.literal(playerData.getName());
			factionText = Text.literal(playerData.getFaction());
			professionText = Text.literal(playerData.getProfession());
			renownText = Text.literal(playerData.getRenownAsString());

			initButtons();
		}
	}

	private void initButtons()
	{
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
			{
				context.drawTexture(skinTexture, backgroundPosX + 13, backgroundPosY + 15, 8, 8, 8, 8, 64, 64);
			}

			textures.forEach(element ->
			{
				element.draw(context);
				if (element.isMouseOver(mouseX, mouseY))
					renderTooltip(context, element.getToolTip(), mouseX, mouseY);
			});

			drawText(context, nameText, backgroundPosX + 30, backgroundPosY + 15);
			drawText(context, regionText, backgroundPosX + 30, backgroundPosY + 36);
			drawText(context, territoryText, backgroundPosX + 30, backgroundPosY + 57);
			drawTextR(context, professionText, backgroundPosX + 215, backgroundPosY + 15);
			drawTextR(context, factionText, backgroundPosX + 215, backgroundPosY + 36);
			drawTextR(context, renownText, backgroundPosX + 215, backgroundPosY + 57);

			context.drawTexture(SEPARATOR_TEXTURE, backgroundPosX + 12, backgroundPosY + 80, 0, 0, 225, 2, 32, 2);
		}

		super.render(context, mouseX, mouseY, delta);
	}

	private void setSkinTexture(PlayerEntity player)
	{
		MinecraftClient.getInstance().getSkinProvider().getTextures(player.getGameProfile()).get(MinecraftProfileTexture.Type.SKIN);
		Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = MinecraftClient.getInstance().getSkinProvider().getTextures(player.getGameProfile());
		skinTexture = textures.containsKey(MinecraftProfileTexture.Type.SKIN) ? MinecraftClient.getInstance().getSkinProvider().loadSkin(textures.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN) : DefaultSkinHelper.getTexture(player.getUuid());
	}

	private void setRegionText(PlayerEntity player)
	{
		if (player.getWorld().getRegistryKey().equals(World.OVERWORLD))
			regionText = Text.literal(RegionManager.getPlayerRegion(player.getBlockPos()));
		else if (player.getWorld().getRegistryKey().equals(World.NETHER))
			regionText = Text.literal("§kNetherworld");
		else if (player.getWorld().getRegistryKey().equals(World.END))
			regionText = Text.literal("The End");
		else
			regionText = Text.literal("Unknown Region");
	}

	private void setTerritoryText(PlayerEntity player, PlayerData playerData)
	{
		if (player.getWorld().getRegistryKey().equals(World.OVERWORLD))
		{
			String territory = SettlementManager.getSettlementTerritory(player.getBlockPos());
			if (SettlementManager.settlementExists(territory))
			{
				int playerReputation = playerData.getReputation(territory);
				territoryText = getFormattedTerritoryText(playerData, territory, playerReputation);
			}
			else
				territoryText = Text.literal(SettlementManager.getSettlementTerritory(player.getBlockPos())).formatted(Formatting.WHITE);
		}
		else if (player.getWorld().getRegistryKey().equals(World.NETHER))
			territoryText = Text.literal("§kNether");
		else if (player.getWorld().getRegistryKey().equals(World.END))
			territoryText = Text.literal("The End");
	}

	private Text getFormattedTerritoryText(PlayerData playerData, String territory, int playerReputation)
	{
		if (playerData.getProfession().equals("Leader") && playerData.getFaction().equals(territory))
			return Text.literal(territory).formatted(Formatting.BLUE);
		else if (playerReputation >= -10 && playerReputation <= 10)
			return Text.literal(territory).formatted(Formatting.WHITE);
		else if (playerReputation >= 11 && playerReputation <= 50)
			return Text.literal(territory).formatted(Formatting.GREEN);
		else if (playerReputation >= 51 && playerReputation <= 99)
			return Text.literal(territory).formatted(Formatting.DARK_AQUA);
		else if (playerReputation == 100)
			return Text.literal(territory).formatted(Formatting.BLUE);
		else if (playerReputation >= -11 && playerReputation <= -50)
			return Text.literal(territory).formatted(Formatting.YELLOW);
		else if (playerReputation >= -51 && playerReputation <= -99)
			return Text.literal(territory).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(ORANGE)));
		else if (playerReputation == -100)
			return Text.literal(territory).formatted(Formatting.RED);
		else
			return Text.literal(territory).formatted(Formatting.WHITE);
	}
	
	private void drawText(DrawContext context, Text text, int x, int y)
	{
		context.drawText(this.textRenderer, text, x, y, Color.WHITE.getRGB(), true);
	}

	private void renderTooltip(DrawContext context, String text, int mouseX, int mouseY)
	{
		if (text != null)
			context.drawTooltip(textRenderer, Text.literal(text), mouseX, mouseY);
	}

	private void drawTextR(DrawContext context, Text text, int rightEdgeX, int y)
	{
		int textWidth = textRenderer.getWidth(text);
		int x = rightEdgeX - textWidth;
		context.drawText(textRenderer, text, x, y, Color.WHITE.getRGB(), true);
	}

	@Override
	public boolean shouldPause()
	{
		return false;
	}
}