package de.blablubbabc.paintball;

import java.text.DecimalFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;

import de.blablubbabc.paintball.statistics.general.GeneralStat;
import de.blablubbabc.paintball.statistics.player.PlayerStat;
import de.blablubbabc.paintball.utils.Translator;


public class Stats {

	private Paintball plugin;
	private DecimalFormat dec = new DecimalFormat("###.##");

	public Stats (Paintball pl) {
		plugin = pl;
	}

	////////////////////////////////////
	//GENERAL
	////////////////////////////////////

	//SETTER
	public void addGeneralStats(final Map<GeneralStat, Integer> stats) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			
			@Override
			public void run() {
				plugin.sql.sqlGeneralStats.addStats(stats);
			}
		});
	}

	public void matchEndStats(final Map<GeneralStat, Integer> stats, final int playerAmount) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			
			@Override
			public void run() {
				plugin.sql.sqlGeneralStats.addStatsMatchEnd(stats, playerAmount);
			}
		});
	}

	public void setGeneralStats(final Map<GeneralStat, Integer> stats) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			
			@Override
			public void run() {
				plugin.sql.sqlGeneralStats.setStats(stats);
			}
		});
	}
	//GETTER
	public Map<GeneralStat, Integer> getGerneralStats() {
		return plugin.sql.sqlGeneralStats.getStats();
	}

	public int getRank(String name, PlayerStat stat) {
		return plugin.sql.sqlPlayers.getRank(name, stat);
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
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			
			@Override
			public void run() {
				Map<String, String> vars = new HashMap<String, String>();
				vars.put("stats", stat.getKey());
				LinkedHashMap<String, Integer> topStats = plugin.sql.sqlPlayers.getTop10Stats(stat);
				String[] players = (String[]) topStats.keySet().toArray();
				Integer[] values = (Integer[]) topStats.values().toArray();
				sender.sendMessage(Translator.getString("TOP_TEN", vars));
				for (int i = 1; i <= 10; i++) {
					if (i <= players.length) {
						vars.put("rank", String.valueOf(i));
						vars.put("player", players[i-1]);
						if (stat == PlayerStat.KD || stat == PlayerStat.ACCURACY) {
							float valueF = (float) values[i-1] / 100;
							vars.put("value", dec.format(valueF));
						} else vars.put("value", String.valueOf(values[i-1]));
						sender.sendMessage(Translator.getString("TOP_TEN_ENTRY", vars));
					} else break;
				}
			}
		});
	}

	public void sendRank(final CommandSender sender, final String name, final String key) {
		PlayerStat stat = PlayerStat.getFromKey(key);
		if (stat != null) {
			sendRank(sender, name, stat);
		} else {
			Map<String, String> vars = new HashMap<String, String>();
			vars.put("values", PlayerStat.getKeysAsString());
			sender.sendMessage(Translator.getString("VALUE_NOT_FOUND", vars));
		}
	}
	
	public void sendRank(final CommandSender sender, final String name, final PlayerStat stat) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			
			@Override
			public void run() {
				Map<String, String> vars = new HashMap<String, String>();
				vars.put("player", name);
				if (plugin.pm.exists(name)) {
					vars.put("rank", String.valueOf(getRank(name, stat)));
					vars.put("stats", stat.getKey());
					sender.sendMessage(Translator.getString("RANK_PLAYER", vars));
				} else {
					sender.sendMessage(Translator.getString("PLAYER_NOT_FOUND", vars));
				}
			}
		});
	}

	public void sendCash(final CommandSender sender, final String name) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			
			@Override
			public void run() {
				Map<String, String> vars = new HashMap<String, String>();
				vars.put("player", name);
				if (plugin.pm.exists(name)) {
					Map<PlayerStat, Integer> pStats = plugin.sql.sqlPlayers.getPlayerStats(name);
					vars.put("money", String.valueOf(pStats.get(PlayerStat.MONEY)));
					sender.sendMessage(Translator.getString("CASH_PLAYER", vars));
				} else {
					sender.sendMessage(Translator.getString("PLAYER_NOT_FOUND", vars));
				}
			}
		});
	}

	private void sendGeneralStats(CommandSender sender) {
		Map<String, String> vars = new HashMap<String, String>();
		//GENERAL STATS
		Map<GeneralStat, Integer> gStats = getGerneralStats();
		for (GeneralStat stat : gStats.keySet()) {
			vars.put(stat.getKey(), String.valueOf(gStats.get(stat)));
		}

		//SEND
		sender.sendMessage(Translator.getString("STATS_GENERAL"));
		for (GeneralStat stat : gStats.keySet()) {
			sender.sendMessage(Translator.getString("STATS_GENERAL_" + stat.getKey().toUpperCase(), vars));
		}
	}

	public void sendStats(final CommandSender sender, final String name) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			
			@Override
			public void run() {
				HashMap<String, String> vars = new HashMap<String, String>();
				vars.put("player", name);
				if (plugin.pm.exists(name)) {
					//GET STATS
					Map<PlayerStat, Integer> pStats = plugin.sql.sqlPlayers.getPlayerStats(name);
					LinkedHashMap<PlayerStat, SimpleEntry<String, Integer>> topStats = plugin.sql.sqlPlayers.getTopStats();

					//KD + HITQUOTE
					float kdF = (float)pStats.get(PlayerStat.KD) / 100;
					float hitquoteF = (float)pStats.get(PlayerStat.ACCURACY) / 100;
					//TOP
					int kdT = topStats.get(PlayerStat.KD).getValue();
					int hitquoteT = topStats.get(PlayerStat.ACCURACY).getValue();
					float kdFT = (float)kdT / 100;
					float hitquoteFT = (float)hitquoteT / 100;

					for (PlayerStat stat : pStats.keySet()) {
						String key = stat.getKey();
						vars.put(key, String.valueOf(pStats.get(stat)));
						vars.put("player_" + key + "_top", topStats.get(stat).getKey());
						vars.put(key + "_top", String.valueOf(topStats.get(stat).getValue()));
					}
					//KD
					vars.put("kd", dec.format(kdF));
					vars.put("kd_top", dec.format(kdFT));
					//HITQUOTE
					vars.put("hitquote", dec.format(hitquoteF));
					vars.put("hitquote_top", dec.format(hitquoteFT));

					//SEND
					sender.sendMessage(Translator.getString("STATS_HEADER"));
					sendGeneralStats(sender);
					sender.sendMessage(Translator.getString("STATS_PLAYER", vars));
					for (PlayerStat stat : pStats.keySet()) {
						String key = stat.getKey();
						sender.sendMessage(Translator.getString("STATS_" + key.toUpperCase(), vars));
					}

				} else {
					sender.sendMessage(Translator.getString("PLAYER_NOT_FOUND", vars));
				}
			}
		});
	}

}
