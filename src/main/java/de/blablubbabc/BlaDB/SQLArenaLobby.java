/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.BlaDB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.statistics.arena.ArenaSetting;
import de.blablubbabc.paintball.statistics.arena.ArenaStat;
import de.blablubbabc.paintball.utils.Log;

public class SQLArenaLobby {

	private static BlaSQLite sql;

	public SQLArenaLobby(BlaSQLite blasql) {
		sql = blasql;

		createDefaultTables();
	}

	private void createDefaultTables() {
		// arenas
		String arenasQuery = "name TEXT, active INTEGER";
		sql.createDefaultTable("arenas", arenasQuery, "name");
		// arenasettings
		HashMap<String, String> arenasettings = new HashMap<String, String>();
		arenasettings.put("name", "TEXT");
		for (String key : ArenaSetting.getKeys()) {
			arenasettings.put(key, "INTEGER");
		}
		sql.createDefaultTable("arenasettings", arenasettings, "name");

		// arenastats
		HashMap<String, String> arenastats = new HashMap<String, String>();
		arenastats.put("name", "TEXT");
		for (String key : ArenaStat.getKeys()) {
			arenastats.put(key, "INTEGER");
		}
		sql.createDefaultTable("arenastats", arenastats, "name");

		// locations
		String locationsQuery = "id INTEGER PRIMARY KEY, world TEXT, x INTEGER, y INTEGER, z INTEGER, yaw REAL, pitch REAL";
		sql.createDefaultTable("locations", locationsQuery, null);

		// red/blue/spec/lobby spawns
		String spawnsQuery = "arena TEXT, location_id INTEGER";
		sql.createDefaultTable("redspawns", spawnsQuery, null);
		sql.createDefaultTable("bluespawns", spawnsQuery, null);
		sql.createDefaultTable("specspawns", spawnsQuery, null);

		String lobbyQuery = "location_id INTEGER";
		sql.createDefaultTable("lobbyspawns", lobbyQuery, null);

		// INIT RESET
		for (String arena : this.getAllArenaNames()) {
			sql.updateQuery("UPDATE OR IGNORE arenas SET active=0 WHERE name='" + arena + "';");
		}
	}

	// ARENADATA and LOBBYSPAWNS
	// GET
	public List<String> getAllArenaNames() {
		List<String> arenas = new ArrayList<String>();

		Result r = sql.resultQuery("SELECT name FROM arenas;");
		ResultSet rs = r.getResultSet();
		try {
			if (rs != null) {
				while (rs.next()) {
					arenas.add(rs.getString(1));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			r.close();
		}
		return arenas;
	}

	public Map<ArenaStat, Integer> getArenaStats(String arena) {
		Map<ArenaStat, Integer> data = new LinkedHashMap<ArenaStat, Integer>();
		Result r = sql.resultQuery("SELECT * FROM arenastats WHERE name='" + arena + "' LIMIT 1;");
		ResultSet rs = r.getResultSet();
		try {
			if (rs != null && rs.next()) {
				for (ArenaStat stat : ArenaStat.values()) {
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

	public Map<ArenaSetting, Integer> getArenaSettings(String arena) {
		Map<ArenaSetting, Integer> data = new LinkedHashMap<ArenaSetting, Integer>();
		Result r = sql.resultQuery("SELECT * FROM arenasettings WHERE name='" + arena + "' LIMIT 1;");
		ResultSet rs = r.getResultSet();
		try {
			if (rs != null && rs.next()) {
				for (ArenaSetting setting : ArenaSetting.values()) {
					data.put(setting, rs.getInt(setting.getKey()));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			r.close();
		}
		return data;
	}

	private List<Location> getLocations(List<Integer> ids) {
		List<Location> locs = new ArrayList<Location>();
		if (ids.size() > 0) {
			String idss = "";
			for (int id : ids) {
				idss += "id='" + id + "' OR ";
			}
			idss = idss.substring(0, idss.length() - 4);
			Result r = sql.resultQuery("SELECT * FROM locations WHERE " + idss + ";");
			ResultSet rs = r.getResultSet();
			try {
				if (rs != null) {
					while (rs.next()) {
						String worldName = rs.getString("world");
						World world = Paintball.instance.getServer().getWorld(worldName);
						if (world == null) {
							Log.warning("[!] Paintball tried to load a location from database which is located in an unknown, not yet loaded, or non-existing world ( "
									+ worldName + " ).    Please report this warning to blablubbabc on 'http://dev.bukkit.org/server-mods/paintball_pure_war/tickets/'"
									+ " together with a list of your plugins, especially plugins you know to be related to world creating, loading, managing or teleportation. Thank you!");
							Log.warning("[!!!] The world '" + worldName + "' will now be loaded, or created if it is not existing. If you want to delete or rename a world, make sure to remove all arena and lobby spawns from it first!");
							world = Paintball.instance.getServer().createWorld(new WorldCreator(worldName));
						}
						Location loc = new Location(world,
													rs.getDouble("x"), rs.getDouble("y"),
													rs.getDouble("z"), rs.getFloat("yaw"),
													rs.getFloat("pitch"));
						locs.add(loc);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				r.close();
			}
		}
		return locs;
	}

	private void removeLocations(List<Integer> ids) {
		if (ids.size() > 0) {
			String idss = "";
			for (int id : ids) {
				idss += "id='" + id + "' OR ";
			}
			idss = idss.substring(0, idss.length() - 4);
			sql.updateQuery("DELETE FROM locations WHERE " + idss + ";");
		}
	}

	public boolean isArenaExisting(String arena) {
		Result r = sql.resultQuery("SELECT EXISTS(SELECT 1 FROM arenas WHERE name='" + arena + "' LIMIT 1);");
		ResultSet rs = r.getResultSet();
		boolean b = false;
		try {
			if (rs != null) {
				b = (rs.getInt(1) == 1);
			}
		} catch (SQLException e) {
			e.printStackTrace();

		} finally {
			r.close();
		}
		return b;

	}

	public int getRedspawnsSize(String arena) {
		Result r = sql.resultQuery("SELECT COUNT(*) FROM redspawns WHERE arena = '" + arena + "';");
		ResultSet rs = r.getResultSet();
		int a = 0;
		try {
			if (rs != null) {
				a = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			r.close();
		}
		return a;
	}

	public int getBluespawnsSize(String arena) {
		Result r = sql.resultQuery("SELECT COUNT(*) FROM bluespawns WHERE arena = '" + arena + "';");
		ResultSet rs = r.getResultSet();
		int a = 0;
		try {
			if (rs != null) {
				a = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			r.close();
		}
		return a;
	}

	public int getSpecspawnsSize(String arena) {
		Result r = sql.resultQuery("SELECT COUNT(*) FROM specspawns WHERE arena = '" + arena + "';");
		ResultSet rs = r.getResultSet();
		int a = 0;
		try {
			if (rs != null) {
				a = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			r.close();
		}
		return a;
	}

	public int getLobbyspawnsSize() {
		Result r = sql.resultQuery("SELECT COUNT(*) FROM lobbyspawns;");
		ResultSet rs = r.getResultSet();
		int a = 0;
		try {
			if (rs != null) {
				a = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			r.close();
		}
		return a;
	}

	private List<Integer> getRedspawnsIds(String arena) {
		List<Integer> ids = new ArrayList<Integer>();
		Result r = sql.resultQuery("SELECT location_id FROM redspawns WHERE arena = '" + arena + "';");
		ResultSet rs = r.getResultSet();
		try {
			if (rs != null) {
				while (rs.next()) {
					ids.add(rs.getInt(1));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			r.close();
		}
		return ids;
	}

	private List<Integer> getBluespawnsIds(String arena) {
		List<Integer> ids = new ArrayList<Integer>();
		Result r = sql.resultQuery("SELECT location_id FROM bluespawns WHERE arena = '" + arena + "';");
		ResultSet rs = r.getResultSet();
		try {
			if (rs != null) {
				while (rs.next()) {
					ids.add(rs.getInt(1));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			r.close();
		}
		return ids;
	}

	private List<Integer> getSpecspawnsIds(String arena) {
		List<Integer> ids = new ArrayList<Integer>();
		Result r = sql.resultQuery("SELECT location_id FROM specspawns WHERE arena = '" + arena + "';");
		ResultSet rs = r.getResultSet();
		try {
			if (rs != null) {
				while (rs.next()) {
					ids.add(rs.getInt(1));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			r.close();
		}
		return ids;
	}

	private List<Integer> getLobbyspawnsIds() {
		List<Integer> ids = new ArrayList<Integer>();
		Result r = sql.resultQuery("SELECT location_id FROM lobbyspawns;");
		ResultSet rs = r.getResultSet();
		try {
			if (rs != null) {
				while (rs.next()) {
					ids.add(rs.getInt(1));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			r.close();
		}
		return ids;
	}

	public List<Location> getRedspawns(String arena) {
		return getLocations(getRedspawnsIds(arena));
	}

	public List<Location> getBluespawns(String arena) {
		return getLocations(getBluespawnsIds(arena));
	}

	public List<Location> getSpecspawns(String arena) {
		return getLocations(getSpecspawnsIds(arena));
	}

	public List<Location> getLobbyspawns() {
		return getLocations(getLobbyspawnsIds());
	}

	public boolean isArenaActive(String arena) {
		Result r = sql.resultQuery("SELECT active FROM arenas WHERE name='" + arena + "' LIMIT 1;");
		ResultSet rs = r.getResultSet();
		boolean active = false;
		try {
			if (rs != null && rs.next()) {
				active = (rs.getInt(1) == 1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			r.close();
		}
		return active;
	}

	// SET
	public void setArenaActive(String arena) {
		sql.updateQuery("UPDATE OR IGNORE arenas SET active=1 WHERE name='" + arena + "';");
	}

	public void setArenaNotActive(String arena) {
		sql.updateQuery("UPDATE OR IGNORE arenas SET active=0 WHERE name='" + arena + "';");
	}

	public void addArenaStats(String arena, Map<ArenaStat, Integer> stats) {
		String query = "";
		for (Entry<ArenaStat, Integer> entry : stats.entrySet()) {
			String key = entry.getKey().getKey();
			query += key + "=" + key + "+'" + entry.getValue() + "',";
		}
		if (query.length() > 0) {
			query = query.substring(0, query.length() - 1);
			sql.updateQuery("UPDATE OR IGNORE arenastats SET " + query + " WHERE name='" + arena + "';");
		}
	}

	public void setArenaStats(String arena, Map<ArenaStat, Integer> stats) {
		String query = "";
		for (Entry<ArenaStat, Integer> entry : stats.entrySet()) {
			String key = entry.getKey().getKey();
			query += key + "='" + entry.getValue() + "',";
		}
		if (query.length() > 0) {
			query = query.substring(0, query.length() - 1);
			sql.updateQuery("UPDATE OR IGNORE arenastats SET " + query + " WHERE name='" + arena + "';");
		}
	}

	public void setArenaSettings(String arena, Map<ArenaSetting, Integer> settings) {
		String query = "";
		for (Entry<ArenaSetting, Integer> entry : settings.entrySet()) {
			String key = entry.getKey().getKey();
			query += key + "='" + entry.getValue() + "',";
		}
		if (query.length() > 0) {
			query = query.substring(0, query.length() - 1);
			sql.updateQuery("UPDATE OR IGNORE arenasettings SET " + query + " WHERE name='" + arena + "';");
		}
	}

	// REMOVE
	public void removeArena(String arena) {
		sql.updateQuery("DELETE FROM arenas WHERE name='" + arena + "';");
		sql.updateQuery("DELETE FROM arenasettings WHERE name='" + arena + "';");
		sql.updateQuery("DELETE FROM arenastats WHERE name='" + arena + "';");
		removeRedspawns(arena);
		removeBluespawns(arena);
		removeSpecspawns(arena);
	}

	public void removeRedspawns(String arena) {
		removeLocations(getRedspawnsIds(arena));
		sql.updateQuery("DELETE FROM redspawns WHERE arena='" + arena + "';");
	}

	public void removeBluespawns(String arena) {
		removeLocations(getBluespawnsIds(arena));
		sql.updateQuery("DELETE FROM bluespawns WHERE arena='" + arena + "';");
	}

	public void removeSpecspawns(String arena) {
		removeLocations(getSpecspawnsIds(arena));
		sql.updateQuery("DELETE FROM specspawns WHERE arena='" + arena + "';");
	}

	public void removeLobbyspawns() {
		removeLocations(getLobbyspawnsIds());
		sql.updateQuery("DELETE FROM lobbyspawns;");
	}

	// ADD NEW
	public int addLocation(Location loc) {
		return sql.updateQuery("INSERT OR IGNORE INTO locations(world, x, y, z, yaw, pitch) VALUES('" + loc.getWorld().getName() + "','" + loc.getX() + "','" + loc.getY() + "','" + loc.getZ() + "','" + loc.getYaw() + "','" + loc.getPitch() + "');");
	}

	public void addNewArena(String arena) {
		// ARENAS name TEXT, active INTEGER
		sql.updateQuery("INSERT OR IGNORE INTO arenas(name, active) VALUES('" + arena + "','0');");
		// ARENASETTINGS
		String settingsQuery = "";
		String settingsValues = "";
		for (String key : ArenaSetting.getKeys()) {
			settingsQuery += "," + key;
			settingsValues += ",'0'";
		}
		sql.updateQuery("INSERT OR IGNORE INTO arenasettings(name" + settingsQuery + ") VALUES('" + arena + "'" + settingsValues + ");");

		// ARENASTATS
		String statsQuery = "";
		String statsValues = "";
		for (String key : ArenaStat.getKeys()) {
			statsQuery += "," + key;
			statsValues += ",'0'";
		}
		sql.updateQuery("INSERT OR IGNORE INTO arenastats(name" + statsQuery + ") VALUES('" + arena + "'" + statsValues + ");");
	}

	public void addLobbyspawn(Location loc) {
		int row = this.addLocation(loc);
		sql.updateQuery("INSERT OR IGNORE INTO lobbyspawns(location_id) VALUES('" + row + "');");
	}

	public void addRedspawn(Location loc, String arena) {
		int row = addLocation(loc);
		sql.updateQuery("INSERT OR IGNORE INTO redspawns(arena, location_id) VALUES('" + arena + "','" + row + "');");
	}

	public void addBluespawn(Location loc, String arena) {
		int row = addLocation(loc);
		sql.updateQuery("INSERT OR IGNORE INTO bluespawns(arena, location_id) VALUES('" + arena + "','" + row + "');");
	}

	public void addSpecspawn(Location loc, String arena) {
		int row = addLocation(loc);
		sql.updateQuery("INSERT OR IGNORE INTO specspawns(arena, location_id) VALUES('" + arena + "','" + row + "');");
	}

}
