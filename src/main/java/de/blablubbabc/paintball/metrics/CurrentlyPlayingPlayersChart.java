/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.metrics;

import org.bstats.charts.SingleLineChart;

import de.blablubbabc.paintball.Lobby;

/**
 * The maximum number of simultaneously playing players (i.e. inside the lobby) since the last
 * update.
 */
public class CurrentlyPlayingPlayersChart extends SingleLineChart {

	public CurrentlyPlayingPlayersChart() {
		super("currently_playing_players", () -> Lobby.getAndResetMaxPlayersInLobby());
	}
}
