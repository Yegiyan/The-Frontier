package com.frontier.calendar;

import java.util.Random;

public class FrontierCalendar
{
	private static final String[] MONTHS = { "Dawnstar", "Diminute", "Morrow", "Burgeon", "Harvest", "Lunaris", "Sunfire", "Aether", "Hearth", "Gloomfall", "Novara", "Duskrest" };
	private static final int[] DAYS_IN_MONTH = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
	private int day, month, year;

	public FrontierCalendar(long seed)
	{
		this.day = generateDate(1, 20, seed);
		this.month = generateDate(0, 11, seed);
		this.year = generateDate(1, 1000, seed);
	}

	public void nextDay()
	{
		day++;
		if (day > DAYS_IN_MONTH[month])
		{
			day = 1;
			month++;
			if (month >= MONTHS.length)
			{
				month = 0;
				year++;
			}
		}
	}

	public static int generateDate(int min, int max, long seed)
	{
		Random rand = new Random(seed);
		return rand.nextInt((max - min) + 1) + min;
	}

	public void setDate(int day, int month, int year)
	{
		if (month < 0 || month >= MONTHS.length)
			throw new IllegalArgumentException("Invalid month: " + month);

		if (day < 1 || day > DAYS_IN_MONTH[month])
			throw new IllegalArgumentException("Invalid day: " + day);

		this.day = day;
		this.month = month;
		this.year = year;
	}

	private static String getDayWithSuffix(int day)
	{
		if (day >= 11 && day <= 13)
			return day + "th";

		switch (day % 10)
		{
			case 1:
				return day + "st";
			case 2:
				return day + "nd";
			case 3:
				return day + "rd";
			default:
				return day + "th";
		}
	}

	public String getCurrentDate() {
		return getDayWithSuffix(day) + " of " + MONTHS[month] + ", Year " + year;
	}

	public int getDay() {
		return day;
	}
	
	public int getMonth() {
		return month;
	}

	public int getYear() {
		return year;
	}
}
