package com.frontier.entities.settler;

import java.util.List;

import com.frontier.Frontier;
import com.frontier.goals.architect.ArchitectState;
import com.frontier.goals.architect.GoToTownHallGoal;
import com.frontier.goals.architect.IdleInTownHallGoal;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
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
	
	private ArchitectState currentState = ArchitectState.GOING_TO_TOWNHALL;
	
	public ArchitectEntity(EntityType<? extends PathAwareEntity> entityType, World world)
	{
	    super(entityType, world);
	    if (!world.isClient)
		{
	    	getInventory().setStack(0, new ItemStack(Items.WOODEN_SHOVEL, 1));
		}
	}
	
	@Override
	protected void initGoals()
	{
		super.initGoals();
	    this.goalSelector.add(1, new GoToTownHallGoal(this, 0.5D));
	    this.goalSelector.add(2, new IdleInTownHallGoal(this, 0.4D));
	}
	
	@Override
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, NbtCompound entityNbt)
	{
		entityData = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
	    return entityData;
	}
	
	@Override
	public void mobTick() 
	{
	    super.mobTick();
	    if (!this.getWorld().isClient)
            pickUpNearbyItems();
	}
	
	@Override
	public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand)
	{
	    return super.interactAt(player, hitPos, hand);
	}
	
	private void pickUpNearbyItems()
	{
        List<ItemEntity> items = this.getWorld().getEntitiesByClass(ItemEntity.class, this.getBoundingBox().expand(2.0D, 1.0D, 2.0D), itemEntity -> !itemEntity.cannotPickup());
        for (ItemEntity itemEntity : items)
        {
            ItemStack itemStack = itemEntity.getStack();
            ItemStack remainder = this.getInventory().addStack(itemStack);

            if (remainder.isEmpty())
                itemEntity.discard();
            else
                itemEntity.setStack(remainder);
            
            if (!getWorld().isClient)
            	syncInventory();
            
            markDirty();
        }
    }
	
	@Override
	public void readCustomDataFromNbt(NbtCompound nbt)
	{
	    super.readCustomDataFromNbt(nbt);

	    if (nbt.contains("ArchitectState"))
	    {
	        try
	        {
	            String state = nbt.getString("ArchitectState");
	            this.currentState = ArchitectState.valueOf(state);
	        }
	        catch (IllegalArgumentException e)
	        {
	            this.currentState = ArchitectState.GOING_TO_TOWNHALL;
	        }
	    }
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt)
	{
	    super.writeCustomDataToNbt(nbt);
	    nbt.putString("ArchitectState", this.currentState.name());
	}
	
	@Override
    public Identifier[] getTextures(boolean isMale) {
        return isMale ? ARCHITECT_TEXTURES_MALE : ARCHITECT_TEXTURES_FEMALE;
    }
	
	public ArchitectState getState() {
	    return this.currentState;
	}

	public void setState(ArchitectState state) {
	    this.currentState = state;
	    Frontier.LOGGER.info("Architect state changed to: " + state);
	}
}