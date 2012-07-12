package me.blablubbabc.paintball;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import me.blablubbabc.BlaDB.Register;
import org.bukkit.Location;

public class ArenaManager {
	private static Paintball plugin;
	private static Register data;
	private LinkedHashMap<String, Boolean> active;
	private int zähler;
	private String nextArenaForce;
	
	public ArenaManager(Paintball pl) {
		plugin = pl;
		data = new Register();
		loadArenas();
		zähler = 0;
		nextArenaForce = "";
	}
	//METHODS
	private void loadArenas() {
		active = new LinkedHashMap<String, Boolean>();
		
		if(plugin.data.getRegister("arenas") == null) saveData();
		else data = plugin.data.getRegister("arenas");
		
		for(String name : data.getData().keySet()) {
			active.put(name, false);
		}
	}
	
	public boolean existing(String name) {
		for(String s : data.getData().keySet()) {
			if(s.equalsIgnoreCase(name)) return true;
		}
		return false;
	}
	//GETTER
	public boolean isReady() {
		for(String arena : active.keySet()) {
			if(isReady(arena)) return true;
		}
		return false;
	}
	
	public boolean isReady(String arena) {
		if(!active.get(arena)) {
			//spawns?
			if(getBlueSpawnsSize(arena) > 0 && getRedSpawnsSize(arena) > 0 && getSpecSpawnsSize(arena) > 0) {
				//worlds pvp on?
				if(pvpEnabled(arena)) return true;
			}
		}
		return false;
	}
	
	public boolean inUse(String arena) {
		if(active.get(arena)) return true;
		else return false;
	}
	
	public boolean pvpEnabled(String arena) {
		for(LinkedHashMap<String, Object> spawn : getBlueSpawns(arena)) {
			Location loc = plugin.transformLocation(spawn);
			if(!loc.getWorld().getPVP()) return false;
		}
		for(LinkedHashMap<String, Object> spawn : getRedSpawns(arena)) {
			Location loc = plugin.transformLocation(spawn);
			if(!loc.getWorld().getPVP()) return false;
		}
		for(LinkedHashMap<String, Object> spawn : getSpecSpawns(arena)) {
			Location loc = plugin.transformLocation(spawn);
			if(!loc.getWorld().getPVP()) return false;
		}
		return true;
	}
	
	private ArrayList<String> readyArenas() {
		ArrayList<String> arenas = new ArrayList<String>();
		for(String a : active.keySet()) {
			if(!active.get(a) && isReady(a)) arenas.add(a);
		}
		return arenas;
	}
	//SETTER
	public void toggleReady(String arena) {
		if(active.get(arena)) active.put(arena, false);
		else active.put(arena, true);
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
	public LinkedHashMap<String, Object> getArenaData() {
		return data.getData();
	}
	
	@SuppressWarnings("unchecked")
	public LinkedHashMap<String, Object> getArena(String name) {
		return (LinkedHashMap<String, Object>) data.getValue(name);
	}
	
	@SuppressWarnings("unchecked")
	public int getBlueSpawnsSize(String name) {
		LinkedHashMap<String, Object> arena = (LinkedHashMap<String, Object>) data.getValue(name);
		ArrayList<LinkedHashMap<String, Object>> spawns = (ArrayList<LinkedHashMap<String, Object>>) arena.get("bluespawns");
		return spawns.size();
	}
	@SuppressWarnings("unchecked")
	public int getRedSpawnsSize(String name) {
		LinkedHashMap<String, Object> arena = (LinkedHashMap<String, Object>) data.getValue(name);
		ArrayList<LinkedHashMap<String, Object>> spawns = (ArrayList<LinkedHashMap<String, Object>>) arena.get("redspawns");
		return spawns.size();
	}
	@SuppressWarnings("unchecked")
	public int getSpecSpawnsSize(String name) {
		LinkedHashMap<String, Object> arena = (LinkedHashMap<String, Object>) data.getValue(name);
		ArrayList<LinkedHashMap<String, Object>> spawns = (ArrayList<LinkedHashMap<String, Object>>) arena.get("specspawns");
		return spawns.size();
	}
	//SPAWNS
	@SuppressWarnings("unchecked")
	public ArrayList<LinkedHashMap<String, Object>> getBlueSpawns(String name) {
		LinkedHashMap<String, Object> arena = (LinkedHashMap<String, Object>) data.getValue(name);
		ArrayList<LinkedHashMap<String, Object>> spawns = (ArrayList<LinkedHashMap<String, Object>>) arena.get("bluespawns");
		return spawns;
	}
	@SuppressWarnings("unchecked")
	public ArrayList<LinkedHashMap<String, Object>> getRedSpawns(String name) {
		LinkedHashMap<String, Object> arena = (LinkedHashMap<String, Object>) data.getValue(name);
		ArrayList<LinkedHashMap<String, Object>> spawns = (ArrayList<LinkedHashMap<String, Object>>) arena.get("redspawns");
		return spawns;
	}
	@SuppressWarnings("unchecked")
	public ArrayList<LinkedHashMap<String, Object>> getSpecSpawns(String name) {
		LinkedHashMap<String, Object> arena = (LinkedHashMap<String, Object>) data.getValue(name);
		ArrayList<LinkedHashMap<String, Object>> spawns = (ArrayList<LinkedHashMap<String, Object>>) arena.get("specspawns");
		return spawns;
	}
	
	//SETTER
	public void saveData() {
		plugin.data.addRegister("arenas", data);
		plugin.data.saveFile();
	}
	public void addArena(String name) {
		LinkedHashMap<String, Object> arena = new LinkedHashMap<String, Object>();
		ArrayList<LinkedHashMap<String, Object>> bluespawns = new ArrayList<LinkedHashMap<String, Object>>();
		ArrayList<LinkedHashMap<String, Object>> redspawns = new ArrayList<LinkedHashMap<String, Object>>();
		ArrayList<LinkedHashMap<String, Object>> specspawns = new ArrayList<LinkedHashMap<String, Object>>();
		
		if(data.getValue(name) == null) {
			arena.put("rounds", 0);
			arena.put("kills", 0);
			arena.put("shots", 0);
			arena.put("size", 0);
			arena.put("bluespawns", bluespawns);
			arena.put("redspawns", redspawns);
			arena.put("specspawns", specspawns);
			
			data.setValue(name, arena);
			active.put(name, false);
			saveData();
		}
	}
	
	public void setNext(String arena) {
		nextArenaForce = arena;
	}
	public void resetNext() {
		nextArenaForce = "";
	}
	
	@SuppressWarnings("unchecked")
	public void addRounds(String name, int rounds) {
		LinkedHashMap<String, Object> arena = (LinkedHashMap<String, Object>) data.getValue(name);
		arena.put("rounds", ((Integer) arena.get("rounds") + rounds));
		data.setValue(name, arena);
	}
	@SuppressWarnings("unchecked")
	public void addKills(String name, int kills) {
		LinkedHashMap<String, Object> arena = (LinkedHashMap<String, Object>) data.getValue(name);
		arena.put("kills", ((Integer) arena.get("kills") + kills));
		data.setValue(name, arena);
	}
	@SuppressWarnings("unchecked")
	public void addShots(String name, int shots) {
		LinkedHashMap<String, Object> arena = (LinkedHashMap<String, Object>) data.getValue(name);
		arena.put("shots", ((Integer) arena.get("shots") + shots));
		data.setValue(name, arena);
	}
	
	@SuppressWarnings("unchecked")
	public void setSize(String name, int size) {
		LinkedHashMap<String, Object> arena = (LinkedHashMap<String, Object>) data.getValue(name);
		arena.put("size", size);
		data.setValue(name, arena);
	}
	
	@SuppressWarnings("unchecked")
	public void addBlueSpawn(String name, Location loc) {
		LinkedHashMap<String, Object> map = plugin.transformLocation(loc);
		LinkedHashMap<String, Object> arena = (LinkedHashMap<String, Object>) data.getValue(name);
		ArrayList<LinkedHashMap<String, Object>> bluespawns = (ArrayList<LinkedHashMap<String, Object>>) arena.get("bluespawns");
		bluespawns.add(map);
		arena.put("bluespawns", bluespawns);
		data.setValue(name, arena);
	}
	@SuppressWarnings("unchecked")
	public void addRedSpawn(String name, Location loc) {
		LinkedHashMap<String, Object> map = plugin.transformLocation(loc);
		LinkedHashMap<String, Object> arena = (LinkedHashMap<String, Object>) data.getValue(name);
		ArrayList<LinkedHashMap<String, Object>> redspawns = (ArrayList<LinkedHashMap<String, Object>>) arena.get("redspawns");
		redspawns.add(map);
		arena.put("redspawns", redspawns);
		data.setValue(name, arena);
	}
	@SuppressWarnings("unchecked")
	public void addSpecSpawn(String name, Location loc) {
		LinkedHashMap<String, Object> map = plugin.transformLocation(loc);
		LinkedHashMap<String, Object> arena = (LinkedHashMap<String, Object>) data.getValue(name);
		ArrayList<LinkedHashMap<String, Object>> specspawns = (ArrayList<LinkedHashMap<String, Object>>) arena.get("specspawns");
		specspawns.add(map); 
		arena.put("specspawns", specspawns);
		data.setValue(name, arena);
	}
	
	@SuppressWarnings("unchecked")
	public void removeBlueSpawns(String name) {
		LinkedHashMap<String, Object> arena = (LinkedHashMap<String, Object>) data.getValue(name);
		ArrayList<LinkedHashMap<String, Object>> bluespawns = new ArrayList<LinkedHashMap<String,Object>>();
		arena.put("bluespawns", bluespawns);
		data.setValue(name, arena);
	}
	@SuppressWarnings("unchecked")
	public void removeRedSpawns(String name) {
		LinkedHashMap<String, Object> arena = (LinkedHashMap<String, Object>) data.getValue(name);
		ArrayList<LinkedHashMap<String, Object>> redspawns = new ArrayList<LinkedHashMap<String,Object>>();
		arena.put("redspawns", redspawns);
		data.setValue(name, arena);
	}
	@SuppressWarnings("unchecked")
	public void removeSpecSpawns(String name) {
		LinkedHashMap<String, Object> arena = (LinkedHashMap<String, Object>) data.getValue(name);
		ArrayList<LinkedHashMap<String, Object>> specspawns = new ArrayList<LinkedHashMap<String,Object>>();
		arena.put("specspawns", specspawns);
		data.setValue(name, arena);
	}
	
	public void remove(String name) {
		data.removeValue(name);
		active.remove(name);
	}
}
