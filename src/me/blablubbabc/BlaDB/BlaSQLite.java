package me.blablubbabc.BlaDB;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import me.blablubbabc.paintball.Paintball;
import org.bukkit.Location;
import org.bukkit.World;

public class BlaSQLite {

	private File databaseFile;
	private Connection connection;
	private static Paintball plugin;

	public BlaSQLite(File databaseFile, Paintball pl) {
		this.databaseFile = databaseFile;
		this.createDefaultDatabase();
		plugin = pl;
	}

	public void refreshConnection() {
		if (connection == null) {
			initialise();
		}
	}

	public boolean initialise() {
		if (!databaseFile.exists()) {
			try {
				databaseFile.createNewFile();
				//databaseFile.getParentFile().mkdir();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + this.databaseFile.getAbsolutePath());
			return true;
		} catch (SQLException ex) {
			log("SQL Exception!");
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			log("Could not find SQLite driver class!");
			ex.printStackTrace();
		}
		return false;
	}

	public void log(String msg) {
		System.out.println("[BlaSQLite ERROR] "+msg);
	}

	public synchronized int updateQuery(String query) {
		this.refreshConnection();
		int row = -1;
		try
		{
			Statement statement = this.connection.createStatement();
			statement.executeUpdate(query);
			row = statement.getGeneratedKeys().getInt(1);
			statement.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return row;
	}

	public synchronized ResultSet resultQuery(String query) {
		this.refreshConnection();
		try
		{
			Statement statement = this.connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			return result;
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/////////////////////////////
	//PAINTBALL SPEZIFISCHER TEIL

	public void createDefaultDatabase() {
		///////////////
		//version 1081:
		//PLAYERDATA
		this.updateQuery("CREATE TABLE IF NOT EXISTS players(name TEXT, points INTEGER, shots INTEGER, hits INTEGER, teamattacks INTEGER, kills INTEGER, deaths INTEGER, wins INTEGER, looses INTEGER, money INTEGER);");
		//ARENADATA
		this.updateQuery("CREATE TABLE IF NOT EXISTS arenas(name TEXT, rounds INTEGER, kills INTEGER, shots INTEGER, size INTEGER);");
		this.updateQuery("CREATE TABLE IF NOT EXISTS locations(id INTEGER PRIMARY KEY, world TEXT, x INTEGER, y INTEGER, z INTEGER, yaw REAL, pitch REAL);");
		this.updateQuery("CREATE TABLE IF NOT EXISTS bluespawns(arena TEXT, location_id INTEGER);");
		this.updateQuery("CREATE TABLE IF NOT EXISTS redspawns(arena TEXT, location_id INTEGER);");
		this.updateQuery("CREATE TABLE IF NOT EXISTS specspawns(arena TEXT, location_id INTEGER);");
		//LOBBYSPAWNS
		this.updateQuery("CREATE TABLE IF NOT EXISTS lobbyspawns(location_id INTEGER);");
		//GENERAL STATS
		this.updateQuery("CREATE TABLE IF NOT EXISTS stats(rounds INTEGER, kills INTEGER, shots INTEGER, money INTEGER);");
	}

	//METHODS
	//ARENADATA and LOBBYSPAWNS
	//GET
	public HashMap<String, Integer> getArenaStats(String arena) {
		HashMap<String, Integer> data = new HashMap<String, Integer>();
		ResultSet rs = this.resultQuery("SELECT * FROM arenas WHERE name = '"+arena+"';");
		try {
			if(rs != null && rs.first()) {
				ResultSetMetaData rsmd = rs.getMetaData();
				int columns = rsmd.getColumnCount();
				for(int i = 2; i <= columns; i++) {
					data.put(rs.getMetaData().getColumnName(i), rs.getInt(i));
				}
				return data;
			} else return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public ArrayList<Location> getLocations(ArrayList<Integer> ids) {
		ArrayList<Location> locs = new ArrayList<Location>();
		if(ids.size() > 0) {
			String idss = "";
			for(int id : ids) {
				idss += "id = "+id+" OR ";
			}
			idss = idss.substring(0, idss.length()-4);
			ResultSet rs = this.resultQuery("SELECT * FROM locations WHERE "+idss+";");
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
					return null;
				}
			}
		}
		return locs;
	}
	
	public ArrayList<Location> getRedspawns(String arena) {
		ResultSet rs = this.resultQuery("SELECT location_id FROM redspawns WHERE arena = '"+arena+"';");
		try {
			if(rs != null) {
				ArrayList<Integer> ids = new ArrayList<Integer>();
				while(rs.next()) {
					ids.add(rs.getInt(1));
				}
				return getLocations(ids);
			} else return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public ArrayList<Location> getBluespawns(String arena) {
		ResultSet rs = this.resultQuery("SELECT location_id FROM redspawns WHERE arena = '"+arena+"';");
		try {
			if(rs != null) {
				ArrayList<Integer> ids = new ArrayList<Integer>();
				while(rs.next()) {
					ids.add(rs.getInt(1));
				}
				return getLocations(ids);
			} else return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public ArrayList<Location> getSpecspawns(String arena) {
		ResultSet rs = this.resultQuery("SELECT location_id FROM redspawns WHERE arena = '"+arena+"';");
		try {
			if(rs != null) {
				ArrayList<Integer> ids = new ArrayList<Integer>();
				while(rs.next()) {
					ids.add(rs.getInt(1));
				}
				return getLocations(ids);
			} else return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//SET
	//REMOVE
	//ADD NEW
	public int addLocation(Location loc) {
		return this.updateQuery("INSERT INTO locations(world, x, y, z, yaw, pitch) VALUES('"+loc.getWorld().getName()+"','"+loc.getX()+"','"+loc.getY()+"','"+loc.getZ()+"','"+loc.getYaw()+"','"+loc.getPitch()+"');");
	}
	
	public void addNewArena(String arena) {
		this.updateQuery("INSERT INTO arenas(name,rounds,kills,shots,size) VALUES('arena',0,0,0,0);");
	}
	
	public void addLobbyspawn(Location loc) {
		int row = this.addLocation(loc);
		this.updateQuery("INSERT INTO lobbyspawns(location_id) VALUES('"+row+"');");
	}

	public void addRedspawn(Location loc, String arena) {
		int row = this.addLocation(loc);
		this.updateQuery("INSERT INTO redspawns(arena, location_id) VALUES('"+arena+"','"+row+"');");
	}

	public void addBluespawn(Location loc, String arena) {
		int row = this.addLocation(loc);
		this.updateQuery("INSERT INTO bluespawns(arena, location_id) VALUES('"+arena+"','"+row+"');");
	}
	
	public void addSpecspawn(Location loc, String arena) {
		int row = this.addLocation(loc);
		this.updateQuery("INSERT INTO specspawns(arena, location_id) VALUES('"+arena+"','"+row+"');");
	}
}
