package de.blablubbabc.paintball.statistics.player;

public enum PlayerStat {
	POINTS("points"),
	MONEY("money"),
	MONEY_SPENT("money_spent"),
	KILLS("kills"),
	DEATHS("deaths"),
	KD("kd"),
	SHOTS("shots"),
	HITS("hits"),
	HITQUOTE("hitquote"),
	TEAMATTACKS("teamattacks"),
	ROUNDS("rounds"),
	WINS("wins"),
	DEFEATS("defeats"),
	DRAWS("draws"),
	GRENADES("grenades"),
	AIRSTRIKES("airstrikes");
	
	private final String key;
	
	private PlayerStat(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
	
	public static PlayerStat getFromKey(String key) {
		for (PlayerStat stat : values()) {
			if (stat.getKey().equals(key)) return stat;
		}
		return null;
	}
	
	private static String[] keys = null;
	
	public static String[] getKeys() {
		if (keys == null) {
			keys = new String[values().length];
			for (PlayerStat stat : values()) {
				keys[stat.ordinal()] = stat.getKey();
			}
		}
		return keys;
	}
	
	public static boolean containsKey(String key) {
		return getFromKey(key) != null;
	}
	
	public static String getKeysAsString() {
		String string = "";
		for(String key : getKeys()) {
			string += key + ", ";
		}
		if(string.length() > 1) string = string.substring(0, (string.length() - 2));
		return string;
	}
}
