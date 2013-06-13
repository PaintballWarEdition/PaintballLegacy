package de.blablubbabc.paintball;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;


public class PlayerManager {
	private Map<String, PlayerDataStore> playerStore;

	public PlayerManager() {
		playerStore = new HashMap<String, PlayerDataStore>();
		
		addAllOnlinePlayers();
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
		if (!Paintball.instance.sql.sqlPlayers.isPlayerExisting(name)) {
			Paintball.instance.sql.sqlPlayers.addNewPlayer(name);
		}
	}

	public void resetData() {
		Paintball.instance.getServer().getScheduler()
				.runTaskAsynchronously(Paintball.instance, new Runnable() {

					@Override
					public void run() {
						Paintball.instance.sql.sqlPlayers.resetAllPlayerStats();
					}
				});
	}

	public void resetDataSameThread() {
		Paintball.instance.sql.sqlPlayers.resetAllPlayerStats();
	}

	public void resetData(final String player) {
		Paintball.instance.getServer().getScheduler()
				.runTaskAsynchronously(Paintball.instance, new Runnable() {

					@Override
					public void run() {
						Paintball.instance.sql.sqlPlayers.resetPlayerStats(player);
					}
				});
	}

	public boolean exists(String player) {
		return Paintball.instance.sql.sqlPlayers.isPlayerExisting(player);
	}

	// STATS
	public void addStatsAsync(final String player,
			final HashMap<String, Integer> stats) {
		Paintball.instance.getServer().getScheduler()
				.runTaskAsynchronously(Paintball.instance, new Runnable() {

					@Override
					public void run() {
						Paintball.instance.sql.sqlPlayers.addPlayerStats(player, stats);
						Paintball.instance.sql.sqlPlayers.calculateStats(player);
					}
				});
	}

	public void addStats(final String player,
			final HashMap<String, Integer> stats) {
		Paintball.instance.sql.sqlPlayers.addPlayerStats(player, stats);
		Paintball.instance.sql.sqlPlayers.calculateStats(player);
	}

	public void setStats(final String player,
			final HashMap<String, Integer> stats) {
		Paintball.instance.getServer().getScheduler()
				.runTaskAsynchronously(Paintball.instance, new Runnable() {

					@Override
					public void run() {
						Paintball.instance.sql.sqlPlayers.setPlayerStats(player, stats);
						Paintball.instance.sql.sqlPlayers.calculateStats(player);
					}
				});
	}

	// GETTER
	public ArrayList<String> getAllPlayerNames() {
		return Paintball.instance.sql.sqlPlayers.getAllPlayerNames();
	}

	public int getPlayerCount() {
		return Paintball.instance.sql.sqlPlayers.getPlayerCount();
	}

	public HashMap<String, Integer> getStats(String player) {
		return Paintball.instance.sql.sqlPlayers.getPlayerStats(player);
	}

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
