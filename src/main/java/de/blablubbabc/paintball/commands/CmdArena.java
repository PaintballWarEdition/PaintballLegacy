/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
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
import de.blablubbabc.paintball.utils.KeyValuePair;
import de.blablubbabc.paintball.utils.Log;
import de.blablubbabc.paintball.utils.Translator;

public class CmdArena {

	private Paintball plugin;

	private int entriesPerPage = 15;

	public CmdArena(Paintball pl) {
		plugin = pl;
	}

	public boolean command(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			ArenaManager arenaManager = plugin.arenaManager;
			if (args[1].equalsIgnoreCase("list")) {
				int page = 1;
				if (args.length >= 3) {
					try {
						page = Math.max(1, Integer.parseInt(args[2]));
					} catch (NumberFormatException e) {
						player.sendMessage(Translator.getString("INVALID_NUMBER"));
						return true;
					}
				}
				// list
				List<String> arenas = arenaManager.getAllArenaNames();
				Map<String, String> vars = new HashMap<String, String>();
				vars.put("arenas", String.valueOf(arenas.size()));
				player.sendMessage(Translator.getString("ARENA_LIST_HEADER", vars));

				int start = (page - 1) * entriesPerPage;
				int max_page = Math.max(1, (int) Math.ceil((double) arenas.size() / entriesPerPage));
				// selected page to high ?
				if (start >= arenas.size()) {
					if (arenas.size() > 0) page = max_page;
					else page = 1;

					// recalculate start
					start = (page - 1) * entriesPerPage;
				}
				int end = Math.min(start + entriesPerPage, arenas.size());

				// page header
				player.sendMessage(Translator.getString("ARENA_LIST_PAGE_HEADER", new KeyValuePair("current_page", String.valueOf(page)), new KeyValuePair("max_page", String.valueOf(max_page))));

				Map<String, String> vars2 = new HashMap<String, String>();
				for (int i = start; i < end; i++) {
					String name = arenas.get(i);
					vars2.put("arena", name);
					vars2.put("status", arenaManager.getArenaStatus(name));
					player.sendMessage(Translator.getString("ARENA_LIST_ENTRY", vars2));
				}
				return true;
			} else {
				String name = args[1];
				Map<String, String> vars = new HashMap<String, String>();
				vars.put("arena", name);
				if (!arenaManager.existing(name)) {
					if (args.length == 2) {
						arenaManager.addArena(name);
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
				// Existing:
				if (args[2].equalsIgnoreCase("info")) {
					Map<ArenaStat, Integer> arenaStats = arenaManager.getArenaStats(name);
					vars.put("status", arenaManager.getArenaStatus(name));
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

					Map<ArenaSetting, Integer> arenaSettings = arenaManager.getArenaSettings(name);
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
					int bluespawnsize = arenaManager.getBlueSpawnsSize(name);
					vars.put("spawns", String.valueOf(bluespawnsize));
					player.sendMessage(Translator.getString("ARENA_INFO_SPAWNS", vars));

					vars.put("team", String.valueOf(Lobby.RED.getName()));
					int redspawnsize = arenaManager.getRedSpawnsSize(name);
					vars.put("spawns", String.valueOf(redspawnsize));
					player.sendMessage(Translator.getString("ARENA_INFO_SPAWNS", vars));

					vars.put("team", String.valueOf(Lobby.SPECTATE.getName()));
					int specspawnsize = arenaManager.getSpecSpawnsSize(name);
					vars.put("spawns", String.valueOf(specspawnsize));
					player.sendMessage(Translator.getString("ARENA_INFO_SPAWNS", vars));

					if (!arenaManager.isReady(name)) {
						player.sendMessage(Translator.getString("ARENA_INFO_NEEDS_HEADER"));
						if (arenaManager.inUse(name)) player.sendMessage(Translator.getString("ARENA_INFO_NEEDS_NO_USE"));
						if (arenaManager.isDisabled(name)) player.sendMessage(Translator.getString("ARENA_INFO_NEEDS_ENABLE"));
						// if(!am.pvpEnabled(name))
						// player.sendMessage(plugin.t.getString("ARENA_INFO_NEEDS_PVP"));
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
				} else if (args[2].equalsIgnoreCase("blue")) {
					if (arenaManager.inUse(name)) {
						player.sendMessage(Translator.getString("ARENA_NO_EDIT_IN_USE"));
						return true;
					}
					arenaManager.addBlueSpawn(name, player.getLocation());
					vars.put("team_color", Lobby.BLUE.color().toString());
					vars.put("team", Lobby.BLUE.getName());
					vars.put("team_spawns", String.valueOf(arenaManager.getBlueSpawnsSize(name)));
					player.sendMessage(Translator.getString("ARENA_SPAWN_ADDED", vars));
					return true;
				} else if (args[2].equalsIgnoreCase("red")) {
					if (arenaManager.inUse(name)) {
						player.sendMessage(Translator.getString("ARENA_NO_EDIT_IN_USE"));
						return true;
					}
					arenaManager.addRedSpawn(name, player.getLocation());
					vars.put("team_color", Lobby.RED.color().toString());
					vars.put("team", Lobby.RED.getName());
					vars.put("team_spawns", String.valueOf(arenaManager.getRedSpawnsSize(name)));
					player.sendMessage(Translator.getString("ARENA_SPAWN_ADDED", vars));
					return true;
				} else if (args[2].equalsIgnoreCase("spec")) {
					if (arenaManager.inUse(name)) {
						player.sendMessage(Translator.getString("ARENA_NO_EDIT_IN_USE"));
						return true;
					}
					arenaManager.addSpecSpawn(name, player.getLocation());
					vars.put("team_color", Lobby.SPECTATE.color().toString());
					vars.put("team", Lobby.SPECTATE.getName());
					vars.put("team_spawns", String.valueOf(arenaManager.getSpecSpawnsSize(name)));
					player.sendMessage(Translator.getString("ARENA_SPAWN_ADDED", vars));
					return true;
				} else if (args[2].equalsIgnoreCase("remove")) {
					if (arenaManager.inUse(name)) {
						player.sendMessage(Translator.getString("ARENA_NO_EDIT_IN_USE"));
						return true;
					}
					arenaManager.remove(name);
					player.sendMessage(Translator.getString("ARENA_REMOVED", vars));
					return true;
				} else if (args[2].equalsIgnoreCase("delblue")) {
					if (arenaManager.inUse(name)) {
						player.sendMessage(Translator.getString("ARENA_NO_EDIT_IN_USE"));
						return true;
					}
					int num = arenaManager.getBlueSpawnsSize(name);
					arenaManager.removeBlueSpawns(name);
					vars.put("team_color", Lobby.BLUE.color().toString());
					vars.put("team", Lobby.BLUE.getName());
					vars.put("team_spawns", String.valueOf(num));
					player.sendMessage(Translator.getString("ARENA_SPAWNS_REMOVED", vars));
					return true;
				} else if (args[2].equalsIgnoreCase("delred")) {
					if (arenaManager.inUse(name)) {
						player.sendMessage(Translator.getString("ARENA_NO_EDIT_IN_USE"));
						return true;
					}
					int num = arenaManager.getRedSpawnsSize(name);
					arenaManager.removeRedSpawns(name);
					vars.put("team_color", Lobby.RED.color().toString());
					vars.put("team", Lobby.RED.getName());
					vars.put("team_spawns", String.valueOf(num));
					player.sendMessage(Translator.getString("ARENA_SPAWNS_REMOVED", vars));
					return true;
				} else if (args[2].equalsIgnoreCase("delspec")) {
					if (arenaManager.inUse(name)) {
						player.sendMessage(Translator.getString("ARENA_NO_EDIT_IN_USE"));
						return true;
					}
					int num = arenaManager.getSpecSpawnsSize(name);
					arenaManager.removeSpecSpawns(name);
					vars.put("team_color", Lobby.SPECTATE.color().toString());
					vars.put("team", Lobby.SPECTATE.getName());
					vars.put("team_spawns", String.valueOf(num));
					player.sendMessage(Translator.getString("ARENA_SPAWNS_REMOVED", vars));
					return true;
				} else if (args[2].equalsIgnoreCase("set")) {
					if (arenaManager.inUse(name)) {
						player.sendMessage(Translator.getString("ARENA_NO_EDIT_IN_USE"));
						return true;
					}
					ArenaSetting setting = null;
					if (args.length == 5 && (setting = ArenaSetting.getFromKey(args[3])) != null) {
						try {
							int value = Integer.parseInt(args[4]);
							Map<ArenaSetting, Integer> newSettings = new HashMap<ArenaSetting, Integer>();
							newSettings.put(setting, value);
							arenaManager.setSettings(name, newSettings);
							vars.put("setting", setting.getKey());
							vars.put("value", String.valueOf(value));
							player.sendMessage(Translator.getString("ARENA_SET_SETTING", vars));
							return true;
						} catch (NumberFormatException e) {
							player.sendMessage(Translator.getString("INVALID_NUMBER"));
							return true;
						}
					} else {
						vars.put("settings", ArenaSetting.getKeysAsString());
						player.sendMessage(Translator.getString("ARENA_INVALID_SETTING", vars));
						return true;
					}
				} else if (args[2].equalsIgnoreCase("disable")) {
					if (arenaManager.inUse(name)) {
						player.sendMessage(Translator.getString("ARENA_NO_EDIT_IN_USE"));
						return true;
					}
					if (arenaManager.disable(name)) {
						player.sendMessage(Translator.getString("ARENA_DISABLED", vars));
					} else {
						player.sendMessage(Translator.getString("ARENA_ALREADY_DISABLED", vars));
					}
					return true;
				} else if (args[2].equalsIgnoreCase("enable")) {
					if (arenaManager.inUse(name)) {
						player.sendMessage(Translator.getString("ARENA_NO_EDIT_IN_USE"));
						return true;
					}
					if (arenaManager.enable(name)) {
						player.sendMessage(Translator.getString("ARENA_ENABLED", vars));
					} else {
						player.sendMessage(Translator.getString("ARENA_ALREADY_ENABLED", vars));
					}
					return true;
				}
			}
		} else {
			Log.info(Translator.getString("COMMAND_NOT_AS_CONSOLE"));
			return true;
		}
		return false;
	}
}
