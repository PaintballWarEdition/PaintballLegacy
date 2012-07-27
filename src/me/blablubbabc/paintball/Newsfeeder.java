package me.blablubbabc.paintball;

import java.util.HashMap;

import org.bukkit.entity.Player;


public class Newsfeeder {
	private Paintball plugin;
	
	public String pluginName;
	
	public Newsfeeder(Paintball pl) {
		plugin = pl;
		pluginName = plugin.t.getString("PLUGIN");
	}
	
	
	
	//METHODS
	public void join(String name) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("player", name);
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(plugin.t.getString("LOBBY_JOIN", vars));
		}
	}
	public void leave(String name) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("player", name);
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(plugin.t.getString("LOBBY_LEAVE", vars));
		}
	}
	
	public void tip(String message) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("message", message);
		for(Player player : Lobby.LOBBY.getMembers()) {
			player.sendMessage(plugin.t.getString("TIP", vars));
		}
	}
	
	public void counter(int counter) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("seconds", String.valueOf(counter));
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(plugin.t.getString("COUNTDOWN", vars));
		}
	}
	
	public void text(String message) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("message", String.valueOf(message));
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(plugin.t.getString("TEXT", vars));
		}
	}
	
	public void status(String message) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("message", String.valueOf(message));
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(plugin.t.getString("MATCH_STATUS", vars));
		}
	}
	public void players() {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("needed_players_overview", getNeededPlayers());
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(plugin.t.getString("WAITING_PLAYERS_OVERVIEW", vars));
		}
	}
	public void players(Player player) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("needed_players_overview", getNeededPlayers());
		player.sendMessage(plugin.t.getString("WAITING_PLAYERS_OVERVIEW", vars));
	}
	public String getPlayersOverview() {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("color_red", Lobby.RED.color().toString());
		vars.put("red", String.valueOf(Lobby.RED.numberWaiting()));
		vars.put("color_blue", Lobby.BLUE.color().toString());
		vars.put("blue", String.valueOf(Lobby.BLUE.numberWaiting()));
		vars.put("color_random", Lobby.RANDOM.color().toString());
		vars.put("random", String.valueOf(Lobby.RANDOM.numberWaiting()));
		vars.put("color_spec", Lobby.SPECTATE.color().toString());
		vars.put("spec", String.valueOf(Lobby.SPECTATE.numberWaiting()));
		String overview = plugin.t.getString("PLAYERS_OVERVIEW", vars);
		return overview;
	}
	public String getNeededPlayers() {
		int players = Lobby.RED.numberWaiting() + Lobby.BLUE.numberWaiting() + Lobby.RANDOM.numberWaiting();
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("players", String.valueOf(players));
		vars.put("needed_players", String.valueOf(plugin.minPlayers));
		vars.put("players_overview", getPlayersOverview());
		String info = plugin.t.getString("NEEDED_PLAYERS_OVERVIEW", vars);
		return info;
	}
	
	public void feed(Player target, Player killer, Match match) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("killer_color", Lobby.getTeam(killer).color().toString());
		vars.put("killer", killer.getName());
		vars.put("target_color", Lobby.getTeam(target).color().toString());
		vars.put("target", target.getName());
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(plugin.t.getString("KILL_FEED", vars));
		}
	}
}
