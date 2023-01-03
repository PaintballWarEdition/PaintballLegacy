/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.statistics.player.match.tdm;

import de.blablubbabc.paintball.statistics.player.PlayerStat;

public enum TDMMatchStat {

	POINTS(PlayerStat.POINTS),
	MONEY(PlayerStat.MONEY),
	MONEY_SPENT(PlayerStat.MONEY_SPENT),
	KILLS(PlayerStat.KILLS),
	DEATHS(PlayerStat.DEATHS),
	KD(PlayerStat.KD),
	SHOTS(PlayerStat.SHOTS),
	HITS(PlayerStat.HITS),
	HITQUOTE(PlayerStat.HITQUOTE),
	TEAMATTACKS(PlayerStat.TEAMATTACKS),
	GRENADES(PlayerStat.GRENADES),
	AIRSTRIKES(PlayerStat.AIRSTRIKES);

	private final PlayerStat playerStat;

	private TDMMatchStat(PlayerStat playerStat) {
		this.playerStat = playerStat;
	}

	public PlayerStat getPlayerStat() {
		return playerStat;
	}
}
