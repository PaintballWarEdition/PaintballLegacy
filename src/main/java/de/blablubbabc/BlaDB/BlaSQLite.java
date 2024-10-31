/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.BlaDB;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.plugin.Plugin;

import de.blablubbabc.paintball.utils.Log;

public class BlaSQLite {

	public boolean aborted = false;

	private File databaseFile;
	private Connection connection;

	public SQLArenaLobby sqlArenaLobby;
	public SQLPlayers sqlPlayers;
	public SQLGeneralStats sqlGeneralStats;

	public BlaSQLite(Plugin plugin) {
		// Version 1.3.0:
		this.databaseFile = new File(plugin.getDataFolder(), "pbdata_130" + ".db");

		// This creates the database file:
		sqlArenaLobby = new SQLArenaLobby(this);
		sqlPlayers = new SQLPlayers(this);
		sqlGeneralStats = new SQLGeneralStats(this);
	}

	public void closeConnection() {
		try {
			if (isConnected()) connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isConnected() {
		try {
			return connection != null && !connection.isClosed();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void refreshConnection() {
		if (!isConnected()) {
			initialise();
			pragmas();
		}
	}

	public void pragmas() {
		// Nothing currently
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
			Log.severe("ERROR: SQL Exception!", true);
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			Log.severe("ERROR: Could not find SQLite driver class!", true);
			ex.printStackTrace();
		}
		return false;
	}

	/*
	 * public Connection getConnection() {
	 * this.refreshConnection();
	 * return connection;
	 * }
	 */

	public synchronized int updateQueryRaw(String query) throws SQLException {
		this.refreshConnection();
		Statement statement = this.connection.createStatement();
		statement.executeUpdate(query);
		int row = statement.getGeneratedKeys().getInt(1);
		statement.close();
		return row;
	}

	public synchronized int updateQuery(String query) {
		try {
			return this.updateQueryRaw(query);
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public synchronized boolean getAutoCommit() {
		this.refreshConnection();
		boolean r = true;
		try {
			r = this.connection.getAutoCommit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return r;
	}

	public synchronized void setAutoCommit(boolean b) {
		this.refreshConnection();
		try {
			this.connection.setAutoCommit(b);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public synchronized void commit() {
		this.refreshConnection();
		try {
			this.connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public synchronized Result resultQuery(String query) {
		this.refreshConnection();
		try {
			Statement statement = this.connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			return new Result(statement, result);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	// ///////////////////////////
	// PAINTBALL SPECIFIC

	public void createDefaultTable(String name, String query, String indexOn) {
		this.updateQuery("CREATE TABLE IF NOT EXISTS " + name + "(" + query + ");");
		if (indexOn != null) {
			this.updateQuery("CREATE UNIQUE INDEX IF NOT EXISTS " + name + "_" + indexOn + " ON " + name + "(" + indexOn + ");");
		}
	}

	public void createDefaultTable(String name, HashMap<String, String> content, String indexOn) {
		String query = "";
		for (Entry<String, String> entry : content.entrySet()) {
			query += entry.getKey() + " " + entry.getValue().toUpperCase() + ", ";
		}
		if (query.length() > 2) {
			query = query.substring(0, query.length() - 2);
			this.createDefaultTable(name, query, indexOn);
		}
	}
}
