package de.blablubbabc.paintball;

import java.util.ArrayList;
import java.util.List;

public class RankManager {
	
	private static List<Rank> ranks = new ArrayList<Rank>();
	
	public static void loadRanks() {
		//TODO
	}
	
	public static Rank getRank(String playerName) {
		Rank highest = null;
		
		if (Paintball.instance.pm.exists(playerName)) {
			int points = Paintball.instance.pm.getStats(playerName).get("points");
			//get highest rank:
			for (Rank rank : ranks) {
				int needed = rank.getNeededPoints();
				if (needed <= points) {
					if (highest == null || needed > highest.getNeededPoints()) {
						highest = rank;
					}
				}
			}
		}

		return highest;
	}
}
