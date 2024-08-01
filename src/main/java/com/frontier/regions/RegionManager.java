package com.frontier.regions;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.frontier.Frontier;
import com.frontier.renderers.RegionMapRenderer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.MapColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class RegionManager 
{
	public static CopyOnWriteArrayList<Region> regions = new CopyOnWriteArrayList<>();
	public static CopyOnWriteArrayList<Zone> zones = new CopyOnWriteArrayList<>();
	
	private static final int ZONE_RADIUS = 2048;
    private static final int ZONE_WIDTH = 128;
    private static final int WILD_CHANCE = 20; // % out of 100
    
    private static Random seed;
	
	public static void registerCallback() 
	{
        ServerTickEvents.END_SERVER_TICK.register(server -> 
        {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
            {
            	BlockPos playerPos = player.getBlockPos();
                if (getPlayerRegion(playerPos).equals("Uncharted Region"))
                {
                    createZonesAroundPlayer(playerPos);
                    assignRegions(player.getWorld());
                }
                retrieveMapBounds(player);
            }
        });
    }

	private static void createZonesAroundPlayer(BlockPos playerPos)
	{
	    int gridX = ((int)playerPos.getX() / ZONE_WIDTH) * ZONE_WIDTH;
	    int gridZ = ((int)playerPos.getZ() / ZONE_WIDTH) * ZONE_WIDTH;

	    // iterate over a grid centered around the player's grid position
	    for (int dx = -ZONE_RADIUS; dx <= ZONE_RADIUS; dx += ZONE_WIDTH)
	    {
	        for (int dz = -ZONE_RADIUS; dz <= ZONE_RADIUS; dz += ZONE_WIDTH)
	        {
	            int x1 = gridX + dx;
	            int z1 = gridZ + dz;
	            int x2 = x1 + ZONE_WIDTH;
	            int z2 = z1 + ZONE_WIDTH;

	            boolean zoneExists = false;
	            for (Zone existingZone : zones)
	            {
	                if (existingZone.overlaps(x1, z1, x2, z2))
	                {
	                    zoneExists = true;
	                    break;
	                }
	            }

	            if (!zoneExists)
	            {
	                Zone newZone = new Zone(x1, z1, x2, z2, "Zone" + seed.nextInt(), null, false, (byte) 0);
	                zones.add(newZone);
	            }
	        }
	    }
	}

	public static void assignRegions(World world)
	{
	    List<Zone> unchartedZones = zones.stream().filter(zone -> !zone.isCharted()).collect(Collectors.toList());

	    for (Zone unchartedZone : unchartedZones)
	    {
	        Region closestRegion = null;
	        double minDistanceToRegion = Double.MAX_VALUE;

	        // find closest existing region to the uncharted zone
	        for (Region region : regions)
	        {
	        	region.assignZoneDirection();
	            double distance = calculateDistanceToRegion(unchartedZone, region);
	            if (distance < minDistanceToRegion)
	            {
	                minDistanceToRegion = distance;
	                closestRegion = region;
	            }
	        }

	        double thresholdDistance = 800; // determines the number & size of regions (higher number = fewer but bigger regions)
	        if (closestRegion != null && minDistanceToRegion <= thresholdDistance)
	        {
	            unchartedZone.setRegion(closestRegion.getName());
	            unchartedZone.setColor(closestRegion.getColor());
	            closestRegion.addZone(unchartedZone);
	            unchartedZone.setCharted(true);
	        }
	        
	        else
	        {
	            // create new region for the uncharted zone if no close region is found
	            MapColor mapColor = Region.colors[seed.nextInt(Region.colors.length)];
	            int colorId = mapColor.getRenderColorByte(MapColor.Brightness.NORMAL);
	            boolean isWild = seed.nextInt(100) < WILD_CHANCE;

	            float avgTemp = calculateAverageTemperature(Collections.singletonList(unchartedZone), world);
	            String regionName = RegionNames.generateName(avgTemp);
	            Region newRegion = new Region(regionName, isWild, (byte) colorId);
	            regions.add(newRegion);

	            unchartedZone.setRegion(newRegion.getName());
	            unchartedZone.setColor(newRegion.getColor());
	            newRegion.addZone(unchartedZone);
	            unchartedZone.setCharted(true);
	        }
	    }
	}

	private static double calculateDistanceToRegion(Zone zone, Region region) 
	{
	    double zoneCenterX = (zone.getX1() + zone.getX2()) / 2.0;
	    double zoneCenterZ = (zone.getZ1() + zone.getZ2()) / 2.0;

	    Vec3d regionCenter = region.getCenter();

	    double deltaX = zoneCenterX - regionCenter.x;
	    double deltaZ = zoneCenterZ - regionCenter.z;
	    return Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
	}
    
    public static void saveRegionData(World world)
    {
        File saveDir = world.getServer().getSavePath(WorldSavePath.ROOT).toFile();
        File nbtFile = new File(saveDir, "thefrontier/RegionData.nbt");

        NbtCompound regionsCompound = new NbtCompound();

        for (Region region : regions)
        {
            NbtCompound regionNbt = new NbtCompound();
            regionNbt.putByte("Color", region.getColor());
            regionNbt.putBoolean("IsWild", region.isWild());

            NbtList zonesList = new NbtList();
            for (Zone zone : region.getZones())
            {
                NbtCompound zoneNbt = new NbtCompound();
                zoneNbt.putString("Name", zone.getName());
                zoneNbt.putString("Region", zone.getRegion());
                zoneNbt.putBoolean("IsCharted", zone.isCharted());
                zoneNbt.putByte("Color", zone.getColor());
                zoneNbt.putString("DirVert", zone.getVDir().toString());
                zoneNbt.putString("DirHori", zone.getHDir().toString());
                zoneNbt.putInt("X1", zone.getX1());
                zoneNbt.putInt("Z1", zone.getZ1());
                zoneNbt.putInt("X2", zone.getX2());
                zoneNbt.putInt("Z2", zone.getZ2());

                zonesList.add(zoneNbt);
            }
            regionNbt.put("Zones", zonesList);
            regionsCompound.put(region.getName(), regionNbt);
        }

        NbtCompound nbt = new NbtCompound();
        nbt.put("Regions", regionsCompound);

        try
        {
        	NbtIo.writeCompressed(nbt, nbtFile);
        	Frontier.LOGGER.info("Saving regions");
        } 
        catch (IOException e) { e.printStackTrace(); }
    }

    public static void loadRegionData(World world)
    {
        File saveDir = world.getServer().getSavePath(WorldSavePath.ROOT).toFile();
        File nbtFile = new File(saveDir, "thefrontier/RegionData.nbt");
        if (!nbtFile.exists()) return;

        try
        {
        	NbtCompound nbt = NbtIo.readCompressed(nbtFile);
        	NbtCompound regionsCompound = nbt.getCompound("Regions");

            regions.clear();
            zones.clear();

            for (String regionName : regionsCompound.getKeys())
            {
            	NbtCompound regionNbt = regionsCompound.getCompound(regionName);
                byte color = regionNbt.getByte("Color");
                boolean isWild = regionNbt.getBoolean("IsWild");

                Region region = new Region(regionName, isWild, color);
                regions.add(region);

                NbtList zonesList = regionNbt.getList("Zones", 10);
                for (NbtElement zoneElement : zonesList)
                {
                    NbtCompound zoneNbt = (NbtCompound) zoneElement;
                    String zoneName = zoneNbt.getString("Name");
                    String zoneRegion = zoneNbt.getString("Region");
                    boolean isZoneCharted = zoneNbt.getBoolean("IsCharted");
                    byte zoneColor = zoneNbt.getByte("Color");
                    Zone.vDir vertDir = Zone.vDir.valueOf(zoneNbt.getString("DirVert"));
                    Zone.hDir horiDir = Zone.hDir.valueOf(zoneNbt.getString("DirHori"));
                    int x1 = zoneNbt.getInt("X1");
                    int z1 = zoneNbt.getInt("Z1");
                    int x2 = zoneNbt.getInt("X2");
                    int z2 = zoneNbt.getInt("Z2");

                    Zone zone = new Zone(x1, z1, x2, z2, zoneName, zoneRegion, isZoneCharted, zoneColor);
                    zone.setVDir(vertDir);
                    zone.setHDir(horiDir);
                    
                    zones.add(zone);
                    region.addZone(zone);
                }
            }
            Frontier.LOGGER.info("Loading regions");
        } 
        catch (IOException e) { e.printStackTrace(); }
    }
    
    private static void retrieveMapBounds(ServerPlayerEntity player)
	{
        ItemStack heldItem = player.getMainHandStack();
        
        if (heldItem.getItem() == Items.FILLED_MAP) 
        {
            MapState mapState = FilledMapItem.getMapState(heldItem, player.getWorld());
            
            if (mapState == null)
            	return;
            	
            
            int centerX = mapState.centerX;
            int centerZ = mapState.centerZ;
            int mapSize = 128 * (1 << mapState.scale);
            
            int minX = centerX - mapSize / 2;
            int minZ = centerZ - mapSize / 2;
            int maxX = centerX + mapSize / 2;
            int maxZ = centerZ + mapSize / 2;

            RegionMapRenderer.minX = minX;
            RegionMapRenderer.minZ = minZ;
            RegionMapRenderer.maxX = maxX;
            RegionMapRenderer.maxZ = maxZ;
        }
    }
    
    private static float calculateAverageTemperature(List<Zone> cluster, World world)
    {
        float totalTemperature = 0;

        for (Zone zone : cluster)
        {
            Biome biome = getBiomeAtZoneCenter(zone, world);
            if (biome != null)
            {
                float biomeTemperature = biome.getTemperature();
                totalTemperature += biomeTemperature;
            }
        }

        if (cluster.size() == 0) return 0;
        return totalTemperature / cluster.size();
    }

    private static Biome getBiomeAtZoneCenter(Zone zone, World world)
    {
        int centerX = (zone.getX1() + zone.getX2()) / 2;
        int centerZ = (zone.getZ1() + zone.getZ2()) / 2;
        int centerY = 64;

        BlockPos centerPos = new BlockPos(centerX, centerY, centerZ);
        RegistryEntry<Biome> biomeRegistryEntry = world.getBiome(centerPos);

        return biomeRegistryEntry.value();
    }
    
    public static String getPlayerDirection(BlockPos playerPosition)
    {
        for (Zone zone : zones) 
            if (zone.isWithinBounds(playerPosition))
                return zone.getDirection();
        return " ";
    }
    
    public static String getRegionWild(BlockPos playerPosition)
    {
    	for (Zone zone : zones)
        {
            if (zone.isWithinBounds(playerPosition))
            {
                Region region = findRegionForZone(zone);
                if (region.isWild())
                    return "Wild";
            }
        }
    	return "";
    }
    
    public static String getPlayerRegion(BlockPos playerPosition)
    {
        for (Zone zone : zones)
        {
            if (zone.isWithinBounds(playerPosition))
            {
                Region region = findRegionForZone(zone);
                if (region != null)
                    return region.getName();
            }
        }
        return "Uncharted Region";
    }

    private static Region findRegionForZone(Zone zone)
    {
        for (Region region : regions)
            if (region.getZones().contains(zone))
                return region;
        return null;
    }
    
    public static String getBiomeName(PlayerEntity player)
	{
		RegistryEntry<Biome> biomeEntry = player.getWorld().getBiome(player.getBlockPos());
		RegistryKey<Biome> biomeKey = biomeEntry.getKey().get();
        Identifier biomeId = biomeKey.getValue();
        String biomeName = "Unknown";
        if (biomeEntry.getKey().isPresent())
        {
        	biomeName = biomeId.toString();
	        int colonIndex = biomeName.indexOf(':');
	        if (colonIndex != -1)
	            biomeName = biomeName.substring(colonIndex + 1);
	        biomeName = formatBiomeName(biomeName);
        }
        return biomeName;
	}
	
	private static String formatBiomeName(String name)
	{
	    String[] words = name.split("_");
	    StringBuilder formatted = new StringBuilder();
	    for (String word : words)
	    {
	        if (!word.isEmpty())
	        {
	            formatted.append(Character.toUpperCase(word.charAt(0)));
	            formatted.append(word.substring(1));
	            formatted.append(" ");
	        }
	    }
	    return formatted.toString().trim();
	}
    
    public static void reset()
    {
        regions.clear();
        zones.clear();
    }
    
    public static void setWorldSeed(long worldSeed) {
        seed = new Random(worldSeed);
    }
}