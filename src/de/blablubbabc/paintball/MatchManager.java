package de.blablubbabc.paintball;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import de.blablubbabc.paintball.statistics.arena.ArenaStat;
import de.blablubbabc.paintball.statistics.general.GeneralStat;
import de.blablubbabc.paintball.statistics.player.PlayerStat;
import de.blablubbabc.paintball.utils.Timer;
import de.blablubbabc.paintball.utils.Translator;


public class MatchManager{

	private Paintball plugin;

	private ArrayList<Match> matches;
	private Timer countdown;

	public MatchManager(Paintball pl) {
		plugin = pl;
		matches = new ArrayList<Match>();
	}

	public synchronized void forceReload() {
		//closing all matches and kicking all players from lobby:
		List<Match> mlist = new ArrayList<Match>();
		for (Match m : matches) {
			mlist.add(m);
		}
		for (Match match : mlist) {
			//colors
			match.undoAllColors();
			//Teleport all remaining players back to lobby:
			match.resetWeaponStuffEnd();
			for (Player p : match.getAll()) {
				if (Lobby.isPlaying(p) || Lobby.isSpectating(p)) {
					match.resetWeaponStuff(p);
					plugin.joinLobby(p);
				}
			}

			//close match
			plugin.am.setNotActive(match.getArena());
			match.endTimers();
			match.updateTags();
			matches.remove(match);	
		}
		//messages:
		plugin.nf.status(Translator.getString("ALL_KICKED_FROM_MATCHES"));
		//stop countdown:
		if (countdown != null) {
			countdown.end();
			countdown = null;
		}
		// Kick all from Lobby:
		plugin.nf.status(Translator.getString("ALL_KICKED_FROM_LOBBY"));
		plugin.nf.status(Translator.getString("RELOADING_PAINTBALL"));
		List<Player> list = new ArrayList<Player>();
		for (Player p : Lobby.LOBBY.getMembers()) {
			list.add(p);
		}
		for (Player p : list) {
			plugin.leaveLobby(p, false);
		}
	}

	public synchronized void gameStart() {
		//auto spec lobby
		if (plugin.autoSpecLobby) {
			for (Player player : Lobby.LOBBY.getMembers()) {
				if (Lobby.getTeam(player).equals(Lobby.LOBBY)) {
					Lobby.SPECTATE.addMember(player);
					HashMap<String, String> vars = new HashMap<String, String>();
					vars.put("color_team", Lobby.SPECTATE.color().toString());
					vars.put("team", Lobby.SPECTATE.getName());
					player.sendMessage(Translator.getString("YOU_JOINED_SPECTATORS", vars));
				}
			}
		}

		int players = Lobby.RED.numberWaiting() + Lobby.BLUE.numberWaiting() + Lobby.RANDOM.numberWaiting();
		String info = plugin.nf.getPlayersOverview();

		for (Player player : Lobby.RANDOM.getMembers()) {
			Lobby.RANDOM.setPlaying(player);
		}
		for (Player player : Lobby.BLUE.getMembers()) {
			Lobby.BLUE.setPlaying(player);
		}
		for (Player player : Lobby.RED.getMembers()) {
			Lobby.RED.setPlaying(player);
		}
		for (Player player : Lobby.SPECTATE.getMembers()) {
			Lobby.SPECTATE.setPlaying(player);
		}
		//Arena:
		String arena = plugin.am.getNextArena();
		plugin.am.resetNext();
		plugin.am.setActive(arena);
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("arena", arena);
		plugin.nf.status(Translator.getString("MATCH_START_ARENA", vars));
		vars.put("players", String.valueOf(players));
		vars.put("players_overview", info);
		plugin.nf.status(Translator.getString("MATCH_START_PLAYERS_OVERVIEW", vars));

		Match match = new Match(plugin, Lobby.RED.getMembers(), Lobby.BLUE.getMembers(), Lobby.SPECTATE.getMembers(), Lobby.RANDOM.getMembers(), arena);
		matches.add(match);
	}
	
	public synchronized void gameEnd(final Match match, boolean draw, Map<String, Location> playersLoc, List<Player> specs, 
			final Map<String, Integer> shots, final Map<String, Integer> hits, final Map<String, Integer> kills, final Map<String, Integer> deaths,
			final Map<String, Integer> teamattacks, final Map<String, Integer> grenades, final Map<String, Integer> airstrikes) {
		//TIME
		final long time1 = System.nanoTime();
		//STATS
		final Map<String, Integer> wins = new HashMap<String, Integer>();
		final Map<String, Integer> defeats = new HashMap<String, Integer>();
		final Map<String, Integer> draws = new HashMap<String, Integer>();
		final Map<String, Integer> points = new HashMap<String, Integer>();
		final Map<String, Integer> money = new HashMap<String, Integer>();

		for (Player p : match.getAllPlayer()) {
			String pName = p.getName();
			points.put(pName, plugin.pointsPerRound);
			money.put(pName, plugin.cashPerRound);
			if (draw) {
				draws.put(pName, 1);
				wins.put(pName, 0);
				defeats.put(pName, 0);
			} else draws.put(pName, 0);
		}
		
		//if(!draw) {
		for (Player p : match.winners) {
			String pName = p.getName();
			//stats
			wins.put(pName, 1);
			defeats.put(pName, 0);
			//bonus
			points.put(pName, points.get(pName) + plugin.pointsPerWin);
			money.put(pName, points.get(pName) + plugin.cashPerWin);

		}
		for (Player p :  match.loosers) {
			String pName = p.getName();
			//stats
			wins.put(pName, 0);
			defeats.put(pName, 1);
		}
		//}

		//Teleport all remaining players back to lobby:
		for (Player p : match.getAll()) {
			//if is a remaining player:
			if (Lobby.isPlaying(p) || Lobby.isSpectating(p)){

				//afk detection update on match end
				if (plugin.afkDetection && !match.isSpec(p)) {
					if (p.getLocation().getWorld().equals(playersLoc.get(p.getName()).getWorld()) && p.getLocation().distance(playersLoc.get(p.getName())) <= plugin.afkRadius && shots.get(p.getName()) == 0 && kills.get(p.getName()) == 0) {
						plugin.afkSet(p.getName(), plugin.afkGet(p.getName())+1);
					} else {
						plugin.afkRemove(p.getName());
					}
				}

				//teleport is survivor:
				plugin.joinLobby(p);
			}
		}

		//afk detection clean up and consequences:
		if (plugin.afkDetection) {
			//clearing players from hashmap which didn't play the during the last match or can't be found
			List<String> entries = plugin.afkGetEntries();
			
			for (String afkP : entries) {
				Player player = plugin.getServer().getPlayer(afkP);
				if (player != null) {
					if (!playersLoc.containsKey(afkP)) {
						plugin.afkRemove(afkP);
					} else if(plugin.afkGet(afkP) >= plugin.afkMatchAmount){
						//afk detection consequences after being afk:
						plugin.afkRemove(afkP);
						Lobby.getTeam(player).removeMember(player);
						plugin.nf.afkLeave(player, match);
						player.sendMessage(Translator.getString("YOU_LEFT_TEAM"));
					}
				} else {
					plugin.afkRemove(afkP);
				}
			}
		}

		//MORE STATS
		int shotsAll = 0;
		int hitsAll = 0;
		int killsAll = 0;
		int teamattacksAll = 0;
		int grenadesAll = 0;
		int airstrikesAll = 0;

		//shots
		for (Entry<String, Integer> e : shots.entrySet()) {
			shotsAll += e.getValue();
		}
		//hits
		for (Entry<String, Integer> e : hits.entrySet()) {
			points.put(e.getKey(), points.get(e.getKey()) + ( e.getValue() * plugin.pointsPerHit ));
			money.put(e.getKey(), money.get(e.getKey()) + ( e.getValue() * plugin.cashPerHit ));
			hitsAll += e.getValue();
		}
		//kills
		for (Entry<String, Integer> e : kills.entrySet()) {
			points.put(e.getKey(), points.get(e.getKey()) + ( e.getValue() * plugin.pointsPerKill ));
			money.put(e.getKey(), money.get(e.getKey()) + ( e.getValue() * plugin.cashPerKill ));
			killsAll += e.getValue();
		}
		//teamattacks
		for (Entry<String, Integer> e : teamattacks.entrySet()) {
			points.put(e.getKey(), points.get(e.getKey()) + ( e.getValue() * plugin.pointsPerTeamattack ));
			teamattacksAll += e.getValue();
		}
		//grenades
		for (Entry<String, Integer> e : grenades.entrySet()) {
			grenadesAll += e.getValue();
		}
		//airstrikes
		for (Entry<String, Integer> e : airstrikes.entrySet()) {
			airstrikesAll += e.getValue();
		}
		
		//PLAYER STATS
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			
			@Override
			public void run() {
				boolean auto = plugin.sql.getAutoCommit();
				plugin.sql.setAutoCommit(false);
				
				for (Player p : match.getAllPlayer()) {
					final Map<PlayerStat, Integer> pStats = new HashMap<PlayerStat, Integer>();
					final String name = p.getName();
					pStats.put(PlayerStat.SHOTS, shots.get(name));
					pStats.put(PlayerStat.HITS, hits.get(name));
					pStats.put(PlayerStat.KILLS, kills.get(name));
					pStats.put(PlayerStat.DEATHS, deaths.get(name));
					pStats.put(PlayerStat.TEAMATTACKS, teamattacks.get(name));
					pStats.put(PlayerStat.GRENADES, grenades.get(name));
					pStats.put(PlayerStat.AIRSTRIKES, airstrikes.get(name));
					pStats.put(PlayerStat.POINTS, points.get(name));
					pStats.put(PlayerStat.MONEY, money.get(name));
					pStats.put(PlayerStat.WINS, wins.get(name));
					pStats.put(PlayerStat.DEFEATS, defeats.get(name));
					pStats.put(PlayerStat.DRAWS, draws.get(name));
					
					plugin.pm.addStats(name, pStats);
				}
				plugin.sql.commit();
				plugin.sql.setAutoCommit(auto);
				
				//Finished. Let's go on:
				plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
					public void run() {
						//close match
						plugin.am.setNotActive(match.getArena());
						match.updateTags();
						matches.remove(match);
						//ready? countdown?
						plugin.nf.status(Translator.getString("CHOOSE_TEAM"));

						//players:
						plugin.nf.players();

						if (ready().equalsIgnoreCase(Translator.getString("READY"))) {
							countdown(plugin.countdown, plugin.countdownInit);
						} else {
							plugin.nf.status(ready());
						}
						//TIME
						long time2 = System.nanoTime();
						Double delta = (time2 - time1)/10E6;
						DecimalFormat dec = new DecimalFormat("#.###");
						if(plugin.debug) plugin.nf.text("+Async Stats Saving: Took " + dec.format(delta) + " ms");
					}
				});
			}
		});
		
		//ARENA STATS
		Map<ArenaStat, Integer> aStats = new HashMap<ArenaStat, Integer>();
		aStats.put(ArenaStat.SHOTS, shotsAll);
		aStats.put(ArenaStat.KILLS, killsAll);
		aStats.put(ArenaStat.ROUNDS, 1);
		aStats.put(ArenaStat.GRENADES, grenadesAll);
		aStats.put(ArenaStat.AIRSTRIKES, airstrikesAll);
		
		plugin.am.addStats(match.getArena(), aStats);

		//GENERAL STATS
		Map<GeneralStat, Integer> gStats = new HashMap<GeneralStat, Integer>();
		gStats.put(GeneralStat.ROUNDS, 1);
		gStats.put(GeneralStat.SHOTS, shotsAll);
		gStats.put(GeneralStat.KILLS, killsAll);
		gStats.put(GeneralStat.GRENADES, grenadesAll);
		gStats.put(GeneralStat.AIRSTRIKES, airstrikesAll);
		
		plugin.stats.matchEndStats(gStats, match.getAllPlayer().size());

		//messages:
		Map<String, String> vars = new HashMap<String, String>();
		plugin.nf.text("-------------------------------------------------");
		plugin.nf.status(Translator.getString("MATCH_IS_OVER"));
		if (draw) {
			plugin.nf.text(Translator.getString("MATCH_DRAW"));
		} else {
			vars.put("winner_color", Lobby.getTeam(match.win).color().toString());
			vars.put("winner", match.win);
			vars.put("winner_size", String.valueOf(match.winners.size()));
			vars.put("looser_color", Lobby.getTeam(match.loose).color().toString());
			vars.put("looser", match.loose);
			vars.put("looser_size", String.valueOf(match.loosers.size()));
			plugin.nf.text(Translator.getString("WINNER_TEAM", vars));

			vars.put("points", String.valueOf(plugin.pointsPerWin));
			vars.put("money", String.valueOf(plugin.cashPerWin));
			plugin.nf.text(Translator.getString("WINNER_BONUS", vars));
		}
		
		vars.put("points", String.valueOf(plugin.pointsPerRound));
		vars.put("money", String.valueOf(plugin.cashPerRound));
		plugin.nf.text(Translator.getString("ROUND_BONUS", vars));

		plugin.nf.text(Translator.getString("MATCH_STATS"));
		vars.put("shots", String.valueOf(shotsAll));
		vars.put("hits", String.valueOf(hitsAll));
		vars.put("teamattacks", String.valueOf(teamattacksAll));
		vars.put("kills", String.valueOf(killsAll));
		plugin.nf.text(Translator.getString("MATCH_SHOTS", vars));
		plugin.nf.text(Translator.getString("MATCH_HITS", vars));
		plugin.nf.text(Translator.getString("MATCH_TEAMATTACKS", vars));
		plugin.nf.text(Translator.getString("MATCH_KILLS", vars));
		plugin.nf.text("-------------------------------------------------");
		if (!draw) {
			for (final Player p : match.winners) {
				if (Lobby.getTeam(p) != null) {
					plugin.nf.status(p, Translator.getString("YOU_WON"));
					if (plugin.melody) {
						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							
							@Override
							public void run() {
								plugin.musik.playWin(p);
							}
						}, plugin.melodyDelay);
						
					}
				}

			}
			for(final Player p :  match.loosers) {
				if (Lobby.getTeam(p) != null) {
					plugin.nf.status(p, Translator.getString("YOU_LOST"));
					if (plugin.melody) {
						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							
							@Override
							public void run() {
								plugin.musik.playDefeat(p);
							}
						}, plugin.melodyDelay);
						
					}
				}
			}
		} else {
			for (final Player p : match.getAllPlayer()) {
				if (Lobby.getTeam(p) != null) {
					plugin.nf.status(p, Translator.getString("YOU_DRAW"));
					if (plugin.melody) {
						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							
							@Override
							public void run() {
								plugin.musik.playDraw(p);
							}
						}, plugin.melodyDelay);
						
					}
				}
			}
		}
		
		//TIME
		long time2 = System.nanoTime();
		Double delta = (time2 - time1)/10E6;
		DecimalFormat dec = new DecimalFormat("#.###");
		if(plugin.debug) plugin.nf.text("Main-Thread: Took " + dec.format(delta) + " ms");
	}
	
	public boolean softCheck() {
		if (plugin.softreload) {
			if (countdown != null) {
				countdown.end();
				countdown = null;
			}
			if (matches.size() <= 0) {
				return true;
			}
		}
		return false;
	}

	public synchronized Match getMatch(Player player) {
		for (Match m : matches) {
			if (m.inMatch(player)) return m;
		}
		return null;
	}

	public synchronized String ready() {
		//softreload-check:
		if (softCheck()) plugin.reload(null);
		//activated?
		if (!plugin.active) return Translator.getString("NEW_MATCHES_DISABLED");
		//no game active
		if (matches.size() > 0) return Translator.getString("ACTIVE_MATCH");
		//1 player in each team waiting for game or 2 randoms (or mix) 
		int players = Lobby.RED.numberWaiting() + Lobby.BLUE.numberWaiting() + Lobby.RANDOM.numberWaiting();
		if (players >= plugin.minPlayers && ( (Lobby.BLUE.numberWaiting() >=1 && Lobby.RED.numberWaiting() >= 1) || (Lobby.RANDOM.numberWaiting() >= 2) || (Lobby.RANDOM.numberWaiting() >= 1 && Lobby.RED.numberWaiting() >= 1) || (Lobby.RANDOM.numberWaiting() >= 1 && Lobby.BLUE.numberWaiting() >= 1) )) {
			if (!plugin.am.isReady()) return Translator.getString("NO_ARENA_READY");
			//ready=>
			return Translator.getString("READY");
		} else return Translator.getString("NOT_ENOUGH_PLAYERS");
	}

	public void countdown(int number, int initial) {
		if (countdown == null && plugin.active) {
			plugin.nf.status(Translator.getString("NEW_MATCH_STARTS_SOON"));
			countdown = new Timer(plugin, 20 * initial, 20L, number, new Runnable() {
				
				@Override
				public void run() {
					if (plugin.useXPBar) {
						for (Player player : Lobby.LOBBY.getMembers()) {
							player.setLevel(countdown.getTime());	
						}
					}
					
					if (countdown.getTime() <= 5) {
						for (Player player : Lobby.LOBBY.getMembers()) {
							player.playSound(player.getLocation(), Sound.ORB_PICKUP, 80L, 1L);	
						}
					}
				}
			}, new Runnable() {
				
				@Override
				public void run() {
					plugin.nf.counter(countdown.getTime());
				}
			}, new Runnable() {
				
				@Override
				public void run() {
					countdown = null;
					for (Player player : Lobby.LOBBY.getMembers()) {
						player.playSound(player.getLocation(), Sound.ORB_PICKUP, 100L, 2L);	
					}
					String status = ready();
					if(status.equalsIgnoreCase(Translator.getString("READY"))) {
						//start match
						gameStart();
					} else {
						plugin.nf.status(status);
					}
				}
			});
		}
	}

}
