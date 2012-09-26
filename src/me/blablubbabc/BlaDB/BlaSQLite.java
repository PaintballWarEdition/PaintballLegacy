package me.blablubbabc.BlaDB;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import me.blablubbabc.paintball.Paintball;

public class BlaSQLite {

	private File databaseFile;
	private Connection connection;
	private static Paintball plugin;
	//ARENA + LOBBY SQL STUFF
	public SQLArenaLobby sqlArenaLobby;

	public BlaSQLite(File databaseFile, Paintball pl) {
		this.databaseFile = databaseFile;
		this.createDefaultDatabase();
		plugin = pl;
		sqlArenaLobby = new SQLArenaLobby(this, plugin);
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
		this.updateQuery("CREATE UNIQUE INDEX ON players(name);");
		//ARENADATA
		this.updateQuery("CREATE TABLE IF NOT EXISTS arenas(name TEXT, rounds INTEGER, kills INTEGER, shots INTEGER, size INTEGER);");
		this.updateQuery("CREATE UNIQUE INDEX ON arenas(name);");
		this.updateQuery("CREATE TABLE IF NOT EXISTS locations(id INTEGER PRIMARY KEY, world TEXT, x INTEGER, y INTEGER, z INTEGER, yaw REAL, pitch REAL);");
		this.updateQuery("CREATE TABLE IF NOT EXISTS bluespawns(arena TEXT, location_id INTEGER);");
		this.updateQuery("CREATE TABLE IF NOT EXISTS redspawns(arena TEXT, location_id INTEGER);");
		this.updateQuery("CREATE TABLE IF NOT EXISTS specspawns(arena TEXT, location_id INTEGER);");
		//LOBBYSPAWNS
		this.updateQuery("CREATE TABLE IF NOT EXISTS lobbyspawns(location_id INTEGER);");
		//GENERAL STATS
		this.updateQuery("CREATE TABLE IF NOT EXISTS stats(rounds INTEGER, kills INTEGER, shots INTEGER, money INTEGER);");
	}
	
}
