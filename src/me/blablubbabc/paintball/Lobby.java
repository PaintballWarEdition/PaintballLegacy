package me.blablubbabc.paintball;

import java.util.LinkedHashMap;
import java.util.Set;
import me.blablubbabc.BlaDB.BlaDBRegister;
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
	private LinkedHashMap<Player, Boolean> players;	//members of a team: true: playing, false: waiting; Lobby: true/false toggle messages
	private int maxPlayers;
	private String name;
	private ItemStack helmet;
	private ChatColor color;
	private BlaDBRegister data;
	
	private Lobby(String name, Material mat, byte data, ChatColor color) {
		this.plugin = (Paintball) Bukkit.getServer().getPluginManager().getPlugin("Paintball");
		this.name = plugin.t.getString(name);
		this.helmet = new ItemStack(mat, 1, Short.parseShort("0"), data);
		this.color = color;
		this.players = new LinkedHashMap<Player, Boolean>();
		this.maxPlayers = 0;
		this.data = new BlaDBRegister();
		this.updateData();
		this.loadData();
	}
	//DATA
	public void updateData() {
		if(plugin.data.getRegister(this.toString().toLowerCase()) == null) saveData();
		else this.data = plugin.data.getRegister(this.toString().toLowerCase());
		
		if(this.data.getValue("helmet.id") == null) this.data.setValue("helmet.id", helmet.getTypeId());
		if(this.data.getValue("helmet.data") == null) this.data.setValue("helmet.data", helmet.getData().getData());
		//if(this.data.getValue("color") == null) this.data.setValue("color", color);
		//UPDATE 1.0.5->1.0.6
		this.data.setValue("color", color);
		this.saveData();
	}
	public void saveData() {
		plugin.data.addRegister(this.toString().toLowerCase(), this.data);
		plugin.data.saveFile();
	}
	public void loadData() {
		this.helmet = new ItemStack(this.data.getInt("helmet.id"), 1, Short.parseShort("0"), (Byte)this.data.getValue("helmet.data"));
		this.color = (ChatColor) this.data.getValue("color");
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
			if(team.isMember(player) && !team.equals(Lobby.LOBBY)) return team;
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
		if(getTeam(player).equals(Lobby.RED) || getTeam(player).equals(Lobby.BLUE) || getTeam(player).equals(Lobby.RANDOM)) {
			return true;
		}
		return false;
	}
	public synchronized static boolean isPlaying(Player player) {
		if(getTeam(player).equals(Lobby.RED) || getTeam(player).equals(Lobby.BLUE) || getTeam(player).equals(Lobby.RANDOM)) {
			if(getTeam(player).players.get(player)) return true;
		}
		return false;
	}
	public synchronized static boolean isSpectating(Player player) {
		if(getTeam(player).equals(Lobby.SPECTATE)) {
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
