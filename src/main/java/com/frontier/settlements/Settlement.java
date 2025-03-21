package com.frontier.settlements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.frontier.PlayerData;
import com.frontier.entities.settler.SettlerEntity;
import com.frontier.structures.Structure;
import com.frontier.structures.StructureType;
import com.frontier.util.FrontierUtil;

import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class Settlement
{
    private String name;
    private UUID uuid;
    private UUID leader;
    private BlockPos position;
    private Set<ChunkPos> territory;
    
    private Map<UUID, Integer> reputations;
    private List<Structure> structures;
    private List<Grave> graves;
    
    private List<SettlerEntity> settlers;
    
    private List<UUID> players;
    private List<UUID> allies;
    private List<UUID> enemies;
    
    private List<String> alliedFactions;
    private List<String> enemyFactions;
    
    private HashMap<String, Integer> statistics;
    
    private final int TERRITORY_CHUNK_RADIUS = 4; // 4 = 128x128 radius (4 chunks in each direction) - prob make this a 6 tbh
    
    public boolean abortSettlementCreation = false;

    public Settlement(String name, UUID leader, BlockPos position, MinecraftServer server)
    {
        this.name = name;
        this.uuid = UUID.randomUUID();
        this.leader = leader;
        this.position = position;
        this.territory = new HashSet<>();
        this.reputations = new HashMap<>();
        this.structures = new ArrayList<>();
        this.graves = new ArrayList<>();
        this.settlers = new ArrayList<>();
        this.players = new ArrayList<>();
        this.allies = new ArrayList<>();
        this.enemies = new ArrayList<>();
        this.alliedFactions = new ArrayList<>();
        this.enemyFactions = new ArrayList<>();
        this.statistics = new HashMap<>();
        
        initSettlementStats();
        generateTerritory();
    }
    
    private void generateTerritory()
    {
        int centerX = this.position.getX() >> 4;
        int centerZ = this.position.getZ() >> 4;
        for (int dx = -TERRITORY_CHUNK_RADIUS; dx <= TERRITORY_CHUNK_RADIUS; dx++)
            for (int dz = -TERRITORY_CHUNK_RADIUS; dz <= TERRITORY_CHUNK_RADIUS; dz++)
                this.territory.add(new ChunkPos(centerX + dx, centerZ + dz));
        // TODO: add territory expansion from watchtowers here
    }
    
    private void initSettlementStats()
    {
        // population
        statistics.put("Players", 0);
        statistics.put("Settlers", 0);
        statistics.put("Visitors", 0);
        statistics.put("Merchants", 0);
        
        // infrastructure
        statistics.put("Structures", 0);
        statistics.put("Housing", 0);
        statistics.put("Roads", 0);
        statistics.put("Graves", 0);
        
        // economy
        statistics.put("Trades", 0);
        statistics.put("Patrons", 0);
        statistics.put("Festivals", 0);
        statistics.put("Influence", 0);
        
        // security
        statistics.put("Militia", 0);
        statistics.put("Towers", 0);
        statistics.put("Bounties", 0);
        statistics.put("Bandits", 0);
    }
    
    // upgrade all eligible structures
    public void upgradeStructures(ServerWorld world)
    {
        for (Structure structure : structures)
            if (structure.upgradeAvailable() && structure.isConstructed() && !structure.isUpgrading())
                structure.upgrade(world);
    }
    
    // upgrade a specific structure
    public boolean upgradeStructure(String structureName, ServerWorld world)
    {
        Structure structure = getStructureByName(structureName);
        if (structure != null && structure.upgradeAvailable() && structure.isConstructed() && !structure.isUpgrading())
        {
            structure.upgrade(world);
            return true;
        }
        return false;
    }
    
    // repair all eligible structures
    public void repairStructures(ServerWorld world)
    {
        for (Structure structure : structures)
            if (structure.requiresRepair() && structure.isConstructed() && !structure.isUpgrading())
                structure.getRepairManager().repairStructure(world);
    }
    
    // repair a specific structure
    public boolean repairStructure(String structureName, ServerWorld world)
    {
        Structure structure = getStructureByName(structureName);
        if (structure != null && structure.requiresRepair() && structure.isConstructed() && !structure.isUpgrading())
        {
            structure.getRepairManager().repairStructure(world);
            return true;
        }
        return false;
    }
    
	public List<Structure> getStructuresByType(StructureType type)
	{
		List<Structure> result = new ArrayList<>();
		for (Structure structure : structures)
			if (structure.getType() == type)
				result.add(structure);
		return result;
	}

	public List<Structure> getActiveStructures()
	{
		List<Structure> result = new ArrayList<>();
		for (Structure structure : structures)
			if (structure.isActive())
				result.add(structure);
		return result;
	}

	public List<Structure> getDamagedStructures(ServerWorld world)
	{
		List<Structure> result = new ArrayList<>();
		for (Structure structure : structures)
			if (structure.isDamaged(world))
				result.add(structure);
		return result;
	}
    
    public boolean isWithinTerritory(BlockPos pos)
    {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
        return this.territory.contains(chunkPos);
    }

    public boolean isLeaderHoldingClock(MinecraftServer server)
    {
        ServerWorld world = server.getOverworld();
        ServerPlayerEntity leaderPlayer = (ServerPlayerEntity) world.getPlayerByUuid(this.leader);
        
        if (leaderPlayer == null)
            return false;
        
        boolean isMainHandClock = leaderPlayer.getMainHandStack().getItem() == Items.CLOCK;
        boolean isOffHandClock = leaderPlayer.getOffHandStack().getItem() == Items.CLOCK;
        
        return isMainHandClock || isOffHandClock;
    }
    
    public void expandTerritory(ChunkPos chunk) {
        this.territory.add(chunk);
    }

    public void contractTerritory(ChunkPos chunk) {
        this.territory.remove(chunk);
    }
    
    public void addStructure(Structure structure) {
    	this.structures.add(structure);
    }
    
    public String getName() {
        return name;
    }
    
    public UUID getUUID() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }
    
    public BlockPos getPosition() {
        return position;
    }

    public void setPosition(BlockPos position) {
        this.position = position;
    }
    
    public Set<ChunkPos> getTerritory() {
        return this.territory;
    }

    public Map<UUID, Integer> getReputations() {
        return this.reputations;
    }
    
    public int getReputation(UUID uuid) {
        reputations.putIfAbsent(uuid, 0);
        return reputations.get(uuid);
    }
    
    public void setReputation(UUID uuid, int rep) {
        this.reputations.put(uuid, FrontierUtil.clamp(rep, -100, 100));
    }
    
    public void updateReputation(UUID uuid, int rep) {
        this.reputations.put(uuid, this.reputations.get(uuid) + FrontierUtil.clamp(rep, -100, 100));
    }
    
    // might have to make a separate method for other entities
    public void checkReputation(UUID uuid, MinecraftServer server) {
        if (reputations.putIfAbsent(uuid, 0) == null)
            SettlementManager.saveSettlements(server);
    }

    public List<Structure> getStructures() {
        return structures;
    }
    
    public Structure getStructureByName(String name)
    {
        for (Structure structure : structures)
            if (structure.getName().equals(name))
                return structure;
        return null;
    }
    
    public Structure getStructureByType(StructureType type)
    {
        for (Structure structure : structures)
            if (structure.getType().equals(type))
                return structure;
        return null;
    }
    
    public Structure getStructureByUUID(UUID uuid)
    {
        for (Structure structure : structures)
            if (structure.getUUID().equals(uuid))
                return structure;
        return null;
    }

    public List<Grave> getGraves() {
        return graves;
    }
    
    public void addGrave(Grave grave) {
        this.graves.add(grave);
        updateStatistic("Graves", 1);
    }
    
    public void removeGrave(String name) {
        if (graves.removeIf(grave -> grave.getName().equals(name)))
            updateStatistic("Graves", -1);
    }
    
    public List<SettlerEntity> getSettlers() {
        return settlers;
    }

    public void addSettler(SettlerEntity settler) {
        this.settlers.add(settler);
        updateStatistic("Settlers", 1);
    }
    
    public SettlerEntity getSettlerByUUID(UUID uuid)
    {
        for (SettlerEntity settler : settlers)
            if (settler.getUuid().equals(uuid))
                return settler;
        return null;
    }
    
    public void removeSettler(UUID uuid) {
        if (settlers.removeIf(settler -> settler.getUuid().equals(uuid)))
            updateStatistic("Settlers", -1);
    }

    public List<UUID> getPlayers() {
        return players;
    }
    
    public PlayerData getPlayerData(UUID uuid) {
        return PlayerData.players.get(uuid);
    }

    public void addPlayer(UUID player) {
        this.players.add(player);
        updateStatistic("Players", 1);
    }
    
    public void removePlayer(UUID player) {
        if (this.players.remove(player))
            updateStatistic("Players", -1);
    }
    
    public List<UUID> getAllies() {
        return allies;
    }

    public void addAlly(UUID faction) {
        this.allies.add(faction);
    }

    public List<UUID> getEnemies() {
        return enemies;
    }

    public void addEnemy(UUID faction) {
        this.enemies.add(faction);
    }

    public List<String> getAlliedFactions() {
        return alliedFactions;
    }

    public void addAllyFaction(String faction) {
        this.alliedFactions.add(faction);
    }

    public List<String> getEnemyFactions() {
        return enemyFactions;
    }

    public void addEnemyFaction(String faction) {
        this.enemyFactions.add(faction);
    }
    
    public HashMap<String, Integer> getStatistics() {
        return statistics;
    }

    public void updateStatistic(String key, int amount) // always non-negative
    {
        int currentValue = statistics.getOrDefault(key, 0);
        int newValue = currentValue + amount;
        if (newValue >= 0) statistics.put(key, newValue);
    }
}