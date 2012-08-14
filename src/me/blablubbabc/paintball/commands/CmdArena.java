package me.blablubbabc.paintball.commands;

import java.util.HashMap;
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
			//PERMISSION CHECK
			if(!sender.isOp() && !sender.hasPermission("paintball.arena")) {
				sender.sendMessage(plugin.t.getString("NO_PERMISSION"));
				return true;
			}
			Player player = (Player) sender;
			if(args[1].equalsIgnoreCase("list")) {
				//list
				LinkedHashMap<String, Object> arenas = am.getArenaData();
				HashMap<String, String> vars = new HashMap<String, String>();
				vars.put("arenas", String.valueOf(arenas.size()));
				player.sendMessage(plugin.t.getString("ARENA_LIST_HEADER", vars));
				for(String name : arenas.keySet()) {
					String ready = "";
					if(am.isReady(name)) ready = plugin.t.getString("ARENA_STATUS_READY");
					else ready = plugin.t.getString("ARENA_STATUS_NOT_READY");
					HashMap<String, String> vars2 = new HashMap<String, String>();
					vars2.put("arena", name);
					vars2.put("status", ready);
					player.sendMessage(plugin.t.getString("ARENA_LIST_ENTRY", vars2));
				}
				return true;
			} else {
				String name = args[1];
				HashMap<String, String> vars = new HashMap<String, String>();
				vars.put("arena", name);
				if(!am.existing(name)) {
					if(args.length == 2) {
						am.addArena(name);
						player.sendMessage(plugin.t.getString("ARENA_CREATED", vars));
						return true;
					} else {
						player.sendMessage(plugin.t.getString("ARENA_NOT_FOUND", vars));
						return true;
					}
				} else if(args.length == 2) {
					player.sendMessage(plugin.t.getString("ARENA_ALREADY_EXISTS", vars));
					return true;
				}
				//Existing:
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				if(args[2].equalsIgnoreCase("info")) {
					LinkedHashMap<String, Object> arena = am.getArena(name);
					String ready = "";
					if(am.isReady(name)) ready = plugin.t.getString("ARENA_STATUS_READY");
					else ready = plugin.t.getString("ARENA_STATUS_NOT_READY");
					vars.put("status", ready);
					vars.put("size", String.valueOf(arena.get("size")));
					vars.put("rounds", String.valueOf(arena.get("rounds")));
					vars.put("kills", String.valueOf(arena.get("kills")));
					vars.put("shots", String.valueOf(arena.get("shots")));
					player.sendMessage(plugin.t.getString("ARENA_INFO_HEADER", vars));
					player.sendMessage(plugin.t.getString("ARENA_INFO_SIZE", vars));
					player.sendMessage(plugin.t.getString("ARENA_INFO_ROUNDS", vars));
					player.sendMessage(plugin.t.getString("ARENA_INFO_KILLS", vars));
					player.sendMessage(plugin.t.getString("ARENA_INFO_SHOTS", vars));
					
					vars.put("team", String.valueOf(Lobby.BLUE.getName()));
					vars.put("spawns", String.valueOf(am.getBlueSpawnsSize(name)));
					player.sendMessage(plugin.t.getString("ARENA_INFO_SPAWNS", vars));
					
					vars.put("team", String.valueOf(Lobby.RED.getName()));
					vars.put("spawns", String.valueOf(am.getRedSpawnsSize(name)));
					player.sendMessage(plugin.t.getString("ARENA_INFO_SPAWNS", vars));
					
					vars.put("team", String.valueOf(Lobby.SPECTATE.getName()));
					vars.put("spawns", String.valueOf(am.getSpecSpawnsSize(name)));
					player.sendMessage(plugin.t.getString("ARENA_INFO_SPAWNS", vars));
					
					if(!am.isReady(name)) {
						player.sendMessage(plugin.t.getString("ARENA_INFO_NEEDS_HEADER"));
						if(am.inUse(name)) player.sendMessage(plugin.t.getString("ARENA_INFO_NEEDS_NO_USE"));
						if(!am.pvpEnabled(name)) player.sendMessage(plugin.t.getString("ARENA_INFO_NEEDS_PVP"));
						if(am.getRedSpawnsSize(name) == 0) {
							vars.put("team", Lobby.RED.getName());
							player.sendMessage(plugin.t.getString("ARENA_INFO_NEEDS_SPAWN", vars));
						}
						if(am.getBlueSpawnsSize(name) == 0) {
							vars.put("team", Lobby.BLUE.getName());
							player.sendMessage(plugin.t.getString("ARENA_INFO_NEEDS_SPAWN", vars));
						}
						if(am.getSpecSpawnsSize(name) == 0) {
							vars.put("team", Lobby.SPECTATE.getName());
							player.sendMessage(plugin.t.getString("ARENA_INFO_NEEDS_SPAWN", vars));
						}
					}

					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[2].equalsIgnoreCase("blue")) {
					am.addBlueSpawn(name, player.getLocation());
					am.saveData();
					vars.put("team_color", Lobby.BLUE.color().toString());
					vars.put("team", Lobby.BLUE.getName());
					vars.put("team_spawns", String.valueOf(am.getBlueSpawnsSize(name)));
					player.sendMessage(plugin.t.getString("ARENA_SPAWN_ADDED", vars));
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[2].equalsIgnoreCase("red")) {
					am.addRedSpawn(name, player.getLocation());
					am.saveData();
					vars.put("team_color", Lobby.RED.color().toString());
					vars.put("team", Lobby.RED.getName());
					vars.put("team_spawns", String.valueOf(am.getRedSpawnsSize(name)));
					player.sendMessage(plugin.t.getString("ARENA_SPAWN_ADDED", vars));
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[2].equalsIgnoreCase("spec")) {
					am.addSpecSpawn(name, player.getLocation());
					am.saveData();
					vars.put("team_color", Lobby.SPECTATE.color().toString());
					vars.put("team", Lobby.SPECTATE.getName());
					vars.put("team_spawns", String.valueOf(am.getSpecSpawnsSize(name)));
					player.sendMessage(plugin.t.getString("ARENA_SPAWN_ADDED", vars));
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[2].equalsIgnoreCase("remove")) {
					am.remove(name);
					am.saveData();
					player.sendMessage(plugin.t.getString("ARENA_REMOVED", vars));
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[2].equalsIgnoreCase("delblue")) {
					int num = am.getBlueSpawnsSize(name);
					am.removeBlueSpawns(name);
					am.saveData();
					vars.put("team_color", Lobby.BLUE.color().toString());
					vars.put("team", Lobby.BLUE.getName());
					vars.put("team_spawns", String.valueOf(num));
					player.sendMessage(plugin.t.getString("ARENA_SPAWNS_REMOVED", vars));
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[2].equalsIgnoreCase("delred")) {
					int num = am.getRedSpawnsSize(name);
					am.removeRedSpawns(name);
					am.saveData();
					vars.put("team_color", Lobby.RED.color().toString());
					vars.put("team", Lobby.RED.getName());
					vars.put("team_spawns", String.valueOf(num));
					player.sendMessage(plugin.t.getString("ARENA_SPAWNS_REMOVED", vars));
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[2].equalsIgnoreCase("delspec")) {
					int num = am.getSpecSpawnsSize(name);
					am.removeSpecSpawns(name);
					am.saveData();
					vars.put("team_color", Lobby.SPECTATE.color().toString());
					vars.put("team", Lobby.SPECTATE.getName());
					vars.put("team_spawns", String.valueOf(num));
					player.sendMessage(plugin.t.getString("ARENA_SPAWNS_REMOVED", vars));
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[2].equalsIgnoreCase("size")) {
					if(args.length == 4) {
						try {
							int size = Integer.parseInt(args[3]);
							am.setSize(name, size);
						}catch(Exception e) {
							player.sendMessage(plugin.t.getString("INVALID_NUMBER"));
							return true;
						}
					}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				}
			}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else {
			plugin.log(plugin.t.getString("COMMAND_NOT_AS_CONSOLE"));
			return true;
		}
		return false;
	}
}
