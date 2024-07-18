package com.frontier.calendar;

import java.util.Random;

public class FrontierCalendar
{
	private static final String[] MONTHS = { "Dawnstar", "Parvus", "Morrow", "Aether", "Verdure", "Solstice", "Solara", "Calidum", "Harvest", "Lunaris", "Novara", "Duskfall" };
	private static final int[] NORMAL_DAYS_IN_MONTH = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
	private static final int[] LEAP_DAYS_IN_MONTH = { 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
	private int day, month, year;

	public FrontierCalendar(long seed) // in data dawnstar = '0' & duskfall = '11'
	{
		this.day = generateDate(1, 20, seed);
		this.month = generateDate(0, 11, seed);
		this.year = generateDate(1, 1000, seed);
	}

	public void nextDay()
	{
		day++;
		if (day > getDaysInMonth(month, year))
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

		if (day < 1 || day > getDaysInMonth(month, year))
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
	
	private static boolean isLeapYear(int year)
	{
		if (year % 4 != 0)
			return false;
		if (year % 100 == 0 && year % 400 != 0)
			return false;
		return true;
	}

	private int getDaysInMonth(int month, int year)
	{
		if (isLeapYear(year))
			return LEAP_DAYS_IN_MONTH[month];
		else
			return NORMAL_DAYS_IN_MONTH[month];
	}
	
	public String getRandomDate(int year)
	{
		Random rand = new Random();
		int month = rand.nextInt(MONTHS.length);
		int day = rand.nextInt(getDaysInMonth(month, year)) + 1;
		return String.format("%02d %s %d", day, MONTHS[month], year);
	}
	
	public String getCurrentDate() {
		return day + " " + MONTHS[month] + " " + year;
	}
	
	public String getCurrentDateWithZero() {
        return String.format("%02d %s %d", day, MONTHS[month], year);
    }
	
	public String getCurrentDateAsSentence() {
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