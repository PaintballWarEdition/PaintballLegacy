package de.blablubbabc.paintball;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;


public class Text {
	
	public String pluginName;
	
	public Text() {
		pluginName = Translator.getString("PLUGIN");
	}
	
	
	//METHODS
	public String joinLobby(String playerName) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("player", playerName);
		return Translator.getString("LOBBY_JOIN", vars);
	}
	public String leaveLobby(String playerName) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("player", playerName);
		return Translator.getString("LOBBY_LEAVE", vars);
	}
	
	public String tip(String message) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("message", message);
		return Translator.getString("TIP", vars);
	}
	
	public String countdown(int counter) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("seconds", String.valueOf(counter));
		return Translator.getString("COUNTDOWN", vars);
	}
	
	public String roundTime(int time) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("seconds", String.valueOf(time));
		return Translator.getString("MATCH_REMAINING_TIME", vars);
	}

	public String startCountdown(int counter) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("seconds", String.valueOf(counter));
		return Translator.getString("COUNTDOWN_START", vars);
	}
	
	public String text(String message) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("message", message);
		return Translator.getString("TEXT", vars);
	}
	
	public String status(String message) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("message", String.valueOf(message));
		return Translator.getString("MATCH_STATUS", vars);
	}
	
	public String happyhour(int seconds) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("time", String.valueOf(seconds));
		return Translator.getString("HAPPYHOUR_TIME", vars);
	}
	
	/*public String players(Lobby lobby) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("needed_players_overview", getNeededPlayers(lobby));
		return Translator.getString("WAITING_PLAYERS_OVERVIEW", vars);
	}*/
	
	/*public String getNeededPlayers(Lobby lobby) {
		int players = LobbyE.RED.numberWaiting() + LobbyE.BLUE.numberWaiting() + LobbyE.RANDOM.numberWaiting();
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("players", String.valueOf(players));
		vars.put("needed_players", String.valueOf(plugin.minPlayers));
		vars.put("players_overview", getPlayersOverview(lobby));
		String info = plugin.t.getString("NEEDED_PLAYERS_OVERVIEW", vars);
		return info;
	}*/
	
	public String waitingPlayersOverview(Lobby lobby) {
		HashMap<String, String> vars = new HashMap<String, String>();
		Map<Team, Integer> waiting = lobby.countWaitingPPlayers();
		for(Team team : Team.values()) {
			String teamName = team.toString().toLowerCase();
			vars.put("color_"+teamName, team.getChatColor().toString());
			vars.put(teamName, String.valueOf(waiting.get(team)));
		}
		return Translator.getString("PLAYERS_OVERVIEW", vars);
	}
	
	public String frag(Player target, Player killer, ChatColor targetColor, ChatColor killerColor) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("killer_color", killerColor.toString());
		vars.put("killer", killer.getName());
		vars.put("target_color", targetColor.toString());
		vars.put("target", target.getName());
		return Translator.getString("KILL_FEED", vars);
	}
	
	public String death(Player player, ChatColor playerColor) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("target_color", playerColor.toString());
		vars.put("target", player.getName());
		return Translator.getString("PLAYER_DIED", vars);
	}
	
	public String afkLeave(Player player, ChatColor playerColor) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("player_color", playerColor.toString());
		vars.put("player", player.getName());
		return Translator.getString("PLAYER_AFK", vars);
	}
}
