package com.frontier.entities.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.frontier.entities.settler.ArchitectEntity;
import com.frontier.settlements.SettlementManager;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

public class HireSettler
{
	private static final Map<String, String> SKILL_TRANSFERS = createSkills();
	
	private static Map<String, String> createSkills()
    {
        Map<String, String> skillTransfers = new HashMap<>();
        skillTransfers.put("ARCHITECT", "GOVERNING");
        skillTransfers.put("COURIER",   "GOVERNING");
        skillTransfers.put("DELIVERER", "GOVERNING");
        skillTransfers.put("INNKEEPER", "GOVERNING");
        skillTransfers.put("MERCHANT",  "GOVERNING");
        skillTransfers.put("PRIEST",    "GOVERNING");

        skillTransfers.put("ARCHER", "MILITARY");
        skillTransfers.put("CLERIC", "MILITARY");
        skillTransfers.put("KNIGHT", "MILITARY");

        skillTransfers.put("FARMER",     "LABORING");
        skillTransfers.put("FISHERMAN",  "LABORING");
        skillTransfers.put("LUMBERJACK", "LABORING");
        skillTransfers.put("MINER",      "LABORING");

        skillTransfers.put("ALCHEMIST",    "CRAFTING");
        skillTransfers.put("ARCANIST",     "CRAFTING");
        skillTransfers.put("BLACKSMITH",   "CRAFTING");
        skillTransfers.put("CARTOGRAPHER", "CRAFTING");
        skillTransfers.put("FLETCHER",     "CRAFTING");
        skillTransfers.put("TANNER",       "CRAFTING");

        skillTransfers.put("BEEKEEPER",  "RANCHING");
        skillTransfers.put("COWHAND",    "RANCHING");
        skillTransfers.put("POULTRYMAN", "RANCHING");
        skillTransfers.put("SHEPHERD",   "RANCHING");
        skillTransfers.put("STABLEHAND", "RANCHING");
        skillTransfers.put("SWINEHERD",  "RANCHING");

        skillTransfers.put("BAKER",       "ARTISAN");
        skillTransfers.put("COOK",        "ARTISAN");
        skillTransfers.put("GREENGROCER", "ARTISAN");
        skillTransfers.put("CARPENTER",   "ARTISAN");
        skillTransfers.put("MASON",       "ARTISAN");

        return skillTransfers;
    }

	public static void architect(ArchitectEntity architect, String firstName, String lastName, String name, String faction, String profession, String expertise, int hunger, int morale, int skill, UUID uuid, String gender, World world)
	{
		LocalDifficulty difficulty = world.getLocalDifficulty(architect.getBlockPos());
	    SpawnReason spawnReason = SpawnReason.NATURAL;
	    EntityData entityData = null;
	    NbtCompound entityNbt = new NbtCompound();
	    
	    Random rand = new Random();

	    architect.setSettlerFirstName(firstName);
	    architect.setSettlerLastName(lastName);
	    architect.setSettlerName(name);
	    architect.setSettlerGender(gender);
	    architect.setSettlerProfession(profession);
	    architect.setSettlerFaction(faction);
	    architect.setSettlerExpertise(expertise);
	    architect.setSettlerHunger(hunger);
	    architect.setSettlerMorale(morale);
	    architect.setUuid(uuid);
	    
	    int settlerSkill = expertise.equals(SKILL_TRANSFERS.get(profession.toUpperCase())) ? skill : 0;
	    architect.setSettlerSkill(settlerSkill);

	    // choose random texture depending on gender
	    int textureIdx;
	    if (gender.equals("MALE")) 
	    	textureIdx = rand.nextInt(ArchitectEntity.ARCHITECT_TEXTURES_MALE.length);
	    else 
	    	textureIdx = rand.nextInt(ArchitectEntity.ARCHITECT_TEXTURES_FEMALE.length);
	    architect.setSettlerTexture(textureIdx);
	    
	    architect.initialize((ServerWorldAccess) world, difficulty, spawnReason, entityData, entityNbt);
	    SettlementManager.getSettlement(faction).addSettler(architect);
	    SettlementManager.saveSettlements(world.getServer());
	    ArchitectEntity.saveEntityData(architect, world);
	    world.spawnEntity(architect);
	}
}