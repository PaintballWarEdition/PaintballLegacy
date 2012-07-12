package me.blablubbabc.paintball.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import me.blablubbabc.paintball.Lobby;
import me.blablubbabc.paintball.Paintball;


public class CmdAdmin {
	private Paintball plugin;

	private ChatColor gray = ChatColor.GRAY;
	private ChatColor green = ChatColor.GREEN;
	//private ChatColor aqua = ChatColor.AQUA;
	private ChatColor yellow = ChatColor.YELLOW;
	private ChatColor red = ChatColor.RED;
	private ChatColor gold = ChatColor.GOLD;
	private ChatColor purple = ChatColor.LIGHT_PURPLE;

	public CmdAdmin(Paintball pl) {
		plugin = pl;
	}

	public boolean command(CommandSender sender, String[] args) {
		if(sender instanceof Player) {
			if(!sender.isOp() && !sender.hasPermission("paintball.admin")) {
				sender.sendMessage(red+"No permission.");
				return true;
			}
			Player player = (Player) sender;
			//player commands:
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			if(args[1].equalsIgnoreCase("lobby")) {
				if(args.length == 3) {
					if(args[2].equalsIgnoreCase("spawn")) {
						plugin.addLobbySpawn(player.getLocation());
						player.sendMessage(Lobby.LOBBY.color() + "Lobby"+green+" spawn added.");
						return true;
					} else if(args[2].equalsIgnoreCase("remove")) {
						plugin.deleteLobbySpawns();
						player.sendMessage(Lobby.LOBBY.color() + "Lobby"+green+" spawns removed.");
						return true;
					}
				}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			} else if(args[1].equalsIgnoreCase("cash")) {
				if(args.length == 3) {
					plugin.stats.sendCash(player, args[2]);
					return true;
				} else if(args.length == 4) {
					try {
						int money = Integer.parseInt(args[3]);
						plugin.pm.addMoney(args[2], money);
						plugin.pm.saveData();
					} catch(Exception e) {
						player.sendMessage(red + "No valid value entered!");
					}
					return true;
				}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			} else if(args[1].equalsIgnoreCase("rank")) {
				if(args.length == 3) {
					plugin.stats.sendRank(player, args[2]);
					return true;
				} else if(args.length == 4) {
					try {
						int points = Integer.parseInt(args[3]);
						plugin.pm.addPoints(args[2], points);
						plugin.pm.saveData();
					} catch(Exception e) {
						player.sendMessage(red + "No valid value entered!");
					}
					return true;
				}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			} else if(args[1].equalsIgnoreCase("helmet")) {
				if(args.length == 3) {
					if(args[2].equalsIgnoreCase("blue")) {
						ItemStack is = player.getItemInHand();
						Lobby.BLUE.setHelmet(is.getType(), is.getData().getData());
						Lobby.BLUE.saveData();
						return true;
					} else if(args[2].equalsIgnoreCase("red")) {
						ItemStack is = player.getItemInHand();
						Lobby.RED.setHelmet(is.getType(), is.getData().getData());
						Lobby.RED.saveData();
						return true;
					} else if(args[2].equalsIgnoreCase("spec") || args[2].equalsIgnoreCase("spectator")) {
						ItemStack is = player.getItemInHand();
						Lobby.SPECTATE.setHelmet(is.getType(), is.getData().getData());
						Lobby.SPECTATE.saveData();
						return true;
					}
				}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			} else if(args[1].equalsIgnoreCase("reset")) {
				if(args.length == 3 && args[2].equalsIgnoreCase("all")) {
					//reload:
					plugin.active = false;
					plugin.reload();
					sender.sendMessage(green+"Reload finished.");
					//playerstats löschen
					plugin.pm.resetData();
					sender.sendMessage(red+"All"+green+" stats have been reset!");
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
						sender.sendMessage(green+"Stats of player "+gray+name+green+" have been reset!");
					} else {
						sender.sendMessage(gray + "Player " + args[2] + " not found.");
					}
					return true;
				} else if(args.length == 4) {
					if(plugin.pm.exists(args[2])) {
						if(plugin.pm.possibleValues.contains(args[3])) {
							plugin.pm.setIntValue(args[2], args[3], 0);
							plugin.pm.saveData();
							sender.sendMessage(gold+args[3]+green+" of player "+gray+args[2]+green+" have been reset!");
						} else {
							String values = "";
							for(String s : plugin.pm.possibleValues) {
								values += s + ",";
							}
							if(values.length() > 1) values.substring(0, (values.length() -1));

							sender.sendMessage(gray + "Value not found. Try: "+values);
						}
					} else {
						sender.sendMessage(gray + "Player " + args[2] + " not found.");
					}
					return true;
				}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			} else if(args[1].equalsIgnoreCase("next")) {
				if(args.length == 3) {
					String arena = args[2];
					if(!plugin.am.existing(arena)) {
						sender.sendMessage(red + "This arena does not exist!");
						return true;
					}
					if(!plugin.am.inUse(arena) && !plugin.am.isReady(arena)) {
						sender.sendMessage(red + "This arena is not ready!");
						return true;
					}
					plugin.am.setNext(arena);
					plugin.nf.text(plugin.nf.pluginName+purple+"Tries to force next arena to be "+yellow+arena );
					return true;
				}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			} else if(args[1].equalsIgnoreCase("disable")) {
				String status = "";
				if(plugin.active) {
					plugin.active = false;
					status = "disabled";
				} else {
					plugin.active = true;
					status = "activated";
				}
				sender.sendMessage(green+"Paintball matches are now " +yellow+ status);
				return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			} else if(args[1].equalsIgnoreCase("reload")) {
				//neue matches verhindern
				plugin.active = false;
				plugin.reload();
				sender.sendMessage(green+"Reload finished.");
				return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			} else if(args[1].equalsIgnoreCase("softreload")) {
				//neue matches verhindern
				plugin.active = false;
				plugin.softreload = true;
				//message:
				plugin.nf.status(purple + "Paintball plugin is reloaded soon. New matches diabled. You will be kicked from the lobby soon..");
				//check
				sender.sendMessage(green+"Reload will be done when all matches are over..");
				plugin.mm.softCheck();
				return true;
			}
			//console-commands:
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else if(args[1].equalsIgnoreCase("reset")) {
			if(args.length == 3 && args[2].equalsIgnoreCase("all")) {
				//reload:
				plugin.active = false;
				plugin.reload();
				sender.sendMessage(green+"Reload finished.");
				//playerstats löschen
				plugin.pm.resetData();
				sender.sendMessage(red+"All"+green+" stats have been reset!");
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
					sender.sendMessage(green+"Stats of player "+gray+name+green+" have been reset!");
				} else {
					sender.sendMessage(gray + "Player " + args[2] + " not found.");
				}
				return true;
			} else if(args.length == 4) {
				if(plugin.pm.exists(args[2])) {
					if(plugin.pm.possibleValues.contains(args[3])) {
						plugin.pm.setIntValue(args[2], args[3], 0);
						plugin.pm.saveData();
						sender.sendMessage(gold+args[3]+green+" of player "+gray+args[2]+green+" have been reset!");
					} else {
						String values = "";
						for(String s : plugin.pm.possibleValues) {
							values += s + ",";
						}
						if(values.length() > 1) values.substring(0, (values.length() -1));

						sender.sendMessage(gray + "Value not found. Try: "+values);
					}
				} else {
					sender.sendMessage(gray + "Player " + args[2] + " not found.");
				}
				return true;
			}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else if(args[1].equalsIgnoreCase("next")) {
			if(args.length == 3) {
				String arena = args[2];
				if(!plugin.am.existing(arena)) {
					sender.sendMessage(red + "This arena does not exist!");
					return true;
				}
				if(!plugin.am.inUse(arena) && !plugin.am.isReady(arena)) {
					sender.sendMessage(red + "This arena is not ready!");
					return true;
				}
				plugin.am.setNext(arena);
				plugin.nf.text(plugin.nf.pluginName+purple+"Tries to force next arena to be "+yellow+arena );
				return true;
			}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else if(args[1].equalsIgnoreCase("disable")) {
			String status = "";
			if(plugin.active) {
				plugin.active = false;
				status = "disabled";
			} else {
				plugin.active = true;
				status = "activated";
			}
			sender.sendMessage(green+"Paintball matches are now " +yellow+ status);
			return true;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else if(args[1].equalsIgnoreCase("reload")) {
			//neue matches verhindern
			plugin.active = false;
			plugin.reload();
			sender.sendMessage(green+"Reload finished.");
			return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else if(args[1].equalsIgnoreCase("softreload")) {
			//neue matches verhindern
			plugin.active = false;
			plugin.softreload = true;
			//message:
			plugin.nf.status(purple + "Paintball plugin is reloaded soon. New matches diabled. You will be kicked from the lobby soon..");
			//check
			sender.sendMessage(green+"Reload will be done when all matches are over..");
			plugin.mm.softCheck();
			return true;
		} else {
			plugin.log("This command cannot be used in console.");
			return true;
		}
		return false;
	}	
}