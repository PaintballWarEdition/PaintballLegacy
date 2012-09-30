package me.blablubbabc.paintball;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Location;

public class ArenaManager {
	private static Paintball plugin;
	private HashMap<String, Boolean> active;
	private HashMap<String, Boolean> checkSpawns;
	private int zähler;
	private String nextArenaForce;

	public ArenaManager(Paintball pl) {
		plugin = pl;
		loadArenas();
		zähler = 0;
		nextArenaForce = "";
	}
	//METHODS
	private synchronized void loadArenas() {
		active = new HashMap<String, Boolean>();
		checkSpawns = new HashMap<String, Boolean>();

		for(String name : plugin.sql.sqlArenaLobby.getAllArenaNames()) {
			active.put(name, false);
			checkSpawns.put(name, hasAllSpawnsCheck(name));
		}
	}

	public boolean existing(String name) {
		//return (active.keySet().contains(name) ? true : false);
		return plugin.sql.sqlArenaLobby.isArenaExisting(name);
	}
	//GETTER
	public ArrayList<String> getAllArenaNames() {
		return plugin.sql.sqlArenaLobby.getAllArenaNames();
	}
	
	public synchronized boolean isReady() {
		for(String arena : active.keySet()) {
			if(isReady(arena)) return true;
		}
		return false;
	}

	private synchronized boolean hasAllSpawnsCheck(String arena) {
		if(getBlueSpawnsSize(arena) > 0 && getRedSpawnsSize(arena) > 0 && getSpecSpawnsSize(arena) > 0) return true;
		else return false;
	}
	
	public boolean hasAllSpawns(String arena) {
		synchronized(checkSpawns) {
			if(!checkSpawns.containsKey(arena) || !checkSpawns.get(arena)) {
				return false;
			} else return true;
		}
	}

	public String getArenaStatus(String name) {
		String ready = "";
		if(isReady(name)) ready = plugin.t.getString("ARENA_STATUS_READY");
		else ready = plugin.t.getString("ARENA_STATUS_NOT_READY");
		return ready;
	}
	
	public synchronized boolean isReady(String arena) {
		if(!active.get(arena)) {
			//spawns?
			if(hasAllSpawns(arena)) {
				//worlds pvp on?
				if(pvpEnabled(arena)) return true;
			}
		}
		return false;
	}

	public synchronized  boolean inUse(String arena) {
		if(active.get(arena)) return true;
		else return false;
	}

	public boolean pvpEnabled(String arena) {
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
	}

	private synchronized ArrayList<String> readyArenas() {
		ArrayList<String> arenas = new ArrayList<String>();
		for(String a : active.keySet()) {
			if(!active.get(a) && isReady(a)) arenas.add(a);
		}
		return arenas;
	}
	//SETTER
	public synchronized void setNotActive(String arena) {
		active.put(arena, false);
	}
	public synchronized void setActive(String arena) {
		active.put(arena, true);
	}


	//GETTING ARENAS
	public String getNextArena() {
		//force map:
		if(!nextArenaForce.equalsIgnoreCase("")) {
			if(readyArenas().contains(nextArenaForce)) {
				return nextArenaForce;
			}
		}
		//rotation:
		if(zähler > (readyArenas().size()-1)) zähler = 0;
		String arena = readyArenas().get(zähler);
		zähler++;
		return arena;
	}
	/////////////////////////////

	public HashMap<String, Integer> getArenaStats(String name) {
		return plugin.sql.sqlArenaLobby.getArenaStats(name);
	}

	public HashMap<String, Integer> getArenaSettings(String name) {
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

	public ArrayList<Location> getBlueSpawns(String name) {
		return plugin.sql.sqlArenaLobby.getBluespawns(name);
	}

	public ArrayList<Location> getRedSpawns(String name) {
		return plugin.sql.sqlArenaLobby.getRedspawns(name);
	}

	public ArrayList<Location> getSpecSpawns(String name) {
		return plugin.sql.sqlArenaLobby.getSpecspawns(name);
	}

	//SETTER

	public synchronized void addArena(String name) {
		active.put(name, false);
		plugin.sql.sqlArenaLobby.addNewArena(name);
	}

	public void setNext(String arena) {
		nextArenaForce = arena;
	}
	public void resetNext() {
		nextArenaForce = "";
	}

	//STATS
	public void addStats(String arena, HashMap<String, Integer> stats) {
		plugin.sql.sqlArenaLobby.addArenaStats(arena, stats);
		//statsList.add("rounds"); statsList.add("kills"); statsList.add("shots"); statsList.add("grenades"); statsList.add("airstrikes");
	}

	public void setStats(String arena, HashMap<String, Integer> stats) {
		plugin.sql.sqlArenaLobby.setArenaStats(arena, stats);
	}
	//SETTINGS
	public void setSettings(String arena, HashMap<String, Integer> settings) {
		plugin.sql.sqlArenaLobby.setArenaSettings(arena, settings);
		//settingsList.add("size"); settingsList.add("balls"); settingsList.add("grenades"); settingsList.add("airstrikes"); settingsList.add("lives"); settingsList.add("respawns");
	}

	//SPAWNS
	public void addBlueSpawn(String arena, Location loc) {
		plugin.sql.sqlArenaLobby.addBluespawn(loc, arena);
		synchronized(checkSpawns) {
			checkSpawns.put(arena, hasAllSpawnsCheck(arena));
		}
	}
	public void addRedSpawn(String arena, Location loc) {
		plugin.sql.sqlArenaLobby.addRedspawn(loc, arena);
		synchronized(checkSpawns) {
			checkSpawns.put(arena, hasAllSpawnsCheck(arena));
		}
	}
	public void addSpecSpawn(String arena, Location loc) {
		plugin.sql.sqlArenaLobby.addSpecspawn(loc, arena);
		synchronized(checkSpawns) {
			checkSpawns.put(arena, hasAllSpawnsCheck(arena));
		}
	}

	public void removeBlueSpawns(String arena) {
		plugin.sql.sqlArenaLobby.removeBluespawns(arena);
		synchronized(checkSpawns) {
			checkSpawns.put(arena, false);
		}
	}
	public void removeRedSpawns(String arena) {
		plugin.sql.sqlArenaLobby.removeBluespawns(arena);
		synchronized(checkSpawns) {
			checkSpawns.put(arena, false);
		}
	}
	public void removeSpecSpawns(String arena) {
		plugin.sql.sqlArenaLobby.removeBluespawns(arena);
		synchronized(checkSpawns) {
			checkSpawns.put(arena, false);
		}
	}

	public synchronized void remove(String name) {
		active.remove(name);
		synchronized(checkSpawns) {
			checkSpawns.remove(name);
		}
		plugin.sql.sqlArenaLobby.removeArena(name);
	}
}
