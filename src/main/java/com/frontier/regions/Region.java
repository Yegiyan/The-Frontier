package com.frontier.regions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.block.MapColor;
import net.minecraft.util.math.Vec3d;

public class Region 
{
    private List<Zone> zones;
    private String name;
    private byte color;
    private boolean isWild;
    
    public static MapColor[] colors = new MapColor[] 
    {
    		MapColor.PALE_GREEN,
    		MapColor.PALE_YELLOW,
    		MapColor.WHITE_GRAY,
    		MapColor.BRIGHT_RED,
    		MapColor.PALE_PURPLE,
   			MapColor.IRON_GRAY,
   			MapColor.DARK_GREEN,
   			MapColor.WHITE,
   	    	MapColor.LIGHT_BLUE_GRAY,
    	   	MapColor.DIRT_BROWN,
    	   	MapColor.STONE_GRAY,
    	   	MapColor.WATER_BLUE,
    	   	MapColor.OAK_TAN,
        	MapColor.OFF_WHITE,
   	    	MapColor.ORANGE,
   	    	MapColor.MAGENTA,
   	    	MapColor.LIGHT_BLUE,
    	    MapColor.YELLOW,
    	   	MapColor.LIME,
    	   	MapColor.PINK,
    	   	MapColor.GRAY,
    	   	MapColor.LIGHT_GRAY,
        	MapColor.CYAN,
   	    	MapColor.PURPLE,
   	    	MapColor.BLUE,
   	    	MapColor.BROWN,
    	    MapColor.GREEN,
    	   	MapColor.RED,
    	   	MapColor.BLACK,
    	   	MapColor.GOLD,
    	   	MapColor.DIAMOND_BLUE,
        	MapColor.LAPIS_BLUE,
   	    	MapColor.EMERALD_GREEN,
   	    	MapColor.SPRUCE_BROWN,
    	    MapColor.DARK_RED,
            MapColor.TERRACOTTA_WHITE,
       	    MapColor.TERRACOTTA_ORANGE,
       	    MapColor.TERRACOTTA_MAGENTA,
       	    MapColor.TERRACOTTA_LIGHT_BLUE,
       	    MapColor.TERRACOTTA_YELLOW,
       	    MapColor.TERRACOTTA_LIME,
       	    MapColor.TERRACOTTA_PINK,
            MapColor.TERRACOTTA_GRAY,
            MapColor.TERRACOTTA_LIGHT_GRAY,
        	MapColor.TERRACOTTA_CYAN,
            MapColor.TERRACOTTA_PURPLE,
            MapColor.TERRACOTTA_BLUE,
        	MapColor.TERRACOTTA_BROWN,
        	MapColor.TERRACOTTA_GREEN,
            MapColor.TERRACOTTA_RED,
        	MapColor.TERRACOTTA_BLACK,
        	MapColor.DULL_RED,
        	MapColor.DULL_PINK,
        	MapColor.DARK_CRIMSON,
        	MapColor.TEAL,
        	MapColor.DARK_AQUA,
        	MapColor.DARK_DULL_PINK,
        	MapColor.BRIGHT_TEAL,
        	MapColor.DEEPSLATE_GRAY,
            MapColor.RAW_IRON_PINK,
        	MapColor.LICHEN_GREEN
    };

    public Region(String name, boolean isWild, byte color) 
    {
        this.zones = new ArrayList<>();
        this.name = name;
        this.color = color;
        this.isWild = isWild;
    }

    public boolean addZone(Zone zone) 
    {
        zones.add(zone);
        return true;
    }

    public void assignZoneDirection() 
    {
        List<Double> xPositions = zones.stream().map(z -> (z.getX1() + z.getX2()) / 2.0).sorted().collect(Collectors.toList());
        List<Double> zPositions = zones.stream().map(z -> (z.getZ1() + z.getZ2()) / 2.0).sorted().collect(Collectors.toList());

        double centerArea = 0.9; // decrease this to make the center area smaller (1.0 is default)

        double xDiv1 = getDividingPosition(xPositions, 1.0 / 3.0, centerArea);
        double xDiv2 = getDividingPosition(xPositions, 2.0 / 3.0, centerArea);
        double zDiv1 = getDividingPosition(zPositions, 1.0 / 3.0, centerArea);
        double zDiv2 = getDividingPosition(zPositions, 2.0 / 3.0, centerArea);

        for (Zone zone : zones) 
        {
            double avgX = (zone.getX1() + zone.getX2()) / 2.0;
            double avgZ = (zone.getZ1() + zone.getZ2()) / 2.0;

            zone.setVDir(avgZ < zDiv1 ? Zone.vDir.NORTH :
                		 avgZ > zDiv2 ? Zone.vDir.SOUTH :
                			 Zone.vDir.CENTER);
            
            zone.setHDir(avgX < xDiv1 ? Zone.hDir.WEST :
                         avgX > xDiv2 ? Zone.hDir.EAST :
                        	 Zone.hDir.CENTER);
        }
    }
    
    private double getDividingPosition(List<Double> positions, double fraction, double sensitivity) 
    {
        double median = positions.get(positions.size() / 2);

        // determine index of the position a fraction of the way between the median and the edge
        int lowerIndex = (int) ((positions.size() / 2) * (1 - sensitivity));
        int upperIndex = (int) ((positions.size() / 2) + ((positions.size() / 2) * sensitivity));

        double lowerBound = positions.get(Math.max(lowerIndex, 0));
        double upperBound = positions.get(Math.min(upperIndex, positions.size() - 1));

        // return a position adjusted by the fraction and sensitivity
        return fraction < 0.5 ? lowerBound + (median - lowerBound) * (fraction / 0.5) : median + (upperBound - median) * ((fraction - 0.5) / 0.5);
    }
    
    public Vec3d getCenter()
    {
        if (zones.isEmpty())
            return new Vec3d(0, 0, 0);

        double totalX = 0;
        double totalZ = 0;

        for (Zone zone : zones)
        {
            double centerX = (zone.getX1() + zone.getX2()) / 2.0;
            double centerZ = (zone.getZ1() + zone.getZ2()) / 2.0;
            totalX += centerX;
            totalZ += centerZ;
        }

        double averageX = totalX / zones.size();
        double averageZ = totalZ / zones.size();

        return new Vec3d(averageX, 0, averageZ);
    }

    public List<Zone> getZones() {
        return zones;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
    	this.name = name;
    }

    public byte getColor() {
        return color;
    }
    
    public void setColor(byte color) {
    	this.color = (byte) color;
    }
    
    public boolean isWild() {
        return isWild;
    }
    
    public void setWild(boolean isWild) {
    	this.isWild = isWild;
    }
    
    @Override
    public String toString() 
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{'").append(name).append('\'');
        sb.append(", Zones = [");
        for (int i = 0; i < zones.size(); i++) 
        {
            sb.append(zones.get(i).toString());
            if (i < zones.size() - 1)
                sb.append(", ");
        }
        sb.append("]}");
        return sb.toString();
    }
}