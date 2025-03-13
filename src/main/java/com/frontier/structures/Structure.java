package com.frontier.structures;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.frontier.Frontier;
import com.frontier.settlements.SettlementManager;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public abstract class Structure
{
	public enum StructureCategory
	{ 
		CORE,       // essential settlement buildings
		HOUSING,    // buildings for settlers to live in
		FOUNDATION, // roads, bridges, and walls
		MILITARY,   // defensive, offensive, and territory expansion buildings
		PRODUCTION, // resource gathering buildings
		CRAFTING,   // processing and crafting buildings
		RANCHING,   // animal husbandry buildings
		SERVICES,   // service oriented buildings
		CULTURAL,   // buildings for cultural and social purposes
		DECORATIVE, // aesthetic and utility buildings
		NULL		// null value
	}

	protected String name;
	protected String faction;
	protected StructureType type;
	protected StructureCategory category;
	protected BlockPos position;
	protected Direction facing;
	protected int tier;
	protected int maxTier;
	protected UUID uuid;

	protected boolean isActive;
	protected boolean isConstructed;

	protected Map<String, Integer> resourceRequirements = new HashMap<>();

	// component managers
	private StructureConstructionManager constructionManager;
	private StructureUpgradeManager upgradeManager;
	private StructureRepairManager repairManager;
	private StructureInventoryManager inventoryManager;

	protected abstract void onConstruction(ServerWorld world);
	protected abstract void onUpgrade();
	protected abstract void onRemove();
	protected abstract void update(ServerWorld world);

	public Structure(String name, String faction, StructureType type, BlockPos position, Direction facing)
	{
		this.name = name;
		this.faction = faction;
		this.type = type;
		this.category = type.getCategory();
		this.position = position;
		this.facing = facing;
		this.tier = 0;
		this.maxTier = 0;
		this.uuid = UUID.randomUUID();
		this.isActive = false;
		this.isConstructed = false;

		this.constructionManager = new StructureConstructionManager(this);
		this.upgradeManager = new StructureUpgradeManager(this);
		this.repairManager = new StructureRepairManager(this);
		this.inventoryManager = new StructureInventoryManager(this);

		loadResourceRequirements();
	}

	public void resume(ServerWorld world)
	{
	    Frontier.LOGGER.info("Resuming structure: " + name + " (UUID: " + uuid + ")");
	    
	    if (constructionManager.isClearing())
	        constructionManager.prepareClearingQueue(world);

	    if (constructionManager.isConstructing())
	        constructionManager.prepareConstructionQueue(world);
	    
	    // We don't register tick handlers anymore - StructureManager handles that
	}
    
    public void constructStructure(ServerWorld world) {
        constructionManager.constructStructure(world);
    }
    
    public void spawnStructure(ServerWorld world) {
        constructionManager.spawnStructure(world);
    }
    
    public void upgrade(ServerWorld world) {
        upgradeManager.upgrade(world);
    }
    
    protected void loadResourceRequirements()
	{
		String path = String.format("data/frontier/structures/settlement/%s_%d.nbt", this.getType(), tier);
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path))
		{
			if (inputStream != null)
			{
				NbtCompound tag = NbtIo.readCompressed(inputStream);
				NbtList blocksList = tag.getList("blocks", 10);
				NbtList paletteList = tag.getList("palette", 10);

				Map<Integer, String> paletteMap = new HashMap<>();
				for (int i = 0; i < paletteList.size(); i++)
				{
					NbtCompound paletteEntry = paletteList.getCompound(i);
					String blockName = paletteEntry.getString("Name");
					paletteMap.put(i, blockName);
				}

				for (int i = 0; i < blocksList.size(); i++)
				{
					NbtCompound blockEntry = blocksList.getCompound(i);
					int state = blockEntry.getInt("state");
					String blockName = paletteMap.get(state);
					resourceRequirements.put(blockName, resourceRequirements.getOrDefault(blockName, 0) + 1);
				}
			}
			else
				Frontier.LOGGER.error("Structure() - NBT file not found: " + path);
		}
		catch (IOException e) { e.printStackTrace(); }
	}
    
    public int[] getStructureSize() {
        return StructureSerializer.getStructureSize(this.type.toString().toLowerCase(), tier);
    }
    
    public int getLength() {
        return getStructureSize()[0];
    }

    public int getWidth() {
        return getStructureSize()[1];
    }

    public int getHeight() {
        return getStructureSize()[2];
    }
    
    public UUID getLeader() {
        return SettlementManager.getSettlement(faction).getLeader();
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFaction() {
        return faction;
    }

    public void setFaction(String faction) {
        this.faction = faction;
    }

    public StructureType getType() {
        return type;
    }

    public String getTypeLowerCaseString() {
        return type.toString().toLowerCase();
    }
    
    public void setType(StructureType type) {
        this.type = type;
    }
    
    public void setType(String typeStr) {
		try { this.type = StructureType.valueOf(typeStr.toUpperCase()); }
		catch (IllegalArgumentException e) { Frontier.LOGGER.error("Invalid structure type: " + typeStr + "!"); }
	}
    
    public StructureCategory getCategory() {
    	return this.type.getCategory();
    }
    
    public void setCategory(StructureCategory category) {
    	this.category = category;
    }
    
    public BlockPos getPosition() {
        return position;
    }

    public void setPosition(BlockPos position) {
        this.position = position;
    }
    
    public Direction getFacing() {
        return facing;
    }
    
    public int getTier() {
        return tier;
    }
    
    public void setTier(int tier) {
        this.tier = tier;
        loadResourceRequirements();
    }
    
    public int getMaxTier() {
        return maxTier;
    }
    
    public void setMaxTier(int maxTier) {
        this.maxTier = maxTier;
    }

    public UUID getUUID() {
        return uuid;
    }
    
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    
    public boolean isConstructed() {
        return isConstructed;
    }
    
    public void setConstructed(boolean isConstructed) {
        this.isConstructed = isConstructed;
    }
    
    public boolean canConstruct() {
        return constructionManager.canConstruct();
    }
    
    public boolean isConstructing() {
        return constructionManager.isConstructing();
    }
    
    public void setConstructing(boolean isConstructing) {
        constructionManager.setConstructing(isConstructing);
    }
    
    public boolean requiresRepair() {
        return repairManager.requiresRepair();
    }
    
    public void setRepair(boolean requiresRepair) {
        repairManager.setRepair(requiresRepair);
    }
    
    public boolean isUpgrading() {
        return upgradeManager.isUpgrading();
    }
    
    public void setUpgrading(boolean isUpgrading) {
        upgradeManager.setUpgrading(isUpgrading);
    }
    
    public boolean isClearing() {
        return constructionManager.isClearing();
    }
    
    public void setClearing(boolean isClearing) {
        constructionManager.setClearing(isClearing);
    }

    public int getConstructionTicksElapsed() {
        return constructionManager.getConstructionTicksElapsed();
    }
    
    public void setConstructionTicksElapsed(int ticksElapsed) {
        constructionManager.setConstructionTicksElapsed(ticksElapsed);
    }
    
    public int getUpgradeTicksElapsed() {
        return upgradeManager.getUpgradeTicksElapsed();
    }
    
    public void setUpgradeTicksElapsed(int ticksElapsed) {
        upgradeManager.setUpgradeTicksElapsed(ticksElapsed);
    }
    
    public int getRepairTicksElapsed() {
        return repairManager.getRepairTicksElapsed();
    }
    
    public void setRepairTicksElapsed(int ticksElapsed) {
        repairManager.setRepairTicksElapsed(ticksElapsed);
    }
    
    public StructureConstructionManager getConstructionManager() {
        return constructionManager;
    }
    
    public StructureUpgradeManager getUpgradeManager() {
        return upgradeManager;
    }
    
    public StructureRepairManager getRepairManager() {
        return repairManager;
    }
    
    public StructureInventoryManager getInventoryManager() {
        return inventoryManager;
    }
    
    public boolean isDamaged(ServerWorld world) {
        return repairManager.isDamaged(world);
    }
    
    public List<ItemStack> getStructureContents(ServerWorld world) {
        return inventoryManager.getStructureContents(world);
    }
    
    public List<ItemStack> getStructureInventory(ServerWorld world) {
        return inventoryManager.getStructureInventory(world);
    }
    
    public boolean upgradeAvailable() {
        return upgradeManager.upgradeAvailable();
    }
}