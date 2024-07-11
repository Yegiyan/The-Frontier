package com.frontier.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.frontier.Frontier;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

public class SpawnSettler
{
	public static void nomad(NomadEntity nomad, String faction, String profession, World world)
	{
		LocalDifficulty difficulty = world.getLocalDifficulty(nomad.getBlockPos());
	    SpawnReason spawnReason = SpawnReason.NATURAL;
	    EntityData entityData = null;
	    NbtCompound entityNbt = new NbtCompound();
	    
	    Random rand = new Random();

	    // choose random gender
	    String gender = nomad.genders.get(rand.nextInt(nomad.genders.size()));
	    nomad.setSettlerGender(gender);

	    // choose random name
	    String name = "";
	    List<String> availableNames = gender.equals("Male") ? new ArrayList<>(nomad.maleSettlerNames) : new ArrayList<>(nomad.femaleSettlerNames);

	    Set<String> occupiedNames = NomadEntity.loadEntityData(world);
	    availableNames.removeAll(occupiedNames);

	    // check if there are any available names left
	    if (!availableNames.isEmpty())
	        name = availableNames.get(rand.nextInt(availableNames.size()));
	    
	    else
	    {
	    	Frontier.LOGGER.error("We've run out of names to use!");
	        name = "Nomad " + (occupiedNames.size() + 1);
	    }

	    nomad.setSettlerName(name);
	    occupiedNames.add(name);

	    nomad.setSettlerProfession(profession);
	    nomad.setSettlerFaction(faction);
	    nomad.setSettlerExpertise(SettlerEntity.generateExpertise().name());
	    nomad.setSettlerHunger(100);
	    nomad.setSettlerMorale(SettlerEntity.generateValue(40, 60));
	    
	    if (nomad.getSettlerExpertise().equals("NOVICE"))
	    	nomad.setSettlerSkill(0);
	    else
	    	nomad.setSettlerSkill(SettlerEntity.generateValue(1, 10));
	    
	    // choose random texture depending on gender
	    int textureIdx;
	    if (gender.equals("MALE")) 
	    	textureIdx = rand.nextInt(NomadEntity.NOMAD_TEXTURES_MALE.length);
	    else 
	    	textureIdx = rand.nextInt(NomadEntity.NOMAD_TEXTURES_FEMALE.length);
	    nomad.setSettlerTexture(textureIdx);
	    
	    nomad.initialize((ServerWorldAccess) world, difficulty, spawnReason, entityData, entityNbt);
	    NomadEntity.saveEntityData(nomad, world);
	    world.spawnEntity(nomad);
	}
}