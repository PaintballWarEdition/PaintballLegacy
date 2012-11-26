package me.blablubbabc.BlaDB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import me.blablubbabc.paintball.Paintball;
import org.bukkit.Location;
import org.bukkit.World;

public class SQLArenaLobby {

	private static BlaSQLite sql;
	private static Paintball plugin;
	
	public LinkedList<String> statsList;
	public LinkedList<String> settingsList;

	public SQLArenaLobby(BlaSQLite blasql, Paintball pl) {
		sql = blasql;
		plugin = pl;
		
		statsList = new LinkedList<String>();
		statsList.add("rounds"); statsList.add("kills"); statsList.add("shots"); statsList.add("grenades"); statsList.add("airstrikes");
		
		settingsList = new LinkedList<String>();
		settingsList.add("lives"); settingsList.add("respawns"); settingsList.add("round_time"); settingsList.add("balls"); settingsList.add("grenades"); settingsList.add("airstrikes");
	}
	
	public void createDefaultTables() {
		//arenas
		String arenasQuery = "name TEXT, active INTEGER";
		sql.createDefaultTable("arenas", arenasQuery, "name");
		//arenasettings
		HashMap<String, String> arenasettings = new HashMap<String, String>();
		arenasettings.put("name", "TEXT");
		for(String s : settingsList) {
			arenasettings.put(s, "INTEGER");
		}
		sql.createDefaultTable("arenasettings", arenasettings, "name");
		
		//arenastats
		HashMap<String, String> arenastats = new HashMap<String, String>();
		arenastats.put("name", "TEXT");
		for(String s : statsList) {
			arenastats.put(s, "INTEGER");
		}
		sql.createDefaultTable("arenastats", arenastats, "name");
		
		//locations
		String locationsQuery = "id INTEGER PRIMARY KEY, world TEXT, x INTEGER, y INTEGER, z INTEGER, yaw REAL, pitch REAL";
		sql.createDefaultTable("locations", locationsQuery, null);
		
		//red/blue/spec/lobby spawns
		String spawnsQuery = "arena TEXT, location_id INTEGER";
		sql.createDefaultTable("redspawns", spawnsQuery, null);
		sql.createDefaultTable("bluespawns", spawnsQuery, null);
		sql.createDefaultTable("specspawns", spawnsQuery, null);
		
		String lobbyQuery = "location_id INTEGER";
		sql.createDefaultTable("lobbyspawns", lobbyQuery, null);
		
		//INIT RESET
		for(String arena : getAllArenaNames()) {
			sql.updateQuery("UPDATE OR IGNORE arenas SET active=0 WHERE name='"+arena+"';");
		}	
	}
	
	

	//ARENADATA and LOBBYSPAWNS
	//GET
	public ArrayList<String> getAllArenaNames() {
		ArrayList<String> arenas = new ArrayList<String>();

		Result r = sql.resultQuery("SELECT name FROM arenas;");
		ResultSet rs = r.getResultSet();
		try {
			if(rs != null) {
				while(rs.next()) {
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

	public LinkedHashMap<String, Integer> getArenaStats(String arena) {
		LinkedHashMap<String, Integer> data = new LinkedHashMap<String, Integer>();
		Result r = sql.resultQuery("SELECT * FROM arenastats WHERE name='"+arena+"' LIMIT 1;");
		ResultSet rs = r.getResultSet();
		try {
			if(rs != null && rs.next()) {
				for(String s : statsList) {
					data.put(s, rs.getInt(s));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			r.close();
		}
		return data;
	}
	
	public LinkedHashMap<String, Integer> getArenaSettings(String arena) {
		LinkedHashMap<String, Integer> data = new LinkedHashMap<String, Integer>();
		Result r = sql.resultQuery("SELECT * FROM arenasettings WHERE name='"+arena+"' LIMIT 1;");
		ResultSet rs = r.getResultSet();
		try {
			if(rs != null && rs.next()) {
				for(String s : settingsList) {
					data.put(s, rs.getInt(s));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			r.close();
		}
		return data;
	}

	private ArrayList<Location> getLocations(ArrayList<Integer> ids) {
		ArrayList<Location> locs = new ArrayList<Location>();
		if(ids.size() > 0) {
			String idss = "";
			for(int id : ids) {
				idss += "id='"+id+"' OR ";
			}
			idss = idss.substring(0, idss.length()-4);
			Result r = sql.resultQuery("SELECT * FROM locations WHERE "+idss+";");
			ResultSet rs = r.getResultSet();
			try {
				if(rs != null) {
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
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				r.close();
			}
		}
		return locs;
	}
	
	private void removeLocations(ArrayList<Integer> ids) {
		if(ids.size() > 0) {
			String idss = "";
			for(int id : ids) {
				idss += "id='"+id+"' OR ";
			}
			idss = idss.substring(0, idss.length()-4);
			sql.updateQuery("DELETE FROM locations WHERE "+idss+";");
		}
	}

	public boolean isArenaExisting(String arena) {
		Result r = sql.resultQuery("SELECT EXISTS(SELECT 1 FROM arenas WHERE name='"+arena+"' LIMIT 1);");
		ResultSet rs = r.getResultSet();
		boolean b = false;
		try {
			if(rs != null) {
				b = (rs.getInt(1) == 1 ? true : false);
			}
		} catch (SQLException e) {
			e.printStackTrace();

		} finally {
			r.close();
		}
		return b;

	}
	
	public int getRedspawnsSize(String arena) {
		Result r = sql.resultQuery("SELECT COUNT(*) FROM redspawns WHERE arena = '"+arena+"';");
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

	public int getBluespawnsSize(String arena) {
		Result r = sql.resultQuery("SELECT COUNT(*) FROM bluespawns WHERE arena = '"+arena+"';");
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

	public int getSpecspawnsSize(String arena) {
		Result r = sql.resultQuery("SELECT COUNT(*) FROM specspawns WHERE arena = '"+arena+"';");
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
	
	public int getLobbyspawnsSize() {
		Result r = sql.resultQuery("SELECT COUNT(*) FROM lobbypawns;");
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

	private ArrayList<Integer> getRedspawnsIds(String arena) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		Result r = sql.resultQuery("SELECT location_id FROM redspawns WHERE arena = '"+arena+"';");
		ResultSet rs = r.getResultSet();
		try {
			if(rs != null) {
				while(rs.next()) {
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
	private ArrayList<Integer> getBluespawnsIds(String arena) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		Result r = sql.resultQuery("SELECT location_id FROM bluespawns WHERE arena = '"+arena+"';");
		ResultSet rs = r.getResultSet();
		try {
			if(rs != null) {
				while(rs.next()) {
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
	private ArrayList<Integer> getSpecspawnsIds(String arena) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		Result r = sql.resultQuery("SELECT location_id FROM specspawns WHERE arena = '"+arena+"';");
		ResultSet rs = r.getResultSet();
		try {
			if(rs != null) {
				while(rs.next()) {
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
	private ArrayList<Integer> getLobbyspawnsIds() {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		Result r = sql.resultQuery("SELECT location_id FROM lobbyspawns;");
		ResultSet rs = r.getResultSet();
		try {
			if(rs != null) {
				while(rs.next()) {
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
	
	public ArrayList<Location> getRedspawns(String arena) {
		return getLocations(getRedspawnsIds(arena));
	}

	public ArrayList<Location> getBluespawns(String arena) {
		return getLocations(getBluespawnsIds(arena));
	}

	public ArrayList<Location> getSpecspawns(String arena) {
		return getLocations(getSpecspawnsIds(arena));
	}
	
	public ArrayList<Location> getLobbyspawns() {
		return getLocations(getLobbyspawnsIds());
	}

	public boolean isArenaActive(String arena) {
		Result r = sql.resultQuery("SELECT active FROM arenas WHERE name='"+arena+"' LIMIT 1;");
		ResultSet rs = r.getResultSet();
		boolean active = false;
		try {
			if(rs != null && rs.next()) {
				if(rs.getInt(1)==1) active = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			r.close();
		}
		return active;
	}
	
	//SET
	public void setArenaActive(String arena) {
		sql.updateQuery("UPDATE OR IGNORE arenas SET active=1 WHERE name='"+arena+"';");
	}
	public void setArenaNotActive(String arena) {
		sql.updateQuery("UPDATE OR IGNORE arenas SET active=0 WHERE name='"+arena+"';");
	}
	
	public void addArenaStats(String arena, HashMap<String, Integer> stats) {
		String query = "";
		for(Entry<String, Integer> entry : stats.entrySet()) {
			String key = entry.getKey();
			if(statsList.contains(key)) {
				query += key+"="+key+"+'"+entry.getValue()+"',";
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
				query += key+"='"+entry.getValue()+"',";
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
				query += key+"='"+entry.getValue()+"',";
			}
		}
		if(query.length() > 0) {
			query = query.substring(0, query.length()-1);
			sql.updateQuery("UPDATE OR IGNORE arenasettings SET "+query+" WHERE name='"+arena+"';");
		}
	}
	//REMOVE
	public void removeArena(String arena) {
		sql.updateQuery("DELETE FROM arenas WHERE name='"+arena+"';");
		sql.updateQuery("DELETE FROM arenasettings WHERE name='"+arena+"';");
		sql.updateQuery("DELETE FROM arenastats WHERE name='"+arena+"';");
		removeRedspawns(arena);
		removeBluespawns(arena);
		removeSpecspawns(arena);
	}

	public void removeRedspawns(String arena) {
		removeLocations(getRedspawnsIds(arena));
		sql.updateQuery("DELETE FROM redspawns WHERE arena='"+arena+"';");
	}

	public void removeBluespawns(String arena) {
		removeLocations(getBluespawnsIds(arena));
		sql.updateQuery("DELETE FROM bluespawns WHERE arena='"+arena+"';");
	}

	public void removeSpecspawns(String arena) {
		removeLocations(getSpecspawnsIds(arena));
		sql.updateQuery("DELETE FROM specspawns WHERE arena='"+arena+"';");
	}
	
	public void removeLobbyspawns() {
		removeLocations(getLobbyspawnsIds());
		sql.updateQuery("DELETE FROM lobbyspawns;");
	}

	//ADD NEW
	public int addLocation(Location loc) {
		return sql.updateQuery("INSERT OR IGNORE INTO locations(world, x, y, z, yaw, pitch) VALUES('"+loc.getWorld().getName()+"','"+loc.getX()+"','"+loc.getY()+"','"+loc.getZ()+"','"+loc.getYaw()+"','"+loc.getPitch()+"');");
	}

	public void addNewArena(String arena) {
		//ARENAS name TEXT, active INTEGER
		sql.updateQuery("INSERT OR IGNORE INTO arenas(name, active) VALUES('"+arena+"','0');");
		//ARENASETTINGS
		String settingsQuery = "";
		String settingsValues = "";
		for(String s : settingsList) {
			settingsQuery += ","+s;
			settingsValues += ",'0'"; 
		}
		sql.updateQuery("INSERT OR IGNORE INTO arenasettings(name"+settingsQuery+") VALUES('"+arena+"'"+settingsValues+");");
		
		//ARENASTATS
		String statsQuery = "";
		String statsValues = "";
		for(String s : statsList) {
			statsQuery += ","+s;
			statsValues += ",'0'"; 
		}
		sql.updateQuery("INSERT OR IGNORE INTO arenastats(name"+statsQuery+") VALUES('"+arena+"'"+statsValues+");");
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
