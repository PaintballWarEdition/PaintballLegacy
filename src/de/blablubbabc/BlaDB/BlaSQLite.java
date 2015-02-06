package de.blablubbabc.BlaDB;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.statistics.player.PlayerStat;
import de.blablubbabc.paintball.utils.Log;
import de.blablubbabc.paintball.utils.UUIDFetcher;

public class BlaSQLite {

	public boolean aborted = false;

	private File databaseFile;
	private Connection connection;

	public SQLArenaLobby sqlArenaLobby;
	public SQLPlayers sqlPlayers;
	public SQLGeneralStats sqlGeneralStats;

	public BlaSQLite(Plugin plugin) {
		// version 1.3.0:
		this.databaseFile = new File(plugin.getDataFolder(), "pbdata_130" + ".db");

		// pre checks:
		File oldDBFile = new File(plugin.getDataFolder(), "pbdata_110" + ".db");
		if (oldDBFile.exists()) {
			if (Paintball.instance.uuidFirstRun) {
				Log.warning("Detected first run with old database file: new configuration options were generated.", true);
				Log.info("Stop the server, open the Paintball config, and make sure the newly added 'UUID Conversion' settings are correctly set.", true);
				Log.info("The next time you restart the server, the old paintball data will get imported into a new database file.", true);
				Log.info("This process will take some while, in which the server will be unresponsive. You will get status reports in the console.", true);

				// in case the plugin was loaded while the server is running:
				for (Player admin : Bukkit.getOnlinePlayers()) {
					if (admin.hasPermission("paintball.admin")) {
						admin.sendMessage(ChatColor.RED + "Important! The next reload of Paintball will start a very slow uuid conversion and data import process.");
						admin.sendMessage(ChatColor.RED + "Please view the server log now for more information.");
					}
				}

				aborted = true;
				return;
			}

			if (databaseFile.exists()) {
				Log.severe("Cannot merge data from old database file ('pbdata_110.db') into already existing new database file ('pbdata_130.db').", true);
				Log.severe("To properly import old data: Stop the server, delete the 'pbdata_130.db' file and then restart the server.", true);

				aborted = true;
				return;
			}
		}

		// this will create the database file:
		sqlArenaLobby = new SQLArenaLobby(this);
		sqlPlayers = new SQLPlayers(this);
		sqlGeneralStats = new SQLGeneralStats(this);

		// import data from old db (version 1.1.0):
		if (oldDBFile.exists()) {
			Log.info("Importing data from old database file 'pbdata_110.db'. This may take a while. Do not abort.");
			// attach old db:
			this.updateQuery("ATTACH '" + oldDBFile.getAbsolutePath() + "' AS oldDB;");

			Log.info("Importing lobby and arenas ...");
			this.updateQuery("INSERT OR IGNORE INTO arenas SELECT * FROM oldDB.arenas;");
			this.updateQuery("INSERT OR IGNORE INTO arenasettings SELECT * FROM oldDB.arenasettings;");
			this.updateQuery("INSERT OR IGNORE INTO arenastats SELECT * FROM oldDB.arenastats;");
			this.updateQuery("INSERT OR IGNORE INTO locations SELECT * FROM oldDB.locations;");
			this.updateQuery("INSERT OR IGNORE INTO redspawns SELECT * FROM oldDB.redspawns;");
			this.updateQuery("INSERT OR IGNORE INTO bluespawns SELECT * FROM oldDB.bluespawns;");
			this.updateQuery("INSERT OR IGNORE INTO specspawns SELECT * FROM oldDB.specspawns;");
			this.updateQuery("INSERT OR IGNORE INTO lobbyspawns SELECT * FROM oldDB.lobbyspawns;");

			Log.info("Importing general statistics ...");
			this.updateQuery("INSERT OR REPLACE INTO general_stats SELECT * FROM oldDB.general_stats;");

			Log.info("Loading player names ...");
			List<String> playerNames = new ArrayList<String>();
			Result r = this.resultQuery("SELECT name FROM oldDB.players;");
			ResultSet rs = r.getResultSet();
			try {
				if (rs != null) {
					while (rs.next()) {
						playerNames.add(rs.getString(1));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				r.close();
			}

			Log.info("Fetching player uuids...");
			Map<String, UUID> uuids;
			try {
				uuids = new UUIDFetcher(playerNames).call();
			} catch (Exception e) {
				e.printStackTrace();
				aborted = true;
				return;
			}

			Log.info("Importing player statistics ...");
			StringBuilder oldColBuilder = new StringBuilder("name,");
			for (String key : PlayerStat.getKeys()) {
				oldColBuilder.append(key).append(',');
			}
			String oldPlayerColumns = oldColBuilder.substring(0, oldColBuilder.length() - 1);
			String newPlayerColumns = "uuid," + oldPlayerColumns;

			int counter = 0;
			List<String> unconverted = new ArrayList<String>();
			for (String playerName : playerNames) {
				UUID uuid = uuids.get(playerName);
				if (uuid == null) {
					unconverted.add(playerName);
					continue;
				}
				// import player statistics:
				this.updateQuery("INSERT OR IGNORE INTO players (" + newPlayerColumns + ") SELECT \""
						+ uuid.toString() + "\"," + oldPlayerColumns + " FROM oldDB.players WHERE name='" + playerName + "';");

				// giving feedback about the progress, and flushing data:
				counter++;
				if ((counter % 250) == 0) {
					Log.info("Progress: " + counter);
				}
			}

			// detach old db:
			this.updateQuery("DETACH oldDB;");

			// rename old db file:
			final File backupDBFile = new File(plugin.getDataFolder(), "pbdata_110-backup" + ".db");
			oldDBFile.renameTo(backupDBFile);

			// informing about unconverted / not imported data:
			File unconvertedConfigFile = new File(plugin.getDataFolder(), "unconvertedPlayers.yml");
			if (unconvertedConfigFile.exists()) {
				Log.warning("Removing old '" + unconvertedConfigFile.getName() + "' file.");
				unconvertedConfigFile.delete();
			}

			if (!unconverted.isEmpty()) {
				Log.warning("Some player statistics couldn't be imported, because we didn't find uuid's for them.", true);
				Log.warning("Their names were saved to the file '" + unconvertedConfigFile.getName() + "'.", true);

				YamlConfiguration unconvertedConfig = YamlConfiguration.loadConfiguration(unconvertedConfigFile);
				unconvertedConfig.set("unconverted players", unconverted);
				try {
					unconvertedConfig.save(unconvertedConfigFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
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
		// nothing here :(
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

	public synchronized int updateQuery(String query) {
		this.refreshConnection();
		int row = -1;
		try
		{
			Statement statement = this.connection.createStatement();
			statement.executeUpdate(query);
			row = statement.getGeneratedKeys().getInt(1);
			statement.close();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		return row;
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
		try
		{
			Statement statement = this.connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			return new Result(statement, result);
		} catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	// ///////////////////////////
	// PAINTBALL SPEZIFISCHER TEIL

	public void createDefaultTable(String name, String query, String indexOn) {
		this.updateQuery("CREATE TABLE IF NOT EXISTS " + name + "(" + query + ");");
		if (indexOn != null)
							this.updateQuery("CREATE UNIQUE INDEX IF NOT EXISTS " + name + "_" + indexOn + " ON " + name + "(" + indexOn + ");");
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
