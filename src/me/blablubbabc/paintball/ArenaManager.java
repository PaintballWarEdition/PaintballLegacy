package me.blablubbabc.paintball;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.bukkit.Location;

public class ArenaManager {
	private static Paintball plugin;
	private int zähler;
	private String nextArenaForce;

	public ArenaManager(Paintball pl) {
		plugin = pl;
		zähler = 0;
		nextArenaForce = "";
	}
	//METHODS

	public boolean existing(String name) {
		return plugin.sql.sqlArenaLobby.isArenaExisting(name);
	}
	//GETTER
	public ArrayList<String> getAllArenaNames() {
		return plugin.sql.sqlArenaLobby.getAllArenaNames();
	}
	
	public boolean isReady() {
		for(String arena : getAllArenaNames()) {
			if(isReady(arena)) return true;
		}
		return false;
	}
	
	public boolean hasAllSpawns(String arena) {
		if(getBlueSpawnsSize(arena) > 0 && getRedSpawnsSize(arena) > 0 && getSpecSpawnsSize(arena) > 0) return true;
		else return false;
	}
	
	public String getArenaStatus(String name) {
		String ready = "";
		if(isReady(name)) ready = plugin.t.getString("ARENA_STATUS_READY");
		else ready = plugin.t.getString("ARENA_STATUS_NOT_READY");
		return ready;
	}
	
	public boolean isReady(String arena) {
		if(!inUse(arena)) {
			//spawns?
			if(hasAllSpawns(arena)) {
				//worlds pvp on?
				if(pvpEnabled(arena)) return true;
			}
		}
		return false;
	}

	public boolean inUse(String arena) {
		if(plugin.sql.sqlArenaLobby.isArenaActive(arena)) return true;
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

	private ArrayList<String> readyArenas() {
		ArrayList<String> arenas = new ArrayList<String>();
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

	public LinkedHashMap<String, Integer> getArenaStats(String name) {
		return plugin.sql.sqlArenaLobby.getArenaStats(name);
	}

	public LinkedHashMap<String, Integer> getArenaSettings(String name) {
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
		//settingsList.add("balls"); settingsList.add("grenades"); settingsList.add("airstrikes"); settingsList.add("lives"); settingsList.add("respawns");
	}

	//SPAWNS
	public void addBlueSpawn(String arena, Location loc) {
		plugin.sql.sqlArenaLobby.addBluespawn(loc, arena);
	}
	public void addRedSpawn(String arena, Location loc) {
		plugin.sql.sqlArenaLobby.addRedspawn(loc, arena);
	}
	public void addSpecSpawn(String arena, Location loc) {
		plugin.sql.sqlArenaLobby.addSpecspawn(loc, arena);
	}

	public void removeBlueSpawns(String arena) {
		plugin.sql.sqlArenaLobby.removeBluespawns(arena);
	}
	public void removeRedSpawns(String arena) {
		plugin.sql.sqlArenaLobby.removeBluespawns(arena);
	}
	public void removeSpecSpawns(String arena) {
		plugin.sql.sqlArenaLobby.removeBluespawns(arena);
	}

	public synchronized void remove(String name) {
		plugin.sql.sqlArenaLobby.removeArena(name);
	}
}
