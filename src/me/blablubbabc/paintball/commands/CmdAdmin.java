package me.blablubbabc.paintball.commands;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.blablubbabc.paintball.Lobby;
import me.blablubbabc.paintball.Paintball;


public class CmdAdmin {
	private Paintball plugin;


	public CmdAdmin(Paintball pl) {
		plugin = pl;
	}

	public boolean command(CommandSender sender, String[] args) {
		if(sender instanceof Player) {
			//PERMISSION CHECK
			if(!sender.isOp() && !sender.hasPermission("paintball.admin")) {
				sender.sendMessage(plugin.t.getString("NO_PERMISSION"));
				return true;
			}
			Player player = (Player) sender;
			//player commands:
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			if(args[1].equalsIgnoreCase("lobby")) {
				if(args.length == 3) {
					if(args[2].equalsIgnoreCase("spawn")) {
						plugin.addLobbySpawn(player.getLocation());
						HashMap<String, String> vars = new HashMap<String, String>();
						vars.put("lobby_color", Lobby.LOBBY.color().toString());
						player.sendMessage(plugin.t.getString("LOBBY_NEW_SPAWN", vars));
						return true;
					} else if(args[2].equalsIgnoreCase("remove")) {
						plugin.deleteLobbySpawns();
						HashMap<String, String> vars = new HashMap<String, String>();
						vars.put("lobby_color", Lobby.LOBBY.color().toString());
						player.sendMessage(plugin.t.getString("LOBBY_SPAWNS_REMOVED", vars));
						return true;
					}
				}
			}
		}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//console_AND_player-commands:
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		if(args[1].equalsIgnoreCase("reset")) {
			if(args.length == 3 && args[2].equalsIgnoreCase("all")) {
				sender.sendMessage(plugin.t.getString("THIS_NEEDS_TIME"));
				long time1 = System.currentTimeMillis();
				plugin.pm.resetData();
				long time2 = System.currentTimeMillis();
				long delta = time2 - time1;
				
				int amount = plugin.pm.getPlayerCount();
				
				HashMap<String, String> vars = new HashMap<String, String>();
				vars.put("time", String.valueOf(delta));
				vars.put("amount", String.valueOf(amount));
				sender.sendMessage(plugin.t.getString("ALL_STATS_RESET", vars));
				return true;
			} else if(args.length == 3) {
				if(plugin.pm.exists(args[2])) {
					String name = args[2];
					plugin.pm.resetData(name);
					HashMap<String, String> vars = new HashMap<String, String>();
					vars.put("player", name);
					sender.sendMessage(plugin.t.getString("PLAYER_ALL_STATS_RESET", vars));
				} else {
					HashMap<String, String> vars = new HashMap<String, String>();
					vars.put("player", args[2]);
					sender.sendMessage(plugin.t.getString("PLAYER_NOT_FOUND", vars));
				}
				return true;
			} else if(args.length == 4) {
				if(plugin.pm.exists(args[2])) {
					if(plugin.sql.sqlPlayers.statsList.contains(args[3])) {
						HashMap<String, Integer> setStat = new HashMap<String, Integer>();
						setStat.put(args[3], 0);
						plugin.pm.setStats(args[2], setStat);
						HashMap<String, String> vars = new HashMap<String, String>();
						vars.put("player", args[2]);
						vars.put("stat", args[3]);
						sender.sendMessage(plugin.t.getString("PLAYER_STAT_RESET", vars));
					} else {
						HashMap<String, String> vars = new HashMap<String, String>();
						vars.put("values", plugin.sql.sqlPlayers.getStatsListString());
						sender.sendMessage(plugin.t.getString("VALUE_NOT_FOUND", vars));
					}
				} else {
					HashMap<String, String> vars = new HashMap<String, String>();
					vars.put("player", args[2]);
					sender.sendMessage(plugin.t.getString("PLAYER_NOT_FOUND", vars));
				}
				return true;
			}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else if(args[1].equalsIgnoreCase("set")) {
			if(args.length == 5) {
				if(plugin.pm.exists(args[2])) {
					if(plugin.sql.sqlPlayers.statsList.contains(args[3])) {
						try {
							String stat = args[3];
							int value = Integer.parseInt(args[4]);
							HashMap<String, Integer> setStat = new HashMap<String, Integer>();
							setStat.put(args[3], value);
							plugin.pm.setStats(args[2], setStat);
							HashMap<String,String> vars = new HashMap<String, String>();
							vars.put("player", args[2]);
							vars.put("stat", stat);
							vars.put("value", String.valueOf(value));
							sender.sendMessage(plugin.t.getString("PLAYER_STAT_SET", vars));
						} catch(Exception e) {
							sender.sendMessage(plugin.t.getString("INVALID_NUMBER"));
						}
					} else {
						HashMap<String, String> vars = new HashMap<String, String>();
						vars.put("values", plugin.sql.sqlPlayers.getStatsListString());
						sender.sendMessage(plugin.t.getString("VALUE_NOT_FOUND", vars));
					}
				} else {
					HashMap<String, String> vars = new HashMap<String, String>();
					vars.put("player", args[2]);
					sender.sendMessage(plugin.t.getString("PLAYER_NOT_FOUND", vars));
				}
				return true;
			}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else if(args[1].equalsIgnoreCase("add")) {
			if(args.length == 5) {
				if(plugin.pm.exists(args[2])) {
					if(plugin.sql.sqlPlayers.statsList.contains(args[3])) {
						try {
							String stat = args[3];
							int value = Integer.parseInt(args[4]);
							HashMap<String, Integer> setStat = new HashMap<String, Integer>();
							setStat.put(args[3], value);
							plugin.pm.addStats(args[2], setStat);
							HashMap<String,String> vars = new HashMap<String, String>();
							vars.put("player", args[2]);
							vars.put("stat", stat);
							vars.put("value", String.valueOf(value));
							sender.sendMessage(plugin.t.getString("PLAYER_STAT_ADDED", vars));
						} catch(Exception e) {
							sender.sendMessage(plugin.t.getString("INVALID_NUMBER"));
						}
					} else {
						HashMap<String, String> vars = new HashMap<String, String>();
						vars.put("values", plugin.sql.sqlPlayers.getStatsListString());
						sender.sendMessage(plugin.t.getString("VALUE_NOT_FOUND", vars));
					}
				} else {
					HashMap<String, String> vars = new HashMap<String, String>();
					vars.put("player", args[2]);
					sender.sendMessage(plugin.t.getString("PLAYER_NOT_FOUND", vars));
				}
				return true;
			}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else if(args[1].equalsIgnoreCase("stats")) {
			if(args.length == 3) {
				plugin.stats.sendStats(sender, args[2]);
				return true;
			}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else if(args[1].equalsIgnoreCase("rank")) {
			if(args.length >= 3) {
				if(plugin.pm.exists(args[2])) {
					if(args.length == 3) plugin.stats.sendRank(sender, args[2], "points");
					else if(args.length == 4) plugin.stats.sendRank(sender, args[2], args[3]);
					else return false;
				} else {
					HashMap<String, String> vars = new HashMap<String, String>();
					vars.put("player", args[2]);
					sender.sendMessage(plugin.t.getString("PLAYER_NOT_FOUND", vars));
				}
				return true;
			}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else if(args[1].equalsIgnoreCase("next")) {
			if(args.length == 3) {
				String arena = args[2];
				if(!plugin.am.existing(arena)) {
					HashMap<String, String> vars = new HashMap<String, String>();
					vars.put("arena", arena);
					sender.sendMessage(plugin.t.getString("ARENA_NOT_FOUND", vars));
					return true;
				}
				if(!plugin.am.inUse(arena) && !plugin.am.isReady(arena)) {
					sender.sendMessage(plugin.t.getString("ARENA_NOT_READY"));
					return true;
				}
				plugin.am.setNext(arena);
				HashMap<String, String> vars = new HashMap<String, String>();
				vars.put("plugin", plugin.nf.pluginName);
				vars.put("arena", arena);
				plugin.nf.text(sender, plugin.t.getString("NEXT_ARENA_SET", vars));
				return true;
			}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else if(args[1].equalsIgnoreCase("disable")) {
			String status = "";
			if(plugin.active) {
				plugin.active = false;
				status = plugin.t.getString("OFF");
			} else {
				plugin.active = true;
				status = plugin.t.getString("ON");
			}
			HashMap<String, String> vars = new HashMap<String, String>();
			vars.put("status", status);
			sender.sendMessage(plugin.t.getString("PLUGIN_STATUS", vars));
			return true;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else if(args[1].equalsIgnoreCase("reload")) {
			//neue matches verhindern
			plugin.active = false;
			plugin.reload();
			sender.sendMessage(plugin.t.getString("REALOAD_FINISHED"));
			return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else if(args[1].equalsIgnoreCase("softreload")) {
			//neue matches verhindern
			plugin.active = false;
			plugin.softreload = true;
			//message:
			plugin.nf.status(plugin.t.getString("SOFTRELOAD"));
			//check
			sender.sendMessage(plugin.t.getString("RELOAD_SOON"));
			plugin.mm.softCheck();
			return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else if(args[1].equalsIgnoreCase("random")) {
			//Toggle only random:
			if(plugin.onlyRandom) {
				plugin.onlyRandom = false;
				sender.sendMessage(plugin.t.getString("ONLY_RANDOM_OFF"));
			} else {
				plugin.onlyRandom = true;
				sender.sendMessage(plugin.t.getString("ONLY_RANDOM_ON"));
			}
			return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else if(args[1].equalsIgnoreCase("list")) {
			sender.sendMessage(plugin.t.getString("PLAYER_OVERVIEW"));
			for(Lobby l : Lobby.values()) {
				sender.sendMessage(l.color()+l.name()+" ( "+l.number()+" ):");
				for(Player p : l.getMembers()) {
					if(l != Lobby.LOBBY) sender.sendMessage(ChatColor.GRAY+p.getName()+ChatColor.WHITE+" : "+(Lobby.isPlaying(p) ? ((l == Lobby.SPECTATE) ? plugin.t.getString("SPECTATING"):plugin.t.getString("PLAYING")):plugin.t.getString("WAITING")));
					if(l == Lobby.LOBBY && Lobby.getTeam(p) == Lobby.LOBBY) sender.sendMessage(ChatColor.GRAY+p.getName()+ChatColor.WHITE+" : "+plugin.t.getString("NOT_IN_TEAM"));
				}
			}
			return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else {
			if(sender instanceof Player) return false;
			else sender.sendMessage(plugin.t.getString("COMMAND_UNKNOWN_OR_NOT_CONSOLE"));
			return true;
		}
		return false;
	}	
}