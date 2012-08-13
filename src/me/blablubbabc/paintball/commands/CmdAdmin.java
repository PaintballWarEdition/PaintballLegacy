package me.blablubbabc.paintball.commands;

import java.util.HashMap;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import me.blablubbabc.paintball.Lobby;
import me.blablubbabc.paintball.Paintball;


public class CmdAdmin {
	private Paintball plugin;


	public CmdAdmin(Paintball pl) {
		plugin = pl;
	}

	public boolean command(CommandSender sender, String[] args) {
		if(sender instanceof Player) {
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
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			} else if(args[1].equalsIgnoreCase("helmet")) {
				if(args.length == 3) {
					if(args[2].equalsIgnoreCase("blue")) {
						ItemStack is = player.getItemInHand();
						Lobby.BLUE.setHelmet(is.getType(), is.getData().getData());
						Lobby.BLUE.saveData();
						player.sendMessage(plugin.t.getString("HELMET_SET"));
						return true;
					} else if(args[2].equalsIgnoreCase("red")) {
						ItemStack is = player.getItemInHand();
						Lobby.RED.setHelmet(is.getType(), is.getData().getData());
						Lobby.RED.saveData();
						player.sendMessage(plugin.t.getString("HELMET_SET"));
						return true;
					} else if(args[2].equalsIgnoreCase("spec") || args[2].equalsIgnoreCase("spectator")) {
						ItemStack is = player.getItemInHand();
						Lobby.SPECTATE.setHelmet(is.getType(), is.getData().getData());
						Lobby.SPECTATE.saveData();
						player.sendMessage(plugin.t.getString("HELMET_SET"));
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
				//reload:
				plugin.active = false;
				plugin.reload();
				sender.sendMessage(plugin.t.getString("REALOAD_FINISHED"));
				//playerstats löschen
				plugin.pm.resetData();
				sender.sendMessage(plugin.t.getString("ALL_STATS_RESET"));
			} else if(args.length == 3) {
				if(plugin.pm.exists(args[2])) {
					String name = args[2];
					//reset all values:
					plugin.pm.setDeaths(name, 0);
					plugin.pm.setKills(name, 0);
					plugin.pm.setLooses(name, 0);
					plugin.pm.setWins(name, 0);
					plugin.pm.setMoney(name, 0);
					plugin.pm.setPoints(name, 0);
					plugin.pm.setShots(name, 0);
					plugin.pm.saveData();
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
					if(plugin.pm.possibleValues.contains(args[3])) {
						plugin.pm.setIntValue(args[2], args[3], 0);
						plugin.pm.saveData();
						HashMap<String, String> vars = new HashMap<String, String>();
						vars.put("player", args[2]);
						vars.put("stat", args[3]);
						sender.sendMessage(plugin.t.getString("PLAYER_STAT_RESET", vars));
					} else {
						String values = "";
						for(String s : plugin.pm.possibleValues) {
							values += s + ",";
						}
						if(values.length() > 1) values.substring(0, (values.length() -1));
						HashMap<String, String> vars = new HashMap<String, String>();
						vars.put("values", values);
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
		} else if(args[1].equalsIgnoreCase("cash")) {
			if(args.length == 3) {
				plugin.stats.sendCash(sender, args[2]);
				return true;
			} else if(args.length == 4) {
				try {
					int money = Integer.parseInt(args[3]);
					plugin.pm.addMoney(args[2], money);
					plugin.pm.saveData();
					HashMap<String,String> vars = new HashMap<String, String>();
					vars.put("player", args[2]);
					vars.put("value", String.valueOf(money));
					sender.sendMessage(plugin.t.getString("PLAYER_RECEIVED_VALUE", vars));
				} catch(Exception e) {
					sender.sendMessage(plugin.t.getString("INVALID_NUMBER"));
				}
				return true;
			}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else if(args[1].equalsIgnoreCase("rank")) {
			if(args.length == 3) {
				plugin.stats.sendRank(sender, args[2]);
				return true;
			} else if(args.length == 4) {
				try {
					int points = Integer.parseInt(args[3]);
					plugin.pm.addPoints(args[2], points);
					plugin.pm.saveData();
					HashMap<String,String> vars = new HashMap<String, String>();
					vars.put("player", args[2]);
					vars.put("value", String.valueOf(points));
					sender.sendMessage(plugin.t.getString("PLAYER_RECEIVED_VALUE", vars));
				} catch(Exception e) {
					sender.sendMessage(plugin.t.getString("INVALID_NUMBER"));
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
				plugin.nf.text(plugin.t.getString("NEXT_ARENA_SET", vars));
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
		} else {
			if(sender instanceof Player) return false;
			else sender.sendMessage(plugin.t.getString("COMMAND_UNKNOWN_OR_NOT_CONSOLE"));
			return true;
		}
		return false;
	}	
}