package com.frontier.calendar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.frontier.Frontier;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;

public class FrontierCalendarManager
{
	private static FrontierCalendar calendar;
	private static long lastDayTime = 0;
	private static long seed;

	public static void initialize()
	{
		ServerLifecycleEvents.SERVER_STARTED.register(server ->
		{
			seed = server.getOverworld().getSeed();
			loadData(server);
			if (calendar == null)
				calendar = new FrontierCalendar(seed);
		});

		ServerLifecycleEvents.SERVER_STOPPED.register(server ->
		{
			saveData(server);
		});

		ServerWorldEvents.LOAD.register((server, world) ->
		{
			if (world.getRegistryKey() == World.OVERWORLD)
				lastDayTime = world.getTimeOfDay() % 24000;
		});

		ServerTickEvents.START_WORLD_TICK.register(world ->
        {
            if (world.getRegistryKey() != World.OVERWORLD)
                return;

            long dayTime = world.getTimeOfDay() % 24000;

            // check if we've moved to a new day
            if (dayTime < lastDayTime)
            {
                calendar.nextDay();
                Frontier.LOGGER.info("Date: " + calendar.getCurrentDateWithZero());
            }

            lastDayTime = dayTime;
        });
	}

	public static void saveData(MinecraftServer server)
	{
		File saveDir = server.getSavePath(WorldSavePath.ROOT).toFile();
		File file = new File(saveDir, "thefrontier/CalendarData.dat");

		try
		{
			if (!file.exists())
			{
				file.getParentFile().mkdirs();
				file.createNewFile();
			}

			String data = calendar.getDay() + "," + calendar.getMonth() + "," + calendar.getYear();
			Files.write(file.toPath(), data.getBytes());
			Frontier.LOGGER.info("Saving calendar data");
		}
		catch (IOException e) { e.printStackTrace(); }
	}

	public static void loadData(MinecraftServer server)
	{
		File saveDir = server.getSavePath(WorldSavePath.ROOT).toFile();
		File file = new File(saveDir, "thefrontier/CalendarData.dat");

		if (!file.exists())
			return;

		try
		{
			String data = new String(Files.readAllBytes(file.toPath()));
			String[] parts = data.split(",");
			int day = Integer.parseInt(parts[0]);
			int month = Integer.parseInt(parts[1]);
			int year = Integer.parseInt(parts[2]);

			calendar = new FrontierCalendar(seed);
			calendar.setDate(day, month, year);
			Frontier.LOGGER.info("Loading calendar data");
		}
		catch (IOException e) { e.printStackTrace(); }
	}

	public static String getDate() {
		return calendar.getCurrentDate();
	}
	
	public static String getDateWithZero() {
		return calendar.getCurrentDateWithZero();
	}
	
	public static String getDateAsSentence() {
		return calendar.getCurrentDateAsSentence();
	}
	
	public static FrontierCalendar getCalendar() {
		return calendar;
	}

	public static long getWorldSeed() {
		return seed;
	}
}