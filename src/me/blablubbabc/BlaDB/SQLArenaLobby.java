package me.blablubbabc.BlaDB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import me.blablubbabc.paintball.Paintball;
import org.bukkit.Location;
import org.bukkit.World;

public class SQLArenaLobby {

	private static BlaSQLite sql;
	private static Paintball plugin;
	
	public ArrayList<String> statsList;
	
	public ArrayList<String> settingsList;

	public SQLArenaLobby(BlaSQLite blasql, Paintball pl) {
		sql = blasql;
		plugin = pl;
		
		statsList = new ArrayList<String>();
		statsList.add("rounds"); statsList.add("kills"); statsList.add("shots"); statsList.add("grenades"); statsList.add("airstrikes");
		
		settingsList = new ArrayList<String>();
		settingsList.add("size"); settingsList.add("balls"); settingsList.add("grenades"); settingsList.add("airstrikes"); settingsList.add("lives"); settingsList.add("respawns");
	}
	
	public void createDefaultTables() {
		//arenas
		HashMap<String, String> arenas = new HashMap<String, String>();
		arenas.put("name", "TEXT");
		for(String s : settingsList) {
			arenas.put(s, "INTEGER");
		}
		sql.createDefaultTable("arenas", arenas, "name");
		
		//arenastats
		HashMap<String, String> arenastats = new HashMap<String, String>();
		arenastats.put("name", "TEXT");
		for(String s : statsList) {
			arenastats.put(s, "INTEGER");
		}
		sql.createDefaultTable("arenastats", arenastats, "name");
		
		//locations
		String locationsQuery = "id INTEGER PRIMARY KEY, world TEXT, x INTEGER, y INTEGER, z INTEGER, yaw REAL, pitch REAL";
		sql.createDefaultTable("locations", locationsQuery);
		
		//red/blue/spec/lobby spawns
		String spawnsQuery = "arena TEXT, location_id INTEGER";
		sql.createDefaultTable("redspawns", spawnsQuery);
		sql.createDefaultTable("bluespawns", spawnsQuery);
		sql.createDefaultTable("specspawns", spawnsQuery);
		
		String lobbyQuery = "location_id INTEGER";
		sql.createDefaultTable("lobbyspawns", lobbyQuery);
		
	}

	//ARENADATA and LOBBYSPAWNS
	//GET
	public ArrayList<String> getAllArenaNames() {
		ArrayList<String> arenas = new ArrayList<String>();

		ResultSet rs = sql.resultQuery("SELECT name FROM arenas;");
		try {
			if(rs != null) {
				while(rs.next()) {
					arenas.add(rs.getString(1));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return arenas;
	}

	public HashMap<String, Integer> getArenaStats(String arena) {
		HashMap<String, Integer> data = new HashMap<String, Integer>();
		ResultSet rs = sql.resultQuery("SELECT * FROM arenastats WHERE name = '"+arena+"' LIMIT 1;");
		try {
			if(rs != null && rs.first()) {
				for(String s : statsList) {
					data.put(s, rs.getInt(s));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return data;
	}
	
	public HashMap<String, Integer> getArenaSettings(String arena) {
		HashMap<String, Integer> data = new HashMap<String, Integer>();
		ResultSet rs = sql.resultQuery("SELECT * FROM arenas WHERE name = '"+arena+"' LIMIT 1;");
		try {
			if(rs != null && rs.first()) {
				for(String s : settingsList) {
					data.put(s, rs.getInt(s));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return data;
	}

	public ArrayList<Location> getLocations(ArrayList<Integer> ids) {
		ArrayList<Location> locs = new ArrayList<Location>();
		if(ids.size() > 0) {
			String idss = "";
			for(int id : ids) {
				idss += "id = "+id+" OR ";
			}
			idss = idss.substring(0, idss.length()-4);
			ResultSet rs = sql.resultQuery("SELECT * FROM locations WHERE "+idss+";");
			if(rs != null) {
				try {
					while (rs.next()) {
						World world = plugin.getServer().getWorld(
								rs.getString("world"));
						if (world != null) {
							Location loc = new Location(world,
									rs.getDouble("x"), rs.getDouble("y"),
									rs.getDouble("z"), rs.getFloat("yaw"),
									rs.getFloat("pitch"));
							locs.add(loc);
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return locs;
	}

	public boolean isArenaExisting(String arena) {
		ResultSet rs = sql.resultQuery("SELECT EXISTS(SELECT 1 FROM arenas WHERE name='"+arena+"' LIMIT 1);");
		try {
			if(rs != null) {
				return (rs.getInt(1) == 1 ? true : false);
			} else return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

	}

	public int getRedspawnsSize(String arena) {
		ResultSet rs = sql.resultQuery("SELECT COUNT(*) FROM redspawns WHERE arena = '"+arena+"';");
		try {
			if(rs != null) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int getBluespawnsSize(String arena) {
		ResultSet rs = sql.resultQuery("SELECT COUNT(*) FROM bluespawns WHERE arena = '"+arena+"';");
		try {
			if(rs != null) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int getSpecspawnsSize(String arena) {
		ResultSet rs = sql.resultQuery("SELECT COUNT(*) FROM specspawns WHERE arena = '"+arena+"';");
		try {
			if(rs != null) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public int getLobbyspawnsSize() {
		ResultSet rs = sql.resultQuery("SELECT COUNT(*) FROM lobbypawns;");
		try {
			if(rs != null) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public ArrayList<Location> getRedspawns(String arena) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		ResultSet rs = sql.resultQuery("SELECT location_id FROM redspawns WHERE arena = '"+arena+"';");
		try {
			if(rs != null) {
				while(rs.next()) {
					ids.add(rs.getInt(1));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return getLocations(ids);
	}

	public ArrayList<Location> getBluespawns(String arena) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		ResultSet rs = sql.resultQuery("SELECT location_id FROM redspawns WHERE arena = '"+arena+"';");
		try {
			if(rs != null) {
				while(rs.next()) {
					ids.add(rs.getInt(1));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return getLocations(ids);
	}

	public ArrayList<Location> getSpecspawns(String arena) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		ResultSet rs = sql.resultQuery("SELECT location_id FROM redspawns WHERE arena = '"+arena+"';");
		try {
			if(rs != null) {
				while(rs.next()) {
					ids.add(rs.getInt(1));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return getLocations(ids);
	}
	
	public ArrayList<Location> getLobbyspawns() {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		ResultSet rs = sql.resultQuery("SELECT location_id FROM lobbyspawns;");
		try {
			if(rs != null) {
				while(rs.next()) {
					ids.add(rs.getInt(1));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return getLocations(ids);
	}

	//SET
	public void addArenaStats(String arena, HashMap<String, Integer> stats) {
		String query = "";
		for(Entry<String, Integer> entry : stats.entrySet()) {
			String key = entry.getKey();
			if(statsList.contains(key)) {
				query += key+"="+key+"+"+entry.getValue()+",";
			}
		}
		if(query.length() > 0) {
			query = query.substring(0, query.length()-1);
			sql.updateQuery("UPDATE OR IGNORE arenastats SET "+query+" WHERE name='"+arena+"';");
		}
	}
	
	public void setArenaStats(String arena, HashMap<String, Integer> stats) {
		String query = "";
		for(Entry<String, Integer> entry : stats.entrySet()) {
			String key = entry.getKey();
			if(statsList.contains(key)) {
				query += key+"="+entry.getValue()+",";
			}
		}
		if(query.length() > 0) {
			query = query.substring(0, query.length()-1);
			sql.updateQuery("UPDATE OR IGNORE arenastats SET "+query+" WHERE name='"+arena+"';");
		}
	}
	
	public void setArenaSettings(String arena, HashMap<String, Integer> settings) {
		String query = "";
		for(Entry<String, Integer> entry : settings.entrySet()) {
			String key = entry.getKey();
			if(settingsList.contains(key)) {
				query += key+"="+entry.getValue()+",";
			}
		}
		if(query.length() > 0) {
			query = query.substring(0, query.length()-1);
			sql.updateQuery("UPDATE OR IGNORE arenas SET "+query+" WHERE name='"+arena+"';");
		}
	}
	//REMOVE
	public void removeArena(String arena) {
		sql.updateQuery("DELETE FROM arenas WHERE name='"+arena+"';");
		sql.updateQuery("DELETE FROM arenastats WHERE name='"+arena+"';");
		removeRedspawns(arena);
		removeBluespawns(arena);
		removeSpecspawns(arena);
	}

	public void removeRedspawns(String arena) {
		sql.updateQuery("DELETE FROM redspawns WHERE arena='"+arena+"';");
	}

	public void removeBluespawns(String arena) {
		sql.updateQuery("DELETE FROM bluespawns WHERE arena='"+arena+"';");
	}

	public void removeSpecspawns(String arena) {
		sql.updateQuery("DELETE FROM specspawns WHERE arena='"+arena+"';");
	}
	
	public void removeLobbyspawns() {
		sql.updateQuery("DELETE FROM lobbyspawns;");
	}

	//ADD NEW
	public int addLocation(Location loc) {
		return sql.updateQuery("INSERT OR IGNORE INTO locations(world, x, y, z, yaw, pitch) VALUES('"+loc.getWorld().getName()+"','"+loc.getX()+"','"+loc.getY()+"','"+loc.getZ()+"','"+loc.getYaw()+"','"+loc.getPitch()+"');");
	}

	public void addNewArena(String arena) {
		//ARENASETTINGS
		String settingsQuery = "";
		String settingsValues = "";
		for(String s : settingsList) {
			settingsQuery += ","+s;
			settingsValues += ",0"; 
		}
		sql.updateQuery("INSERT OR IGNORE INTO arenas(name,"+settingsQuery+") VALUES('"+arena+"'"+settingsValues+");");
		
		//ARENASTATS
		String statsQuery = "";
		String statsValues = "";
		for(String s : statsList) {
			statsQuery += ","+s;
			statsValues += ",0"; 
		}
		sql.updateQuery("INSERT OR IGNORE INTO arenastats(name,"+statsQuery+") VALUES('"+arena+"'"+statsValues+");");
	}

	public void addLobbyspawn(Location loc) {
		int row = this.addLocation(loc);
		sql.updateQuery("INSERT OR IGNORE INTO lobbyspawns(location_id) VALUES('"+row+"');");
	}

	public void addRedspawn(Location loc, String arena) {
		int row = addLocation(loc);
		sql.updateQuery("INSERT OR IGNORE INTO redspawns(arena, location_id) VALUES('"+arena+"','"+row+"');");
	}

	public void addBluespawn(Location loc, String arena) {
		int row = addLocation(loc);
		sql.updateQuery("INSERT OR IGNORE INTO bluespawns(arena, location_id) VALUES('"+arena+"','"+row+"');");
	}

	public void addSpecspawn(Location loc, String arena) {
		int row = addLocation(loc);
		sql.updateQuery("INSERT OR IGNORE INTO specspawns(arena, location_id) VALUES('"+arena+"','"+row+"');");
	}

}
