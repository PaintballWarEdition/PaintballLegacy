package de.blablubbabc.paintball.statistics.player.match.tdm;

import java.util.HashMap;
import java.util.Map;

import de.blablubbabc.paintball.statistics.player.PlayerStats;
import de.blablubbabc.paintball.utils.Utils;

public class TDMMatchStats {
	private final Map<TDMMatchStat, Integer> stats = new HashMap<TDMMatchStat, Integer>();
	private final PlayerStats playerStats;
	
	public TDMMatchStats(PlayerStats playerStats) {
		// PlayerStats to mirror changes to:
		this.playerStats = playerStats;
		// init with 0 each:
		resetStats();
	}
	
	public PlayerStats getPlayerStats() {
		return playerStats;
	}
	
	public void resetStats() {
		for(TDMMatchStat stat : TDMMatchStat.values()) {
			stats.put(stat, 0);
		}
		calculateQuotes();
	}
	
	public void addStat(TDMMatchStat stat, int value, boolean mirrorOnPlayerStats) {
		stats.put(stat, getStat(stat) + value);
		if (mirrorOnPlayerStats && playerStats != null && stat.getPlayerStat() != null) playerStats.addStat(stat.getPlayerStat(), value);
	}
	
	public int getStat(TDMMatchStat stat) {
		return stats.get(stat);
	}
	
	public Map<TDMMatchStat, Integer> getStats() {
		return stats;
	}
	
	public void calculateQuotes() {
		stats.put(TDMMatchStat.HITQUOTE, Utils.calculateQuote(getStat(TDMMatchStat.HITS), getStat(TDMMatchStat.SHOTS)));
		stats.put(TDMMatchStat.KD, Utils.calculateQuote(getStat(TDMMatchStat.KILLS), getStat(TDMMatchStat.DEATHS)));
		
		if (playerStats != null) playerStats.calculateQuotes();
	}
}
