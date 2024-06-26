package com.frontier.settlements;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import com.frontier.PlayerData;
import com.frontier.gui.AbandonSettlementScreen;
import com.frontier.gui.CreateSettlementScreen;
import com.frontier.structures.Structure;
import com.frontier.structures.TownHall;
import com.frontier.structures.Warehouse;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;

public class SettlementManager
{
    private static Map<String, Settlement> settlements = new HashMap<>();
    private static BlockPos bellPos = null;

    public static void registerCallback()
	{
	    UseBlockCallback.EVENT.register((player, world, hand, blockHitResult) -> 
	    {
	    	PlayerData playerData = PlayerData.map.get(player.getUuid());
	    	if (playerData != null)
	    	{
	    		if (!world.isClient && !playerData.getProfession().equals("Leader") && player.getStackInHand(hand).getItem() == Items.CLOCK && world.getBlockState(blockHitResult.getBlockPos()).getBlock() == Blocks.BELL)
	    			bellPos = blockHitResult.getBlockPos();
	    		
	    		if (world.isClient && !playerData.getProfession().equals("Leader") && player.getStackInHand(hand).getItem() == Items.CLOCK && world.getBlockState(blockHitResult.getBlockPos()).getBlock() == Blocks.BELL)
		        {
		        	MinecraftClient.getInstance().setScreen(new CreateSettlementScreen());
		            return ActionResult.SUCCESS;
		        }
		        
		        else if (world.isClient && playerData.getProfession().equals("Leader") && player.getStackInHand(hand).getItem() == Items.COMPASS && world.getBlockState(blockHitResult.getBlockPos()).getBlock() == Blocks.BELL) 
		        {
		        	MinecraftClient.getInstance().setScreen(new AbandonSettlementScreen());
		            return ActionResult.SUCCESS;
		        }
	    	}
	    	return ActionResult.PASS;
	    });
	}
    
    public static Settlement createSettlement(UUID leader, String factionName, MinecraftServer server)
    {
        PlayerData playerData = PlayerData.map.get(leader);
        if(playerData != null)
        {
            if (!settlementExists(factionName)) // TODO: check for faction names that aren't necessarily settlements
            {
                BlockPos newSettlementPos = playerData.getPlayer(server).getBlockPos();
                if (!isTerritoryOverlap(newSettlementPos))
                {
                    Settlement settlement = new Settlement(factionName, leader, newSettlementPos, server);
                    settlements.put(factionName, settlement);
                    
                    ServerPlayerEntity player = playerData.getPlayer(server);
                    BlockPos townHallPos = getFrontPosition(player, 2);
                    Direction facing = player.getHorizontalFacing();
                    settlement.constructStructure("townhall", townHallPos, server.getOverworld(), facing);
                    
                    if (settlement.abortSettlementCreation)
                    {
                    	playerData.getPlayer(server).sendMessage(Text.literal("Cannot construct townhall here, choose a more suitable location!").styled(style -> style.withColor(Formatting.WHITE)), false);
                    	settlements.remove(factionName);
                    	return null;
                    }
                    
                    server.getOverworld().setBlockState(bellPos, Blocks.AIR.getDefaultState());
                    return settlement;
                } 
                else
                {
                    playerData.getPlayer(server).sendMessage(Text.literal("Settlement not created due to territory overlap with another settlement!").styled(style -> style.withColor(Formatting.WHITE)), false);
                    return null;
                }
            }
            else
            {
                playerData.getPlayer(server).sendMessage(Text.literal("Settlement not created, faction name is taken!").styled(style -> style.withColor(Formatting.WHITE)), false);
                return null;
            }
        }
        System.err.println("createSettlement() playerData is null!");
        return null;
    }
    
    public static void create(UUID leader, String factionName, MinecraftServer server)
	{
		Settlement newSettlement = createSettlement(leader, factionName, server);
		PlayerData playerData = PlayerData.map.get(leader);
		
		if (playerData != null)
		{
			if (newSettlement != null)
		    {
		    	playerData.setFaction(factionName);
	    		playerData.setProfession("Leader");
	    		playerData.saveData();
	    		
	    		newSettlement.addPlayer(playerData.getPlayer(server).getUuid());
	    		newSettlement.setLeader(playerData.getPlayer(server).getUuid());
	    		saveSettlements(server);
	    		
		    	playerData.getPlayer(server).sendMessage(Text.literal("You're now the leader of a new settlement called " + factionName + "! Ring the bell with a clock to request nomads or ring it with a compass to abandon your settlement.").styled(style -> style.withColor(Formatting.WHITE)), false);
		    }
		}
		else
			System.err.println("create() playerData is null!");
    }
	
    public static void abandon(UUID leader, String factionName, MinecraftServer server)
	{
		PlayerData playerData = PlayerData.map.get(leader);
		loadSettlements(server);

		if (playerData != null)
		{
			if(playerData.getPlayer(server) != null)
	    	{
				playerData.getPlayer(server).sendMessage(Text.literal("You've abandoned " + playerData.getFaction() + "!").styled(style -> style.withColor(Formatting.WHITE)), false);
				
				getSettlement(factionName).getPlayers().remove(playerData.getPlayer(server).getUuid());
	    		playerData.setFaction("N/A");
	    		playerData.setProfession("Adventurer");
	    		playerData.saveData();
	    	}
		}
		else
			System.err.println("abandon() playerData is null!");
		
		electNewLeader(leader, factionName, server);
    }
	
	private static void electNewLeader(UUID leader, String faction, MinecraftServer server)
	{
		if (getSettlement(faction).getSettlers().isEmpty() && getSettlement(faction).getPlayers().isEmpty())
		{
			if (server != null)
			{
			    PlayerManager playerManager = server.getPlayerManager();
			    Text message = Text.literal("The faction '" + faction + "' has been disbanded!").styled(style -> style.withColor(Formatting.WHITE));
			    playerManager.broadcast(message, false);
			}
			deleteSettlement(faction);
			saveSettlements(server);
		}
		
		else if (!getSettlement(faction).getPlayers().isEmpty())
		{
		    List<UUID> players = new ArrayList<>(getSettlement(faction).getPlayers());
		    players.removeIf(uuid -> uuid.equals(leader));

		    Random rand = new Random();
		    UUID newLeader = players.get(rand.nextInt(players.size()));
		    getSettlement(faction).setLeader(newLeader);
			PlayerData.map.get(newLeader).setProfession("Leader");
			PlayerData.map.get(newLeader).saveData();
			saveSettlements(server);
		}
		
		else
		{
			// TODO: assign the most appropriate settler as the leader
			saveSettlements(server);
		}
	}
	
	public static void switchPlayerSettlement(UUID uuid, String factionFrom, String factionTo, MinecraftServer server)
	{
		if (settlementExists(factionFrom))
		{
			if(getSettlement(factionFrom).getLeader() == uuid)
				abandon(uuid, factionFrom, server);
			else
				getSettlement(factionFrom).removePlayer(uuid);
		}
		
		if (settlementExists(factionTo))
			getSettlement(factionTo).addPlayer(uuid);
		
		saveSettlements(server);
	}
    
	public static void drawSettlementBorder(MinecraftServer server)
	{
		for (Map.Entry<String, Settlement> entry : settlements.entrySet())
        {
            Settlement settlement = entry.getValue();
            if (settlement.isLeaderHoldingClock(server))
            	settlement.drawTerritoryEdge(server);
        }
	}
	
	public static void saveSettlements(MinecraftServer server)
	{
	    File saveDir = server.getSavePath(WorldSavePath.ROOT).toFile();
	    File nbtFile = new File(saveDir, "thefrontier/SettlementData.nbt");
	    NbtCompound rootNbt;

	    if (nbtFile.exists())
	    {
	        try
	        {
	            rootNbt = NbtIo.readCompressed(nbtFile);
	        }
	        catch (IOException e)
	        {
	        	e.printStackTrace(); 
	        	return;
	        }
	    }
	    else
	    {
	        rootNbt = new NbtCompound();
	        try
	        {
	            nbtFile.createNewFile();
	        }
	        catch (IOException e) { e.printStackTrace(); }
	    }

	    NbtCompound settlementsNbt = new NbtCompound();
	    for (Map.Entry<String, Settlement> entry : settlements.entrySet())
	    {
	        String factionName = entry.getKey();
	        Settlement settlement = entry.getValue();
	        NbtCompound settlementNbt = new NbtCompound();

	        // save territory
	        NbtList territoryNbt = new NbtList();
	        for (ChunkPos chunk : settlement.getTerritory())
	        {
	            NbtCompound chunkNbt = new NbtCompound();
	            chunkNbt.putInt("X", chunk.x);
	            chunkNbt.putInt("Z", chunk.z);
	            territoryNbt.add(chunkNbt);
	        }

	        // save reputations
	        NbtCompound reputationsNbt = new NbtCompound();
	        for (Map.Entry<UUID, Integer> repEntry : settlement.getReputations().entrySet())
	            reputationsNbt.putInt(repEntry.getKey().toString(), repEntry.getValue());

	        // save players
	        NbtList playersNbt = new NbtList();
	        for (UUID playerUuid : settlement.getPlayers())
	            playersNbt.add(NbtString.of(playerUuid.toString()));

	        // save allies and enemies
	        NbtList alliesNbt = new NbtList();
	        for (String ally : settlement.getAllies())
	            alliesNbt.add(NbtString.of(ally));

	        NbtList enemiesNbt = new NbtList();
	        for (String enemy : settlement.getEnemies())
	            enemiesNbt.add(NbtString.of(enemy));

	        // save structures
	        NbtList structuresNbt = new NbtList();
	        for (Structure structure : settlement.getStructures())
	        {
	            NbtCompound structureNbt = new NbtCompound();
	            structureNbt.putString("Name", structure.getName());
	            structureNbt.putString("Faction", structure.getFaction());
	            structureNbt.putLong("Position", structure.getPosition().asLong());
	            structureNbt.putString("Facing", structure.getFacing().getName());
	            structureNbt.putInt("Tier", structure.getTier());
	            structureNbt.putUuid("UUID", structure.getUUID());
	            structureNbt.putBoolean("IsActive", structure.isActive());
	            structureNbt.putBoolean("IsConstructed", structure.isConstructed());
	            structureNbt.putBoolean("RequiresRepair", structure.requiresRepair());
	            structureNbt.putBoolean("IsConstructing", structure.isConstructing());
	            structureNbt.putBoolean("IsUpgrading", structure.isUpgrading());
	            structureNbt.putBoolean("IsClearing", structure.isClearing());
	            structureNbt.putInt("ConstructionTicksElapsed", structure.getConstructionTicksElapsed());
	            structureNbt.putInt("UpgradeTicksElapsed", structure.getUpgradeTicksElapsed());
	            structureNbt.putInt("RepairTicksElapsed", structure.getRepairTicksElapsed());
	            
	            structureNbt.put("AirBlocksQueue", serializeQueue(structure.getAirBlocksQueue()));
	            structureNbt.put("NonAirBlocksQueue", serializeQueue(structure.getNonAirBlocksQueue()));
	            structureNbt.put("ClearingQueue", serializeQueue(structure.getClearingQueue()));
	            structureNbt.put("UpgradeQueue", serializeQueue(structure.getUpgradeQueue()));
	            structureNbt.put("RepairQueue", serializeRepairQueue(structure.getRepairQueue()));
	            
	            structureNbt.put("ConstructionMap", serializeMap(structure.getConstructionMap()));
	            structureNbt.put("UpgradeMap", serializeMap(structure.getUpgradeMap()));
	            
	            structuresNbt.add(structureNbt);
	        }

	        settlementNbt.putUuid("Leader", settlement.getLeader());
	        settlementNbt.putLong("Position", settlement.getPosition().asLong());
	        settlementNbt.put("Territory", territoryNbt);
	        settlementNbt.put("Reputations", reputationsNbt);
	        settlementNbt.put("Players", playersNbt);
	        settlementNbt.put("Allies", alliesNbt);
	        settlementNbt.put("Enemies", enemiesNbt);
	        settlementNbt.put("Structures", structuresNbt);

	        settlementsNbt.put(factionName, settlementNbt);
	    }

	    rootNbt.put("Settlements", settlementsNbt);

	    try
	    {
	        NbtIo.writeCompressed(rootNbt, nbtFile);
	    }
	    catch (IOException e) { e.printStackTrace(); }
	}

	public static void loadSettlements(MinecraftServer server)
	{
	    File saveDir = server.getSavePath(WorldSavePath.ROOT).toFile();
	    File nbtFile = new File(saveDir, "thefrontier/SettlementData.nbt");

	    if (!nbtFile.exists())
	    {
	        saveSettlements(server);
	        return;
	    }

	    try
	    {
	        NbtCompound rootNbt = NbtIo.readCompressed(nbtFile);
	        if (rootNbt.contains("Settlements"))
	        {
	            NbtCompound settlementsNbt = rootNbt.getCompound("Settlements");

	            for (String factionName : settlementsNbt.getKeys())
	            {
	                NbtCompound settlementNbt = settlementsNbt.getCompound(factionName);

	                UUID leader = settlementNbt.getUuid("Leader");
	                BlockPos position = BlockPos.fromLong(settlementNbt.getLong("Position"));
	                Settlement settlement = new Settlement(factionName, leader, position, server);

	                // load territory
	                if (settlementNbt.contains("Territory", 9))
	                {
	                    NbtList territoryNbt = settlementNbt.getList("Territory", 10);
	                    for (int i = 0; i < territoryNbt.size(); i++) {
	                        NbtCompound chunkNbt = territoryNbt.getCompound(i);
	                        int x = chunkNbt.getInt("X");
	                        int z = chunkNbt.getInt("Z");
	                        ChunkPos chunkPos = new ChunkPos(x, z);
	                        settlement.expandTerritory(chunkPos);
	                    }
	                }

	                // load reputations
	                NbtCompound reputationsNbt = settlementNbt.getCompound("Reputations");
	                for (String uuidKey : reputationsNbt.getKeys())
	                {
	                    UUID uuid = UUID.fromString(uuidKey);
	                    int reputation = reputationsNbt.getInt(uuidKey);
	                    settlement.setReputation(uuid, reputation);
	                }

	                // load players
	                NbtList playersNbt = settlementNbt.getList("Players", 8);
	                for (int i = 0; i < playersNbt.size(); i++)
	                {
	                    UUID playerUuid = UUID.fromString(playersNbt.getString(i));
	                    settlement.addPlayer(playerUuid);
	                }

	                // load allies and enemies
	                NbtList alliesNbt = settlementNbt.getList("Allies", 8);
	                for (int i = 0; i < alliesNbt.size(); i++)
	                    settlement.addAlly(alliesNbt.getString(i));

	                NbtList enemiesNbt = settlementNbt.getList("Enemies", 8);
	                for (int i = 0; i < enemiesNbt.size(); i++)
	                    settlement.addEnemy(enemiesNbt.getString(i));

	                // load structures
	                NbtList structuresNbt = settlementNbt.getList("Structures", 10);
	                for (int i = 0; i < structuresNbt.size(); i++)
	                {
	                    NbtCompound structureNbt = structuresNbt.getCompound(i);
	                    String structureName = structureNbt.getString("Name");
	                    String faction = structureNbt.getString("Faction");
	                    BlockPos structurePos = BlockPos.fromLong(structureNbt.getLong("Position"));
	                    Direction facing = Direction.byName(structureNbt.getString("Facing"));
	                    int tier = structureNbt.getInt("Tier");
	                    UUID uuid = structureNbt.getUuid("UUID");

	                    Structure structure;
	                    switch (structureName)
	                    {
	                        case "townhall":
	                            structure = new TownHall(structureName, faction, structurePos, facing);
	                            break;
	                        case "warehouse":
	                            structure = new Warehouse(structureName, faction, structurePos, facing);
	                            break;
	                        default:
	                            throw new IllegalArgumentException("Unknown structure type: " + structureName);
	                    }
	                    
	                    structure.setTier(tier);
	                    structure.setUUID(uuid);
	                    structure.setActive(structureNbt.getBoolean("IsActive"));
	                    structure.setConstructed(structureNbt.getBoolean("IsConstructed"));
	                    structure.setRepair(structureNbt.getBoolean("RequiresRepair"));
	                    structure.setConstructing(structureNbt.getBoolean("IsConstructing"));
	                    structure.setUpgrading(structureNbt.getBoolean("IsUpgrading"));
	                    structure.setClearing(structureNbt.getBoolean("IsClearing"));
	                    structure.setConstructionTicksElapsed(structureNbt.getInt("ConstructionTicksElapsed"));
	                    structure.setUpgradeTicksElapsed(structureNbt.getInt("UpgradeTicksElapsed"));
	                    structure.setRepairTicksElapsed(structureNbt.getInt("RepairTicksElapsed"));
	                    
	                    structure.setAirBlocksQueue(deserializeQueue(structureNbt.getList("AirBlocksQueue", 10)));
	                    structure.setNonAirBlocksQueue(deserializeQueue(structureNbt.getList("NonAirBlocksQueue", 10)));
	                    structure.setClearingQueue(deserializeQueue(structureNbt.getList("ClearingQueue", 10)));
	                    structure.setUpgradeQueue(deserializeQueue(structureNbt.getList("UpgradeQueue", 10)));
	                    structure.setRepairQueue(deserializeRepairQueue(structureNbt.getList("RepairQueue", 10)));
	                    
	                    structure.setConstructionMap(deserializeMap(structureNbt.getList("ConstructionMap", 10)));
	                    structure.setUpgradeMap(deserializeMap(structureNbt.getList("UpgradeMap", 10)));
	                    
	                    settlement.getStructures().add(structure);
	                }

	                settlements.put(factionName, settlement);
	            }
	        }
	    }
	    catch (IOException e) { e.printStackTrace(); }
	}
	
	private static NbtList serializeMap(Map<BlockPos, BlockState> map)
	{
		NbtList list = new NbtList();
		for (Map.Entry<BlockPos, BlockState> entry : map.entrySet())
		{
			NbtCompound mapEntry = new NbtCompound();
			mapEntry.putLong("Position", entry.getKey().asLong());
			mapEntry.put("BlockState", NbtHelper.fromBlockState(entry.getValue()));
			list.add(mapEntry);
		}
		return list;
	}

	private static Map<BlockPos, BlockState> deserializeMap(NbtList list)
	{
		Map<BlockPos, BlockState> map = new HashMap<>();
		for (int i = 0; i < list.size(); i++)
		{
			NbtCompound mapEntry = list.getCompound(i);
			BlockPos pos = BlockPos.fromLong(mapEntry.getLong("Position"));
			BlockState state = deserializeBlockState(mapEntry.getCompound("BlockState"));
			map.put(pos, state);
		}
		return map;
	}

	private static BlockState deserializeBlockState(NbtCompound compound)
	{
		String blockName = compound.getString("Name");
		Block block = Registries.BLOCK.get(new Identifier(blockName));
		BlockState state = block.getDefaultState();
		NbtCompound properties = compound.getCompound("Properties");
		for (String key : properties.getKeys())
		{
			Property<?> property = block.getStateManager().getProperty(key);
			if (property != null)
				state = applyProperty(state, property, properties.getString(key));
		}
		return state;
	}
	
	private static NbtList serializeQueue(Queue<BlockPos> queue)
	{
	    NbtList list = new NbtList();
	    for (BlockPos pos : queue)
	        list.add(NbtLong.of(pos.asLong()));
	    return list;
	}
	
	private static Queue<BlockPos> deserializeQueue(NbtList list)
	{
	    Queue<BlockPos> queue = new LinkedList<>();
	    list.forEach(item ->
	    {
	        long posLong = ((NbtLong) item).longValue();
	        queue.add(BlockPos.fromLong(posLong));
	    });
	    return queue;
	}
	
	private static NbtList serializeRepairQueue(Queue<Map.Entry<BlockPos, BlockState>> queue)
	{
		NbtList list = new NbtList();
		for (Map.Entry<BlockPos, BlockState> entry : queue)
		{
			NbtCompound compound = new NbtCompound();
			compound.putLong("pos", entry.getKey().asLong());
			compound.put("state", NbtHelper.fromBlockState(entry.getValue()));
			list.add(compound);
		}
		return list;
	}

	private static Queue<Map.Entry<BlockPos, BlockState>> deserializeRepairQueue(NbtList list)
	{
		Queue<Map.Entry<BlockPos, BlockState>> queue = new LinkedList<>();
		list.forEach(item ->
		{
			NbtCompound compound = (NbtCompound) item;
			long posLong = compound.getLong("pos");
			BlockPos pos = BlockPos.fromLong(posLong);
			BlockState state = deserializeBlockState(compound.getCompound("state"));
			queue.add(new AbstractMap.SimpleEntry<>(pos, state));
		});
		return queue;
	}
	
	private static <T extends Comparable<T>> BlockState applyProperty(BlockState state, Property<T> property, String value)
	{
		Optional<T> optional = property.parse(value);
		if (optional.isPresent())
			return state.with(property, optional.get());
		return state;
	}

	private static BlockPos getFrontPosition(ServerPlayerEntity player, int distance)
	{
	    Direction facing = player.getHorizontalFacing();
	    return bellPos.offset(facing, distance);
	}
	
	public static boolean isTerritoryOverlap(BlockPos newSettlementPos)
	{
        for (Settlement existingSettlement : settlements.values())
        {
            Set<ChunkPos> existingTerritory = existingSettlement.getTerritory();
            Set<ChunkPos> proposedTerritory = calculateInitialTerritory(newSettlementPos);
            for (ChunkPos chunk : proposedTerritory)
                if (existingTerritory.contains(chunk))
                    return true;
        }
        return false;
    }

    private static Set<ChunkPos> calculateInitialTerritory(BlockPos center)
    {
        Set<ChunkPos> territory = new HashSet<>();
        int centerX = center.getX() >> 4;
        int centerZ = center.getZ() >> 4;
        int radius = 1; // assuming a radius of 1 chunk for initial territory
        for (int dx = -radius; dx <= radius; dx++)
            for (int dz = -radius; dz <= radius; dz++)
                territory.add(new ChunkPos(centerX + dx, centerZ + dz));
        return territory;
    }
	
	public static void updatePlayerReputations(MinecraftServer server)
	{
        List<ServerPlayerEntity> onlinePlayers = server.getPlayerManager().getPlayerList();
        for (ServerPlayerEntity player : onlinePlayers)
        {
            UUID playerUUID = player.getUuid();
            BlockPos playerPos = player.getBlockPos();
            for (Settlement settlement : settlements.values())
                if (settlement.isWithinTerritory(playerPos))
                    settlement.checkReputation(playerUUID, server);
        }
    }
    
	public static String getSettlementTerritory(BlockPos pos)
	{
        for (Map.Entry<String, Settlement> entry : settlements.entrySet())
        {
            String settlementName = entry.getKey();
            Settlement settlement = entry.getValue();
            if (settlement.isWithinTerritory(pos))
                return settlementName;
        }
        return "Uncontested";
    }
	
    public static Map<String, Settlement> getSettlements() {
        return settlements;
    }
    
    public static Settlement getSettlement(String name) {
        return settlements.get(name);
    }
    
    public static boolean settlementExists(String name) {
    	return settlements.containsKey(name);
    }

    public static void deleteSettlement(String name) {
        settlements.remove(name);
    }
    
    public static void reset() {
        settlements.clear();
    }
}