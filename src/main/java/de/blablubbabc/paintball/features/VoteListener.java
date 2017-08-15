/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.features;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.statistics.player.PlayerStat;
import de.blablubbabc.paintball.statistics.player.PlayerStats;
import de.blablubbabc.paintball.utils.Callback;
import de.blablubbabc.paintball.utils.Log;

public class VoteListener implements Listener {

	private final Paintball plugin;

	public VoteListener(Paintball plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onVotifierEvent(VotifierEvent event) {
		Vote vote = event.getVote();
		final String playerName = vote.getUsername();
		if (playerName != null && !playerName.isEmpty()) {
			plugin.playerManager.lookupPlayerUUIDForName(playerName, new Callback<UUID>() {

				@Override
				protected void onComplete(UUID uuid) {
					if (uuid == null) {
						Log.warning("Unknown player voted: " + playerName);
						return;
					}
					PlayerStats stats = plugin.playerManager.getPlayerStats(uuid);
					if (stats != null) {
						stats.addStat(PlayerStat.MONEY, plugin.voteCash);
						stats.saveAsync();
					}
				}
			});
		}

	}
}
