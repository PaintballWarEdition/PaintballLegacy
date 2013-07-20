package de.blablubbabc.paintball;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;

import de.blablubbabc.paintball.statistics.arena.ArenaSetting;
import de.blablubbabc.paintball.statistics.arena.ArenaStat;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;


public class ArenaManager {
	private Paintball plugin;
	private String last = null;
	private int current = 0;
	private String nextArenaForce;

	public ArenaManager(Paintball pl) {
		plugin = pl;
		nextArenaForce = "";
	}
	//METHODS

	public boolean existing(String name) {
		return plugin.sql.sqlArenaLobby.isArenaExisting(name);
	}
	//GETTER
	public List<String> getAllArenaNames() {
		return plugin.sql.sqlArenaLobby.getAllArenaNames();
	}
	
	public boolean isReady() {
		for(String arena : getAllArenaNames()) {
			if(isReady(arena)) return true;
		}
		return false;
	}
	
	public boolean hasAllSpawns(String arena) {
		if (getBlueSpawnsSize(arena) > 0 && getRedSpawnsSize(arena) > 0 && getSpecSpawnsSize(arena) > 0) return true;
		else return false;
	}
	
	public String getArenaStatus(String name) {
		String ready = "";
		if(isReady(name)) ready = Translator.getString("ARENA_STATUS_READY");
		else ready = Translator.getString("ARENA_STATUS_NOT_READY");
		return ready;
	}
	
	public boolean isReady(String arena) {
		if(!isDisabled(arena) && !inUse(arena)) {
			//spawns?
			if(hasAllSpawns(arena)) {
				return true;
				//worlds pvp on?
				//if(pvpEnabled(arena)) return true;
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
		for(String arena : getAllArenaNames()) {
			if(!inUse(arena) && isReady(arena)) arenas.add(arena);
		}
		return arenas;
	}
	//SETTER
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


	//GETTING ARENAS
	public String getNextArena() {
		// ready arenas:
		List<String> ready = getReadyArenas();
		//force map:
		String next = null;
		if(!nextArenaForce.equalsIgnoreCase("") && ready.contains(nextArenaForce)) {
			next = nextArenaForce;
		} else {
			if (plugin.arenaRotationRandom) {
				// random next arena:
				if (ready.size() >= 2) {
					int index = Utils.random.nextInt(ready.size());
					next = ready.get(index);
					if (last != null) {
						// get next arena which is not last:
						while (next.equals(last)) {
							index += 1;
							next = ready.get(index >= ready.size() ? 0 : index);
						}
					}
				} else {
					// there is only one ready arena..:
					next = ready.get(0);
				}
			} else {
				// rotation:
				if(current > (ready.size() - 1)) current = 0;
				String arena = ready.get(current);
				current++;
				return arena;
			}
			
		}
		last = next;
		return next;
	}
	/////////////////////////////

	public Map<ArenaStat, Integer> getArenaStats(String name) {
		return plugin.sql.sqlArenaLobby.getArenaStats(name);
	}

	public Map<ArenaSetting, Integer> getArenaSettings(String name) {
		return plugin.sql.sqlArenaLobby.getArenaSettings(name);
	}

	//SPAWNS
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

	//SETTER

	public void addArena(String name) {
		plugin.sql.sqlArenaLobby.addNewArena(name);
	}

	public void setNext(String arena) {
		nextArenaForce = arena;
	}
	public void resetNext() {
		nextArenaForce = "";
	}

	//STATS
	public void addStats(final String arena, final Map<ArenaStat, Integer> stats) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			
			@Override
			public void run() {
				plugin.sql.sqlArenaLobby.addArenaStats(arena, stats);
				//statsList.add("rounds"); statsList.add("kills"); statsList.add("shots"); statsList.add("grenades"); statsList.add("airstrikes");
			}
		});
	}

	public void setStats(final String arena, final Map<ArenaStat, Integer> stats) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			
			@Override
			public void run() {
				plugin.sql.sqlArenaLobby.setArenaStats(arena, stats);
			}
		});
	}
	//SETTINGS
	public void setSettings(final String arena, final Map<ArenaSetting, Integer> settings) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			
			@Override
			public void run() {
				plugin.sql.sqlArenaLobby.setArenaSettings(arena, settings);
				//settingsList.add("balls"); settingsList.add("grenades"); settingsList.add("airstrikes"); settingsList.add("lives"); settingsList.add("respawns");
			}
		});
	}

	//SPAWNS
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

	public synchronized void remove(String name) {
		plugin.sql.sqlArenaLobby.removeArena(name);
	}
}
