package de.blablubbabc.paintball.player;

import java.util.HashMap;
import java.util.Map;

public class PlayerStats {
	private Map<PlayerStat, Integer> stats = new HashMap<PlayerStat, Integer>();
	private boolean dirty = false;
	
	public PlayerStats() {
		load();
		calculate();
		dirty = false;
	}
	
	public void resetStats() {
		for(PlayerStat stat : PlayerStat.values()) {
			setStat(stat, 0);
		}
		calculate();
		// calculate already marks this stats as "dirty"
	}
	
	public void addStat(PlayerStat stat, int value) {
		setStat(stat, getStat(stat)+value);
		dirty = true;
	}
	
	public void setStat(PlayerStat stat, int value) {
		stats.put(stat, value);
		dirty = true;
	}
	
	public int getStat(PlayerStat stat) {
		return stats.get(stat);
	}
	
	public void calculate() {
		setStat(PlayerStat.ACCURACY, StatsUtils.calculateQuote(getStat(PlayerStat.HITS), getStat(PlayerStat.SHOTS)));
		setStat(PlayerStat.KD, StatsUtils.calculateQuote(getStat(PlayerStat.KILLS), getStat(PlayerStat.DEATHS)));
		dirty = true;
	}
	
	public void save() {
		if (dirty) {
			for(PlayerStat stat : PlayerStat.values()) {
				//sql save
			}
			dirty = false;
		}
	}

	public void load() {
		for(PlayerStat stat : PlayerStat.values()) {
			// sql load
			int value = 0;
			setStat(stat, value);
		}
	}
	
}
