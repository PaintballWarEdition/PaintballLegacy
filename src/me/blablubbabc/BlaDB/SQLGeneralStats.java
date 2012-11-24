package me.blablubbabc.BlaDB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import me.blablubbabc.paintball.Paintball;

public class SQLGeneralStats {

	private static BlaSQLite sql;
	@SuppressWarnings("unused")
	private static Paintball plugin;
	
	public LinkedList<String> statsList;

	public SQLGeneralStats(BlaSQLite blasql, Paintball pl) {
		sql = blasql;
		plugin = pl;
		statsList = new LinkedList<String>();
		statsList.add("shots"); statsList.add("grenades"); statsList.add("airstrikes"); statsList.add("kills"); statsList.add("rounds"); statsList.add("money_spent"); 
		statsList.add("average_players"); statsList.add("max_players");
	}

	public void createDefaultTables() {
		//general stats
		sql.createDefaultTable("general_stats", "key TEXT, value INTEGER", "key");
		
		//DEFAULT VALUES:
		for(String s : statsList) {
			sql.updateQuery("INSERT OR IGNORE INTO general_stats(key,value) VALUES('"+s+"','0');");
		}
	}
	
	//GET

	public LinkedHashMap<String, Integer> getStats() {
		LinkedHashMap<String, Integer> data = new LinkedHashMap<String, Integer>();
		Result r = sql.resultQuery("SELECT * FROM general_stats;");
		ResultSet rs = r.getResultSet();
		try {
			if(rs != null) {
				while(rs.next()) {
					data.put(rs.getString("key"), rs.getInt("value"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		r.close();
		return data;
	}
	//SET
	
	public void addStats(HashMap<String, Integer> stats) {
		for(String key : stats.keySet()) {
			if(statsList.contains(key)) {
				String query = "value=value+'"+stats.get(key)+"'";
				sql.updateQuery("UPDATE OR IGNORE general_stats SET "+query+" WHERE key='"+key+"';");
			}
		}
	}

	public void setStats(HashMap<String, Integer> stats) {
		for(String key : stats.keySet()) {
			if(statsList.contains(key)) {
				String query = "value=value+'"+stats.get(key)+"'";
				sql.updateQuery("UPDATE OR IGNORE general_stats SET "+query+" WHERE key='"+key+"';");
			}
		}
	}
	
	public void addStatsMatchEnd(HashMap<String, Integer> stats, int playerAmount) {
		for(String key : stats.keySet()) {
			if(statsList.contains(key)) {
				String query = "value=value+'"+stats.get(key)+"'";
				sql.updateQuery("UPDATE OR IGNORE general_stats SET "+query+" WHERE key='"+key+"';");
			}
		}
		//CALCULATE AVERAGE PLAYERS + MAX PLAYERS
		String queryC = "value=value+(("+playerAmount+"-value)/(SELECT value FROM general_stats WHERE key='rounds'))";
		sql.updateQuery("UPDATE OR IGNORE general_stats SET "+queryC+" WHERE key='average_players';");
		
		String queryMax = "value=(CASE WHEN value>='"+playerAmount+"' THEN value ELSE '"+playerAmount+"' END)";
		sql.updateQuery("UPDATE OR IGNORE general_stats SET "+queryMax+" WHERE key='max_players';");
	}

	//REMOVE
	//ADD NEW

}
