package me.blablubbabc.paintball.commands;

import java.util.LinkedHashMap;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.blablubbabc.paintball.ArenaManager;
import me.blablubbabc.paintball.Lobby;
import me.blablubbabc.paintball.Paintball;

public class CmdArena {
	private Paintball plugin;
	private ArenaManager am;

	private ChatColor gray = ChatColor.GRAY;
	private ChatColor green = ChatColor.GREEN;
	private ChatColor aqua = ChatColor.AQUA;
	private ChatColor yellow = ChatColor.YELLOW;
	private ChatColor red = ChatColor.RED;
	private ChatColor gold = ChatColor.GOLD;

	public CmdArena(Paintball pl, ArenaManager a) {
		plugin = pl;
		am = a;
	}

	public boolean command(CommandSender sender, String[] args) {
		if(sender instanceof Player) {
			if(!sender.isOp() && !sender.hasPermission("paintball.arena")) {
				sender.sendMessage(red+"No permission.");
				return true;
			}
			Player player = (Player) sender;
			if(args[1].equalsIgnoreCase("list")) {
				//list
				LinkedHashMap<String, Object> arenas = am.getArenaData();
				player.sendMessage(aqua+""+ ChatColor.BOLD+"["+yellow+""+ ChatColor.BOLD+"Paintball Arenas: "+gray+arenas.size() +aqua+""+ ChatColor.BOLD+"] ");
				for(String name : arenas.keySet()) {
					String ready = "";
					if(am.isReady(name)) ready = green+" |ready|";
					else ready = red+" |not ready|";
					player.sendMessage(gray+"- "+name+ready);
				}
				return true;
			} else {
				String name = args[1];
				if(!am.existing(name)) {
					if(args.length == 2) {
						am.addArena(name);
						player.sendMessage(green + "New arena created: " + yellow + name);
						return true;
					} else {
						player.sendMessage(red + "Arena not found: " + yellow + name);
						return true;
					}
				} else if(args.length == 2) {
					player.sendMessage(green + "Arena "+ yellow + name +green+" already exists!");
					return true;
				}
				//Existing:
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				if(args[2].equalsIgnoreCase("info")) {
					LinkedHashMap<String, Object> arena = am.getArena(name);
					String ready = "";
					if(am.isReady(name)) ready = green+" |ready|";
					else ready = red+" |not ready|";
					player.sendMessage(aqua+""+ ChatColor.BOLD+"["+yellow+""+ ChatColor.BOLD+"Paintball Arena: "+green+name+ready+aqua+""+ ChatColor.BOLD+"] ");
					player.sendMessage(aqua+"Size: "+yellow+arena.get("size"));
					player.sendMessage(aqua+"Played Rounds: "+yellow+arena.get("rounds"));
					player.sendMessage(aqua+"Frags: "+yellow+arena.get("kills"));
					player.sendMessage(aqua+"Fired Shots: "+yellow+arena.get("shots"));
					player.sendMessage(aqua+"Blue Spawns: "+yellow+am.getBlueSpawnsSize(name));
					player.sendMessage(aqua+"Red Spawns: "+yellow+am.getRedSpawnsSize(name));
					player.sendMessage(aqua+"Spectator Spawns: "+yellow+am.getSpecSpawnsSize(name));
					if(!am.isReady(name)) {
						player.sendMessage(red+"Needs following to be marked as ready:");
						if(am.inUse(name)) player.sendMessage(gray+"- finish current match at this arena");
						if(!am.pvpEnabled(name)) player.sendMessage(gray+"- enable PvP in all worlds where spawns can be found");
						if(am.getRedSpawnsSize(name) == 0) player.sendMessage(gray+"- 1 red spawn");
						if(am.getBlueSpawnsSize(name) == 0) player.sendMessage(gray+"- 1 blue spawn");
						if(am.getSpecSpawnsSize(name) == 0) player.sendMessage(gray+"- 1 spectator spawn");
					}

					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[2].equalsIgnoreCase("blue")) {
					am.addBlueSpawn(name, player.getLocation());
					am.saveData();
					player.sendMessage(green+"Blue spawn added. Number of blue spawns now: "+Lobby.BLUE.color()+am.getBlueSpawnsSize(name));
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[2].equalsIgnoreCase("red")) {
					am.addRedSpawn(name, player.getLocation());
					am.saveData();
					player.sendMessage(green+"Red spawn added. Number of red spawns now: "+Lobby.RED.color()+am.getRedSpawnsSize(name));
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[2].equalsIgnoreCase("spec")) {
					am.addSpecSpawn(name, player.getLocation());
					am.saveData();
					player.sendMessage(green+"Spectator spawn added. Number of spactator spawns now: "+Lobby.SPECTATE.color()+am.getSpecSpawnsSize(name));
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[2].equalsIgnoreCase("remove")) {
					am.remove(name);
					am.saveData();
					player.sendMessage(green+"Arena "+gray+name+green+" removed.");
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[2].equalsIgnoreCase("delblue")) {
					int num = am.getBlueSpawnsSize(name);
					am.removeBlueSpawns(name);
					am.saveData();
					player.sendMessage(gold+""+num+Lobby.BLUE.color()+" blue "+green+"spawns removed in arena "+gray+name);
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[2].equalsIgnoreCase("delred")) {
					int num = am.getRedSpawnsSize(name);
					am.removeRedSpawns(name);
					am.saveData();
					player.sendMessage(gold+""+num+Lobby.RED.color()+" red "+green+"spawns removed in arena "+gray+name);
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[2].equalsIgnoreCase("delspec")) {
					int num = am.getSpecSpawnsSize(name);
					am.removeSpecSpawns(name);
					am.saveData();
					player.sendMessage(gold+""+num+Lobby.SPECTATE.color()+" spactator "+green+"spawns removed in arena "+gray+name);
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[2].equalsIgnoreCase("size")) {
					if(args.length == 4) {
						try {
							int size = Integer.parseInt(args[3]);
							am.setSize(name, size);
						}catch(Exception e) {
							player.sendMessage(red+"Invalid number.");
							return true;
						}
					}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				}
			}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else {
			plugin.log("This command cannot be used in console.");
			return true;
		}
		return false;
	}
}
