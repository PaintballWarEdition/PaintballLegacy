/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.BlaDB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import de.blablubbabc.paintball.statistics.general.GeneralStat;

public class SQLGeneralStats {

	private static BlaSQLite sql;

	public SQLGeneralStats(BlaSQLite blasql) {
		sql = blasql;

		createDefaultTables();
	}

	private void createDefaultTables() {
		// general stats
		sql.createDefaultTable("general_stats", "key TEXT, value INTEGER", "key");

		// DEFAULT VALUES:
		for (String key : GeneralStat.getKeys()) {
			sql.updateQuery("INSERT OR IGNORE INTO general_stats (key,value) VALUES('" + key + "','0');");
		}
	}

	// GET

	public Map<GeneralStat, Integer> getStats() {
		Map<GeneralStat, Integer> data = new LinkedHashMap<GeneralStat, Integer>();
		Result r = sql.resultQuery("SELECT * FROM general_stats;");
		ResultSet rs = r.getResultSet();
		try {
			if (rs != null) {
				while (rs.next()) {
					GeneralStat stat = GeneralStat.getFromKey(rs.getString("key"));
					if (stat != null) data.put(stat, rs.getInt("value"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			r.close();
		}
		return data;
	}
	// SET

	public void addStats(Map<GeneralStat, Integer> stats) {
		for (GeneralStat stat : stats.keySet()) {
			String query = "value=value+'" + stats.get(stat) + "'";
			sql.updateQuery("UPDATE OR IGNORE general_stats SET " + query + " WHERE key='" + stat.getKey() + "';");
		}
	}

	public void setStats(Map<GeneralStat, Integer> stats) {
		for (GeneralStat stat : stats.keySet()) {
			String query = "value=value+'" + stats.get(stat) + "'";
			sql.updateQuery("UPDATE OR IGNORE general_stats SET " + query + " WHERE key='" + stat.getKey() + "';");
		}
	}

	public void addStatsMatchEnd(Map<GeneralStat, Integer> stats, int playerAmount) {
		for (GeneralStat stat : stats.keySet()) {
			String query = "value=value+'" + stats.get(stat) + "'";
			sql.updateQuery("UPDATE OR IGNORE general_stats SET " + query + " WHERE key='" + stat.getKey() + "';");
		}
		// CALCULATE AVERAGE PLAYERS + MAX PLAYERS
		String queryC = "value=value+((" + playerAmount + "-value)/(SELECT value FROM general_stats WHERE key='rounds'))";
		sql.updateQuery("UPDATE OR IGNORE general_stats SET " + queryC + " WHERE key='average_players';");

		String queryMax = "value=(CASE WHEN value>='" + playerAmount + "' THEN value ELSE '" + playerAmount + "' END)";
		sql.updateQuery("UPDATE OR IGNORE general_stats SET " + queryMax + " WHERE key='max_players';");
	}

	// REMOVE
	// ADD NEW
}
