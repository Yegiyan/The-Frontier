package com.frontier.entities;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

public class SettlerEntity extends PathAwareEntity
{	
	private static final TrackedData<String> SETTLER_NAME = DataTracker.registerData(SettlerEntity.class, TrackedDataHandlerRegistry.STRING);
	private static final TrackedData<String> SETTLER_FACTION = DataTracker.registerData(SettlerEntity.class, TrackedDataHandlerRegistry.STRING);
	private static final TrackedData<String> SETTLER_PROFESSION = DataTracker.registerData(SettlerEntity.class, TrackedDataHandlerRegistry.STRING);
	private static final TrackedData<Integer> SETTLER_MORALE = DataTracker.registerData(SettlerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<String> SETTLER_GENDER = DataTracker.registerData(SettlerEntity.class, TrackedDataHandlerRegistry.STRING);
	private static final TrackedData<Integer> SETTLER_TEXTURE = DataTracker.registerData(SettlerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	
	public List<String> genders = Arrays.asList("Male", "Female");
	private UUID uuid;
	
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
	
	public SettlerEntity(EntityType<? extends PathAwareEntity> entityType, World world)
    {
        super(entityType, world);
        this.uuid = UUID.randomUUID();
        this.dataTracker.startTracking(SETTLER_NAME, "");
        this.dataTracker.startTracking(SETTLER_FACTION, "");
        this.dataTracker.startTracking(SETTLER_PROFESSION, "");
        this.dataTracker.startTracking(SETTLER_MORALE, 0);
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
	    
	    // for debugging purposes
	    /*
	    if (!this.getWorld().isClient && player.getStackInHand(hand).isEmpty() && hand.equals(Hand.MAIN_HAND)) 
	    {
	    	System.out.println("------------");
	        System.out.println("Name: " + this.getSettlerName());
	        System.out.println("Faction: " + this.getSettlerFaction());
	        System.out.println("Profession: " + this.getSettlerProfession());
	        System.out.println("Gender: " + this.getSettlerGender());
	        System.out.println("Texture: " + this.getSettlerTexture());
	        System.out.println("------------");
	    }
	    */
	    
	    return super.interactAt(player, hitPos, hand);
	}
	
	@Override
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, NbtCompound entityNbt) 
	{
		entityData = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);

	    if (entityNbt.contains("Name")) 
	        setSettlerName(entityNbt.getString("Name"));
	    
	    if (entityNbt.contains("Faction")) 
	        setSettlerFaction(entityNbt.getString("Faction"));
	    
	    if (entityNbt.contains("Profession")) 
	        setSettlerProfession(entityNbt.getString("Profession"));
	    
	    if (entityNbt.contains("Morale"))
	        setSettlerMorale(entityNbt.getInt("Morale"));
	    
	    if (entityNbt.contains("Gender")) 
	        setSettlerGender(entityNbt.getString("Gender"));
	    
	    this.getDataTracker().set(SETTLER_TEXTURE, getSettlerTexture());
	    
	    return entityData;
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
	    
	    if (nbt.contains("Morale"))
	        this.getDataTracker().set(SETTLER_MORALE, nbt.getInt("Morale"));
	    
	    if (nbt.contains("Gender"))
	        this.getDataTracker().set(SETTLER_GENDER, nbt.getString("Gender"));
	    
	    if (nbt.contains("SettlerTexture"))
	        this.getDataTracker().set(SETTLER_TEXTURE, nbt.getInt("SettlerTexture"));
	    
	    loadData(this.getWorld());
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) 
	{
	    super.writeCustomDataToNbt(nbt);
	    nbt.putUuid("UUID", this.uuid);
	    nbt.putString("Name", getSettlerName());
	    nbt.putString("Faction", getSettlerFaction());
	    nbt.putString("Profession", getSettlerProfession());
	    nbt.putInt("Morale", this.getDataTracker().get(SETTLER_MORALE));
	    nbt.putString("Gender", getSettlerGender());
	    nbt.putInt("SettlerTexture", this.getDataTracker().get(SETTLER_TEXTURE));
	}

	public static void saveData(SettlerEntity settler, World world)
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
	    settlerNbt.putInt("Morale", settler.getSettlerMorale());
	    settlerNbt.putString("Gender", settler.getSettlerGender());
	    settlerNbt.putInt("Texture", settler.getSettlerTexture());
	    faction.put(settlerUUID, settlerNbt); 
	    settlers.put(settlerFaction, faction);
	    nbt.put("Settlers", settlers);

	    try { NbtIo.writeCompressed(nbt, nbtFile); }
	    catch (IOException e) { e.printStackTrace(); }
	}

	public static Set<String> loadData(World world)
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
	
	public static void removeSettlerFromData(String name, World world) 
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
	            if(settlerNbt.getString("Name").equals(name)) 
	            {
	                factionNbt.remove(uuid);
	                break;
	            }
	        }
	        settlers.put(faction, factionNbt);
	    }
	    nbt.put("Settlers", settlers);

	    try { NbtIo.writeCompressed(nbt, nbtFile); } 
	    catch (IOException e) { e.printStackTrace(); }
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
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }
	
	@Override
	public void remove(RemovalReason reason) {
	    super.remove(reason);
	    removeSettlerFromData(this.getSettlerName(), getEntityWorld());
	}
	
	@Override
	public Text getName() {
	    if (this.hasCustomName())
	        return this.getCustomName();
	    else 
	        return Text.of(getSettlerName());
	}
	
	public UUID getUUID() {
	    return this.uuid;
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
	
	public int getSettlerMorale() {
	    return this.dataTracker.get(SETTLER_MORALE);
	}

	public void setSettlerMorale(int morale) {
	    this.dataTracker.set(SETTLER_MORALE, morale);
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