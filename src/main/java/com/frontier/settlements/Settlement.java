package com.frontier.settlements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.frontier.entities.SettlerEntity;
import com.frontier.structures.Structure;
import com.frontier.structures.TownHall;
import com.frontier.structures.Warehouse;

import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;

public class Settlement
{
    private String name;
	private UUID leader;
	private BlockPos position;
	private Set<ChunkPos> territory;
    
    private Map<UUID, Integer> reputations;
    private List<Structure> structures;
    
    private List<SettlerEntity> settlers;
    private List<UUID> players;
    
    private List<String> allies;
    private List<String> enemies;
    
    private final int territoryChunkRadius = 1;

    public Settlement(String name, UUID leader, BlockPos position, MinecraftServer server)
    {
        this.name = name;
        this.leader = leader;
        this.position = position;
        this.territory = new HashSet<>();
        this.reputations = new HashMap<>();
        this.structures = new ArrayList<>();
        this.settlers = new ArrayList<>();
        this.players = new ArrayList<>();
        this.allies = new ArrayList<>();
        this.enemies = new ArrayList<>();
        generateTerritory();
    }
    
    private void generateTerritory() // everytime we loadSettlements() this method is called so faction territories persist across sessions
	{
        int centerX = this.position.getX() >> 4;
        int centerZ = this.position.getZ() >> 4;
        for (int dx = -territoryChunkRadius; dx <= territoryChunkRadius; dx++)
            for (int dz = -territoryChunkRadius; dz <= territoryChunkRadius; dz++)
                this.territory.add(new ChunkPos(centerX + dx, centerZ + dz));
        // TODO: add territory expansion from watchtowers here
    }
    
    protected void constructStructure(String structureName, BlockPos position, ServerWorld world, Direction facing)
    {
        Structure structure;
        switch (structureName)
        {
            case "town_hall":
                structure = new TownHall(structureName, this.name, position, facing);
                break;
            case "warehouse":
                structure = new Warehouse(structureName, this.name, position, facing);
                break;
            default:
                throw new IllegalArgumentException("Unknown structure type: " + structureName);
        }

        structure.constructStructure(world);
        structures.add(structure);
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
    
    public void drawTerritoryEdge(MinecraftServer server)
    {
        ServerWorld world = server.getOverworld();
        ServerPlayerEntity leader = (ServerPlayerEntity) world.getPlayerByUuid(this.leader);

        if (leader == null)
            return;

        BlockPos leaderPos = leader.getBlockPos();
        final int visibilityBlockRange = 24;

        // calculate territory bounds
        int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (ChunkPos chunkPos : this.territory)
        {
            minX = Math.min(minX, chunkPos.getStartX());
            minZ = Math.min(minZ, chunkPos.getStartZ());
            maxX = Math.max(maxX, chunkPos.getEndX() + 1);
            maxZ = Math.max(maxZ, chunkPos.getEndZ() + 1);
        }

        // iterate over the bounding box of the territory
        ParticleEffect particleEffect = ParticleTypes.HAPPY_VILLAGER;
        for (int x = minX; x <= maxX; x++)
        {
            for (int z = minZ; z <= maxZ; z++)
            {
                boolean isEdge = x == minX || x == maxX || z == minZ || z == maxZ;
                boolean isWithinRange = Math.abs(leaderPos.getX() - x) <= visibilityBlockRange && Math.abs(leaderPos.getZ() - z) <= visibilityBlockRange;
                if (isEdge && isWithinRange)
                {
                	world.spawnParticles(particleEffect, x, leaderPos.getY() + 0, z, 1, 0, 0, 0, 0.1);
                	world.spawnParticles(particleEffect, x, leaderPos.getY() + 1, z, 1, 0, 0, 0, 0.1);
                	world.spawnParticles(particleEffect, x, leaderPos.getY() + 2, z, 1, 0, 0, 0, 0.1);
                }
            }
        }
    }
    
    public void expandTerritory(ChunkPos chunk) {
        this.territory.add(chunk);
    }

    public void contractTerritory(ChunkPos chunk) {
        this.territory.remove(chunk);
    }
    
    public String getName() {
		return name;
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
		this.reputations.put(uuid, clamp(rep, -100, 100));
	}
	
	public void updateReputation(UUID uuid, int rep) {
		this.reputations.put(uuid, this.reputations.get(uuid) + clamp(rep, -100, 100));
	}
	
	public void checkReputation(UUID uuid, MinecraftServer server) { // might have to make a separate method for other entities
    	reputations.putIfAbsent(uuid, 0);
    	SettlementManager.saveSettlements(server);
    }

	public List<Structure> getStructures() {
		return structures;
	}

	public List<SettlerEntity> getSettlers() {
		return settlers;
	}

	public void addSettler(SettlerEntity settler) {
		this.settlers.add(settler);
	}
	
	public void removeSettler(SettlerEntity settler) {
		this.settlers.remove(settler);
	}

	public List<UUID> getPlayers() {
		return players;
	}

	public void addPlayer(UUID player) {
		this.players.add(player);
	}
	
	public void removePlayer(UUID player)
	{
		this.players.remove(player);
	}

	public List<String> getAllies() {
		return allies;
	}

	public void addAlly(String faction) {
		this.allies.add(faction);
	}

	public List<String> getEnemies() {
		return enemies;
	}

	public void addEnemy(String faction) {
		this.enemies.add(faction);
	}
	
	int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}