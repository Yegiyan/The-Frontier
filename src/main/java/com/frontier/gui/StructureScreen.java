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
    
    private static final Identifier CONSTRUCTION_TEXTURE = new Identifier("minecraft", "textures/painting/sea.png");
    private static final Identifier RESOURCES_TEXTURE = new Identifier("minecraft", "textures/painting/creebet.png");
    private static final Identifier UPGRADE_TEXTURE = new Identifier("minecraft", "textures/painting/courbet.png");
    
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
    private static final Identifier FORAGINGLODGE_TEXTURE = new Identifier("minecraft", "textures/block/cave_vines_plant.png");
    private static final Identifier GROVE_TEXTURE = new Identifier("minecraft", "textures/block/oak_log_top.png");
    private static final Identifier MINE_TEXTURE = new Identifier("minecraft", "textures/block/gold_ore.png");
    
    private static final Identifier ALCHEMYLAB_TEXTURE = new Identifier("minecraft", "textures/block/brewing_stand_base.png");
    private static final Identifier ARCANUM_TEXTURE = new Identifier("minecraft", "textures/block/amethyst_block.png");
    private static final Identifier FORGESMITH_TEXTURE = new Identifier("minecraft", "textures/block/blast_furnace_front.png");
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
        FARM(4), FISHERY(5), FORAGINGLODGE(3), GROVE(4), MINE(8),
        ALCHEMYLAB(12), ARCANUM(10), SMITHFORGE(8), CARTOGRAPHY(10), FLETCHERY(6), TANNERY(8),
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
    
    private ButtonWidget buildButton;
    
    private ButtonWidget constructButton;
    private ButtonWidget resourcesButton;
    private ButtonWidget upgradeButton;
    
    private ButtonWidget coreButton;
    private ButtonWidget militiaButton;
    private ButtonWidget laboringButton;
    private ButtonWidget craftingButton;
    private ButtonWidget ranchingButton;
    private ButtonWidget artisanButton;
    private ButtonWidget customsButton;
    private ButtonWidget miscButton;
    
    private ButtonWidget barracksButton;
    private ButtonWidget watchTowerButton;
    private ButtonWidget bountyHallButton;
    
    private ButtonWidget townhallButton;
    private ButtonWidget warehouseButton;
    private ButtonWidget houseButton;
    private ButtonWidget roadButton;
    private ButtonWidget bridgeButton;
    private ButtonWidget wallButton;
    
    private ButtonWidget farmButton;
    private ButtonWidget fisheryButton;
    private ButtonWidget foragingLodgeButton;
    private ButtonWidget groveButton;
    private ButtonWidget mineButton;
    
    private ButtonWidget alchemyLabButton;
    private ButtonWidget arcanumButton;
    private ButtonWidget smithForgeButton;
    private ButtonWidget cartographyButton;
    private ButtonWidget fletcheryButton;
    private ButtonWidget tanneryButton;
    
    private ButtonWidget apiaryButton;
    private ButtonWidget cowBarnButton;
    private ButtonWidget chickenCoopButton;
    private ButtonWidget sheepPastureButton;
    private ButtonWidget stableButton;
    private ButtonWidget pigPenButton;
    
    private ButtonWidget bakeryButton;
    private ButtonWidget abattoirButton;
    private ButtonWidget greengroceryButton;
    private ButtonWidget woodshopButton;
    private ButtonWidget masonryButton;
    
    private ButtonWidget marketplaceButton;
    private ButtonWidget tavernButton;
    
    private ButtonWidget churchButton;
    private ButtonWidget libraryButton;
    private ButtonWidget cemeteryButton;
    private ButtonWidget wellButton;
    private ButtonWidget fountainButton;
    
    private Text priceTitle;
    private Text priceText;
    
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

		priceTitle = Text.literal("Building Cost");
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
			priceText = Text.literal("0");

		initializeBuildButton();
        addDrawableChild(buildButton);
		addDrawableChild(constructButton);
		addDrawableChild(resourcesButton);
		addDrawableChild(upgradeButton);
	}

	private void initializeMainButtons()
	{
		constructButton = ButtonWidget.builder(Text.literal("Construct"), button ->
		{
			page = Page.CONSTRUCTION;
			updateButtons();
		}).dimensions(backgroundPosX + 0, backgroundPosY - 25, 70, 20).build();
		constructButton.active = true;
		
		resourcesButton = ButtonWidget.builder(Text.literal("Resources"), button ->
		{
			page = Page.RESOURCES;
			updateButtons();
		}).dimensions(backgroundPosX + 89, backgroundPosY - 25, 70, 20).build();
		resourcesButton.active = true;
		
		upgradeButton = ButtonWidget.builder(Text.literal("Upgrade"), button ->
		{
			page = Page.UPGRADE;
			updateButtons();
		}).dimensions(backgroundPosX + 178, backgroundPosY - 25, 70, 20).build();
		upgradeButton.active = true;
	}

	private void setupConstructionPage()
	{
		constructText1 = Text.literal("This is the construction page.");
		constructText2 = Text.literal("TALLY HO.");
		
		build = Build.NONE;
		setupTextures();
		initializeMainPageButtons();
	}
	
	private void setupResourcesPage()
	{
		resourcesText1 = Text.literal("This is the resources page.");
		resourcesText2 = Text.literal("TALLY HO.");
		
		build = Build.NONE;
		setupTextures();
		initializeResorucesPageButtons();
	}
	
	private void setupUpgradePage()
	{
		upgradeText1 = Text.literal("This is the upgrade page.");
		upgradeText2 = Text.literal("TALLY HO.");
		
		build = Build.NONE;
		setupTextures();
		initializeUpgradePageButtons();
	}

	private void setupTextures()
	{
		constructionTextures.add(new TextureElement(CONSTRUCTION_TEXTURE, (backgroundPosX + 177), (backgroundPosY + 10), 32, 16, 2.0f));
		//resourcesTextures.add(new TextureElement(RESOURCES_TEXTURE, (backgroundPosX + 90), (backgroundPosY + 95), 32, 16, 2.0f));
		//upgradeTextures.add(new TextureElement(UPGRADE_TEXTURE, (backgroundPosX + 90), (backgroundPosY + 95), 32, 16, 2.0f));
		
		coreTextures.add(new TextureElement(TOWNHALL_TEXTURE, (backgroundPosX + 5), (backgroundPosY + 118), 16, 16, "Uses settlement supplies to construct buildings", 1.0f));
		coreTextures.add(new TextureElement(WAREHOUSE_TEXTURE, (backgroundPosX + 87), (backgroundPosY + 120), 16, 16, "Sends requests or demands to other settlements", 0.75f));
		coreTextures.add(new TextureElement(HOUSE_TEXTURE, (backgroundPosX + 167), (backgroundPosY + 120), 16, 16, "Disperses settlement supplies to other settlers", 0.75f));
		coreTextures.add(new TextureElement(ROAD_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 150), 16, 16, "Provides shelter and rest for foreigners", 0.75f));
		coreTextures.add(new TextureElement(BRIDGE_TEXTURE, (backgroundPosX + 87), (backgroundPosY + 150), 16, 16, "Exports your settlement goods to foreigners", 0.75f));
		
		militaryTextures.add(new TextureElement(BARRACKS_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 120), 16, 16, "Ranged fighter that wears leather armor", 0.75f));
		militaryTextures.add(new TextureElement(WATCHTOWER_TEXTURE, (backgroundPosX + 87), (backgroundPosY + 120), 16, 16, "Medic that heals allies in and out of combat", 0.75f));
		militaryTextures.add(new TextureElement(BOUNTYHALL_TEXTURE, (backgroundPosX + 167), (backgroundPosY + 120), 16, 16, "Melee fighter that wears leather or heavy armor", 0.75f));
		militaryTextures.add(new TextureElement(WALL_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 150), 16, 16, "Provides shelter and rest for foreigners", 0.75f));
		
		laboringTextures.add(new TextureElement(FARM_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 120), 16, 16, "Plants and harvests various types of crops", 0.75f));
		laboringTextures.add(new TextureElement(GROVE_TEXTURE, (backgroundPosX + 87), (backgroundPosY + 120), 16, 16, "Finds and cuts down trees for lumber", 0.75f));
		laboringTextures.add(new TextureElement(MINE_TEXTURE, (backgroundPosX + 167), (backgroundPosY + 120), 16, 16, "DIGGY DIGGY HOLE, I'M DIGGING A HOLE", 0.75f));
		laboringTextures.add(new TextureElement(FISHERY_TEXTURE, (backgroundPosX + 87), (backgroundPosY + 150), 16, 16, "Catches fish and oddities found in the water", 0.75f));
		laboringTextures.add(new TextureElement(FORAGINGLODGE_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 150), 16, 16, "Searches for seeds, fruits, and vegetables", 0.75f));
		
		craftingTextures.add(new TextureElement(ALCHEMYLAB_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 120), 16, 16, "Sells alchemy ingredients and potions", 0.75f));
		craftingTextures.add(new TextureElement(ARCANUM_TEXTURE, (backgroundPosX + 87), (backgroundPosY + 120), 16, 16, "Sells enchanting items and gear", 0.75f));
		craftingTextures.add(new TextureElement(FORGESMITH_TEXTURE, (backgroundPosX + 167), (backgroundPosY + 120), 16, 16, "Sells tools, armor, and weapons", 0.75f));
		craftingTextures.add(new TextureElement(CARTOGRAPHY_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 150), 16, 16, "Creates empty and treasure maps", 0.75f));
		craftingTextures.add(new TextureElement(FLETCHERY_TEXTURE, (backgroundPosX + 87), (backgroundPosY + 150), 16, 16, "Sells bows, arrows, and fletching items", 0.75f));
		craftingTextures.add(new TextureElement(TANNERY_TEXTURE, (backgroundPosX + 167), (backgroundPosY + 150), 16, 16, "Sells leather and stable items", 0.75f));	
		
		ranchingTextures.add(new TextureElement(APIARY_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 120), 16, 16, "OH GOD NO, NOT THE BEES", 0.75f));
		ranchingTextures.add(new TextureElement(COWBARN_TEXTURE, (backgroundPosX + 87), (backgroundPosY + 120), 16, 16, "Raises cows for their meat and leather", 0.75f));
		ranchingTextures.add(new TextureElement(CHICKENCOOP_TEXTURE, (backgroundPosX + 167), (backgroundPosY + 120), 16, 16, "Raises chickens for their meat and eggs", 0.75f));
		ranchingTextures.add(new TextureElement(SHEEPPASTURE_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 150), 16, 16, "Raises sheep for their wool", 0.75f));
		ranchingTextures.add(new TextureElement(STABLE_TEXTURE, (backgroundPosX + 87), (backgroundPosY + 150), 16, 16, "Raises donkeys, mules, horses, and wolves", 0.75f));
		ranchingTextures.add(new TextureElement(PIGPEN_TEXTURE, (backgroundPosX + 167), (backgroundPosY + 150), 16, 16, "Raises pigs for their meat", 0.75f));
		
		artisanTextures.add(new TextureElement(BAKERY_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 108), 16, 24, "Bakes bread, cookies, and cakes", 1.0f));
		artisanTextures.add(new TextureElement(ABATTOIR_TEXTURE, (backgroundPosX + 87), (backgroundPosY + 120), 16, 16, "Cooks a variety of meat and soups", 0.75f));
		artisanTextures.add(new TextureElement(GREENGROCERY_TEXTURE, (backgroundPosX + 167), (backgroundPosY + 120), 16, 16, "Prepares fresh fruits and vegetables", 0.75f));
		artisanTextures.add(new TextureElement(WOODSHOP_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 150), 16, 16, "Sells a variety of wooden items and blocks", 0.75f));
		artisanTextures.add(new TextureElement(MASONRY_TEXTURE, (backgroundPosX + 87), (backgroundPosY + 150), 16, 16, "Sells a variety of stone blocks and brick", 0.75f));
		
		customsTextures.add(new TextureElement(MARKETPLACE_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 120), 16, 16, "Bakes bread, cookies, and cakes", 0.75f));
		customsTextures.add(new TextureElement(TAVERN_TEXTURE, (backgroundPosX + 87), (backgroundPosY + 120), 16, 16, "Cooks a variety of meat and soups", 0.75f));
		
		miscTextures.add(new TextureElement(CHURCH_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 120), 16, 16, "Bakes bread, cookies, and cakes", 0.75f));
		miscTextures.add(new TextureElement(CEMETERY_TEXTURE, (backgroundPosX + 87), (backgroundPosY + 120), 16, 16, "Prepares fresh fruits and vegetables", 0.75f));
		miscTextures.add(new TextureElement(LIBRARY_TEXTURE, (backgroundPosX + 167), (backgroundPosY + 120), 16, 16, "Cooks a variety of meat and soups", 0.75f));
		miscTextures.add(new TextureElement(WELL_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 150), 32, 16, "Sells a variety of wooden items and blocks", 0.75f));
		miscTextures.add(new TextureElement(FOUNTAIN_TEXTURE, (backgroundPosX + 87), (backgroundPosY + 150), 16, 16, "Sells a variety of stone blocks and brick", 0.75f));
	}

	private void initializeMainPageButtons()
	{
		constructButton.active = false;
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
	
	private void initializeResorucesPageButtons()
	{
		resourcesButton.active = false;
	}
	
	private void initializeUpgradePageButtons()
	{
		upgradeButton.active = false;
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
        }).dimensions(backgroundPosX + 180, backgroundPosY + 70, 45, 20).build();
    }

	private void setupCorePage()
	{
		titleText1 = Text.literal("These structures make up the foundation of your settlement.");
		titleText2 = Text.literal("Your architect resides in your townhall and the warehouse is where your resources are stored.");
		
		townhallButton = ButtonWidget.builder(Text.literal("Town Hall"), button ->
		{
			build = Build.TOWNHALL;
			priceText = Build.TOWNHALL.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		warehouseButton = ButtonWidget.builder(Text.literal("Warehouse"), button ->
		{
			build = Build.WAREHOUSE;
			priceText = Build.WAREHOUSE.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		houseButton = ButtonWidget.builder(Text.literal("House"), button ->
		{
			build = Build.HOUSE;
			priceText = Build.HOUSE.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		roadButton = ButtonWidget.builder(Text.literal("Road"), button ->
		{
			build = Build.ROAD;
			priceText = Build.ROAD.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 155, 65, 20).build();
		
		bridgeButton = ButtonWidget.builder(Text.literal("Bridge"), button ->
		{
			build = Build.BRIDGE;
			priceText = Build.BRIDGE.getText();
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
			priceText = Build.BARRACKS.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		watchTowerButton = ButtonWidget.builder(Text.literal("Watch Tower"), button ->
		{
			build = Build.WATCHTOWER;
			priceText = Build.WATCHTOWER.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		bountyHallButton = ButtonWidget.builder(Text.literal("Bounty Hall"), button ->
		{
			build = Build.BOUNTYHALL;
			priceText = Build.BOUNTYHALL.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		wallButton = ButtonWidget.builder(Text.literal("Wall"), button ->
		{
			build = Build.WALL;
			priceText = Build.WALL.getText();
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
			priceText = Build.FARM.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		groveButton = ButtonWidget.builder(Text.literal("Grove"), button ->
		{
			build = Build.GROVE;
			priceText = Build.GROVE.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		mineButton = ButtonWidget.builder(Text.literal("Mine"), button ->
		{
			build = Build.MINE;
			priceText = Build.MINE.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		fisheryButton = ButtonWidget.builder(Text.literal("Fishery"), button ->
		{
			build = Build.FISHERY;
			priceText = Build.FISHERY.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 155, 65, 20).build();
		
		foragingLodgeButton = ButtonWidget.builder(Text.literal("Foraging Lodge"), button ->
		{
			build = Build.FORAGINGLODGE;
			priceText = Build.FORAGINGLODGE.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 155, 65, 20).build();
		
		addDrawableChild(farmButton);
		addDrawableChild(fisheryButton);
		addDrawableChild(foragingLodgeButton);
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
			priceText = Build.ALCHEMYLAB.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		arcanumButton = ButtonWidget.builder(Text.literal("Arcanum"), button ->
		{
			build = Build.ARCANUM;
			priceText = Build.ARCANUM.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		smithForgeButton = ButtonWidget.builder(Text.literal("Forge & Smith"), button ->
		{
			build = Build.SMITHFORGE;
			priceText = Build.SMITHFORGE.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		cartographyButton = ButtonWidget.builder(Text.literal("Cartography"), button ->
		{
			build = Build.CARTOGRAPHY;
			priceText = Build.CARTOGRAPHY.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 155, 65, 20).build();
		
		fletcheryButton = ButtonWidget.builder(Text.literal("Fletchery"), button ->
		{
			build = Build.FLETCHERY;
			priceText = Build.FLETCHERY.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 155, 65, 20).build();

		tanneryButton = ButtonWidget.builder(Text.literal("Tannery"), button ->
		{
			build = Build.TANNERY;
			priceText = Build.TANNERY.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 155, 65, 20).build();
		
		addDrawableChild(alchemyLabButton);
		addDrawableChild(arcanumButton);
		addDrawableChild(smithForgeButton);
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
			priceText = Build.APIARY.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		cowBarnButton = ButtonWidget.builder(Text.literal("Cow Barn"), button ->
		{
			build = Build.COWBARN;
			priceText = Build.COWBARN.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		chickenCoopButton = ButtonWidget.builder(Text.literal("Chicken Coop"), button ->
		{
			build = Build.CHICKENCOOP;
			priceText = Build.CHICKENCOOP.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		sheepPastureButton = ButtonWidget.builder(Text.literal("Sheep Pasture"), button ->
		{
			build = Build.SHEEPPASTURE;
			priceText = Build.SHEEPPASTURE.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 155, 65, 20).build();
		
		stableButton = ButtonWidget.builder(Text.literal("Stable"), button ->
		{
			build = Build.STABLE;
			priceText = Build.STABLE.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 155, 65, 20).build();
		
		pigPenButton = ButtonWidget.builder(Text.literal("Pig Pen"), button ->
		{
			build = Build.PIGPEN;
			priceText = Build.PIGPEN.getText();
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
		titleText1 = Text.literal("Artisanal structures will provide fresh produce, food, and lavish building materials.");
		titleText2 = Text.literal("Every human in your settlement needs to eat, why not fine dine within extravagant halls?");
		
		bakeryButton = ButtonWidget.builder(Text.literal("Bakery"), button ->
		{
			build = Build.BAKERY;
			priceText = Build.BAKERY.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		abattoirButton = ButtonWidget.builder(Text.literal("Abattoir"), button ->
		{
			build = Build.ABATTOIR;
			priceText = Build.ABATTOIR.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		greengroceryButton = ButtonWidget.builder(Text.literal("Greengrocery"), button ->
		{
			build = Build.GREENGROCERY;
			priceText = Build.GREENGROCERY.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		woodshopButton = ButtonWidget.builder(Text.literal("Woodshop"), button ->
		{
			build = Build.WOODSHOP;
			priceText = Build.WOODSHOP.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 155, 65, 20).build();
		
		masonryButton = ButtonWidget.builder(Text.literal("Masonry"), button ->
		{
			build = Build.MASONRY;
			priceText = Build.MASONRY.getText();
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
			priceText = Build.MARKETPLACE.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		tavernButton = ButtonWidget.builder(Text.literal("Tavern"), button ->
		{
			build = Build.TAVERN;
			priceText = Build.TAVERN.getText();
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
			priceText = Build.CHURCH.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		cemeteryButton = ButtonWidget.builder(Text.literal("Cemetery"), button ->
		{
			build = Build.CEMETERY;
			priceText = Build.CEMETERY.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		libraryButton = ButtonWidget.builder(Text.literal("Library"), button ->
		{
			build = Build.LIBRARY;
			priceText = Build.LIBRARY.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		wellButton = ButtonWidget.builder(Text.literal("Well"), button ->
		{
			build = Build.WELL;
			priceText = Build.WELL.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 155, 65, 20).build();
		
		fountainButton = ButtonWidget.builder(Text.literal("Fountain"), button ->
		{
			build = Build.FOUNTAIN;
			priceText = Build.FOUNTAIN.getText();
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
		this.renderBackground(context);
		context.drawTexture(BACKGROUND_TEXTURE, backgroundPosX, backgroundPosY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
		super.render(context, mouseX, mouseY, delta);

		if (page == Page.CONSTRUCTION || page == Page.RESOURCES || page == Page.UPGRADE)
			buildButton.visible = false;
		else
			buildButton.visible = true;
		
		if (build != Build.NONE && this.playerEmeralds >= build.getValue())
			buildButton.active = true;
		else
			buildButton.active = false;
		
		if (page != Page.CONSTRUCTION && page != Page.RESOURCES && page != Page.UPGRADE)
		{
			TextWrapper.render(context, this.textRenderer, titleText1, backgroundPosX + 14, backgroundPosY + 16, new Color(255, 255, 255).getRGB(), 145);
			TextWrapper.render(context, this.textRenderer, titleText2, backgroundPosX + 14, backgroundPosY + 62, new Color(255, 255, 255).getRGB(), 145);
			context.drawText(this.textRenderer, priceTitle, (backgroundPosX + 170), (backgroundPosY + 16), new Color(255, 255, 255).getRGB(), true);
			context.drawTexture(SEPARATOR_TEXTURE, (backgroundPosX + 10), (backgroundPosY + 110), 0, 0, 225, 2, 32, 2);
			context.drawTexture(HANGINGSIGN_TEXTURE, (backgroundPosX + 175), (backgroundPosY + 28), 0, 0, 54, 32, 54, 32);
			context.drawTexture(EMERALD_TEXTURE, (backgroundPosX + 207), (backgroundPosY + 43), 0, 0, 14, 14, 14, 14);
		}
		
		if (page == Page.CONSTRUCTION)
		{
			TextWrapper.render(context, this.textRenderer, constructText1, backgroundPosX + 14, backgroundPosY + 16, new Color(255, 255, 255).getRGB(), 145);
			TextWrapper.render(context, this.textRenderer, constructText2, backgroundPosX + 14, backgroundPosY + 62, new Color(255, 255, 255).getRGB(), 145);
		}
		
		if (page == Page.RESOURCES)
		{
			TextWrapper.render(context, this.textRenderer, resourcesText1, backgroundPosX + 14, backgroundPosY + 16, new Color(255, 255, 255).getRGB(), 145);
			TextWrapper.render(context, this.textRenderer, resourcesText2, backgroundPosX + 14, backgroundPosY + 62, new Color(255, 255, 255).getRGB(), 145);
		}
		
		if (page == Page.UPGRADE)
		{
			TextWrapper.render(context, this.textRenderer, upgradeText1, backgroundPosX + 14, backgroundPosY + 16, new Color(255, 255, 255).getRGB(), 145);
			TextWrapper.render(context, this.textRenderer, upgradeText2, backgroundPosX + 14, backgroundPosY + 62, new Color(255, 255, 255).getRGB(), 145);
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
		for (TextureElement element : resourcesTextures)
		{
			element.draw(context);
			if (element.isMouseOver(mouseX, mouseY))
				TextUtil.renderTooltip(context, this.textRenderer, element.getToolTip(), mouseX, mouseY);
		}
	}
	
	private void renderUpgradePage(DrawContext context, int mouseX, int mouseY)
	{
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
		    priceText = getFormattedPriceText(Build.TOWNHALL);
		else if (warehouseButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.WAREHOUSE);
		else if (houseButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.HOUSE);
		else if (roadButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.ROAD);
		else if (bridgeButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.BRIDGE);
		else
		    priceText = build.getText();

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
		    priceText = getFormattedPriceText(Build.BARRACKS);
		else if (watchTowerButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.WATCHTOWER);
		else if (bountyHallButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.BOUNTYHALL);
		else if (wallButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.WALL);
		else
			priceText = build.getText();

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
		foragingLodgeButton.active = (build != Build.FORAGINGLODGE);
		groveButton.active = (build != Build.GROVE);
		mineButton.active = (build != Build.MINE);

		if (farmButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.FARM);
		else if (fisheryButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.FISHERY);
		else if (foragingLodgeButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.FORAGINGLODGE);
		else if (groveButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.GROVE);
		else if (mineButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.MINE);
		else
			priceText = build.getText();

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
		smithForgeButton.active = (build != Build.SMITHFORGE);
		cartographyButton.active = (build != Build.CARTOGRAPHY);
		fletcheryButton.active = (build != Build.FLETCHERY);
		tanneryButton.active = (build != Build.TANNERY);

		if (alchemyLabButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.ALCHEMYLAB);
		else if (arcanumButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.ARCANUM);
		else if (smithForgeButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.SMITHFORGE);
		else if (cartographyButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.CARTOGRAPHY);
		else if (fletcheryButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.FLETCHERY);
		else if (tanneryButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.TANNERY);
		else
			priceText = build.getText();

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
		    priceText = getFormattedPriceText(Build.APIARY);
		else if (cowBarnButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.COWBARN);
		else if (chickenCoopButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.CHICKENCOOP);
		else if (sheepPastureButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.SHEEPPASTURE);
		else if (stableButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.STABLE);
		else if (pigPenButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.PIGPEN);
		else
			priceText = build.getText();

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
		    priceText = getFormattedPriceText(Build.BAKERY);
		else if (abattoirButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.ABATTOIR);
		else if (greengroceryButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.GREENGROCERY);
		else if (woodshopButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.WOODSHOP);
		else if (masonryButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.MASONRY);
		else
			priceText = build.getText();

		for (TextureElement element : artisanTextures)
		{
			element.draw(context);
			if (element.isMouseOver(mouseX, mouseY))
				TextUtil.renderTooltip(context, this.textRenderer, element.getToolTip(), mouseX, mouseY);
		}

		if (this.playerEmeralds < build.getValue())
			TextUtil.drawTextR(context, this.textRenderer, priceText, backgroundPosX + 201, backgroundPosY + 46, new Color(235, 50, 30).getRGB(), true);
		else
			TextUtil.drawTextR(context, this.textRenderer, priceText, backgroundPosX + 201, backgroundPosY + 46, new Color(255, 255, 255).getRGB(), true);
    }
    
    private void renderCustomsPage(DrawContext context, int mouseX, int mouseY)
    {
		marketplaceButton.active = (build != Build.MARKETPLACE);
		tavernButton.active = (build != Build.TAVERN);

		if (marketplaceButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.MARKETPLACE);
		else if (tavernButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.TAVERN);
		else
			priceText = build.getText();

		for (TextureElement element : customsTextures)
		{
			element.draw(context);
			if (element.isMouseOver(mouseX, mouseY))
				TextUtil.renderTooltip(context, this.textRenderer, element.getToolTip(), mouseX, mouseY);
		}

		if (this.playerEmeralds < build.getValue())
			TextUtil.drawTextR(context, this.textRenderer, priceText, backgroundPosX + 201, backgroundPosY + 46, new Color(235, 50, 30).getRGB(), true);
		else
			TextUtil.drawTextR(context, this.textRenderer, priceText, backgroundPosX + 201, backgroundPosY + 46, new Color(255, 255, 255).getRGB(), true);
    }
    
    private void renderMiscPage(DrawContext context, int mouseX, int mouseY)
    {
		churchButton.active = (build != Build.CHURCH);
		libraryButton.active = (build != Build.LIBRARY);
		cemeteryButton.active = (build != Build.CEMETERY);
		wellButton.active = (build != Build.WELL);
		fountainButton.active = (build != Build.FOUNTAIN);

		if (churchButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.CHURCH);
		else if (libraryButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.LIBRARY);
		else if (cemeteryButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.CEMETERY);
		else if (wellButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.WELL);
		else if (fountainButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Build.FOUNTAIN);
		else
			priceText = build.getText();

		for (TextureElement element : miscTextures)
		{
			element.draw(context);
			if (element.isMouseOver(mouseX, mouseY))
				TextUtil.renderTooltip(context, this.textRenderer, element.getToolTip(), mouseX, mouseY);
		}

		if (this.playerEmeralds < build.getValue())
			TextUtil.drawTextR(context, this.textRenderer, priceText, backgroundPosX + 201, backgroundPosY + 46, new Color(235, 50, 30).getRGB(), true);
		else
			TextUtil.drawTextR(context, this.textRenderer, priceText, backgroundPosX + 201, backgroundPosY + 46, new Color(255, 255, 255).getRGB(), true);
    }

    private  void drawPriceText(DrawContext context)
    {
    	if (this.playerEmeralds < build.getValue())
			TextUtil.drawTextR(context, this.textRenderer, priceText, backgroundPosX + 201, backgroundPosY + 46, new Color(235, 50, 30).getRGB(), true);
		else
			TextUtil.drawTextR(context, this.textRenderer, priceText, backgroundPosX + 201, backgroundPosY + 46, new Color(255, 255, 255).getRGB(), true);
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