/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.statistics.general;

public enum GeneralStat {
	SHOTS("shots"),
	GRENADES("grenades"),
	AIRSTRIKES("airstrikes"),
	KILLS("kills"),
	ROUNDS("rounds"),
	MONEY_SPENT("money_spent"),
	AVERAGE_PLAYERS("average_players"),
	MAX_PLAYERS("max_players");
	
	private final String key;
	
	private GeneralStat(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
	
	public static GeneralStat getFromKey(String key) {
		for (GeneralStat stat : values()) {
			if (stat.getKey().equals(key)) return stat;
		}
		return null;
	}
	
	private static String[] keys = null;
	
	public static String[] getKeys() {
		if (keys == null) {
			keys = new String[values().length];
			for (GeneralStat stat : values()) {
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

