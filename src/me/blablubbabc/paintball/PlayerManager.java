package me.blablubbabc.paintball;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class PlayerManager {
	private static Paintball plugin;
	private HashMap<Player, Location> locations;
	private HashMap<Player, ItemStack[]> invContent;
	private HashMap<Player, ItemStack[]> invArmor;
	private HashMap<Player, Integer> level;
	private HashMap<Player, Float> exp;

	public PlayerManager(Paintball pl) {
		plugin = pl;
		locations = new HashMap<Player, Location>();
		invContent = new HashMap<Player, ItemStack[]>();
		invArmor = new HashMap<Player, ItemStack[]>();
		level = new HashMap<Player, Integer>();
		exp = new HashMap<Player, Float>();
		
		addAllOnlinePlayers();
	}

	// METHODS
	// SETTER
	public void addAllOnlinePlayers() {
		plugin.getServer().getScheduler()
				.runTaskAsynchronously(plugin, new Runnable() {

					@Override
					public void run() {
						for (Player p : plugin.getServer().getOnlinePlayers()) {
							addPlayer(p.getName());
						}
					}
				});
	}
	
	public void addPlayerAsync(final String name) {
		plugin.getServer().getScheduler()
				.runTaskAsynchronously(plugin, new Runnable() {

					@Override
					public void run() {
						addPlayer(name);
					}
				});
	}
	
	private void addPlayer(final String name) {
		if (!plugin.sql.sqlPlayers.isPlayerExisting(name)) {
			plugin.sql.sqlPlayers.addNewPlayer(name);
		}
	}

	public void resetData() {
		plugin.getServer().getScheduler()
				.runTaskAsynchronously(plugin, new Runnable() {

					@Override
					public void run() {
						plugin.sql.sqlPlayers.resetAllPlayerStats();
					}
				});
	}

	public void resetDataSameThread() {
		plugin.sql.sqlPlayers.resetAllPlayerStats();
	}

	public void resetData(final String player) {
		plugin.getServer().getScheduler()
				.runTaskAsynchronously(plugin, new Runnable() {

					@Override
					public void run() {
						plugin.sql.sqlPlayers.resetPlayerStats(player);
					}
				});
	}

	public boolean exists(String player) {
		return plugin.sql.sqlPlayers.isPlayerExisting(player);
	}

	// STATS
	public void addStatsAsync(final String player,
			final HashMap<String, Integer> stats) {
		plugin.getServer().getScheduler()
				.runTaskAsynchronously(plugin, new Runnable() {

					@Override
					public void run() {
						plugin.sql.sqlPlayers.addPlayerStats(player, stats);
						plugin.sql.sqlPlayers.calculateStats(player);
					}
				});
	}

	public void addStats(final String player,
			final HashMap<String, Integer> stats) {
		plugin.sql.sqlPlayers.addPlayerStats(player, stats);
		plugin.sql.sqlPlayers.calculateStats(player);
	}

	public void setStats(final String player,
			final HashMap<String, Integer> stats) {
		plugin.getServer().getScheduler()
				.runTaskAsynchronously(plugin, new Runnable() {

					@Override
					public void run() {
						plugin.sql.sqlPlayers.setPlayerStats(player, stats);
						plugin.sql.sqlPlayers.calculateStats(player);
					}
				});
	}

	// GETTER
	public ArrayList<String> getAllPlayerNames() {
		return plugin.sql.sqlPlayers.getAllPlayerNames();
	}

	public int getPlayerCount() {
		return plugin.sql.sqlPlayers.getPlayerCount();
	}

	public HashMap<String, Integer> getStats(String player) {
		return plugin.sql.sqlPlayers.getPlayerStats(player);
	}

	public synchronized Location getLoc(Player player) {
		if (locations.get(player) != null) {
			Location loc = locations.get(player);
			locations.remove(player);
			return loc;
		} else
			return null;
	}

	public void restoreInventory(Player player) {
		//PlayerInventory
		//null check added:
		ItemStack[] isc = getInvContent(player);
		if(isc != null) {
			player.getInventory().setContents(isc);
		}
		ItemStack[] isa = getInvArmor(player);
		if(isa != null) {
			player.getInventory().setArmorContents(isa);
		}

		player.sendMessage(plugin.t.getString("INVENTORY_RESTORED"));
	}
	
	private ItemStack[] getInvContent(Player player) {
		ItemStack[] inv = invContent.get(player);
		invContent.remove(player);
		return inv;
	}

	private ItemStack[] getInvArmor(Player player) {
		ItemStack[] inv = invArmor.get(player);
		invArmor.remove(player);
		return inv;
	}

	public void setLoc(Player player, Location loc) {
		locations.put(player, loc);
	}

	public void storeInventory(Player player) {
		PlayerInventory inv = player.getInventory();
		invContent.put(player, inv.getContents());
		invArmor.put(player, inv.getArmorContents());
	}
	
	public void restoreExp(Player player) {
		Integer levelInt = level.get(player);
		if (levelInt != null) player.setLevel(levelInt);
		Float expFloat = exp.get(player);
		if (exp != null) player.setExp(expFloat);
		
		level.remove(player);
		exp.remove(player);
	}
	
	public void storeExp(Player player) {
		level.put(player, player.getLevel());
		exp.put(player, player.getExp());
	}

}
