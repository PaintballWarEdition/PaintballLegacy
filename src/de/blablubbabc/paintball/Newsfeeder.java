package de.blablubbabc.paintball;

import java.util.HashMap;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.blablubbabc.paintball.utils.Translator;



public class Newsfeeder {
	private Paintball plugin;
	
	public String pluginName;
	
	public Newsfeeder(Paintball pl) {
		plugin = pl;
		pluginName = Translator.getString("PLUGIN");
	}
	
	
	
	//METHODS
	public void join(String name) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("player", name);
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(Translator.getString("LOBBY_JOIN", vars));
		}
	}
	public void leave(String name) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("player", name);
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(Translator.getString("LOBBY_LEAVE", vars));
		}
	}
	
	public void tip(String message) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("message", message);
		for(Player player : Lobby.LOBBY.getMembers()) {
			player.sendMessage(Translator.getString("TIP", vars));
		}
	}
	
	public void counter(int counter) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("seconds", String.valueOf(counter));
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(Translator.getString("COUNTDOWN", vars));
		}
	}
	
	public void roundTime(int time) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("seconds", String.valueOf(time));
		for (Player player : Lobby.LOBBY.getMembers()) {
			player.sendMessage(Translator.getString("MATCH_REMAINING_TIME", vars));
		}
	}
	
	public void text(String message) {
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) text(player, message);
		}
	}
	
	public void textUntoggled(String message) {
		for(Player player : Lobby.LOBBY.getMembers()) {
			text(player, message);
		}
	}
	
	public void text(CommandSender sender, String message) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("message", message);
		sender.sendMessage(Translator.getString("TEXT", vars));
	}
	
	public void status(String message) {
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) status(player, message);
		}
	}
	
	public String happyhour(int seconds) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("time", String.valueOf(seconds));
		return Translator.getString("HAPPYHOUR_TIME", vars);
	}
	
	public void status(CommandSender sender, String message) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("message", String.valueOf(message));
		sender.sendMessage(Translator.getString("MATCH_STATUS", vars));
	}
	
	public void players() {
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) players(player);
		}
	}
	public void players(CommandSender sender) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("needed_players_overview", getNeededPlayers());
		sender.sendMessage(Translator.getString("WAITING_PLAYERS_OVERVIEW", vars));
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
		String overview = Translator.getString("PLAYERS_OVERVIEW", vars);
		return overview;
	}
	public String getNeededPlayers() {
		int players = Lobby.RED.numberWaiting() + Lobby.BLUE.numberWaiting() + Lobby.RANDOM.numberWaiting();
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("players", String.valueOf(players));
		vars.put("needed_players", String.valueOf(plugin.minPlayers));
		vars.put("players_overview", getPlayersOverview());
		String info = Translator.getString("NEEDED_PLAYERS_OVERVIEW", vars);
		return info;
	}
	
	public void feed(Player target, Player killer, Match match) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("killer_color", Lobby.getTeam(match.getTeamName(killer)).color().toString());
		vars.put("killer", killer.getName());
		vars.put("target_color", Lobby.getTeam(match.getTeamName(target)).color().toString());
		vars.put("target", target.getName());
		if (match.setting_respawns != -1 && match.setting_respawns != 0) {
			vars.put("lives", String.valueOf(match.setting_respawns + 1));
			vars.put("lives_left", String.valueOf(match.respawnsLeft(target)));
			for(Player player : Lobby.LOBBY.getMembers()) {
				if(!Lobby.toggledFeed(player)) player.sendMessage(Translator.getString("KILL_FEED_LIVES", vars));
			}
		} else {
			for(Player player : Lobby.LOBBY.getMembers()) {
				if(!Lobby.toggledFeed(player)) player.sendMessage(Translator.getString("KILL_FEED", vars));
			}	
		}
	}
	
	public void death(Player target, Match match) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("target_color", Lobby.getTeam(match.getTeamName(target)).color().toString());
		vars.put("target", target.getName());
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(Translator.getString("PLAYER_DIED", vars));
		}
	}
	
	public void afkLeave(Player target, Match match) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("player_color", Lobby.getTeam(match.getTeamName(target)).color().toString());
		vars.put("player", target.getName());
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(Translator.getString("PLAYER_AFK", vars));
		}
	}
}