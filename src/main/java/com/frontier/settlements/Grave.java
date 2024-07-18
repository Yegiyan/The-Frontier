package com.frontier.settlements;

import java.util.Random;

public class Grave
{
	private static final String[] ADJECTIVES = { "Beloved", "Faithful", "Brave", "Noble", "Loyal", "Kind", "Gentle", "Fierce", "Wise", "Eternal" };
	private static final String[] NOUNS = { "Warrior", "Friend", "Guardian", "Soul", "Hero", "Spirit", "Heart", "Champion", "Adventurer", "Companion" };
	private static final Random RANDOM = new Random();
	
	private String name;
	private String experise;
	private String birth;
	private String death;
	private String eulogy;
	
	public Grave(String name, String expertise, String birth, String death)
	{
		this.name = name;
		this.experise = expertise;
		this.birth = birth;
		this.death = death;
		this.eulogy = generateEulogy();
	}
	
	private String generateEulogy() // TODO: personalize depending on expertise
	{
		String adjective = ADJECTIVES[RANDOM.nextInt(ADJECTIVES.length)];
		String noun = NOUNS[RANDOM.nextInt(NOUNS.length)];
		return adjective + " " + noun;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExperise() {
		return experise;
	}

	public void setExperise(String experise) {
		this.experise = experise;
	}

	public String getBirth() {
		return birth;
	}

	public void setBirth(String birth) {
		this.birth = birth;
	}

	public String getDeath() {
		return death;
	}

	public void setDeath(String death) {
		this.death = death;
	}

	public String getEulogy() {
		return eulogy;
	}

	public void setEulogy(String phrase) {
		this.eulogy = phrase;
	}
}