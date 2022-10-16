/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;

import de.blablubbabc.paintball.utils.Translator;

public enum Lobby {

	RED("red", Color.RED, ChatColor.RED),
	BLUE("blue", Color.BLUE, ChatColor.BLUE),
	RANDOM("random", Color.GREEN, ChatColor.GREEN),
	SPECTATE("spectator", Color.YELLOW, ChatColor.YELLOW),
	LOBBY("lobby", Color.WHITE, ChatColor.WHITE);

	// Statistics:
	private static volatile int playersInLobby = 0;
	private static final AtomicInteger maxPlayersInLobby = new AtomicInteger();

	private static void onPlayersInLobbyChanged(int newPlayersInLobby) {
		int oldPlayersInLobby = playersInLobby;
		playersInLobby = newPlayersInLobby;

		if (newPlayersInLobby > oldPlayersInLobby) {
			maxPlayersInLobby.updateAndGet(value -> Math.max(value, newPlayersInLobby));
		}
	}

	// Can be called from a different thread.
	public static int getAndResetMaxPlayersInLobby() {
		return maxPlayersInLobby.getAndSet(playersInLobby);
	}

	// members of a team: true: playing, false: waiting; Lobby: true/false toggle messages
	private Map<Player, Boolean> players;
	private String nameKey;
	private ChatColor color;
	// private int colorA;
	private Color colorA;

	private Lobby(String nameKey, Color colorA, ChatColor color) {
		this.nameKey = nameKey;
		this.color = color;
		this.colorA = colorA;
		this.players = new HashMap<Player, Boolean>();
	}

	// METHODS
	// SETTER
	public synchronized void addMember(Player player) {
		if (players.putIfAbsent(player, false) == null) {
			if (this == Lobby.LOBBY) {
				onPlayersInLobbyChanged(players.size());
			}
		}
	}

	public synchronized void removeMember(Player player) {
		if (players.remove(player) != null) {
			if (this == Lobby.LOBBY) {
				onPlayersInLobbyChanged(players.size());
			}
		}
	}

	public synchronized void setPlaying(Player player) {
		if (players.containsKey(player)) players.put(player, true);
	}

	public synchronized void setWaiting(Player player) {
		if (players.containsKey(player)) players.put(player, false);
	}

	// GETTER
	public synchronized Set<Player> getMembers() {
		return players.keySet();
	}

	public synchronized boolean isMember(Player player) {
		return players.containsKey(player);
	}

	public synchronized int numberInGame() {
		int number = 0;
		for (Player player : players.keySet()) {
			if (players.get(player)) number++;
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

	public String getName() {
		return Translator.getString(nameKey);
	}

	public ChatColor color() {
		return color;
	}

	public Color colorA() {
		return colorA;
	}

	// STATIC
	// GETTER
	/*public synchronized static Lobby getTeam(String team) {
		for (Lobby t : Lobby.values()) {
			if (t.getName().equalsIgnoreCase(team)) return t;
		}
		return null;
	}*/
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

	// SETTER
	public synchronized static void remove(Player player) {
		for (Lobby l : Lobby.values()) {
			l.removeMember(player);
		}
	}
}
