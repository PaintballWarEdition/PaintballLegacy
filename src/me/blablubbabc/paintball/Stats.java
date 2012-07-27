package me.blablubbabc.paintball;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;
import me.blablubbabc.BlaDB.Register;
import org.bukkit.entity.Player;

public class Stats {

	private Paintball plugin;
	private Register data;
	
	
	private LinkedHashMap<String, Integer> points;
	private LinkedHashMap<String, Integer> kills;
	private LinkedHashMap<String, Integer> deaths;
	private LinkedHashMap<String, Integer> shots;
	private LinkedHashMap<String, Integer> hits;
	private LinkedHashMap<String, Integer> teamattacks;
	private LinkedHashMap<String, Integer> hitquote;
	private LinkedHashMap<String, Integer> wins;
	private LinkedHashMap<String, Integer> looses;
	private LinkedHashMap<String, Integer> money;
	private LinkedHashMap<String, Integer> kd;
	private LinkedHashMap<String, Integer> rounds;
	
	private LinkedHashMap<String, Integer> topPoints;
	private LinkedHashMap<String, Integer> topKills;
	private LinkedHashMap<String, Integer> topDeaths;
	private LinkedHashMap<String, Integer> topShots;
	private LinkedHashMap<String, Integer> topHits;
	private LinkedHashMap<String, Integer> topTeamattacks;
	private LinkedHashMap<String, Integer> topHitquote;
	private LinkedHashMap<String, Integer> topWins;
	private LinkedHashMap<String, Integer> topLooses;
	private LinkedHashMap<String, Integer> topMoney;
	private LinkedHashMap<String, Integer> topKD;
	private LinkedHashMap<String, Integer> topRounds;
	
	
	public Stats (Paintball pl) {
		plugin = pl;
		data = new Register();
		loadDB();
		calculateRanks();
	}
	
	private void loadDB() {
		if(plugin.data.getRegister("stats") == null) saveData();
		else data = plugin.data.getRegister("stats");
		if(data.getValue("rounds") == null) data.setValue("rounds", 0);
		if(data.getValue("kills") == null) data.setValue("kills", 0);
		if(data.getValue("shots") == null) data.setValue("shots", 0);
		if(data.getValue("money") == null) data.setValue("money", 0);
		
		saveData();
	}
	
	///////////
	//SORTING
	private LinkedHashMap<String, Integer> sortMap(Map<String,Integer> map) {
        SortedSet<Map.Entry<String,Integer>> sortedEntries = new TreeSet<Map.Entry<String,Integer>>(
            new Comparator<Map.Entry<String,Integer>>() {
                @Override public int compare(Map.Entry<String,Integer> e1, Map.Entry<String,Integer> e2) {
                    int res = e2.getValue().compareTo(e1.getValue());
                    return res != 0 ? res : 1; // Special fix to preserve items with equal values
                }
            }
        );
        sortedEntries.addAll(map.entrySet());
        LinkedHashMap<String, Integer> sorted_map = new LinkedHashMap<String, Integer>();
		for(Entry<String, Integer> e : sortedEntries) {
			sorted_map.put(e.getKey(), e.getValue());
		}
        return sorted_map;
    }
	
	////////////////////////////////////
	//GENERAL
	////////////////////////////////////
	public void resetData() {
		data.clear();
	}
	
	public void saveData() {
		plugin.data.addRegister("stats", data);
		plugin.data.saveFile();
	}
	
	public void addRounds(int rounds) {
		data.setValue("rounds", (data.getInt("rounds") + rounds)) ;
	}
	public void addShots(int shots) {
		data.setValue("shots", (data.getInt("shots") + shots));
	}
	public void addKills(int kills) {
		data.setValue("kills", (data.getInt("kills") + kills));
	}
	public void addMoney(int money) {
		data.setValue("money", (data.getInt("money") + money));
	}
	//GETTER
	public int getKills() {
		return data.getInt("kills");
	}
	public int getRounds() {
		return data.getInt("rounds");
	}
	public int getShots() {
		return data.getInt("shots");
	}
	public int getMoney() { 
		return data.getInt("money");
	}
	
	
	//STATS
	@SuppressWarnings("unchecked")
	public void calculateRanks() {
		points = new LinkedHashMap<String, Integer>();
		kills = new LinkedHashMap<String, Integer>();
		deaths = new LinkedHashMap<String, Integer>();
		shots = new LinkedHashMap<String, Integer>();
		hits = new LinkedHashMap<String, Integer>();
		teamattacks = new LinkedHashMap<String, Integer>();
		hitquote = new LinkedHashMap<String, Integer>();
		wins = new LinkedHashMap<String, Integer>();
		looses = new LinkedHashMap<String, Integer>();
		money = new LinkedHashMap<String, Integer>();
		kd = new LinkedHashMap<String, Integer>();
		rounds = new LinkedHashMap<String, Integer>();
		
		LinkedHashMap<String, Object> players = plugin.pm.getData();
		for(String name : players.keySet()) {
			LinkedHashMap<String, Object> player = (LinkedHashMap<String, Object>) players.get(name);
			points.put(name, (Integer) player.get("points"));
			kills.put(name, (Integer) player.get("kills"));
			deaths.put(name, (Integer) player.get("deaths"));
			shots.put(name, (Integer) player.get("shots"));
			hits.put(name, (Integer) player.get("hits"));
			if(shots.get(name) > 0) hitquote.put(name, ( ((Integer) player.get("hits")*100) / (Integer) player.get("shots") ));
			else hitquote.put(name, 0);
			teamattacks.put(name, (Integer) player.get("teamattacks"));
			wins.put(name, (Integer) player.get("wins"));
			looses.put(name, (Integer) player.get("looses"));
			money.put(name, (Integer) player.get("money"));
			if(deaths.get(name) > 0) kd.put(name, ( ((Integer)player.get("kills")*100) / (Integer) player.get("deaths") ));
			else kd.put(name, ( (Integer)player.get("kills") / 1 ));
			rounds.put(name, ( (Integer)player.get("wins") + (Integer)player.get("looses") ));
		}
		
		topPoints = new LinkedHashMap<String, Integer>();
		topKills = new LinkedHashMap<String, Integer>();
		topDeaths = new LinkedHashMap<String, Integer>();
		topShots = new LinkedHashMap<String, Integer>();
		topHits = new LinkedHashMap<String, Integer>();
		topHitquote = new LinkedHashMap<String, Integer>();
		topTeamattacks = new LinkedHashMap<String, Integer>();
		topWins = new LinkedHashMap<String, Integer>();
		topLooses = new LinkedHashMap<String, Integer>();
		topMoney = new LinkedHashMap<String, Integer>();
		topKD = new LinkedHashMap<String, Integer>();
		topRounds = new LinkedHashMap<String, Integer>();
		
		topPoints = sortMap(points);
		topKills = sortMap(kills);
		topDeaths = sortMap(deaths);
		topShots = sortMap(shots);
		topHits = sortMap(hits);
		topHitquote = sortMap(hitquote);
		topTeamattacks = sortMap(teamattacks);
		topWins = sortMap(wins);
		topLooses = sortMap(looses);
		topMoney = sortMap(money);
		topKD = sortMap(kd);
		topRounds = sortMap(rounds);
		
			
	}
	
	public int getRank(String name) {
		int rank  = 1;
		for(Entry<String, Integer> e : topPoints.entrySet()) {
			if(e.getKey().equalsIgnoreCase(name)) {
				return rank;
			} else {
				rank++;
			}	
		}
		return rank;
	}
	
	public void sendTop(Player player) {
		calculateRanks();
		player.sendMessage(plugin.t.getString("TOP_TEN"));
		HashMap<String, String> vars = new HashMap<String, String>();
		for(int i = 1; i <= 10; i++) {
			if(i <= topPoints.keySet().toArray().length) {
				vars.put("rank", String.valueOf(i));
				vars.put("player", (String)topPoints.keySet().toArray()[i-1]);
				vars.put("points", String.valueOf((Integer)topPoints.values().toArray()[i-1]));
				player.sendMessage(plugin.t.getString("TOP_TEN_ENTRY", vars));
			}
			else break;
		}
	}
	
	public void sendRank(Player player, String name) {
		calculateRanks();
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("player", name);
		if(plugin.pm.exists(name)) {
			vars.put("rank", String.valueOf(getRank(name)));
			player.sendMessage(plugin.t.getString("RANK_PLAYER", vars));
		} else {
			player.sendMessage(plugin.t.getString("PLAYER_NOT_FOUND", vars));
		}
	}
	
	public void sendCash(Player player, String name) {
		calculateRanks();
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("player", name);
		if(plugin.pm.exists(name)) {
			vars.put("cash", String.valueOf(topMoney.get(name)));
			player.sendMessage(plugin.t.getString("CASH_PLAYER", vars));
		} else {
			player.sendMessage(plugin.t.getString("PLAYER_NOT_FOUND", vars));
		}
	}
	
	public void sendStats(Player player, String name) {
		calculateRanks();
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("player", name);
		if(plugin.pm.exists(name)) {
			
			//TOP PLAYERS
			String player_points_top = (String)topPoints.keySet().toArray()[0];
			String player_cash_top = (String)topMoney.keySet().toArray()[0];
			String player_kills_top = (String)topKills.keySet().toArray()[0];
			String player_deaths_top = (String)topDeaths.keySet().toArray()[0];
			String player_kd_top = (String)topKD.keySet().toArray()[0];
			String player_shots_top = (String)topShots.keySet().toArray()[0];
			String player_hits_top = (String)topHits.keySet().toArray()[0];
			String player_hitquote_top = (String)topHitquote.keySet().toArray()[0];
			String player_teamattacks_top = (String)topTeamattacks.keySet().toArray()[0];
			String player_rounds_top = (String)topRounds.keySet().toArray()[0];
			String player_wins_top = (String)topWins.keySet().toArray()[0];
			String player_looses_top = (String)topLooses.keySet().toArray()[0];
			
			//KD + HITQUOTE
			float kdF = (float)topKD.get(name) / 100;
			float hitquoteF = (float)topHitquote.get(name) / 100;
			//TOP
			int kdT = (Integer) topKD.get(player_kd_top);
			int hitquoteT = (Integer) topHitquote.get(player_hitquote_top);
			float kdFT = (float)kdT / 100;
			float hitquoteFT = (float)hitquoteT / 100;
			
			DecimalFormat dec = new DecimalFormat("###.##");
			
			
			vars.put("points", String.valueOf(topPoints.get(name)));
			vars.put("player_points_top", player_points_top);
			vars.put("points_top", String.valueOf(topPoints.get(player_points_top)));
			
			vars.put("cash", String.valueOf(topMoney.get(name)));
			vars.put("player_cash_top", player_cash_top);
			vars.put("cash_top", String.valueOf(topMoney.get(player_cash_top)));
			
			vars.put("kills", String.valueOf(topKills.get(name)));
			vars.put("player_kills_top", player_kills_top);
			vars.put("kills_top", String.valueOf(topKills.get(player_kills_top)));
			
			vars.put("deaths", String.valueOf(topDeaths.get(name)));
			vars.put("player_deaths_top", player_deaths_top);
			vars.put("deaths_top", String.valueOf(topDeaths.get(player_deaths_top)));
			
			vars.put("kd", dec.format(kdF));
			vars.put("player_kd_top", player_kd_top);
			vars.put("kd_top", dec.format(kdFT));
			
			vars.put("shots", String.valueOf(topShots.get(name)));
			vars.put("player_shots_top", player_shots_top);
			vars.put("shots_top", String.valueOf(topShots.get(player_shots_top)));
			
			vars.put("hits", String.valueOf(topHits.get(name)));
			vars.put("player_hits_top", player_hits_top);
			vars.put("hits_top", String.valueOf(topHits.get(player_hits_top)));
			
			vars.put("hitquote", dec.format(hitquoteF));
			vars.put("player_hitquote_top", player_hitquote_top);
			vars.put("hitquote_top", dec.format(hitquoteFT));
			
			vars.put("teamattacks", String.valueOf(topTeamattacks.get(name)));
			vars.put("player_teamattacks_top", player_teamattacks_top);
			vars.put("teamattacks_top", String.valueOf(topTeamattacks.get(player_teamattacks_top)));
			
			vars.put("rounds", String.valueOf(topRounds.get(name)));
			vars.put("player_rounds_top", player_rounds_top);
			vars.put("rounds_top", String.valueOf(topRounds.get(player_rounds_top)));
			
			vars.put("wins", String.valueOf(topWins.get(name)));
			vars.put("player_wins_top", player_wins_top);
			vars.put("wins_top", String.valueOf(topWins.get(player_wins_top)));
			
			vars.put("looses", String.valueOf(topLooses.get(name)));
			vars.put("player_looses_top", player_looses_top);
			vars.put("looses_top", String.valueOf(topLooses.get(player_looses_top)));
			
			
			player.sendMessage(plugin.t.getString("STATS_HEADER"));
			player.sendMessage(plugin.t.getString("STATS_PLAYER", vars));
			player.sendMessage(plugin.t.getString("STATS_POINTS", vars));
			player.sendMessage(plugin.t.getString("STATS_CASH", vars));
			player.sendMessage(plugin.t.getString("STATS_KILLS", vars));
			player.sendMessage(plugin.t.getString("STATS_DEATHS", vars));
			player.sendMessage(plugin.t.getString("STATS_KD", vars));
			player.sendMessage(plugin.t.getString("STATS_SHOTS", vars));
			player.sendMessage(plugin.t.getString("STATS_HITS", vars));
			player.sendMessage(plugin.t.getString("STATS_HITQUOTE", vars));
			player.sendMessage(plugin.t.getString("STATS_TEAMATTACKS", vars));
			player.sendMessage(plugin.t.getString("STATS_ROUNDS", vars));
			player.sendMessage(plugin.t.getString("STATS_WINS", vars));
			player.sendMessage(plugin.t.getString("STATS_LOOSES", vars));
			
			player.sendMessage(plugin.t.getString("STATS_GENERAL"));
			vars.put("shots", String.valueOf(getShots()));
			vars.put("kills", String.valueOf(getKills()));
			vars.put("rounds", String.valueOf(getRounds()));
			vars.put("cash", String.valueOf(getMoney()));
			player.sendMessage(plugin.t.getString("STATS_GENERAL_SHOTS", vars));
			player.sendMessage(plugin.t.getString("STATS_GENERAL_KILLS", vars));
			player.sendMessage(plugin.t.getString("STATS_GENERAL_ROUNDS", vars));
			player.sendMessage(plugin.t.getString("STATS_GENERAL_MONEY", vars));
		} else {
			player.sendMessage(plugin.t.getString("PLAYER_NOT_FOUND", vars));
		}	
	}
	
}
