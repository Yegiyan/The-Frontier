package com.frontier.entities.util;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SettlerName
{
	public static List<String> maleFirstNames = Arrays.asList("Alden", "Balin", "Cedric", "Darian", "Eamon", "Fabian", "Gawain", "Hadrian",
			"Ivor", "Jareth", "Kael", "Lancelot", "Merrick", "Nolan", "Osric", "Percival", "Quinlan", "Roland", "Soren", "Tristan", "Urien",
			"Valerian", "Wystan", "Xavier", "Ywain", "Zephyr", "Alain", "Bromley", "Corwin", "Drake", "Emrys", "Fenris", "Garret", "Helmer",
			"Isolde", "Jasper", "Keiran", "Leif", "Morcant", "Neville", "Orion", "Pellinore", "Quillan", "Rafael", "Sawyer", "Thorne",
			"Ulric", "Vance", "Wayland", "Xander", "Yorick", "Zephyrus", "Alaric", "Bryant", "Crispin", "Duncan", "Eldon", "Fergus",
			"Gideon", "Hugo", "Ivan", "Jerome", "Killian", "Luther", "Malcolm", "Nash", "Osgar", "Pascal", "Quinton", "Reynard", "Sylvan",
			"Tybalt", "Uther", "Vaughan", "Wolfram", "Xan", "York", "Zane", "Ambrose", "Brand", "Caspar", "Didrik", "Erik", "Fletcher",
			"Griffin", "Heath", "Ignatius", "Julian", "Kendrick", "Leopold", "Miles", "Nigel", "Osborn", "Phineas", "Quincy", "Rupert",
			"Sebastian", "Thornton", "Udolf", "Vernon", "Wilfred", "Xerxes", "Yale", "Zachariah", "Ansel", "Benedict", "Clarence",
			"Dietrich", "Edgar", "Florian", "Godfrey", "Harold", "Isidore", "Justus", "Konrad", "Linus", "Milo", "Nathaniel", "Otto",
			"Philip", "Quentin", "Hayk", "Reginald", "Stefan", "Tobias", "Ulrich", "Victor", "Walter", "Zenosi", "Yves", "Zebulon",
			"Arnold", "Rafayel", "Bernard", "Clement", "Dominic", "Egbert", "Ferdinand", "Gregory", "Horace", "Isaac", "Justin", "Armen",
			"Klaus", "Ludwig", "Magnus", "Norbert", "Oliver", "Pius", "Quirinus", "Rudolf", "Siegfried", "Theodore", "Udo", "Vincent",
			"Werner", "Xaver", "Yannick", "Zigmund", "Alfred", "Bruno", "Conrad", "Desmond", "Ernest", "Frederick", "Gerard", "Hendrik",
			"Ingmar", "Joachim", "Kurt", "Leonard", "Maximilian", "Norman", "Oscar", "Peter", "Quintus", "Rainer", "Sven", "Thomas", "Uwe",
			"Vitus", "Wilhelm", "Hovo", "Yorick", "Narek");

	public static List<String> femaleFirstNames = Arrays.asList("Aria", "Brielle", "Cassandra", "Dahlia", "Elara", "Fiona", "Gwendolyn",
			"Hazel", "Isolde", "Jasmine", "Kiera", "Luna", "Maeve", "Nyla", "Ophelia", "Orin", "Penelope", "Quinn", "Rhiannon", "Seraphina",
			"Thea", "Ursula", "Vivienne", "Willow", "Xanthe", "Yara", "Zara", "Aveline", "Bronwyn", "Cordelia", "Demelza", "Evelyn",
			"Freya", "Genevieve", "Helena", "Imogen", "Juliet", "Kathryn", "Lillian", "Mirabel", "Nadine", "Odette", "Pandora", "Rosamund",
			"Sabina", "Tabitha", "Undine", "Verity", "Winifred", "Xena", "Yvaine", "Zipporah", "Allegra", "Beatrice", "Cecily", "Daphne",
			"Edith", "Florence", "Griselda", "Henrietta", "Iris", "Josephine", "Keziah", "Lucinda", "Meredith", "Nerissa", "Octavia",
			"Prudence", "Rowena", "Sibyl", "Temperance", "Unity", "Venetia", "Winona", "Xanadu", "Yolanda", "Zenobia", "Adelaide", "Briony",
			"Clarissa", "Dorothea", "Evangeline", "Francesca", "Ginevra", "Honoria", "Isadora", "Jemima", "Kallista", "Lydia", "Marguerite",
			"Nicolette", "Olivette", "Philomena", "Rosalind", "Sidonia", "Thomasina", "Ursuline", "Valentina", "Wilhelmina", "Xantippe",
			"Yesenia", "Zinnia", "Aurelia", "Blythe", "Constance", "Delilah", "Esther", "Felicity", "Guinevere", "Hermione", "Iolanthe",
			"Jessamine", "Kerensa", "Lavender", "Melisande", "Narcissa", "Opaline", "Persephone", "Romilda", "Susannah", "Tallulah",
			"Umbrielle", "Valetta", "Wisteria", "Xanthia", "Yolande", "Zephyrine", "Anastasia", "Bernadette", "Clementine", "Desdemona",
			"Eugenia", "Fenella", "Gwendolen", "Hortensia", "Isabeau", "Jacqueline", "Kitty", "Lorelei", "Minerva", "Nerida", "Oriana",
			"Pandora", "Rosabel", "Seraphine", "Tatiana", "Urania", "Violetta", "Wynonna", "Xenobia", "Yasmine", "Zuleika", "Amabel",
			"Beatrix", "Celestine", "Dove", "Emmeline", "Fleur", "Georgiana", "Hildegard", "Isolde", "Jocasta", "Kismet", "Leticia",
			"Morgana", "Nephele", "Oriole", "Perdita", "Rafaela", "Salome", "Tempest", "Lily", "Veronica", "Winola", "Xandria", "Ysabel",
			"Zelda");

	private static final String[] MEDIEVAL_PREFIXES = { "Bar", "Wolf", "Drake", "Duke", "Griff", "Hawk", "Lan", "Red", "Stone", "Raven",
			"Ash", "Thor", "Iron", "Frost", "Storm", "Silver", "Night", "Bright", "Green", "Gold", "Shadow", "Bran", "Hart", "Mar", "Win",
			"Oak", "Falcon", "Lion", "Fox", "Hale", "West", "Wild", "Gale", "Shade", "Wyn", "Vale" };

	private static final String[] MEDIEVAL_SUFFIXES = { "ton", "wood", "field", "fort", "ridge", "well", "son", "land", "hall", "stone",
			"helm", "ward", "brook", "grave", "dale", "wick", "worth", "cliffe", "more", "gard", "bridge", "burn", "burg", "shire", "croft",
			"borough", "ham", "hill", "ford", "view", "side", "holt", "water", "watch", "rock", "keep" };

	private static final String[] CELTIC_PREFIXES = { "Mac", "O'", "Fitz", "Kil", "Mal", "Con", "Bran", "Glen", "Finn", "Cael", "Eog",
			"Niall", "Riogh", "Tadhg", "Aodh", "Don", "Seán", "Colm", "Dermot", "Lugh", "Cath", "Murch", "Sorcha", "Siobh", "Ail", "Ruar",
			"Muir", "Eime", "Ciar", "Fion", "Tir", "Dara", "Cadh", "Gorm", "Fear", "Rón" };

	private static final String[] CELTIC_SUFFIXES = { "der", "dan", "an", "lough", "bane", "ly", "more", "lan", "han", "ley", "gh", "ty",
			"nan", "ron", "gus", "lach", "leen", "mara", "can", "y", "ard", "ghan", "uin", "len", "tair", "wyn", "nall", "vor", "cal",
			"ter", "mir", "dal", "dor", "eth", "bel", "rad" };

	private static final String[] SLAVIC_PREFIXES = { "Vol", "Mil", "Bor", "Slav", "Drag", "Vlad", "Nik", "Dmitri", "Stan", "Rad", "Yuri",
			"Miros", "Kaz", "Ivan", "Pet", "Jar", "Boh", "Vas", "Gor", "Toma", "Dan", "Geor", "Leon", "Grig", "Serg", "Fedor", "Rus",
			"Tikh", "Andr", "Nik", "Artem", "Anat", "Egor", "Fil", "Yev", "Aleks" };

	private static final String[] SLAVIC_SUFFIXES = { "ov", "ski", "ich", "ev", "in", "ovitch", "ovska", "ovskaya", "ovich", "ovsky",
			"ovich", "enski", "ovich", "ovich", "enko", "kin", "sky", "rov", "ovitch", "ik", "iy", "tsov", "ak", "shyn", "ych", "nov",
			"lev", "jev", "evich", "skiy", "chev", "ich", "off", "yov", "achev", "ovich" };

	private static final String[] GERMANIC_PREFIXES = { "Sch", "Berg", "Stein", "Grimm", "Fried", "Roth", "Klein", "Weiss", "Schwarz",
			"Braun", "Müller", "Schmidt", "Schneider", "Fischer", "Weber", "Zimmer", "Koch", "Wagner", "Lor", "Wolf", "Hart", "Hoff",
			"Schul", "Bauer", "Meier", "Lang", "Beck", "Kraus", "Schultz", "Jung", "Maur", "Lind", "Vogt", "Reis", "Bock", "Vogel" };

	private static final String[] GERMANIC_SUFFIXES = { "mann", "berg", "stein", "wald", "hardt", "rich", "sen", "ner", "ler", "heim",
			"brandt", "hoff", "bauer", "meyer", "schultz", "hofer", "holz", "feld", "meister", "mann", "waldt", "strom", "gärtner",
			"schwarz", "lieber", "hauser", "hofer", "schmid", "reich", "brecht", "hauser", "kaiser", "rad", "freud", "singer", "bruck" };

	private static final Random random = new Random();

	public static String generateFirstName(String gender)
	{
		Random random = new Random();
		if ("Male".equalsIgnoreCase(gender))
		{
			int index = random.nextInt(maleFirstNames.size());
			return maleFirstNames.get(index);
		}
		else if ("Female".equalsIgnoreCase(gender))
		{
			int index = random.nextInt(femaleFirstNames.size());
			return femaleFirstNames.get(index);
		}
		else
			throw new IllegalArgumentException("NameGenerator() - Invalid gender: " + gender + "!");
	}

	public static String generateLastName()
	{
		String[] prefixes;
		String[] suffixes;

		int culture = random.nextInt(4);
		switch (culture)
		{
			case 0:
				prefixes = MEDIEVAL_PREFIXES;
				suffixes = MEDIEVAL_SUFFIXES;
				break;
			case 1:
				prefixes = CELTIC_PREFIXES;
				suffixes = CELTIC_SUFFIXES;
				break;
			case 2:
				prefixes = SLAVIC_PREFIXES;
				suffixes = SLAVIC_SUFFIXES;
				break;
			case 3:
				prefixes = GERMANIC_PREFIXES;
				suffixes = GERMANIC_SUFFIXES;
				break;
			default:
				prefixes = MEDIEVAL_PREFIXES;
				suffixes = MEDIEVAL_SUFFIXES;
		}

		String prefix = prefixes[random.nextInt(prefixes.length)];
		String suffix = suffixes[random.nextInt(suffixes.length)];
		return prefix + suffix;
	}
}