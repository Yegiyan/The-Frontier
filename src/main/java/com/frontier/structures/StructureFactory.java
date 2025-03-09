package com.frontier.structures;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.frontier.Frontier;
import com.frontier.settlements.Settlement;
import com.frontier.settlements.SettlementManager;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class StructureFactory
{
	private static final Map<String, Class<? extends Structure>> STRUCTURE_TYPES = new HashMap<>();

	// register structure types
	static
	{
		registerStructureType("townhall", TownHall.class);
		registerStructureType("warehouse", Warehouse.class);
		// add more structure types here as they are implemented
	}

	public static void registerStructureType(String typeName, Class<? extends Structure> structureClass)
	{
		STRUCTURE_TYPES.put(typeName.toLowerCase(), structureClass);
	}

	// create a new structure instance by type name
	public static Structure createStructure(String typeName, String faction, BlockPos position, Direction facing)
	{
		try
		{
			Class<? extends Structure> structureClass = STRUCTURE_TYPES.get(typeName.toLowerCase());
			if (structureClass == null)
			{
				Frontier.LOGGER.error("Unknown structure type: " + typeName);
				return null;
			}

			// create a new instance using reflection
			return structureClass.getConstructor(String.class, String.class, BlockPos.class, Direction.class).newInstance(typeName, faction, position, facing);
		}
		catch (Exception e)
		{
			Frontier.LOGGER.error("Failed to create structure: " + typeName, e);
			return null;
		}
	}

	public static Structure loadStructureFromNbt(NbtCompound structureNbt, ServerWorld world)
	{
		try
		{
			String structureName = structureNbt.getString("Name");
			String faction = structureNbt.getString("Faction");
			String type = structureNbt.getString("Type");
			BlockPos position = BlockPos.fromLong(structureNbt.getLong("Position"));
			Direction facing = Direction.byName(structureNbt.getString("Facing"));
			int tier = structureNbt.getInt("Tier");
			UUID uuid = structureNbt.getUuid("UUID");

			Structure structure = createStructure(structureName, faction, position, facing);
			if (structure == null)
				return null;

			// set basic properties
			structure.setType(Structure.StructureType.valueOf(type));
			structure.setTier(tier);
			structure.setUUID(uuid);
			structure.setActive(structureNbt.getBoolean("IsActive"));
			structure.setConstructed(structureNbt.getBoolean("IsConstructed"));

			// set component properties
			structure.getRepairManager().setRepair(structureNbt.getBoolean("RequiresRepair"));
			structure.getConstructionManager().setConstructing(structureNbt.getBoolean("IsConstructing"));
			structure.getUpgradeManager().setUpgrading(structureNbt.getBoolean("IsUpgrading"));
			structure.getConstructionManager().setClearing(structureNbt.getBoolean("IsClearing"));
			structure.getConstructionManager().setConstructionTicksElapsed(structureNbt.getInt("ConstructionTicksElapsed"));
			structure.getUpgradeManager().setUpgradeTicksElapsed(structureNbt.getInt("UpgradeTicksElapsed"));
			structure.getRepairManager().setRepairTicksElapsed(structureNbt.getInt("RepairTicksElapsed"));

			// set queues and maps through managers
			structure.getConstructionManager().setAirBlocksQueue(SettlementManager.deserializeQueue(structureNbt.getList("AirBlocksQueue", 10)));
			structure.getConstructionManager().setNonAirBlocksQueue(SettlementManager.deserializeQueue(structureNbt.getList("NonAirBlocksQueue", 10)));
			structure.getConstructionManager().setClearingQueue(SettlementManager.deserializeQueue(structureNbt.getList("ClearingQueue", 10)));
			structure.getUpgradeManager().setUpgradeQueue(SettlementManager.deserializeQueue(structureNbt.getList("UpgradeQueue", 10)));
			structure.getRepairManager().setRepairQueue(SettlementManager.deserializeRepairQueue(structureNbt.getList("RepairQueue", 10)));

			structure.getConstructionManager().setConstructionMap(SettlementManager.deserializeMap(structureNbt.getList("ConstructionMap", 10)));
			structure.getUpgradeManager().setUpgradeMap(SettlementManager.deserializeMap(structureNbt.getList("UpgradeMap", 10)));

			// resume structure's operation if needed
			if (structure.isConstructing() || structure.isUpgrading() || structure.requiresRepair())
			{
				structure.resume(world);
			}

			return structure;
		}
		catch (Exception e)
		{
			Frontier.LOGGER.error("Failed to load structure from NBT", e);
			return null;
		}
	}

	public static void saveStructureToNbt(Structure structure, NbtCompound structureNbt)
	{
		structureNbt.putString("Name", structure.getName());
		structureNbt.putString("Faction", structure.getFaction());
		structureNbt.putString("Type", structure.getType().toString());
		structureNbt.putLong("Position", structure.getPosition().asLong());
		structureNbt.putString("Facing", structure.getFacing().getName());
		structureNbt.putInt("Tier", structure.getTier());
		structureNbt.putUuid("UUID", structure.getUUID());
		structureNbt.putBoolean("IsActive", structure.isActive());
		structureNbt.putBoolean("IsConstructed", structure.isConstructed());

		// access through manager components
		structureNbt.putBoolean("RequiresRepair", structure.getRepairManager().requiresRepair());
		structureNbt.putBoolean("IsConstructing", structure.getConstructionManager().isConstructing());
		structureNbt.putBoolean("IsUpgrading", structure.getUpgradeManager().isUpgrading());
		structureNbt.putBoolean("IsClearing", structure.getConstructionManager().isClearing());
		structureNbt.putInt("ConstructionTicksElapsed", structure.getConstructionManager().getConstructionTicksElapsed());
		structureNbt.putInt("UpgradeTicksElapsed", structure.getUpgradeManager().getUpgradeTicksElapsed());
		structureNbt.putInt("RepairTicksElapsed", structure.getRepairManager().getRepairTicksElapsed());

		// access queues and maps through managers
		structureNbt.put("AirBlocksQueue", SettlementManager.serializeQueue(structure.getConstructionManager().getAirBlocksQueue()));
		structureNbt.put("NonAirBlocksQueue", SettlementManager.serializeQueue(structure.getConstructionManager().getNonAirBlocksQueue()));
		structureNbt.put("ClearingQueue", SettlementManager.serializeQueue(structure.getConstructionManager().getClearingQueue()));
		structureNbt.put("UpgradeQueue", SettlementManager.serializeQueue(structure.getUpgradeManager().getUpgradeQueue()));
		structureNbt.put("RepairQueue", SettlementManager.serializeRepairQueue(structure.getRepairManager().getRepairQueue()));

		structureNbt.put("ConstructionMap", SettlementManager.serializeMap(structure.getConstructionManager().getConstructionMap()));
		structureNbt.put("UpgradeMap", SettlementManager.serializeMap(structure.getUpgradeManager().getUpgradeMap()));
	}

	// helper method to construct a structure at a specific location
	public static Structure constructStructureAtLocation(String typeName, String faction, BlockPos position, Direction facing, ServerWorld world)
	{
		Structure structure = createStructure(typeName, faction, position, facing);
		if (structure != null)
		{
			structure.constructStructure(world);

			Settlement settlement = SettlementManager.getSettlement(faction);
			if (settlement != null)
				settlement.getStructures().add(structure);
			else
				Frontier.LOGGER.error("Settlement not found for structure: " + typeName);
		}
		return structure;
	}

	// helper method to spawn a structure instantly at a specific location
	public static Structure spawnStructureAtLocation(String typeName, String faction, BlockPos position, Direction facing, ServerWorld world)
	{
		Structure structure = createStructure(typeName, faction, position, facing);
		if (structure != null)
		{
			structure.spawnStructure(world);

			Settlement settlement = SettlementManager.getSettlement(faction);
			if (settlement != null)
				settlement.getStructures().add(structure);
			else
				Frontier.LOGGER.error("Settlement not found for structure: " + typeName);
		}
		return structure;
	}
}