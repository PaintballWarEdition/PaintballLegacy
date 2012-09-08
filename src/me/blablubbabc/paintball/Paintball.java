package me.blablubbabc.paintball;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import me.blablubbabc.BlaDB.BlaDB;
import me.blablubbabc.paintball.Metrics.Graph;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

//This file is part of blablubbabc's paintball-Plugin. Do not redistribute or modify. Use it as it is. Usage on own risk. No warranties. No commercial usage!

public class Paintball extends JavaPlugin{
	public PlayerManager pm;
	public MatchManager mm;
	public EventListener listener;
	public Newsfeeder nf;
	public ArenaManager am;
	public Translator t;
	public Stats stats;
	public boolean active;
	public boolean softreload;
	public int lobbyspawn;
	
	//ChatColors
	public ChatColor gray = ChatColor.GRAY;
	public ChatColor gold = ChatColor.GOLD;
	public ChatColor green = ChatColor.GREEN;
	public ChatColor aqua = ChatColor.AQUA;
	public ChatColor red = ChatColor.RED;
	public ChatColor blue = ChatColor.BLUE;
	public ChatColor yellow = ChatColor.YELLOW;
	public ChatColor light_purple = ChatColor.LIGHT_PURPLE;
	public ChatColor dark_green = ChatColor.DARK_GREEN;
	
	public ChatColor bold = ChatColor.BOLD;
	
	//CONFIG:
	//general:
	public String local;
	public int countdown;
	public int countdownInit;
	public int countdownStart;
	public int minPlayers;
	public int maxPlayers;
	public int lives;
	public int balls;
	public double speedmulti;
	public boolean listnames;
	public boolean chatnames;
	public boolean shop;
	public ArrayList<String> shopGoods;
	public ArrayList<String> allowedCommands;
	public boolean saveInventory;
	public boolean onlyRandom;
	public boolean autoRandom;
	public boolean noPerms;
	public boolean damage;
	public boolean allowMelee;
	public int meleeDamage;
	public boolean autoLobby;
	
	//lobby join checks
	public boolean checkInventory;
	public boolean checkGamemode;
	public boolean checkFlymode;
	public boolean checkBurning;
	public boolean checkHealth;
	public boolean checkFood;
	public boolean checkEffects;
	
	//points und cash
	public int pointsPerKill;
	public int pointsPerHit;
	public int pointsPerTeamattack;
	public int pointsPerWin;
	public int pointsPerRound;
	public int cashPerKill;
	public int cashPerHit;
	public int cashPerWin;
	public int cashPerRound;
	
	//Extras:
	public boolean grenades;
	public int grenadeTime;
	public double grenadeSpeed;
	public int grenadeAmount;
	
	public boolean airstrike;
	public int airstrikeRange;
	public int airstrikeBombs;
	public int airstrikeAmount;
	public int airstrikeHeight;
	
	//TODO
	//shop-items
	//Extras: Luftschlag, verschiedene munition, airdrops, punktesystem abhängig von spieler im match, evt. bester spieler der runde, mehr extras :D
	//schneemann-geschütz
	//Lobby schilder für points und rank und tops
	//map rotation with size-matters
	//map vote
	//map restriction
	//config in-game ändern
	//grenade
	//luftschlag
	//friendly-fire on/off
	//friendly-fire-detection on/off
	//mySQL support für online stats
	//- arena disable/enable
	//- airstrike finalMark bleibt
	
	//important:
	//- map list mit übersicht, ob ready, und warum nicht ready (/)
	//- admin: next <name> zum arena festlegen. (/)
	//- config-header-beschreibung
	//- infinite eggs bug! (/)
	//- count nades and airstrike seperate!
	//- infinite mode (/)
	//- invisible bug
	//- arena not ready bug (/)
	//- admin cash und rank kommando überprüfen, ob ein spieler mit namen args existiert
	
	
	//Permissions:
	//paintball.general
	//paintball.arena
	//paintball.admin
	//paintball.shop.not<id> (starting with 1)
	
	
	
	public BlaDB data;
	
	@SuppressWarnings("unchecked")
	public void onEnable(){	
		
		//CONFIG
		ArrayList<String> goodsDef = new ArrayList<String>();
		goodsDef.add("10-Balls-15");
		goodsDef.add("50-Balls-65");
		goodsDef.add("100-Balls-120");
		goodsDef.add("1-Grenade-20");
		goodsDef.add("1-Airstrike-100");
		
		allowedCommands = new ArrayList<String>();
		
		
		getConfig().options().header("Use a value of -1 to give the players infinite balls or extras.");
		if(getConfig().get("Paintball.Language") == null)getConfig().set("Paintball.Language", "enUS");
		if(getConfig().get("Paintball.No Permissions") == null)getConfig().set("Paintball.No Permissions", false);
		if(getConfig().get("Paintball.Auto Lobby") == null)getConfig().set("Paintball.Auto Lobby", false);
		if(getConfig().get("Paintball.Points per Kill") == null)getConfig().set("Paintball.Points per Kill", 2);
		if(getConfig().get("Paintball.Points per Hit") == null)getConfig().set("Paintball.Points per Hit", 1);
		if(getConfig().get("Paintball.Points per Team-Attack") == null)getConfig().set("Paintball.Points per Team-Attack", -1);
		if(getConfig().get("Paintball.Points per Win") == null)getConfig().set("Paintball.Points per Win", 5);
		if(getConfig().get("Paintball.Points per Round") == null)getConfig().set("Paintball.Points per Round", 1);
		if(getConfig().get("Paintball.Cash per Kill") == null)getConfig().set("Paintball.Cash per Kill", 10);
		if(getConfig().get("Paintball.Cash per Hit") == null)getConfig().set("Paintball.Cash per Hit", 0);
		if(getConfig().get("Paintball.Cash per Win") == null)getConfig().set("Paintball.Cash per Win", 10);
		if(getConfig().get("Paintball.Cash per Round") == null)getConfig().set("Paintball.Cash per Round", 0);
		if(getConfig().get("Paintball.Ball speed multi") == null)getConfig().set("Paintball.Ball speed multi", 1.5);
		if(getConfig().get("Paintball.Colored listnames") == null)getConfig().set("Paintball.Colored listnames", true);
		if(getConfig().get("Paintball.Colored chatnames") == null)getConfig().set("Paintball.Colored chatnames", true);
		if(getConfig().get("Paintball.Only Random") == null)getConfig().set("Paintball.Only Random", false);
		if(getConfig().get("Paintball.Auto Random") == null)getConfig().set("Paintball.Auto Random", true);
		if(getConfig().get("Paintball.Allowed Commands") == null)getConfig().set("Paintball.Allowed Commands", allowedCommands);
		//lobby join checks
		if(getConfig().get("Paintball.Lobby join.Checks.Inventory") == null)getConfig().set("Paintball.Lobby join.Checks.Inventory", true);
		if(getConfig().get("Paintball.Lobby join.Checks.Inventory Save") == null)getConfig().set("Paintball.Lobby join.Checks.Inventory Save", true);
		if(getConfig().get("Paintball.Lobby join.Checks.Gamemode") == null)getConfig().set("Paintball.Lobby join.Checks.Gamemode", true);
		if(getConfig().get("Paintball.Lobby join.Checks.Creative-Fly-Mode") == null)getConfig().set("Paintball.Lobby join.Checks.Creative-Fly-Mode", true);
		if(getConfig().get("Paintball.Lobby join.Checks.Burning, Falling, Immersion") == null)getConfig().set("Paintball.Lobby join.Checks.Burning, Falling, Immersion", true);
		if(getConfig().get("Paintball.Lobby join.Checks.Health") == null)getConfig().set("Paintball.Lobby join.Checks.Health", true);
		if(getConfig().get("Paintball.Lobby join.Checks.FoodLevel") == null)getConfig().set("Paintball.Lobby join.Checks.FoodLevel", true);
		if(getConfig().get("Paintball.Lobby join.Checks.Effects") == null)getConfig().set("Paintball.Lobby join.Checks.Effects", true);
		
		if(getConfig().get("Paintball.Match.Damage") == null)getConfig().set("Paintball.Match.Damage", false);
		if(getConfig().get("Paintball.Match.Allow Melee") == null)getConfig().set("Paintball.Match.Allow Melee", true);
		if(getConfig().get("Paintball.Match.Melee Damage") == null)getConfig().set("Paintball.Match.Melee Damage", 1);
		if(getConfig().get("Paintball.Match.Lives") == null)getConfig().set("Paintball.Match.Lives", 1);
		if(getConfig().get("Paintball.Match.Balls") == null)getConfig().set("Paintball.Match.Balls", 50);
		if(getConfig().get("Paintball.Match.Minimum players") == null)getConfig().set("Paintball.Match.Minimum players", 2);
		if(getConfig().get("Paintball.Match.Maximum players") == null)getConfig().set("Paintball.Match.Maximum players", 1000);
		if(getConfig().get("Paintball.Match.Countdown.Time") == null)getConfig().set("Paintball.Match.Countdown.Time", 20);
		if(getConfig().get("Paintball.Match.Countdown.Delay") == null)getConfig().set("Paintball.Match.Countdown.Delay", 10);
		if(getConfig().get("Paintball.Match.CountdownStart.Time") == null)getConfig().set("Paintball.Match.CountdownStart.Time", 5);
		if(getConfig().get("Paintball.Extras.Grenades.enabled") == null)getConfig().set("Paintball.Extras.Grenades.enabled", true);
		if(getConfig().get("Paintball.Extras.Grenades.Time-Radius in Ticks (= 1/20 sec)") == null)getConfig().set("Paintball.Extras.Grenades.Time-Radius in Ticks (= 1/20 sec)", 60);
		if(getConfig().get("Paintball.Extras.Grenades.Speed multi") == null)getConfig().set("Paintball.Extras.Grenades.Speed multi", 1.0);
		if(getConfig().get("Paintball.Extras.Grenades.Amount") == null)getConfig().set("Paintball.Extras.Grenades.Amount", 0);
		if(getConfig().get("Paintball.Extras.Airstrike.enabled") == null)getConfig().set("Paintball.Extras.Airstrike.enabled", true);
		if(getConfig().get("Paintball.Extras.Airstrike.Height") == null)getConfig().set("Paintball.Extras.Airstrike.Height", 15);
		if(getConfig().get("Paintball.Extras.Airstrike.Range (half)") == null)getConfig().set("Paintball.Extras.Airstrike.Range (half)", 30);
		if(getConfig().get("Paintball.Extras.Airstrike.Bombs") == null)getConfig().set("Paintball.Extras.Airstrike.Bombs", 15);
		if(getConfig().get("Paintball.Extras.Airstrike.Amount") == null)getConfig().set("Paintball.Extras.Airstrike.Amount", 0);
		if(getConfig().get("Paintball.Shop.enabled") == null)getConfig().set("Paintball.Shop.enabled", true);
		if(getConfig().get("Paintball.Shop.Goods") == null)getConfig().set("Paintball.Shop.Goods", goodsDef);
		saveConfig();
		
		//points+cash:
		pointsPerKill = getConfig().getInt("Paintball.Points per Kill", 2);
		pointsPerHit = getConfig().getInt("Paintball.Points per Hit", 1);
		pointsPerTeamattack = getConfig().getInt("Paintball.Points per Team-Attack", -1);
		pointsPerWin = getConfig().getInt("Paintball.Points per Win", 5);
		pointsPerRound = getConfig().getInt("Paintball.Points per Round", 1);
		cashPerKill = getConfig().getInt("Paintball.Cash per Kill", 10);
		cashPerHit = getConfig().getInt("Paintball.Cash per Hit", 0);
		cashPerWin = getConfig().getInt("Paintball.Cash per Win", 10);
		cashPerRound = getConfig().getInt("Paintball.Cash per Round", 0);
		
		//gerneral:
		damage = getConfig().getBoolean("Paintball.Match.Damage", false);
		allowMelee = getConfig().getBoolean("Paintball.Match.Allow Melee", true);
		meleeDamage = getConfig().getInt("Paintball.Match.Melee Damage", 1);
		if(meleeDamage < 1) meleeDamage = 1;
		local = getConfig().getString("Paintball.Language", "enUS");
		noPerms = getConfig().getBoolean("Paintball.No Permissions", false);
		autoLobby = getConfig().getBoolean("Paintball.Auto Lobby", false);
		allowedCommands = (ArrayList<String>) getConfig().getList("Paintball.Allowed Commands", allowedCommands);
		
		lives = getConfig().getInt("Paintball.Match.Lives", 1);
		if(lives < 1) lives = 1;
		balls = getConfig().getInt("Paintball.Match.Balls", 50);
		if(balls < -1) balls = -1;
		minPlayers = getConfig().getInt("Paintball.Match.Minimum players", 2);
		if(minPlayers < 2) minPlayers = 2;
		maxPlayers = getConfig().getInt("Paintball.Match.Maximum players", 1000);
		if(maxPlayers < 2) maxPlayers = 2;
		//countdown:
		countdown = getConfig().getInt("Paintball.Match.Countdown.Time", 20);
		if(countdown < 0) countdown = 0;
		countdownInit = getConfig().getInt("Paintball.Match.Countdown.Delay", 10);
		if(countdownInit < 0) countdownInit = 0;
		countdownStart = getConfig().getInt("Paintball.Match.CountdownStart.Time", 5);
		if(countdownStart < 0) countdownStart = 0;
		
		speedmulti = getConfig().getDouble("Paintball.Ball speed multi", 1.5);
		listnames = getConfig().getBoolean("Paintball.Colored listnames", true);
		chatnames = getConfig().getBoolean("Paintball.Colored chatnames", true);
		onlyRandom = getConfig().getBoolean("Paintball.Only Random", false);
		autoRandom = getConfig().getBoolean("Paintball.Auto Random", true);
		
		//shop:
		shop = getConfig().getBoolean("Paintball.Shop.enabled", true);
		shopGoods = (ArrayList<String>) getConfig().getList("Paintball.Shop.Goods", goodsDef);
		
		//lobby join checks
		checkInventory = getConfig().getBoolean("Paintball.Lobby join.Checks.Inventory", true);
		saveInventory = getConfig().getBoolean("Paintball.Lobby join.Checks.Inventory Save", true);
		checkGamemode = getConfig().getBoolean("Paintball.Lobby join.Checks.Gamemode", true);
		checkFlymode = getConfig().getBoolean("Paintball.Lobby join.Checks.Creative-Fly-Mode", true);
		checkBurning = getConfig().getBoolean("Paintball.Lobby join.Checks.Burning, Falling, Immersion", true);
		checkHealth = getConfig().getBoolean("Paintball.Lobby join.Checks.Health", true);
		checkFood = getConfig().getBoolean("Paintball.Lobby join.Checks.FoodLevel", true);
		checkEffects = getConfig().getBoolean("Paintball.Lobby join.Checks.Effects", true);
		
		
		//Extras
		grenades = getConfig().getBoolean("Paintball.Extras.Grenades.enabled", true);
		grenadeTime = getConfig().getInt("Paintball.Extras.Grenades.Time-Radius in Ticks (= 1/20 sec)", 60);
		if(grenadeTime < 0) grenadeTime = 0;
		grenadeSpeed = getConfig().getDouble("Paintball.Extras.Grenades.Speed multi", 1.0);
		grenadeAmount = getConfig().getInt("Paintball.Extras.Grenades.Amount", 0);
		if(grenadeAmount < -1) grenadeAmount = -1;
		
		airstrike = getConfig().getBoolean("Paintball.Extras.Airstrike.enabled", true);
		airstrikeHeight = getConfig().getInt("Paintball.Extras.Airstrike.Height", 15);
		if(airstrikeHeight < 2) airstrikeHeight = 2;
		airstrikeRange = getConfig().getInt("Paintball.Extras.Airstrike.Range (half)", 30);
		airstrikeBombs = getConfig().getInt("Paintball.Extras.Airstrike.Bombs", 15);
		if(airstrikeBombs < 0) airstrikeBombs = 0;
		airstrikeAmount = getConfig().getInt("Paintball.Extras.Airstrike.Amount", 0);
		if(airstrikeAmount < -1) airstrikeAmount = -1;
		
		//DB
		data = new BlaDB("paintball", this.getDataFolder().toString());
		loadDB();
		//TRANSLATOR
		t = new Translator(this, local);
		if(!t.success) {
			log("ERROR: Couldn't find/load the default language file. Disables now..");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		//WAKE TEAM-ENUMS
		Lobby.values();
		//PLAYERMANAGER
		pm = new PlayerManager(this);
		//Newsfeeder
		nf = new Newsfeeder(this);
		//MATCHMANAGER|LISTENER
		mm = new MatchManager(this);
		listener = new EventListener(this);
		//ARENAMANAGER
		am = new ArenaManager(this);
		//STATS
		stats = new Stats(this);
		getServer().getPluginManager().registerEvents(listener, this);
		//COMMANDS
		CommandExecutor cm = new CommandManager(this);
		getCommand("pb").setExecutor(cm);
		
		active = true;
		softreload = false;
		lobbyspawn = 0;
		
		//autoLobby
		if(autoLobby) {
			for(Player player : getServer().getOnlinePlayers()) {
				//Lobby vorhanden?
				if(getLobbySpawns().size() == 0) {
					player.sendMessage(t.getString("NO_LOBBY_FOUND"));
					continue;
				}
				
				//inventory
				if(saveInventory) {
					pm.setInv(player, player.getInventory());
					player.sendMessage(t.getString("INVENTORY_SAVED"));
				}
				//save Location
				pm.setLoc(player, player.getLocation());
				//lobby add
				Lobby.LOBBY.addMember(player);
				nf.join(player.getName());
				
				joinLobby(player);
			}
		}
		
		//METRICS
		try {
			Metrics metrics = new Metrics(this);
			
			//Custom Data:
			
			//Default graph:
			//Actual playing players (Lobby)
			metrics.addCustomData(new Metrics.Plotter("Actual playing (lobby)") {

				@Override
				public int getValue() {
					try {
						return Lobby.LOBBY.number();
					} catch (Exception e) {
						// Failed to get the value :(
						return 0;
					}
				}
			});
			
			//Maximum playing (lobby) since last update
			metrics.addCustomData(new Metrics.Plotter("Maximum playing (lobby) since last update") {

				@Override
				public int getValue() {
					try {
						//get max:
						int max = Lobby.LOBBY.maxNumber();
						//reset max:
						Lobby.resetMaxPlayers();
						return max;
					} catch (Exception e) {
						// Failed to get the value :(
						//reset max:
						Lobby.resetMaxPlayers();
						return 0;
					}
				}
			});
			
			//Graph 2
			Graph graph = metrics.createGraph("Players ever played Paintball");
			
			//Players ever played Paintball Plotter
			graph.addPlotter(new Metrics.Plotter("Ever played Paintball") {

				@Override
				public int getValue() {
					try {
						int number = 0;
						for(String name : pm.getData().keySet()) {
							LinkedHashMap<String, Object> player = new LinkedHashMap<String, Object>();
							player = (LinkedHashMap<String, Object>) pm.getData().get(name);
							if((Integer) player.get("shots") > 0) number++;
						}
						return number;
					} catch (Exception e) {
						// Failed to get the value :(
						return 0;
					}
				}
			});

			metrics.start();
		} catch (IOException e) {
			Lobby.resetMaxPlayers();
			// Failed to submit the stats :-(
		}
				
				
		log("By blablubbabc enabled.");
		log("Do not redistribute or modify. Use it as it is and not for commercial purposes! Usage on own risk. No warranties.");
		log("If you like this, give feedback and donate at wir-sind-wir.de/lukas");
	}

	public void onDisable(){
		if(mm != null) mm.forceReload();
		log("Disabled!");
	}
	
	public void log(String message) {
		System.out.println("["+this.getName()+"] "+message);
	}
	
	public void reload() {
		reloadConfig();
		getServer().getPluginManager().disablePlugin(this);
		getServer().getPluginManager().enablePlugin(this);
	}
	
	//METHODS LOBBYSPAWNS
	private void loadDB() {
		ArrayList<LinkedHashMap<String, Object>> lobbyspawns = new ArrayList<LinkedHashMap<String, Object>>();
		if(data.getValue("lobbyspawns") == null) data.setValue("lobbyspawns", lobbyspawns);

		data.saveFile();	
	}
	
	
	@SuppressWarnings("unchecked")
	public void addLobbySpawn(Location loc) {
		LinkedHashMap<String, Object> map = transformLocation(loc);
		ArrayList<LinkedHashMap<String, Object>> lobbyspawns = (ArrayList<LinkedHashMap<String, Object>>) data.getValue("lobbyspawns");
		lobbyspawns.add(map);
		data.setValue("lobbyspawns", lobbyspawns);
		
		data.saveFile();	
	}
	public void deleteLobbySpawns() {
		ArrayList<LinkedHashMap<String, Object>> lobbyspawns = new ArrayList<LinkedHashMap<String, Object>>();
		data.setValue("lobbyspawns", lobbyspawns);
		
		data.saveFile();	
	}
	@SuppressWarnings("unchecked")
	public ArrayList<LinkedHashMap<String, Object>> getLobbySpawns() {
		ArrayList<LinkedHashMap<String, Object>> lobbyspawns = (ArrayList<LinkedHashMap<String, Object>>) data.getValue("lobbyspawns");
		return lobbyspawns;
	}
	
	////////////////////////////////////
	//UTILS
	////////////////////////////////////
	
	public LinkedHashMap<String, Object> transformLocation(Location loc) {
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("world", loc.getWorld().getName());
		map.put("x", loc.getBlockX());
		map.put("y", loc.getBlockY());
		map.put("z", loc.getBlockZ());
		return map;
	}
	public Location transformLocation(LinkedHashMap<String, Object> map){
		String world = (String) map.get("world");
		int x = (Integer) map.get("x");
		int y = (Integer) map.get("y");
		int z = (Integer) map.get("z");
		Location loc = new Location(getServer().getWorld(world), x, y, z);
		return loc;
	}
	
	public int getNextLobbySpawn() {
		lobbyspawn++;
		if(lobbyspawn > (getLobbySpawns().size()-1)) lobbyspawn = 0;
		return lobbyspawn;
	}
	
	public void joinLobby(Player player) {
		if(!isEmpty(player)) clearInv(player);
		//gamemode
		if(!player.getGameMode().equals(GameMode.SURVIVAL)) player.setGameMode(GameMode.SURVIVAL);
		//flymode (built-in)
		if(player.getAllowFlight()) player.setAllowFlight(false);
		if(player.isFlying()) player.setFlying(false);
		//feuer
		if(player.getFireTicks() > 0) player.setFireTicks(0);
		//Health + Food
		if(player.getHealth() < 20) player.setHealth(20);
		if(player.getFoodLevel() < 20) player.setFoodLevel(20);
		//effekte entfernen
		if(player.getActivePotionEffects().size() > 0) {
			ArrayList<PotionEffect> effects = new ArrayList<PotionEffect>();
			for(PotionEffect eff : player.getActivePotionEffects()) {
				effects.add(eff);
			}
			for(PotionEffect eff :effects) {
				player.removePotionEffect(eff.getType());
			}	
		}
		//Vehicle
		if(player.isInsideVehicle()) player.leaveVehicle();
		//listname
		if(listnames) player.setPlayerListName(null);
		//Lobbyteleport
		player.teleport(transformLocation(getLobbySpawns().get(getNextLobbySpawn())));
	}
	
	public void leaveLobby(Player player, boolean messages, boolean teleport, boolean restoreInventory) {
		//lobby remove:
		Lobby.remove(player);
		//inventory:
		//clear inventory
		clearInv(player);
		//restore saved inventory
		if(restoreInventory && saveInventory) {
			//PlayerInventory
			player.getInventory().setContents(pm.getInvContent(player));
			player.getInventory().setArmorContents(pm.getInvArmor(player));
			player.sendMessage(t.getString("INVENTORY_RESTORED"));
		}
		//teleport:
		if(teleport) player.teleport(pm.getLoc(player));
		if (messages) {
			//messages:
			player.sendMessage(t.getString("YOU_LEFT_LOBBY"));
			nf.leave(player.getName());
		}
	}
	
	public boolean isEmpty(Player p) {
		for(ItemStack i : p.getInventory()) {
			if(i == null) continue;
			if(i.getTypeId() != 0) return false;
		}
		for(ItemStack i : p.getInventory().getArmorContents()) {
			if(i == null) continue;
			if(i.getTypeId() != 0) return false;
		}
		return true;
	}
	
	public void clearInv(Player p) {
		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
	}
	



}
