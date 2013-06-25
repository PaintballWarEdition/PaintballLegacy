package de.blablubbabc.paintball;

import java.util.ArrayList;
import java.util.List;

import de.blablubbabc.paintball.statistics.player.PlayerStat;
import de.blablubbabc.paintball.statistics.player.PlayerStats;

public class RankManager {
	
	private static List<Rank> ranks = new ArrayList<Rank>();
	
	public static void loadRanks() {
		//TODO
	}
	
	public static Rank getRank(String playerName) {
		Rank highest = null;
		
		PlayerStats stats = Paintball.instance.pm.getPlayerStats(playerName);
		// stats even exist for this player ?
		if (stats == null) return null;
		int points = stats.getStat(PlayerStat.POINTS);
		//get highest rank:
		for (Rank rank : ranks) {
			int needed = rank.getNeededPoints();
			if (needed <= points) {
				if (highest == null || needed > highest.getNeededPoints()) {
					highest = rank;
				}
			}
		}

		return highest;
	}
}
