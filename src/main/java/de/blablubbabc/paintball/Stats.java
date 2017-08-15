/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball;

import java.text.DecimalFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import de.blablubbabc.paintball.statistics.general.GeneralStat;
import de.blablubbabc.paintball.statistics.player.PlayerStat;
import de.blablubbabc.paintball.statistics.player.PlayerStats;
import de.blablubbabc.paintball.utils.Translator;

public class Stats {

	private Paintball plugin;
	public static DecimalFormat decimalFormat = new DecimalFormat("###.##");

	public Stats(Paintball pl) {
		plugin = pl;
	}

	// //////////////////////////////////
	// GENERAL
	// //////////////////////////////////

	// SETTER
	public void addGeneralStats(final Map<GeneralStat, Integer> stats) {
		Paintball.getInstance().addAsyncTask();
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				plugin.sql.sqlGeneralStats.addStats(stats);
				Paintball.getInstance().removeAsyncTask();
			}
		});
	}

	public void matchEndStats(final Map<GeneralStat, Integer> stats, final int playerAmount) {
		Paintball.getInstance().addAsyncTask();
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				plugin.sql.sqlGeneralStats.addStatsMatchEnd(stats, playerAmount);
				Paintball.getInstance().removeAsyncTask();
			}
		});
	}

	public void setGeneralStats(final Map<GeneralStat, Integer> stats) {
		Paintball.getInstance().addAsyncTask();
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				plugin.sql.sqlGeneralStats.setStats(stats);
				Paintball.getInstance().removeAsyncTask();
			}
		});
	}

	// GETTER
	public Map<GeneralStat, Integer> getGerneralStats() {
		return plugin.sql.sqlGeneralStats.getStats();
	}

	public int getRank(UUID playerUUID, PlayerStat stat) {
		return plugin.sql.sqlPlayers.getRank(playerUUID, stat);
	}

	public void sendTop(final CommandSender sender, final String key) {
		PlayerStat stat = PlayerStat.getFromKey(key);
		if (stat != null) {
			sendTop(sender, stat);
		} else {
			Map<String, String> vars = new HashMap<String, String>();
			vars.put("values", PlayerStat.getKeysAsString());
			sender.sendMessage(Translator.getString("VALUE_NOT_FOUND", vars));
		}
	}

	public void sendTop(final CommandSender sender, final PlayerStat stat) {
		Paintball.getInstance().addAsyncTask();
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				Map<String, String> vars = new HashMap<String, String>();
				vars.put("stats", stat.getKey());
				LinkedHashMap<String, Integer> topStats = plugin.sql.sqlPlayers.getTop10Stats(stat);

				String[] players = new String[topStats.size()];
				int j = 0;
				for (String name : topStats.keySet()) {
					players[j++] = name;
				}

				Integer[] values = new Integer[topStats.size()];
				j = 0;
				for (Integer value : topStats.values()) {
					values[j++] = value;
				}

				sender.sendMessage(Translator.getString("TOP_TEN", vars));
				for (int i = 1; i <= 10; i++) {
					if (i <= players.length) {
						vars.put("rank", String.valueOf(i));
						vars.put("player", players[i - 1]);
						if (stat == PlayerStat.KD || stat == PlayerStat.HITQUOTE) {
							float valueF = (float) values[i - 1] / 100;
							vars.put("value", decimalFormat.format(valueF));
						} else vars.put("value", String.valueOf(values[i - 1]));
						sender.sendMessage(Translator.getString("TOP_TEN_ENTRY", vars));
					} else break;
				}
				Paintball.getInstance().removeAsyncTask();
			}
		});
	}

	public void sendRank(final CommandSender sender, final UUID playerUUID, final String playerName, final String key) {
		PlayerStat stat = PlayerStat.getFromKey(key);
		if (stat != null) {
			sendRank(sender, playerUUID, playerName, stat);
		} else {
			Map<String, String> vars = new HashMap<String, String>();
			vars.put("values", PlayerStat.getKeysAsString());
			sender.sendMessage(Translator.getString("VALUE_NOT_FOUND", vars));
		}
	}

	public void sendRank(final CommandSender sender, final UUID playerUUID, final String playerName, final PlayerStat stat) {
		Paintball.getInstance().addAsyncTask();
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				Map<String, String> vars = new HashMap<String, String>();
				vars.put("player", playerName);
				if (plugin.playerManager.exists(playerUUID)) {
					vars.put("rank", String.valueOf(getRank(playerUUID, stat)));
					vars.put("stats", stat.getKey());
					sender.sendMessage(Translator.getString("RANK_PLAYER", vars));
				} else {
					sender.sendMessage(Translator.getString("PLAYER_NOT_FOUND", vars));
				}
				Paintball.getInstance().removeAsyncTask();
			}
		});
	}

	public void sendCash(final CommandSender sender, final UUID playerUUID, final String playerName) {
		Paintball.getInstance().addAsyncTask();
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				Map<String, String> vars = new HashMap<String, String>();
				vars.put("player", playerName);
				PlayerStats stats = plugin.playerManager.getPlayerStats(playerUUID);
				if (stats != null) {
					vars.put("money", String.valueOf(stats.getStat(PlayerStat.MONEY)));
					sender.sendMessage(Translator.getString("CASH_PLAYER", vars));
				} else {
					sender.sendMessage(Translator.getString("PLAYER_NOT_FOUND", vars));
				}
				Paintball.getInstance().removeAsyncTask();
			}
		});
	}

	private void sendGeneralStats(CommandSender sender) {
		Map<String, String> vars = new HashMap<String, String>();
		// GENERAL STATS
		Map<GeneralStat, Integer> generalStats = getGerneralStats();
		for (GeneralStat stat : generalStats.keySet()) {
			vars.put(stat.getKey(), String.valueOf(generalStats.get(stat)));
		}

		// SEND
		sender.sendMessage(Translator.getString("STATS_GENERAL"));
		for (GeneralStat stat : generalStats.keySet()) {
			sender.sendMessage(Translator.getString("STATS_GENERAL_" + stat.getKey().toUpperCase(), vars));
		}
	}

	public void sendStats(final CommandSender sender, final UUID playerUUID, final String playerName) {
		Paintball.getInstance().addAsyncTask();
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				Map<String, String> vars = new HashMap<String, String>();
				vars.put("player", playerName);
				PlayerStats stats = plugin.playerManager.getPlayerStats(playerUUID);
				if (stats != null) {
					// GET STATS
					LinkedHashMap<PlayerStat, SimpleEntry<String, Integer>> topStats = plugin.sql.sqlPlayers.getTopStats();

					// KD + HITQUOTE
					float kdF = (float) stats.getStat(PlayerStat.KD) / 100;
					float hitquoteF = (float) stats.getStat(PlayerStat.HITQUOTE) / 100;
					// TOP
					int kdT = topStats.get(PlayerStat.KD).getValue();
					int hitquoteT = topStats.get(PlayerStat.HITQUOTE).getValue();
					float kdFT = (float) kdT / 100;
					float hitquoteFT = (float) hitquoteT / 100;

					for (PlayerStat stat : PlayerStat.values()) {
						String key = stat.getKey();
						vars.put(key, String.valueOf(stats.getStat(stat)));
						vars.put("player_" + key + "_top", topStats.get(stat).getKey());
						vars.put(key + "_top", String.valueOf(topStats.get(stat).getValue()));
					}
					// KD
					vars.put("kd", decimalFormat.format(kdF));
					vars.put("kd_top", decimalFormat.format(kdFT));
					// HITQUOTE
					vars.put("hitquote", decimalFormat.format(hitquoteF));
					vars.put("hitquote_top", decimalFormat.format(hitquoteFT));

					// SEND
					sender.sendMessage(Translator.getString("STATS_HEADER"));
					sendGeneralStats(sender);
					sender.sendMessage(Translator.getString("STATS_PLAYER", vars));
					for (PlayerStat stat : PlayerStat.values()) {
						String key = stat.getKey();
						sender.sendMessage(Translator.getString("STATS_" + key.toUpperCase(), vars));
					}

				} else {
					sender.sendMessage(Translator.getString("PLAYER_NOT_FOUND", vars));
				}
				Paintball.getInstance().removeAsyncTask();
			}
		});
	}

}
