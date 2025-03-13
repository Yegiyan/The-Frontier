package com.frontier.structures;

import com.frontier.structures.Structure.StructureCategory;

public enum StructureType
{
	TOWNHALL(StructureCategory.CORE),
	WAREHOUSE(StructureCategory.CORE),
	HOUSE(StructureCategory.CORE),
	
	ROAD(StructureCategory.FOUNDATION),
	BRIDGE(StructureCategory.FOUNDATION),
	WALL(StructureCategory.FOUNDATION),

	BARRACKS(StructureCategory.MILITARY),
	BOUNTYHALL(StructureCategory.MILITARY),
	WATCHTOWER(StructureCategory.MILITARY),
	
	FARM(StructureCategory.PRODUCTION),
	FISHERY(StructureCategory.PRODUCTION),
	LODGE(StructureCategory.PRODUCTION),
	GROVE(StructureCategory.PRODUCTION),
	MINE(StructureCategory.PRODUCTION),
	
	ALCHEMYLAB(StructureCategory.CRAFTING),
	ARCANUM(StructureCategory.CRAFTING),
	BLACKSMITH(StructureCategory.CRAFTING),
	CARTOGRAPHY(StructureCategory.CRAFTING),
	FLETCHERY(StructureCategory.CRAFTING),
	TANNERY(StructureCategory.CRAFTING),
	
	APIARY(StructureCategory.RANCHING),
	COWBARN(StructureCategory.RANCHING),
	CHICKENCOOP(StructureCategory.RANCHING),
	SHEEPPASTURE(StructureCategory.RANCHING),
	STABLE(StructureCategory.RANCHING),
	PIGPEN(StructureCategory.RANCHING),
	
	BAKERY(StructureCategory.SERVICES),
	ABATTOIR(StructureCategory.SERVICES),
	GREENGROCERY(StructureCategory.SERVICES),
	WOODSHOP(StructureCategory.SERVICES),
	MASONRY(StructureCategory.SERVICES),
	APOTHECARY(StructureCategory.SERVICES),
	MARKETPLACE(StructureCategory.SERVICES),
	TAVERN(StructureCategory.SERVICES),
	
	CHURCH(StructureCategory.CULTURAL),
	LIBRARY(StructureCategory.CULTURAL),
	CEMETERY(StructureCategory.CULTURAL),
	
	WELL(StructureCategory.DECORATIVE),
	FOUNTAIN(StructureCategory.DECORATIVE),
	
	NULL(StructureCategory.NULL);
	
	private final StructureCategory category;
	
	StructureType(StructureCategory category) {
		this.category = category;
	}
	
	public StructureCategory getCategory() {
		return category;
	}
	
	public static StructureType fromString(String type)
	{
		try { return valueOf(type.toUpperCase()); }
		catch (IllegalArgumentException e) { return null; }
	}
}