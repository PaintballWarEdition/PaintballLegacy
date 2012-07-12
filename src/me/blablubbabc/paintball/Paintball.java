package me.blablubbabc.paintball;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import me.blablubbabc.BlaDB.BlaDB;
import me.blablubbabc.paintball.Metrics.Graph;

import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

//This file is part of blablubbabc's paintball-Plugin. Do not redistribute or modify. Use it as it is. Usage on own risk. No warranties. No commercial usage!

public class Paintball extends JavaPlugin{
	public PlayerManager pm;
	public MatchManager mm;
	public EventListener listener;
	public Newsfeeder nf;
	public ArenaManager am;
	public Stats stats;
	public boolean active;
	public boolean softreload;
	
	//Config:
	//general:
	public int countdown;
	public int countdownInit;
	public int minPlayers;
	public int maxPlayers;
	public int lives;
	public int balls;
	public double speedmulti;
	public boolean listnames;
	public boolean chatnames;
	public boolean shop;
	public ArrayList<String> shopGoods;
	
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
	//- infinite mode
	
	
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
		
		getConfig().options().header("Use a value of -1 to give the players infinite balls or extras.");
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
		if(getConfig().get("Paintball.Match.Lives") == null)getConfig().set("Paintball.Match.Lives", 1);
		if(getConfig().get("Paintball.Match.Balls") == null)getConfig().set("Paintball.Match.Balls", 50);
		if(getConfig().get("Paintball.Match.Minimum players") == null)getConfig().set("Paintball.Match.Minimum players", 2);
		if(getConfig().get("Paintball.Match.Maximum players") == null)getConfig().set("Paintball.Match.Maximum players", 1000);
		if(getConfig().get("Paintball.Match.Countdown.Time") == null)getConfig().set("Paintball.Match.Countdown.Time", 20);
		if(getConfig().get("Paintball.Match.Countdown.Delay") == null)getConfig().set("Paintball.Match.Countdown.Delay", 10);
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
		
		speedmulti = getConfig().getDouble("Paintball.Ball speed multi", 1.5);
		listnames = getConfig().getBoolean("Paintball.Colored listnames", true);
		chatnames = getConfig().getBoolean("Paintball.Colored chatnames", true);
		
		//shop:
		shop = getConfig().getBoolean("Paintball.Shop.enabled", true);
		shopGoods = (ArrayList<String>) getConfig().getList("Paintball.Shop.Goods", goodsDef);
		
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
		
		//METRICS
		try {
			Metrics metrics = new Metrics(this);
			
			//Custom Data:
			
			// Construct a graph, which can be immediately used and considered as valid
			Graph graph = metrics.createGraph("Players");
			
			//Effective players
			graph.addPlotter(new Metrics.Plotter("Ever played Paintball") {

				@Override
				public int getValue() {
					try {
						int number = 0;
						for(String name : pm.getData().keySet()) {
							LinkedHashMap<String, Object> player = (LinkedHashMap<String, Object>) data.getValue(name);
							if((Integer) player.get("shots") > 0) number++;
						}
						return number;
					} catch (Exception e) {
						// Failed to get the value :(
						return 0;
					}
				}
			});

			//Actual playing players (Lobby)
			graph.addPlotter(new Metrics.Plotter("Actual playing (lobby)") {

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

			metrics.start();
		} catch (IOException e) {
			// Failed to submit the stats :-(
		}
				
				
		log("["+this.toString()+"]" + " by blablubbabc enabled.");
		log("["+this.toString()+"]" + " Do not redistribute or modify. Use it as it is and not for commercial purposes! Usage on own risk. No warranties.");
		log("["+this.toString()+"]" + " If you like this, give feedback and donate at wir-sind-wir.de/lukas");
	}

	public void onDisable(){
		mm.forceReload();
		log(this.toString() + " disabled!");
	}
	
	public void log(String message) {
		System.out.println(message);
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
	



}
