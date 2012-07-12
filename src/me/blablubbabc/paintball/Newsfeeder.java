package me.blablubbabc.paintball;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;


public class Newsfeeder {
	private Paintball plugin;
	
	private ChatColor gray = ChatColor.GRAY;
	private ChatColor green = ChatColor.GREEN;
	private ChatColor aqua = ChatColor.AQUA;
	private ChatColor red = ChatColor.RED;
	private ChatColor blue = ChatColor.BLUE;
	private ChatColor yellow = ChatColor.YELLOW;
	private ChatColor gold = ChatColor.GOLD;
	public String pluginName = aqua+""+ ChatColor.BOLD+"["+yellow+""+ ChatColor.BOLD+"Paintball"+aqua+""+ ChatColor.BOLD+"] ";
	
	public Newsfeeder(Paintball pl) {
		plugin = pl;
	}
	
	
	
	//METHODS
	public void join(String name) {
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(pluginName+gold + name + green + " joined the lobby.");
		}
	}
	public void leave(String name) {
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(pluginName+gold + name + gray + " left the lobby.");
		}
	}
	
	public void tip(String message) {
		for(Player player : Lobby.LOBBY.getMembers()) {
			player.sendMessage("[Tip]" + gold + message);
		}
	}
	
	public void counter(int counter) {
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(ChatColor.LIGHT_PURPLE+"Countdown:"+aqua+" Match starts in " + gold + counter + aqua + " seconds!");
		}
	}
	
	public void text(String message) {
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(aqua+ message);
		}
	}
	
	public void status(String message) {
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(aqua+""+"Match status: " + gold + message);
		}
	}
	public void players() {
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(aqua+""+"Waiting players: " + getPlayers());
		}
	}
	public String getPlayers() {
		int players = Lobby.RED.numberWaiting() + Lobby.BLUE.numberWaiting() + Lobby.RANDOM.numberWaiting();
		String info = gold+" ("+aqua+players+gold+" ["+red+Lobby.RED.numberWaiting()+gold+"]["+blue+Lobby.BLUE.numberWaiting()+gold+"]["+green+Lobby.RANDOM.numberWaiting()+gold+"]["+yellow+Lobby.SPECTATE.numberWaiting()+gold+"]"+" / "+aqua+plugin.minPlayers+gold+")";
		return info;
	}
	
	public void feed(Player target, Player killer, Match match) {
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(pluginName + Lobby.getTeam(match.getTeamName(killer)).color() + killer.getName() + aqua + " fragged " + Lobby.getTeam(match.getTeamName(target)).color() + target.getName());
		}
	}
}
