/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import de.blablubbabc.paintball.statistics.arena.ArenaSetting;
import de.blablubbabc.paintball.statistics.arena.ArenaStat;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class ArenaManager {

	private final Paintball plugin;
	private String last = null;
	private int current = 0;
	private String nextArenaForce;

	public ArenaManager(Paintball pl) {
		plugin = pl;
		nextArenaForce = null;
	}

	// METHODS

	public boolean existing(String name) {
		return plugin.sql.sqlArenaLobby.isArenaExisting(name);
	}

	// GETTER
	public List<String> getAllArenaNames() {
		return plugin.sql.sqlArenaLobby.getAllArenaNames();
	}

	public boolean isReady() {
		for (String arena : getAllArenaNames()) {
			if (isReady(arena)) return true;
		}
		return false;
	}

	public boolean hasAllSpawns(String arena) {
		if (getBlueSpawnsSize(arena) > 0 && getRedSpawnsSize(arena) > 0 && getSpecSpawnsSize(arena) > 0) return true;
		else return false;
	}

	public String getArenaStatus(String name) {
		String ready = "";
		if (isReady(name)) ready = Translator.getString("ARENA_STATUS_READY");
		else ready = Translator.getString("ARENA_STATUS_NOT_READY");
		return ready;
	}

	public boolean isReady(String arena) {
		if (!isDisabled(arena) && !inUse(arena)) {
			// spawns?
			if (hasAllSpawns(arena)) {
				return true;
				// worlds pvp on?
				// if(pvpEnabled(arena)) return true;
			}
		}
		return false;
	}

	public boolean inUse(String arena) {
		return plugin.sql.sqlArenaLobby.isArenaActive(arena);
	}

	public boolean isDisabled(String arena) {
		return plugin.disabledArenas.contains(arena);
	}

	/*public boolean pvpEnabled(String arena) {
		for(Location loc : getBlueSpawns(arena)) {
			if(!loc.getWorld().getPVP()) return false;
		}
		for(Location loc : getRedSpawns(arena)) {
			if(!loc.getWorld().getPVP()) return false;
		}
		for(Location loc : getSpecSpawns(arena)) {
			if(!loc.getWorld().getPVP()) return false;
		}
		return true;
	}*/

	public List<String> getReadyArenas() {
		List<String> arenas = new ArrayList<String>();
		for (String arena : getAllArenaNames()) {
			if (!inUse(arena) && isReady(arena)) arenas.add(arena);
		}
		return arenas;
	}

	// SETTER
	public void setNotActive(String arena) {
		plugin.sql.sqlArenaLobby.setArenaNotActive(arena);
	}

	public void setActive(String arena) {
		plugin.sql.sqlArenaLobby.setArenaActive(arena);
	}

	// return true, if arena was NOT disabled before and is now disabled
	public boolean disable(String arena) {
		if (!plugin.disabledArenas.contains(arena)) {
			plugin.disabledArenas.add(arena);
			plugin.getConfig().set("Paintball.Arena.Disabled Arenas", plugin.disabledArenas);
			plugin.saveConfig();
			return true;
		}
		return false;
	}

	// return true, if arena was NOT enabled before and is now enabled
	public boolean enable(String arena) {
		if (plugin.disabledArenas.contains(arena)) {
			plugin.disabledArenas.remove(arena);
			plugin.getConfig().set("Paintball.Arena.Disabled Arenas", plugin.disabledArenas);
			plugin.saveConfig();
			return true;
		}
		return false;
	}

	// GETTING ARENAS
	public String getNextArena(VoteManager voteManager) {
		String next = null;

		// ready arenas:
		List<String> readyArenas = getReadyArenas();
		// is there even a ready arena?
		if (readyArenas.isEmpty()) return null;

		// force map:
		if (nextArenaForce != null && readyArenas.contains(nextArenaForce)) {
			next = nextArenaForce;
		} else {
			// voteManager
			if (plugin.arenaVoting && voteManager != null && voteManager.isValid() && voteManager.didSomebodyVote()) {
				next = voteManager.getVotedAndReadyArena(readyArenas);
			}

			// still null -> there must be another ready arena, because this methods gets only
			// called after a check
			if (next == null) {
				if (plugin.arenaRotationRandom) {
					// random next arena:
					if (readyArenas.size() >= 2) {
						int index = Utils.random.nextInt(readyArenas.size());
						next = readyArenas.get(index);
						if (last != null) {
							// get next arena which is not last:
							while (next.equals(last)) {
								index += 1;
								next = readyArenas.get(index >= readyArenas.size() ? 0 : index);
							}
						}
					} else {
						// there is only one ready arena..:
						next = readyArenas.get(0);
					}
				} else {
					// rotation:
					if (current >= readyArenas.size()) current = 0;
					String arena = readyArenas.get(current);
					current++;
					return arena;
				}
			}
		}

		last = next;
		return next;
	}

	// ///////////////////////////

	public Map<ArenaStat, Integer> getArenaStats(String name) {
		return plugin.sql.sqlArenaLobby.getArenaStats(name);
	}

	public Map<ArenaSetting, Integer> getArenaSettings(String name) {
		return plugin.sql.sqlArenaLobby.getArenaSettings(name);
	}

	// SPAWNS
	public int getBlueSpawnsSize(String name) {
		return plugin.sql.sqlArenaLobby.getBluespawnsSize(name);
	}

	public int getRedSpawnsSize(String name) {
		return plugin.sql.sqlArenaLobby.getRedspawnsSize(name);
	}

	public int getSpecSpawnsSize(String name) {
		return plugin.sql.sqlArenaLobby.getSpecspawnsSize(name);
	}

	public List<Location> getBlueSpawns(String name) {
		return plugin.sql.sqlArenaLobby.getBluespawns(name);
	}

	public List<Location> getRedSpawns(String name) {
		return plugin.sql.sqlArenaLobby.getRedspawns(name);
	}

	public List<Location> getSpecSpawns(String name) {
		return plugin.sql.sqlArenaLobby.getSpecspawns(name);
	}

	// SETTER

	public void addArena(String name) {
		plugin.sql.sqlArenaLobby.addNewArena(name);
	}

	public void setNext(String arena) {
		nextArenaForce = arena;
	}

	public void resetNext() {
		nextArenaForce = null;
	}

	// STATS
	public void addStats(final String arena, final Map<ArenaStat, Integer> stats) {
		Paintball.addAsyncTask();
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				plugin.sql.sqlArenaLobby.addArenaStats(arena, stats);
				Paintball.removeAsyncTask();
			}
		});
	}

	public void setStats(final String arena, final Map<ArenaStat, Integer> stats) {
		Paintball.addAsyncTask();
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				plugin.sql.sqlArenaLobby.setArenaStats(arena, stats);
				Paintball.removeAsyncTask();
			}
		});
	}

	// SETTINGS
	public void setSettings(final String arena, final Map<ArenaSetting, Integer> settings) {
		Paintball.addAsyncTask();
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				plugin.sql.sqlArenaLobby.setArenaSettings(arena, settings);
				Paintball.removeAsyncTask();
			}
		});
	}

	// SPAWNS
	public void addBlueSpawn(final String arena, final Location loc) {
		plugin.sql.sqlArenaLobby.addBluespawn(loc, arena);
	}

	public void addRedSpawn(final String arena, final Location loc) {
		plugin.sql.sqlArenaLobby.addRedspawn(loc, arena);
	}

	public void addSpecSpawn(final String arena, final Location loc) {
		plugin.sql.sqlArenaLobby.addSpecspawn(loc, arena);
	}

	public void removeBlueSpawns(final String arena) {
		plugin.sql.sqlArenaLobby.removeBluespawns(arena);
	}

	public void removeRedSpawns(final String arena) {
		plugin.sql.sqlArenaLobby.removeRedspawns(arena);
	}

	public void removeSpecSpawns(final String arena) {
		plugin.sql.sqlArenaLobby.removeSpecspawns(arena);
	}

	public void remove(String name) {
		plugin.sql.sqlArenaLobby.removeArena(name);
	}
}
