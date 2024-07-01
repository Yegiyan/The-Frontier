package com.frontier.entities;

import java.util.Random;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

public class HireSettler
{	
	public static void architect(ArchitectEntity architect, String name, String faction, String profession, String expertise, String gender, World world)
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
	    
	    // TODO: Set skill level depending on expertise and chosen profession

	    // choose random texture depending on gender
	    int textureIdx;
	    if (gender.equals("MALE")) 
	    	textureIdx = rand.nextInt(ArchitectEntity.ARCHITECT_TEXTURES_MALE.length);
	    else 
	    	textureIdx = rand.nextInt(ArchitectEntity.ARCHITECT_TEXTURES_FEMALE.length);
	    architect.setSettlerTexture(textureIdx);
	    
	    architect.initialize((ServerWorldAccess) world, difficulty, spawnReason, entityData, entityNbt);
	    ArchitectEntity.saveData(architect, world);
	    world.spawnEntity(architect);
	}
}