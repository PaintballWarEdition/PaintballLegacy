package me.blablubbabc.BlaDB;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import me.blablubbabc.paintball.Paintball;

import org.bukkit.Location;
import org.bukkit.World;

public class SQLArenaLobby {

	private static BlaSQLite sql;
	private static Paintball plugin;

	public SQLArenaLobby(BlaSQLite blasql, Paintball pl) {
		sql = blasql;
		plugin = pl;
	}

	//ARENADATA and LOBBYSPAWNS
	//GET
	public String[] getAllArenaNames() {
		String[] arenas = new String[]{};

		ResultSet rs = sql.resultQuery("SELECT name FROM arenas;");
		try {
			if(rs != null) {
				int index = 0;
				while(rs.next()) {
					arenas[index] = rs.getString(1);
					index++;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return arenas;
	}

	public HashMap<String, Integer> getArenaStats(String arena) {
		HashMap<String, Integer> data = new HashMap<String, Integer>();
		ResultSet rs = sql.resultQuery("SELECT * FROM arenas WHERE name = '"+arena+"';");
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

	//SET
	public void addArenaStats(String arena, int rounds, int kills, int shots) {
		sql.updateQuery("UPDATE OR IGNORE arenas SET rounds=rounds+"+rounds+",kills=kills+"+kills+",shots=shots+"+shots+" WHERE name='"+arena+"';");
	}
	
	public void setArenaStats(String arena, int rounds, int kills, int shots) {
		sql.updateQuery("UPDATE OR IGNORE arenas SET rounds="+rounds+",kills="+kills+",shots="+shots+" WHERE name='"+arena+"';");
	}
	
	public void setArenaSize(String arena, int size) {
		sql.updateQuery("UPDATE OR IGNORE arenas SET size="+size+" WHERE name='"+arena+"';");
	}
	//REMOVE
	public void removeArena(String arena) {
		sql.updateQuery("DELETE FROM arenas WHERE name='"+arena+"';");
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

	//ADD NEW
	public int addLocation(Location loc) {
		return sql.updateQuery("INSERT OR IGNORE INTO locations(world, x, y, z, yaw, pitch) VALUES('"+loc.getWorld().getName()+"','"+loc.getX()+"','"+loc.getY()+"','"+loc.getZ()+"','"+loc.getYaw()+"','"+loc.getPitch()+"');");
	}

	public void addNewArena(String arena) {
		sql.updateQuery("INSERT OR IGNORE INTO arenas(name,rounds,kills,shots,size) VALUES('arena',0,0,0,0);");
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
