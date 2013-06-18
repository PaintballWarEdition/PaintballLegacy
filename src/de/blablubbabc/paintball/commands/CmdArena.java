package de.blablubbabc.paintball.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.blablubbabc.paintball.ArenaManager;
import de.blablubbabc.paintball.Lobby;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.statistics.arena.ArenaSetting;
import de.blablubbabc.paintball.statistics.arena.ArenaStat;
import de.blablubbabc.paintball.utils.Log;
import de.blablubbabc.paintball.utils.Translator;

public class CmdArena {
	private Paintball plugin;
	private ArenaManager am;

	public CmdArena(Paintball pl, ArenaManager a) {
		plugin = pl;
		am = a;
	}

	public boolean command(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			//PERMISSION CHECK
			/*if(!sender.isOp() && !sender.hasPermission("paintball.arena")) {
				sender.sendMessage(plugin.t.getString("NO_PERMISSION"));
				return true;
			}*/
			Player player = (Player) sender;
			if (args[1].equalsIgnoreCase("list")) {
				//list
				List<String> arenas = am.getAllArenaNames();
				Map<String, String> vars = new HashMap<String, String>();
				vars.put("arenas", String.valueOf(arenas.size()));
				player.sendMessage(Translator.getString("ARENA_LIST_HEADER", vars));
				for (String name : arenas) {
					HashMap<String, String> vars2 = new HashMap<String, String>();
					vars2.put("arena", name);
					vars2.put("status", am.getArenaStatus(name));
					player.sendMessage(Translator.getString("ARENA_LIST_ENTRY", vars2));
				}
				return true;
			} else {
				String name = args[1];
				Map<String, String> vars = new HashMap<String, String>();
				vars.put("arena", name);
				if (!am.existing(name)) {
					if (args.length == 2) {
						am.addArena(name);
						player.sendMessage(Translator.getString("ARENA_CREATED", vars));
						return true;
					} else {
						player.sendMessage(Translator.getString("ARENA_NOT_FOUND", vars));
						return true;
					}
				} else if (args.length == 2) {
					player.sendMessage(Translator.getString("ARENA_ALREADY_EXISTS", vars));
					return true;
				}
				//Existing:
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				if (args[2].equalsIgnoreCase("info")) {
					Map<ArenaStat, Integer> arenaStats = am.getArenaStats(name);
					vars.put("status", am.getArenaStatus(name));
					for (Entry<ArenaStat, Integer> entry : arenaStats.entrySet()) {
						vars.put(entry.getKey().getKey(), String.valueOf(entry.getValue()));
					}
					
					player.sendMessage(Translator.getString("ARENA_INFO_HEADER", vars));
					player.sendMessage(Translator.getString("ARENA_INFO_STATS_HEADER"));
					player.sendMessage(Translator.getString("ARENA_INFO_STATS_ROUNDS", vars));
					player.sendMessage(Translator.getString("ARENA_INFO_STATS_KILLS", vars));
					player.sendMessage(Translator.getString("ARENA_INFO_STATS_SHOTS", vars));
					player.sendMessage(Translator.getString("ARENA_INFO_STATS_GRENADES", vars));
					player.sendMessage(Translator.getString("ARENA_INFO_STATS_AIRSTRIKES", vars));
					
					Map<ArenaSetting, Integer> arenaSettings = am.getArenaSettings(name);
					for (Entry<ArenaSetting, Integer> entry : arenaSettings.entrySet()) {
						vars.put(entry.getKey().getKey(), String.valueOf(entry.getValue()));
					}
					vars.put("balls_def", String.valueOf(plugin.balls));
					vars.put("grenades_def", String.valueOf(plugin.grenadeAmount));
					vars.put("airstrikes_def", String.valueOf(plugin.airstrikeAmount));
					vars.put("lives_def", String.valueOf(plugin.lives));
					vars.put("respawns_def", String.valueOf(plugin.respawns));
					vars.put("round_time_def", String.valueOf(plugin.roundTimer));
					
					player.sendMessage(Translator.getString("ARENA_INFO_SETTINGS_HEADER"));
					player.sendMessage(Translator.getString("ARENA_INFO_SETTINGS_BALLS", vars));
					player.sendMessage(Translator.getString("ARENA_INFO_SETTINGS_GRENADES", vars));
					player.sendMessage(Translator.getString("ARENA_INFO_SETTINGS_AIRSTRIKES", vars));
					player.sendMessage(Translator.getString("ARENA_INFO_SETTINGS_LIVES", vars));
					player.sendMessage(Translator.getString("ARENA_INFO_SETTINGS_RESPAWNS", vars));
					player.sendMessage(Translator.getString("ARENA_INFO_SETTINGS_ROUND_TIME", vars));
					
					vars.put("team", String.valueOf(Lobby.BLUE.getName()));
					int bluespawnsize = am.getBlueSpawnsSize(name);
					vars.put("spawns", String.valueOf(bluespawnsize));
					player.sendMessage(Translator.getString("ARENA_INFO_SPAWNS", vars));
					
					vars.put("team", String.valueOf(Lobby.RED.getName()));
					int redspawnsize = am.getRedSpawnsSize(name);
					vars.put("spawns", String.valueOf(redspawnsize));
					player.sendMessage(Translator.getString("ARENA_INFO_SPAWNS", vars));
					
					vars.put("team", String.valueOf(Lobby.SPECTATE.getName()));
					int specspawnsize = am.getSpecSpawnsSize(name);
					vars.put("spawns", String.valueOf(specspawnsize));
					player.sendMessage(Translator.getString("ARENA_INFO_SPAWNS", vars));
					
					if (!am.isReady(name)) {
						player.sendMessage(Translator.getString("ARENA_INFO_NEEDS_HEADER"));
						if (am.inUse(name)) player.sendMessage(Translator.getString("ARENA_INFO_NEEDS_NO_USE"));
						if (am.isDisabled(name)) player.sendMessage(Translator.getString("ARENA_INFO_NEEDS_ENABLE"));
						//if(!am.pvpEnabled(name)) player.sendMessage(plugin.t.getString("ARENA_INFO_NEEDS_PVP"));
						if (redspawnsize == 0) {
							vars.put("team", Lobby.RED.getName());
							player.sendMessage(Translator.getString("ARENA_INFO_NEEDS_SPAWN", vars));
						}
						if (bluespawnsize == 0) {
							vars.put("team", Lobby.BLUE.getName());
							player.sendMessage(Translator.getString("ARENA_INFO_NEEDS_SPAWN", vars));
						}
						if (specspawnsize == 0) {
							vars.put("team", Lobby.SPECTATE.getName());
							player.sendMessage(Translator.getString("ARENA_INFO_NEEDS_SPAWN", vars));
						}
					}

					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if (args[2].equalsIgnoreCase("blue")) {
					if (am.inUse(name)) {
						player.sendMessage(Translator.getString("ARENA_NO_EDIT_IN_USE"));
						return true;
					}
					am.addBlueSpawn(name, player.getLocation());
					vars.put("team_color", Lobby.BLUE.color().toString());
					vars.put("team", Lobby.BLUE.getName());
					vars.put("team_spawns", String.valueOf(am.getBlueSpawnsSize(name)));
					player.sendMessage(Translator.getString("ARENA_SPAWN_ADDED", vars));
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if (args[2].equalsIgnoreCase("red")) {
					if (am.inUse(name)) {
						player.sendMessage(Translator.getString("ARENA_NO_EDIT_IN_USE"));
						return true;
					}
					am.addRedSpawn(name, player.getLocation());
					vars.put("team_color", Lobby.RED.color().toString());
					vars.put("team", Lobby.RED.getName());
					vars.put("team_spawns", String.valueOf(am.getRedSpawnsSize(name)));
					player.sendMessage(Translator.getString("ARENA_SPAWN_ADDED", vars));
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if (args[2].equalsIgnoreCase("spec")) {
					if (am.inUse(name)) {
						player.sendMessage(Translator.getString("ARENA_NO_EDIT_IN_USE"));
						return true;
					}
					am.addSpecSpawn(name, player.getLocation());
					vars.put("team_color", Lobby.SPECTATE.color().toString());
					vars.put("team", Lobby.SPECTATE.getName());
					vars.put("team_spawns", String.valueOf(am.getSpecSpawnsSize(name)));
					player.sendMessage(Translator.getString("ARENA_SPAWN_ADDED", vars));
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if (args[2].equalsIgnoreCase("remove")) {
					if (am.inUse(name)) {
						player.sendMessage(Translator.getString("ARENA_NO_EDIT_IN_USE"));
						return true;
					}
					am.remove(name);
					player.sendMessage(Translator.getString("ARENA_REMOVED", vars));
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if (args[2].equalsIgnoreCase("delblue")) {
					if (am.inUse(name)) {
						player.sendMessage(Translator.getString("ARENA_NO_EDIT_IN_USE"));
						return true;
					}
					int num = am.getBlueSpawnsSize(name);
					am.removeBlueSpawns(name);
					vars.put("team_color", Lobby.BLUE.color().toString());
					vars.put("team", Lobby.BLUE.getName());
					vars.put("team_spawns", String.valueOf(num));
					player.sendMessage(Translator.getString("ARENA_SPAWNS_REMOVED", vars));
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if (args[2].equalsIgnoreCase("delred")) {
					if (am.inUse(name)) {
						player.sendMessage(Translator.getString("ARENA_NO_EDIT_IN_USE"));
						return true;
					}
					int num = am.getRedSpawnsSize(name);
					am.removeRedSpawns(name);
					vars.put("team_color", Lobby.RED.color().toString());
					vars.put("team", Lobby.RED.getName());
					vars.put("team_spawns", String.valueOf(num));
					player.sendMessage(Translator.getString("ARENA_SPAWNS_REMOVED", vars));
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if (args[2].equalsIgnoreCase("delspec")) {
					if (am.inUse(name)) {
						player.sendMessage(Translator.getString("ARENA_NO_EDIT_IN_USE"));
						return true;
					}
					int num = am.getSpecSpawnsSize(name);
					am.removeSpecSpawns(name);
					vars.put("team_color", Lobby.SPECTATE.color().toString());
					vars.put("team", Lobby.SPECTATE.getName());
					vars.put("team_spawns", String.valueOf(num));
					player.sendMessage(Translator.getString("ARENA_SPAWNS_REMOVED", vars));
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if (args[2].equalsIgnoreCase("set")) {
					if (am.inUse(name)) {
						player.sendMessage(Translator.getString("ARENA_NO_EDIT_IN_USE"));
						return true;
					}
					ArenaSetting setting = null;
					if (args.length == 5 && (setting = ArenaSetting.getFromKey(args[3])) != null) {
						try {
							int value = Integer.parseInt(args[4]);
							Map<ArenaSetting, Integer> newSettings = new HashMap<ArenaSetting, Integer>();
							newSettings.put(setting, value);
							am.setSettings(name, newSettings);
							vars.put("setting", setting.getKey());
							vars.put("value", String.valueOf(value));
							player.sendMessage(Translator.getString("ARENA_SET_SETTING", vars));
							return true;
						} catch(Exception e) {
							player.sendMessage(Translator.getString("INVALID_NUMBER"));
							return true;
						}
					} else {
						vars.put("settings", ArenaSetting.getKeysAsString());
						player.sendMessage(Translator.getString("ARENA_INVALID_SETTING", vars));
						return true;
					}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if (args[2].equalsIgnoreCase("disable")) {
					if (am.inUse(name)) {
						player.sendMessage(Translator.getString("ARENA_NO_EDIT_IN_USE"));
						return true;
					}
					if (am.disable(name)) {
						player.sendMessage(Translator.getString("ARENA_DISABLED", vars));
					} else {
						player.sendMessage(Translator.getString("ARENA_ALREADY_DISABLED", vars));
					}
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if (args[2].equalsIgnoreCase("enable")) {
					if (am.inUse(name)) {
						player.sendMessage(Translator.getString("ARENA_NO_EDIT_IN_USE"));
						return true;
					}
					if (am.enable(name)) {
						player.sendMessage(Translator.getString("ARENA_ENABLED", vars));
					} else {
						player.sendMessage(Translator.getString("ARENA_ALREADY_ENABLED", vars));
					}
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				}
			}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else {
			Log.info(Translator.getString("COMMAND_NOT_AS_CONSOLE"));
			return true;
		}
		return false;
	}
}
