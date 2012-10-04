package me.blablubbabc.paintball;

import java.text.DecimalFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.bukkit.command.CommandSender;

public class Stats {

	private Paintball plugin;
	
	public Stats (Paintball pl) {
		plugin = pl;
	}
	
	////////////////////////////////////
	//GENERAL
	////////////////////////////////////
	
	//SETTER
	public void addGeneralStats(HashMap<String, Integer> stats) {
		plugin.sql.sqlGeneralStats.addStats(stats);
	}
	
	public void matchEndStats(HashMap<String, Integer> stats, int playerAmount) {
		plugin.sql.sqlGeneralStats.addStatsMatchEnd(stats, playerAmount);
	}
	
	public void setGeneralStats(HashMap<String, Integer> stats) {
		plugin.sql.sqlGeneralStats.setStats(stats);
	}
	//GETTER
	public LinkedHashMap<String, Integer> getGerneralStats() {
		return plugin.sql.sqlGeneralStats.getStats();
	}
	
	public int getRank(String name, String stat) {
		return plugin.sql.sqlPlayers.getRank(name, stat);
	}
	
	public void sendTop(CommandSender sender, String stat) {
		HashMap<String, String> vars = new HashMap<String, String>();
		if(plugin.sql.sqlPlayers.statsList.contains(stat)) {
			vars.put("stats", stat);
			LinkedHashMap<String, Integer> topStats = plugin.sql.sqlPlayers.getTop10Stats(stat);
			sender.sendMessage(plugin.t.getString("TOP_TEN", vars));
			for(int i = 1; i <= 10; i++) {
				if(i <= topStats.keySet().toArray().length) {
					vars.put("rank", String.valueOf(i));
					vars.put("player", (String)topStats.keySet().toArray()[i-1]);
					vars.put("value", String.valueOf((Integer)topStats.values().toArray()[i-1]));
					sender.sendMessage(plugin.t.getString("TOP_TEN_ENTRY", vars));
				}
				else break;
			}
		} else {
			vars.put("values", plugin.sql.sqlPlayers.getStatsListString());
			sender.sendMessage(plugin.t.getString("VALUE_NOT_FOUND", vars));
		}
	}
	
	public void sendRank(CommandSender sender, String name, String stat) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("player", name);
		if(plugin.pm.exists(name)) {
			vars.put("rank", String.valueOf(getRank(name, stat)));
			sender.sendMessage(plugin.t.getString("RANK_PLAYER", vars));
		} else {
			sender.sendMessage(plugin.t.getString("PLAYER_NOT_FOUND", vars));
		}
	}
	
	public void sendCash(CommandSender sender, String name) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("player", name);
		if(plugin.pm.exists(name)) {
			HashMap<String, Integer> pStats = plugin.sql.sqlPlayers.getPlayerStats(name);
			vars.put("money", String.valueOf(pStats.get("money")));
			sender.sendMessage(plugin.t.getString("CASH_PLAYER", vars));
		} else {
			sender.sendMessage(plugin.t.getString("PLAYER_NOT_FOUND", vars));
		}
	}
	
	public void sendStats(CommandSender sender, String name) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("player", name);
		if(plugin.pm.exists(name)) {
			//GET STATS
			LinkedHashMap<String, Integer> pStats = plugin.sql.sqlPlayers.getPlayerStats(name);
			LinkedHashMap<String, SimpleEntry<String, Integer>> topStats = plugin.sql.sqlPlayers.getTopStats();
			LinkedHashMap<String, Integer> gStats = getGerneralStats();
			
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
			sender.sendMessage(plugin.t.getString("STATS_HEADER"));
			sender.sendMessage(plugin.t.getString("STATS_PLAYER", vars));
			for(String stat : pStats.keySet()) {
				sender.sendMessage(plugin.t.getString("STATS_"+stat.toUpperCase(), vars));
			}
			
			//GENERAL STATS
			for(String stat : gStats.keySet()) {
				vars.put(stat, String.valueOf(gStats.get(stat)));
			}
			
			//SEND
			sender.sendMessage(plugin.t.getString("STATS_GENERAL"));
			for(String stat : gStats.keySet()) {
				sender.sendMessage(plugin.t.getString("STATS_GENERAL_"+stat.toUpperCase(), vars));
			}
			
		} else {
			sender.sendMessage(plugin.t.getString("PLAYER_NOT_FOUND", vars));
		}	
	}
	
}
