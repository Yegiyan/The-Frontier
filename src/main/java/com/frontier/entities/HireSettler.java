package com.frontier.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.frontier.settlements.SettlementManager;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
        skillTransfers.put("DELIVERER", "GOVERNING");
        skillTransfers.put("COURIER",   "GOVERNING");
        skillTransfers.put("PRIEST",    "GOVERNING");
        skillTransfers.put("INNKEEPER", "GOVERNING");

        skillTransfers.put("ARCHER", "MILITARY");
        skillTransfers.put("KNIGHT", "MILITARY");
        skillTransfers.put("CLERIC", "MILITARY");

        skillTransfers.put("FARMER",     "HARVESTING");
        skillTransfers.put("MINER",      "HARVESTING");
        skillTransfers.put("LUMBERJACK", "HARVESTING");
        skillTransfers.put("FISHERMAN",  "HARVESTING");

        skillTransfers.put("ALCHEMIST",    "CRAFTING");
        skillTransfers.put("BLACKSMITH",   "CRAFTING");
        skillTransfers.put("FLETCHER",     "CRAFTING");
        skillTransfers.put("MASON",        "CRAFTING");
        skillTransfers.put("CARPENTER",    "CRAFTING");
        skillTransfers.put("CARTOGRAPHER", "CRAFTING");

        skillTransfers.put("BEEKEEPER",  "RANCHING");
        skillTransfers.put("POULTRYMAN", "RANCHING");
        skillTransfers.put("COWHAND",    "RANCHING");
        skillTransfers.put("SWINEHERD",  "RANCHING");
        skillTransfers.put("SHEPHERD",   "RANCHING");
        skillTransfers.put("STABLEHAND", "RANCHING");

        skillTransfers.put("BAKER",    "TRADING");
        skillTransfers.put("COOK",     "TRADING");
        skillTransfers.put("ARCANIST", "TRADING");
        skillTransfers.put("TANNER",   "TRADING");
        skillTransfers.put("MERCHANT", "TRADING");

        return skillTransfers;
    }
	
	public static boolean removeEmeraldsFromPlayer(PlayerEntity player, int amount)
	{
	    int remaining = amount;
	    for (int i = 0; i < player.getInventory().size(); i++)
	    {
	        ItemStack stack = player.getInventory().getStack(i);
	        if (stack.getItem() == Items.EMERALD)
	        {
	            if (stack.getCount() > remaining)
	            {
	                stack.decrement(remaining);
	                return true;
	            }
	            else
	            {
	                remaining -= stack.getCount();
	                stack.setCount(0);
	            }
	        }
	        if (remaining <= 0)
	            return true;
	    }
	    return false;
	}
	
	public static void refundEmeraldsToPlayer(PlayerEntity player, int amount)
	{
		while (amount > 0)
		{
			int stackSize = Math.min(amount, Items.EMERALD.getMaxCount());
			ItemStack emeraldStack = new ItemStack(Items.EMERALD, stackSize);
			if (!player.getInventory().insertStack(emeraldStack))
				player.dropItem(emeraldStack, false);
			amount -= stackSize;
		}
	}

	public static void architect(ArchitectEntity architect, String name, String faction, String profession, String expertise, int hunger, int morale, int skill, UUID uuid, String gender, World world)
	{
		LocalDifficulty difficulty = world.getLocalDifficulty(architect.getBlockPos());
	    SpawnReason spawnReason = SpawnReason.NATURAL;
	    EntityData entityData = null;
	    NbtCompound entityNbt = new NbtCompound();
	    
	    Random rand = new Random();

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
	    ArchitectEntity.saveData(architect, world);
	    world.spawnEntity(architect);
	}
}