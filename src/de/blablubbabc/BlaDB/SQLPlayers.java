package de.blablubbabc.BlaDB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.blablubbabc.paintball.statistics.player.PlayerStat;


public class SQLPlayers {

	private static BlaSQLite sql;

	public SQLPlayers(BlaSQLite blasql) {
		sql = blasql;
	}

	public void createDefaultTables() {
		//playerstats
		HashMap<String, String> players = new HashMap<String, String>();
		players.put("name", "TEXT");
		for (String key : PlayerStat.getKeys()) {
			players.put(key, "INTEGER");
		}
		sql.createDefaultTable("players", players, "name");
	}

	//PLAYERDATA
	//GET
	public int getRank(String player, PlayerStat stat) {
		String key = stat.getKey();
		int rank = 0;
		Result r = sql.resultQuery("SELECT COUNT(*) FROM players WHERE " + key + " > (SELECT " + key + " from players WHERE name='" + player + "');");
		ResultSet rs = r.getResultSet();
		try {
			if(rs != null && rs.next()) {
				rank = rs.getInt(1) + 1;
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			r.close();
		}
		return rank;
	}

	public int getPlayerCount() {
		Result r = sql.resultQuery("SELECT COUNT(*) FROM players;");
		ResultSet rs = r.getResultSet();
		int a = 0;
		try {
			if(rs != null) {
				a = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			r.close();
		}
		return a;
	}

	/*public List<String> getAllPlayerNames() {
		List<String> players = new ArrayList<String>();

		Result r = sql.resultQuery("SELECT name FROM players;");
		ResultSet rs = r.getResultSet();
		try {
			if(rs != null) {
				while(rs.next()) {
					players.add(rs.getString(1));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			r.close();
		}
		return players;
	}*/
	
	public int getPlayersEverPlayedCount() {
		Result r = sql.resultQuery("SELECT COUNT(*) FROM players WHERE rounds > 0;");
		ResultSet rs = r.getResultSet();
		int result = 0;
		try {
			if(rs != null) {
				while(rs.next()) {
					result = rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			r.close();
		}
		return result;
	}

	public boolean isPlayerExisting(String player) {
		Result r = sql.resultQuery("SELECT EXISTS(SELECT 1 FROM players WHERE name='" + player + "' LIMIT 1);");
		ResultSet rs = r.getResultSet();
		boolean b = false;
		try {
			if(rs != null) {
				b = (rs.getInt(1) == 1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			r.close();
		}
		return b;

	}

	public Map<PlayerStat, Integer> getPlayerStats(String player) {
		Map<PlayerStat, Integer> data = new LinkedHashMap<PlayerStat, Integer>();
		Result r = sql.resultQuery("SELECT * FROM players WHERE name = '" + player + "' LIMIT 1;");
		ResultSet rs = r.getResultSet();
		try {
			if(rs != null && rs.next()) {
				for (PlayerStat stat : PlayerStat.values()) {
					data.put(stat, rs.getInt(stat.getKey()));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			r.close();
		}
		return data;
	}
	//SET
	public void addPlayerStats(String player, Map<PlayerStat, Integer> stats) {
		String query = "";
		for (Entry<PlayerStat, Integer> entry : stats.entrySet()) {
			String key = entry.getKey().getKey();
			query += key + "=" + key + "+'" + entry.getValue() + "',";
		}
		if (query.length() > 0) {
			query = query.substring(0, query.length() - 1);
			sql.updateQuery("UPDATE OR IGNORE players SET " + query + " WHERE name='" + player + "';");
		}
	}

	public void setPlayerStats(String player, Map<PlayerStat, Integer> stats) {
		String query = "";
		for (Entry<PlayerStat, Integer> entry : stats.entrySet()) {
			String key = entry.getKey().getKey();
			query += key + "='" + entry.getValue() + "',";
		}
		if (query.length() > 0) {
			query = query.substring(0, query.length() - 1);
			sql.updateQuery("UPDATE OR IGNORE players SET "+query+" WHERE name='" + player + "';");
		}
	}

	public void resetAllPlayerStats() {
		String query = "";
		for (String stat : PlayerStat.getKeys()) {
			query += stat + "='0',";
		}
		if (query.length() > 0) {
			query = query.substring(0, query.length() - 1);
			sql.updateQuery("UPDATE OR IGNORE players SET " + query + ";");
		}
	}

	public void resetPlayerStats(String player) {
		String query = "";
		for (String stat : PlayerStat.getKeys()) {
			query += stat + "='0',";
		}
		if (query.length() > 0) {
			query = query.substring(0, query.length() - 1);
			sql.updateQuery("UPDATE OR IGNORE players SET " + query + " WHERE name='" + player + "';");
		}
	}

	//REMOVE
	public void removePlayer(String player) {
		sql.updateQuery("DELETE FROM players WHERE name='" + player + "';");
	}

	//ADD NEW
	public void addNewPlayer(String player) {
		String query = "";
		String queryV = "";
		for (String s : PlayerStat.getKeys()) {
			query += "," + s;
			queryV += ",'0'";
		}
		sql.updateQuery("INSERT OR IGNORE INTO players(name" + query + ") VALUES('" + player + "'" + queryV + ");");
	}

	//STATS, RANGLISTEN, TOP
	//TODO necesarry ?
	public void calculateStats(String player) {
		String kd = "";
		String hitquote = "";

		Map<PlayerStat, Integer> pStats = getPlayerStats(player);

		if (pStats.get(PlayerStat.SHOTS) > 0) hitquote = "(hits*'100')/shots";
		else hitquote = "hits*'100'";

		if (pStats.get(PlayerStat.DEATHS) > 0) kd = "(kills*'100')/deaths";
		else kd = "kills*'100'";

		String query = "rounds=wins+deaths+draws,kd=" + kd + ",hitquote=" + hitquote + "";
		sql.updateQuery("UPDATE OR IGNORE players SET "+query+" WHERE name='"+player+"';");
	}

	public LinkedHashMap<PlayerStat, SimpleEntry<String, Integer>> getTopStats() {
		LinkedHashMap<PlayerStat, SimpleEntry<String, Integer>> topStats = new LinkedHashMap<PlayerStat, SimpleEntry<String, Integer>>();
		for (PlayerStat stat : PlayerStat.values()) {
			String key = stat.getKey();
			Result r = sql.resultQuery("SELECT name," + key + " FROM players ORDER BY " + key + " DESC LIMIT 1");
			ResultSet rs = r.getResultSet();
			try {
				if(rs != null && rs.next()) {
					topStats.put(stat, new SimpleEntry<String, Integer>(rs.getString("name"), rs.getInt(key)));
				}
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				r.close();
			}
		}
		return topStats;
	}

	public LinkedHashMap<String, Integer> getTop10Stats(PlayerStat stat) {
		String key = stat.getKey();
		LinkedHashMap<String, Integer> topStats = new LinkedHashMap<String, Integer>();
		Result r = sql.resultQuery("SELECT name," + stat + " FROM players ORDER BY " + stat + " DESC LIMIT 10");
		ResultSet rs = r.getResultSet();
		try {
			if(rs != null) {
				while(rs.next()) {
					topStats.put(rs.getString("name"), rs.getInt(key));
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			r.close();
		}
		return topStats;
	}


}
