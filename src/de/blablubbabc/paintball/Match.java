package de.blablubbabc.paintball;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.kitteh.tag.TagAPI;

import de.blablubbabc.paintball.extras.Airstrike;
import de.blablubbabc.paintball.extras.Ball;
import de.blablubbabc.paintball.extras.Flashbang;
import de.blablubbabc.paintball.extras.Gifts;
import de.blablubbabc.paintball.extras.Grenade;
import de.blablubbabc.paintball.extras.GrenadeM2;
import de.blablubbabc.paintball.extras.ItemManager;
import de.blablubbabc.paintball.extras.Mine;
import de.blablubbabc.paintball.extras.Orbitalstrike;
import de.blablubbabc.paintball.extras.Sniper;
import de.blablubbabc.paintball.extras.Turret;
import de.blablubbabc.paintball.utils.Timer;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class Match {

	private Paintball plugin;
	private HashMap<Player, Integer> livesLeft = new HashMap<Player, Integer>();
	private HashMap<Player, Integer> respawnsLeft = new HashMap<Player, Integer>();
	private ArrayList<Player> redT = new ArrayList<Player>();
	private ArrayList<Player> blueT = new ArrayList<Player>();
	private ArrayList<Player> bothTeams = new ArrayList<Player>();
	private ArrayList<Player> allPlayers = new ArrayList<Player>();
	private HashMap<Player, Integer> protection = new HashMap<Player, Integer>();
	private Set<String> justRespawned = new HashSet<String>();
	// STATS
	private HashMap<String, Integer> shots = new HashMap<String, Integer>();
	private HashMap<String, Integer> hits = new HashMap<String, Integer>();
	private HashMap<String, Integer> kills = new HashMap<String, Integer>();
	private HashMap<String, Integer> deaths = new HashMap<String, Integer>();
	private HashMap<String, Integer> teamattacks = new HashMap<String, Integer>();
	private HashMap<String, Integer> grenades = new HashMap<String, Integer>();
	private HashMap<String, Integer> airstrikes = new HashMap<String, Integer>();

	private Random random;

	private HashMap<String, Location> playersLoc = new HashMap<String, Location>();;
	private boolean matchOver = false;

	private ArrayList<Player> spec = new ArrayList<Player>();
	private String arena;

	private Timer startTimer;
	private Timer roundTimer;

	private ArrayList<Location> redspawns;
	private ArrayList<Location> bluespawns;
	private ArrayList<Location> specspawns;

	private int spawnBlue;
	private int spawnRed;
	private int spawnSpec;

	public int setting_balls;
	public int setting_grenades;
	public int setting_airstrikes;
	public int setting_lives;
	public int setting_respawns;
	public int setting_round_time;

	public boolean started = false;

	public ArrayList<Player> winners = new ArrayList<Player>();
	public ArrayList<Player> loosers = new ArrayList<Player>();
	public String win = "";
	public String loose = "";

	public Match(final Paintball plugin, Set<Player> red, Set<Player> blue, Set<Player> spec,
			Set<Player> random, String arena) {
		this.plugin = plugin;
		this.arena = arena;
		this.started = false;
		this.random = new Random();

		this.redspawns = plugin.am.getRedSpawns(arena);
		this.bluespawns = plugin.am.getBlueSpawns(arena);
		this.specspawns = plugin.am.getSpecSpawns(arena);

		// random spawns
		Random randSpawns = new Random();
		this.spawnBlue = randSpawns.nextInt(bluespawns.size());
		this.spawnRed = randSpawns.nextInt(redspawns.size());
		this.spawnSpec = randSpawns.nextInt(specspawns.size());

		calculateSettings();

		// TEAMS
		for (Player p : red) {
			this.redT.add(p);
			addToPlayerLists(p);
		}
		for (Player p : blue) {
			this.blueT.add(p);
			addToPlayerLists(p);
		}
		for (Player p : spec) {
			this.spec.add(p);
			this.allPlayers.add(p);
		}
		// this.spec = spec;

		// randoms:
		List<Player> rand = new ArrayList<Player>();
		for (Player p : random) {
			rand.add(p);
		}
		Collections.shuffle(rand);
		for (Player p : rand) {
			// players.add(p);
			if (this.blueT.size() < this.redT.size()) {
				this.blueT.add(p);
				addToPlayerLists(p);
			} else if (this.redT.size() <= this.blueT.size()) {
				this.redT.add(p);
				addToPlayerLists(p);
			}
		}

		// LISTS FINISHED

		for (Player p : getAllPlayer()) {
			// LIVES + RESPAWNS
			livesLeft.put(p, setting_lives);
			respawnsLeft.put(p, setting_respawns);
			// STATS
			this.shots.put(p.getName(), 0);
			this.hits.put(p.getName(), 0);
			this.kills.put(p.getName(), 0);
			this.deaths.put(p.getName(), 0);
			this.teamattacks.put(p.getName(), 0);
			this.grenades.put(p.getName(), 0);
			this.airstrikes.put(p.getName(), 0);

			plugin.checks(p, true, true);
			spawnPlayer(p);
		}

		for (Player p : this.spec) {
			plugin.checks(p, true, true);
			spawnSpec(p);
		}
		// colorchanges:
		changeAllColors();
		updateTags();

		// WAITING TIMER:
		this.started = false;
		
		startTimer = new Timer(Paintball.instance, 0L, 20L, plugin.countdownStart, new Runnable() {
			
			@Override
			public void run() {
				for (Player p : getAllPlayer()) {
					Location ploc = p.getLocation();
					Location loc = playersLoc.get(p.getName());
					loc.setPitch(ploc.getPitch());
					loc.setYaw(ploc.getYaw());
					p.teleport(loc);
				}
			}
		}, new Runnable() {
			
			@Override
			public void run() {
				HashMap<String, String> vars = new HashMap<String, String>();
				vars.put("seconds", String.valueOf(startTimer.getTime()));
				String msg = Translator.getString("COUNTDOWN_START", vars);
				for (Player player : getAll()) {
					if (plugin.useXPBar) player.setLevel(startTimer.getTime());
					player.sendMessage(msg);
				}
			}
		}, new Runnable() {
			
			@Override
			public void run() {
				startTimer = null;
				// START:
				started = true;
				// lives + start!:
				HashMap<String, String> vars = new HashMap<String, String>();
				vars.put("lives", String.valueOf(setting_lives));
				if (setting_respawns == -1)
					vars.put("respawns", Translator.getString("INFINITE"));
				else
					vars.put("respawns", String.valueOf(setting_respawns));
				vars.put("round_time", String.valueOf(setting_round_time));

				plugin.nf.status(Translator.getString("MATCH_SETTINGS_INFO", vars));
				plugin.nf.status(Translator.getString("MATCH_START"));

				makeAllVisible();
				startRoundTimer();
			}
		});
		
	}

	private void addToPlayerLists(Player p) {
		this.allPlayers.add(p);
		this.bothTeams.add(p);
	}

	public void endTimers() {
		if (startTimer != null) startTimer.end();
		if (roundTimer != null) roundTimer.end();
	}
	
	private void startRoundTimer() {
		roundTimer = new Timer(Paintball.instance, 0L, 20L, setting_round_time, new Runnable() {
			
			@Override
			public void run() {
				// Spawn protection:
				Iterator<Map.Entry<Player, Integer>> iter = protection.entrySet()
						.iterator();
				while (iter.hasNext()) {
					Map.Entry<Player, Integer> entry = iter.next();
					Player p = entry.getKey();
					int t = entry.getValue() - 1;
					if (t <= 0) {
						if (p.isOnline() && isSurvivor(p))
							p.sendMessage(Translator.getString("PROTECTION_OVER"));
						iter.remove();
					} else {
						protection.put(p, t);
					}
				}
				// level bar
				if (plugin.useXPBar) {
					for (Player player : Lobby.LOBBY.getMembers()) {
						player.setLevel(roundTimer.getTime());
					}
				}
			}
		}, new Runnable() {
			
			@Override
			public void run() {
				plugin.nf.roundTime(roundTimer.getTime());
			}
		}, new Runnable() {
			
			@Override
			public void run() {
				roundTimer = null;
				// END:
				if (matchOver)
					return;
				// winner?
				ArrayList<Player> winnerTeam = getWinner();
				if (winnerTeam == null) {
					// draw:
					gameEnd(true, null, null, null, null);
				} else {
					Player p = winnerTeam.get(0);
					gameEnd(false, winnerTeam, getEnemyTeam(p), getTeamName(p),
							getEnemyTeamName(p));
				}
			}
		});
	}

	private ArrayList<Player> getWinner() {
		// compare survivors:
		if (survivors(redT) > survivors(blueT))
			return redT;
		if (survivors(blueT) > survivors(redT))
			return blueT;
		// else: survivors(blueT) == survivors(redT)-> DRAW
		return null;
	}

	// SPAWNS

	@SuppressWarnings("deprecation")
	public synchronized void spawnPlayer(final Player player) {
		boolean red = false;
		Location loc;
		if (redT.contains(player)) {
			red = true;
			if (spawnRed > (redspawns.size() - 1))
				spawnRed = 0;
			loc = redspawns.get(spawnRed);
			spawnRed++;
		} else if (blueT.contains(player)) {
			if (spawnBlue > (bluespawns.size() - 1))
				spawnBlue = 0;
			loc = bluespawns.get(spawnBlue);
			spawnBlue++;
		} else {
			return;
		}
		player.teleport(loc);
		// sound
		player.playSound(loc, Sound.BAT_TAKEOFF, 100L, 1L);
		// afk Location
		playersLoc.put(player.getName(), loc);
		// PLAYER
		plugin.checks(player, false, false);
		// INVENTORY

		player.getInventory().setHelmet(
				Utils.setLeatherArmorColor(new ItemStack(Material.LEATHER_HELMET, 1),
						Lobby.getTeam(getTeamName(player)).colorA()));
		player.getInventory().setChestplate(
				Utils.setLeatherArmorColor(new ItemStack(Material.LEATHER_CHESTPLATE, 1),
						Lobby.getTeam(getTeamName(player)).colorA()));
		player.getInventory().setLeggings(
				Utils.setLeatherArmorColor(new ItemStack(Material.LEATHER_LEGGINGS, 1),
						Lobby.getTeam(getTeamName(player)).colorA()));
		player.getInventory().setBoots(
				Utils.setLeatherArmorColor(new ItemStack(Material.LEATHER_BOOTS, 1),
						Lobby.getTeam(getTeamName(player)).colorA()));
		
		if (red) {
			player.getInventory().setItem(8, ItemManager.setMeta(new ItemStack(Material.WOOL, 1, (short)0, DyeColor.RED.getWoolData())));
		} else {
			player.getInventory().setItem(8, ItemManager.setMeta(new ItemStack(Material.WOOL, 1, (short)0, DyeColor.BLUE.getWoolData())));
		}
		
		if (setting_balls > 0)
			player.getInventory().addItem(ItemManager.setMeta(new ItemStack(Material.SNOW_BALL, setting_balls)));
		else if (setting_balls == -1)
			player.getInventory().addItem(ItemManager.setMeta(new ItemStack(Material.SNOW_BALL, 10)));
		if (setting_grenades > 0)
			player.getInventory().addItem(ItemManager.setMeta(new ItemStack(Material.EGG, setting_grenades)));
		else if (setting_grenades == -1)
			player.getInventory().addItem(ItemManager.setMeta(new ItemStack(Material.EGG, 10)));
		if (setting_airstrikes > 0)
			player.getInventory().addItem(ItemManager.setMeta(new ItemStack(Material.STICK, setting_airstrikes)));
		else if (setting_airstrikes == -1)
			player.getInventory().addItem(ItemManager.setMeta(new ItemStack(Material.STICK, 10)));
		// gifts
		if (plugin.giftsEnabled) {
			int r = random.nextInt(1000);
			if (plugin.giftOnSpawnChance > (r / 10)) {
				Gifts.receiveGift(player, 1, false);
			}
		}
		player.updateInventory();
		// MESSAGE
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("team_color", Lobby.getTeam(getTeamName(player)).color().toString());
		vars.put("team", getTeamName(player));
		player.sendMessage(Translator.getString("BE_IN_TEAM", vars));
		// SPAWN PROTECTION
		if (plugin.protectionTime > 0) {
			vars.put("protection", String.valueOf(plugin.protectionTime));
			protection.put(player, plugin.protectionTime);
			player.sendMessage(Translator.getString("PROTECTION", vars));
		}
		// JUST RESPAWNED TIMER
		// this will not work that accurate like intended, if the player gets killed, while being justRespawned.
		// For that reason is the timers delay very short.
		final String name = player.getName();
		justRespawned.add(name);
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

			@Override
			public void run() {
				justRespawned.remove(name);
			}
			
		}, 12L);
	}
	
	public boolean isJustRespawned(String playerName) {
		return justRespawned.contains(playerName);
	}

	@SuppressWarnings("deprecation")
	public synchronized void spawnSpec(Player player) {
		if (spawnSpec > (specspawns.size() - 1))
			spawnSpec = 0;
		player.teleport(specspawns.get(spawnSpec));
		spawnSpec++;
		// INVENTORY
		player.getInventory().setHelmet(
				Utils.setLeatherArmorColor(new ItemStack(Material.LEATHER_HELMET, 1), Lobby.SPECTATE.colorA()));
		player.getInventory().setChestplate(
				Utils.setLeatherArmorColor(new ItemStack(Material.LEATHER_CHESTPLATE, 1), Lobby.SPECTATE.colorA()));
		player.getInventory().setLeggings(
				Utils.setLeatherArmorColor(new ItemStack(Material.LEATHER_LEGGINGS, 1), Lobby.SPECTATE.colorA()));
		player.getInventory().setBoots(
				Utils.setLeatherArmorColor(new ItemStack(Material.LEATHER_BOOTS, 1), Lobby.SPECTATE.colorA()));
		
		player.getInventory().setItem(8, ItemManager.setMeta(new ItemStack(Material.WOOL, 1, (short)0, DyeColor.YELLOW.getWoolData())));
		
		player.updateInventory();
		// MESSAGE
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("team_color", Lobby.getTeam(player).color().toString());
		vars.put("team", Lobby.getTeam(player).getName());
		player.sendMessage(Translator.getString("BE_SPECTATOR", vars));
	}

	// INVENTORY
	private void calculateSettings() {
		HashMap<String, Integer> settings = plugin.am.getArenaSettings(arena);
		// BALLS
		setting_balls = plugin.balls + settings.get("balls");
		if (setting_balls < -1)
			setting_balls = -1;
		// GRENADES
		setting_grenades = plugin.grenadeAmount + settings.get("grenades");
		if (setting_grenades < -1)
			setting_grenades = -1;
		// AIRSTRIKES
		setting_airstrikes = plugin.airstrikeAmount + settings.get("airstrikes");
		if (setting_airstrikes < -1)
			setting_airstrikes = -1;
		// LIVES
		setting_lives = plugin.lives + settings.get("lives");
		if (setting_lives < 1)
			setting_lives = 1;
		// RESPAWNS
		setting_respawns = plugin.respawns + settings.get("respawns");
		if (setting_respawns < -1)
			setting_respawns = -1;
		// ROUND TIME
		setting_round_time = plugin.roundTimer + settings.get("round_time");
		if (setting_round_time < 30)
			setting_round_time = 30;
	}

	private void makeAllVisible() {
		for (Player pl : getAll()) {
			for (Player p : getAll()) {
				if (!p.equals(pl))
					pl.showPlayer(p);
			}
		}
	}

	public void changeAllColors() {
		for (Player p : redT) {
			// chatnames
			String n = plugin.red + p.getName();
			if (n.length() > 16)
				n = (String) n.subSequence(0, n.length() - (n.length() - 16));
			/*
			 * if(plugin.chatnames) { p.setDisplayName(n+white); }
			 */
			// listnames
			if (plugin.listnames) {
				p.setPlayerListName(n);
			}
		}
		for (Player p : blueT) {
			// chatnames
			String n = plugin.blue + p.getName();
			if (n.length() > 16)
				n = (String) n.subSequence(0, n.length() - (n.length() - 16));
			/*
			 * if(plugin.chatnames) { p.setDisplayName(n+white); }
			 */
			// listnames
			if (plugin.listnames) {
				p.setPlayerListName(n);
			}
		}
	}

	public void updateTags() {
		// player tags:
		if (plugin.tagAPI != null && plugin.tags) {
			for (Player p : plugin.getServer().getOnlinePlayers()) {
				Set<Player> set = new HashSet<Player>();
				for (Player pl : getAll()) {
					if (pl != p) {
						set.add(pl);
					}
				}
				TagAPI.refreshPlayer(p, set);
			}
		}
	}

	public void undoAllColors() {
		for (Player p : getAllPlayer()) {
			/*
			 * if(plugin.chatnames) { p.setDisplayName(p.getName()); }
			 */
			// listnames
			if (plugin.listnames) {
				if (Lobby.isPlaying(p))
					p.setPlayerListName(null);
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

	public synchronized int survivors(ArrayList<Player> team) {
		int survivors = 0;
		for (Player p : team) {
			if (isSurvivor(p)) {
				survivors++;
			}
		}
		return survivors;
	}

	public synchronized boolean isSurvivor(Player player) {
		if (spec.contains(player))
			return true;
		if (getTeam(player) != null) {
			// if(setting_respawns == -1) return true;
			if (respawnsLeft.get(player) != 0)
				return true;
			else if (livesLeft.get(player) > 0)
				return true;
		}
		return false;
	}

	public String getTeamName(Player player) {
		if (redT.contains(player))
			return Lobby.RED.getName();
		if (blueT.contains(player))
			return Lobby.BLUE.getName();
		return null;
	}

	public String getEnemyTeamName(Player player) {
		if (redT.contains(player))
			return Lobby.BLUE.getName();
		if (blueT.contains(player))
			return Lobby.RED.getName();
		return null;
	}

	public boolean isSpec(Player player) {
		if (spec.contains(player))
			return true;
		else
			return false;
	}

	public boolean isRed(Player player) {
		if (redT.contains(player))
			return true;
		else
			return false;
	}

	public boolean isBlue(Player player) {
		if (blueT.contains(player))
			return true;
		else
			return false;
	}

	public ArrayList<Player> getTeam(Player player) {
		if (redT.contains(player))
			return redT;
		if (blueT.contains(player))
			return blueT;
		return null;
	}

	public ArrayList<Player> getEnemyTeam(Player player) {
		if (redT.contains(player))
			return blueT;
		if (blueT.contains(player))
			return redT;
		return null;
	}

	public boolean inMatch(Player player) {
		if (redT.contains(player))
			return true;
		if (blueT.contains(player))
			return true;
		if (spec.contains(player))
			return true;
		return false;
	}

	public boolean enemys(Player player1, Player player2) {
		if (redT.contains(player1) && blueT.contains(player2))
			return true;
		if (redT.contains(player2) && blueT.contains(player1))
			return true;
		return false;
	}

	public boolean friendly(Player player1, Player player2) {
		if (redT.contains(player1) && redT.contains(player2))
			return true;
		if (blueT.contains(player1) && blueT.contains(player2))
			return true;
		return false;
	}

	public ArrayList<Player> getAllPlayer() {
		// ArrayList<Player> players = new ArrayList<Player>();
		// players.addAll(redT);
		// players.addAll(blueT);
		return bothTeams;
	}

	public ArrayList<Player> getAllSpec() {
		/*
		 * ArrayList<Player> list = new ArrayList<Player>(); for (Player p :
		 * spec) { list.add(p); } return list;
		 */
		return spec;
	}

	public ArrayList<Player> getAll() {
		/*
		 * // return players; ArrayList<Player> list = new
		 * ArrayList<Player>(getAllPlayer()); for (Player p : spec) {
		 * list.add(p); } return list;
		 */
		return allPlayers;
	}

	public boolean isProtected(Player player) {
		return protection.containsKey(player);
	}

	// AKTIONS

	public synchronized void left(Player player) {
		// team?
		if (getTeam(player) != null) {
			// 0 leben aka tot
			livesLeft.put(player, 0);
			respawnsLeft.put(player, 0);
			// spawn protection
			protection.remove(player);
			// afk detection-> remove player
			if (plugin.afkDetection) {
				plugin.afkRemove(player.getName());
			}
			resetWeaponStuffDeath(player);
			// survivors?->endGame
			// math over already?
			if (matchOver)
				return;
			if (survivors(getTeam(player)) == 0) {
				gameEnd(false, getEnemyTeam(player), getTeam(player), getEnemyTeamName(player),
						getTeamName(player));
			}
		} else if (spec.contains(player))
			spec.remove(player);
	}

	public int respawnsLeft(Player player) {
		return respawnsLeft.get(player);
	}
	
	private synchronized void respawn(Player player) {
		livesLeft.put(player, setting_lives);
		if (setting_respawns != -1)
			respawnsLeft.put(player, respawnsLeft.get(player) - 1);
		// message
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("lives", String.valueOf(setting_lives));
		if (setting_respawns == -1)
			vars.put("respawns", Translator.getString("INFINITE"));
		else
			vars.put("respawns", String.valueOf(respawnsLeft.get(player)));
		player.sendMessage(Translator.getString("RESPAWN", vars));
		// spawn protection
		protection.remove(player);
		// spawn
		spawnPlayer(player);
	}

	public void addShots(Player player, int amount) {
		// add 1
		shots.put(player.getName(), shots.get(player.getName()) + amount);
		// effekt
		Location loc = player.getLocation();
		player.playSound(loc, Sound.WOOD_CLICK, 100F, 0F);
	}

	public void grenade(Player player) {
		// add 1
		grenades.put(player.getName(), grenades.get(player.getName()) + 1);
	}

	public void airstrike(Player player) {
		// add 1
		airstrikes.put(player.getName(), airstrikes.get(player.getName()) + 1);
	}

	public synchronized void hitSnow(Player target, Player shooter, Origin source) {
		// math over already?
		if (matchOver)
			return;

		// target already dead?
		if (livesLeft.get(target) <= 0)
			return;
		
		String targetName = target.getName();
		String shooterName = shooter.getName();
		
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("target", targetName);
		vars.put("shooter", shooterName);
		
		// Teams?
		if (enemys(target, shooter)) {
			// player not dead already?
			if (livesLeft.get(target) > 0) {
				// protection
				if (isProtected(target)) {
					shooter.playSound(shooter.getLocation(), Sound.ANVIL_LAND, 70F, 2F);
					target.playSound(shooter.getLocation(), Sound.ANVIL_LAND, 60F, 0F);
					shooter.sendMessage(Translator.getString("YOU_HIT_PROTECTED", vars));
					target.sendMessage(Translator.getString("YOU_WERE_HIT_PROTECTED", vars));
				} else {
					int healthLeft = livesLeft.get(target) - 1;
					// -1 live
					livesLeft.put(target, healthLeft);
					// stats
					hits.put(shooter.getName(), hits.get(shooter.getName()) + 1);
					// dead?->frag
					// message:
					if (healthLeft <= 0) {
						frag(target, shooter, source);
					} else {
						// xp bar
						if (plugin.useXPBar) {
							target.setExp((float)healthLeft / setting_lives);
						}
						shooter.playSound(shooter.getLocation(), Sound.MAGMACUBE_WALK, 100F, 1F);
						target.playSound(shooter.getLocation(), Sound.HURT_FLESH, 100F, 1F);
						
						vars.put("hits_taken", String.valueOf(setting_lives - healthLeft));
						vars.put("health_left", String.valueOf(healthLeft));
						vars.put("health", String.valueOf(setting_lives));
						
						shooter.sendMessage(Translator.getString("YOU_HIT", vars));
						target.sendMessage(Translator.getString("YOU_WERE_HIT", vars));
					}
				}
			}
		} else if (friendly(target, shooter)) {
			// message
			// -points
			teamattacks.put(shooterName, teamattacks.get(shooter.getName()) + 1);
			shooter.playSound(shooter.getLocation(), Sound.ANVIL_LAND, 70F, 1F);
			if (plugin.pointsPerTeamattack != 0) {
				vars.put("points", String.valueOf(plugin.pointsPerTeamattack));
				shooter.sendMessage(Translator.getString("YOU_HIT_MATE_POINTS", vars));
			} else {
				shooter.sendMessage(Translator.getString("YOU_HIT_MATE", vars));
			}
		}
	}

	public synchronized void frag(final Player target, Player killer, Origin source) {
		// math over already?
		if (matchOver)
			return;
		killer.playSound(killer.getLocation(), Sound.MAGMACUBE_WALK, 100F, 0F);
		target.playSound(target.getLocation(), Sound.GHAST_SCREAM2, 100F, 0F);

		// STATS
		deaths.put(target.getName(), deaths.get(target.getName()) + 1);
		kills.put(killer.getName(), kills.get(killer.getName()) + 1);
		
		// 0 leben aka tot
		livesLeft.put(target, 0);
		// spawn protection
		protection.remove(target);

		// feed
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("target", target.getName());
		vars.put("killer", killer.getName());
		vars.put("points", String.valueOf(plugin.pointsPerKill));
		vars.put("money", String.valueOf(plugin.cashPerKill));
		killer.sendMessage(Translator.getString("YOU_KILLED", vars));
		target.sendMessage(Translator.getString("YOU_WERE_KILLED", vars));
		plugin.nf.feed(target, killer, this);

		// afk detection on frag
		if (plugin.afkDetection) {
			String name = target.getName();
			if (target.getLocation().getWorld().equals(playersLoc.get(name).getWorld())
					&& target.getLocation().distance(playersLoc.get(name)) <= plugin.afkRadius
					&& shots.get(name) == 0 && kills.get(name) == 0) {
				plugin.afkSet(name, plugin.afkGet(name) + 1);
			} else {
				plugin.afkRemove(name);
			}
		}

		if (isSurvivor(target)) {
			// respawn(target);
			// afk check
			String name = target.getName();
			if (plugin.afkDetection && (plugin.afkGet(name) >= plugin.afkMatchAmount)) {
				// consequences after being afk:
				plugin.afkRemove(name);
				respawnsLeft.put(target, 0);
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					@Override
					public void run() {
						plugin.joinLobby(target);
					}
				}, 1L);

				Lobby.getTeam(target).removeMember(target);
				plugin.nf.afkLeave(target, this);
				target.sendMessage(Translator.getString("YOU_LEFT_TEAM"));
			} else
				respawn(target);
		} else {
			resetWeaponStuffDeath(target);
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					plugin.joinLobby(target);
				}
			}, 1L);
		}

		// survivors?->endGame
		if (survivors(getTeam(target)) == 0) {
			gameEnd(false, getTeam(killer), getTeam(target), getTeamName(killer),
					getTeamName(target));
		}

	}

	public synchronized void death(final Player target) {
		// math over already?
		if (matchOver)
			return;

		// feed
		target.sendMessage(Translator.getString("YOU_DIED"));
		plugin.nf.death(target, this);
		// points+cash+kill+death
		deaths.put(target.getName(), (deaths.get(target.getName()) + 1));
		// survivors?->endGame
		// 0 leben aka tot
		livesLeft.put(target, 0);
		// spawn protection
		protection.remove(target);

		// afk detection on death
		if (plugin.afkDetection) {
			String name = target.getName();
			if (target.getLocation().getWorld().equals(playersLoc.get(name).getWorld())
					&& target.getLocation().distance(playersLoc.get(name)) <= plugin.afkRadius
					&& shots.get(name) == 0 && kills.get(name) == 0) {
				plugin.afkSet(name, plugin.afkGet(name) + 1);
			} else {
				plugin.afkRemove(name);
			}
		}

		if (isSurvivor(target)) {
			// afk check
			String name = target.getName();
			if (plugin.afkDetection && (plugin.afkGet(name) >= plugin.afkMatchAmount)) {
				// consequences after being afk:
				plugin.afkRemove(name);
				respawnsLeft.put(target, 0);
				resetWeaponStuffDeath(target);
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					@Override
					public void run() {
						plugin.joinLobby(target);
					}
				}, 1L);

				Lobby.getTeam(target).removeMember(target);
				plugin.nf.afkLeave(target, this);
				target.sendMessage(Translator.getString("YOU_LEFT_TEAM"));
			} else
				respawn(target);
		} else {
			resetWeaponStuffDeath(target);
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					plugin.joinLobby(target);
				}
			}, 1L);
		}

		// survivors?->endGame
		if (survivors(getTeam(target)) == 0) {
			gameEnd(false, getEnemyTeam(target), getTeam(target), getEnemyTeamName(target),
					getTeamName(target));
		}
	}

	private synchronized void gameEnd(final boolean draw, ArrayList<Player> winnerS,
			ArrayList<Player> looserS, String winS, String looseS) {
		matchOver = true;
		endTimers();
		undoAllColors();
		for (Player p : getAllPlayer()) {
			resetWeaponStuffEnd(p);
		}
		resetMainWeaponStuffEnd();
		if (!draw) {
			for (Player p : winnerS) {
				this.winners.add(p);
			}
			for (Player p : looserS) {
				this.loosers.add(p);
			}
			this.win = winS;
			this.loose = looseS;
		}
		final Match this2 = this;
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

			@Override
			public void run() {
				plugin.mm.gameEnd(this2, draw, playersLoc, spec, shots, hits, kills, deaths,
						teamattacks, grenades, airstrikes);
			}
		}, 1L);
	}

	public void resetWeaponStuffEnd(Player p) {
		// remove turrets:
		ArrayList<Turret> pturrets = new ArrayList<Turret>(Turret.getTurrets(p.getName()));
		for (Turret t : pturrets) {
			t.die(false);
		}
		// remove mines:
		ArrayList<Mine> pmines = new ArrayList<Mine>(Mine.getMines(p.getName()));
		for (Mine m : pmines) {
			m.explode(false);
		}
		
		// remove zooming
		if (Sniper.isZooming(p))
			Sniper.setNotZooming(p);
		
	}
	
	public void resetMainWeaponStuffEnd() {
		//remove airstrikes
		Airstrike.clear();
		//remove orbitalstrikes
		Orbitalstrike.clear();
		//remove grenades
		Grenade.clear();
		GrenadeM2.clear();
		Flashbang.clear();
		Ball.clear();
	}

	public void resetWeaponStuffDeath(Player p) {
		// remove turrets:
		ArrayList<Turret> pturrets = new ArrayList<Turret>(Turret.getTurrets(p.getName()));
		for (Turret t : pturrets) {
			t.die(true);
		}
		// remove mines:
		ArrayList<Mine> pmines = new ArrayList<Mine>(Mine.getMines(p.getName()));
		for (Mine m : pmines) {
			m.explode(false);
		}
		// remove zooming
		if (Sniper.isZooming(p))
			Sniper.setNotZooming(p);
	}

}