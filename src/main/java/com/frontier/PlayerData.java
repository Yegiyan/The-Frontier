package com.frontier;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.frontier.settlements.SettlementManager;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;

public class PlayerData 
{
	public static Map<UUID, PlayerData> players = new HashMap<>();
    protected ServerPlayerEntity player;
    private MinecraftServer server;
    private UUID uuid;
    
    private String name;
    private String faction;
    private String profession;
    private int renown;
    
    public PlayerData(UUID uuid, String name, String faction, String profession, int renown, MinecraftServer server)
    {
    	this.uuid = uuid;
        this.name = name;
        this.faction = faction;
        this.profession = profession;
        this.renown = renown;
        this.server = server;
    }
    
    public void updateRenown(int score)
    {
        this.renown += score;
        this.renown = clamp(this.renown, -100, 100);
    }
    
    public void saveData()
    {
        File saveDir = server.getSavePath(WorldSavePath.ROOT).toFile();
        File nbtFile = new File(saveDir, "thefrontier/EntityData.nbt");
        NbtCompound rootNbt;
        
        if (nbtFile.exists()) 
        {
            try { rootNbt = NbtIo.readCompressed(nbtFile); }
            catch (IOException e) 
            { 
                e.printStackTrace();
                return;
            }
        }
        
        else
        {
            rootNbt = new NbtCompound();
            try { nbtFile.createNewFile(); } 
            catch (IOException e) { e.printStackTrace(); }
        }

        NbtCompound playersNbt = new NbtCompound();
        for (UUID uuid : players.keySet()) 
        {
            PlayerData playerData = players.get(uuid);
            NbtCompound playerNbt = new NbtCompound();
            String playerFaction = playerData.getFaction();
            NbtCompound factionNbt = playersNbt.getCompound(playerFaction);

            playerNbt.putString("Name", playerData.getName());
            playerNbt.putString("Profession", playerData.getProfession());
            playerNbt.putInt("Renown", playerData.getRenown());
            factionNbt.put(uuid.toString(), playerNbt);
            playersNbt.put(playerFaction, factionNbt);
        }

        rootNbt.put("Players", playersNbt);

        try
        {
        	NbtIo.writeCompressed(rootNbt, nbtFile);
        	Frontier.LOGGER.info("Saving player data");
        } 
        catch (IOException e) { e.printStackTrace(); }
    }

    public static void loadData(MinecraftServer server)
    {
        File saveDir = server.getSavePath(WorldSavePath.ROOT).toFile();
        File nbtFile = new File(saveDir, "thefrontier/EntityData.nbt");

        if (!nbtFile.exists()) return;

        try
        {
            NbtCompound rootNbt = NbtIo.readCompressed(nbtFile);
            NbtCompound playersNbt = rootNbt.getCompound("Players");

            for (String faction : playersNbt.getKeys())
            {
                NbtCompound factionNbt = playersNbt.getCompound(faction);
                for (String uuidString : factionNbt.getKeys())
                {
                    UUID uuid = UUID.fromString(uuidString);
                    NbtCompound playerNbt = factionNbt.getCompound(uuidString);
                    PlayerData playerData = new PlayerData
                    (
                        uuid,
                        playerNbt.getString("Name"),
                        faction,
                        playerNbt.getString("Profession"),
                        playerNbt.getInt("Renown"),
                        server
                    );
                    players.put(uuid, playerData);
                }
            }
            Frontier.LOGGER.info("Loading player data");
        }
        catch (IOException e) { e.printStackTrace(); }
    }
    
    public ServerPlayerEntity getPlayer(MinecraftServer server) {
        if (server == null) return null;
        return server.getPlayerManager().getPlayer(this.uuid);
    }

    public void setPlayer(ServerPlayerEntity player) {
        this.player = player;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public UUID getUUID() {
        return uuid;
    }

    public String getFaction() {
        return faction;
    }

    public void setFaction(String faction) {
        this.faction = faction;
    }
    
    public String getProfession() {
        return profession;
    }
    
    public int getRenown() {
        return renown;
    }
    
    public String getRenownAsString() {
        return String.valueOf(this.renown);
    }

    public void setRenown(int renown) {
    	renown = clamp(renown, -100, 100);
        this.renown = renown;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }
    
    public int getReputation(String settlement) {
    	return SettlementManager.getSettlement(settlement).getReputation(getUUID());
    }
    
    public static void reset() {
        players.clear();
    }
    
    int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }  
}