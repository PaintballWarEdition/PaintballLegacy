package de.blablubbabc.paintball;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.blablubbabc.paintball.statistics.player.PlayerStats;


public class PlayerManager {
	private Map<String, PlayerDataStore> playerStore;
	private Map<String, PlayerStats> playerStats;

	public PlayerManager() {
		playerStore = new HashMap<String, PlayerDataStore>();
		playerStats = new HashMap<String, PlayerStats>();
		
		addAllOnlinePlayers();
	}

	// PLAYERSTATS
	
	public void loadPlayerStats(String playerName) {
		playerStats.put(playerName, new PlayerStats(playerName));
	}
	
	public PlayerStats getPlayerStats(String playerName) {
		// check if in cache, if not return temporary retrieved PlayerStats or null, 
		// if no stats exist for this player:
		PlayerStats stats = playerStats.get(playerName);
		if (stats == null) {
			if (exists(playerName)) stats = new PlayerStats(playerName);
		}
		return stats;
	}
	
	
	
	// METHODS
	// SETTER
	public void addAllOnlinePlayers() {
		Paintball.instance.getServer().getScheduler()
				.runTaskAsynchronously(Paintball.instance, new Runnable() {

					@Override
					public void run() {
						for (Player p : Paintball.instance.getServer().getOnlinePlayers()) {
							addPlayer(p.getName());
						}
					}
				});
	}
	
	public void addPlayerAsync(final String name) {
		Paintball.instance.getServer().getScheduler()
				.runTaskAsynchronously(Paintball.instance, new Runnable() {

					@Override
					public void run() {
						addPlayer(name);
					}
				});
	}
	
	private void addPlayer(final String name) {
		// TODO is exits check really necessary here ?
		if (!Paintball.instance.sql.sqlPlayers.isPlayerExisting(name)) {
			Paintball.instance.sql.sqlPlayers.addNewPlayer(name);
		}
	}

	public void resetAllDataAsync() {
		Paintball.instance.getServer().getScheduler()
				.runTaskAsynchronously(Paintball.instance, new Runnable() {

					@Override
					public void run() {
						resetAllData();
					}
				});
	}

	public void resetAllData() {
		// reset stats in cache:
		for (PlayerStats stats : playerStats.values()) {
			stats.resetStats();
		}
		// reset in databse:
		Paintball.instance.sql.sqlPlayers.resetAllPlayerStats();
	}

	/*public void resetDataOfPlayerAsync(final String player) {
		Paintball.instance.getServer().getScheduler()
				.runTaskAsynchronously(Paintball.instance, new Runnable() {

					@Override
					public void run() {
						Paintball.instance.sql.sqlPlayers.resetPlayerStats(player);
					}
				});
	}*/

	public boolean exists(String player) {
		return Paintball.instance.sql.sqlPlayers.isPlayerExisting(player);
	}

	// STATS
	/*public void addStatsAsync(final String player, final Map<PlayerStat, Integer> stats) {
		Paintball.instance.getServer().getScheduler()
				.runTaskAsynchronously(Paintball.instance, new Runnable() {

					@Override
					public void run() {
						addStats(player, stats);
					}
				});
	}

	public void addStats(final String player, final Map<PlayerStat, Integer> stats) {
		Paintball.instance.sql.sqlPlayers.addPlayerStats(player, stats);
		Paintball.instance.sql.sqlPlayers.calculateStats(player);
	}

	public void setStatsAsync(final String player, final Map<PlayerStat, Integer> stats) {
		Paintball.instance.getServer().getScheduler()
				.runTaskAsynchronously(Paintball.instance, new Runnable() {

					@Override
					public void run() {
						setStats(player, stats);
					}
				});
	}
	
	public void setStats(final String player, final Map<PlayerStat, Integer> stats) {
		Paintball.instance.sql.sqlPlayers.setPlayerStats(player, stats);
		Paintball.instance.sql.sqlPlayers.calculateStats(player);
	}*/

	// GETTER
	
	public int getPlayersEverPlayedCount() {
		return Paintball.instance.sql.sqlPlayers.getPlayersEverPlayedCount();
	}

	public int getPlayerCount() {
		return Paintball.instance.sql.sqlPlayers.getPlayerCount();
	}

	/*public Map<PlayerStat, Integer> getStats(String player) {
		return Paintball.instance.sql.sqlPlayers.getPlayerStats(player);
	}*/

	public void teleportStoreClearPlayer(Player player, Location to) {
		playerStore.put(player.getName(), new PlayerDataStore(player, to));
	}
	
	public void clearRestoreTeleportPlayer(Player player) {
		PlayerDataStore playerData = playerStore.remove(player.getName());
		if (playerData != null) {
			playerData.restoreTeleportPlayer(player);
		}
	}

}
