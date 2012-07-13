package me.blablubbabc.paintball;

import java.util.LinkedHashMap;
import java.util.Set;
import me.blablubbabc.BlaDB.Register;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public enum Lobby {

	RED			(Material.WOOL, (byte)14, ChatColor.RED),
	BLUE		(Material.WOOL, (byte)11, ChatColor.BLUE),
	RANDOM		(Material.AIR, (byte)0, ChatColor.GREEN),
	SPECTATE	(Material.AIR, (byte)0, ChatColor.YELLOW),
	LOBBY		(Material.AIR, (byte)0, ChatColor.WHITE);
	
	private Paintball plugin;
	private LinkedHashMap<Player, Boolean> players;	//members of a team: true: playing, false: waiting
	private ItemStack helmet;
	private ChatColor color;
	private Register data;
	
	private Lobby(Material mat, byte data, ChatColor color) {
		this.plugin = (Paintball) Bukkit.getServer().getPluginManager().getPlugin("Paintball");
		this.helmet = new ItemStack(mat, 1, Short.parseShort("0"), data);
		this.color = color;
		this.players = new LinkedHashMap<Player, Boolean>();
		this.data = new Register();
		this.updateData();
		this.loadData();
	}
	//DATA
	public void updateData() {
		if(plugin.data.getRegister(this.toString().toLowerCase()) == null) saveData();
		else this.data = plugin.data.getRegister(this.toString().toLowerCase());
		
		if(this.data.getValue("helmet.id") == null) this.data.setValue("helmet.id", helmet.getTypeId());
		if(this.data.getValue("helmet.data") == null) this.data.setValue("helmet.data", helmet.getData().getData());
		if(this.data.getValue("color") == null) this.data.setValue("color", color);
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
	public void addMember(Player player) {
		if(!players.containsKey(player)) players.put(player, false);
	}
	public void removeMember(Player player) {
		if(players.containsKey(player)) players.remove(player);
	}
	public void setHelmet(Material mat, byte data) {
		helmet = new ItemStack(mat, 1, Short.parseShort("0"), data);
	}
	public void setPlaying(Player player) {
		if(players.containsKey(player)) players.put(player, true);
	}
	public void setWaiting(Player player) {
		if(players.containsKey(player)) players.put(player, false);
	}
	//GETTER
	public  Set<Player> getMembers() {
		return players.keySet();
	}
	public boolean isMember(Player player) {
		if(players.containsKey(player)) return true;
		return false;
	}
	public int numberInGame() {
		int number = 0;
		for(Player player : players.keySet()) {
			if(players.get(player)) number++;
		}
		return number;
	}
	public int numberWaiting() {
		int number = 0;
		for(Player player : players.keySet()) {
			if(!players.get(player)) number++;
		}
		return number;
	}
	public int number() {
		return players.size();
	}
	public ItemStack helmet() {
		return helmet;
	}
	public ChatColor color() {
		return color;
	}
	//STATIC
	//GETTER
	public static Lobby getTeam(String team) {
		for(Lobby t : Lobby.values()) {
			if(t.toString().toLowerCase().contains(team.toLowerCase())) return t;
		}
		return null;
	}
	public static Lobby getTeam(Player player) {
		for(Lobby team : Lobby.values()) {
			if(team.isMember(player) && !team.equals(Lobby.LOBBY)) return team;
		}
		if(Lobby.LOBBY.isMember(player)) return Lobby.LOBBY;
		return null;
	}
	public static boolean toggledFeed(Player player) {
		if(Lobby.LOBBY.players.get(player)) return true;
		return false;
	}
	public static void toggleFeed(Player player) {
		if(toggledFeed(player)) Lobby.LOBBY.players.put(player, false);
		else Lobby.LOBBY.players.put(player, true);
	}
	public static boolean inTeam(Player player) {
		if(getTeam(player).equals(Lobby.RED) || getTeam(player).equals(Lobby.BLUE) || getTeam(player).equals(Lobby.RANDOM)) {
			return true;
		}
		return false;
	}
	public static boolean isPlaying(Player player) {
		if(getTeam(player).equals(Lobby.RED) || getTeam(player).equals(Lobby.BLUE) || getTeam(player).equals(Lobby.RANDOM)) {
			if(getTeam(player).players.get(player)) return true;
		}
		return false;
	}
	public static boolean isSpectating(Player player) {
		if(getTeam(player).equals(Lobby.SPECTATE)) {
			if(getTeam(player).players.get(player)) return true;
		}
		return false;
	}
	//SETTER
	public static void remove(Player player) {
		
		for(Lobby l : Lobby.values()) {
			l.removeMember(player);
		}
	}
}
