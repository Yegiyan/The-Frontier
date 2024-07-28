package com.frontier.settlements;

import com.frontier.util.FrontierUtil;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public enum Blueprint
{
    NONE(0), TOWNHALL(2), WAREHOUSE(4), HOUSE(3),  ROAD(12), BRIDGE(10), WALL(6),
    BARRACKS(4), APOTHECARY(12), BOUNTYHALL(6), WATCHTOWER(10),
    FARM(4), FISHERY(5), LODGE(3), GROVE(4), MINE(8),
    ALCHEMYLAB(12), ARCANUM(10), BLACKSMITH(8), CARTOGRAPHY(10), FLETCHERY(6), TANNERY(8),
    APIARY(8), COWBARN(4), CHICKENCOOP(6), SHEEPPASTURE(8), STABLE(14), PIGPEN(4), 
    BAKERY(6), ABATTOIR(8), GREENGROCERY(4), WOODSHOP(8), MASONRY(10),
    MARKETPLACE(12), TAVERN(8),
    CHURCH(10), LIBRARY(12), CEMETERY(6), WELL(4), FOUNTAIN(6);

	private int value;

	Blueprint(int value)
	{
		this.value = value;
	}
	
	public void updateValue(int value)
	{
		this.value += value;
		this.value += FrontierUtil.clamp(this.value, 1, 100);
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public MutableText getText() {
		return Text.literal(String.valueOf(value));
	}
}