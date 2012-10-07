package me.blablubbabc.BlaDB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import me.blablubbabc.paintball.Paintball;

public class SQLPlayers {

	private static BlaSQLite sql;
	@SuppressWarnings("unused")
	private static Paintball plugin;

	public LinkedList<String> statsList;

	public SQLPlayers(BlaSQLite blasql, Paintball pl) {
		sql = blasql;
		plugin = pl;

		statsList = new LinkedList<String>();
		statsList.add("points"); statsList.add("money"); statsList.add("money_spent"); 
		statsList.add("kills"); statsList.add("deaths"); statsList.add("kd");
		statsList.add("shots"); statsList.add("hits"); statsList.add("hitquote"); statsList.add("teamattacks");
		statsList.add("grenades"); statsList.add("airstrikes");
		statsList.add("rounds"); statsList.add("wins"); statsList.add("defeats"); statsList.add("draws");

	}

	public void createDefaultTables() {
		//playerstats
		HashMap<String, String> players = new HashMap<String, String>();
		players.put("name", "TEXT");
		for(String s : statsList) {
			players.put(s, "INTEGER");
		}
		sql.createDefaultTable("players", players, "name");
	}

	//PLAYERDATA
	//GET
	public String getStatsListString() {
		String values = "";
		for(String s : statsList) {
			values += s + ",";
		}
		if(values.length() > 1) values.substring(0, (values.length() -1));
		return values;
	}
	public int getRank(String player, String stat) {
		int rank = 0;
		ResultSet rs = sql.resultQuery("SELECT COUNT(*) FROM players WHERE "+stat+" > (SELECT "+stat+" from players WHERE name='"+player+"');");
		try {
			if(rs != null && rs.next()) {
				rank = rs.getInt(1)+1 ;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return rank;
	}

	public int getPlayerCount() {
		ResultSet rs = sql.resultQuery("SELECT COUNT(*) FROM players;");
		try {
			if(rs != null) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public ArrayList<String> getAllPlayerNames() {
		ArrayList<String> players = new ArrayList<String>();

		ResultSet rs = sql.resultQuery("SELECT name FROM players;");
		try {
			if(rs != null) {
				while(rs.next()) {
					players.add(rs.getString(1));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return players;
	}

	public boolean isPlayerExisting(String player) {
		ResultSet rs = sql.resultQuery("SELECT EXISTS(SELECT 1 FROM players WHERE name='"+player+"' LIMIT 1);");
		try {
			if(rs != null) {
				return (rs.getInt(1) == 1 ? true : false);
			} else return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

	}

	public LinkedHashMap<String, Integer> getPlayerStats(String player) {
		LinkedHashMap<String, Integer> data = new LinkedHashMap<String, Integer>();
		ResultSet rs = sql.resultQuery("SELECT * FROM players WHERE name = '"+player+"' LIMIT 1;");
		try {
			if(rs != null && rs.next()) {
				for(String stat : statsList) {
					data.put(stat, rs.getInt(stat));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return data;
	}
	//SET
	public void addPlayerStats(String player, HashMap<String, Integer> stats) {
		String query = "";
		for(Entry<String, Integer> entry : stats.entrySet()) {
			String key = entry.getKey();
			if(statsList.contains(key)) {
				query += key+"="+key+"+'"+entry.getValue()+"',";
			}
		}
		if(query.length() > 0) {
			query = query.substring(0, query.length()-1);
			sql.updateQuery("UPDATE OR IGNORE players SET "+query+" WHERE name='"+player+"';");
		}
	}

	public void setPlayerStats(String player, HashMap<String, Integer> stats) {
		String query = "";
		for(Entry<String, Integer> entry : stats.entrySet()) {
			String key = entry.getKey();
			if(statsList.contains(key)) {
				query += key+"='"+entry.getValue()+"',";
			}
		}
		if(query.length() > 0) {
			query = query.substring(0, query.length()-1);
			sql.updateQuery("UPDATE OR IGNORE players SET "+query+" WHERE name='"+player+"';");
		}
	}

	public void resetAllPlayerStats() {
		String query = "";
		for(String stat : statsList) {
			query += stat+"='0',";
		}
		if(query.length() > 0) {
			query = query.substring(0, query.length()-1);
			sql.updateQuery("UPDATE OR IGNORE players SET "+query+";");
		}
	}

	public void resetPlayerStats(String player) {
		String query = "";
		for(String stat : statsList) {
			query += stat+"='0',";
		}
		if(query.length() > 0) {
			query = query.substring(0, query.length()-1);
			sql.updateQuery("UPDATE OR IGNORE players SET "+query+" WHERE name='"+player+"';");
		}
	}

	//REMOVE
	public void removePlayer(String player) {
		sql.updateQuery("DELETE FROM players WHERE name='"+player+"';");
	}

	//ADD NEW
	public void addNewPlayer(String player) {
		String query = "";
		String queryV = "";
		if(statsList.size() > 0) {
			for(String s : statsList) {
				query += ","+s;
				queryV += ",'0'";
			}
		}
		sql.updateQuery("INSERT OR IGNORE INTO players(name"+query+") VALUES('"+player+"'"+queryV+");");
	}

	//STATS, RANGLISTEN, TOP
	public void calculateStats(String player) {
		/*statsList = new ArrayList<String>();
		statsList.add("points"); statsList.add("kills"); statsList.add("deaths"); statsList.add("kd");
		statsList.add("shots"); statsList.add("hits"); statsList.add("hitquote"); statsList.add("teamattacks");
		statsList.add("rounds"); statsList.add("wins"); statsList.add("defeats");
		statsList.add("money"); statsList.add("money_spent"); 
		statsList.add("grenades"); statsList.add("airstrikes");
		 */

		String kd = "";
		String hitquote = "";

		HashMap<String, Integer> pStats = getPlayerStats(player);

		if(pStats.get("shots") > 0) hitquote="(hits*'100')/shots";
		else hitquote = "hits*'100'";

		if(pStats.get("deaths") > 0) kd="(kills*'100')/deaths";
		else kd = "kills*'100'";

		String query = "rounds=wins+deaths+draws,kd="+kd+",hitquote="+hitquote+"";
		sql.updateQuery("UPDATE OR IGNORE players SET "+query+" WHERE name='"+player+"';");
	}

	public LinkedHashMap<String, SimpleEntry<String, Integer>> getTopStats() {
		LinkedHashMap<String, SimpleEntry<String, Integer>> topStats = new LinkedHashMap<String, SimpleEntry<String, Integer>>();
		for(String stats : statsList) {
			ResultSet rs = sql.resultQuery("SELECT name,"+stats+" FROM players ORDER BY "+stats+" DESC LIMIT 1");
			try {
				if(rs != null && rs.next()) {
					topStats.put(stats, new SimpleEntry<String, Integer>(rs.getString("name"), rs.getInt(stats)));
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return topStats;
	}

	public LinkedHashMap<String, Integer> getTop10Stats(String stat) {
		LinkedHashMap<String, Integer> topStats = new LinkedHashMap<String, Integer>();
		ResultSet rs = sql.resultQuery("SELECT name,"+stat+" FROM players ORDER BY "+stat+" DESC LIMIT 10");
		try {
			if(rs != null) {
				while(rs.next()) {
					topStats.put(rs.getString("name"), rs.getInt(stat));
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return topStats;
	}


}
