package me.blablubbabc.paintball;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import me.blablubbabc.BlaDB.Register;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class PlayerManager {
	private static Paintball plugin;
	private static  Register data;
	private HashMap<Player, Location> locations;
	private HashMap<Player, ItemStack[]> invContent;
	private HashMap<Player, ItemStack[]> invArmor;
	
	public ArrayList<String> possibleValues;
	
	public PlayerManager(Paintball pl) {
		plugin = pl;
		data = new Register();
		if(plugin.data.getRegister("players") == null) saveData();
		else data = plugin.data.getRegister("players");
		for(Player p : plugin.getServer().getOnlinePlayers()) {
			addPlayer(p.getName());
		}
		locations = new HashMap<Player, Location>();
		invContent = new HashMap<Player, ItemStack[]>();
		invArmor = new HashMap<Player, ItemStack[]>();
		
		possibleValues = new ArrayList<String>(setPossibleValues());
	}
	
	private ArrayList<String> setPossibleValues() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("points"); list.add("shots"); list.add("hits"); list.add("teamattacks"); list.add("kills"); list.add("deaths"); list.add("wins"); list.add("looses"); list.add("money");
		return list;
	}
	//METHODS
	//SETTER
	public void saveData() {
		plugin.data.addRegister("players", data);
		plugin.data.saveFile();
	}
	public void addPlayer(String name) {
		LinkedHashMap<String, Object> player = new LinkedHashMap<String, Object>();
		//LinkedHashMap<String, Object> loc = new LinkedHashMap<String, Object>();
		if(data.getValue(name) == null) {
			player.put("points", 0);
			player.put("shots", 0);
			player.put("hits", 0);
			player.put("teamattacks", 0);
			player.put("kills", 0);
			player.put("deaths", 0);
			player.put("wins", 0);
			player.put("looses", 0);
			player.put("money", 0);
			//player.put("location", loc);
			//UPDATE_CODE 1.0.5->1.0.6
			if(player.containsKey("location")) player.remove("location");
			
			data.setValue(name, player);
			saveData();
		}
		
	}
	
	public LinkedHashMap<String, Object> getData() {
		return data.getData();
	}
	
	public void resetData() {
		for(String name : data.getData().keySet()) {
			LinkedHashMap<String, Object> player = new LinkedHashMap<String, Object>();
			//LinkedHashMap<String, Object> loc = new LinkedHashMap<String, Object>();
			player.put("points", 0);
			player.put("shots", 0);
			player.put("hits", 0);
			player.put("teamattacks", 0);
			player.put("kills", 0);
			player.put("deaths", 0);
			player.put("wins", 0);
			player.put("looses", 0);
			player.put("money", 0);
			//player.put("location", loc);
			
			data.setValue(name, player);
			saveData();
		}
	}
	
	public boolean exists(String name) {
		if(data.getValue(name) != null) return true;
		else return false;
	}
	
	
	@SuppressWarnings("unchecked")
	public void addPoints(String name, int points) {
		if(data.getValue(name) == null) addPlayer(name);
		LinkedHashMap<String, Object> player = (LinkedHashMap<String, Object>) data.getValue(name);
		player.put("points", ((Integer) player.get("points") + points));
		data.setValue(name, player);
	}
	@SuppressWarnings("unchecked")
	public void addShots(String name, int shots) {
		if(data.getValue(name) == null) addPlayer(name);
		LinkedHashMap<String, Object> player = (LinkedHashMap<String, Object>) data.getValue(name);
		player.put("shots", ((Integer) player.get("shots") + shots));
		data.setValue(name, player);
	}
	@SuppressWarnings("unchecked")
	public void addHits(String name, int hits) {
		if(data.getValue(name) == null) addPlayer(name);
		LinkedHashMap<String, Object> player = (LinkedHashMap<String, Object>) data.getValue(name);
		player.put("hits", ((Integer) player.get("hits") + hits));
		data.setValue(name, player);
	}
	@SuppressWarnings("unchecked")
	public void addTeamattacks(String name, int attacks) {
		if(data.getValue(name) == null) addPlayer(name);
		LinkedHashMap<String, Object> player = (LinkedHashMap<String, Object>) data.getValue(name);
		player.put("teamattacks", ((Integer) player.get("teamattacks") + attacks));
		data.setValue(name, player);
	}
	@SuppressWarnings("unchecked")
	public void addkills(String name, int kills) {
		if(data.getValue(name) == null) addPlayer(name);
		LinkedHashMap<String, Object> player = (LinkedHashMap<String, Object>) data.getValue(name);
		player.put("kills", ((Integer) player.get("kills") + kills));
		data.setValue(name, player);
	}
	@SuppressWarnings("unchecked")
	public void addDeaths(String name, int deaths) {
		if(data.getValue(name) == null) addPlayer(name);
		LinkedHashMap<String, Object> player = (LinkedHashMap<String, Object>) data.getValue(name);
		player.put("deaths", ((Integer) player.get("deaths") + deaths));
		data.setValue(name, player);
	}
	@SuppressWarnings("unchecked")
	public void addWins(String name, int wins) {
		if(data.getValue(name) == null) addPlayer(name);
		LinkedHashMap<String, Object> player = (LinkedHashMap<String, Object>) data.getValue(name);
		player.put("wins", ((Integer) player.get("wins") + wins));
		data.setValue(name, player);
	}
	@SuppressWarnings("unchecked")
	public void addLooses(String name, int looses) {
		if(data.getValue(name) == null) addPlayer(name);
		LinkedHashMap<String, Object> player = (LinkedHashMap<String, Object>) data.getValue(name);
		player.put("looses", ((Integer) player.get("looses") + looses));
		data.setValue(name, player);
	}
	@SuppressWarnings("unchecked")
	public void addMoney(String name, int money) {
		if(data.getValue(name) == null) addPlayer(name);
		LinkedHashMap<String, Object> player = (LinkedHashMap<String, Object>) data.getValue(name);
		player.put("money", ((Integer) player.get("money") + money));
		data.setValue(name, player);
	}
	//GETTER
	@SuppressWarnings("unchecked")
	public LinkedHashMap<String, Object> getStats(String name) {
		if(data.getValue(name) == null) addPlayer(name);	
		LinkedHashMap<String, Object> player = (LinkedHashMap<String, Object>) data.getValue(name);
		return player;
	}
	
	
	public Location getLoc(Player player) {
		if(locations.get(player) != null) return locations.get(player);
		else return null;
	}
	public ItemStack[] getInvContent(Player player) {
		return invContent.get(player);
	}
	public ItemStack[] getInvArmor(Player player) {
		return invArmor.get(player);
	}
	
	//UPDATES
	@SuppressWarnings("unchecked")
	public void setPoints(String name, int points) {
		if(data.getValue(name) == null) addPlayer(name);
		LinkedHashMap<String, Object> player = (LinkedHashMap<String, Object>) data.getValue(name);
		player.put("points", points);
		data.setValue(name, player);
	}
	@SuppressWarnings("unchecked")
	public void setShots(String name, int shots) {
		if(data.getValue(name) == null) addPlayer(name);
		LinkedHashMap<String, Object> player = (LinkedHashMap<String, Object>) data.getValue(name);
		player.put("shots", shots);
		data.setValue(name, player);
	}
	@SuppressWarnings("unchecked")
	public void setKills(String name, int kills) {
		if(data.getValue(name) == null) addPlayer(name);
		LinkedHashMap<String, Object> player = (LinkedHashMap<String, Object>) data.getValue(name);
		player.put("kills", kills);
		data.setValue(name, player);
	}
	@SuppressWarnings("unchecked")
	public void setDeaths(String name, int deaths) {
		if(data.getValue(name) == null) addPlayer(name);
		LinkedHashMap<String, Object> player = (LinkedHashMap<String, Object>) data.getValue(name);
		player.put("deaths", deaths);
		data.setValue(name, player);
	}
	@SuppressWarnings("unchecked")
	public void setWins(String name, int wins) {
		if(data.getValue(name) == null) addPlayer(name);
		LinkedHashMap<String, Object> player = (LinkedHashMap<String, Object>) data.getValue(name);
		player.put("wins", wins);
		data.setValue(name, player);
	}
	@SuppressWarnings("unchecked")
	public void setLooses(String name, int looses) {
		if(data.getValue(name) == null) addPlayer(name);
		LinkedHashMap<String, Object> player = (LinkedHashMap<String, Object>) data.getValue(name);
		player.put("looses", looses);
		data.setValue(name, player);
	}
	@SuppressWarnings("unchecked")
	public void setMoney(String name, int money) {
		if(data.getValue(name) == null) addPlayer(name);
		LinkedHashMap<String, Object> player = (LinkedHashMap<String, Object>) data.getValue(name);
		player.put("money", money);
		data.setValue(name, player);
	}
	
	public void setLoc(Player player, Location loc) {
		locations.put(player, loc);
	}
	
	public void setInv(Player player, PlayerInventory inv) {
		invContent.put(player, inv.getContents());
		invArmor.put(player, inv.getArmorContents());
	}
	
	
	@SuppressWarnings("unchecked")
	public void setIntValue(String name, String value, int valueInt) {
		if(data.getValue(name) == null) addPlayer(name);
		if (possibleValues.contains(value.toLowerCase())) {
			LinkedHashMap<String, Object> player = (LinkedHashMap<String, Object>) data.getValue(name);
			player.put(value.toLowerCase(), valueInt);
			data.setValue(name, player);
		}
	}
	
}
