package de.blablubbabc.paintball;

import java.util.HashMap;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;

import de.blablubbabc.paintball.utils.Translator;


public enum Lobby {
	
	RED			("red", Color.RED, ChatColor.RED),
	BLUE		("blue", Color.BLUE, ChatColor.BLUE),
	RANDOM		("random", Color.GREEN, ChatColor.GREEN),
	SPECTATE	("spectator", Color.YELLOW, ChatColor.YELLOW),
	LOBBY		("lobby", Color.WHITE, ChatColor.WHITE);
	
	private HashMap<Player, Boolean> players;	//members of a team: true: playing, false: waiting; Lobby: true/false toggle messages
	private int maxPlayers;
	private String name;
	private ChatColor color;
	//private int colorA;
	private Color colorA;
	
	private Lobby(String name, Color colorA, ChatColor color) {
		this.name = Translator.getString(name);
		this.color = color;
		this.colorA = colorA;
		this.players = new HashMap<Player, Boolean>();
		this.maxPlayers = 0;
	}
	//METHODS
	//SETTER
	public synchronized void addMember(Player player) {
		if (!players.containsKey(player)) {
			players.put(player, false);
			//max Players since last metrics submit-try
			if (players.size() > maxPlayers) maxPlayers = players.size();
		}
	}
	public synchronized void removeMember(Player player) {
		if (players.containsKey(player)) players.remove(player);
	}
	public synchronized void setPlaying(Player player) {
		if (players.containsKey(player)) players.put(player, true);
	}
	public synchronized void setWaiting(Player player) {
		if (players.containsKey(player)) players.put(player, false);
	}
	//GETTER
	public synchronized Set<Player> getMembers() {
		return players.keySet();
	}
	
	public synchronized boolean isMember(Player player) {
		return players.containsKey(player);
	}
	public synchronized int numberInGame() {
		int number = 0;
		for (Player player : players.keySet()) {
			if(players.get(player)) number++;
		}
		return number;
	}
	public synchronized int numberWaiting() {
		int number = 0;
		for (Player player : players.keySet()) {
			if (!players.get(player)) number++;
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
	public ChatColor color() {
		return color;
	}
	public Color colorA() {
		return colorA;
	}
	//STATIC
	//GETTER
	public synchronized static Lobby getTeam(String team) {
		for (Lobby t : Lobby.values()) {
			if (t.getName().equalsIgnoreCase(team)) return t;
		}
		return null;
	}
	public synchronized static Lobby getTeam(Player player) {
		for (Lobby team : Lobby.values()) {
			if (team.isMember(player) && team != Lobby.LOBBY) return team;
		}
		if (Lobby.LOBBY.isMember(player)) return Lobby.LOBBY;
		return null;
	}
	public synchronized static boolean toggledFeed(Player player) {
		return Lobby.LOBBY.players.get(player);
	}
	public synchronized static void toggleFeed(Player player) {
		if (toggledFeed(player)) Lobby.LOBBY.players.put(player, false);
		else Lobby.LOBBY.players.put(player, true);
	}
	public synchronized static boolean inTeam(Player player) {
		Lobby team = getTeam(player);
		return team == Lobby.RED || team == Lobby.BLUE || team == Lobby.RANDOM;
	}
	public synchronized static boolean isPlaying(Player player) {
		if (inTeam(player)) {
			if (getTeam(player).players.get(player)) return true;
		}
		return false;
	}
	public synchronized static boolean isSpectating(Player player) {
		if (getTeam(player) == Lobby.SPECTATE) {
			if (Lobby.SPECTATE.players.get(player)) return true;
		}
		return false;
	}
	//SETTER
	public synchronized static void remove(Player player) {
		for (Lobby l : Lobby.values()) {
			l.removeMember(player);
		}
	}
	
	public synchronized static void resetMaxPlayers() {
		for (Lobby l : Lobby.values()) {
			l.maxPlayers = l.players.size();
		}
	}
}
