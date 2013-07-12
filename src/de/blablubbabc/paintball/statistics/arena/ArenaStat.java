package de.blablubbabc.paintball.statistics.arena;

public enum ArenaStat {
	ROUNDS("rounds"),
	KILLS("kills"),
	SHOTS("shots"),
	GRENADES("grenades"),
	AIRSTRIKES("airstrikes");
	
	private final String key;
	
	private ArenaStat(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
	
	public static ArenaStat getFromKey(String key) {
		for (ArenaStat stat : values()) {
			if (stat.getKey().equals(key)) return stat;
		}
		return null;
	}
	
	private static String[] keys = null;
	
	public static String[] getKeys() {
		if (keys == null) {
			keys = new String[values().length];
			for (ArenaStat stat : values()) {
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

