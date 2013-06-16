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

	public void sendTop(final CommandSender sender, final PlayerStat stat) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			
			@Override
			public void run() {
				Map<String, String> vars = new HashMap<String, String>();
				if (plugin.sql.sqlPlayers.statsList.contains(stat)) {
					vars.put("stats", stat.getKey());
					Map<PlayerStat, Integer> topStats = plugin.sql.sqlPlayers.getTop10Stats(stat);
					sender.sendMessage(Translator.getString("TOP_TEN", vars));
					for (int i = 1; i <= 10; i++) {
						if (i <= topStats.keySet().toArray().length) {
							vars.put("rank", String.valueOf(i));
							vars.put("player", (String)topStats.keySet().toArray()[i-1]);
							if (stat.equalsIgnoreCase("kd")||stat.equalsIgnoreCase("hitquote")) {
								float valueF = (float)(Integer)topStats.values().toArray()[i-1] / 100;
								DecimalFormat dec = new DecimalFormat("###.##");
								vars.put("value", dec.format(valueF));
							} else vars.put("value", String.valueOf((Integer)topStats.values().toArray()[i-1]));
							sender.sendMessage(Translator.getString("TOP_TEN_ENTRY", vars));
						}
						else break;
					}
				} else {
					vars.put("values", plugin.sql.sqlPlayers.getStatsListString());
					sender.sendMessage(Translator.getString("VALUE_NOT_FOUND", vars));
				}
			}
		});
	}

	public void sendRank(final CommandSender sender, final String name, final PlayerStat stat) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			
			@Override
			public void run() {
				Map<String, String> vars = new HashMap<String, String>();
				vars.put("player", name);
				if (plugin.pm.exists(name)) {
					if (plugin.sql.sqlPlayers.statsList.contains(stat)) {
						vars.put("rank", String.valueOf(getRank(name, stat)));
						vars.put("stats", stat.getKey());
						sender.sendMessage(Translator.getString("RANK_PLAYER", vars));
					} else {
						vars.put("values", plugin.sql.sqlPlayers.getStatsListString());
						sender.sendMessage(Translator.getString("VALUE_NOT_FOUND", vars));
					}
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
		HashMap<String, String> vars = new HashMap<String, String>();
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
				if(plugin.pm.exists(name)) {
					//GET STATS
					LinkedHashMap<String, Integer> pStats = plugin.sql.sqlPlayers.getPlayerStats(name);
					LinkedHashMap<String, SimpleEntry<String, Integer>> topStats = plugin.sql.sqlPlayers.getTopStats();

					//KD + HITQUOTE
					float kdF = (float)pStats.get("kd") / 100;
					float hitquoteF = (float)pStats.get("hitquote") / 100;
					//TOP
					int kdT = topStats.get("kd").getValue();
					int hitquoteT = topStats.get("hitquote").getValue();
					float kdFT = (float)kdT / 100;
					float hitquoteFT = (float)hitquoteT / 100;

					DecimalFormat dec = new DecimalFormat("###.##");

					for(String stat : pStats.keySet()) {
						vars.put(stat, String.valueOf(pStats.get(stat)));
						vars.put("player_"+stat+"_top", topStats.get(stat).getKey());
						vars.put(""+stat+"_top", String.valueOf(topStats.get(stat).getValue()));
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
					for(String stat : pStats.keySet()) {
						sender.sendMessage(Translator.getString("STATS_"+stat.toUpperCase(), vars));
					}

				} else {
					sender.sendMessage(Translator.getString("PLAYER_NOT_FOUND", vars));
				}
			}
		});
	}

}
