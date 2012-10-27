package me.blablubbabc.paintball;

import java.util.HashMap;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public enum Lobby {

	RED			("red", Material.WOOL, (byte)14, ChatColor.RED),
	BLUE		("blue", Material.WOOL, (byte)11, ChatColor.BLUE),
	RANDOM		("random", Material.AIR, (byte)0, ChatColor.GREEN),
	SPECTATE	("spectator", Material.WOOL, (byte)4, ChatColor.YELLOW),
	LOBBY		("lobby", Material.AIR, (byte)0, ChatColor.WHITE);
	
	private Paintball plugin;
	private HashMap<Player, Boolean> players;	//members of a team: true: playing, false: waiting; Lobby: true/false toggle messages
	private int maxPlayers;
	private String name;
	private ItemStack helmet;
	private ChatColor color;
	
	private Lobby(String name, Material mat, byte data, ChatColor color) {
		this.plugin = (Paintball) Bukkit.getServer().getPluginManager().getPlugin("Paintball");
		this.name = plugin.t.getString(name);
		this.helmet = new ItemStack(mat, 1, Short.parseShort("0"), data);
		this.color = color;
		this.players = new HashMap<Player, Boolean>();
		this.maxPlayers = 0;
		this.loadData();
	}
	//DATA
	public void updateData() {
		if(!plugin.sql.sqlData.exists(this.name()+".helmet.id")) plugin.sql.sqlData.addInt(this.name()+".helmet.id", this.helmet.getTypeId());
		if(!plugin.sql.sqlData.exists(this.name()+".helmet.data")) plugin.sql.sqlData.addString(this.name()+".helmet.data", String.valueOf(this.helmet.getData().getData()));
		if(!plugin.sql.sqlData.exists(this.name()+".color")) plugin.sql.sqlData.addString(this.name()+".color", this.color.name());
	}
	public void loadData() {
		this.updateData();
		this.helmet = new ItemStack(plugin.sql.sqlData.getInt(this.name()+".helmet.id"), 1, Short.parseShort("0"), Byte.valueOf(plugin.sql.sqlData.getString(this.name()+".helmet.data")));
		this.color = ChatColor.valueOf(plugin.sql.sqlData.getString(this.name()+".color"));
	}
	//METHODS
	//SETTER
	public synchronized void addMember(Player player) {
		if(!players.containsKey(player)) {
			players.put(player, false);
			//max Players since last metrics submit-try
			if(players.size() > maxPlayers) maxPlayers = players.size();
		}
	}
	public synchronized void removeMember(Player player) {
		if(players.containsKey(player)) players.remove(player);
	}
	public void setHelmet(Material mat, byte data) {
		helmet = new ItemStack(mat, 1, Short.parseShort("0"), data);
	}
	public synchronized void setPlaying(Player player) {
		if(players.containsKey(player)) players.put(player, true);
	}
	public synchronized void setWaiting(Player player) {
		if(players.containsKey(player)) players.put(player, false);
	}
	//GETTER
	public synchronized Set<Player> getMembers() {
		return players.keySet();
	}
	public synchronized boolean isMember(Player player) {
		if(players.containsKey(player)) return true;
		return false;
	}
	public synchronized int numberInGame() {
		int number = 0;
		for(Player player : players.keySet()) {
			if(players.get(player)) number++;
		}
		return number;
	}
	public synchronized int numberWaiting() {
		int number = 0;
		for(Player player : players.keySet()) {
			if(!players.get(player)) number++;
		}
		return number;
	}
	public synchronized int number() {
		return players.size();
	}
	public int maxNumber() {
		return maxPlayers;
	}
	public String getName() {
		return name;
	}
	public ItemStack helmet() {
		return helmet;
	}
	public ChatColor color() {
		return color;
	}
	//STATIC
	//GETTER
	public synchronized static Lobby getTeam(String team) {
		for(Lobby t : Lobby.values()) {
			if(t.getName().equalsIgnoreCase(team)) return t;
		}
		return null;
	}
	public synchronized static Lobby getTeam(Player player) {
		for(Lobby team : Lobby.values()) {
			if(team.isMember(player) && team !=Lobby.LOBBY) return team;
		}
		if(Lobby.LOBBY.isMember(player)) return Lobby.LOBBY;
		return null;
	}
	public synchronized static boolean toggledFeed(Player player) {
		if(Lobby.LOBBY.players.get(player)) return true;
		return false;
	}
	public synchronized static void toggleFeed(Player player) {
		if(toggledFeed(player)) Lobby.LOBBY.players.put(player, false);
		else Lobby.LOBBY.players.put(player, true);
	}
	public synchronized static boolean inTeam(Player player) {
		if(getTeam(player) == Lobby.RED || getTeam(player) == Lobby.BLUE || getTeam(player) == Lobby.RANDOM) {
			return true;
		}
		return false;
	}
	public synchronized static boolean isPlaying(Player player) {
		if(inTeam(player)) {
			if(getTeam(player).players.get(player)) return true;
		}
		return false;
	}
	public synchronized static boolean isSpectating(Player player) {
		if(getTeam(player) == Lobby.SPECTATE) {
			if(getTeam(player).players.get(player)) return true;
		}
		return false;
	}
	//SETTER
	public synchronized static void remove(Player player) {
		
		for(Lobby l : Lobby.values()) {
			l.removeMember(player);
		}
	}
	
	public synchronized  static void resetMaxPlayers() {
		for(Lobby l : Lobby.values()) {
			l.maxPlayers = l.players.size();
		}
	}
}
