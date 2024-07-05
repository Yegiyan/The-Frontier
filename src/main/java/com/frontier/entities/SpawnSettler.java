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

	    Set<String> occupiedNames = NomadEntity.loadData(world);
	    availableNames.removeAll(occupiedNames);

	    // check if there are any available names left
	    if (!availableNames.isEmpty())
	        name = availableNames.get(rand.nextInt(availableNames.size()));
	    
	    else
	    {
	    	Frontier.LOGGER.info("We've run out of names to use!");
	        name = "Nomad " + (occupiedNames.size() + 1);
	    }

	    nomad.setSettlerName(name);
	    occupiedNames.add(name);

	    // set faction, profession, & expertise
	    nomad.setSettlerProfession(profession);
	    nomad.setSettlerFaction(faction);
	    nomad.setSettlerExpertise(SettlerEntity.chooseRandomExpertise().name());
	    
	    // set morale & skill
	    nomad.setSettlerMorale(SettlerEntity.chooseRandomValue(40, 60, false));
	    nomad.setSettlerSkill(SettlerEntity.chooseRandomValue(1, 20, true));

	    // choose random texture depending on gender
	    int textureIdx;
	    if (gender.equals("MALE")) 
	    	textureIdx = rand.nextInt(NomadEntity.NOMAD_TEXTURES_MALE.length);
	    else 
	    	textureIdx = rand.nextInt(NomadEntity.NOMAD_TEXTURES_FEMALE.length);
	    nomad.setSettlerTexture(textureIdx);
	    
	    nomad.initialize((ServerWorldAccess) world, difficulty, spawnReason, entityData, entityNbt);
	    NomadEntity.saveData(nomad, world);
	    world.spawnEntity(nomad);
	}
}