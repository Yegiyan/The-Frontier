package com.frontier.items;

import java.util.Optional;

import com.frontier.Frontier;
import com.frontier.renderers.ForcefieldRenderer;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class BlueprintItem extends Item
{
	private static int minSizeX = 0, minSizeY = 0, minSizeZ = 0;
	private static BlockPos placementPos = BlockPos.ORIGIN;

	public BlueprintItem(Settings settings)
	{
		super(settings);
	}

	public static void register()
	{
		WorldRenderEvents.LAST.register(context ->
		{
			MatrixStack matrixStack = context.matrixStack();
			MinecraftClient client = MinecraftClient.getInstance();
			Camera camera = context.camera();

			if (client.player != null)
				ForcefieldRenderer.drawForcefieldAtPosition(client, matrixStack, placementPos, minSizeX, minSizeY, minSizeZ, camera);
		});
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context)
	{
		World world = context.getWorld();
		BlockPos blockPos = context.getBlockPos();
		PlayerEntity player = context.getPlayer();
		placementPos = blockPos;

		if (!world.isClient && player != null)
		{
			Vec3i structureSize = getBlueprintNbt(world, this.getName().getString(), blockPos);

			minSizeX = structureSize.getX();
			minSizeY = structureSize.getY();
			minSizeZ = structureSize.getZ();

			Direction facing = player.getHorizontalFacing();
			adjustPositionForFacing(facing, blockPos);
			System.out.println("Blueprint used at: " + blockPos + ", Facing: " + facing + ", New Position: " + placementPos);
		}

		return ActionResult.SUCCESS;
	}

	private void adjustPositionForFacing(Direction facing, BlockPos blockPos)
	{
		switch (facing)
		{
			case EAST:
				adjustPosition(blockPos, 1, -minSizeZ / 2 - 1);
				break;
			case WEST:
				adjustPosition(blockPos, -minSizeX + 3, -minSizeZ / 2 - 1);
				break;
			case NORTH:
				placementPos = new BlockPos(placementPos.getX() - minSizeX / 2, placementPos.getY(), placementPos.getZ() - minSizeZ);
				break;
			case SOUTH:
				placementPos = new BlockPos(placementPos.getX() - minSizeX / 2, placementPos.getY(), placementPos.getZ() + 1);
				break;
			default:
				break;
		}
	}

	private void adjustPosition(BlockPos blockPos, int xAdjustment, int zAdjustment)
	{
		placementPos = rotatePosition90Degrees(placementPos, blockPos);
		swapSizes();
		placementPos = new BlockPos(placementPos.getX() + xAdjustment, placementPos.getY(), placementPos.getZ() + zAdjustment);
	}

	private static BlockPos rotatePosition90Degrees(BlockPos pos, BlockPos pivot)
	{
		int x = pos.getX() - pivot.getX();
		int z = pos.getZ() - pivot.getZ();
		int newX = -z;
		int newZ = x;
		return new BlockPos(newX + pivot.getX(), pos.getY(), newZ + pivot.getZ());
	}
	
	private void swapSizes()
	{
		int temp = minSizeX;
		minSizeX = minSizeZ;
		minSizeZ = temp;
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
}