package com.frontier.entities;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

public class ArchitectEntity extends SettlerEntity
{
	public static final Identifier[] ARCHITECT_TEXTURES_MALE = new Identifier[]
			{
	        new Identifier("frontier", "textures/entity/architect/architect_male_0.png"),
	        new Identifier("frontier", "textures/entity/architect/architect_male_1.png"),
	        new Identifier("frontier", "textures/entity/architect/architect_male_2.png"),
	        };
	
	public static final Identifier[] ARCHITECT_TEXTURES_FEMALE = new Identifier[] 
			{
	        new Identifier("frontier", "textures/entity/architect/architect_female_0.png"),
	        new Identifier("frontier", "textures/entity/architect/architect_female_1.png"),
	        new Identifier("frontier", "textures/entity/architect/architect_female_2.png"),
	        };
	
	public ArchitectEntity(EntityType<? extends PathAwareEntity> entityType, World world) 
	{
	    super(entityType, world);
	}
	
	@Override
	protected void initGoals() 
	{
		super.initGoals();
	}
	
	@Override
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, NbtCompound entityNbt) 
	{
		entityData = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
	    return entityData;
	}
	
	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) 
	{
	    super.readCustomDataFromNbt(nbt);
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) 
	{
	    super.writeCustomDataToNbt(nbt);
	}
}