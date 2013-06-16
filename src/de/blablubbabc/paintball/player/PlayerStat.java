package de.blablubbabc.paintball.player;

public enum PlayerStat {
	POINTS("points"),
	MONEY("money"),
	MONEY_SPENT("money_spent"),
	KILLS("kills"),
	DEATHS("deaths"),
	KD("kd"),
	SHOTS("shots"),
	HITS("hits"),
	ACCURACY("hitquote"),
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
}
