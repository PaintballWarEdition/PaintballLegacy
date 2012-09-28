package me.blablubbabc.BlaDB;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import me.blablubbabc.paintball.Paintball;

public class SQLGeneralStats {

	private static BlaSQLite sql;
	@SuppressWarnings("unused")
	private static Paintball plugin;
	
	public ArrayList<String> statsList;

	public SQLGeneralStats(BlaSQLite blasql, Paintball pl) {
		sql = blasql;
		plugin = pl;
		statsList = new ArrayList<String>();
		statsList.add("rounds"); statsList.add("kills"); statsList.add("shots"); statsList.add("money_spent"); 
		statsList.add("average_players"); statsList.add("max_players");
	}

	public void createDefaultTables() {
		//general stats
		HashMap<String, String> stats = new HashMap<String, String>();
		for(String s : statsList) {
			stats.put(s, "INTEGER");
		}
		sql.createDefaultTable("general_stats", stats, null);		
	}
	
	//PLAYERDATA
	//GET

	public HashMap<String, Integer> getStats() {
		HashMap<String, Integer> data = new HashMap<String, Integer>();
		ResultSet rs = sql.resultQuery("SELECT * FROM general_stats WHERE rowid=1 LIMIT 1;");
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
	public void addStats(String player, HashMap<String, Integer> stats) {
		String query = "";
		for(Entry<String, Integer> entry : stats.entrySet()) {
			String key = entry.getKey();
			if(statsList.contains(key)) {
				query += key+"="+key+"+"+entry.getValue()+",";
			}
		}
		if(query.length() > 0) {
			query = query.substring(0, query.length()-1);
			sql.updateQuery("UPDATE OR IGNORE general_stats SET "+query+" WHERE name='"+player+"';");
		}
	}

	public void setStats(String player, HashMap<String, Integer> stats) {
		String query = "";
		for(Entry<String, Integer> entry : stats.entrySet()) {
			String key = entry.getKey();
			if(statsList.contains(key)) {
				query += key+"="+entry.getValue()+",";
			}
		}
		if(query.length() > 0) {
			query = query.substring(0, query.length()-1);
			sql.updateQuery("UPDATE OR IGNORE general_stats SET "+query+" WHERE name='"+player+"';");
		}
	}

	//REMOVE
	//ADD NEW

}
