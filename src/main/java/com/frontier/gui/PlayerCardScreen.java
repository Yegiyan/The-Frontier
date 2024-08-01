package com.frontier.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.frontier.Frontier;
import com.frontier.PlayerData;
import com.frontier.gui.util.CheckboxElement;
import com.frontier.gui.util.SideButtonElement;
import com.frontier.gui.util.TextUtil;
import com.frontier.gui.util.TextUtil.TextAlign;
import com.frontier.gui.util.TextureElement;
import com.frontier.regions.RegionManager;
import com.frontier.settlements.SettlementManager;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CakeBlock;
import net.minecraft.block.CandleCakeBlock;
import net.minecraft.block.ChorusFlowerBlock;
import net.minecraft.block.ChorusPlantBlock;
import net.minecraft.block.CoralBlock;
import net.minecraft.block.CoralBlockBlock;
import net.minecraft.block.CoralFanBlock;
import net.minecraft.block.CoralParentBlock;
import net.minecraft.block.CoralWallFanBlock;
import net.minecraft.block.DeadCoralBlock;
import net.minecraft.block.DeadCoralFanBlock;
import net.minecraft.block.DeadCoralWallFanBlock;
import net.minecraft.block.FungusBlock;
import net.minecraft.block.GlowLichenBlock;
import net.minecraft.block.MelonBlock;
import net.minecraft.block.MushroomBlock;
import net.minecraft.block.MushroomPlantBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.PumpkinBlock;
import net.minecraft.block.SnifferEggBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.block.TurtleEggBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BoatItem;
import net.minecraft.item.BookItem;
import net.minecraft.item.BrushItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.CompassItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.DyeableHorseArmorItem;
import net.minecraft.item.EggItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.EmptyMapItem;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.EnderEyeItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.FireChargeItem;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.FireworkStarItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.GoatHornItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.HorseArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.KnowledgeBookItem;
import net.minecraft.item.LeadItem;
import net.minecraft.item.MinecartItem;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.item.NameTagItem;
import net.minecraft.item.OnAStickItem;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.SaddleItem;
import net.minecraft.item.ShearsItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SpyglassItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.item.WritableBookItem;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class PlayerCardScreen extends Screen
{
	private static final int UI_OFFSET_X = 0;
	private static final int UI_OFFSET_Y = 72;
	
	private static final Identifier BACKGROUND_TEXTURE = new Identifier("minecraft", "textures/gui/demo_background.png");
	private static final int BACKGROUND_WIDTH = 256;
	private static final int BACKGROUND_HEIGHT = 338;

	private static final Identifier NAMEPLATE_TEXTURE = new Identifier("minecraft", "textures/gui/social_interactions.png");
	private static final Identifier WINDOW_TEXTURE = new Identifier("minecraft", "textures/gui/advancements/window.png");
	private static final Identifier WINDOW_BACKGROUND_TEXTURE = new Identifier("minecraft", "textures/gui/advancements/widgets.png");
	
	private static final Identifier FACTION_TEXTURE = new Identifier("minecraft", "textures/mob_effect/resistance.png");
	private static final Identifier RENOWN_TEXTURE = new Identifier("minecraft", "textures/mob_effect/hero_of_the_village.png");
	//private static final Identifier REGION_TEXTURE = new Identifier("minecraft", "textures/mob_effect/darkness.png");
	//private static final Identifier TERRITORY_TEXTURE = new Identifier("minecraft", "textures/item/mojang_banner_pattern.png");
	private static final Identifier SEPARATOR_TEXTURE = new Identifier("minecraft", "textures/gui/header_separator.png");

	private static final Identifier RELATIONS_TEXTURE = new Identifier("minecraft", "textures/block/cartography_table_top.png");
	private static final Identifier RESOURCES_TEXTURE = new Identifier("minecraft", "textures/block/barrel_side.png");
	private static final Identifier INFORMATION_TEXTURE = new Identifier("minecraft", "textures/block/chiseled_bookshelf_occupied.png");
	
	private static final int ORANGE = 0xF74B03;
	private static final Map<String, Identifier> PROFESSION_TEXTURES = Map.of
			(
			"Adventurer", new Identifier("minecraft", "textures/item/spyglass.png"),
			"Denizen", new Identifier("minecraft", "textures/item/compass_19.png"), 
			"Merchant", new Identifier("minecraft", "textures/item/bundle_filled.png"), 
			"Commander", new Identifier("minecraft", "textures/item/goat_horn.png"),
			"Leader", new Identifier("minecraft", "textures/item/bell.png"), 
			"Outlaw", new Identifier("minecraft", "textures/mob_effect/bad_omen.png"), 
			"Maverick", new Identifier("minecraft", "textures/item/skull_pottery_sherd.png")
			);

	private int backgroundPosX;
	private int backgroundPosY;
	
	public enum Page { PLAYER, FACTION, BOUNTIES }
    private Page page;
    
    public enum Tab { RELATIONS, RESOURCES, STATISTICS }
    private Tab tab;
    
    private List<TextureElement> textures = new ArrayList<>();
    private List<TextureElement> rectTextures = new ArrayList<>();
    
    private List<SideButtonElement> sideButtons = new ArrayList<>();
    private boolean[] sideButtonStates;
	
	private Text nameText;
	private Text factionText;
	private Text professionText;
	private Text renownText;
	private Text regionText;
	private Text territoryText;
	private Text directionText;
	private Text biomeNameText;
	
	private Text resourcesText1;
    private Text resourcesText2;
	
	private ButtonWidget playerButton;
	private ButtonWidget bountyButton;
	private ButtonWidget factionButton;
	
	private static final int MAX_VISIBLE_ROWS = 6;
	private static final int ITEM_SIZE = 18;
	private static final int ITEMS_PER_ROW = 11;
	private static final int SCROLLBAR_HEIGHT = 116;
	private static final int SCROLLBAR_WIDTH = 2;
	List<ItemStack> structureInventory;
	private int scrollOffset = 0;
	
	private List<ItemStack> filteredInventory = new ArrayList<>();
	private List<CheckboxElement> checkboxes = new ArrayList<>();
	private boolean[] checkboxStates;
	
	private static Identifier professionTexture;
	private Identifier skinTexture;
	
	PlayerEntity player;
	PlayerData playerData;

	public PlayerCardScreen(List<ItemStack> structureInventory)
	{
		super(Text.literal("Player Card Screen"));
		player = MinecraftClient.getInstance().player;
		playerData = PlayerData.players.get(player.getUuid());
		this.structureInventory = structureInventory;
		page = Page.PLAYER;
		tab = Tab.RELATIONS;
	}

	@Override
	protected void init()
	{
		professionTexture = PROFESSION_TEXTURES.getOrDefault(playerData.getProfession(), new Identifier("minecraft", "textures/item/barrier.png"));

		backgroundPosX = (this.width - BACKGROUND_WIDTH) / 2 + UI_OFFSET_X;
		backgroundPosY = (this.height - BACKGROUND_HEIGHT) / 2 + UI_OFFSET_Y;

		textures.add(new TextureElement(RENOWN_TEXTURE, backgroundPosX + 20, backgroundPosY + 37, 12, 12, "Renown", 1.0f, 1.0f));
		
		textures.add(new TextureElement(professionTexture, backgroundPosX + 214, backgroundPosY + 24, 12, 12, "Profession", 1.0f, 1.0f));
		textures.add(new TextureElement(FACTION_TEXTURE, backgroundPosX + 214, backgroundPosY + 37, 12, 12, "Faction", 1.0f, 1.0f));
		
		//textures.add(new TextureElement(REGION_TEXTURE, backgroundPosX + 25, backgroundPosY + 91, 10, 10, "Current Region", 1.0f, 1.0f));
		//textures.add(new TextureElement(TERRITORY_TEXTURE, backgroundPosX + 25, backgroundPosY + 105, 12, 12, "Current Territory", 1.0f, 1.0f));
		
		if (player != null && playerData != null)
		{			
			setSkinTexture(player);
			setRegionText(player);
			setTerritoryText(player, playerData);

			nameText = Text.literal(playerData.getName());
			factionText = Text.literal(playerData.getFaction());
			professionText = Text.literal(playerData.getProfession());
			renownText = Text.literal(playerData.getRenownAsString());
			directionText = Text.literal(RegionManager.getPlayerDirection(player.getBlockPos()));
			biomeNameText = Text.literal(RegionManager.getBiomeName(player)); // "Windswept Savanna Plateau" RegionManager.getBiomeName(player)
			
			updateButtons();
		}
	}
	
	private void updateButtons()
	{
		initializeMainButtons();
		
		switch (page)
		{
			case FACTION:
				initializeFactionButtons();
				break;
			case BOUNTIES:
				break;
			default:
				break;
		}
	}

	private void initializeMainButtons()
	{
		this.playerButton = ButtonWidget.builder(Text.literal("Player"), button ->
		{
			page = Page.PLAYER;
			updateButtons();
		}).dimensions(backgroundPosX + 0, backgroundPosY - 25, 80, 20).build();
		
		this.factionButton = ButtonWidget.builder(Text.literal("Faction"), button ->
		{
			page = Page.FACTION;
			updateButtons();
		}).dimensions(backgroundPosX + 84, backgroundPosY - 25, 80, 20).build();

		this.bountyButton = ButtonWidget.builder(Text.literal("Bounties"), button ->
		{
			page = Page.BOUNTIES;
			updateButtons();
		}).dimensions(backgroundPosX + 168, backgroundPosY - 25, 80, 20).build();

		addDrawableChild(playerButton);
		addDrawableChild(bountyButton);
		addDrawableChild(factionButton);
	}
	
	private void initializeFactionButtons()
	{
		float scaleX = 1.0f;
		float scaleY = 1.0f;
		
		sideButtons.clear();
		sideButtons.add(new SideButtonElement(backgroundPosX - 32, backgroundPosY + 10, "Faction Relations", RELATIONS_TEXTURE, Tab.RELATIONS, scaleX, scaleY));
		sideButtons.add(new SideButtonElement(backgroundPosX - 32, backgroundPosY + 40, "Faction Resources", RESOURCES_TEXTURE, Tab.RESOURCES, scaleX, scaleY));
		sideButtons.add(new SideButtonElement(backgroundPosX - 32, backgroundPosY + 70, "Faction Statistics", INFORMATION_TEXTURE, Tab.STATISTICS, scaleX, scaleY));
		
		if (!playerData.getProfession().equals("Leader"))
		{
			sideButtons.get(1).setToolTip("Faction Resources - must be leader to view");
			sideButtons.get(2).setToolTip("Faction Statistics  - must be leader to view");
			sideButtons.get(1).setActive(false);
			sideButtons.get(2).setActive(false);
		}
		
		if (sideButtonStates == null)
	    {
			sideButtonStates = new boolean[sideButtons.size()];
	    	sideButtonStates[0] = true;
	    }
		
		//initializeRelationsButtons();
		initializeResourcesButtons();
		//initializeStatisticsButtons();
	    loadSideButtonStates();
	}
	
	private void initializeResourcesButtons()
	{
		resourcesText1 = Text.literal("AVAILABLE RESOURCES").formatted(Formatting.BOLD);
	    resourcesText2 = Text.literal("(Town Hall / Warehouse / Barracks)");
	    
		float scale = 0.75f;
		checkboxes.clear();
	    checkboxes.add(new CheckboxElement(backgroundPosX + 13, backgroundPosY + 52, "All", scale));
	    checkboxes.add(new CheckboxElement(backgroundPosX + 13, backgroundPosY + 70, "Build", scale));
	    checkboxes.add(new CheckboxElement(backgroundPosX + 73, backgroundPosY + 52, "Tools", scale));
	    checkboxes.add(new CheckboxElement(backgroundPosX + 73, backgroundPosY + 70, "Combat", scale));
	    checkboxes.add(new CheckboxElement(backgroundPosX + 133, backgroundPosY + 52, "Food", scale));
	    checkboxes.add(new CheckboxElement(backgroundPosX + 133, backgroundPosY + 70, "Crops", scale));
	    checkboxes.add(new CheckboxElement(backgroundPosX + 193, backgroundPosY + 52, "Ores", scale));
	    checkboxes.add(new CheckboxElement(backgroundPosX + 193, backgroundPosY + 70, "Misc", scale));
	    
	    if (checkboxStates == null)
	    {
	    	checkboxStates = new boolean[checkboxes.size()];
	    	checkboxStates[0] = true;
	    }

	    loadCheckboxStates();
	    updateInventoryFilter();
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		this.renderBackground(context);
		rectTextures.clear();
		context.drawTexture(BACKGROUND_TEXTURE, backgroundPosX, backgroundPosY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
		playerButton.active = true;
		factionButton.active = true;
		bountyButton.active = true;
		
		switch (page)
		{
			case PLAYER:
				renderPlayerPage(context, mouseX, mouseY);
				break;
			case FACTION:
				renderFactionPage(context, mouseX, mouseY);
				break;
			case BOUNTIES:
				renderBountiesPage(context, mouseX, mouseY);
				break;
			default:
				Frontier.LOGGER.error("PlayerCardScreen - No page found!");
				break;
		}
		
		if (this.playerData.getFaction().equalsIgnoreCase("N/A"))
			factionButton.visible = false;
		
		super.render(context, mouseX, mouseY, delta);
	}

	private void renderPlayerPage(DrawContext context, int mouseX, int mouseY)
	{
		playerButton.active = false;

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		rectTextures.add(new TextureElement(NAMEPLATE_TEXTURE, (backgroundPosX + 5), (backgroundPosY + 8), 238, 36, 0, 0, 256, 256, null, 1.0f, 1.6225f));
		rectTextures.add(new TextureElement(WINDOW_BACKGROUND_TEXTURE, (backgroundPosX + 9), (backgroundPosY + 75), 196, 16, 2, 57, 256, 256, null, 1.16f, 8.4f));
		rectTextures.add(new TextureElement(WINDOW_TEXTURE, (backgroundPosX + 8), (backgroundPosY + 68), 252, 140, 0, 0, 256, 256, null, 0.923f, 1.03f));
		for (TextureElement element : rectTextures)
			element.drawRect(context);
		RenderSystem.disableBlend();
		
		textures.forEach(element ->
		{
			element.draw(context);
			if (element.isMouseOver(mouseX, mouseY))
				TextUtil.renderTooltip(context, this.textRenderer, element.getToolTip(), mouseX, mouseY);
		});
		
		if (skinTexture != null)
			context.drawTexture(skinTexture, backgroundPosX + 22, backgroundPosY + 26, 8, 8, 8, 8, 64, 64);

		TextUtil.drawText(context, this.textRenderer, nameText, backgroundPosX + 36, backgroundPosY + 26);
		TextUtil.drawText(context, textRenderer, renownText, backgroundPosX + 36, backgroundPosY + 39, new Color(255, 255, 255).getRGB(), true, true, 145, TextAlign.LEFT);
		
		TextUtil.drawText(context, textRenderer, professionText, backgroundPosX + 213, backgroundPosY + 26, new Color(255, 255, 255).getRGB(), true, true, 145, TextAlign.RIGHT);
		TextUtil.drawText(context, textRenderer, factionText, backgroundPosX + 213, backgroundPosY + 39, new Color(255, 255, 255).getRGB(), true, true, 145, TextAlign.RIGHT);
		
		TextUtil.drawText(context, this.textRenderer, regionText, backgroundPosX + 27, backgroundPosY + 92, new Color(255, 255, 255).getRGB(), true, true, 145, TextAlign.LEFT, mouseX, mouseY, "Current Region");
		TextUtil.drawText(context, textRenderer, territoryText, backgroundPosX + 27, backgroundPosY + 107, new Color(255, 255, 255).getRGB(), true, true, 145, TextAlign.LEFT, mouseX, mouseY, "Current Territory");
		
		TextUtil.drawText(context, textRenderer, directionText, backgroundPosX + 225, backgroundPosY + 92, new Color(255, 255, 255).getRGB(), true, true, 145, TextAlign.RIGHT, mouseX, mouseY, "Current Region Location");
		TextUtil.drawText(context, textRenderer, biomeNameText, backgroundPosX + 225, backgroundPosY + 107, new Color(255, 255, 255).getRGB(), true, true, 85, TextAlign.RIGHT, mouseX, mouseY, "Current Biome");

		if (biomeNameText.getString().length() >= 16)
			context.drawTexture(SEPARATOR_TEXTURE, backgroundPosX + 27, backgroundPosY + 130, 0, 0, 194, 2, 32, 2);
		else
			context.drawTexture(SEPARATOR_TEXTURE, backgroundPosX + 27, backgroundPosY + 122, 0, 0, 194, 2, 32, 2);
	}

	private void renderFactionPage(DrawContext context, int mouseX, int mouseY)
	{
		factionButton.active = false;
		
		switch (tab)
		{
			case RELATIONS:
				break;
			case RESOURCES:
				renderResourcesPage(context, mouseX, mouseY);
				break;
			case STATISTICS:
				break;
			default:
				break;
		}
		
		for (SideButtonElement sideButton : sideButtons)
		{
			sideButton.render(context, this.textRenderer);
			if (sideButton.isMouseOver(mouseX, mouseY))
				TextUtil.renderTooltip(context, this.textRenderer, sideButton.getToolTip(), mouseX, mouseY);
		}
	}
	
	private void renderResourcesPage(DrawContext context, int mouseX, int mouseY)
	{
		TextUtil.drawText(context, textRenderer, resourcesText1, backgroundPosX + 64, backgroundPosY + 20, new Color(255, 255, 255).getRGB(), true, true, 145, TextAlign.LEFT);
		TextUtil.drawText(context, textRenderer, resourcesText2, backgroundPosX + 33, backgroundPosY + 30, new Color(255, 255, 255).getRGB(), true, true, 200, TextAlign.LEFT);

		context.drawTexture(SEPARATOR_TEXTURE, (backgroundPosX + 10), (backgroundPosY + 90), 0, 0, 225, 2, 32, 2);
		
		rectTextures.add(new TextureElement(NAMEPLATE_TEXTURE, (backgroundPosX + 5), (backgroundPosY + 8), 238, 36, 0, 0, 256, 256, null, 1.0f, 1.15f));
		for (TextureElement element : rectTextures)
		{
			element.drawRect(context);
			if (element.isMouseOver(mouseX, mouseY))
				TextUtil.renderTooltip(context, this.textRenderer, element.getToolTip(), mouseX, mouseY);
		}

		for (CheckboxElement checkbox : checkboxes)
			checkbox.render(context, this.textRenderer);

		int x = backgroundPosX + 10;
		int y = backgroundPosY + 95;

		if (filteredInventory != null)
		{
			filteredInventory.sort((itemStack1, itemStack2) -> Integer.compare(itemStack2.getCount(), itemStack1.getCount()));

			int totalItems = filteredInventory.size();

			for (int i = scrollOffset * ITEMS_PER_ROW; i < Math.min(totalItems, (scrollOffset + MAX_VISIBLE_ROWS) * ITEMS_PER_ROW); i++)
			{
				ItemStack itemStack = filteredInventory.get(i);
				if (itemStack != null)
				{
					context.drawItem(itemStack, x, y);

					int count = Math.min(itemStack.getCount(), 999);
					String countString = String.valueOf(count);

					context.getMatrices().push();
					context.getMatrices().translate(0, 0, 200);
					context.drawText(this.textRenderer, countString, x + ITEM_SIZE - this.textRenderer.getWidth(countString), y + ITEM_SIZE - this.textRenderer.fontHeight, Colors.WHITE, true);
					context.getMatrices().pop();
				}
				x += ITEM_SIZE + 3;
				if ((i + 1) % ITEMS_PER_ROW == 0)
				{
					x = backgroundPosX + 10;
					y += ITEM_SIZE + 2;
				}
			}
		}
		
		renderScrollbar(context);
	}
	
	private void renderBountiesPage(DrawContext context, int mouseX, int mouseY)
	{
		bountyButton.active = false;
	}
	
	private void renderScrollbar(DrawContext context)
	{
		int totalItems = filteredInventory.size();
		int visibleItems = MAX_VISIBLE_ROWS * ITEMS_PER_ROW;
		int maxScroll = Math.max(0, (int) Math.ceil((double) totalItems / ITEMS_PER_ROW) - MAX_VISIBLE_ROWS);

		if (totalItems > visibleItems)
		{
			int scrollbarHeight = Math.max(SCROLLBAR_HEIGHT * visibleItems / totalItems, 10); // minimum scrollbar height
			int scrollbarX = backgroundPosX + 242;
			int scrollbarY = backgroundPosY + 95 + (SCROLLBAR_HEIGHT - scrollbarHeight) * scrollOffset / maxScroll;
			context.fill(scrollbarX, scrollbarY, scrollbarX + SCROLLBAR_WIDTH, scrollbarY + scrollbarHeight, new Color(145, 145, 145).getRGB());
		}
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
			String territory = SettlementManager.getSettlementTerritoryName(player.getBlockPos());
			if (SettlementManager.settlementExists(territory))
			{
				int playerReputation = playerData.getReputation(territory);
				territoryText = getFormattedTerritoryText(playerData, territory, playerReputation);
			}
			else
				territoryText = Text.literal(SettlementManager.getSettlementTerritoryName(player.getBlockPos())).formatted(Formatting.WHITE);
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
	
	private void saveSideButtonStates()
    {
        for (int i = 0; i < sideButtons.size(); i++)
            sideButtonStates[i] = sideButtons.get(i).isFocused();
    }

    private void loadSideButtonStates()
    {
        if (sideButtonStates != null && sideButtonStates.length == sideButtons.size())
            for (int i = 0; i < sideButtons.size(); i++)
            	sideButtons.get(i).setFocused(sideButtonStates[i]);
    }
    
    private void saveCheckboxStates()
    {
        for (int i = 0; i < checkboxes.size(); i++)
            checkboxStates[i] = checkboxes.get(i).isChecked();
    }

    private void loadCheckboxStates()
    {
        if (checkboxStates != null && checkboxStates.length == checkboxes.size())
            for (int i = 0; i < checkboxes.size(); i++)
                checkboxes.get(i).setChecked(checkboxStates[i]);
    }
    
    private void updateScrollbar()
    {
        int totalItems = filteredInventory.size();
        int maxScroll = Math.max(0, (int) Math.ceil((double) totalItems / ITEMS_PER_ROW) - MAX_VISIBLE_ROWS);
        scrollOffset = Math.min(scrollOffset, maxScroll);
    }
    
    @Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount)
	{
		int totalItems = filteredInventory.size();
		int maxScroll = Math.max(0, (int) Math.ceil((double) totalItems / ITEMS_PER_ROW) - MAX_VISIBLE_ROWS);

		if (amount > 0)
			scrollOffset = Math.max(0, scrollOffset - 1);
		else if (amount < 0)
			scrollOffset = Math.min(maxScroll, scrollOffset + 1);
		return super.mouseScrolled(mouseX, mouseY, amount);
	}
    
    @Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
    	for (SideButtonElement sideButton : sideButtons)
		{
			if (sideButton.isMouseOver((int) mouseX, (int) mouseY) && sideButton.isActive())
			{
				for (SideButtonElement btn : sideButtons)
					btn.setFocused(false);

				sideButton.setFocused(true);
				tab = sideButton.getTab();
				
				saveSideButtonStates();
				return true;
			}
		}
		
		if (tab == Tab.RESOURCES)
		{
			for (CheckboxElement checkbox : checkboxes)
			{
				if (checkbox.isMouseOver((int) mouseX, (int) mouseY))
				{
					checkbox.toggle();
					if (checkbox != checkboxes.get(0) && checkbox.isChecked())
						checkboxes.get(0).setChecked(false);

					if (checkbox == checkboxes.get(0) && checkboxes.get(0).isChecked())
						for (int i = 1; i < checkboxes.size(); i++)
							checkboxes.get(i).setChecked(false);

					updateInventoryFilter();
					saveCheckboxStates();
					return true;
				}
			}
		}
		
		return super.mouseClicked(mouseX, mouseY, button);
	}
    
    private void updateInventoryFilter()
	{
	    filteredInventory.clear();
	    boolean allSelected = checkboxes.get(0).isChecked();
	    
	    if (allSelected)
	    {
	        filteredInventory.addAll(structureInventory);
	        return;
	    }

	    Set<String> selectedCategories = new HashSet<>();
	    if (checkboxes.get(1).isChecked())
	        selectedCategories.add("BUILD");
	    if (checkboxes.get(2).isChecked())
	        selectedCategories.add("TOOLS");
	    if (checkboxes.get(3).isChecked())
	        selectedCategories.add("COMBAT");
	    if (checkboxes.get(4).isChecked())
	        selectedCategories.add("FOOD");
	    if (checkboxes.get(5).isChecked())
	        selectedCategories.add("CROPS");
	    if (checkboxes.get(6).isChecked())
	        selectedCategories.add("ORES");
	    if (checkboxes.get(7).isChecked())
	        selectedCategories.add("MISC");

	    for (ItemStack itemStack : structureInventory)
	        if (selectedCategories.contains(getCategory(itemStack)))
	            filteredInventory.add(itemStack);
	    
	    if (filteredInventory.size() <= MAX_VISIBLE_ROWS * ITEMS_PER_ROW)
	        scrollOffset = 0;
	    
	    updateScrollbar();
	}
	
	private String getCategory(ItemStack itemStack)
	{
		Item item = itemStack.getItem();
		
		if (item instanceof BlockItem)
		{
	        Block block = ((BlockItem) item).getBlock();
	        
	        if (block.getDefaultState().isIn(BlockTags.RAILS) || item.equals(Items.STRING))
	            return "TOOLS";
	        
	        if (itemStack.isIn(ItemTags.VILLAGER_PLANTABLE_SEEDS)
	         || item.equals(Items.BEETROOT_SEEDS)
	         || item.equals(Items.MELON_SEEDS)
	         || item.equals(Items.PUMPKIN_SEEDS)
	         || item.equals(Items.TORCHFLOWER_SEEDS)
	         || item.equals(Items.WHEAT_SEEDS)
	         || item.equals(Items.BONE_BLOCK)
	         || item.equals(Items.NETHER_SPROUTS)
	         || block instanceof CoralBlock
	         || block instanceof CoralBlockBlock
	         || block instanceof CoralFanBlock
	         || block instanceof CoralParentBlock
	         || block instanceof CoralWallFanBlock
	         || block instanceof DeadCoralBlock
	         || block instanceof DeadCoralFanBlock
	         || block instanceof DeadCoralWallFanBlock
	         || block instanceof GlowLichenBlock
	         || block instanceof MushroomBlock
	         || block instanceof MushroomPlantBlock
	         || block instanceof SnifferEggBlock
	         || block instanceof TurtleEggBlock
	         || block instanceof ChorusFlowerBlock
	         || block instanceof ChorusPlantBlock
	         || block instanceof FungusBlock
	         || block instanceof NetherWartBlock)
	            return "CROPS";
	        
	        if (block instanceof CakeBlock
	         || block instanceof CandleCakeBlock
	         || block instanceof MelonBlock
	         || block instanceof PumpkinBlock
	         || block instanceof SweetBerryBushBlock
	         || item.equals(Items.GLOW_BERRIES)
	         || item.equals(Items.CHORUS_FRUIT))
	            return "FOOD";
	        
	        if (block.getDefaultState().isIn(BlockTags.COAL_ORES)
	         || block.getDefaultState().isIn(BlockTags.COPPER_ORES)
	         || block.getDefaultState().isIn(BlockTags.IRON_ORES)
	         || block.getDefaultState().isIn(BlockTags.GOLD_ORES)
	         || block.getDefaultState().isIn(BlockTags.REDSTONE_ORES)
	         || block.getDefaultState().isIn(BlockTags.DIAMOND_ORES)
	         || block.getDefaultState().isIn(BlockTags.EMERALD_ORES)
	         || block.equals(Blocks.AMETHYST_BLOCK)
	         || block.equals(Blocks.AMETHYST_CLUSTER)
	         || block.equals(Blocks.BUDDING_AMETHYST)
	         || block.equals(Blocks.SMALL_AMETHYST_BUD)
	         || block.equals(Blocks.MEDIUM_AMETHYST_BUD)
	         || block.equals(Blocks.LARGE_AMETHYST_BUD)
	         || block.equals(Blocks.RAW_COPPER_BLOCK)
	         || block.equals(Blocks.RAW_IRON_BLOCK)
	         || block.equals(Blocks.RAW_GOLD_BLOCK)
	         || block.equals(Blocks.REDSTONE_BLOCK)
	         || block.equals(Blocks.NETHER_QUARTZ_ORE)
	         || item.equals(Items.REDSTONE))
	            return "ORES";
	        
	        return "BUILD";
	    }
		
	    if (itemStack.getItem() instanceof BlockItem)
	        return "BUILD";
	    
	    if (itemStack.getItem() instanceof ShovelItem 
	     || itemStack.getItem() instanceof AxeItem
	     || itemStack.getItem() instanceof PickaxeItem
	     || itemStack.getItem() instanceof FishingRodItem
	     || itemStack.getItem() instanceof OnAStickItem
	     || itemStack.getItem() instanceof HoeItem
	     || itemStack.getItem() instanceof ShearsItem
	     || itemStack.getItem() instanceof BucketItem
	     || itemStack.getItem() instanceof BrushItem
	     || itemStack.getItem() instanceof CompassItem
	     || itemStack.getItem() instanceof SpyglassItem
	     || itemStack.getItem() instanceof SaddleItem
	     || itemStack.getItem() instanceof EnderEyeItem
	     || itemStack.getItem() instanceof EnderPearlItem
	     || itemStack.getItem() instanceof FireworkRocketItem
	     || itemStack.getItem() instanceof FireworkStarItem
	     || itemStack.getItem() instanceof BoatItem
	     || itemStack.getItem() instanceof EmptyMapItem
	     || itemStack.getItem() instanceof FilledMapItem
	     || itemStack.getItem() instanceof FireChargeItem
	     || itemStack.getItem() instanceof FlintAndSteelItem
	     || itemStack.getItem() instanceof BookItem
	     || itemStack.getItem() instanceof EnchantedBookItem
	     || itemStack.getItem() instanceof KnowledgeBookItem
	     || itemStack.getItem() instanceof WritableBookItem
	     || itemStack.getItem() instanceof WrittenBookItem
	     || itemStack.getItem() instanceof ElytraItem
	     || itemStack.getItem() instanceof MusicDiscItem
	     || itemStack.getItem() instanceof GoatHornItem
	     || itemStack.getItem() instanceof MinecartItem
	     || itemStack.getItem() instanceof NameTagItem
	     || itemStack.getItem() instanceof LeadItem
	     || item.equals(Items.CLOCK)
	     || item.equals(Items.BONE_MEAL))
	        return "TOOLS";
	    
	    if (itemStack.getItem() instanceof SwordItem 
	     || itemStack.getItem() instanceof AxeItem
	     || itemStack.getItem() instanceof RangedWeaponItem
	     || itemStack.getItem() instanceof CrossbowItem
	     || itemStack.getItem() instanceof ArmorItem
	     || itemStack.getItem() instanceof TridentItem
	     || itemStack.getItem() instanceof ArrowItem
	     || itemStack.getItem() instanceof ShieldItem
	     || itemStack.getItem() instanceof HorseArmorItem
	     || itemStack.getItem() instanceof DyeableHorseArmorItem)
	        return "COMBAT";

	    if (item.isFood()
	     || item.equals(Items.GLISTERING_MELON_SLICE)
	     || item.equals(Items.MILK_BUCKET)
	     || item.equals(Items.POPPED_CHORUS_FRUIT))
	        return "FOOD";

	    if (item.equals(Items.BONE) || itemStack.getItem() instanceof EggItem)
	    	return "CROPS";
	    
	    if (item.equals(Items.COAL)
		 || item.equals(Items.CHARCOAL)
		 || item.equals(Items.COPPER_INGOT)
		 || item.equals(Items.IRON_INGOT)
		 || item.equals(Items.GOLD_INGOT)
		 || item.equals(Items.DIAMOND)
		 || item.equals(Items.EMERALD)
		 || item.equals(Items.RAW_COPPER)
		 || item.equals(Items.RAW_IRON)
		 || item.equals(Items.RAW_GOLD)
		 || item.equals(Items.AMETHYST_SHARD)
		 || item.equals(Items.QUARTZ))
	    	return "ORES";
	    
	    return "MISC";
	}

	@Override
	public boolean shouldPause()
	{
		return false;
	}
}