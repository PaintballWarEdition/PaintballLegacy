package me.blablubbabc.paintball;

import java.util.HashMap;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;

public enum Lobby {
	
	RED			("red", Color.RED, ChatColor.RED),
	BLUE		("blue", Color.BLUE, ChatColor.BLUE),
	RANDOM		("random", Color.GREEN, ChatColor.GREEN),
	SPECTATE	("spectator", Color.YELLOW, ChatColor.YELLOW),
	LOBBY		("lobby", Color.WHITE, ChatColor.WHITE);
	
	private Paintball plugin;
	private HashMap<Player, Boolean> players;	//members of a team: true: playing, false: waiting; Lobby: true/false toggle messages
	private int maxPlayers;
	private String name;
	private ChatColor color;
	//private int colorA;
	private Color colorA;
	
	private Lobby(String name, Color colorA, ChatColor color) {
		this.plugin = (Paintball) Bukkit.getServer().getPluginManager().getPlugin("Paintball");
		this.name = plugin.t.getString(name);
		this.color = color;
		this.colorA = colorA;
		this.players = new HashMap<Player, Boolean>();
		this.maxPlayers = 0;
	}
	//METHODS
	//SETTER
	public void addMember(Player player) {
		if(!players.containsKey(player)) {
			players.put(player, false);
			//max Players since last metrics submit-try
			if(players.size() > maxPlayers) maxPlayers = players.size();
		}
	}
	public void removeMember(Player player) {
		if(players.containsKey(player)) players.remove(player);
	}
	public void setPlaying(Player player) {
		if(players.containsKey(player)) players.put(player, true);
	}
	public void setWaiting(Player player) {
		if(players.containsKey(player)) players.put(player, false);
	}
	//GETTER
	public Set<Player> getMembers() {
		return players.keySet();
	}
	//TEST unsynchronised:
	public boolean isMember(Player player) {
		return players.containsKey(player);
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
	public int maxNumber() {
		return maxPlayers;
	}
	public String getName() {
		return name;
	}
	public ChatColor color() {
		return color;
	}
	public Color colorA() {
		return colorA;
	}
	//STATIC
	//GETTER
	public static Lobby getTeam(String team) {
		for(Lobby t : Lobby.values()) {
			if(t.getName().equalsIgnoreCase(team)) return t;
		}
		return null;
	}
	public static Lobby getTeam(Player player) {
		for(Lobby team : Lobby.values()) {
			if(team.isMember(player) && team != Lobby.LOBBY) return team;
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
		if(getTeam(player) == Lobby.RED || getTeam(player) == Lobby.BLUE || getTeam(player) == Lobby.RANDOM) {
			return true;
		}
		return false;
	}
	public static boolean isPlaying(Player player) {
		if(inTeam(player)) {
			if(getTeam(player).players.get(player)) return true;
		}
		return false;
	}
	public static boolean isSpectating(Player player) {
		if(getTeam(player) == Lobby.SPECTATE) {
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
	
	public static void resetMaxPlayers() {
		for(Lobby l : Lobby.values()) {
			l.maxPlayers = l.players.size();
		}
	}
}
