package de.blablubbabc.paintball.statistics.player;

import java.util.Map;

import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.utils.Utils;

public class PlayerStats {
	private final String playerName;
	private Map<PlayerStat, Integer> stats = null;
	private boolean dirty = false;
	
	public PlayerStats(String playerName) {
		this.playerName = playerName;
		loadAsync();
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
		setStat(PlayerStat.ACCURACY, Utils.calculateQuote(getStat(PlayerStat.HITS), getStat(PlayerStat.SHOTS)));
		setStat(PlayerStat.KD, Utils.calculateQuote(getStat(PlayerStat.KILLS), getStat(PlayerStat.DEATHS)));
		dirty = true;
	}
	
	
	
	public void save() {
		if (dirty) {
			Paintball.instance.pm.setStats(playerName, stats);
			dirty = false;
		}
	}
	
	public void saveAsync() {
		Paintball.instance.getServer().getScheduler().runTaskAsynchronously(Paintball.instance, new Runnable() {
			
			@Override
			public void run() {
				save();
			}
		});
	}

	public void load() {
		stats = Paintball.instance.pm.getStats(playerName);
		calculate();
		dirty = false;
	}
	
	public void loadAsync() {
		Paintball.instance.getServer().getScheduler().runTaskAsynchronously(Paintball.instance, new Runnable() {
			
			@Override
			public void run() {
				load();
			}
		});
	}
	
}
