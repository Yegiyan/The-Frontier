package com.frontier.regions;

import net.minecraft.util.math.BlockPos;

public class Zone 
{
    private int x1, z1, x2, z2;
    private String name;
    private String region;
    private byte color;
    private boolean charted;
    
    enum vDir { NORTH, SOUTH, CENTER } 
    enum hDir { WEST, EAST, CENTER }
    
    private vDir vertDir;
    private hDir horiDir;

    public Zone(int x1, int z1, int x2, int z2, String name, String region, boolean charted, byte color) {
        this.x1 = x1;
        this.z1 = z1;
        this.x2 = x2;
        this.z2 = z2;
        this.name = name;
        this.region = region;
        this.charted = charted;
        this.color = color;
        this.vertDir = vDir.CENTER;
        this.horiDir = hDir.CENTER;
    }
    
    public boolean isWithinBounds(BlockPos position)
    {
        return position.getX() >= x1 && position.getX() <= x2 && position.getZ() >= z1 && position.getZ() <= z2;
    }

    public boolean overlaps(int x1, int z1, int x2, int z2)
	{
	    return !(this.x2 <= x1 || x2 <= this.x1 || this.z2 <= z1 || z2 <= this.z1);
	}
    
    public int getX1() {
        return x1;
    }

    public int getZ1() {
        return z1;
    }

    public int getX2() {
        return x2;
    }

    public int getZ2() {
        return z2;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
    	this.region = region;
    }

    public byte getColor() {
        return (byte) color;
    }
    
    public void setColor(byte color) {
    	this.color = (byte) color;
    }
    
    public boolean isCharted() {
        return charted;
    }
    
    public void setCharted(boolean charted) {
    	this.charted = charted;
    }
    
    public String getDirection()
    {
    	if (vertDir == null || horiDir == null)
            return "Unknown";
    	
    	if (vertDir.equals(vDir.CENTER) && horiDir.equals(hDir.CENTER))
    		return "CENTER";

    	else if (!vertDir.equals(vDir.CENTER) && horiDir.equals(hDir.CENTER))
    		return "" + vertDir;
    	
    	else if (vertDir.equals(vDir.CENTER) && !horiDir.equals(hDir.CENTER))
    		return "" + horiDir;
    	
    	else
    		return vertDir + "-" + horiDir;
    }
    
    public vDir getVDir() {
        return vertDir;
    }
    
    public void setVDir(vDir dir) {
        this.vertDir = dir;
    }
    
    public hDir getHDir() {
        return horiDir;
    }

    public void setHDir(hDir dir) {
        this.horiDir = dir;
    }

    @Override
    public String toString() 
    {
        return "{" + name + ": " +
               "(" + x1 +
               ", " + z1 + ")" +
               " > (" + x2 +
               ", " + z2 + ")" +
               ", dir: " + vertDir + " " + horiDir +
               "}";
    }
}