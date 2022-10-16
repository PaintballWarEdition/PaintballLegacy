/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.metrics;

import org.bstats.charts.SingleLineChart;

import de.blablubbabc.paintball.PlayerManager;

/**
 * The total number of players that have ever played Paintball on this server.
 */
public class TotalPlayersEverPlayedChart extends SingleLineChart {

	public TotalPlayersEverPlayedChart(PlayerManager playerManager) {
		super("total_players_ever_played", () -> {
			try {
				return playerManager.getPlayersEverPlayedCount();
			} catch (Exception e) {
				return 0;
			}
		});
	}
}
