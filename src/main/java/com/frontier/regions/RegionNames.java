package com.frontier.regions;

import java.util.Random;

public class RegionNames
{
	private static final String[] COLD_PREFIXES = {"Frost", "Snow", "Ice", "Winter", "Glacial", "Arctic", "Chill", "Frozen"};
    private static final String[] COOL_PREFIXES = {"Mist", "Rain", "Pine", "Mountain", "Cloud", "Storm", "Fog", "Twilight"};
    private static final String[] TEMPERATE_PREFIXES = {"Green", "Bloom", "Meadow", "Forest", "River", "Sunlit", "Breezy", "Peaceful"};
    private static final String[] HOT_PREFIXES = {"Desert", "Sunny", "Dune", "Savanna", "Scorch", "Blaze", "Mirage", "Barren"};

    private static final String[] MIDDLE_PARTS = {"land", "wood", "vale", "field", "haven", "dale", "ridge", "grove"};

    private static final String[] COLD_SUFFIXES = {"Tundra", "Peaks", "Spikes", "Expanse", "Wastes", "Cliffs", "Depths", "Frontier"};
    private static final String[] COOL_SUFFIXES = {"Taiga", "Ridge", "Edge", "Grove", "Thicket", "Hollow", "Glade", "Wilds"};
    private static final String[] TEMPERATE_SUFFIXES = {"Plains", "Hills", "Garden", "Glade", "Valley", "Path", "Wood", "Brook"};
    private static final String[] HOT_SUFFIXES = {"Sands", "Wastes", "Oasis", "Plateau", "Expanse", "Basin", "Dunes", "Terrace"};

    public static String generateName(float temperature)
    {
        Random random = new Random();
        String prefix, middle, suffix;

        if (temperature <= 0.2) 
        {
            prefix = COLD_PREFIXES[random.nextInt(COLD_PREFIXES.length)];
            suffix = COLD_SUFFIXES[random.nextInt(COLD_SUFFIXES.length)];
        } 
        
        else if (temperature <= 0.5) 
        {
            prefix = COOL_PREFIXES[random.nextInt(COOL_PREFIXES.length)];
            suffix = COOL_SUFFIXES[random.nextInt(COOL_SUFFIXES.length)];
        } 
        
        else if (temperature <= 0.8) 
        {
            prefix = TEMPERATE_PREFIXES[random.nextInt(TEMPERATE_PREFIXES.length)];
            suffix = TEMPERATE_SUFFIXES[random.nextInt(TEMPERATE_SUFFIXES.length)];
        } 
        
        else 
        {
            prefix = HOT_PREFIXES[random.nextInt(HOT_PREFIXES.length)];
            suffix = HOT_SUFFIXES[random.nextInt(HOT_SUFFIXES.length)];
        }

        middle = MIDDLE_PARTS[random.nextInt(MIDDLE_PARTS.length)];
        
        return prefix + middle + " " + suffix;
    }
}