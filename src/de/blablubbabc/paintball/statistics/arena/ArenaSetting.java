package de.blablubbabc.paintball.statistics.arena;

public enum ArenaSetting {
	LIVES("lives"),
	RESPAWNS("respawns"),
	ROUND_TIME("round_time"),
	BALLS("balls"),
	GRENADES("grenades"),
	AIRSTRIKES("airstrikes");
	
	private final String key;
	
	private ArenaSetting(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
	
	public static ArenaSetting getFromKey(String key) {
		for (ArenaSetting stat : values()) {
			if (stat.getKey().equals(key)) return stat;
		}
		return null;
	}
	
	private static String[] keys = null;
	
	public static String[] getKeys() {
		if (keys == null) {
			keys = new String[values().length];
			for (ArenaSetting stat : values()) {
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
			string += key + ",";
		}
		if(string.length() > 1) string = string.substring(0, (string.length() - 1));
		return string;
	}
}

