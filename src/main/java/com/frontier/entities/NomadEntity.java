package com.frontier.entities;

import com.frontier.PlayerData;
import com.frontier.goals.SelfDefenseGoal;
import com.frontier.goals.nomad.MoveTowardsBellGoal;
import com.frontier.goals.nomad.MoveTowardsDespawnGoal;
import com.frontier.gui.HireSettlerScreen;
import com.frontier.settlements.SettlementManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

public class NomadEntity extends SettlerEntity
{
	private static final TrackedData<BlockPos> BELL_POSITION = DataTracker.registerData(SettlerEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);
	private BlockPos bellPos;
	
	public static final Identifier[] NOMAD_TEXTURES_MALE = new Identifier[]
			{
	        new Identifier("frontier", "textures/entity/nomad/nomad_male_0.png"),
	        new Identifier("frontier", "textures/entity/nomad/nomad_male_1.png"),
	        new Identifier("frontier", "textures/entity/nomad/nomad_male_2.png"),
	        };
	
	public static final Identifier[] NOMAD_TEXTURES_FEMALE = new Identifier[] 
			{
	        new Identifier("frontier", "textures/entity/nomad/nomad_female_0.png"),
	        new Identifier("frontier", "textures/entity/nomad/nomad_female_1.png"),
	        new Identifier("frontier", "textures/entity/nomad/nomad_female_2.png"),
	        };
	
	public NomadEntity(EntityType<? extends PathAwareEntity> entityType, World world) 
	{
	    super(entityType, world);
	    this.dataTracker.startTracking(BELL_POSITION, BlockPos.ORIGIN);
	}
	
	@Override
	protected void initGoals() 
	{
		super.initGoals();
		this.goalSelector.add(0, new MoveTowardsBellGoal(this, .42D));
	    this.goalSelector.add(1, new WanderAroundFarGoal(this, .2D, .015F));
	    this.goalSelector.add(2, new MoveTowardsDespawnGoal(this, .42D));
	    this.goalSelector.add(3, new SelfDefenseGoal(this));
	}
	
	@Override
	public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) 
	{
		PlayerData playerData = PlayerData.map.get(player.getUuid());
		ItemStack itemStack = player.getStackInHand(hand);
		
		if (player.getWorld().isClient && playerData.getProfession().equals("Leader") && itemStack.getItem() == Items.CLOCK && SettlementManager.getSettlement(playerData.getFaction()).isWithinTerritory(player.getBlockPos()))
	    	MinecraftClient.getInstance().setScreen(new HireSettlerScreen(this));
		
	    return super.interactAt(player, hitPos, hand);
	}
	
	@Override
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, NbtCompound entityNbt)
	{
		entityData = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
		if (entityNbt.contains("BellPos"))
		    setBellPosition(NbtHelper.toBlockPos(entityNbt.getCompound("BellPos")));
	    return entityData;
	}
	
	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) 
	{
	    super.readCustomDataFromNbt(nbt);
	    if (nbt.contains("BellPos")) 
	    {
	        this.bellPos = NbtHelper.toBlockPos(nbt.getCompound("BellPos"));
	        this.dataTracker.set(BELL_POSITION, bellPos);
	    }
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) 
	{
	    super.writeCustomDataToNbt(nbt);
	    if (this.getBellPosition() != null)
	        nbt.put("BellPos", NbtHelper.fromBlockPos(this.getBellPosition()));
	}
	
	@Override
    public Identifier[] getTextures(boolean isMale) {
        return isMale ? NOMAD_TEXTURES_MALE : NOMAD_TEXTURES_FEMALE;
    }

	public BlockPos getBellPosition() {
	    return this.dataTracker.get(BELL_POSITION);
	}

	public void setBellPosition(BlockPos bellPos) {
	    this.dataTracker.set(BELL_POSITION, bellPos);
	}
}
