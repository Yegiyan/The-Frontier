package com.frontier.blueprint;

import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

public class BlueprintState
{
	private BlockPos placementPos = BlockPos.ORIGIN;
	private int minSizeX = 0, minSizeY = 0, minSizeZ = 0;
	private boolean isPlacing = false, isInspecting = false;
	private Item activeBlueprint;
	private String name = "null";

	public void reset()
	{
	    this.setPlacementPos(BlockPos.ORIGIN);
	    this.setMinSizeX(0);
	    this.setMinSizeY(0);
	    this.setMinSizeZ(0);
	    this.setPlacing(false);
	    this.setInspecting(false);
	    this.activeBlueprint = null;
	    this.setName("null");
	}
	
	public BlockPos getPlacementPos() {
		return placementPos;
	}

	public void setPlacementPos(BlockPos placementPos) {
		this.placementPos = placementPos;
	}

	public boolean isPlacing() {
		return isPlacing;
	}

	public void setPlacing(boolean placing) {
		isPlacing = placing;
	}

	public boolean isInspecting() {
		return isInspecting;
	}

	public void setInspecting(boolean inspecting) {
		isInspecting = inspecting;
	}

	public int getMinSizeX() {
		return minSizeX;
	}

	public void setMinSizeX(int minSizeX) {
		this.minSizeX = minSizeX;
	}

	public int getMinSizeY() {
		return minSizeY;
	}

	public void setMinSizeY(int minSizeY) {
		this.minSizeY = minSizeY;
	}

	public int getMinSizeZ() {
		return minSizeZ;
	}

	public void setMinSizeZ(int minSizeZ) {
		this.minSizeZ = minSizeZ;
	}

	public Item getActiveBlueprint() {
        return activeBlueprint;
    }

    public void setActiveBlueprint(Item activeBlueprint) {
        this.activeBlueprint = activeBlueprint;
    }
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}