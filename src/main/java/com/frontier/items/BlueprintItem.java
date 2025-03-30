package com.frontier.items;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.lwjgl.glfw.GLFW;

import com.frontier.Frontier;
import com.frontier.PlayerData;
import com.frontier.blueprint.BlueprintState;
import com.frontier.blueprint.BlueprintStateManager;
import com.frontier.gui.BlueprintScreen;
import com.frontier.renderers.ForcefieldRenderer;
import com.frontier.settlements.Settlement;
import com.frontier.settlements.SettlementManager;
import com.frontier.structures.Structure;
import com.frontier.structures.StructureType;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class BlueprintItem extends Item
{
	private static Map<String, String> blueprintNameMap = new HashMap<>();

	public BlueprintItem(Settings settings) // AND REGISTER DOWN BELOW FOR TIER 0 STRUCTURE SIZES IN getBlueprintNbt() !!!
	{
		super(settings);
		blueprintNameMap.put("Blueprint: Town Hall", "TOWNHALL");
		blueprintNameMap.put("Blueprint: Warehouse", "WAREHOUSE");
		blueprintNameMap.put("Blueprint: House", "HOUSE");
	}

	public static void register()
	{
		WorldRenderEvents.LAST.register(context ->
		{
			MatrixStack matrixStack = context.matrixStack();
			MinecraftClient client = MinecraftClient.getInstance();
			long windowHandle = client.getWindow().getHandle();
			PlayerEntity player = client.player;
			PlayerData playerData = PlayerData.players.get(player.getUuid());

			if (player != null && playerData != null && playerData.isLeader())
			{
				BlueprintState blueprintState = BlueprintStateManager.getOrCreateBlueprintState(player);
				ItemStack itemStackInHand = player.getMainHandStack();

				if (itemStackInHand != null && itemStackInHand.getItem() instanceof BlueprintItem)
				{
					String blueprintName = itemStackInHand.getName().getString();
					blueprintState.setName(blueprintNameMap.getOrDefault(blueprintName, "NULL"));
				}

				if (GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS && blueprintState.isPlacing() && !blueprintState.isInspecting())
					blueprintState.setInspecting(true);

				if (matrixStack != null && blueprintState.isPlacing())
				{
					if (!(client.currentScreen instanceof BlueprintScreen) && blueprintState.isPlacing() && !blueprintState.isInspecting())
						client.setScreen(new BlueprintScreen(blueprintState));

					if (blueprintState.isPlacing())
					{
						BlockPos placementPos = blueprintState.getPlacementPos();
						int minSizeX = blueprintState.getMinSizeX();
						int minSizeY = blueprintState.getMinSizeY();
						int minSizeZ = blueprintState.getMinSizeZ();
						
						ForcefieldRenderer.drawForcefieldAtPosition(client, matrixStack, placementPos, minSizeX, minSizeY, minSizeZ, context.camera());
					}
				}
			}
		});
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand)
	{
	    BlueprintState blueprintState = BlueprintStateManager.getOrCreateBlueprintState(player);
	    ItemStack itemStackInHand = player.getStackInHand(hand);
	    PlayerData playerData = PlayerData.players.get(player.getUuid());

	    if (!world.isClient)
	    {
	        HitResult hitResult = player.raycast(5.0D, 0.0F, false);

	        if (!playerData.isLeader())
	            return new TypedActionResult<>(ActionResult.FAIL, itemStackInHand);
	        
	        String blueprintName = itemStackInHand.getName().getString();
	        boolean isTownHall = "Blueprint: Town Hall".equals(blueprintName);
	        
	        // check if player's settlement has active Town Hall (if the blueprint is not a Town Hall)
	        if (!isTownHall && !hasTownHall(playerData.getFaction())) {
	            if (player instanceof ServerPlayerEntity serverPlayer) {
	                Frontier.sendMessage(serverPlayer, "A Town Hall must be constructed before using other blueprints!", Formatting.RED);
	            }
	            return new TypedActionResult<>(ActionResult.FAIL, itemStackInHand);
	        }
	        
	        // check if blueprint is the same
	        if (blueprintState.isPlacing() && blueprintState.getActiveBlueprint() != itemStackInHand.getItem())
	        {
	            Frontier.sendMessage((ServerPlayerEntity) player, "Confirm or cancel previous blueprint placement first before trying to place another one!", Formatting.RED);
	            return new TypedActionResult<>(ActionResult.FAIL, itemStackInHand);
	        }

	        if (hitResult.getType() == HitResult.Type.MISS)
	        {
	            blueprintState.setInspecting(false);
	            return new TypedActionResult<>(ActionResult.SUCCESS, itemStackInHand);
	        }
	    }

	    return new TypedActionResult<>(ActionResult.PASS, itemStackInHand);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context)
	{
		PlayerEntity player = context.getPlayer();
		if (player != null)
		{
			PlayerData playerData = PlayerData.players.get(player.getUuid());
			if (!playerData.isLeader())
			{
				return ActionResult.FAIL;
			}

			BlueprintState blueprintState = BlueprintStateManager.getOrCreateBlueprintState(player);
			ItemStack itemStackInHand = player.getMainHandStack();

			if (itemStackInHand.getItem() instanceof BlueprintItem)
			{
				Item blueprintItem = itemStackInHand.getItem();
				String blueprintName = itemStackInHand.getName().getString();
				boolean isTownHall = "Blueprint: Town Hall".equals(blueprintName);
				
				if (!isTownHall && !hasTownHall(playerData.getFaction()))
				{
					if (!context.getWorld().isClient && player instanceof ServerPlayerEntity serverPlayer)
						Frontier.sendMessage(serverPlayer, "A Town Hall must be constructed before using other blueprints!", Formatting.RED);
					return ActionResult.FAIL;
				}

				// check if the player is trying to place a different blueprint before confirming the previous one
				if (blueprintState.isPlacing() && blueprintState.getActiveBlueprint() != blueprintItem)
				{
					if (!context.getWorld().isClient)
						if (player instanceof ServerPlayerEntity serverPlayer)
							Frontier.sendMessage(serverPlayer, "Confirm or cancel previous blueprint placement first before trying to place another one!",
									Formatting.RED);
					return ActionResult.FAIL;
				}

				// set active blueprint when placing
				blueprintState.setActiveBlueprint(blueprintItem);

				BlockPos blockPos = context.getBlockPos();
				blueprintState.setPlacementPos(blockPos);

				Direction facing = player.getHorizontalFacing();
				blueprintState.setFacing(facing);

				if (!context.getWorld().isClient)
				{
					Vec3i structureSize = getBlueprintNbt(context.getWorld(), this.getName().getString(), blockPos);
					System.out.println("Structure size: " + structureSize);
					blueprintState.setMinSizeX(structureSize.getX());
					blueprintState.setMinSizeY(structureSize.getY());
					blueprintState.setMinSizeZ(structureSize.getZ());
					adjustPositionForFacing(facing, blockPos, blueprintState);
				}

				blueprintState.setInspecting(false);
				blueprintState.setPlacing(true);
				return ActionResult.SUCCESS;
			}
		}

		return ActionResult.FAIL;
	}

	private void adjustPositionForFacing(Direction facing, BlockPos blockPos, BlueprintState blueprintState)
	{
		int minSizeX = blueprintState.getMinSizeX();
		int minSizeZ = blueprintState.getMinSizeZ();
		int placementPosX = blueprintState.getPlacementPos().getX();
		int placementPosY = blueprintState.getPlacementPos().getY();
		int placementPosZ = blueprintState.getPlacementPos().getZ();

		switch (facing)
		{
			case EAST:
				adjustPosition(blockPos, blueprintState, 1, -blueprintState.getMinSizeZ() / 2 - 1);
				break;
			case WEST:
				if (blueprintState.getMinSizeZ() % 2 == 0)
					adjustPosition(blockPos, blueprintState, -minSizeX + 3, -minSizeZ / 2 - 1);
				else
					adjustPosition(blockPos, blueprintState, -minSizeX + 2, -minSizeZ / 2 - 1);
				break;
			case NORTH:
				blueprintState.setPlacementPos(new BlockPos(placementPosX - minSizeX / 2, placementPosY, placementPosZ - minSizeZ));
				break;
			case SOUTH:
				blueprintState.setPlacementPos(new BlockPos(placementPosX - minSizeX / 2, placementPosY, placementPosZ + 1));
				break;
			default:
				break;
		}
	}

	private void adjustPosition(BlockPos blockPos, BlueprintState blueprintState, int xAdjustment, int zAdjustment)
	{
		int placementPosX = blueprintState.getPlacementPos().getX();
		int placementPosY = blueprintState.getPlacementPos().getY();
		int placementPosZ = blueprintState.getPlacementPos().getZ();

		blueprintState.setPlacementPos(rotatePosition90Degrees(blueprintState.getPlacementPos(), blockPos));
		swapSizes(blueprintState);
		blueprintState.setPlacementPos(new BlockPos(placementPosX + xAdjustment, placementPosY, placementPosZ + zAdjustment));
	}

	private static BlockPos rotatePosition90Degrees(BlockPos pos, BlockPos pivot)
	{
		int x = pos.getX() - pivot.getX();
		int z = pos.getZ() - pivot.getZ();
		int newX = -z;
		int newZ = x;
		return new BlockPos(newX + pivot.getX(), pos.getY(), newZ + pivot.getZ());
	}

	private void swapSizes(BlueprintState blueprintState)
	{
		int minSizeX = blueprintState.getMinSizeX();
		int minSizeZ = blueprintState.getMinSizeZ();

		int temp = minSizeX;
		blueprintState.setMinSizeX(minSizeZ);
		blueprintState.setMinSizeZ(temp);
	}

	private Vec3i getBlueprintNbt(World world, String itemName, BlockPos blockPos)
	{
		String nbtPath = null;

		switch (itemName)
		{
			case "Blueprint: Town Hall":
				nbtPath = "frontier:settlement/townhall_0";
				break;
			case "Blueprint: Warehouse":
				nbtPath = "frontier:settlement/warehouse_0";
				break;
			case "Blueprint: House":
				nbtPath = "frontier:settlement/house_0";
				break;
			default:
				Frontier.LOGGER.error("No structure size found for blueprint!");
				break;
		}

		return (nbtPath != null) ? getStructureSize(world, nbtPath) : BlockPos.ORIGIN;
	}

	private static Vec3i getStructureSize(World world, String nbtPath)
	{
		Identifier structureId = new Identifier(nbtPath);
		Optional<StructureTemplate> structureTemplateOptional = ((ServerWorld) world).getStructureTemplateManager().getTemplate(structureId);

		return structureTemplateOptional.map(StructureTemplate::getSize).orElseGet(() ->
		{
			System.out.println("Failed to load structure: " + nbtPath);
			return BlockPos.ORIGIN;
		});
	}
	
	public boolean hasTownHall(String settlementName)
	{
		if (!SettlementManager.settlementExists(settlementName))
			return false;

		Settlement settlement = SettlementManager.getSettlement(settlementName);
		for (Structure structure : settlement.getStructures())
			if (structure.getType() == StructureType.TOWNHALL && structure.isActive())
				return true;
		
		return false;
	}
}