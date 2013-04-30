package me.blablubbabc.BlaDB;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map.Entry;

import me.blablubbabc.paintball.Paintball;

public class BlaSQLite {

	private File databaseFile;
	private Connection connection;
	private static Paintball plugin;

	public SQLArenaLobby sqlArenaLobby;
	public SQLPlayers sqlPlayers;
	public SQLGeneralStats sqlGeneralStats;

	public BlaSQLite(File databaseFile, Paintball pl) {
		this.databaseFile = databaseFile;
		plugin = pl;

		sqlArenaLobby = new SQLArenaLobby(this, plugin);
		sqlPlayers = new SQLPlayers(this, plugin);
		sqlGeneralStats = new SQLGeneralStats(this, plugin);

		//CREATE TABLES
		sqlArenaLobby.createDefaultTables();
		sqlPlayers.createDefaultTables();
		sqlGeneralStats.createDefaultTables();
	}

	public void closeConnection() {
		try {
			connection.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean isConnected() {
		try {
			return connection!=null && !connection.isClosed();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void refreshConnection() {
		if (connection == null) {
			initialise();
			pragmas();
		}
	}
	public void pragmas() {
		//nothing here :(
	}

	public boolean initialise() {
		if (!databaseFile.exists()) {
			try {
				databaseFile.getParentFile().mkdirs();
				databaseFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + this.databaseFile.getAbsolutePath());
			return true;
		} catch (SQLException ex) {
			log("ERROR: SQL Exception!");
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			log("ERROR: Could not find SQLite driver class!");
			ex.printStackTrace();
		}
		return false;
	}

	public void log(String msg) {
		System.out.println("[BlaSQLite] "+msg);
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
	
	public synchronized boolean getAutoCommit() {
		boolean r = true;
		try {
			r = this.connection.getAutoCommit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return r;
	}
	
	public synchronized void setAutoCommit(boolean b) {
		try {
			this.connection.setAutoCommit(b);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void commit() {
		try {
			this.connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public synchronized Result resultQuery(String query) {
		this.refreshConnection();
		try
		{
			Statement statement = this.connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			
			return new Result(statement, result);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/////////////////////////////
	//PAINTBALL SPEZIFISCHER TEIL

	public void createDefaultTable(String name, String query, String indexOn) {
		this.updateQuery("CREATE TABLE IF NOT EXISTS " + name + "(" + query + ");");
		if (indexOn != null)
			this.updateQuery("CREATE UNIQUE INDEX IF NOT EXISTS "+name+"_"+indexOn+" ON " + name + "(" + indexOn + ");");
	}

	public void createDefaultTable(String name, HashMap<String, String> content, String indexOn) {
		String query = "";
		for (Entry<String, String> entry : content.entrySet()) {
			query += entry.getKey() + " " + entry.getValue().toUpperCase() + ", ";
		}
		if (query.length() > 2) {
			query = query.substring(0, query.length() - 2);
			createDefaultTable(name, query, indexOn);
		}
	}

}
