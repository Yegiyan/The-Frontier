package com.frontier.entities;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import com.frontier.Frontier;
import com.frontier.PlayerData;
import com.frontier.gui.SettlerCardScreen;
import com.frontier.network.FrontierPackets;
import com.frontier.settlements.SettlementManager;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

public abstract class SettlerEntity extends PathAwareEntity implements Inventory
{	
	private static final TrackedData<String> SETTLER_NAME = DataTracker.registerData(SettlerEntity.class, TrackedDataHandlerRegistry.STRING);
	private static final TrackedData<String> SETTLER_FACTION = DataTracker.registerData(SettlerEntity.class, TrackedDataHandlerRegistry.STRING);
	private static final TrackedData<String> SETTLER_PROFESSION = DataTracker.registerData(SettlerEntity.class, TrackedDataHandlerRegistry.STRING);
	private static final TrackedData<String> SETTLER_EXPERTISE = DataTracker.registerData(SettlerEntity.class, TrackedDataHandlerRegistry.STRING);
	private static final TrackedData<Integer> SETTLER_MORALE = DataTracker.registerData(SettlerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Integer> SETTLER_SKILL = DataTracker.registerData(SettlerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Integer> SETTLER_HUNGER = DataTracker.registerData(SettlerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<String> SETTLER_GENDER = DataTracker.registerData(SettlerEntity.class, TrackedDataHandlerRegistry.STRING);
	private static final TrackedData<Integer> SETTLER_TEXTURE = DataTracker.registerData(SettlerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	
	private SimpleInventory inventory;
	
	public enum Expertise { GOVERNING, LABORING, CRAFTING, RANCHING, ARTISAN, MILITARY, NOVICE }
	
	public List<String> genders = Arrays.asList("Male", "Female");
	
	public List<String> maleSettlerNames = Arrays.asList(
		    "Alden", "Balin", "Cedric", "Darian", "Eamon", "Fabian", "Gawain", "Hadrian", "Ivor", "Jareth", 
		    "Kael", "Lancelot", "Merrick", "Nolan", "Osric", "Percival", "Quinlan", "Roland", "Soren", "Tristan", 
		    "Urien", "Valerian", "Wystan", "Xavier", "Ywain", "Zephyr", "Alain", "Bromley", "Corwin", "Drake", 
		    "Emrys", "Fenris", "Garret", "Helmer", "Isolde", "Jasper", "Keiran", "Leif", "Morcant", "Neville", 
		    "Orion", "Pellinore", "Quillan", "Rafael", "Sawyer", "Thorne", "Ulric", "Vance", "Wayland", "Xander", 
		    "Yorick", "Zephyrus", "Alaric", "Bryant", "Crispin", "Duncan", "Eldon", "Fergus", "Gideon", "Hugo", 
		    "Ivan", "Jerome", "Killian", "Luther", "Malcolm", "Nash", "Osgar", "Pascal", "Quinton", "Reynard", 
		    "Sylvan", "Tybalt", "Uther", "Vaughan", "Wolfram", "Xan", "York", "Zane", "Ambrose", "Brand", "Caspar", 
		    "Didrik", "Erik", "Fletcher", "Griffin", "Heath", "Ignatius", "Julian", "Kendrick", "Leopold", "Miles", 
		    "Nigel", "Osborn", "Phineas", "Quincy", "Rupert", "Sebastian", "Thornton", "Udolf", "Vernon", "Wilfred", 
		    "Xerxes", "Yale", "Zachariah", "Ansel", "Benedict", "Clarence", "Dietrich", "Edgar", "Florian", "Godfrey", 
		    "Harold", "Isidore", "Justus", "Konrad", "Linus", "Milo", "Nathaniel", "Otto", "Philip", "Quentin", "Hayk",
		    "Reginald", "Stefan", "Tobias", "Ulrich", "Victor", "Walter", "Zenosi", "Yves", "Zebulon", "Arnold", "Rafayel",
		    "Bernard", "Clement", "Dominic", "Egbert", "Ferdinand", "Gregory", "Horace", "Isaac", "Justin", "Armen",
		    "Klaus", "Ludwig", "Magnus", "Norbert", "Oliver", "Pius", "Quirinus", "Rudolf", "Siegfried", "Theodore", 
		    "Udo", "Vincent", "Werner", "Xaver", "Yannick", "Zigmund", "Alfred", "Bruno", "Conrad", "Desmond", 
		    "Ernest", "Frederick", "Gerard", "Hendrik", "Ingmar", "Joachim", "Kurt", "Leonard", "Maximilian", "Norman", 
		    "Oscar", "Peter", "Quintus", "Rainer", "Sven", "Thomas", "Uwe", "Vitus", "Wilhelm", "Hovo", "Yorick", "Narek");

	public List<String> femaleSettlerNames = Arrays.asList(
		    "Aria", "Brielle", "Cassandra", "Dahlia", "Elara", "Fiona", "Gwendolyn", "Hazel", "Isolde", "Jasmine",
		    "Kiera", "Luna", "Maeve", "Nyla", "Ophelia", "Orin", "Penelope", "Quinn", "Rhiannon", "Seraphina", "Thea",
		    "Ursula", "Vivienne", "Willow", "Xanthe", "Yara", "Zara", "Aveline", "Bronwyn", "Cordelia", "Demelza", 
		    "Evelyn", "Freya", "Genevieve", "Helena", "Imogen", "Juliet", "Kathryn", "Lillian", "Mirabel", "Nadine", 
		    "Odette", "Pandora", "Rosamund", "Sabina", "Tabitha", "Undine", "Verity", "Winifred", "Xena", "Yvaine", 
		    "Zipporah", "Allegra", "Beatrice", "Cecily", "Daphne", "Edith", "Florence", "Griselda", "Henrietta", 
		    "Iris", "Josephine", "Keziah", "Lucinda", "Meredith", "Nerissa", "Octavia", "Prudence", "Rowena", 
		    "Sibyl", "Temperance", "Unity", "Venetia", "Winona", "Xanadu", "Yolanda", "Zenobia", "Adelaide", 
		    "Briony", "Clarissa", "Dorothea", "Evangeline", "Francesca", "Ginevra", "Honoria", "Isadora", "Jemima", 
		    "Kallista", "Lydia", "Marguerite", "Nicolette", "Olivette", "Philomena", "Rosalind", "Sidonia", "Thomasina", 
		    "Ursuline", "Valentina", "Wilhelmina", "Xantippe", "Yesenia", "Zinnia", "Aurelia", "Blythe", "Constance", 
		    "Delilah", "Esther", "Felicity", "Guinevere", "Hermione", "Iolanthe", "Jessamine", "Kerensa", "Lavender", 
		    "Melisande", "Narcissa", "Opaline", "Persephone", "Romilda", "Susannah", "Tallulah", "Umbrielle", "Valetta", 
		    "Wisteria", "Xanthia", "Yolande", "Zephyrine", "Anastasia", "Bernadette", "Clementine", "Desdemona", 
		    "Eugenia", "Fenella", "Gwendolen", "Hortensia", "Isabeau", "Jacqueline", "Kitty", "Lorelei", "Minerva", 
		    "Nerida", "Oriana", "Pandora", "Rosabel", "Seraphine", "Tatiana", "Urania", "Violetta", "Wynonna", 
		    "Xenobia", "Yasmine", "Zuleika", "Amabel", "Beatrix", "Celestine", "Dove", "Emmeline", "Fleur", 
		    "Georgiana", "Hildegard", "Isolde", "Jocasta", "Kismet", "Leticia", "Morgana", "Nephele", "Oriole", 
		    "Perdita", "Rafaela", "Salome", "Tempest", "Lily", "Veronica", "Winola", "Xandria", "Ysabel", "Zelda");
	
	public abstract Identifier[] getTextures(boolean isMale);
	
	public SettlerEntity(EntityType<? extends PathAwareEntity> entityType, World world)
    {
        super(entityType, world);
        this.inventory = new SimpleInventory(27);
        this.dataTracker.startTracking(SETTLER_NAME, "");
        this.dataTracker.startTracking(SETTLER_FACTION, "");
        this.dataTracker.startTracking(SETTLER_PROFESSION, "");
        this.dataTracker.startTracking(SETTLER_EXPERTISE, "");
        this.dataTracker.startTracking(SETTLER_HUNGER, 0);
        this.dataTracker.startTracking(SETTLER_MORALE, 0);
        this.dataTracker.startTracking(SETTLER_SKILL, 0);
        this.dataTracker.startTracking(SETTLER_GENDER, "");
        this.dataTracker.startTracking(SETTLER_TEXTURE, 0);
    }
	
	@Override
	protected void initGoals() 
	{
	    this.goalSelector.add(0, new SwimGoal(this));
	}
	
	@Override
	public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand)
	{
	    ItemStack itemStack = player.getStackInHand(hand);
	    PlayerData playerData = PlayerData.players.get(player.getUuid());
	    
	    if (!player.getWorld().isClient)
	    	syncInventory();
	    
	    if (itemStack.getItem() == Items.NAME_TAG)
	    {
	        if (itemStack.hasCustomName() && !this.getWorld().isClient())
	        {
	            this.setCustomName(itemStack.getName());
	            itemStack.decrement(1);
	            changeSettlerName(this.getSettlerName(), itemStack.getName().getString(), getWorld());
	            this.setSettlerName(itemStack.getName().getString());
	            return ActionResult.SUCCESS;
	        }
	    }

	    if (player.getWorld().isClient && playerData.getProfession().equals("Leader") && hand.equals(Hand.MAIN_HAND) && !this.getSettlerProfession().equals("Nomad"))
	    	MinecraftClient.getInstance().setScreen(new SettlerCardScreen(this));
	    
	    //printEntityInfo(player, hand);
	    return super.interactAt(player, hitPos, hand);
	}
	
	@Override
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, NbtCompound entityNbt)
	{
		entityData = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
		if (entityNbt.contains("UUID"))
			setUuid(entityNbt.getUuid("UUID"));
	    if (entityNbt.contains("Name")) 
	        setSettlerName(entityNbt.getString("Name"));
	    if (entityNbt.contains("Faction")) 
	        setSettlerFaction(entityNbt.getString("Faction"));
	    if (entityNbt.contains("Profession")) 
	        setSettlerProfession(entityNbt.getString("Profession"));
	    if (entityNbt.contains("Expertise"))
	    	setSettlerExpertise(entityNbt.getString("Expertise"));
	    if (entityNbt.contains("Morale"))
	        setSettlerMorale(entityNbt.getInt("Morale"));
	    if (entityNbt.contains("Skill"))
	        setSettlerSkill(entityNbt.getInt("Skill"));
	    if (entityNbt.contains("Hunger"))
	        setSettlerHunger(entityNbt.getInt("Hunger"));
	    if (entityNbt.contains("Gender")) 
	        setSettlerGender(entityNbt.getString("Gender"));
	    this.getDataTracker().set(SETTLER_TEXTURE, getSettlerTexture());
	    return entityData;
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) 
	{
	    super.writeCustomDataToNbt(nbt);
	    nbt.putUuid("UUID", this.uuid);
	    nbt.putString("Name", getSettlerName());
	    nbt.putString("Faction", getSettlerFaction());
	    nbt.putString("Profession", getSettlerProfession());
	    nbt.putString("Expertise", this.getDataTracker().get(SETTLER_EXPERTISE));
	    nbt.putInt("Morale", this.getDataTracker().get(SETTLER_MORALE));
	    nbt.putInt("Skill", this.getDataTracker().get(SETTLER_SKILL));
	    nbt.putInt("Hunger", this.getDataTracker().get(SETTLER_HUNGER));
	    nbt.putString("Gender", getSettlerGender());
	    nbt.putInt("Texture", this.getDataTracker().get(SETTLER_TEXTURE));
	    
	    nbt.putString("EntityType", this.getType().toString());
	    
	    NbtList inventoryList = new NbtList();
	    for (int i = 0; i < inventory.size(); i++)
	    {
	        ItemStack stack = inventory.getStack(i);
	        if (!stack.isEmpty())
	        {
	            NbtCompound itemTag = new NbtCompound();
	            itemTag.putInt("Slot", i);
	            stack.writeNbt(itemTag);
	            inventoryList.add(itemTag);
	        }
	    }
	    nbt.put("Inventory", inventoryList);
	}
	
	@Override
	public void readCustomDataFromNbt(NbtCompound nbt)
	{
	    super.readCustomDataFromNbt(nbt);
	    if (nbt.contains("UUID")) 
	        this.uuid = nbt.getUuid("UUID");
	    if (nbt.contains("Name"))
	        this.getDataTracker().set(SETTLER_NAME, nbt.getString("Name"));
	    if (nbt.contains("Faction"))
	        this.getDataTracker().set(SETTLER_FACTION, nbt.getString("Faction"));
	    if (nbt.contains("Profession"))
	        this.getDataTracker().set(SETTLER_PROFESSION, nbt.getString("Profession"));
	    if (nbt.contains("Expertise"))
	    	this.getDataTracker().set(SETTLER_EXPERTISE, nbt.getString("Expertise"));
	    if (nbt.contains("Morale"))
	        this.getDataTracker().set(SETTLER_MORALE, nbt.getInt("Morale"));
	    if (nbt.contains("Skill"))
	        this.getDataTracker().set(SETTLER_SKILL, nbt.getInt("Skill"));
	    if (nbt.contains("Hunger"))
	        this.getDataTracker().set(SETTLER_HUNGER, nbt.getInt("Hunger"));
	    if (nbt.contains("Gender"))
	        this.getDataTracker().set(SETTLER_GENDER, nbt.getString("Gender"));
	    if (nbt.contains("Texture"))
	        this.getDataTracker().set(SETTLER_TEXTURE, nbt.getInt("Texture"));
	    
	    NbtList inventoryList = nbt.getList("Inventory", NbtElement.COMPOUND_TYPE);
	    for (int i = 0; i < inventoryList.size(); i++)
	    {
	        NbtCompound itemTag = inventoryList.getCompound(i);
	        int slot = itemTag.getInt("Slot");
	        if (slot >= 0 && slot < inventory.size())
	            inventory.setStack(slot, ItemStack.fromNbt(itemTag));
	    }
	    
	    loadEntityData(this.getWorld());
	}

	public static void saveEntityData(SettlerEntity settler, World world)
	{
	    File saveDir = world.getServer().getSavePath(WorldSavePath.ROOT).toFile();
	    File nbtFile = new File(saveDir, "thefrontier/EntityData.nbt");
	    NbtCompound nbt;

	    if (nbtFile.exists())
	    {
	        try { nbt = NbtIo.readCompressed(nbtFile); } 
	        catch (IOException e) { e.printStackTrace(); return; }
	    }
	    
	    else
	    {
	        nbt = new NbtCompound();
	        nbt.put("Settlers", new NbtCompound()); 
	    }

	    NbtCompound settlerNbt = new NbtCompound();
	    NbtCompound settlers = nbt.getCompound("Settlers");
	    String settlerFaction = settler.getSettlerFaction();
	    NbtCompound faction = settlers.getCompound(settlerFaction);
	    String settlerUUID = settler.getUuid().toString();
	    
	    settlerNbt.putString("Name", settler.getSettlerName());
	    settlerNbt.putString("Profession", settler.getSettlerProfession());
	    settlerNbt.putString("Expertise", settler.getSettlerExpertise());
	    settlerNbt.putInt("Morale", settler.getSettlerMorale());
	    settlerNbt.putInt("Skill", settler.getSettlerSkill());
	    settlerNbt.putInt("Hunger", settler.getSettlerHunger());
	    settlerNbt.putString("Gender", settler.getSettlerGender());
	    settlerNbt.putInt("Texture", settler.getSettlerTexture());
	    faction.put(settlerUUID, settlerNbt); 
	    settlers.put(settlerFaction, faction);
	    nbt.put("Settlers", settlers);

	    try { NbtIo.writeCompressed(nbt, nbtFile); }
	    catch (IOException e) { e.printStackTrace(); }
	}

	public static Set<String> loadEntityData(World world)
	{
	    Set<String> occupiedNames = new HashSet<>();
	    File saveDir = world.getServer().getSavePath(WorldSavePath.ROOT).toFile();
	    File nbtFile = new File(saveDir, "thefrontier/EntityData.nbt");
	    NbtCompound nbt;
	    
	    try 
	    {
	        if (!nbtFile.exists())
	        {
	            if(!nbtFile.getParentFile().exists())
	                nbtFile.getParentFile().mkdirs();
	            
	            nbtFile.createNewFile();
	            nbt = new NbtCompound();
	            nbt.put("Settlers", new NbtCompound());
	            NbtIo.writeCompressed(nbt, nbtFile);
	        }
	        else
	            nbt = NbtIo.readCompressed(nbtFile);

	        NbtCompound settlers = nbt.getCompound("Settlers");
	        for (String faction : settlers.getKeys()) 
	        {
	            NbtCompound factionNbt = settlers.getCompound(faction);
	            for(String uuid : factionNbt.getKeys()) 
	            {
	                NbtCompound settlerNbt = factionNbt.getCompound(uuid);
	                occupiedNames.add(settlerNbt.getString("Name"));
	            }
	        }
	    } 
	    catch(IOException e) { e.printStackTrace(); }
	    return occupiedNames;
	}
	
	public static void removeSettlerFromEntityData(UUID uuid, World world)
	{
	    File saveDir = world.getServer().getSavePath(WorldSavePath.ROOT).toFile();
	    File nbtFile = new File(saveDir, "thefrontier/EntityData.nbt");

	    NbtCompound nbt;
	    if (nbtFile.exists()) 
	    {
	        try { nbt = NbtIo.readCompressed(nbtFile); } 
	        catch (IOException e) { e.printStackTrace(); return; }
	    } 
	    else 
	        return;

	    NbtCompound settlers = nbt.getCompound("Settlers");
	    for (String faction : settlers.getKeys()) 
	    {
	        NbtCompound factionNbt = settlers.getCompound(faction);
	        if (factionNbt.contains(uuid.toString())) 
	        {
	            factionNbt.remove(uuid.toString());
	            settlers.put(faction, factionNbt);
	            break;
	        }
	    }
	    nbt.put("Settlers", settlers);

	    try { NbtIo.writeCompressed(nbt, nbtFile); }
	    catch (IOException e) { e.printStackTrace(); }
	}
	
	public static void removeSettlerFromSettlementData(UUID uuid, String factionName, World world) 
	{
	    File saveDir = world.getServer().getSavePath(WorldSavePath.ROOT).toFile();
	    File nbtFile = new File(saveDir, "thefrontier/SettlementData.nbt");

	    NbtCompound nbt;
	    if (nbtFile.exists()) 
	    {
	        try { nbt = NbtIo.readCompressed(nbtFile); } 
	        catch (IOException e) { e.printStackTrace(); return; }
	    } 
	    else 
	        return;

	    if (nbt.contains("Settlements")) 
	    {
	        NbtCompound settlements = nbt.getCompound("Settlements");
	        if (settlements.contains(factionName)) 
	        {
	            NbtCompound factionNbt = settlements.getCompound(factionName);
	            if (factionNbt.contains("Settlers")) 
	            {
	                NbtCompound settlers = factionNbt.getCompound("Settlers");

	                for (String key : settlers.getKeys()) 
	                {
	                    NbtCompound settlerNbt = settlers.getCompound(key);
	                    int[] settlerUUID = settlerNbt.getIntArray("UUID");
	                    UUID settlerUUIDObject = new UUID(
	                        (long) settlerUUID[0] << 32 | (settlerUUID[1] & 0xFFFFFFFFL),
	                        (long) settlerUUID[2] << 32 | (settlerUUID[3] & 0xFFFFFFFFL)
	                    );

	                    if (settlerUUIDObject.equals(uuid)) 
	                    {
	                        settlers.remove(key);
	                        break;
	                    }
	                }

	                factionNbt.put("Settlers", settlers);
	                settlements.put(factionName, factionNbt);
	                nbt.put("Settlements", settlements);

	                try { NbtIo.writeCompressed(nbt, nbtFile); }
	                catch (IOException e) { e.printStackTrace(); }
	            }
	        }
	    }
	}
	
	public static void changeSettlerName(String oldName, String newName, World world) 
	{
	    File saveDir = world.getServer().getSavePath(WorldSavePath.ROOT).toFile();
	    File nbtFile = new File(saveDir, "thefrontier/EntityData.nbt");

	    NbtCompound nbt;
	    if (nbtFile.exists()) 
	    {
	        try { nbt = NbtIo.readCompressed(nbtFile); } 
	        catch (IOException e) { e.printStackTrace(); return; }
	    }
	    else
	        return;

	    NbtCompound settlers = nbt.getCompound("Settlers");
	    for (String faction : settlers.getKeys()) 
	    {
	        NbtCompound factionNbt = settlers.getCompound(faction);
	        for(String uuid : factionNbt.getKeys()) 
	        {
	            NbtCompound settlerNbt = factionNbt.getCompound(uuid);
	            if(settlerNbt.getString("Name").equals(oldName)) 
	            {
	                settlerNbt.putString("Name", newName);
	                factionNbt.put(uuid, settlerNbt);
	                break;
	            }
	        }
	        settlers.put(faction, factionNbt);
	    }

	    nbt.put("Settlers", settlers);

	    try { NbtIo.writeCompressed(nbt, nbtFile); } 
	    catch (IOException e) { e.printStackTrace(); }
	}
	
	public static SettlerEntity findSettlerEntityInRadius(World world, BlockPos pos, String name)
	{
		Box searchBox = new Box(pos).expand(10); // 20x20x20 area around pos
	    List<SettlerEntity> settlers = world.getEntitiesByClass(SettlerEntity.class, searchBox, settler -> settler.getSettlerName().equals(name));
	    return settlers.isEmpty() ? null : settlers.get(0);
	}

	public static Expertise generateExpertise()
	{
		int pick = new Random().nextInt(Expertise.values().length);
	    return Expertise.values()[pick];
	}
	
	public static int generateValue(int min, int max)
	{
	    if (min > max)
	        throw new IllegalArgumentException("SettlerEntity() - generateValue() max must be greater than min!");

	    Random rand = new Random();
	    int value = min + rand.nextInt((max - min) + 1);

	    double chance = 0.05; // 5%
	    if (rand.nextDouble() < chance)
	        value *= 2;

	    return value;
	}
	
	@SuppressWarnings("unused")
	private void printEntityInfo(PlayerEntity player, Hand hand)
	{
		if (!this.getWorld().isClient && player.getStackInHand(hand).isEmpty() && hand.equals(Hand.MAIN_HAND)) 
	    {
			Frontier.LOGGER.info("------------");
			Frontier.LOGGER.info("UUID: " + getUuid());
			Frontier.LOGGER.info("Name: " + this.getSettlerName());
			Frontier.LOGGER.info("Faction: " + this.getSettlerFaction());
			Frontier.LOGGER.info("Profession: " + this.getSettlerProfession());
			Frontier.LOGGER.info("Expertise: " + this.getSettlerExpertise());
			Frontier.LOGGER.info("Morale: " + this.getSettlerMorale());
			Frontier.LOGGER.info("Skill: " + this.getSettlerSkill());
			Frontier.LOGGER.info("Hunger: " + this.getSettlerHunger());
			Frontier.LOGGER.info("Gender: " + this.getSettlerGender());
			Frontier.LOGGER.info("Texture: " + this.getSettlerTexture());
			Frontier.LOGGER.info("Inventory: " + this.getInventory());
			Frontier.LOGGER.info("------------");
	    }
	}
	
	@Override
	public void mobTick() 
	{
	    super.mobTick();
	    LivingEntity target = this.getTarget();
	    if (target != null) 
	        this.getLookControl().lookAt(target, 30.0F, 30.0F);
	}
	
	@Override
	public void onDeath(DamageSource source)
	{
	    super.onDeath(source);
	    this.dropInventory();
	}
	
	@Override
	public void dropInventory()
	{
		super.dropInventory();
		for (int i = 0; i < inventory.size(); i++)
		{
			ItemStack stack = inventory.getStack(i);
			if (!stack.isEmpty())
			{
				this.dropStack(stack);
				inventory.setStack(i, ItemStack.EMPTY); // clear inventory slot after dropping item
			}
		}
	}
	
	@Override
	public void remove(RemovalReason reason)
	{
		if (!this.getWorld().isClient)
		{
			if (this.getServer() != null)
			{
				if (!isNomad())
				{
					removeSettlerFromSettlementData(uuid, getSettlerFaction(), getEntityWorld());
					SettlementManager.getSettlement(getSettlerFaction()).removeSettler(getUuid());
					SettlementManager.saveSettlements(getServer());
				}
				
				removeSettlerFromEntityData(this.getUuid(), this.getEntityWorld());
			}
			else
				Frontier.LOGGER.error("SettlerEntity() - getServer is null!");
		}
		super.remove(reason);
	}

	public void syncInventory()
	{
	    PacketByteBuf buf = PacketByteBufs.create();
	    buf.writeInt(this.getId());
	    buf.writeInt(inventory.size());
	    for (int i = 0; i < inventory.size(); i++)
	        buf.writeItemStack(inventory.getStack(i));
	    for (ServerPlayerEntity player : PlayerLookup.tracking(this))
	        ServerPlayNetworking.send(player, FrontierPackets.SYNC_SETTLER_INVENTORY_ID, buf);
	}

	public void setClientInventory(DefaultedList<ItemStack> inventory)
	{
	    this.inventory.clear();
	    for (int i = 0; i < inventory.size(); i++)
	        this.inventory.setStack(i, inventory.get(i));
	}
	
	public SimpleInventory getInventory() {
		return inventory;
	}
	
	@Override
    public int size() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.getStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return inventory.removeStack(slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return inventory.removeStack(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.setStack(slot, stack);
    }

    @Override
    public void markDirty() {
        inventory.markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        inventory.clear();
    }
	
    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }
    
    public boolean isNomad()
    {
    	return getSettlerFaction().equalsIgnoreCase("N/A");
    }
    
	@Override
	public Text getName() {
	    if (this.hasCustomName())
	        return this.getCustomName();
	    else 
	        return Text.of(getSettlerName());
	}
	
	public String getSettlerName() {
	    return this.dataTracker.get(SETTLER_NAME);
	}

	public void setSettlerName(String name) {
	    this.dataTracker.set(SETTLER_NAME, name);
	}

	public String getSettlerFaction() {
	    return this.dataTracker.get(SETTLER_FACTION);
	}

	public void setSettlerFaction(String faction) {
	    this.dataTracker.set(SETTLER_FACTION, faction);
	}
	
	public String getSettlerProfession() {
	    return this.dataTracker.get(SETTLER_PROFESSION);
	}

	public void setSettlerProfession(String profession) {
	    this.dataTracker.set(SETTLER_PROFESSION, profession);
	}
	
	public String getSettlerExpertise() {
	    return this.dataTracker.get(SETTLER_EXPERTISE);
	}

	public void setSettlerExpertise(String expertise) {
	    this.dataTracker.set(SETTLER_EXPERTISE, expertise);
	}
	
	public int getSettlerMorale() {
	    return this.dataTracker.get(SETTLER_MORALE);
	}

	public void setSettlerMorale(int morale) {
	    this.dataTracker.set(SETTLER_MORALE, morale);
	}
	
	public int getSettlerSkill() {
	    return this.dataTracker.get(SETTLER_SKILL);
	}

	public void setSettlerSkill(int skill) {
	    this.dataTracker.set(SETTLER_SKILL, skill);
	}
	
	public int getSettlerHunger() {
	    return this.dataTracker.get(SETTLER_HUNGER);
	}

	public void setSettlerHunger(int hunger) {
	    this.dataTracker.set(SETTLER_HUNGER, hunger);
	}
	
	public String getSettlerGender() {
	    return this.dataTracker.get(SETTLER_GENDER);
	}

	public void setSettlerGender(String gender) {
	    this.dataTracker.set(SETTLER_GENDER, gender);
	}

	public int getSettlerTexture() {
	    return this.dataTracker.get(SETTLER_TEXTURE);
	}

	public void setSettlerTexture(int texture) {
	    this.dataTracker.set(SETTLER_TEXTURE, texture);
	}
}