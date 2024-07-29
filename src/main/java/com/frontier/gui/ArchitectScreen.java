package com.frontier.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.frontier.PlayerData;
import com.frontier.entities.settler.ArchitectEntity;
import com.frontier.gui.util.CheckboxElement;
import com.frontier.gui.util.TextUtil;
import com.frontier.gui.util.TextUtil.TextAlign;
import com.frontier.gui.util.TextureElement;
import com.frontier.network.FrontierPacketsServer;
import com.frontier.settlements.Blueprint;
import com.frontier.util.FrontierUtil;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
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
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class ArchitectScreen extends Screen
{
	public static final int UI_OFFSET_X = 0;
    public static final int UI_OFFSET_Y = 72;
    
    private static final Identifier BACKGROUND_TEXTURE = new Identifier("minecraft", "textures/gui/demo_background.png");
    public static final int BACKGROUND_WIDTH = 256;
    public static final int BACKGROUND_HEIGHT = 338;
	
    private static final Identifier NAMEPLATE_TEXTURE = new Identifier("minecraft", "textures/gui/social_interactions.png");
    private static final Identifier SEPARATOR_TEXTURE = new Identifier("minecraft", "textures/gui/header_separator.png");
    private static final Identifier HANGINGSIGN_TEXTURE = new Identifier("minecraft", "textures/gui/hanging_signs/spruce.png");
    private static final Identifier EMERALD_TEXTURE = new Identifier("minecraft", "textures/item/emerald.png");    
    private static final Identifier INFO_TEXTURE = new Identifier("minecraft", "textures/gui/widgets.png");

    private static final Identifier TOWNHALL_TEXTURE = new Identifier("minecraft", "textures/item/bell.png");
    private static final Identifier WAREHOUSE_TEXTURE = new Identifier("minecraft", "textures/block/barrel_side.png");
    private static final Identifier HOUSE_TEXTURE = new Identifier("minecraft", "textures/block/oak_door_top.png");
    private static final Identifier ROAD_TEXTURE = new Identifier("minecraft", "textures/block/dirt_path_side.png");
    private static final Identifier BRIDGE_TEXTURE = new Identifier("minecraft", "textures/block/stripped_bamboo_block.png");
    
    private static final Identifier BARRACKS_TEXTURE = new Identifier("minecraft", "textures/block/fletching_table_front.png");
    private static final Identifier WATCHTOWER_TEXTURE = new Identifier("minecraft", "textures/block/redstone_lamp_on.png");
    private static final Identifier BOUNTYHALL_TEXTURE = new Identifier("minecraft", "textures/block/spawner.png");
    private static final Identifier WALL_TEXTURE = new Identifier("minecraft", "textures/block/stone_bricks.png");
    
    private static final Identifier FARM_TEXTURE = new Identifier("minecraft", "textures/block/wheat_stage6.png");
    private static final Identifier FISHERY_TEXTURE = new Identifier("minecraft", "textures/block/tube_coral.png");
    private static final Identifier LODGE_TEXTURE = new Identifier("minecraft", "textures/block/cave_vines_plant.png");
    private static final Identifier GROVE_TEXTURE = new Identifier("minecraft", "textures/block/oak_log_top.png");
    private static final Identifier MINE_TEXTURE = new Identifier("minecraft", "textures/block/gold_ore.png");
    
    private static final Identifier ALCHEMYLAB_TEXTURE = new Identifier("minecraft", "textures/block/brewing_stand_base.png");
    private static final Identifier ARCANUM_TEXTURE = new Identifier("minecraft", "textures/block/amethyst_block.png");
    private static final Identifier BLACKSMITH_TEXTURE = new Identifier("minecraft", "textures/block/blast_furnace_front.png");
    private static final Identifier CARTOGRAPHY_TEXTURE = new Identifier("minecraft", "textures/block/cartography_table_top.png");
    private static final Identifier FLETCHERY_TEXTURE = new Identifier("minecraft", "textures/block/fletching_table_top.png");
    private static final Identifier TANNERY_TEXTURE = new Identifier("minecraft", "textures/block/loom_front.png");
    
    private static final Identifier APIARY_TEXTURE = new Identifier("minecraft", "textures/block/bee_nest_front.png");
    private static final Identifier CHICKENCOOP_TEXTURE = new Identifier("minecraft", "textures/block/spruce_trapdoor.png");
    private static final Identifier COWBARN_TEXTURE = new Identifier("minecraft", "textures/block/composter_bottom.png");
    private static final Identifier PIGPEN_TEXTURE = new Identifier("minecraft", "textures/block/packed_mud.png");
    private static final Identifier SHEEPPASTURE_TEXTURE = new Identifier("minecraft", "textures/block/white_wool.png");
    private static final Identifier STABLE_TEXTURE = new Identifier("minecraft", "textures/block/hay_block_side.png");
    
    private static final Identifier BAKERY_TEXTURE = new Identifier("minecraft", "textures/block/cake_side.png");
    private static final Identifier ABATTOIR_TEXTURE = new Identifier("minecraft", "textures/block/smoker_front.png");
    private static final Identifier GREENGROCERY_TEXTURE = new Identifier("minecraft", "textures/block/azalea_leaves.png");
    private static final Identifier WOODSHOP_TEXTURE = new Identifier("minecraft", "textures/block/stripped_oak_log.png");
    private static final Identifier MASONRY_TEXTURE = new Identifier("minecraft", "textures/block/bricks.png");
    
    private static final Identifier MARKETPLACE_TEXTURE = new Identifier("minecraft", "textures/block/emerald_block.png");
    private static final Identifier TAVERN_TEXTURE = new Identifier("minecraft", "textures/block/jukebox_side.png");
    
    private static final Identifier CHURCH_TEXTURE = new Identifier("minecraft", "textures/block/glass.png");
    private static final Identifier LIBRARY_TEXTURE = new Identifier("minecraft", "textures/block/chiseled_bookshelf_occupied.png");
    private static final Identifier CEMETERY_TEXTURE = new Identifier("minecraft", "textures/block/iron_bars.png");
    private static final Identifier WELL_TEXTURE = new Identifier("minecraft", "textures/block/chain.png");
    private static final Identifier FOUNTAIN_TEXTURE = new Identifier("minecraft", "textures/block/dark_prismarine.png");
    
    private List<TextureElement> blueprintTextures = new ArrayList<>();
    private List<TextureElement> upgradeTextures = new ArrayList<>();
    
    private List<TextureElement> rectTextures = new ArrayList<>();
    
    private List<TextureElement> coreTextures = new ArrayList<>();
    private List<TextureElement> militaryTextures = new ArrayList<>();
    private List<TextureElement> laboringTextures = new ArrayList<>();
    private List<TextureElement> craftingTextures = new ArrayList<>();
    private List<TextureElement> ranchingTextures = new ArrayList<>();
    private List<TextureElement> artisanTextures = new ArrayList<>();
    private List<TextureElement> customsTextures = new ArrayList<>();
    private List<TextureElement> miscTextures = new ArrayList<>();
    
    private int backgroundPosX;
    private int backgroundPosY;
    
    public enum Page { BLUEPRINT, RESOURCES, UPGRADE, CORE, MILITARY, LABOR, CRAFT, RANCH, ARTISAN, CUSTOMS, MISC }
    private Page page;
    
    private Blueprint blueprint;
    
    private ButtonWidget buyButton;
    private ButtonWidget architectButton, blueprintPageButton, resourcesPageButton, upgradePageButton;
    private ButtonWidget coreButton, militiaButton, laboringButton, craftingButton, ranchingButton, artisanButton, customsButton, miscButton;
    private ButtonWidget barracksButton, watchTowerButton, bountyHallButton;
    private ButtonWidget townhallButton, warehouseButton, houseButton, roadButton, bridgeButton, wallButton;
    private ButtonWidget farmButton, fisheryButton, lodgeButton, groveButton, mineButton;
    private ButtonWidget alchemyLabButton, arcanumButton, blacksmithButton, cartographyButton, fletcheryButton, tanneryButton;
    private ButtonWidget apiaryButton, cowBarnButton, chickenCoopButton, sheepPastureButton, stableButton, pigPenButton;
    private ButtonWidget bakeryButton, abattoirButton, greengroceryButton, woodshopButton, masonryButton;
    private ButtonWidget marketplaceButton, tavernButton;
    private ButtonWidget churchButton, libraryButton, cemeteryButton, wellButton, fountainButton;
    
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
    
    private Text priceText;
    
    private Text titleText1;
    private Text titleText2;
    
    private Text blueprintText1;
    private Text blueprintText2;
    private Text blueprintText3;
    
    private Text resourcesText1;
    private Text resourcesText2;
    
    private Text upgradeText1;
    private Text upgradeText2;
    
    PlayerEntity player;
    ArchitectEntity architect;
    int playerEmeralds;
    
	public ArchitectScreen(List<ItemStack> structureInventory, ArchitectEntity architect)
	{
		super(Text.literal("Structure Management Screen"));
		this.page = Page.BLUEPRINT;
		this.player = MinecraftClient.getInstance().player;
		this.playerEmeralds = 0;
		this.structureInventory = structureInventory;
		this.architect = architect;
	}

	@Override
	protected void init()
	{
		backgroundPosX = ((this.width - BACKGROUND_WIDTH) / 2) + UI_OFFSET_X;
		backgroundPosY = ((this.height - BACKGROUND_HEIGHT) / 2) + UI_OFFSET_Y;

		priceText = Text.literal("0");
		
		updateButtons();
	}

	private void updateButtons()
	{
		this.clearChildren();
		initializeMainButtons();
		
		if (this.player != null)
			this.playerEmeralds = countEmeralds(this.player);
		
		switch (page)
		{
			case BLUEPRINT:
				setupBlueprintPage();
				break;
			case RESOURCES:
				setupResourcesPage();
				break;
			case UPGRADE:
				setupUpgradePage();
				break;
			case CORE:
				setupCorePage();
				break;
			case MILITARY:
				setupMilitaryPage();
				break;
			case LABOR:
				setupLaboringPage();
				break;
			case CRAFT:
				setupCraftingPage();
				break;
			case RANCH:
				setupRanchingPage();
				break;
			case ARTISAN:
				setupArtisanPage();
				break;
			case CUSTOMS:
				setupCustomsPage();
				break;
			case MISC:
				setupMiscPage();
				break;
			default:
				break;
		}

		if (blueprint == Blueprint.NONE)
			priceText = Text.literal("0");

		initializeBuyButton();
		
        addDrawableChild(buyButton);
        addDrawableChild(architectButton);
		addDrawableChild(blueprintPageButton);
		addDrawableChild(resourcesPageButton);
		addDrawableChild(upgradePageButton);
	}

	private void initializeMainButtons()
	{
		architectButton = ButtonWidget.builder(Text.literal(architect.getSettlerFirstName()), button ->
		{
			MinecraftClient.getInstance().setScreen(new SettlerCardScreen(architect));
		}).dimensions(backgroundPosX + 0, backgroundPosY - 25, 61, 20).build();
		architectButton.active = true;
		
		blueprintPageButton = ButtonWidget.builder(Text.literal("Blueprints"), button ->
		{
			page = Page.BLUEPRINT;
			updateButtons();
		}).dimensions(backgroundPosX + 62, backgroundPosY - 25, 61, 20).build();
		blueprintPageButton.active = true;
		
		resourcesPageButton = ButtonWidget.builder(Text.literal("Resources"), button ->
		{
			page = Page.RESOURCES;
			updateButtons();
		}).dimensions(backgroundPosX + 125, backgroundPosY - 25, 61, 20).build();
		resourcesPageButton.active = true;
		
		upgradePageButton = ButtonWidget.builder(Text.literal("Upgrade"), button ->
		{
			page = Page.UPGRADE;
			updateButtons();
		}).dimensions(backgroundPosX + 187, backgroundPosY - 25, 61, 20).build();
		upgradePageButton.active = true;
	}

	private void initializeResourcesPageButtons()
	{
		resourcesPageButton.active = false;
		
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
	
	private void initializeUpgradePageButtons()
	{
		upgradePageButton.active = false;
	}
	
	private void initializeBuyButton()
	{
        buyButton = ButtonWidget.builder(Text.literal("Buy"), button ->
        {
            PlayerData playerData = PlayerData.players.get(this.player.getUuid());
            if (this.player != null && playerData != null)
            {
                PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer()); // TODO: Pass building info
                passedData.writeInt(blueprint.getValue());
                passedData.writeEnumConstant(blueprint);
                ClientPlayNetworking.send(FrontierPacketsServer.BUY_BLUEPRINT_ID, passedData);
                MinecraftClient.getInstance().setScreen(null);
            }
        }).dimensions(backgroundPosX + 192, backgroundPosY + 63, 45, 20).build();
    }
	
	private void initializeJobCategoryButtons()
	{
		blueprintPageButton.active = false;
		coreButton = ButtonWidget.builder(Text.literal("Core"), button ->
		{
			page = Page.CORE;
			updateButtons();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 155, 51, 20).build();
		
		militiaButton = ButtonWidget.builder(Text.literal("Military"), button ->
		{
			page = Page.MILITARY;
			updateButtons();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 185, 51, 20).build();
		
		laboringButton = ButtonWidget.builder(Text.literal("Labor"), button ->
		{
			page = Page.LABOR;
			updateButtons();
		}).dimensions(backgroundPosX + 68, backgroundPosY + 155, 51, 20).build();
		
		craftingButton = ButtonWidget.builder(Text.literal("Craft"), button ->
		{
			page = Page.CRAFT;
			updateButtons();
		}).dimensions(backgroundPosX + 68, backgroundPosY + 185, 51, 20).build();
		
		ranchingButton = ButtonWidget.builder(Text.literal("Ranch"), button ->
		{
			page = Page.RANCH;
			updateButtons();
		}).dimensions(backgroundPosX + 126, backgroundPosY + 155, 51, 20).build();
		
		artisanButton = ButtonWidget.builder(Text.literal("Artisan"), button ->
		{
			page = Page.ARTISAN;
			updateButtons();
		}).dimensions(backgroundPosX + 126, backgroundPosY + 185, 51, 20).build();
		
		customsButton = ButtonWidget.builder(Text.literal("Customs"), button ->
		{
			page = Page.CUSTOMS;
			updateButtons();
		}).dimensions(backgroundPosX + 184, backgroundPosY + 155, 51, 20).build();
		
		miscButton = ButtonWidget.builder(Text.literal("Misc"), button ->
		{
			page = Page.MISC;
			updateButtons();
		}).dimensions(backgroundPosX + 184, backgroundPosY + 185, 51, 20).build();
		
		addDrawableChild(coreButton);
		addDrawableChild(militiaButton);
		addDrawableChild(laboringButton);
		addDrawableChild(craftingButton);
		addDrawableChild(ranchingButton);
		addDrawableChild(artisanButton);
		addDrawableChild(customsButton);
		addDrawableChild(miscButton);
	}
	
	private void setupBlueprintPage()
	{
		blueprintText1 = Text.literal("PURCHASE BLUEPRINTS").formatted(Formatting.BOLD);
		blueprintText2 = Text.literal("Use the blueprint by right-clicking a desired location for your building. The Architect will use your settlement resources to construct and maintain it.");
		blueprintText3 = Text.literal("You can also place the blueprint in an item frame above a door or gate of your own custom structure to designate it.");
		blueprint = Blueprint.NONE;
		initializeJobCategoryButtons();
	}
	
	private void setupResourcesPage()
	{
		resourcesText1 = Text.literal("AVAILABLE RESOURCES").formatted(Formatting.BOLD);
	    resourcesText2 = Text.literal("(Town Hall / Warehouse / Barracks)");
	    blueprint = Blueprint.NONE;
	    initializeResourcesPageButtons();
	}
	
	private void setupUpgradePage()
	{
		upgradeText1 = Text.literal("UPGRADE BUILDING").formatted(Formatting.BOLD);
		upgradeText2 = Text.literal("Each building has 5 upgradeable tiers (0-4). Upgrades cannot surpass the tier of the Town Hall by more than 1. Buildings cannot be upgraded if they require repairs.");
		blueprint = Blueprint.NONE;
		initializeUpgradePageButtons();
	}

	private void setupCorePage()
	{
		titleText1 = Text.literal("These structures make up the foundation of your settlement.");
		titleText2 = Text.literal("Your architect resides in your townhall and the warehouse is where your resources are stored.");
		
		townhallButton = ButtonWidget.builder(Text.literal("Town Hall"), button ->
		{
			blueprint = Blueprint.TOWNHALL;
			priceText = Blueprint.TOWNHALL.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		warehouseButton = ButtonWidget.builder(Text.literal("Warehouse"), button ->
		{
			blueprint = Blueprint.WAREHOUSE;
			priceText = Blueprint.WAREHOUSE.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		houseButton = ButtonWidget.builder(Text.literal("House"), button ->
		{
			blueprint = Blueprint.HOUSE;
			priceText = Blueprint.HOUSE.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		roadButton = ButtonWidget.builder(Text.literal("Road"), button ->
		{
			blueprint = Blueprint.ROAD;
			priceText = Blueprint.ROAD.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 155, 65, 20).build();
		
		bridgeButton = ButtonWidget.builder(Text.literal("Bridge"), button ->
		{
			blueprint = Blueprint.BRIDGE;
			priceText = Blueprint.BRIDGE.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 155, 65, 20).build();
		
		addDrawableChild(townhallButton);
		addDrawableChild(warehouseButton);
		addDrawableChild(houseButton);
		addDrawableChild(roadButton);
		addDrawableChild(bridgeButton);
	}

	private void setupMilitaryPage()
	{
		titleText1 = Text.literal("Both the defense and offense of your settlement rely upon your military structures.");
		titleText2 = Text.literal("Make sure the morale of your troops are high, lest they begin to forego their duties.");

		barracksButton = ButtonWidget.builder(Text.literal("Barracks"), button ->
		{
			blueprint = Blueprint.BARRACKS;
			priceText = Blueprint.BARRACKS.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		watchTowerButton = ButtonWidget.builder(Text.literal("Watch Tower"), button ->
		{
			blueprint = Blueprint.WATCHTOWER;
			priceText = Blueprint.WATCHTOWER.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		bountyHallButton = ButtonWidget.builder(Text.literal("Bounty Hall"), button ->
		{
			blueprint = Blueprint.BOUNTYHALL;
			priceText = Blueprint.BOUNTYHALL.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		wallButton = ButtonWidget.builder(Text.literal("Wall"), button ->
		{
			blueprint = Blueprint.WALL;
			priceText = Blueprint.WALL.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 155, 65, 20).build();

		addDrawableChild(barracksButton);
		addDrawableChild(watchTowerButton);
		addDrawableChild(bountyHallButton);
		addDrawableChild(wallButton);
	}
	
	private void setupLaboringPage()
	{
		titleText1 = Text.literal("The backbone of your settlement relies on your labor structures production of resources.");
		titleText2 = Text.literal("Farming, fishing, mining, foraging, and woodcutting stations are all a boon to your settlement.");
		
		farmButton = ButtonWidget.builder(Text.literal("Farm"), button ->
		{
			blueprint = Blueprint.FARM;
			priceText = Blueprint.FARM.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		groveButton = ButtonWidget.builder(Text.literal("Grove"), button ->
		{
			blueprint = Blueprint.GROVE;
			priceText = Blueprint.GROVE.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		mineButton = ButtonWidget.builder(Text.literal("Mine"), button ->
		{
			blueprint = Blueprint.MINE;
			priceText = Blueprint.MINE.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		fisheryButton = ButtonWidget.builder(Text.literal("Fishery"), button ->
		{
			blueprint = Blueprint.FISHERY;
			priceText = Blueprint.FISHERY.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 155, 65, 20).build();
		
		lodgeButton = ButtonWidget.builder(Text.literal("Lodge"), button ->
		{
			blueprint = Blueprint.LODGE;
			priceText = Blueprint.LODGE.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 155, 65, 20).build();
		
		addDrawableChild(farmButton);
		addDrawableChild(fisheryButton);
		addDrawableChild(lodgeButton);
		addDrawableChild(groveButton);
		addDrawableChild(mineButton);
	}
	
	private void setupCraftingPage()
	{
		titleText1 = Text.literal("These shops and outlets are responsible for producing usable goods in your settlement.");
		titleText2 = Text.literal("They will provide you and outsiders with tools, weapons, potions, and more.");
		
		alchemyLabButton = ButtonWidget.builder(Text.literal("Alchemy Lab"), button ->
		{
			blueprint = Blueprint.ALCHEMYLAB;
			priceText = Blueprint.ALCHEMYLAB.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		arcanumButton = ButtonWidget.builder(Text.literal("Arcanum"), button ->
		{
			blueprint = Blueprint.ARCANUM;
			priceText = Blueprint.ARCANUM.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		blacksmithButton = ButtonWidget.builder(Text.literal("Blacksmith"), button ->
		{
			blueprint = Blueprint.BLACKSMITH;
			priceText = Blueprint.BLACKSMITH.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		cartographyButton = ButtonWidget.builder(Text.literal("Cartography"), button ->
		{
			blueprint = Blueprint.CARTOGRAPHY;
			priceText = Blueprint.CARTOGRAPHY.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 155, 65, 20).build();
		
		fletcheryButton = ButtonWidget.builder(Text.literal("Fletchery"), button ->
		{
			blueprint = Blueprint.FLETCHERY;
			priceText = Blueprint.FLETCHERY.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 155, 65, 20).build();

		tanneryButton = ButtonWidget.builder(Text.literal("Tannery"), button ->
		{
			blueprint = Blueprint.TANNERY;
			priceText = Blueprint.TANNERY.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 155, 65, 20).build();
		
		addDrawableChild(alchemyLabButton);
		addDrawableChild(arcanumButton);
		addDrawableChild(blacksmithButton);
		addDrawableChild(cartographyButton);
		addDrawableChild(fletcheryButton);
		addDrawableChild(tanneryButton);
	}
	
	private void setupRanchingPage()
	{
		titleText1 = Text.literal("Ranching structures will produce all your meat, honey, hide, and wool.");
		titleText2 = Text.literal("Your stable can house horses, donkeys, mules, camels, and wolves to your cause and sell to visitors.");
		
		apiaryButton = ButtonWidget.builder(Text.literal("Apiary"), button ->
		{
			blueprint = Blueprint.APIARY;
			priceText = Blueprint.APIARY.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		cowBarnButton = ButtonWidget.builder(Text.literal("Cow Barn"), button ->
		{
			blueprint = Blueprint.COWBARN;
			priceText = Blueprint.COWBARN.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		chickenCoopButton = ButtonWidget.builder(Text.literal("Chicken Coop"), button ->
		{
			blueprint = Blueprint.CHICKENCOOP;
			priceText = Blueprint.CHICKENCOOP.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		sheepPastureButton = ButtonWidget.builder(Text.literal("Sheep Pasture"), button ->
		{
			blueprint = Blueprint.SHEEPPASTURE;
			priceText = Blueprint.SHEEPPASTURE.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 155, 65, 20).build();
		
		stableButton = ButtonWidget.builder(Text.literal("Stable"), button ->
		{
			blueprint = Blueprint.STABLE;
			priceText = Blueprint.STABLE.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 155, 65, 20).build();
		
		pigPenButton = ButtonWidget.builder(Text.literal("Pig Pen"), button ->
		{
			blueprint = Blueprint.PIGPEN;
			priceText = Blueprint.PIGPEN.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 155, 65, 20).build();
		
		addDrawableChild(apiaryButton);
		addDrawableChild(cowBarnButton);
		addDrawableChild(chickenCoopButton);
		addDrawableChild(sheepPastureButton);
		addDrawableChild(stableButton);
		addDrawableChild(pigPenButton);
	}
	
	private  void setupArtisanPage()
	{
		titleText1 = Text.literal("Artisanal shops will provide fresh produce, food, and lavish building materials.");
		titleText2 = Text.literal("Everyone in your settlement needs to eat, why not fine dine within extravagant halls?");
		
		bakeryButton = ButtonWidget.builder(Text.literal("Bakery"), button ->
		{
			blueprint = Blueprint.BAKERY;
			priceText = Blueprint.BAKERY.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		abattoirButton = ButtonWidget.builder(Text.literal("Abattoir"), button ->
		{
			blueprint = Blueprint.ABATTOIR;
			priceText = Blueprint.ABATTOIR.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		greengroceryButton = ButtonWidget.builder(Text.literal("Greengrocery"), button ->
		{
			blueprint = Blueprint.GREENGROCERY;
			priceText = Blueprint.GREENGROCERY.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		woodshopButton = ButtonWidget.builder(Text.literal("Woodshop"), button ->
		{
			blueprint = Blueprint.WOODSHOP;
			priceText = Blueprint.WOODSHOP.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 155, 65, 20).build();
		
		masonryButton = ButtonWidget.builder(Text.literal("Masonry"), button ->
		{
			blueprint = Blueprint.MASONRY;
			priceText = Blueprint.MASONRY.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 155, 65, 20).build();
		
		addDrawableChild(bakeryButton);
		addDrawableChild(abattoirButton);
		addDrawableChild(greengroceryButton);
		addDrawableChild(woodshopButton);
		addDrawableChild(masonryButton);
	}
	
	private  void setupCustomsPage()
	{
		titleText1 = Text.literal("These structures will handle the visitor affairs of your settlement.");
		titleText2 = Text.literal("Expect adventurers, merchants, and the like to sleep and conduct business at taverns and the market.");
		
		marketplaceButton = ButtonWidget.builder(Text.literal("Marketplace"), button ->
		{
			blueprint = Blueprint.MARKETPLACE;
			priceText = Blueprint.MARKETPLACE.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		tavernButton = ButtonWidget.builder(Text.literal("Tavern"), button ->
		{
			blueprint = Blueprint.TAVERN;
			priceText = Blueprint.TAVERN.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		addDrawableChild(marketplaceButton);
		addDrawableChild(tavernButton);
	}
	
	private  void setupMiscPage()
	{
		titleText1 = Text.literal("These are unnecessary but ultimately useful structures you can add to your settlement.");
		titleText2 = Text.literal("Priests stay in the church, the library increases your settlers intellect, and the cemetery houses your dead.");
		
		churchButton = ButtonWidget.builder(Text.literal("Church"), button ->
		{
			blueprint = Blueprint.CHURCH;
			priceText = Blueprint.CHURCH.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		cemeteryButton = ButtonWidget.builder(Text.literal("Cemetery"), button ->
		{
			blueprint = Blueprint.CEMETERY;
			priceText = Blueprint.CEMETERY.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		libraryButton = ButtonWidget.builder(Text.literal("Library"), button ->
		{
			blueprint = Blueprint.LIBRARY;
			priceText = Blueprint.LIBRARY.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		wellButton = ButtonWidget.builder(Text.literal("Well"), button ->
		{
			blueprint = Blueprint.WELL;
			priceText = Blueprint.WELL.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 155, 65, 20).build();
		
		fountainButton = ButtonWidget.builder(Text.literal("Fountain"), button ->
		{
			blueprint = Blueprint.FOUNTAIN;
			priceText = Blueprint.FOUNTAIN.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 155, 65, 20).build();
		
		addDrawableChild(churchButton);
		addDrawableChild(libraryButton);
		addDrawableChild(cemeteryButton);
		addDrawableChild(wellButton);
		addDrawableChild(fountainButton);
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		resetTexturesArrays();
		
		if (page == Page.BLUEPRINT || page == Page.RESOURCES || page == Page.UPGRADE)
			buyButton.visible = false;
		else
			buyButton.visible = true;

		if (blueprint != Blueprint.NONE && this.playerEmeralds >= blueprint.getValue())
			buyButton.active = true;
		else
			buyButton.active = false;
		
		if (!FrontierUtil.hasEnoughFreeSlots(this.player.getInventory(), 1))
		{
			buyButton.active = false;
			buyButton.setTooltip(Tooltip.of(Text.literal("Not enough inventory space!")));
		}

	    this.renderBackground(context);
	    context.drawTexture(BACKGROUND_TEXTURE, backgroundPosX, backgroundPosY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
	    super.render(context, mouseX, mouseY, delta);

	    if (page != Page.BLUEPRINT && page != Page.RESOURCES && page != Page.UPGRADE)
	    {
	        TextUtil.drawText(context, textRenderer, titleText1, backgroundPosX + 12, backgroundPosY + 16, new Color(255, 255, 255).getRGB(), true, true, 145, TextAlign.LEFT);
			TextUtil.drawText(context, textRenderer, titleText2, backgroundPosX + 12, backgroundPosY + 62, new Color(255, 255, 255).getRGB(), true, true, 145, TextAlign.LEFT);
	        
	        context.drawTexture(SEPARATOR_TEXTURE, (backgroundPosX + 10), (backgroundPosY + 110), 0, 0, 225, 2, 32, 2);
	        context.drawTexture(HANGINGSIGN_TEXTURE, (backgroundPosX + 175), (backgroundPosY + 16), 0, 0, 55, 32, 54, 32);
	        context.drawTexture(EMERALD_TEXTURE, (backgroundPosX + 207), (backgroundPosY + 30), 0, 0, 16, 16, 16, 16);
	        
	        rectTextures.add(new TextureElement(INFO_TEXTURE, (backgroundPosX + 166), (backgroundPosY + 62), 22, 22, 1, 23, 256, 256, null, 1.0f, 1.0f));
	        for (TextureElement element : rectTextures)
			{
				element.drawRect(context);
				if (element.isMouseOver(mouseX, mouseY))
					TextUtil.renderTooltip(context, this.textRenderer, element.getToolTip(), mouseX, mouseY);
			}
	        
	        drawTextureForBuild(context, mouseX, mouseY, blueprint);
	    }
	    
	    switch (page)
	    {
	        case BLUEPRINT:
	            renderBlueprintPage(context, mouseX, mouseY);
	            break;
	        case RESOURCES:
	            renderResourcesPage(context, mouseX, mouseY);
	            break;
	        case UPGRADE:
	            renderUpgradePage(context, mouseX, mouseY);
	            break;
	        case CORE:
	            renderCorePage(context, mouseX, mouseY);
	            break;
	        case MILITARY:
	            renderMilitaryPage(context, mouseX, mouseY);
	            break;
	        case LABOR:
	            renderLaboringPage(context, mouseX, mouseY);
	            break;
	        case CRAFT:
	            renderCraftingPage(context, mouseX, mouseY);
	            break;
	        case RANCH:
	            renderRanchingPage(context, mouseX, mouseY);
	            break;
	        case ARTISAN:
	            renderArtisanPage(context, mouseX, mouseY);
	            break;
	        case CUSTOMS:
	            renderCustomsPage(context, mouseX, mouseY);
	            break;
	        case MISC:
	            renderMiscPage(context, mouseX, mouseY);
	            break;
	    }
	}

	private void renderBlueprintPage(DrawContext context, int mouseX, int mouseY)
	{
		TextUtil.drawText(context, textRenderer, blueprintText1, backgroundPosX + 64, backgroundPosY + 22, new Color(255, 255, 255).getRGB(), true, true, 145, TextAlign.LEFT);
		TextUtil.drawText(context, textRenderer, blueprintText2, backgroundPosX + 125, backgroundPosY + 50, new Color(255, 255, 255).getRGB(), true, true, 220, TextAlign.CENTER);
		TextUtil.drawText(context, textRenderer, blueprintText3, backgroundPosX + 125, backgroundPosY + 97, new Color(255, 255, 255).getRGB(), true, true, 220, TextAlign.CENTER);
		
		context.drawTexture(SEPARATOR_TEXTURE, (backgroundPosX + 10), (backgroundPosY + 137), 0, 0, 225, 2, 32, 2);

		rectTextures.add(new TextureElement(NAMEPLATE_TEXTURE, (backgroundPosX + 5), (backgroundPosY + 8), 238, 36, 0, 0, 256, 256, null, 1.0f, 1.0f));
		for (TextureElement element : rectTextures)
		{
			element.drawRect(context);
			if (element.isMouseOver(mouseX, mouseY))
				TextUtil.renderTooltip(context, this.textRenderer, element.getToolTip(), mouseX, mouseY);
		}
		
		for (TextureElement element : blueprintTextures)
		{
			element.draw(context);
			if (element.isMouseOver(mouseX, mouseY))
				TextUtil.renderTooltip(context, this.textRenderer, element.getToolTip(), mouseX, mouseY);
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
	
	private void renderUpgradePage(DrawContext context, int mouseX, int mouseY)
	{
		TextUtil.drawText(context, this.textRenderer, upgradeText1, backgroundPosX + 76, backgroundPosY + 22, new Color(255, 255, 255).getRGB(), true, true, 145, TextAlign.LEFT);
		TextUtil.drawText(context, this.textRenderer, upgradeText2, backgroundPosX + 125, backgroundPosY + 50, new Color(255, 255, 255).getRGB(), true, true, 225, TextAlign.CENTER);
		
		context.drawTexture(SEPARATOR_TEXTURE, (backgroundPosX + 10), (backgroundPosY + 95), 0, 0, 225, 2, 32, 2);

        for (TextureElement element : rectTextures)
		{
			element.drawRect(context);
			if (element.isMouseOver(mouseX, mouseY))
				TextUtil.renderTooltip(context, this.textRenderer, element.getToolTip(), mouseX, mouseY);
		}
        
        rectTextures.add(new TextureElement(NAMEPLATE_TEXTURE, (backgroundPosX + 5), (backgroundPosY + 8), 238, 36, 0, 0, 256, 256, null, 1.0f, 1.0f));
		for (TextureElement element : rectTextures)
		{
			element.drawRect(context);
			if (element.isMouseOver(mouseX, mouseY))
				TextUtil.renderTooltip(context, this.textRenderer, element.getToolTip(), mouseX, mouseY);
		}
		
		for (TextureElement element : upgradeTextures)
		{
			element.draw(context);
			if (element.isMouseOver(mouseX, mouseY))
				TextUtil.renderTooltip(context, this.textRenderer, element.getToolTip(), mouseX, mouseY);
		}
	}

	private void renderCorePage(DrawContext context, int mouseX, int mouseY)
	{
		townhallButton.active = (blueprint != Blueprint.TOWNHALL);
		warehouseButton.active = (blueprint != Blueprint.WAREHOUSE);
		houseButton.active = (blueprint != Blueprint.HOUSE);
		roadButton.active = (blueprint != Blueprint.ROAD);
		bridgeButton.active = (blueprint != Blueprint.BRIDGE);
		
		if (townhallButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.TOWNHALL);
		else if (warehouseButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.WAREHOUSE);
		else if (houseButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.HOUSE);
		else if (roadButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.ROAD);
		else if (bridgeButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.BRIDGE);
		else
		    priceText = blueprint.getText();

		for (TextureElement element : coreTextures)
		{
			element.draw(context);
			if (element.isMouseOver(mouseX, mouseY))
				TextUtil.renderTooltip(context, this.textRenderer, element.getToolTip(), mouseX, mouseY);
		}

		drawPriceText(context);
	}

	private void renderMilitaryPage(DrawContext context, int mouseX, int mouseY)
	{
		barracksButton.active = (blueprint != Blueprint.BARRACKS);
		watchTowerButton.active = (blueprint != Blueprint.WATCHTOWER);
		bountyHallButton.active = (blueprint != Blueprint.BOUNTYHALL);
		wallButton.active = (blueprint != Blueprint.WALL);

		if (barracksButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.BARRACKS);
		else if (watchTowerButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.WATCHTOWER);
		else if (bountyHallButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.BOUNTYHALL);
		else if (wallButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.WALL);
		else
			priceText = blueprint.getText();

		for (TextureElement element : militaryTextures)
		{
			element.draw(context);
			if (element.isMouseOver(mouseX, mouseY))
				TextUtil.renderTooltip(context, this.textRenderer, element.getToolTip(), mouseX, mouseY);
		}

		drawPriceText(context);
	}

    private void renderLaboringPage(DrawContext context, int mouseX, int mouseY)
    {
		farmButton.active = (blueprint != Blueprint.FARM);
		fisheryButton.active = (blueprint != Blueprint.FISHERY);
		lodgeButton.active = (blueprint != Blueprint.LODGE);
		groveButton.active = (blueprint != Blueprint.GROVE);
		mineButton.active = (blueprint != Blueprint.MINE);

		if (farmButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.FARM);
		else if (fisheryButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.FISHERY);
		else if (lodgeButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.LODGE);
		else if (groveButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.GROVE);
		else if (mineButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.MINE);
		else
			priceText = blueprint.getText();

		for (TextureElement element : laboringTextures)
		{
			element.draw(context);
			if (element.isMouseOver(mouseX, mouseY))
				TextUtil.renderTooltip(context, this.textRenderer, element.getToolTip(), mouseX, mouseY);
		}

		drawPriceText(context);
    }

    private void renderCraftingPage(DrawContext context, int mouseX, int mouseY)
    {
		alchemyLabButton.active = (blueprint != Blueprint.ALCHEMYLAB);
		arcanumButton.active = (blueprint != Blueprint.ARCANUM);
		blacksmithButton.active = (blueprint != Blueprint.BLACKSMITH);
		cartographyButton.active = (blueprint != Blueprint.CARTOGRAPHY);
		fletcheryButton.active = (blueprint != Blueprint.FLETCHERY);
		tanneryButton.active = (blueprint != Blueprint.TANNERY);

		if (alchemyLabButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.ALCHEMYLAB);
		else if (arcanumButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.ARCANUM);
		else if (blacksmithButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.BLACKSMITH);
		else if (cartographyButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.CARTOGRAPHY);
		else if (fletcheryButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.FLETCHERY);
		else if (tanneryButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.TANNERY);
		else
			priceText = blueprint.getText();

		for (TextureElement element : craftingTextures)
		{
			element.draw(context);
			if (element.isMouseOver(mouseX, mouseY))
				TextUtil.renderTooltip(context, this.textRenderer, element.getToolTip(), mouseX, mouseY);
		}

		drawPriceText(context);
    }

    private void renderRanchingPage(DrawContext context, int mouseX, int mouseY)
    {
		apiaryButton.active = (blueprint != Blueprint.APIARY);
		cowBarnButton.active = (blueprint != Blueprint.COWBARN);
		chickenCoopButton.active = (blueprint != Blueprint.CHICKENCOOP);
		sheepPastureButton.active = (blueprint != Blueprint.SHEEPPASTURE);
		stableButton.active = (blueprint != Blueprint.STABLE);
		pigPenButton.active = (blueprint != Blueprint.PIGPEN);
		
		if (apiaryButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.APIARY);
		else if (cowBarnButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.COWBARN);
		else if (chickenCoopButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.CHICKENCOOP);
		else if (sheepPastureButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.SHEEPPASTURE);
		else if (stableButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.STABLE);
		else if (pigPenButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.PIGPEN);
		else
			priceText = blueprint.getText();

		for (TextureElement element : ranchingTextures)
		{
			element.draw(context);
			if (element.isMouseOver(mouseX, mouseY))
				TextUtil.renderTooltip(context, this.textRenderer, element.getToolTip(), mouseX, mouseY);
		}

		drawPriceText(context);
    }

    private void renderArtisanPage(DrawContext context, int mouseX, int mouseY)
    {
		bakeryButton.active = (blueprint != Blueprint.BAKERY);
		abattoirButton.active = (blueprint != Blueprint.ABATTOIR);
		greengroceryButton.active = (blueprint != Blueprint.GREENGROCERY);
		woodshopButton.active = (blueprint != Blueprint.WOODSHOP);
		masonryButton.active = (blueprint != Blueprint.MASONRY);

		if (bakeryButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.BAKERY);
		else if (abattoirButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.ABATTOIR);
		else if (greengroceryButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.GREENGROCERY);
		else if (woodshopButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.WOODSHOP);
		else if (masonryButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.MASONRY);
		else
			priceText = blueprint.getText();

		for (TextureElement element : artisanTextures)
		{
			element.draw(context);
			if (element.isMouseOver(mouseX, mouseY))
				TextUtil.renderTooltip(context, this.textRenderer, element.getToolTip(), mouseX, mouseY);
		}

		drawPriceText(context);
    }
    
    private void renderCustomsPage(DrawContext context, int mouseX, int mouseY)
    {
		marketplaceButton.active = (blueprint != Blueprint.MARKETPLACE);
		tavernButton.active = (blueprint != Blueprint.TAVERN);

		if (marketplaceButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.MARKETPLACE);
		else if (tavernButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.TAVERN);
		else
			priceText = blueprint.getText();

		for (TextureElement element : customsTextures)
		{
			element.draw(context);
			if (element.isMouseOver(mouseX, mouseY))
				TextUtil.renderTooltip(context, this.textRenderer, element.getToolTip(), mouseX, mouseY);
		}

		drawPriceText(context);
    }
    
    private void renderMiscPage(DrawContext context, int mouseX, int mouseY)
    {
		churchButton.active = (blueprint != Blueprint.CHURCH);
		libraryButton.active = (blueprint != Blueprint.LIBRARY);
		cemeteryButton.active = (blueprint != Blueprint.CEMETERY);
		wellButton.active = (blueprint != Blueprint.WELL);
		fountainButton.active = (blueprint != Blueprint.FOUNTAIN);

		if (churchButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.CHURCH);
		else if (libraryButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.LIBRARY);
		else if (cemeteryButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.CEMETERY);
		else if (wellButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.WELL);
		else if (fountainButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Blueprint.FOUNTAIN);
		else
			priceText = blueprint.getText();

		for (TextureElement element : miscTextures)
		{
			element.draw(context);
			if (element.isMouseOver(mouseX, mouseY))
				TextUtil.renderTooltip(context, this.textRenderer, element.getToolTip(), mouseX, mouseY);
		}

		drawPriceText(context);
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
    
    private  void drawPriceText(DrawContext context)
    {
    	if (this.playerEmeralds < blueprint.getValue())
			TextUtil.drawText(context, textRenderer, priceText, backgroundPosX + 201, backgroundPosY + 34, new Color(235, 50, 30).getRGB(), true, true, 200, TextAlign.RIGHT);
		else
			TextUtil.drawText(context, textRenderer, priceText, backgroundPosX + 201, backgroundPosY + 34, new Color(255, 255, 255).getRGB(), true, true, 200, TextAlign.RIGHT);
    }
    
    private void drawTextureForBuild(DrawContext context, int mouseX, int mouseY, Blueprint build)
    {
        TextureElement textureElement = null;
        int posX = 171;
        int posY = 67;
        
        switch (build)
        {
            case TOWNHALL:
                textureElement = new TextureElement(TOWNHALL_TEXTURE, (backgroundPosX + 169), (backgroundPosY + 66), 16, 16, "Uses settlement supplies to construct buildings", 1.0f, 1.0f);
                break;
            case WAREHOUSE:
                textureElement = new TextureElement(WAREHOUSE_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Stores all resources for your settlement to utilize", 0.75f, 0.75f);
                break;
            case HOUSE:
                textureElement = new TextureElement(HOUSE_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "The required living space for your settlers", 0.75f, 0.75f);
                break;
            case ROAD:
                textureElement = new TextureElement(ROAD_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Connects you to other settlements and creates traffic", 0.75f, 0.75f);
                break;
            case BRIDGE:
                textureElement = new TextureElement(BRIDGE_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Connects roads that are divided by empty space", 0.75f, 0.75f);
                break;
            case BARRACKS:
                textureElement = new TextureElement(BARRACKS_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Houses your military units and gives them benefits", 0.75f, 0.75f);
                break;
            case WATCHTOWER:
                textureElement = new TextureElement(WATCHTOWER_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Increases faction territory and security", 0.75f, 0.75f);
                break;
            case BOUNTYHALL:
                textureElement = new TextureElement(BOUNTYHALL_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Gives bounties that anyone can attempt to fulfill", 0.75f, 0.75f);
                break;
            case WALL:
                textureElement = new TextureElement(WALL_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Provides security for your settlement", 0.75f, 0.75f);
                break;
            case FARM:
                textureElement = new TextureElement(FARM_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Designated area for farmers to work", 0.75f, 0.75f);
                break;
            case GROVE:
                textureElement = new TextureElement(GROVE_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Designated area for lumberjacks to work", 0.75f, 0.75f);
                break;
            case MINE:
                textureElement = new TextureElement(MINE_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Designated mine for miners to work", 0.75f, 0.75f);
                break;
            case FISHERY:
                textureElement = new TextureElement(FISHERY_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Designated water for a fisher to work", 0.75f, 0.75f);
                break;
            case LODGE:
                textureElement = new TextureElement(LODGE_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Lodge for foragers to bring their findings", 0.75f, 0.75f);
                break;
            case ALCHEMYLAB:
                textureElement = new TextureElement(ALCHEMYLAB_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Shop that sells alchemic potions and items", 0.75f, 0.75f);
                break;
            case ARCANUM:
                textureElement = new TextureElement(ARCANUM_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Shop that sells enchanting gear and services", 0.75f, 0.75f);
                break;
            case BLACKSMITH:
                textureElement = new TextureElement(BLACKSMITH_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Smith for tools, weapons, and gear", 0.75f, 0.75f);
                break;
            case CARTOGRAPHY:
                textureElement = new TextureElement(CARTOGRAPHY_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Shop that sells empty and treasure maps", 0.75f, 0.75f);
                break;
            case FLETCHERY:
                textureElement = new TextureElement(FLETCHERY_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Shop for selling bows, arrows, and fletching items", 0.75f, 0.75f);
                break;
            case TANNERY:
                textureElement = new TextureElement(TANNERY_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Shop for selling leather and stable items", 0.75f, 0.75f);
                break;
            case APIARY:
                textureElement = new TextureElement(APIARY_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Station for maintaining beehives", 0.75f, 0.75f);
                break;
            case COWBARN:
                textureElement = new TextureElement(COWBARN_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Barn for holding cows", 0.75f, 0.75f);
                break;
            case CHICKENCOOP:
                textureElement = new TextureElement(CHICKENCOOP_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Building for holding chickens", 0.75f, 0.75f);
                break;
            case SHEEPPASTURE:
                textureElement = new TextureElement(SHEEPPASTURE_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Pasture area for holding sheep", 0.75f, 0.75f);
                break;
            case STABLE:
                textureElement = new TextureElement(STABLE_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "A stable for holding various four-legged friends", 0.75f, 0.75f);
                break;
            case PIGPEN:
                textureElement = new TextureElement(PIGPEN_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "An area for holding pigs", 0.75f, 0.75f);
                break;
            case BAKERY:
                textureElement = new TextureElement(BAKERY_TEXTURE, (backgroundPosX + 169), (backgroundPosY + 55), 16, 24, "Shop that sells various baked goods", 1.0f, 1.0f);
                break;
            case ABATTOIR:
                textureElement = new TextureElement(ABATTOIR_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Shop that sells a large variety of cooked food", 0.75f, 0.75f);
                break;
            case GREENGROCERY:
                textureElement = new TextureElement(GREENGROCERY_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Shop for selling fresh produce and crops", 0.75f, 0.75f);
                break;
            case WOODSHOP:
                textureElement = new TextureElement(WOODSHOP_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Shop for selling carpentry blocks and items", 0.75f, 0.75f);
                break;
            case MASONRY:
                textureElement = new TextureElement(MASONRY_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Shop for selling various stones and bricks", 0.75f, 0.75f);
                break;
            case MARKETPLACE:
                textureElement = new TextureElement(MARKETPLACE_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Designated area for merchant stalls", 0.75f, 0.75f);
                break;
            case TAVERN:
                textureElement = new TextureElement(TAVERN_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "An inn for visitors of your settlement", 0.75f, 0.75f);
                break;
            case CHURCH:
                textureElement = new TextureElement(CHURCH_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Houses the priest and helps with settler morale", 0.75f, 0.75f);
                break;
            case CEMETERY:
                textureElement = new TextureElement(CEMETERY_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Provides graves for your unfortunate casualties", 0.75f, 0.75f);
                break;
            case LIBRARY:
                textureElement = new TextureElement(LIBRARY_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "A building for helping increase settler skills", 0.75f, 0.75f);
                break;
            case WELL:
                textureElement = new TextureElement(WELL_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 42, 16, "A well that adds flavor to your settlement", 0.75f, 0.75f);
                break;
            case FOUNTAIN:
                textureElement = new TextureElement(FOUNTAIN_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "A fountain that adds flavor to your settlement", 0.75f, 0.75f);
                break;
            default:
                break;
        }
        
        if (textureElement != null)
        {
            textureElement.draw(context);
            if (textureElement.isMouseOver(mouseX, mouseY))
                TextUtil.renderTooltip(context, this.textRenderer, textureElement.getToolTip(), mouseX, mouseY);
        }
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
    
    private int countEmeralds(PlayerEntity player)
    {
        int emeraldCount = 0;
        for (ItemStack stack : player.getInventory().main)
            if (stack.getItem() == Items.EMERALD)
                emeraldCount += stack.getCount();
        return emeraldCount;
    }
    
    private Text getFormattedPriceText(Blueprint build)
    {
        if (this.playerEmeralds >= build.getValue())
            return build.getText().formatted(Formatting.GRAY);
        else 
            return build.getText().formatted(Formatting.RED);
    }
    
    private void resetTexturesArrays()
    {
    	blueprintTextures.clear();
	    upgradeTextures.clear();
	    rectTextures.clear();
	    coreTextures.clear();
	    militaryTextures.clear();
	    laboringTextures.clear();
	    craftingTextures.clear();
	    ranchingTextures.clear();
	    artisanTextures.clear();
	    customsTextures.clear();
	    miscTextures.clear();
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
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
    public void tick()
    {
        super.tick();
        if (this.architect.isDead() || !this.architect.isAlive() || this.architect.isRemoved())
        	MinecraftClient.getInstance().setScreen(null);
    }
    
    @Override
    public boolean shouldPause() 
    {
    	return false;
    }
}