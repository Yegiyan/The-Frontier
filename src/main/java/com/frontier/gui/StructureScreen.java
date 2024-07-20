package com.frontier.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.frontier.PlayerData;
import com.frontier.gui.util.TextUtil;
import com.frontier.gui.util.TextWrapper;
import com.frontier.gui.util.TextureElement;
import com.frontier.network.FrontierPackets;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class StructureScreen extends Screen
{
	public static final int UI_OFFSET_X = 0;
    public static final int UI_OFFSET_Y = 72;
    
    private static final Identifier BACKGROUND_TEXTURE = new Identifier("minecraft", "textures/gui/demo_background.png");
    public static final int BACKGROUND_WIDTH = 256;
    public static final int BACKGROUND_HEIGHT = 335;
	
    private static final Identifier SEPARATOR_TEXTURE = new Identifier("minecraft", "textures/gui/header_separator.png");
    private static final Identifier HANGINGSIGN_TEXTURE = new Identifier("minecraft", "textures/gui/hanging_signs/spruce.png");
    private static final Identifier EMERALD_TEXTURE = new Identifier("minecraft", "textures/item/emerald.png");    
    private static final Identifier INFO_TEXTURE = new Identifier("minecraft", "textures/gui/widgets.png");

    private static final Identifier CONSTRUCTION_TEXTURE = new Identifier("minecraft", "textures/painting/earth.png");
    private static final Identifier UPGRADE_TEXTURE = new Identifier("minecraft", "textures/painting/wind.png");
    
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
    private static final Identifier CHICKENCOOP_TEXTURE = new Identifier("minecraft", "textures/block/farmland.png");
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
    
    private List<TextureElement> constructionTextures = new ArrayList<>();
    private List<TextureElement> resourcesTextures = new ArrayList<>();
    private List<TextureElement> upgradeTextures = new ArrayList<>();
    
    private List<TextureElement> rect1Textures = new ArrayList<>();
    private List<TextureElement> rect2Textures = new ArrayList<>();
    
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
    
    public enum Page { CONSTRUCTION, RESOURCES, UPGRADE, CORE, MILITARY, LABOR, CRAFT, RANCH, ARTISAN, CUSTOMS, MISC }
    private Page page;
    
    public enum Build
    {
        NONE(0), TOWNHALL(2), WAREHOUSE(4), HOUSE(3),  ROAD(12), BRIDGE(10), WALL(6),
        BARRACKS(4), APOTHECARY(12), BOUNTYHALL(6), WATCHTOWER(10),
        FARM(4), FISHERY(5), LODGE(3), GROVE(4), MINE(8),
        ALCHEMYLAB(12), ARCANUM(10), BLACKSMITH(8), CARTOGRAPHY(10), FLETCHERY(6), TANNERY(8),
        APIARY(8), COWBARN(4), CHICKENCOOP(6), SHEEPPASTURE(8), STABLE(14), PIGPEN(4), 
        BAKERY(6), ABATTOIR(8), GREENGROCERY(4), WOODSHOP(8), MASONRY(10),
        MARKETPLACE(12), TAVERN(8),
        CHURCH(10), LIBRARY(12), CEMETERY(6), WELL(4), FOUNTAIN(6);

        private int value;
        Build(int value) { this.value = value; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
        public MutableText getText() { return Text.literal(String.valueOf(value)); }
    }
    private Build build;
    
    private ButtonWidget buildButton, upgradeButton;
    private ButtonWidget constructPageButton, resourcesPageButton, upgradePageButton;
    private ButtonWidget coreButton, militiaButton, laboringButton, craftingButton, ranchingButton, artisanButton, customsButton, miscButton;
    private ButtonWidget barracksButton, watchTowerButton, bountyHallButton;
    private ButtonWidget townhallButton, warehouseButton, houseButton, roadButton, bridgeButton, wallButton;
    private ButtonWidget farmButton, fisheryButton, lodgeButton, groveButton, mineButton;
    private ButtonWidget alchemyLabButton, arcanumButton, blacksmithButton, cartographyButton, fletcheryButton, tanneryButton;
    private ButtonWidget apiaryButton, cowBarnButton, chickenCoopButton, sheepPastureButton, stableButton, pigPenButton;
    private ButtonWidget bakeryButton, abattoirButton, greengroceryButton, woodshopButton, masonryButton;
    private ButtonWidget marketplaceButton, tavernButton;
    private ButtonWidget churchButton, libraryButton, cemeteryButton, wellButton, fountainButton;
    
    @SuppressWarnings("unused") private Text buildPriceTitle;
    private Text buildPriceText;
    
    private Text titleText1;
    private Text titleText2;
    
    private Text constructText1;
    private Text constructText2;
    
    private Text resourcesText1;
    private Text resourcesText2;
    
    private Text upgradeText1;
    private Text upgradeText2;
    
    PlayerEntity player;
    int playerEmeralds;
    
	public StructureScreen()
	{
		super(Text.literal("Construction Screen"));
		this.page = Page.CONSTRUCTION;
		this.player = MinecraftClient.getInstance().player;
		this.playerEmeralds = 0;
	}

	@Override
	protected void init()
	{
		backgroundPosX = ((this.width - BACKGROUND_WIDTH) / 2) + UI_OFFSET_X;
		backgroundPosY = ((this.height - BACKGROUND_HEIGHT) / 2) + UI_OFFSET_Y;

		buildPriceTitle = Text.literal("Building Cost");
		buildPriceText = Text.literal("0");
		
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
			case CONSTRUCTION:
				setupConstructionPage();
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

		if (build == Build.NONE)
			buildPriceText = Text.literal("0");

		initializeBuildButton();
		initializeUpgradeButton();
		
        addDrawableChild(buildButton);
        addDrawableChild(upgradeButton);
		addDrawableChild(constructPageButton);
		addDrawableChild(resourcesPageButton);
		addDrawableChild(upgradePageButton);
	}

	private void initializeMainButtons()
	{
		constructPageButton = ButtonWidget.builder(Text.literal("Construct"), button ->
		{
			page = Page.CONSTRUCTION;
			updateButtons();
		}).dimensions(backgroundPosX + 0, backgroundPosY - 25, 70, 20).build();
		constructPageButton.active = true;
		
		resourcesPageButton = ButtonWidget.builder(Text.literal("Resources"), button ->
		{
			page = Page.RESOURCES;
			updateButtons();
		}).dimensions(backgroundPosX + 89, backgroundPosY - 25, 70, 20).build();
		resourcesPageButton.active = true;
		
		upgradePageButton = ButtonWidget.builder(Text.literal("Upgrade"), button ->
		{
			page = Page.UPGRADE;
			updateButtons();
		}).dimensions(backgroundPosX + 178, backgroundPosY - 25, 70, 20).build();
		upgradePageButton.active = true;
	}

	private void initializeResorucesPageButtons()
	{
		resourcesPageButton.active = false;
	}
	
	private void initializeUpgradePageButtons()
	{
		upgradePageButton.active = false;
	}
	
	private void initializeBuildButton()
	{
        buildButton = ButtonWidget.builder(Text.literal("Build"), button ->
        {
            PlayerData playerData = PlayerData.players.get(this.player.getUuid());
            if (this.player != null && playerData != null)
            {
                PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
                // TODO: Pass building info
                passedData.writeInt(build.getValue());
                ClientPlayNetworking.send(FrontierPackets.BUILD_STRUCTURE_ID, passedData);
                MinecraftClient.getInstance().setScreen(null);
            }
        }).dimensions(backgroundPosX + 192, backgroundPosY + 63, 45, 20).build();
    }
	
	private void initializeUpgradeButton()
	{
        upgradeButton = ButtonWidget.builder(Text.literal("Upgrade"), button ->
        {
            PlayerData playerData = PlayerData.players.get(this.player.getUuid());
            if (this.player != null && playerData != null)
            {
                PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
                // TODO: Pass building info
                //passedData.writeInt();
                ClientPlayNetworking.send(FrontierPackets.UPGRADE_STRUCTURE_ID, passedData);
                MinecraftClient.getInstance().setScreen(null);
            }
        }).dimensions(backgroundPosX + 120, backgroundPosY + 44, 52, 20).build();
    }
	
	private void initializeJobCategoryButtons()
	{
		constructPageButton.active = false;
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
	
	private void setupConstructionPage()
	{
		constructText1 = Text.literal("CONSTRUCT BUILDING").formatted(Formatting.BOLD).formatted(Formatting.UNDERLINE);
		constructText2 = Text.literal("As the leader you can order the architect to construct a variety of buildings. "
				+ "Each building has 5 upgradeable tiers (0-4). Upgrades cannot surpass the tier of the town hall by more than 1. "
				+ "You can also create your own custom buildings by...");
		
		build = Build.NONE;
		setupTextures();
		initializeJobCategoryButtons();
	}
	
	private void setupResourcesPage()
	{
		resourcesText1 = Text.literal("AVAILABLE RESOURCES").formatted(Formatting.BOLD).formatted(Formatting.UNDERLINE);
		resourcesText2 = Text.literal("(Town Hall & Warehouse)");
		
		build = Build.NONE;
		setupTextures();
		initializeResorucesPageButtons();
	}
	
	private void setupUpgradePage()
	{
		upgradeText1 = Text.literal("UPGRADE BUILDING").formatted(Formatting.BOLD).formatted(Formatting.UNDERLINE);
		upgradeText2 = Text.literal("");
		
		build = Build.NONE;
		setupTextures();
		initializeUpgradePageButtons();
	}

	private void setupTextures()
	{
		constructionTextures.add(new TextureElement(CONSTRUCTION_TEXTURE, (backgroundPosX + 177), (backgroundPosY + 9), 32, 16, 2.0f));
		upgradeTextures.add(new TextureElement(UPGRADE_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 9), 32, 16, 2.0f));
	}

	private void setupCorePage()
	{
		titleText1 = Text.literal("These structures make up the foundation of your settlement.");
		titleText2 = Text.literal("Your architect resides in your townhall and the warehouse is where your resources are stored.");
		
		townhallButton = ButtonWidget.builder(Text.literal("Town Hall"), button ->
		{
			build = Build.TOWNHALL;
			buildPriceText = Build.TOWNHALL.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		warehouseButton = ButtonWidget.builder(Text.literal("Warehouse"), button ->
		{
			build = Build.WAREHOUSE;
			buildPriceText = Build.WAREHOUSE.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		houseButton = ButtonWidget.builder(Text.literal("House"), button ->
		{
			build = Build.HOUSE;
			buildPriceText = Build.HOUSE.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		roadButton = ButtonWidget.builder(Text.literal("Road"), button ->
		{
			build = Build.ROAD;
			buildPriceText = Build.ROAD.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 155, 65, 20).build();
		
		bridgeButton = ButtonWidget.builder(Text.literal("Bridge"), button ->
		{
			build = Build.BRIDGE;
			buildPriceText = Build.BRIDGE.getText();
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
			build = Build.BARRACKS;
			buildPriceText = Build.BARRACKS.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		watchTowerButton = ButtonWidget.builder(Text.literal("Watch Tower"), button ->
		{
			build = Build.WATCHTOWER;
			buildPriceText = Build.WATCHTOWER.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		bountyHallButton = ButtonWidget.builder(Text.literal("Bounty Hall"), button ->
		{
			build = Build.BOUNTYHALL;
			buildPriceText = Build.BOUNTYHALL.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		wallButton = ButtonWidget.builder(Text.literal("Wall"), button ->
		{
			build = Build.WALL;
			buildPriceText = Build.WALL.getText();
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
			build = Build.FARM;
			buildPriceText = Build.FARM.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		groveButton = ButtonWidget.builder(Text.literal("Grove"), button ->
		{
			build = Build.GROVE;
			buildPriceText = Build.GROVE.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		mineButton = ButtonWidget.builder(Text.literal("Mine"), button ->
		{
			build = Build.MINE;
			buildPriceText = Build.MINE.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		fisheryButton = ButtonWidget.builder(Text.literal("Fishery"), button ->
		{
			build = Build.FISHERY;
			buildPriceText = Build.FISHERY.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 155, 65, 20).build();
		
		lodgeButton = ButtonWidget.builder(Text.literal("Lodge"), button ->
		{
			build = Build.LODGE;
			buildPriceText = Build.LODGE.getText();
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
			build = Build.ALCHEMYLAB;
			buildPriceText = Build.ALCHEMYLAB.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		arcanumButton = ButtonWidget.builder(Text.literal("Arcanum"), button ->
		{
			build = Build.ARCANUM;
			buildPriceText = Build.ARCANUM.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		blacksmithButton = ButtonWidget.builder(Text.literal("Blacksmith"), button ->
		{
			build = Build.BLACKSMITH;
			buildPriceText = Build.BLACKSMITH.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		cartographyButton = ButtonWidget.builder(Text.literal("Cartography"), button ->
		{
			build = Build.CARTOGRAPHY;
			buildPriceText = Build.CARTOGRAPHY.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 155, 65, 20).build();
		
		fletcheryButton = ButtonWidget.builder(Text.literal("Fletchery"), button ->
		{
			build = Build.FLETCHERY;
			buildPriceText = Build.FLETCHERY.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 155, 65, 20).build();

		tanneryButton = ButtonWidget.builder(Text.literal("Tannery"), button ->
		{
			build = Build.TANNERY;
			buildPriceText = Build.TANNERY.getText();
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
			build = Build.APIARY;
			buildPriceText = Build.APIARY.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		cowBarnButton = ButtonWidget.builder(Text.literal("Cow Barn"), button ->
		{
			build = Build.COWBARN;
			buildPriceText = Build.COWBARN.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		chickenCoopButton = ButtonWidget.builder(Text.literal("Chicken Coop"), button ->
		{
			build = Build.CHICKENCOOP;
			buildPriceText = Build.CHICKENCOOP.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		sheepPastureButton = ButtonWidget.builder(Text.literal("Sheep Pasture"), button ->
		{
			build = Build.SHEEPPASTURE;
			buildPriceText = Build.SHEEPPASTURE.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 155, 65, 20).build();
		
		stableButton = ButtonWidget.builder(Text.literal("Stable"), button ->
		{
			build = Build.STABLE;
			buildPriceText = Build.STABLE.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 155, 65, 20).build();
		
		pigPenButton = ButtonWidget.builder(Text.literal("Pig Pen"), button ->
		{
			build = Build.PIGPEN;
			buildPriceText = Build.PIGPEN.getText();
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
			build = Build.BAKERY;
			buildPriceText = Build.BAKERY.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		abattoirButton = ButtonWidget.builder(Text.literal("Abattoir"), button ->
		{
			build = Build.ABATTOIR;
			buildPriceText = Build.ABATTOIR.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		greengroceryButton = ButtonWidget.builder(Text.literal("Greengrocery"), button ->
		{
			build = Build.GREENGROCERY;
			buildPriceText = Build.GREENGROCERY.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		woodshopButton = ButtonWidget.builder(Text.literal("Woodshop"), button ->
		{
			build = Build.WOODSHOP;
			buildPriceText = Build.WOODSHOP.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 155, 65, 20).build();
		
		masonryButton = ButtonWidget.builder(Text.literal("Masonry"), button ->
		{
			build = Build.MASONRY;
			buildPriceText = Build.MASONRY.getText();
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
			build = Build.MARKETPLACE;
			buildPriceText = Build.MARKETPLACE.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		tavernButton = ButtonWidget.builder(Text.literal("Tavern"), button ->
		{
			build = Build.TAVERN;
			buildPriceText = Build.TAVERN.getText();
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
			build = Build.CHURCH;
			buildPriceText = Build.CHURCH.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		cemeteryButton = ButtonWidget.builder(Text.literal("Cemetery"), button ->
		{
			build = Build.CEMETERY;
			buildPriceText = Build.CEMETERY.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		libraryButton = ButtonWidget.builder(Text.literal("Library"), button ->
		{
			build = Build.LIBRARY;
			buildPriceText = Build.LIBRARY.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		wellButton = ButtonWidget.builder(Text.literal("Well"), button ->
		{
			build = Build.WELL;
			buildPriceText = Build.WELL.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 155, 65, 20).build();
		
		fountainButton = ButtonWidget.builder(Text.literal("Fountain"), button ->
		{
			build = Build.FOUNTAIN;
			buildPriceText = Build.FOUNTAIN.getText();
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
		if (page == Page.CONSTRUCTION || page == Page.RESOURCES || page == Page.UPGRADE)
			buildButton.visible = false;
		else
			buildButton.visible = true;

		if (page == Page.UPGRADE)
			upgradeButton.visible = true;
		else
			upgradeButton.visible = false;

		if (build != Build.NONE && this.playerEmeralds >= build.getValue())
			buildButton.active = true;
		else
			buildButton.active = false;

	    this.renderBackground(context);
	    context.drawTexture(BACKGROUND_TEXTURE, backgroundPosX, backgroundPosY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
	    super.render(context, mouseX, mouseY, delta);

	    if (page != Page.CONSTRUCTION && page != Page.RESOURCES && page != Page.UPGRADE)
	    {
	        TextWrapper.render(context, this.textRenderer, titleText1, backgroundPosX + 14, backgroundPosY + 16, new Color(255, 255, 255).getRGB(), 145);
	        TextWrapper.render(context, this.textRenderer, titleText2, backgroundPosX + 14, backgroundPosY + 62, new Color(255, 255, 255).getRGB(), 145);
	        context.drawTexture(SEPARATOR_TEXTURE, (backgroundPosX + 10), (backgroundPosY + 110), 0, 0, 225, 2, 32, 2);
	        context.drawTexture(HANGINGSIGN_TEXTURE, (backgroundPosX + 175), (backgroundPosY + 16), 0, 0, 55, 32, 54, 32);
	        context.drawTexture(EMERALD_TEXTURE, (backgroundPosX + 207), (backgroundPosY + 30), 0, 0, 16, 16, 16, 16);
	        
	        rect1Textures.add(new TextureElement(INFO_TEXTURE, (backgroundPosX + 166), (backgroundPosY + 62), 22, 22, 1, 23, 256, 256, null, 1.0f));
	        for (TextureElement element : rect1Textures)
			{
				element.drawRect(context);
				if (element.isMouseOver(mouseX, mouseY))
					TextUtil.renderTooltip(context, this.textRenderer, element.getToolTip(), mouseX, mouseY);
			}
	        
	        drawTextureForBuild(context, mouseX, mouseY, build);
	    }

	    switch (page)
	    {
	        case CONSTRUCTION:
	            renderConstructionPage(context, mouseX, mouseY);
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

	private void renderConstructionPage(DrawContext context, int mouseX, int mouseY)
	{
		TextWrapper.render(context, this.textRenderer, constructText1, backgroundPosX + 14, backgroundPosY + 16, new Color(255, 255, 255).getRGB(), 145);
		TextWrapper.render(context, this.textRenderer, constructText2, backgroundPosX + 14, backgroundPosY + 50, new Color(255, 255, 255).getRGB(), 220);
		
		context.drawTexture(SEPARATOR_TEXTURE, (backgroundPosX + 10), (backgroundPosY + 140), 0, 0, 225, 2, 32, 2);

		for (TextureElement element : constructionTextures)
		{
			element.draw(context);
			if (element.isMouseOver(mouseX, mouseY))
				TextUtil.renderTooltip(context, this.textRenderer, element.getToolTip(), mouseX, mouseY);
		}
	}
	
	private void renderResourcesPage(DrawContext context, int mouseX, int mouseY)
	{
		TextWrapper.render(context, this.textRenderer, resourcesText1, backgroundPosX + 60, backgroundPosY + 16, new Color(255, 255, 255).getRGB(), 145);
		TextWrapper.render(context, this.textRenderer, resourcesText2, backgroundPosX + 64, backgroundPosY + 28, new Color(255, 255, 255).getRGB(), 145);
		
		for (TextureElement element : resourcesTextures)
		{
			element.draw(context);
			if (element.isMouseOver(mouseX, mouseY))
				TextUtil.renderTooltip(context, this.textRenderer, element.getToolTip(), mouseX, mouseY);
		}
	}
	
	private void renderUpgradePage(DrawContext context, int mouseX, int mouseY)
	{
		TextWrapper.render(context, this.textRenderer, upgradeText1, backgroundPosX + 120, backgroundPosY + 16, new Color(255, 255, 255).getRGB(), 145);
		TextWrapper.render(context, this.textRenderer, upgradeText2, backgroundPosX + 14, backgroundPosY + 62, new Color(255, 255, 255).getRGB(), 220);
		
		context.drawTexture(HANGINGSIGN_TEXTURE, (backgroundPosX + 175), (backgroundPosY + 32), 0, 0, 55, 32, 54, 32);
		context.drawTexture(EMERALD_TEXTURE, (backgroundPosX + 207), (backgroundPosY + 46), 0, 0, 16, 16, 16, 16);
		context.drawTexture(SEPARATOR_TEXTURE, (backgroundPosX + 10), (backgroundPosY + 70), 0, 0, 225, 2, 32, 2);

		rect2Textures.add(new TextureElement(INFO_TEXTURE, (backgroundPosX + 141), (backgroundPosY + 30), 22, 22, 1, 23, 256, 256, null, 0.5f));
        for (TextureElement element : rect2Textures)
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
		townhallButton.active = (build != Build.TOWNHALL);
		warehouseButton.active = (build != Build.WAREHOUSE);
		houseButton.active = (build != Build.HOUSE);
		roadButton.active = (build != Build.ROAD);
		bridgeButton.active = (build != Build.BRIDGE);
		
		if (townhallButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.TOWNHALL);
		else if (warehouseButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.WAREHOUSE);
		else if (houseButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.HOUSE);
		else if (roadButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.ROAD);
		else if (bridgeButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.BRIDGE);
		else
		    buildPriceText = build.getText();

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
		barracksButton.active = (build != Build.BARRACKS);
		watchTowerButton.active = (build != Build.WATCHTOWER);
		bountyHallButton.active = (build != Build.BOUNTYHALL);
		wallButton.active = (build != Build.WALL);

		if (barracksButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.BARRACKS);
		else if (watchTowerButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.WATCHTOWER);
		else if (bountyHallButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.BOUNTYHALL);
		else if (wallButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.WALL);
		else
			buildPriceText = build.getText();

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
		farmButton.active = (build != Build.FARM);
		fisheryButton.active = (build != Build.FISHERY);
		lodgeButton.active = (build != Build.LODGE);
		groveButton.active = (build != Build.GROVE);
		mineButton.active = (build != Build.MINE);

		if (farmButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.FARM);
		else if (fisheryButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.FISHERY);
		else if (lodgeButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.LODGE);
		else if (groveButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.GROVE);
		else if (mineButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.MINE);
		else
			buildPriceText = build.getText();

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
		alchemyLabButton.active = (build != Build.ALCHEMYLAB);
		arcanumButton.active = (build != Build.ARCANUM);
		blacksmithButton.active = (build != Build.BLACKSMITH);
		cartographyButton.active = (build != Build.CARTOGRAPHY);
		fletcheryButton.active = (build != Build.FLETCHERY);
		tanneryButton.active = (build != Build.TANNERY);

		if (alchemyLabButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.ALCHEMYLAB);
		else if (arcanumButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.ARCANUM);
		else if (blacksmithButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.BLACKSMITH);
		else if (cartographyButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.CARTOGRAPHY);
		else if (fletcheryButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.FLETCHERY);
		else if (tanneryButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.TANNERY);
		else
			buildPriceText = build.getText();

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
		apiaryButton.active = (build != Build.APIARY);
		cowBarnButton.active = (build != Build.COWBARN);
		chickenCoopButton.active = (build != Build.CHICKENCOOP);
		sheepPastureButton.active = (build != Build.SHEEPPASTURE);
		stableButton.active = (build != Build.STABLE);
		pigPenButton.active = (build != Build.PIGPEN);
		
		if (apiaryButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.APIARY);
		else if (cowBarnButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.COWBARN);
		else if (chickenCoopButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.CHICKENCOOP);
		else if (sheepPastureButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.SHEEPPASTURE);
		else if (stableButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.STABLE);
		else if (pigPenButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.PIGPEN);
		else
			buildPriceText = build.getText();

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
		bakeryButton.active = (build != Build.BAKERY);
		abattoirButton.active = (build != Build.ABATTOIR);
		greengroceryButton.active = (build != Build.GREENGROCERY);
		woodshopButton.active = (build != Build.WOODSHOP);
		masonryButton.active = (build != Build.MASONRY);

		if (bakeryButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.BAKERY);
		else if (abattoirButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.ABATTOIR);
		else if (greengroceryButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.GREENGROCERY);
		else if (woodshopButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.WOODSHOP);
		else if (masonryButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.MASONRY);
		else
			buildPriceText = build.getText();

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
		marketplaceButton.active = (build != Build.MARKETPLACE);
		tavernButton.active = (build != Build.TAVERN);

		if (marketplaceButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.MARKETPLACE);
		else if (tavernButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.TAVERN);
		else
			buildPriceText = build.getText();

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
		churchButton.active = (build != Build.CHURCH);
		libraryButton.active = (build != Build.LIBRARY);
		cemeteryButton.active = (build != Build.CEMETERY);
		wellButton.active = (build != Build.WELL);
		fountainButton.active = (build != Build.FOUNTAIN);

		if (churchButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.CHURCH);
		else if (libraryButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.LIBRARY);
		else if (cemeteryButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.CEMETERY);
		else if (wellButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.WELL);
		else if (fountainButton.isMouseOver(mouseX, mouseY))
		    buildPriceText = getFormattedPriceText(Build.FOUNTAIN);
		else
			buildPriceText = build.getText();

		for (TextureElement element : miscTextures)
		{
			element.draw(context);
			if (element.isMouseOver(mouseX, mouseY))
				TextUtil.renderTooltip(context, this.textRenderer, element.getToolTip(), mouseX, mouseY);
		}

		drawPriceText(context);
    }

    private  void drawPriceText(DrawContext context)
    {
    	if (this.playerEmeralds < build.getValue())
			TextUtil.drawTextR(context, this.textRenderer, buildPriceText, backgroundPosX + 201, backgroundPosY + 34, new Color(235, 50, 30).getRGB(), true);
		else
			TextUtil.drawTextR(context, this.textRenderer, buildPriceText, backgroundPosX + 201, backgroundPosY + 34, new Color(255, 255, 255).getRGB(), true);
    }
    
    private void drawTextureForBuild(DrawContext context, int mouseX, int mouseY, Build build)
    {
        TextureElement textureElement = null;
        int posX = 171;
        int posY = 67;
        
        switch (build)
        {
            case TOWNHALL:
                textureElement = new TextureElement(TOWNHALL_TEXTURE, (backgroundPosX + 169), (backgroundPosY + 66), 16, 16, "Uses settlement supplies to construct buildings", 1.0f);
                break;
            case WAREHOUSE:
                textureElement = new TextureElement(WAREHOUSE_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Stores all resources for your settlement to utilize", 0.75f);
                break;
            case HOUSE:
                textureElement = new TextureElement(HOUSE_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "The required living space for your settlers", 0.75f);
                break;
            case ROAD:
                textureElement = new TextureElement(ROAD_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Connects you to other settlements and creates traffic", 0.75f);
                break;
            case BRIDGE:
                textureElement = new TextureElement(BRIDGE_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Connects roads that are divided by empty space", 0.75f);
                break;
            case BARRACKS:
                textureElement = new TextureElement(BARRACKS_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Houses your military units and gives them benefits", 0.75f);
                break;
            case WATCHTOWER:
                textureElement = new TextureElement(WATCHTOWER_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Increases faction territory and security", 0.75f);
                break;
            case BOUNTYHALL:
                textureElement = new TextureElement(BOUNTYHALL_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Gives bounties that anyone can attempt to fulfill", 0.75f);
                break;
            case WALL:
                textureElement = new TextureElement(WALL_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Provides security for your settlement", 0.75f);
                break;
            case FARM:
                textureElement = new TextureElement(FARM_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Designated area for farmers to work", 0.75f);
                break;
            case GROVE:
                textureElement = new TextureElement(GROVE_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Designated area for lumberjacks to work", 0.75f);
                break;
            case MINE:
                textureElement = new TextureElement(MINE_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Designated mine for miners to work", 0.75f);
                break;
            case FISHERY:
                textureElement = new TextureElement(FISHERY_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Designated water for a fisher to work", 0.75f);
                break;
            case LODGE:
                textureElement = new TextureElement(LODGE_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Lodge for foragers to bring their findings", 0.75f);
                break;
            case ALCHEMYLAB:
                textureElement = new TextureElement(ALCHEMYLAB_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Shop that sells alchemic potions and items", 0.75f);
                break;
            case ARCANUM:
                textureElement = new TextureElement(ARCANUM_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Shop that sells enchanting gear and services", 0.75f);
                break;
            case BLACKSMITH:
                textureElement = new TextureElement(BLACKSMITH_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Smith for tools, weapons, and gear", 0.75f);
                break;
            case CARTOGRAPHY:
                textureElement = new TextureElement(CARTOGRAPHY_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Shop that sells empty and treasure maps", 0.75f);
                break;
            case FLETCHERY:
                textureElement = new TextureElement(FLETCHERY_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Shop for selling bows, arrows, and fletching items", 0.75f);
                break;
            case TANNERY:
                textureElement = new TextureElement(TANNERY_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Shop for selling leather and stable items", 0.75f);
                break;
            case APIARY:
                textureElement = new TextureElement(APIARY_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Station for maintaining beehives", 0.75f);
                break;
            case COWBARN:
                textureElement = new TextureElement(COWBARN_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Barn for holding cows", 0.75f);
                break;
            case CHICKENCOOP:
                textureElement = new TextureElement(CHICKENCOOP_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Building for holding chickens", 0.75f);
                break;
            case SHEEPPASTURE:
                textureElement = new TextureElement(SHEEPPASTURE_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Pasture area for holding sheep", 0.75f);
                break;
            case STABLE:
                textureElement = new TextureElement(STABLE_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "A stable for holding various four-legged friends", 0.75f);
                break;
            case PIGPEN:
                textureElement = new TextureElement(PIGPEN_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "An area for holding pigs", 0.75f);
                break;
            case BAKERY:
                textureElement = new TextureElement(BAKERY_TEXTURE, (backgroundPosX + 194), (backgroundPosY + 79), 16, 24, "Shop that sells various baked goods", 1.0f);
                break;
            case ABATTOIR:
                textureElement = new TextureElement(ABATTOIR_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Shop that sells a large variety of cooked food", 0.75f);
                break;
            case GREENGROCERY:
                textureElement = new TextureElement(GREENGROCERY_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Shop for selling fresh produce and crops", 0.75f);
                break;
            case WOODSHOP:
                textureElement = new TextureElement(WOODSHOP_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Shop for selling carpentry blocks and items", 0.75f);
                break;
            case MASONRY:
                textureElement = new TextureElement(MASONRY_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Shop for selling various stones and bricks", 0.75f);
                break;
            case MARKETPLACE:
                textureElement = new TextureElement(MARKETPLACE_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Designated area for merchant stalls", 0.75f);
                break;
            case TAVERN:
                textureElement = new TextureElement(TAVERN_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "An inn for visitors of your settlement", 0.75f);
                break;
            case CHURCH:
                textureElement = new TextureElement(CHURCH_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Houses the priest and helps with settler morale", 0.75f);
                break;
            case CEMETERY:
                textureElement = new TextureElement(CEMETERY_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "Provides graves for your unfortunate casualties", 0.75f);
                break;
            case LIBRARY:
                textureElement = new TextureElement(LIBRARY_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "A building for helping increase settler skills", 0.75f);
                break;
            case WELL:
                textureElement = new TextureElement(WELL_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 42, 16, "A well that adds flavor to your settlement", 0.75f);
                break;
            case FOUNTAIN:
                textureElement = new TextureElement(FOUNTAIN_TEXTURE, (backgroundPosX + posX), (backgroundPosY + posY), 16, 16, "A fountain that adds flavor to your settlement", 0.75f);
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
    
    private int countEmeralds(PlayerEntity player)
    {
        int emeraldCount = 0;
        for (ItemStack stack : player.getInventory().main)
            if (stack.getItem() == Items.EMERALD)
                emeraldCount += stack.getCount();
        return emeraldCount;
    }
    
    private Text getFormattedPriceText(Build build)
    {
        if (this.playerEmeralds >= build.getValue())
            return build.getText().formatted(Formatting.GRAY);
        else 
            return build.getText().formatted(Formatting.RED);
    }
    
    @Override
    public boolean shouldPause() 
    {
    	return false;
    }
}