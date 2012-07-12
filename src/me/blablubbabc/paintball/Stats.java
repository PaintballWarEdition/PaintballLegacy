package me.blablubbabc.paintball;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;
import me.blablubbabc.BlaDB.Register;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Stats {

	private Paintball plugin;
	private Register data;
	
	private ChatColor red = ChatColor.RED;
	private ChatColor green = ChatColor.GREEN;
	private ChatColor aqua = ChatColor.AQUA;
	private ChatColor yellow = ChatColor.YELLOW;
	private ChatColor gold = ChatColor.GOLD;
	private ChatColor gray = ChatColor.GRAY;
	
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
			if(shots.get(name) > 0) hitquote.put(name, ( (Integer) player.get("hits") / (Integer) player.get("shots") ));
			else hitquote.put(name, 0);
			teamattacks.put(name, (Integer) player.get("teamattacks"));
			wins.put(name, (Integer) player.get("wins"));
			looses.put(name, (Integer) player.get("looses"));
			money.put(name, (Integer) player.get("money"));
			if(deaths.get(name) > 0) kd.put(name, ( (Integer)player.get("kills") / (Integer) player.get("deaths") ));
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
		player.sendMessage(aqua+""+ ChatColor.BOLD+"["+yellow+""+ ChatColor.BOLD+" ***** Paintball Top 10 Players ***** "+aqua+""+ ChatColor.BOLD+"] ");
		for(int i = 1; i <= 10; i++) {
			if(i <= topPoints.keySet().toArray().length) player.sendMessage(gold+""+ChatColor.BOLD+ "Rank " + i + " " + aqua + topPoints.keySet().toArray()[i-1] + " ( " + topPoints.values().toArray()[i-1] + " )");
			else break;
		}
	}
	
	public void sendRank(Player player, String name) {
		calculateRanks();
		if(plugin.pm.exists(name)) {
			player.sendMessage(yellow+""+ ChatColor.BOLD+"Paintball Rank: " + green + getRank(name));
		} else {
			player.sendMessage(gray + "Player " + name + " not found.");
		}
	}
	
	public void sendCash(Player player, String name) {
		calculateRanks();
		if(plugin.pm.exists(name)) {
			player.sendMessage(green+"Cash "+gray+name+green+": "+topMoney.get(name));
		} else {
			player.sendMessage(gray + "Player " + name + " not found.");
		}
	}
	
	public void sendStats(Player player, String name) {
		calculateRanks();
		if(plugin.pm.exists(name)) {
			player.sendMessage(aqua+""+ ChatColor.BOLD+"["+yellow+""+ ChatColor.BOLD+" -------Paintball Stats------- "+aqua+""+ ChatColor.BOLD+"] ");
			player.sendMessage(red+"__________Stats: "+green+ name +red+"__________");
			player.sendMessage(green+"Points: "+aqua+topPoints.get(name)+gold+" ( Top: "+ topPoints.values().toArray()[0] + " )");
			player.sendMessage(green+"Cash: "+aqua+topMoney.get(name)+gold+" ( Top: "+ topMoney.values().toArray()[0] + " )");
			player.sendMessage(green+"Kills: "+aqua+topKills.get(name)+gold+" ( Top: "+ topKills.values().toArray()[0] + " )");
			player.sendMessage(green+"Deaths: "+aqua+topDeaths.get(name)+gold+" ( Top: "+ topDeaths.values().toArray()[0] + " )");
			player.sendMessage(green+"K/D: "+aqua+topKD.get(name)+gold+" ( Top: "+ topKD.values().toArray()[0] + " )");
			player.sendMessage(green+"Shots: "+aqua+topShots.get(name)+gold+" ( Top: "+ topShots.values().toArray()[0] + " )");
			player.sendMessage(green+"Hits: "+aqua+topHits.get(name)+gold+" ( Top: "+ topHits.values().toArray()[0] + " )");
			player.sendMessage(green+"Hitquote: "+aqua+topHitquote.get(name)+gold+" ( Top: "+ topHitquote.values().toArray()[0] + " )");
			player.sendMessage(green+"Teamattacks: "+aqua+topTeamattacks.get(name)+gold+" ( Top: "+ topTeamattacks.values().toArray()[0] + " )");
			player.sendMessage(green+"Rounds: "+aqua+topRounds.get(name)+gold+" ( Top: "+ topRounds.values().toArray()[0] + " )");
			player.sendMessage(green+"Wins: "+aqua+topWins.get(name)+gold+" ( Top: "+ topWins.values().toArray()[0] + " )");
			player.sendMessage(green+"Looses: "+aqua+topLooses.get(name)+gold+" ( Top: "+ topLooses.values().toArray()[0] + " )");
			player.sendMessage(red+"__________General Stats__________");
			player.sendMessage(green+"Fired Shots: "+aqua+getShots());
			player.sendMessage(green+"Frags: "+aqua+getKills());
			player.sendMessage(green+"Played Rounds: "+aqua+getRounds());
			player.sendMessage(green+"Spent Cash: "+aqua+getMoney());
		} else {
			player.sendMessage(gray + "Player " + name + " not found.");
		}	
	}
	
}
