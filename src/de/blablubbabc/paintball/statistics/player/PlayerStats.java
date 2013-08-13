package de.blablubbabc.paintball.statistics.player;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.Rank;
import de.blablubbabc.paintball.utils.KeyValuePair;
import de.blablubbabc.paintball.utils.Translator;
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
		if (stat == PlayerStat.POINTS) {
			rankupNotification(getStat(PlayerStat.POINTS), value);
		}
		
		stats.put(stat, value);
		dirty = true;
	}
	
	public void setStats(Map<PlayerStat, Integer> otherStats) {
		for (Entry<PlayerStat, Integer> entry : otherStats.entrySet()) {
			setStat(entry.getKey(), entry.getValue());
		}
	}
	
	private void rankupNotification(int oldPoints, int newPoints) {
		Rank rank = Paintball.instance.rankManager.getRank(playerName);
		Rank new_rank = Paintball.instance.rankManager.getRank(newPoints);
		
		if (rank != new_rank) {
			if (newPoints > oldPoints) {
				// will this rank be reached?
				Player player = Bukkit.getPlayerExact(playerName);
				if (player != null && player.isOnline()) {
					
					player.playSound(player.getEyeLocation(), Sound.LEVEL_UP, 1F, 2F);
					// highest Rank now?
					KeyValuePair pluginString = new KeyValuePair("plugin", Translator.getString("PLUGIN"));
					KeyValuePair newRankName = new KeyValuePair("new_rank", new_rank.getName());
					
					Rank next_rank = Paintball.instance.rankManager.getNextRank(new_rank);
					// max rank
					if (next_rank == new_rank) {
						player.sendMessage(Translator.getString("RANK_UP_NOTIFICATION_MAX", pluginString, newRankName));
					} else {
						int needed_points = next_rank.getNeededPoints() - newPoints;
						player.sendMessage(Translator.getString("RANK_UP_NOTIFICATION", pluginString, newRankName, new KeyValuePair("needed_points", String.valueOf(needed_points)), new KeyValuePair("next_rank", next_rank.getName())));
					}
				}
			} else if (newPoints < oldPoints) {
				Player player = Bukkit.getPlayerExact(playerName);
				if (player != null && player.isOnline()) {
					KeyValuePair pluginString = new KeyValuePair("plugin", Translator.getString("PLUGIN"));
					KeyValuePair newRankName = new KeyValuePair("new_rank", new_rank.getName());
					
					player.sendMessage(Translator.getString("RANK_DOWN_NOTIFICATION", pluginString, newRankName));
				}
			}
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
		stats.put(PlayerStat.HITQUOTE, Utils.calculateQuote(getStat(PlayerStat.HITS), getStat(PlayerStat.SHOTS)));
		stats.put(PlayerStat.KD, Utils.calculateQuote(getStat(PlayerStat.KILLS), getStat(PlayerStat.DEATHS)));
	}
	
	public void save() {
		if (dirty) {
			Paintball.instance.sql.sqlPlayers.setPlayerStats(playerName, stats);
			
			//Paintball.instance.pm.setStats(playerName, stats);
			dirty = false;
			
			// update stats on scoreboard, if player is in lobby:
			Paintball.instance.playerManager.updateLobbyScoreboard(playerName);
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
