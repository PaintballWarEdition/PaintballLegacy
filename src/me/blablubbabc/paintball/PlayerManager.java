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

	public PlayerManager(Paintball pl) {
		plugin = pl;
		locations = new HashMap<Player, Location>();
		invContent = new HashMap<Player, ItemStack[]>();
		invArmor = new HashMap<Player, ItemStack[]>();
		
		for(Player p : plugin.getServer().getOnlinePlayers()) {
			addPlayer(p.getName());
		}
	}

	//METHODS
	//SETTER
	public void addPlayer(String name) {
		if(!plugin.sql.sqlPlayers.isPlayerExisting(name)) {
			plugin.sql.sqlPlayers.addNewPlayer(name);
		}		
	}

	public void resetData() {
		plugin.sql.sqlPlayers.resetAllPlayerStats();
	}
	
	public void resetData(String player) {
		plugin.sql.sqlPlayers.resetPlayerStats(player);
	}

	public boolean exists(String player) {
		return plugin.sql.sqlPlayers.isPlayerExisting(player);
	}

	//STATS
	public void addStats(String player, HashMap<String, Integer> stats) {
		plugin.sql.sqlPlayers.addPlayerStats(player, stats);
		plugin.sql.sqlPlayers.calculateStats(player);
	}

	public void setStats(String player, HashMap<String, Integer> stats) {
		plugin.sql.sqlPlayers.setPlayerStats(player, stats);
		plugin.sql.sqlPlayers.calculateStats(player);
	}
	
	//GETTER
	public ArrayList<String> getAllPlayerNames() {
		return plugin.sql.sqlPlayers.getAllPlayerNames();
	}
	public int getPlayerCount() {
		return plugin.sql.sqlPlayers.getPlayerCount();
	}
	public HashMap<String, Integer> getStats(String player) {
		return plugin.sql.sqlPlayers.getPlayerStats(player);
	}

	public Location getLoc(Player player) {
		if(locations.get(player) != null) return locations.get(player);
		else return null;
	}
	public ItemStack[] getInvContent(Player player) {
		return invContent.get(player);
	}
	public ItemStack[] getInvArmor(Player player) {
		return invArmor.get(player);
	}

	public void setLoc(Player player, Location loc) {
		locations.put(player, loc);
	}

	public void setInv(Player player, PlayerInventory inv) {
		invContent.put(player, inv.getContents());
		invArmor.put(player, inv.getArmorContents());
	}

}
