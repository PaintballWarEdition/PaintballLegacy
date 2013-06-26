package de.blablubbabc.paintball.statistics.player;

import java.util.Map;
import java.util.Map.Entry;

import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.utils.Utils;

public class PlayerStats {
	private final String playerName;
	private Map<PlayerStat, Integer> stats = null;
	
	private boolean dirty = false;
	
	public PlayerStats(String playerName) {
		this.playerName = playerName;
		load();
	}
	
	public void resetStats() {
		for(PlayerStat stat : PlayerStat.values()) {
			setStat(stat, 0);
		}
		calculateQuotes();
		dirty = true;
	}
	
	public void addStat(PlayerStat stat, int value) {
		setStat(stat, getStat(stat) + value);
	}
	
	public void addStats(Map<PlayerStat, Integer> otherStats) {
		for (Entry<PlayerStat, Integer> entry : otherStats.entrySet()) {
			addStat(entry.getKey(), entry.getValue());
		}
	}
	
	public void setStat(PlayerStat stat, int value) {
		stats.put(stat, value);
		dirty = true;
	}
	
	public void setStats(Map<PlayerStat, Integer> otherStats) {
		for (Entry<PlayerStat, Integer> entry : otherStats.entrySet()) {
			setStat(entry.getKey(), entry.getValue());
		}
	}
	
	public int getStat(PlayerStat stat) {
		return stats.get(stat);
	}
	
	public Map<PlayerStat, Integer> getStats() {
		return stats;
	}
	
	public void calculateQuotes() {
		// set stats, without changing dirty state:
		stats.put(PlayerStat.ACCURACY, Utils.calculateQuote(getStat(PlayerStat.HITS), getStat(PlayerStat.SHOTS)));
		stats.put(PlayerStat.KD, Utils.calculateQuote(getStat(PlayerStat.KILLS), getStat(PlayerStat.DEATHS)));
	}
	
	public void save() {
		if (dirty) {
			Paintball.instance.sql.sqlPlayers.setPlayerStats(playerName, stats);
			
			//Paintball.instance.pm.setStats(playerName, stats);
			dirty = false;
		}
	}
	
	public void saveAsync() {
		if (dirty) {
			Paintball.instance.getServer().getScheduler().runTaskAsynchronously(Paintball.instance, new Runnable() {
				
				@Override
				public void run() {
					save();
				}
			});
		}
	}

	public void load() {
		stats = Paintball.instance.sql.sqlPlayers.getPlayerStats(playerName);
		//stats = Paintball.instance.pm.getStats(playerName);
		calculateQuotes();
	}
	
}
