package me.blablubbabc.paintball;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Match {

	private Paintball plugin;
	private HashMap<Player, Integer> redT = new HashMap<Player, Integer>();
	private HashMap<Player, Integer> blueT = new HashMap<Player, Integer>();
	//STATS
	private HashMap<String, Integer> shots = new HashMap<String, Integer>();
	private HashMap<String, Integer> hits = new HashMap<String, Integer>();
	private HashMap<String, Integer> kills = new HashMap<String, Integer>();
	private HashMap<String, Integer> deaths = new HashMap<String, Integer>();
	private HashMap<String, Integer> teamattacks = new HashMap<String, Integer>();
	private HashMap<String, Integer> grenades = new HashMap<String, Integer>();
	private HashMap<String, Integer> airstrikes = new HashMap<String, Integer>();

	private Set<Player> spec;
	private ArrayList<Player> players;
	private HashMap<String, Location> playersLoc;
	private ArrayList<String> left;
	private String arena;
	private boolean matchOver;
	private int taskId;
	private int count;
	private int spawnBlue;
	private int spawnRed;
	private int spawnSpec;

	private int setting_balls;
	private int setting_grenades;
	private int setting_airstrikes;
	private int setting_lives;
	private int setting_respawns;




	public boolean started;

	public Match(final Paintball plugin, Set<Player> red, Set<Player> blue, Set<Player> spec, Set<Player> random, String arena) {
		this.plugin = plugin;
		this.arena = arena;
		this.players = new ArrayList<Player>();
		this.playersLoc = new HashMap<String, Location>();
		this.left = new ArrayList<String>();
		this.matchOver = false;
		this.started = false;

		this.spawnBlue = 0;
		this.spawnRed = 0;
		this.spawnSpec = 0;

		this.setting_balls = plugin.balls;
		this.setting_grenades = plugin.grenadeAmount;
		this.setting_airstrikes = plugin.airstrikeAmount;
		this.setting_lives = plugin.lives;
		this.setting_respawns = plugin.respawns;
		calculateSettings();

		//TEAMS
		for(Player p : red) {
			this.redT.put(p, plugin.lives);
			players.add(p);
		}
		for(Player p : blue) {
			this.blueT.put(p, plugin.lives);
			players.add(p);
		}
		this.spec = spec;

		//randoms:
		List<Player> rand = new ArrayList<Player>();
		for(Player p : random) {
			rand.add(p);
		}
		Collections.shuffle(rand);
		for(Player p : rand) {
			players.add(p);
			if(this.blueT.size() < this.redT.size()){
				this.blueT.put(p, plugin.lives);
			}
			else if(this.redT.size() <= this.blueT.size()){
				this.redT.put(p, plugin.lives);
			}
		}

		for(Player p : players) {
			//STATS
			this.shots.put(p.getName(), 0);
			this.hits.put(p.getName(), 0);
			this.kills.put(p.getName(), 0);
			this.deaths.put(p.getName(), 0);
			this.teamattacks.put(p.getName(), 0);
			this.grenades.put(p.getName(), 0);
			this.airstrikes.put(p.getName(), 0);

			plugin.checks(p);
			spawnPlayer(p);
		}

		for(Player p : this.spec) {
			plugin.checks(p);
			spawnSpec(p);
		}
		//colorchanges:
		changeAllColors();

		//WAITING TIMER:
		this.started = false;
		count = plugin.countdownStart;

		taskId = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable() {

			@Override
			public void run() {
				if( count == plugin.countdownStart && count > 0) {
					sendCountdown(count);
					count--;
					return;
				}
				if(( count % 10 ) == 0 && count > 3 )
				{
					//if above 3 and divisable by 10 message here
					sendCountdown(count);
				}

				if( count < 4 && count > 0)
				{
					//if below 4 message here (regardless of divisibility)
					sendCountdown(count);
				}
				count--;
				if( count < 1) {
					plugin.getServer().getScheduler().cancelTask(taskId);
					//START:
					started = true;
					//lives + start!:
					HashMap<String, String> vars = new HashMap<String, String>();
					vars.put("lives", String.valueOf(plugin.lives));
					if(plugin.lives == 1) plugin.nf.status(plugin.t.getString("MATCH_START_ONE_LIFE", vars));
					else plugin.nf.status(plugin.t.getString("MATCH_START_MORE_LIVES", vars));
					makeAllVisible();
				}
			}
		}, 0L, 20L);
	}

	private void sendCountdown(int counter) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("seconds", String.valueOf(counter));
		for(Player player : getAll()) {
			player.sendMessage(plugin.t.getString("COUNTDOWN", vars));
		}
	}

	//SPAWNS

	public void spawnPlayer(Player player) {
		if(redT.keySet().contains(player)) {
			ArrayList<Location> redspawns = plugin.am.getRedSpawns(arena);
			if(spawnRed > (redspawns.size()-1)) spawnRed = 0;
			Location loc = redspawns.get(spawnRed);
			player.teleport(loc);
			spawnRed++;
			//afk Location
			playersLoc.put(player.getName(), loc);
		} else if(blueT.keySet().contains(player)) {
			ArrayList<Location> bluespawns = plugin.am.getBlueSpawns(arena);
			if(spawnBlue > (bluespawns.size()-1)) spawnBlue = 0;
			Location loc = bluespawns.get(spawnBlue);
			player.teleport(loc);
			spawnBlue++;
			//afk Location
			playersLoc.put(player.getName(), loc);
		} else {
			return;
		}
		//INVENTORY
		player.getInventory().setHelmet(Lobby.getTeam(getTeamName(player)).helmet());
		if(plugin.balls > 0 ) player.getInventory().addItem(new ItemStack(Material.SNOW_BALL, plugin.balls));
		else if(plugin.balls == -1 ) player.getInventory().addItem(new ItemStack(Material.SNOW_BALL, 10));
		if(plugin.grenadeAmount > 0 ) player.getInventory().addItem(new ItemStack(Material.EGG, plugin.grenadeAmount));
		else if(plugin.grenadeAmount == -1 ) player.getInventory().addItem(new ItemStack(Material.EGG, 10));
		if(plugin.airstrikeAmount > 0 ) player.getInventory().addItem(new ItemStack(Material.STICK, plugin.airstrikeAmount));
		else if(plugin.airstrikeAmount == -1 ) player.getInventory().addItem(new ItemStack(Material.STICK, 10));
		//MESSAGE
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("team_color", Lobby.getTeam(getTeamName(player)).color().toString());
		vars.put("team", getTeamName(player));
		player.sendMessage(plugin.t.getString("BE_IN_TEAM", vars));
	}

	public void spawnSpec(Player player) {
		ArrayList<Location> specspawns = plugin.am.getSpecSpawns(arena);
		if(spawnSpec > (specspawns.size()-1)) spawnSpec = 0;
		player.teleport(specspawns.get(spawnSpec));
		spawnSpec++;
		//INVENTORY
		player.getInventory().setHelmet(Lobby.SPECTATE.helmet());
		//MESSAGE
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("team_color", Lobby.getTeam(player).color().toString());
		vars.put("team", Lobby.getTeam(player).getName());
		player.sendMessage(plugin.t.getString("BE_SPECTATOR", vars));
	}

	//INVENTORY
	private void calculateSettings() {
		HashMap<String, Integer> settings = plugin.am.getArenaSettings(arena);
		//BALLS
		setting_balls += settings.get("balls");
		if(setting_balls < -1) setting_balls = -1;
		//GRENADES
		setting_grenades += settings.get("grenades");
		if(setting_grenades < -1) setting_grenades = -1;
		//AIRSTRIKES
		setting_airstrikes += settings.get("airstrikes");
		if(setting_airstrikes < -1) setting_airstrikes = -1;
		//LIVES
		setting_lives += settings.get("lives");
		if(setting_lives < 1) setting_lives = 1;
		//RESPAWNS
		setting_respawns += settings.get("respawns");
		if(setting_respawns < -1) setting_respawns = -1;
	}

	public void makeAllVisible() {
		for(Player pl : getAll()) {
			for(Player p : getAll()) {
				if(!p.equals(pl)) pl.showPlayer(p);
			}	
		}
	}

	public void changeAllColors() {
		for(Player p : redT.keySet()) {
			//chatnames
			String n = plugin.red+p.getName();
			if(n.length() > 16) n = (String) n.subSequence(0, n.length() - (n.length()-16));
			/*if(plugin.chatnames) {
				p.setDisplayName(n+white);
			}*/
			//listnames
			if(plugin.listnames) {
				p.setPlayerListName(n);
			}
		}
		for(Player p : blueT.keySet()) {
			//chatnames
			String n = plugin.blue+p.getName();
			if(n.length() > 16) n = (String) n.subSequence(0, n.length() - (n.length()-16));
			/*if(plugin.chatnames) {
				p.setDisplayName(n+white);
			}*/
			//listnames
			if(plugin.listnames) {
				p.setPlayerListName(n);
			}
		}
	}

	public void undoAllColors() {
		for(Player p : getAllPlayer()) {
			/*if(plugin.chatnames) {
				p.setDisplayName(p.getName());
			}*/
			//listnames
			if(plugin.listnames) {
				if(!hasLeft(p)) p.setPlayerListName(null);
			}
		}
	}

	public int teamSizeRed() {
		return redT.size();
	}
	public int teamSizeBlue() {
		return blueT.size();
	}

	public String getArena() {
		return this.arena;
	}

	public synchronized int survivors(HashMap<Player, Integer> team) {
		int survivors = 0;
		for(Player p : team.keySet()) {
			if(team.get(p) > 0) {
				survivors++;
			}
		}
		return survivors;
	}
	public synchronized boolean hasLeft(Player player) {
		if(left.contains(player)) return true;
		else return false;
	}
	public synchronized boolean isSurvivor(Player player) {
		if(spec.contains(player)) return true;
		if(getTeam(player) != null) {
			if(getTeam(player).get(player) > 0) return true;
		}
		return false;
	}

	public String getTeamName(Player player) {
		if(redT.keySet().contains(player)) return Lobby.RED.getName();
		if(blueT.keySet().contains(player)) return Lobby.BLUE.getName();
		return null;
	}
	public String getEnemyTeamName(Player player) {
		if(redT.keySet().contains(player)) return Lobby.BLUE.getName();
		if(blueT.keySet().contains(player)) return Lobby.RED.getName();
		return null;
	}

	public boolean isSpec(Player player) {
		if(spec.contains(player)) return true;
		else return false;
	}
	public boolean isRed(Player player) {
		if(redT.keySet().contains(player)) return true;
		else return false;
	}
	public boolean isBlue(Player player) {
		if(blueT.keySet().contains(player)) return true;
		else return false;
	}

	public HashMap<Player, Integer> getTeam(Player player) {
		if(redT.keySet().contains(player)) return redT;
		if(blueT.keySet().contains(player)) return blueT;
		return null;
	}
	public HashMap<Player, Integer> getEnemyTeam(Player player) {
		if(redT.keySet().contains(player)) return blueT;
		if(blueT.keySet().contains(player)) return redT;
		return null;
	}

	public boolean inMatch(Player player) {
		if(redT.keySet().contains(player)) return true;
		if(blueT.keySet().contains(player)) return true;
		if(spec.contains(player)) return true;
		return false;
	}

	public boolean enemys(Player player1, Player player2) {
		if(redT.keySet().contains(player1) && blueT.keySet().contains(player2)) return true;
		if(redT.keySet().contains(player2) && blueT.keySet().contains(player1)) return true;
		return false;
	}

	public boolean friendly(Player player1, Player player2) {
		if(redT.keySet().contains(player1) && redT.keySet().contains(player2)) return true;
		if(blueT.keySet().contains(player1) && blueT.keySet().contains(player2)) return true;
		return false;
	}

	public ArrayList<Player> getAllPlayer() {
		return players;
	}

	public ArrayList<Player> getAllSpec() {
		ArrayList<Player> list = new ArrayList<Player>();
		for(Player p : spec) {
			list.add(p);
		}
		return list;
	}

	public ArrayList<Player> getAll() {
		//return players;
		ArrayList<Player> list = new ArrayList<Player>(players);
		for(Player p : spec) {
			list.add(p);
		}
		return list;
	}

	//AKTIONS

	public synchronized void left(Player player) {
		//left
		left.add(player.getName());
		//listname
		//duplicated?
		//if(plugin.listnames) player.setPlayerListName(null);
		//team?
		if(getTeam(player) != null) {
			//0 leben aka tot
			getTeam(player).put(player, 0);
			//survivors?->endGame
			if(survivors(getTeam(player)) == 0) {
				matchOver = true;
				//unhideAll();
				undoAllColors();

				plugin.mm.gameEnd(this, playersLoc, getEnemyTeam(player).keySet(), getEnemyTeamName(player), getTeam(player).keySet(), getTeamName(player), spec, shots, hits, kills, deaths, teamattacks, grenades, airstrikes);
			}
		} else if(spec.contains(player)) spec.remove(player);
	}

	public synchronized void shot(Player player) {
		//add 1
		shots.put(player.getName(), shots.get(player.getName())+1);
	}
	public synchronized void grenade(Player player) {
		//add 1
		grenades.put(player.getName(), grenades.get(player.getName())+1);
	}
	public synchronized void airstrike(Player player) {
		//add 1
		airstrikes.put(player.getName(), airstrikes.get(player.getName())+1);
	}

	public synchronized void hitSnow(Player target, Player shooter) {
		//math over already?
		if(matchOver) return;
		//target already dead?
		if(getTeam(target).get(target) <= 0) return;
		//Teams?
		if(enemys(target, shooter)) {
			//player not dead already?
			if(getTeam(target).get(target) > 0) {
				//-1 live
				getTeam(target).put(target, (getTeam(target).get(target) -1));
				//stats
				hits.put(shooter.getName(), hits.get(shooter.getName())+1);
				//dead?->frag
				//message:
				if(getTeam(target).get(target) <= 0) {
					frag(target, shooter);
				} else {
					shooter.sendMessage(plugin.t.getString("YOU_HIT"));
					target.sendMessage(plugin.t.getString("YOU_WERE_HIT"));
				}
			}
		} else if(friendly(target, shooter)) {
			//message
			//-points
			teamattacks.put(shooter.getName(), teamattacks.get(shooter.getName())+1);
			if(plugin.pointsPerTeamattack != 0) {
				HashMap<String,String> vars = new HashMap<String, String>();
				vars.put("points", String.valueOf(plugin.pointsPerTeamattack));
				shooter.sendMessage(plugin.t.getString("YOU_HIT_MATE_POINTS", vars));
			} else {
				shooter.sendMessage(plugin.t.getString("YOU_HIT_MATE"));
			}
		}
	}

	public synchronized void frag(final Player target, final Player killer) {
		final Match this2 = this;
		//STATS
		deaths.put(target.getName(), deaths.get(target.getName())+1);
		kills.put(killer.getName(), kills.get(killer.getName())+1);

		//feed
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("target", target.getName());
		vars.put("killer", killer.getName());
		vars.put("points", String.valueOf(plugin.pointsPerKill));
		vars.put("money", String.valueOf(plugin.cashPerKill));
		killer.sendMessage(plugin.t.getString("YOU_KILLED", vars));
		target.sendMessage(plugin.t.getString("YOU_WERE_KILLED", vars));
		plugin.nf.feed(target, killer, this2);

		if(survivors(getTeam(target)) == 0) {
			matchOver = true;
		}

		//afk detection on frag
		if(plugin.afkDetection) {
			if(target.getLocation().getWorld().equals(playersLoc.get(target.getName()).getWorld()) && target.getLocation().distance(playersLoc.get(target.getName())) <= plugin.afkRadius && shots.get(target) == 0 && kills.get(target) == 0) {
				int afkCount;
				if(plugin.afkMatchCount.get(target.getName()) != null) {
					afkCount = plugin.afkMatchCount.get(target.getName());
				} else {
					afkCount = 0;
				}
				plugin.afkMatchCount.put(target.getName(), afkCount+1);
			}else {
				plugin.afkMatchCount.remove(target.getName());
			}
		}

		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

			@Override
			public void run() {
				plugin.joinLobby(target);
				//survivors?->endGame
				if(survivors(getTeam(target)) == 0) {
					//unhideAll();
					matchOver = true;
					undoAllColors();

					plugin.mm.gameEnd(this2, playersLoc, getTeam(killer).keySet(), getTeamName(killer), getTeam(target).keySet(), getTeamName(target), spec, shots, hits, kills, deaths, teamattacks, grenades, airstrikes);
				}

			}
		}, 1L);

	}

	public void death(final Player target) {
		//afk detection on frag
		if(plugin.afkDetection) {
			if(target.getLocation().getWorld().equals(playersLoc.get(target.getName()).getWorld()) && target.getLocation().distance(playersLoc.get(target.getName())) <= plugin.afkRadius && shots.get(target) == 0 && kills.get(target) == 0) {
				int afkCount;
				if(plugin.afkMatchCount.get(target.getName()) != null) {
					afkCount = plugin.afkMatchCount.get(target.getName());
				} else {
					afkCount = 0;
				}
				plugin.afkMatchCount.put(target.getName(), afkCount+1);
			}else {
				plugin.afkMatchCount.remove(target.getName());
			}
		}

		plugin.joinLobby(target);
		//feed
		target.sendMessage(plugin.t.getString("YOU_DIED"));
		plugin.nf.death(target, this);
		//points+cash+kill+death
		deaths.put(target.getName(), (deaths.get(target.getName())+1));
		//survivors?->endGame
		//0 leben aka tot
		getTeam(target).put(target, 0);
		//survivors?->endGame
		if(survivors(getTeam(target)) == 0) {
			matchOver = true;
			//unhideAll();
			undoAllColors();

			plugin.mm.gameEnd(this, playersLoc, getEnemyTeam(target).keySet(), getEnemyTeamName(target), getTeam(target).keySet(), getTeamName(target), spec, shots, hits, kills, deaths, teamattacks, grenades, airstrikes);
		}
	}

}
