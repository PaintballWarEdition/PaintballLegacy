package me.blablubbabc.paintball.commands;

import java.util.LinkedHashMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.blablubbabc.paintball.ArenaManager;
import me.blablubbabc.paintball.Lobby;
import me.blablubbabc.paintball.Paintball;

public class CmdArena {
	private Paintball plugin;
	private ArenaManager am;

	public CmdArena(Paintball pl, ArenaManager a) {
		plugin = pl;
		am = a;
	}

	public boolean command(CommandSender sender, String[] args) {
		if(sender instanceof Player) {
			if(!sender.isOp() && !sender.hasPermission("paintball.arena")) {
				sender.sendMessage(plugin.red+"No permission.");
				return true;
			}
			Player player = (Player) sender;
			if(args[1].equalsIgnoreCase("list")) {
				//list
				LinkedHashMap<String, Object> arenas = am.getArenaData();
				player.sendMessage(plugin.aqua+""+ plugin.bold+"["+plugin.yellow+""+ plugin.bold+"Paintball Arenas: "+plugin.gray+arenas.size() +plugin.aqua+""+ plugin.bold+"] ");
				for(String name : arenas.keySet()) {
					String ready = "";
					if(am.isReady(name)) ready = plugin.green+" |ready|";
					else ready = plugin.red+" |not ready|";
					player.sendMessage(plugin.gray+"- "+name+ready);
				}
				return true;
			} else {
				String name = args[1];
				if(!am.existing(name)) {
					if(args.length == 2) {
						am.addArena(name);
						player.sendMessage(plugin.green + "New arena created: " + plugin.yellow + name);
						return true;
					} else {
						player.sendMessage(plugin.red + "Arena not found: " + plugin.yellow + name);
						return true;
					}
				} else if(args.length == 2) {
					player.sendMessage(plugin.green + "Arena "+ plugin.yellow + name +plugin.green+" already exists!");
					return true;
				}
				//Existing:
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				if(args[2].equalsIgnoreCase("info")) {
					LinkedHashMap<String, Object> arena = am.getArena(name);
					String ready = "";
					if(am.isReady(name)) ready = plugin.green+" |ready|";
					else ready = plugin.red+" |not ready|";
					player.sendMessage(plugin.aqua+""+ plugin.bold+"["+plugin.yellow+""+ plugin.bold+"Paintball Arena: "+plugin.green+name+ready+plugin.aqua+""+ plugin.bold+"] ");
					player.sendMessage(plugin.aqua+"Size: "+plugin.yellow+arena.get("size"));
					player.sendMessage(plugin.aqua+"Played Rounds: "+plugin.yellow+arena.get("rounds"));
					player.sendMessage(plugin.aqua+"Frags: "+plugin.yellow+arena.get("kills"));
					player.sendMessage(plugin.aqua+"Fiplugin.red Shots: "+plugin.yellow+arena.get("shots"));
					player.sendMessage(plugin.aqua+"Blue Spawns: "+plugin.yellow+am.getBlueSpawnsSize(name));
					player.sendMessage(plugin.aqua+"plugin.red Spawns: "+plugin.yellow+am.getRedSpawnsSize(name));
					player.sendMessage(plugin.aqua+"Spectator Spawns: "+plugin.yellow+am.getSpecSpawnsSize(name));
					if(!am.isReady(name)) {
						player.sendMessage(plugin.red+"Needs following to be marked as ready:");
						if(am.inUse(name)) player.sendMessage(plugin.gray+"- finish current match at this arena");
						if(!am.pvpEnabled(name)) player.sendMessage(plugin.gray+"- enable PvP in all worlds where spawns can be found");
						if(am.getRedSpawnsSize(name) == 0) player.sendMessage(plugin.gray+"- 1 plugin.red spawn");
						if(am.getBlueSpawnsSize(name) == 0) player.sendMessage(plugin.gray+"- 1 blue spawn");
						if(am.getSpecSpawnsSize(name) == 0) player.sendMessage(plugin.gray+"- 1 spectator spawn");
					}

					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[2].equalsIgnoreCase("blue")) {
					am.addBlueSpawn(name, player.getLocation());
					am.saveData();
					player.sendMessage(Lobby.BLUE.color()+"Blue"+plugin.green+" spawn added. Number of blue spawns now: "+Lobby.BLUE.color()+am.getBlueSpawnsSize(name));
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[2].equalsIgnoreCase("red")) {
					am.addRedSpawn(name, player.getLocation());
					am.saveData();
					player.sendMessage(Lobby.RED.color()+"Red"+plugin.green+" spawn added. Number of red spawns now: "+Lobby.RED.color()+am.getRedSpawnsSize(name));
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[2].equalsIgnoreCase("spec")) {
					am.addSpecSpawn(name, player.getLocation());
					am.saveData();
					player.sendMessage(Lobby.SPECTATE.color()+"Spectator"+plugin.green+" spawn added. Number of spactator spawns now: "+Lobby.SPECTATE.color()+am.getSpecSpawnsSize(name));
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[2].equalsIgnoreCase("remove")) {
					am.remove(name);
					am.saveData();
					player.sendMessage(plugin.green+"Arena "+plugin.gray+name+plugin.green+" removed.");
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[2].equalsIgnoreCase("delblue")) {
					int num = am.getBlueSpawnsSize(name);
					am.removeBlueSpawns(name);
					am.saveData();
					player.sendMessage(plugin.gold+""+num+Lobby.BLUE.color()+" blue "+plugin.green+"spawns removed in arena "+plugin.gray+name);
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[2].equalsIgnoreCase("delred")) {
					int num = am.getRedSpawnsSize(name);
					am.removeRedSpawns(name);
					am.saveData();
					player.sendMessage(plugin.gold+""+num+Lobby.RED.color()+" red "+plugin.green+"spawns removed in arena "+plugin.gray+name);
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[2].equalsIgnoreCase("delspec")) {
					int num = am.getSpecSpawnsSize(name);
					am.removeSpecSpawns(name);
					am.saveData();
					player.sendMessage(plugin.gold+""+num+Lobby.SPECTATE.color()+" spactator "+plugin.green+"spawns removed in arena "+plugin.gray+name);
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[2].equalsIgnoreCase("size")) {
					if(args.length == 4) {
						try {
							int size = Integer.parseInt(args[3]);
							am.setSize(name, size);
						}catch(Exception e) {
							player.sendMessage(plugin.red+"Invalid number.");
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
