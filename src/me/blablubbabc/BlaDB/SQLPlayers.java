package me.blablubbabc.BlaDB;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import me.blablubbabc.paintball.Paintball;

public class SQLPlayers {

	private static BlaSQLite sql;
	@SuppressWarnings("unused")
	private static Paintball plugin;
	
	public ArrayList<String> statsList;

	public SQLPlayers(BlaSQLite blasql, Paintball pl) {
		sql = blasql;
		plugin = pl;

		statsList = new ArrayList<String>();
		statsList.add("points"); statsList.add("kills"); statsList.add("deaths"); statsList.add("kd");
		statsList.add("shots"); statsList.add("hits"); statsList.add("hitquote"); statsList.add("teamattacks");
		statsList.add("rounds"); statsList.add("wins"); statsList.add("looses");
		statsList.add("money"); statsList.add("money_spent"); 
		statsList.add("grenades"); statsList.add("airstrikes");
		
	}
	
	public void createDefaultTables() {
		//playerstats
		HashMap<String, String> arenas = new HashMap<String, String>();
		arenas.put("name", "TEXT");
		for(String s : statsList) {
			arenas.put(s, "INTEGER");
		}
		sql.createDefaultTable("players", arenas, "name");
	}
	
	//PLAYERDATA
	//GET
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

	public HashMap<String, Integer> getPlayerStats(String player) {
		HashMap<String, Integer> data = new HashMap<String, Integer>();
		ResultSet rs = sql.resultQuery("SELECT * FROM players WHERE name = '"+player+"' LIMIT 1;");
		try {
			if(rs != null && rs.first()) {
				ResultSetMetaData rsmd = rs.getMetaData();
				int columns = rsmd.getColumnCount();
				for(int i = 2; i <= columns; i++) {
					data.put(rs.getMetaData().getColumnName(i), rs.getInt(i));
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
				query += key+"="+key+"+"+entry.getValue()+",";
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
				query += key+"="+entry.getValue()+",";
			}
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
				queryV += ",0";
			}
		}
		sql.updateQuery("INSERT OR IGNORE INTO players(name"+query+") VALUES('"+player+"'"+queryV+");");
	}


}
