package de.blablubbabc.paintball.statistics.player.match.tdm;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
	
	public void resetStats() {
		for(TDMMatchStat stat : TDMMatchStat.values()) {
			setStat(stat, 0, false);
		}
		calculateQuotes();
	}
	
	public void addStat(TDMMatchStat stat, int value) {
		setStat(stat, getStat(stat) + value, true);
	}
	
	public void addStats(Map<TDMMatchStat, Integer> otherStats) {
		for (Entry<TDMMatchStat, Integer> entry : otherStats.entrySet()) {
			addStat(entry.getKey(), entry.getValue());
		}
	}
	
	public void setStat(TDMMatchStat stat, int value, boolean mirror) {
		stats.put(stat, value);
		if (mirror && playerStats != null && stat.getPlayerStat() != null) playerStats.setStat(stat.getPlayerStat(), value);
	}
	
	public int getStat(TDMMatchStat stat) {
		return stats.get(stat);
	}
	
	public Map<TDMMatchStat, Integer> getStats() {
		return stats;
	}
	
	public void calculateQuotes() {
		setStat(TDMMatchStat.ACCURACY, Utils.calculateQuote(getStat(TDMMatchStat.HITS), getStat(TDMMatchStat.SHOTS)), false);
		setStat(TDMMatchStat.KD, Utils.calculateQuote(getStat(TDMMatchStat.KILLS), getStat(TDMMatchStat.DEATHS)), false);
		if (playerStats != null) playerStats.calculateQuotes();
	}
}
