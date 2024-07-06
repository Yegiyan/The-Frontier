package com.frontier.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.frontier.Frontier;
import com.frontier.PlayerData;
import com.frontier.entities.SettlerEntity;
import com.frontier.gui.util.TextWrapper;
import com.frontier.gui.util.TextureElement;
import com.frontier.network.FrontierPackets;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class HireSettlerScreen extends Screen
{
    // textures
    private static final Identifier BACKGROUND_TEXTURE = new Identifier("minecraft", "textures/gui/demo_background.png");
    private static final Identifier NAMEPLATE_TEXTURE = new Identifier("minecraft", "textures/gui/social_interactions.png");
    private static final Identifier SEPARATOR_TEXTURE = new Identifier("minecraft", "textures/gui/header_separator.png");
    private static final Identifier BARS_TEXTURE = new Identifier("minecraft", "textures/gui/bars.png");
    private static final Identifier ICONS_TEXTURE = new Identifier("minecraft", "textures/gui/icons.png");
    private static final Identifier HANGINGSIGN_TEXTURE = new Identifier("minecraft", "textures/gui/hanging_signs/spruce.png");
    private static final Identifier EMERALD_TEXTURE = new Identifier("minecraft", "textures/item/emerald.png");
    
    private static final Identifier ARCHITECT_TEXTURE = new Identifier("minecraft", "textures/item/clock_48.png");
    private static final Identifier DELIVERER_TEXTURE = new Identifier("minecraft", "textures/item/written_book.png");
    private static final Identifier COURIER_TEXTURE = new Identifier("minecraft", "textures/item/chest_minecart.png");
    private static final Identifier PRIEST_TEXTURE = new Identifier("minecraft", "textures/item/light.png");
    private static final Identifier INNKEEPER_TEXTURE = new Identifier("minecraft", "textures/item/candle.png");
    
    private static final Identifier ARCHER_TEXTURE = new Identifier("minecraft", "textures/item/bow_pulling_2.png");
    private static final Identifier KNIGHT_TEXTURE = new Identifier("minecraft", "textures/item/iron_sword.png");
    private static final Identifier CLERIC_TEXTURE = new Identifier("minecraft", "textures/mob_effect/health_boost.png");
    
    private static final Identifier FARMER_TEXTURE = new Identifier("minecraft", "textures/item/iron_hoe.png");
    private static final Identifier MINER_TEXTURE = new Identifier("minecraft", "textures/item/iron_pickaxe.png");
    private static final Identifier LUMBERJACK_TEXTURE = new Identifier("minecraft", "textures/item/iron_axe.png");
    private static final Identifier FISHERMAN_TEXTURE = new Identifier("minecraft", "textures/item/fishing_rod.png");
    
    private static final Identifier ALCHEMIST_TEXTURE = new Identifier("minecraft", "textures/item/brewing_stand.png");
    private static final Identifier BLACKSMITH_TEXTURE = new Identifier("minecraft", "textures/item/netherite_scrap.png");
    private static final Identifier FLETCHER_TEXTURE = new Identifier("minecraft", "textures/item/arrow.png");
    private static final Identifier MASON_TEXTURE = new Identifier("minecraft", "textures/item/brick.png");
    private static final Identifier CARPENTER_TEXTURE = new Identifier("minecraft", "textures/item/brush.png");
    private static final Identifier CARTOGRAPHER_TEXTURE = new Identifier("minecraft", "textures/item/filled_map.png");
    
    private static final Identifier BEEKEEPER_TEXTURE = new Identifier("minecraft", "textures/item/honey_bottle.png");
    private static final Identifier POULTRYMAN_TEXTURE = new Identifier("minecraft", "textures/item/chicken.png");
    private static final Identifier COWHAND_TEXTURE = new Identifier("minecraft", "textures/item/beef.png");
    private static final Identifier SWINEHERD_TEXTURE = new Identifier("minecraft", "textures/item/porkchop.png");
    private static final Identifier SHEPHERD_TEXTURE = new Identifier("minecraft", "textures/item/mutton.png");
    private static final Identifier STABLEHAND_TEXTURE = new Identifier("minecraft", "textures/item/saddle.png");
    
    private static final Identifier BAKER_TEXTURE = new Identifier("minecraft", "textures/item/cake.png");
    private static final Identifier COOK_TEXTURE = new Identifier("minecraft", "textures/item/beetroot_soup.png");
    private static final Identifier ARCANIST_TEXTURE = new Identifier("minecraft", "textures/item/amethyst_shard.png");
    private static final Identifier MERCHANT_TEXTURE = new Identifier("minecraft", "textures/item/bundle_filled.png");
    private static final Identifier TANNER_TEXTURE = new Identifier("minecraft", "textures/item/leather.png");
    
    // textures lists
    private List<TextureElement> mainTextures = new ArrayList<>();
    private List<TextureElement> barTextures = new ArrayList<>();
    private List<TextureElement> governingTextures = new ArrayList<>();
    private List<TextureElement> militaryTextures = new ArrayList<>();
    private List<TextureElement> harvestingTextures = new ArrayList<>();
    private List<TextureElement> craftingTextures = new ArrayList<>();
    private List<TextureElement> ranchingTextures = new ArrayList<>();
    private List<TextureElement> tradingTextures = new ArrayList<>();
    
    
    // UI & background dimensions
    public static final int UI_OFFSET_X = 0;
    public static final int UI_OFFSET_Y = 60;
    
    public static final int BACKGROUND_WIDTH = 256;
    public static final int BACKGROUND_HEIGHT = 285;
    private int backgroundPosX;
    private int backgroundPosY;
    
    // page and hire enums
    public enum Page { MAIN, GOVERNING, MILITARY, HARVESTING, CRAFTING, RANCHING, TRADING }
    private Page page;
    
    public enum Hire
    {
        NONE(0), ARCHITECT(2), DELIVERER(3), COURIER(4), PRIEST(6), INNKEEPER(12),
        ARCHER(4), KNIGHT(6), CLERIC(10),
        FARMER(4), MINER(8), LUMBERJACK(4), FISHERMAN(5),
        ALCHEMIST(12), BLACKSMITH(8), FLETCHER(6), MASON(10), CARPENTER(8), CARTOGRAPHER(10),
        BEEKEEPER(8), POULTRYMAN(6), COWHAND(4), SWINEHERD(4), SHEPHERD(8), STABLEHAND(14), 
        BAKER(6), COOK(8), ARCANIST(10), TANNER(8), MERCHANT(10);

        private int value;
        Hire(int value) { this.value = value; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
        public MutableText getText() { return Text.literal(String.valueOf(value)); }
    }
    private Hire hire;
    
    // buttons
    private ButtonWidget infoButton;
    
    private ButtonWidget governingButton;
    private ButtonWidget militiaButton;
    private ButtonWidget harvestingButton;
    private ButtonWidget craftingButton;
    private ButtonWidget ranchingButton;
    private ButtonWidget tradingButton;
    
    private ButtonWidget hireButton;
    private ButtonWidget hireArchitectButton;
    private ButtonWidget hireCourierButton;
    private ButtonWidget hireDelivererButton;
    private ButtonWidget hirePriestButton;
    private ButtonWidget hireInnkeeperButton;
    
    private ButtonWidget hireFarmerButton;
    private ButtonWidget hireMinerButton;
    private ButtonWidget hireLumberjackButton;
    private ButtonWidget hireFishermanButton;
    
    private ButtonWidget hireAlchemistButton;
    private ButtonWidget hireBlacksmithButton;
    private ButtonWidget hireFletcherButton;
    private ButtonWidget hireMasonButton;
    private ButtonWidget hireCarpenterButton;
    private ButtonWidget hireCartographerButton;
    
    private ButtonWidget hireBeekeeperButton;
    private ButtonWidget hirePoultrymanButton;
    private ButtonWidget hireCowhandButton;
    private ButtonWidget hireSwineherdButton;
    private ButtonWidget hireShepherdButton;
    private ButtonWidget hireStablehandButton;
    
    private ButtonWidget hireBakerButton;
    private ButtonWidget hireCookButton;
    private ButtonWidget hireArcanistButton;
    private ButtonWidget hireMerchantButton;
    private ButtonWidget hireTannerButton;
    
    private ButtonWidget hireArcherButton;
    private ButtonWidget hireKnightButton;
    private ButtonWidget hireClericButton;
    
    // texts
    private Text priceText;
    private Text hireTitle;
    private Text hireText1;
    private Text hireText2;
    
    private Text nameText;
    private Text expertiseText;
    
    private Text healthText;
    private Text hungerText;
    private Text moraleText;
    private Text skillText;

    // settler vars
    private SettlerEntity settler;
    private Identifier skinTexture;
    private static Identifier expertiseTexture;
    private String expertise;
    
    PlayerEntity player;
    int playerEmeralds;
    
	public HireSettlerScreen(SettlerEntity settler)
	{
		super(Text.literal("Hire a Settler Screen"));
		this.settler = settler;
		this.expertise = settler.getSettlerExpertise();
		this.page = Page.MAIN;
		setExpertiseTexture(expertise);
		
		this.player = MinecraftClient.getInstance().player;
		this.playerEmeralds = 0;
	}

	@Override
	protected void init()
	{
		backgroundPosX = ((this.width - BACKGROUND_WIDTH) / 2) + UI_OFFSET_X;
		backgroundPosY = ((this.height - BACKGROUND_HEIGHT) / 2) + UI_OFFSET_Y;
		
		nameText = Text.literal(settler.getSettlerName());
		expertiseText = Text.literal(capitalize(expertise));

		healthText = Text.literal(String.format("%.0f", (settler.getHealth() / 20.0f) * 100));
		hungerText = Text.literal(String.valueOf(settler.getSettlerHunger()));
		moraleText = Text.literal(String.valueOf(settler.getSettlerMorale()));
		skillText = Text.literal(String.valueOf(settler.getSettlerSkill()));

		hireTitle = Text.literal("Hiring Cost");
		priceText = Text.literal("0");
		
		updateButtons();
	}

	private void updateButtons()
	{
		this.clearChildren();
		initializeInfoButton();
		
		if (this.player != null)
			this.playerEmeralds = countEmeralds(this.player);
		
		switch (page)
		{
			case MAIN:
				setupMainPage();
				break;
			case GOVERNING:
				setupGoverningPage();
				break;
			case MILITARY:
				setupMilitaryPage();
				break;
			case HARVESTING:
				setupHarvestingPage();
				break;
			case CRAFTING:
				setupCraftingPage();
				break;
			case RANCHING:
				setupRanchingPage();
				break;
			case TRADING:
				setupTradingPage();
				break;
			default:
				break;
		}

		if (hire == Hire.NONE)
			priceText = Text.literal("0");

		initializeHireButton();
        addDrawableChild(hireButton);
		addDrawableChild(infoButton);
	}

	private void initializeInfoButton()
	{
		infoButton = ButtonWidget.builder(Text.literal("Nomad's Info"), button ->
		{
			page = Page.MAIN;
			updateButtons();
		}).dimensions(backgroundPosX + 0, backgroundPosY - 25, 80, 20).build();
		infoButton.active = true;
	}

	private void setupMainPage()
	{
		hire = Hire.NONE;
		setupTextures();
		setupBarTextures();
		setupPortrait();
		initializeMainPageButtons();
		addMainPageButtons();
	}

	private void setupTextures()
	{
		barTextures.add(new TextureElement(NAMEPLATE_TEXTURE, (backgroundPosX + 5), (backgroundPosY + 8), 238, 36, 0, 0, 256, 256, null, 1.0f));
		mainTextures.add(new TextureElement(expertiseTexture, (backgroundPosX + 214), (backgroundPosY + 18), 12, 12, "Expertise", 1.2f));

		militaryTextures.add(new TextureElement(ARCHER_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 120), 16, 16, "Ranged fighter that wears leather armor", 1.0f));
		militaryTextures.add(new TextureElement(KNIGHT_TEXTURE, (backgroundPosX + 87), (backgroundPosY + 120), 16, 16, "Melee fighter that wears leather or heavy armor", 1.0f));
		militaryTextures.add(new TextureElement(CLERIC_TEXTURE, (backgroundPosX + 167), (backgroundPosY + 120), 16, 16, "Armored medic that heals allies in and out of combat", 1.0f));
		
		governingTextures.add(new TextureElement(ARCHITECT_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 120), 16, 16, "Uses settlement supplies to construct buildings", 1.0f));
		governingTextures.add(new TextureElement(DELIVERER_TEXTURE, (backgroundPosX + 87), (backgroundPosY + 120), 16, 16, "Disperses settlement supplies to other settlers", 1.0f));
		governingTextures.add(new TextureElement(PRIEST_TEXTURE, (backgroundPosX + 167), (backgroundPosY + 120), 16, 16, "Tends to the cemetary and provides decursing services", 1.0f));
		governingTextures.add(new TextureElement(COURIER_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 150), 16, 16, "Sends your requests or demands to other settlements", 1.0f));
		governingTextures.add(new TextureElement(INNKEEPER_TEXTURE, (backgroundPosX + 87), (backgroundPosY + 150), 16, 16, "Provides shelter and rest for foreigners", 1.0f));
		
		harvestingTextures.add(new TextureElement(FARMER_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 120), 16, 16, "Plants and harvests various types of crops", 1.0f));
		harvestingTextures.add(new TextureElement(MINER_TEXTURE, (backgroundPosX + 87), (backgroundPosY + 120), 16, 16, "DIGGY DIGGY HOLE, I'M DIGGING A HOLE", 1.0f));
		harvestingTextures.add(new TextureElement(LUMBERJACK_TEXTURE, (backgroundPosX + 167), (backgroundPosY + 120), 16, 16, "Finds and cuts down trees for lumber", 1.0f));
		harvestingTextures.add(new TextureElement(FISHERMAN_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 150), 16, 16, "Catches fish and oddities found in the water", 1.0f));
		
		craftingTextures.add(new TextureElement(ALCHEMIST_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 120), 16, 16, "Sells alchemy ingredients and potions", 1.0f));
		craftingTextures.add(new TextureElement(BLACKSMITH_TEXTURE, (backgroundPosX + 87), (backgroundPosY + 120), 16, 16, "Sells tools, armor, and weapons", 1.0f));
		craftingTextures.add(new TextureElement(FLETCHER_TEXTURE, (backgroundPosX + 167), (backgroundPosY + 120), 16, 16, "Sells bows, arrows, and fletching items", 1.0f));
		craftingTextures.add(new TextureElement(MASON_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 150), 16, 16, "Sells a variety of stone blocks and brick", 1.0f));
		craftingTextures.add(new TextureElement(CARPENTER_TEXTURE, (backgroundPosX + 87), (backgroundPosY + 150), 16, 16, "Sells a variety of wooden items and blocks", 1.0f));
		craftingTextures.add(new TextureElement(CARTOGRAPHER_TEXTURE, (backgroundPosX + 167), (backgroundPosY + 150), 16, 16, "Creates empty and treasure maps", 1.0f));
		
		ranchingTextures.add(new TextureElement(BEEKEEPER_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 120), 16, 16, "OH GOD NO, NOT THE BEES", 1.0f));
		ranchingTextures.add(new TextureElement(POULTRYMAN_TEXTURE, (backgroundPosX + 87), (backgroundPosY + 120), 16, 16, "Raises chickens for their meat and eggs", 1.0f));
		ranchingTextures.add(new TextureElement(COWHAND_TEXTURE, (backgroundPosX + 167), (backgroundPosY + 120), 16, 16, "Raises cows for their meat and leather", 1.0f));
		ranchingTextures.add(new TextureElement(SWINEHERD_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 150), 16, 16, "Raises pigs for their meat", 1.0f));
		ranchingTextures.add(new TextureElement(SHEPHERD_TEXTURE, (backgroundPosX + 87), (backgroundPosY + 150), 16, 16, "Raises sheep for their wool", 1.0f));
		ranchingTextures.add(new TextureElement(STABLEHAND_TEXTURE, (backgroundPosX + 167), (backgroundPosY + 150), 16, 16, "Raises donkeys, mules, horses, and wolves", 1.0f));
		
		tradingTextures.add(new TextureElement(BAKER_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 120), 16, 16, "Bakes bread, cookies, and cakes", 1.0f));
		tradingTextures.add(new TextureElement(COOK_TEXTURE, (backgroundPosX + 87), (backgroundPosY + 120), 16, 16, "Cooks a variety of meat and soups", 1.0f));
		tradingTextures.add(new TextureElement(ARCANIST_TEXTURE, (backgroundPosX + 167), (backgroundPosY + 120), 16, 16, "Sells enchanting items and gear", 1.0f));
		tradingTextures.add(new TextureElement(MERCHANT_TEXTURE, (backgroundPosX + 7), (backgroundPosY + 150), 16, 16, "Exports your settlement goods to foreigners", 1.0f));
		tradingTextures.add(new TextureElement(TANNER_TEXTURE, (backgroundPosX + 87), (backgroundPosY + 150), 16, 16, "Sells leather and stable items", 1.0f));
	}

	private void setupBarTextures()
	{
		barTextures.add(new TextureElement(ICONS_TEXTURE, (backgroundPosX + 14), (backgroundPosY + 51), 9, 9, 53, 0, 256, 256, "Health (" + (int) settler.getHealth() + " / 20)", 1.0f));
		barTextures.add(new TextureElement(ICONS_TEXTURE, (backgroundPosX + 14), (backgroundPosY + 64), 9, 8, 53, 28, 256, 256, "Hunger", 1.0f));
		barTextures.add(new TextureElement(ICONS_TEXTURE, (backgroundPosX + 14), (backgroundPosY + 75), 9, 9, 161, 0, 256, 256, "Morale", 1.0f));
		barTextures.add(new TextureElement(ICONS_TEXTURE, (backgroundPosX + 14), (backgroundPosY + 87), 9, 9, 89, 0, 256, 256, "Skill (in field of expertise)", 1.0f));
	}

	private void setupPortrait()
	{
		boolean isMale = settler.getSettlerGender().equals("Male");
		Identifier[] textures = settler.getTextures(isMale);
		int textureIndex = settler.getSettlerTexture();

		if (textures != null && textures.length > 0)
			skinTexture = textures[textureIndex];
		else
			skinTexture = DefaultSkinHelper.getTexture(settler.getUuid());
	}

	private void initializeMainPageButtons()
	{
		infoButton.active = false;
		governingButton = ButtonWidget.builder(Text.literal("Governing"), button ->
		{
			page = Page.GOVERNING;
			updateButtons();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		militiaButton = ButtonWidget.builder(Text.literal("Military"), button ->
		{
			page = Page.MILITARY;
			updateButtons();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 155, 65, 20).build();
		
		harvestingButton = ButtonWidget.builder(Text.literal("Harvesting"), button ->
		{
			page = Page.HARVESTING;
			updateButtons();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		craftingButton = ButtonWidget.builder(Text.literal("Crafting"), button ->
		{
			page = Page.CRAFTING;
			updateButtons();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 155, 65, 20).build();
		
		ranchingButton = ButtonWidget.builder(Text.literal("Ranching"), button ->
		{
			page = Page.RANCHING;
			updateButtons();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		tradingButton = ButtonWidget.builder(Text.literal("Trading"), button ->
		{
			page = Page.TRADING;
			updateButtons();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 155, 65, 20).build();
	}

	private void addMainPageButtons()
	{
		addDrawableChild(governingButton);
		addDrawableChild(militiaButton);
		addDrawableChild(harvestingButton);
		addDrawableChild(craftingButton);
		addDrawableChild(ranchingButton);
		addDrawableChild(tradingButton);
	}
	
	private void initializeHireButton()
	{
        hireButton = ButtonWidget.builder(Text.literal("Hire"), button ->
        {
            PlayerData playerData = PlayerData.map.get(this.player.getUuid());
            if (this.player != null && playerData != null && this.settler != null)
            {
                PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
                passedData.writeString(hire.toString());
                passedData.writeString(playerData.getFaction());
                passedData.writeString(settler.getSettlerName());
                passedData.writeString(settler.getSettlerGender());
                passedData.writeString(settler.getSettlerExpertise());
                passedData.writeInt(settler.getSettlerMorale());
                passedData.writeInt(settler.getSettlerSkill());
                passedData.writeUuid(settler.getUuid());
                passedData.writeBlockPos(settler.getBlockPos());
                passedData.writeInt(hire.getValue());
                ClientPlayNetworking.send(FrontierPackets.HIRE_SETTLER_ID, passedData);
                MinecraftClient.getInstance().setScreen(null);
            }
        }).dimensions(backgroundPosX + 180, backgroundPosY + 70, 45, 20).build();
    }

	private void setupGoverningPage()
	{
		hireText1 = Text.literal("Governing personnel will help you build and manage your settlement.");
		hireText2 = Text.literal("As the leader you can optionally fulfill the role of architects and deliverers.");
		
		hireArchitectButton = ButtonWidget.builder(Text.literal("Architect"), button ->
		{
			hire = Hire.ARCHITECT;
			priceText = Hire.ARCHITECT.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		hireDelivererButton = ButtonWidget.builder(Text.literal("Deliverer"), button ->
		{
			hire = Hire.DELIVERER;
			priceText = Hire.DELIVERER.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		hirePriestButton = ButtonWidget.builder(Text.literal("Priest"), button ->
		{
			hire = Hire.PRIEST;
			priceText = Hire.PRIEST.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		hireCourierButton = ButtonWidget.builder(Text.literal("Courier"), button ->
		{
			hire = Hire.COURIER;
			priceText = Hire.COURIER.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 155, 65, 20).build();
		
		hireInnkeeperButton = ButtonWidget.builder(Text.literal("Innkeeper"), button ->
		{
			hire = Hire.INNKEEPER;
			priceText = Hire.INNKEEPER.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 155, 65, 20).build();
		
		addDrawableChild(hireArchitectButton);
		addDrawableChild(hireDelivererButton);
		addDrawableChild(hireCourierButton);
		addDrawableChild(hirePriestButton);
		addDrawableChild(hireInnkeeperButton);
	}

	private void setupMilitaryPage()
	{
		hireText1 = Text.literal("Military units will guard, patrol, and defend your settlement and merchants.");
		hireText2 = Text.literal("They will also follow their leader and enact raids on your enemies. Make sure they are armed!");

		hireArcherButton = ButtonWidget.builder(Text.literal("Archer"), button ->
		{
			hire = Hire.ARCHER;
			priceText = Hire.ARCHER.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		hireKnightButton = ButtonWidget.builder(Text.literal("Knight"), button ->
		{
			hire = Hire.KNIGHT;
			priceText = Hire.KNIGHT.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		hireClericButton = ButtonWidget.builder(Text.literal("Cleric"), button ->
		{
			hire = Hire.CLERIC;
			priceText = Hire.CLERIC.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();

		addDrawableChild(hireArcherButton);
		addDrawableChild(hireKnightButton);
		addDrawableChild(hireClericButton);
	}
	
	private void setupHarvestingPage()
	{
		hireText1 = Text.literal("Harvesting workers are the backbone of your settlement.");
		hireText2 = Text.literal("They produce your wood, ores, crops, and seafood.");
		
		hireFarmerButton = ButtonWidget.builder(Text.literal("Farmer"), button ->
		{
			hire = Hire.FARMER;
			priceText = Hire.FARMER.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		hireMinerButton = ButtonWidget.builder(Text.literal("Miner"), button ->
		{
			hire = Hire.MINER;
			priceText = Hire.MINER.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		hireLumberjackButton = ButtonWidget.builder(Text.literal("Lumberjack"), button ->
		{
			hire = Hire.LUMBERJACK;
			priceText = Hire.LUMBERJACK.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		hireFishermanButton = ButtonWidget.builder(Text.literal("Fisherman"), button ->
		{
			hire = Hire.FISHERMAN;
			priceText = Hire.FISHERMAN.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 155, 65, 20).build();
		
		addDrawableChild(hireFarmerButton);
		addDrawableChild(hireMinerButton);
		addDrawableChild(hireLumberjackButton);
		addDrawableChild(hireFishermanButton);
	}
	
	private void setupCraftingPage()
	{
		hireText1 = Text.literal("Crafting settlers are responsible for creating your settlements gear and venture items.");
		hireText2 = Text.literal("These can range from armor to potions to building materials and more.");
		
		hireAlchemistButton = ButtonWidget.builder(Text.literal("Alchemist"), button ->
		{
			hire = Hire.ALCHEMIST;
			priceText = Hire.ALCHEMIST.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		hireBlacksmithButton = ButtonWidget.builder(Text.literal("Blacksmith"), button ->
		{
			hire = Hire.BLACKSMITH;
			priceText = Hire.BLACKSMITH.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		hireFletcherButton = ButtonWidget.builder(Text.literal("Fletcher"), button ->
		{
			hire = Hire.FLETCHER;
			priceText = Hire.FLETCHER.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		hireMasonButton = ButtonWidget.builder(Text.literal("Mason"), button ->
		{
			hire = Hire.MASON;
			priceText = Hire.MASON.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 155, 65, 20).build();
		
		hireCarpenterButton = ButtonWidget.builder(Text.literal("Carpenter"), button ->
		{
			hire = Hire.CARPENTER;
			priceText = Hire.CARPENTER.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 155, 65, 20).build();
		
		hireCartographerButton = ButtonWidget.builder(Text.literal("Cartographer"), button ->
		{
			hire = Hire.CARTOGRAPHER;
			priceText = Hire.CARTOGRAPHER.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 155, 65, 20).build();
		
		addDrawableChild(hireAlchemistButton);
		addDrawableChild(hireBlacksmithButton);
		addDrawableChild(hireFletcherButton);
		addDrawableChild(hireMasonButton);
		addDrawableChild(hireCarpenterButton);
		addDrawableChild(hireCartographerButton);
	}
	
	private void setupRanchingPage()
	{
		hireText1 = Text.literal("Ranchers are responsible for the bees and animals in your settlement.");
		hireText2 = Text.literal("They'll produce your honey, meat, wool, and leather pelts.");
		
		hireBeekeeperButton = ButtonWidget.builder(Text.literal("Beekeeper"), button ->
		{
			hire = Hire.BEEKEEPER;
			priceText = Hire.BEEKEEPER.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		hirePoultrymanButton = ButtonWidget.builder(Text.literal("Poultryman"), button ->
		{
			hire = Hire.POULTRYMAN;
			priceText = Hire.POULTRYMAN.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		hireCowhandButton = ButtonWidget.builder(Text.literal("Cowhand"), button ->
		{
			hire = Hire.COWHAND;
			priceText = Hire.COWHAND.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		hireSwineherdButton = ButtonWidget.builder(Text.literal("Swineherd"), button ->
		{
			hire = Hire.SWINEHERD;
			priceText = Hire.SWINEHERD.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 155, 65, 20).build();
		
		hireShepherdButton = ButtonWidget.builder(Text.literal("Shepherd"), button ->
		{
			hire = Hire.SHEPHERD;
			priceText = Hire.SHEPHERD.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 155, 65, 20).build();
		
		hireStablehandButton = ButtonWidget.builder(Text.literal("Stablehand"), button ->
		{
			hire = Hire.STABLEHAND;
			priceText = Hire.STABLEHAND.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 155, 65, 20).build();
		
		addDrawableChild(hireBeekeeperButton);
		addDrawableChild(hirePoultrymanButton);
		addDrawableChild(hireCowhandButton);
		addDrawableChild(hireSwineherdButton);
		addDrawableChild(hireShepherdButton);
		addDrawableChild(hireStablehandButton);
	}
	
	private  void setupTradingPage()
	{
		hireText1 = Text.literal("Traders import and export the commerce of your settlement.");
		hireText2 = Text.literal("Regulars will sell locally while your merchant will sell foreignly.");
		
		hireBakerButton = ButtonWidget.builder(Text.literal("Baker"), button ->
		{
			hire = Hire.BAKER;
			priceText = Hire.BAKER.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 125, 65, 20).build();
		
		hireCookButton = ButtonWidget.builder(Text.literal("Cook"), button ->
		{
			hire = Hire.COOK;
			priceText = Hire.COOK.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 125, 65, 20).build();
		
		hireArcanistButton = ButtonWidget.builder(Text.literal("Arcanist"), button ->
		{
			hire = Hire.ARCANIST;
			priceText = Hire.ARCANIST.getText();
		}).dimensions(backgroundPosX + 170, backgroundPosY + 125, 65, 20).build();
		
		hireMerchantButton = ButtonWidget.builder(Text.literal("Merchant"), button ->
		{
			hire = Hire.MERCHANT;
			priceText = Hire.MERCHANT.getText();
		}).dimensions(backgroundPosX + 10, backgroundPosY + 155, 65, 20).build();
		
		hireTannerButton = ButtonWidget.builder(Text.literal("Tanner"), button ->
		{
			hire = Hire.TANNER;
			priceText = Hire.TANNER.getText();
		}).dimensions(backgroundPosX + 90, backgroundPosY + 155, 65, 20).build();
		
		addDrawableChild(hireBakerButton);
		addDrawableChild(hireCookButton);
		addDrawableChild(hireArcanistButton);
		addDrawableChild(hireMerchantButton);
		addDrawableChild(hireTannerButton);
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		this.renderBackground(context);
		context.drawTexture(BACKGROUND_TEXTURE, backgroundPosX, backgroundPosY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
		super.render(context, mouseX, mouseY, delta);

		if (page == Page.MAIN)
			hireButton.visible = false;
		else
			hireButton.visible = true;
		
		if (page != Page.MAIN && hire != Hire.NONE && this.playerEmeralds >= hire.getValue())
			hireButton.active = true;
		else
			hireButton.active = false;
		
		if (page != Page.MAIN)
		{
			context.drawText(this.textRenderer, hireTitle, (backgroundPosX + 175), (backgroundPosY + 16), new Color(255, 255, 255).getRGB(), true);
			TextWrapper.render(context, this.textRenderer, hireText1, backgroundPosX + 14, backgroundPosY + 16, new Color(255, 255, 255).getRGB(), 135);
			TextWrapper.render(context, this.textRenderer, hireText2, backgroundPosX + 14, backgroundPosY + 64, new Color(255, 255, 255).getRGB(), 140);
			context.drawTexture(SEPARATOR_TEXTURE, (backgroundPosX + 10), (backgroundPosY + 110), 0, 0, 225, 2, 32, 2);

			context.drawTexture(HANGINGSIGN_TEXTURE, (backgroundPosX + 175), (backgroundPosY + 28), 0, 0, 54, 32, 54, 32);
			context.drawTexture(EMERALD_TEXTURE, (backgroundPosX + 207), (backgroundPosY + 43), 0, 0, 14, 14, 14, 14);
		}
		
		switch (page)
		{
			case MAIN:
				renderMainPage(context, mouseX, mouseY);
				break;
			case GOVERNING:
				renderGoverningPage(context, mouseX, mouseY);
				break;
			case MILITARY:
				renderMilitaryPage(context, mouseX, mouseY);
				break;
			case HARVESTING:
				renderHarvestingPage(context, mouseX, mouseY);
				break;
			case CRAFTING:
				renderCraftingPage(context, mouseX, mouseY);
				break;
			case RANCHING:
				renderRanchingPage(context, mouseX, mouseY);
				break;
			case TRADING:
				renderTradingPage(context, mouseX, mouseY);
				break;
		}
	}

	private void renderMainPage(DrawContext context, int mouseX, int mouseY)
	{
		context.drawTexture(SEPARATOR_TEXTURE, (backgroundPosX + 10), (backgroundPosY + 110), 0, 0, 225, 2, 32, 2);

		for (TextureElement element : barTextures)
		{
			element.drawRect(context);
			if (element.isMouseOver(mouseX, mouseY))
				renderTooltip(context, element.getToolTip(), mouseX, mouseY);
		}

		for (TextureElement element : mainTextures)
		{
			element.draw(context);
			if (element.isMouseOver(mouseX, mouseY))
				renderTooltip(context, element.getToolTip(), mouseX, mouseY);
		}

		context.drawTexture(skinTexture, (backgroundPosX + 20), (backgroundPosY + 22), 8, 8, 8, 8, 64, 64);

		context.drawText(this.textRenderer, nameText, (backgroundPosX + 35), (backgroundPosY + 22), new Color(255, 255, 255).getRGB(), true);
		drawTextR(context, this.textRenderer, expertiseText, (backgroundPosX + 207), (backgroundPosY + 22), new Color(70, 150, 25).getRGB(), true);

		context.drawText(this.textRenderer, healthText, (backgroundPosX + 218), (backgroundPosY + 52), new Color(65, 65, 65).getRGB(), false);
		context.drawText(this.textRenderer, hungerText, (backgroundPosX + 218), (backgroundPosY + 64), new Color(65, 65, 65).getRGB(), false);
		context.drawText(this.textRenderer, moraleText, (backgroundPosX + 218), (backgroundPosY + 76), new Color(65, 65, 65).getRGB(), false);
		context.drawText(this.textRenderer, skillText, (backgroundPosX + 218), (backgroundPosY + 88), new Color(65, 65, 65).getRGB(), false);

		drawBar(context, backgroundPosX + 29, backgroundPosY + 53, settler.getHealth() / 20.0f, 4);
		drawBar(context, backgroundPosX + 29, backgroundPosY + 65, settler.getSettlerHunger() / 100.0f, 12);
		drawBar(context, backgroundPosX + 29, backgroundPosY + 77, settler.getSettlerMorale() / 100.0f, 8);
		drawBar(context, backgroundPosX + 29, backgroundPosY + 89, settler.getSettlerSkill() / 100.0f, 6);
	}

	private void renderGoverningPage(DrawContext context, int mouseX, int mouseY)
	{
		hireArchitectButton.active = (hire != Hire.ARCHITECT);
		hireDelivererButton.active = (hire != Hire.DELIVERER);
		hirePriestButton.active = (hire != Hire.PRIEST);
		hireCourierButton.active = (hire != Hire.COURIER);
		hireInnkeeperButton.active = (hire != Hire.INNKEEPER);

		if (hireArchitectButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.ARCHITECT);
		else if (hireDelivererButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.DELIVERER);
		else if (hirePriestButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.PRIEST);
		else if (hireCourierButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.COURIER);
		else if (hireInnkeeperButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.INNKEEPER);
		else
		    priceText = hire.getText();

		for (TextureElement element : governingTextures)
		{
			element.draw(context);
			if (element.isMouseOver(mouseX, mouseY))
				renderTooltip(context, element.getToolTip(), mouseX, mouseY);
		}

		if (this.playerEmeralds < hire.getValue())
			TextWrapper.render(context, this.textRenderer, priceText, backgroundPosX + 190, backgroundPosY + 46, new Color(235, 50, 30).getRGB(), 225);
		else
			TextWrapper.render(context, this.textRenderer, priceText, backgroundPosX + 190, backgroundPosY + 46, new Color(255, 255, 255).getRGB(), 225);
	}

	private void renderMilitaryPage(DrawContext context, int mouseX, int mouseY)
	{
		hireArcherButton.active = (hire != Hire.ARCHER);
		hireKnightButton.active = (hire != Hire.KNIGHT);
		hireClericButton.active = (hire != Hire.CLERIC);

		if (hireArcherButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.ARCHER);
		else if (hireKnightButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.KNIGHT);
		else if (hireClericButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.CLERIC);
		else
			priceText = hire.getText();

		for (TextureElement element : militaryTextures)
		{
			element.draw(context);
			if (element.isMouseOver(mouseX, mouseY))
				renderTooltip(context, element.getToolTip(), mouseX, mouseY);
		}

		if (this.playerEmeralds < hire.getValue())
			TextWrapper.render(context, this.textRenderer, priceText, backgroundPosX + 190, backgroundPosY + 46, new Color(235, 50, 30).getRGB(), 225);
		else
			TextWrapper.render(context, this.textRenderer, priceText, backgroundPosX + 190, backgroundPosY + 46, new Color(255, 255, 255).getRGB(), 225);
	}

    private void renderHarvestingPage(DrawContext context, int mouseX, int mouseY)
    {
		hireFarmerButton.active = (hire != Hire.FARMER);
		hireMinerButton.active = (hire != Hire.MINER);
		hireLumberjackButton.active = (hire != Hire.LUMBERJACK);
		hireFishermanButton.active = (hire != Hire.FISHERMAN);

		if (hireFarmerButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.FARMER);
		else if (hireMinerButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.MINER);
		else if (hireLumberjackButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.LUMBERJACK);
		else if (hireFishermanButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.FISHERMAN);
		else
			priceText = hire.getText();

		for (TextureElement element : harvestingTextures)
		{
			element.draw(context);
			if (element.isMouseOver(mouseX, mouseY))
				renderTooltip(context, element.getToolTip(), mouseX, mouseY);
		}

		if (this.playerEmeralds < hire.getValue())
			TextWrapper.render(context, this.textRenderer, priceText, backgroundPosX + 190, backgroundPosY + 46, new Color(235, 50, 30).getRGB(), 225);
		else
			TextWrapper.render(context, this.textRenderer, priceText, backgroundPosX + 190, backgroundPosY + 46, new Color(255, 255, 255).getRGB(), 225);
    }

    private void renderCraftingPage(DrawContext context, int mouseX, int mouseY)
    {
		hireAlchemistButton.active = (hire != Hire.ALCHEMIST);
		hireBlacksmithButton.active = (hire != Hire.BLACKSMITH);
		hireFletcherButton.active = (hire != Hire.FLETCHER);
		hireMasonButton.active = (hire != Hire.MASON);
		hireCarpenterButton.active = (hire != Hire.CARPENTER);
		hireCartographerButton.active = (hire != Hire.CARTOGRAPHER);

		if (hireAlchemistButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.ALCHEMIST);
		else if (hireBlacksmithButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.BLACKSMITH);
		else if (hireFletcherButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.FLETCHER);
		else if (hireMasonButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.MASON);
		else if (hireCarpenterButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.CARPENTER);
		else if (hireCartographerButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.CARTOGRAPHER);
		else
			priceText = hire.getText();

		for (TextureElement element : craftingTextures)
		{
			element.draw(context);
			if (element.isMouseOver(mouseX, mouseY))
				renderTooltip(context, element.getToolTip(), mouseX, mouseY);
		}

		if (this.playerEmeralds < hire.getValue())
			TextWrapper.render(context, this.textRenderer, priceText, backgroundPosX + 190, backgroundPosY + 46, new Color(235, 50, 30).getRGB(), 225);
		else
			TextWrapper.render(context, this.textRenderer, priceText, backgroundPosX + 190, backgroundPosY + 46, new Color(255, 255, 255).getRGB(), 225);
    }

    private void renderRanchingPage(DrawContext context, int mouseX, int mouseY)
    {
		hireBeekeeperButton.active = (hire != Hire.BEEKEEPER);
		hirePoultrymanButton.active = (hire != Hire.POULTRYMAN);
		hireCowhandButton.active = (hire != Hire.COWHAND);
		hireSwineherdButton.active = (hire != Hire.SWINEHERD);
		hireShepherdButton.active = (hire != Hire.SHEPHERD);
		hireStablehandButton.active = (hire != Hire.STABLEHAND);

		if (hireBeekeeperButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.BEEKEEPER);
		else if (hirePoultrymanButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.POULTRYMAN);
		else if (hireCowhandButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.COWHAND);
		else if (hireSwineherdButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.SWINEHERD);
		else if (hireShepherdButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.SHEPHERD);
		else if (hireStablehandButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.STABLEHAND);
		else
			priceText = hire.getText();

		for (TextureElement element : ranchingTextures)
		{
			element.draw(context);
			if (element.isMouseOver(mouseX, mouseY))
				renderTooltip(context, element.getToolTip(), mouseX, mouseY);
		}

		if (this.playerEmeralds < hire.getValue())
			TextWrapper.render(context, this.textRenderer, priceText, backgroundPosX + 190, backgroundPosY + 46, new Color(235, 50, 30).getRGB(), 225);
		else
			TextWrapper.render(context, this.textRenderer, priceText, backgroundPosX + 190, backgroundPosY + 46, new Color(255, 255, 255).getRGB(), 225);
    }

    private void renderTradingPage(DrawContext context, int mouseX, int mouseY)
    {
		hireBakerButton.active = (hire != Hire.BAKER);
		hireCookButton.active = (hire != Hire.COOK);
		hireArcanistButton.active = (hire != Hire.ARCANIST);
		hireMerchantButton.active = (hire != Hire.MERCHANT);
		hireTannerButton.active = (hire != Hire.TANNER);

		if (hireBakerButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.BAKER);
		else if (hireCookButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.COOK);
		else if (hireArcanistButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.ARCANIST);
		else if (hireMerchantButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.MERCHANT);
		else if (hireTannerButton.isMouseOver(mouseX, mouseY))
		    priceText = getFormattedPriceText(Hire.TANNER);
		else
			priceText = hire.getText();

		for (TextureElement element : tradingTextures)
		{
			element.draw(context);
			if (element.isMouseOver(mouseX, mouseY))
				renderTooltip(context, element.getToolTip(), mouseX, mouseY);
		}

		if (this.playerEmeralds < hire.getValue())
			TextWrapper.render(context, this.textRenderer, priceText, backgroundPosX + 190, backgroundPosY + 46, new Color(235, 50, 30).getRGB(), 225);
		else
			TextWrapper.render(context, this.textRenderer, priceText, backgroundPosX + 190, backgroundPosY + 46, new Color(255, 255, 255).getRGB(), 225);
    }
    
    private void renderTooltip(DrawContext context, String text, int mouseX, int mouseY) 
    {
    	if (text != null)
    		context.drawTooltip(this.textRenderer, Text.literal(text), mouseX, mouseY);
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
    
    private String capitalize(String str)
	{
		if (str == null || str.isEmpty())
			return str;
		return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
	}
    
    private void setExpertiseTexture(String expertise)
	{
		switch (expertise)
		{
			case "GOVERNING":
				expertiseTexture = new Identifier("minecraft", "textures/item/writable_book.png");
				break;
			case "HARVESTING":
				expertiseTexture = new Identifier("minecraft", "textures/item/bucket.png");
				break;
			case "TRADING":
				expertiseTexture = new Identifier("minecraft", "textures/item/bundle_filled.png");
				break;
			case "CRAFTING":
				expertiseTexture = new Identifier("minecraft", "textures/item/flint.png");
				break;
			case "RANCHING":
				expertiseTexture = new Identifier("minecraft", "textures/item/lead.png");
				break;
			case "MILITARY":
				expertiseTexture = new Identifier("minecraft", "textures/item/chainmail_chestplate.png");
				break;
			default:
				Frontier.LOGGER.info("HireSettlerScreen() - Invalid settler expertise!");
				break;
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
    
    private Text getFormattedPriceText(Hire hire)
    {
        if (this.playerEmeralds >= hire.getValue())
            return hire.getText().formatted(Formatting.GRAY);
        else 
            return hire.getText().formatted(Formatting.RED);
    }
    
    @Override
    public boolean shouldPause() 
    {
    	return false;
    }
}