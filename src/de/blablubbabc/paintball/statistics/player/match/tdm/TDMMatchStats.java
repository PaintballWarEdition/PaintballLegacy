package de.blablubbabc.paintball.statistics.player.match.tdm;

import java.util.HashMap;
import java.util.Map;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.statistics.player.PlayerStats;
import de.blablubbabc.paintball.utils.Utils;

public class TDMMatchStats {
	private final Map<TDMMatchStat, Integer> stats = new HashMap<TDMMatchStat, Integer>();
	private final PlayerStats playerStats;
	private final Match match;
	private final String playerName;
	
	public TDMMatchStats(Match match, String playerName, PlayerStats playerStats) {
		// reference to match, for updating scoreboard:
		this.match = match;
		this.playerName = playerName;
		// PlayerStats to mirror changes to:
		this.playerStats = playerStats;
		// init with 0 each:
		resetStats();
	}
	
	public void resetStats() {
		for(TDMMatchStat stat : TDMMatchStat.values()) {
			stats.put(stat, 0);
		}
		calculateQuotes();
		match.updateMatchScoreboard(playerName);
	}
	
	public void addStat(TDMMatchStat stat, int value) {
		stats.put(stat, getStat(stat) + value);
		if (playerStats != null && stat.getPlayerStat() != null) playerStats.addStat(stat.getPlayerStat(), value);
		match.updateMatchScoreboard(playerName);
	}
	
	public int getStat(TDMMatchStat stat) {
		return stats.get(stat);
	}
	
	public Map<TDMMatchStat, Integer> getStats() {
		return stats;
	}
	
	public void calculateQuotes() {
		stats.put(TDMMatchStat.ACCURACY, Utils.calculateQuote(getStat(TDMMatchStat.HITS), getStat(TDMMatchStat.SHOTS)));
		stats.put(TDMMatchStat.KD, Utils.calculateQuote(getStat(TDMMatchStat.KILLS), getStat(TDMMatchStat.DEATHS)));
		
		if (playerStats != null) playerStats.calculateQuotes();
	}
}
