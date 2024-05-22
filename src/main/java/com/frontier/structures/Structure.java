package com.frontier.structures;

import net.minecraft.util.math.BlockPos;

public abstract class Structure
{
    protected String name;
    protected String faction;
    protected BlockPos position;
    protected boolean isConstructed;
    protected int tier;
    
    protected StructureType type;
    public enum StructureType
    {
        CORE,
        GATHERER,
        TRADER,
        CRAFTER,
        OUTSIDER,
        RANCHER,
        MILITIA,
        MISC
    }
    
    protected abstract void onConstruction();
    protected abstract void onUpgrade();
    
    public Structure(String name, String faction, StructureType type, BlockPos position)
    {
        this.name = name;
        this.faction = faction;
        this.type = type;
        this.position = position;
        this.isConstructed = false;
        this.tier = 0;
    }

    public void upgrade()
    {
        if (isConstructed)
        {
            tier++;
            onUpgrade();
        }
    }

    public void startConstruction()
    {
        this.isConstructed = true;
        onConstruction();
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getFaction() {
        return faction;
    }

    public void setFaction(String playerFaction) {
        this.faction = playerFaction;
    }

    public StructureType getType() {
        return type;
    }
    
    public void setType(StructureType type) {
        this.type = type;
    }

    public BlockPos getPosition() {
		return position;
	}
    
	public void setPosition(BlockPos position) {
		this.position = position;
	}
	
	public boolean isConstructed() {
        return isConstructed;
    }

    public int getTier() {
        return tier;
    }
}