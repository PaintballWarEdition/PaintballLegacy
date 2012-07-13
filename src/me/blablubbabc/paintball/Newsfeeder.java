package me.blablubbabc.paintball;

import org.bukkit.entity.Player;


public class Newsfeeder {
	private Paintball plugin;
	
	public String pluginName;
	
	public Newsfeeder(Paintball pl) {
		plugin = pl;
		pluginName = plugin.aqua+""+ plugin.bold+"["+plugin.yellow+""+ plugin.bold+"Paintball"+plugin.aqua+""+ plugin.bold+"] ";
	}
	
	
	
	//METHODS
	public void join(String name) {
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(pluginName+plugin.gold + name + plugin.green + " joined the lobby.");
		}
	}
	public void leave(String name) {
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(pluginName+plugin.gold + name + plugin.gray + " left the lobby.");
		}
	}
	
	public void tip(String message) {
		for(Player player : Lobby.LOBBY.getMembers()) {
			player.sendMessage("[Tip]" + plugin.gold + message);
		}
	}
	
	public void counter(int counter) {
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(plugin.light_purple+"Countdown:"+plugin.aqua+" Match starts in " + plugin.gold + counter + plugin.aqua + " seconds!");
		}
	}
	
	public void text(String message) {
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(plugin.aqua+ message);
		}
	}
	
	public void status(String message) {
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(plugin.aqua+""+"Match status: " + plugin.gold + message);
		}
	}
	public void players() {
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(plugin.aqua+""+"Waiting players: " + getPlayers());
		}
	}
	public String getPlayers() {
		int players = Lobby.RED.numberWaiting() + Lobby.BLUE.numberWaiting() + Lobby.RANDOM.numberWaiting();
		String info = plugin.gold+" ("+plugin.aqua+players+plugin.gold+" ["+Lobby.RED.color()+Lobby.RED.numberWaiting()+plugin.gold+"]["+Lobby.BLUE.color()+Lobby.BLUE.numberWaiting()+plugin.gold+"]["+Lobby.RANDOM.color()+Lobby.RANDOM.numberWaiting()+plugin.gold+"]["+Lobby.SPECTATE.color()+Lobby.SPECTATE.numberWaiting()+plugin.gold+"]"+" / "+plugin.aqua+plugin.minPlayers+plugin.gold+")";
		return info;
	}
	
	public void feed(Player target, Player killer, Match match) {
		for(Player player : Lobby.LOBBY.getMembers()) {
			if(!Lobby.toggledFeed(player)) player.sendMessage(pluginName + Lobby.getTeam(match.getTeamName(killer)).color() + killer.getName() + plugin.aqua + " fragged " + Lobby.getTeam(match.getTeamName(target)).color() + target.getName());
		}
	}
}
