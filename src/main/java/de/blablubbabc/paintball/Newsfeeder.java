/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.blablubbabc.paintball.utils.KeyValuePair;
import de.blablubbabc.paintball.utils.Translator;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

public class Newsfeeder {
	private Paintball plugin;
	private String pluginName;
	private String feedColor;
	
	public Newsfeeder(Paintball pl) {
		plugin = pl;
		pluginName = Translator.getString("PLUGIN");
		feedColor = Translator.getString("FEED_COLOR");
	}
	
	public String getPluginName() {
		return pluginName;
	}
	
	public String getFeedColor() {
		return feedColor;
	}
	
	//METHODS
	public void join(String name) {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("player", name);
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(Translator.getString("LOBBY_JOIN", vars));
		}
	}
	public void leave(String name) {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("player", name);
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(Translator.getString("LOBBY_LEAVE", vars));
		}
	}
	
	public void tip(String message) {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("message", message);
		for(Player player : Lobby.LOBBY.getMembers()) {
			player.sendMessage(Translator.getString("TIP", vars));
		}
	}
	
	public void counter(int counter) {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("seconds", String.valueOf(counter));
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(Translator.getString("COUNTDOWN", vars));
		}
	}
	
	public void roundTime(int time) {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("seconds", String.valueOf(time));
		for (Player player : Lobby.LOBBY.getMembers()) {
			player.sendMessage(Translator.getString("MATCH_REMAINING_TIME", vars));
		}
	}
	
	public void text(String message) {
		text(false, message);
	}
	
	public void textUntoggled(String message) {
		textUntoggled(false, message);
	}
	
	public void text(CommandSender sender, String message) {
		text(sender, false, message);
	}
	
	public void textPrefixed(String message) {
		text(true, message);
	}
	
	public void textUntoggledPrefixed(String message) {
		textUntoggled(true, message);
	}
	
	public void textPrefixed(CommandSender sender, String message) {
		text(sender, true, message);
	}
	
	private void text(boolean withPaintballPrefix, String message) {
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) text(player, withPaintballPrefix, message);
		}
	}
	
	private void textUntoggled(boolean withPaintballPrefix, String message) {
		for(Player player : Lobby.LOBBY.getMembers()) {
			text(player, withPaintballPrefix, message);
		}
	}
	
	private void text(CommandSender sender, boolean withPaintballPrefix, String message) {
		if (withPaintballPrefix) {
			sender.sendMessage(pluginName + " " + Translator.getString("TEXT", new KeyValuePair("plugin", pluginName), new KeyValuePair("message", message)));
		} else {
			sender.sendMessage(Translator.getString("TEXT", new KeyValuePair("plugin", pluginName), new KeyValuePair("message", message)));
		}
	}
	
	public void clickableText(CommandSender sender, String command, String message) {
		String formatted = Translator.getString("TEXT", new KeyValuePair("plugin", pluginName), new KeyValuePair("message", message));
		TextComponent component = new TextComponent(TextComponent.fromLegacyText(formatted));
		component.setClickEvent(new ClickEvent(Action.RUN_COMMAND, command));
		sender.spigot().sendMessage(component);
	}
	
	public void status(String message) {
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) status(player, message);
		}
	}
	
	public String happyhour(int seconds) {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("time", String.valueOf(seconds));
		return Translator.getString("HAPPYHOUR_TIME", vars);
	}
	
	public void status(CommandSender sender, String message) {
		sender.sendMessage(Translator.getString("MATCH_STATUS", new KeyValuePair("plugin", pluginName), new KeyValuePair("message", message)));
	}
	
	public void players() {
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) players(player);
		}
	}
	public void players(CommandSender sender) {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("needed_players_overview", getNeededPlayers());
		sender.sendMessage(Translator.getString("WAITING_PLAYERS_OVERVIEW", vars));
	}
	public String getPlayersOverview() {
		Map<String, String> vars = new HashMap<String, String>();
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
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("players", String.valueOf(players));
		vars.put("needed_players", String.valueOf(plugin.minPlayers));
		vars.put("players_overview", getPlayersOverview());
		String info = Translator.getString("NEEDED_PLAYERS_OVERVIEW", vars);
		return info;
	}
	
	public void death(Player target, Match match) {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("target_color", match.getTeamLobby(target).color().toString());
		vars.put("target", target.getName());
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(Translator.getString("PLAYER_DIED", vars));
		}
	}
	
	public void afkLeave(Player target, Match match) {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("plugin", pluginName);
		vars.put("player_color", match.getTeamLobby(target).color().toString());
		vars.put("player", target.getName());
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(Translator.getString("PLAYER_AFK", vars));
		}
	}
}
