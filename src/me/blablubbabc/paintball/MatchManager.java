package me.blablubbabc.paintball;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class MatchManager{

	private Paintball plugin;

	private ArrayList<Match> matches;
	public boolean countdownStarted;
	private int taskID;
	private int count;

	public MatchManager(Paintball pl) {
		plugin = pl;
		matches = new ArrayList<Match>();

		countdownStarted = false;
	}

	public synchronized void forceReload() {
		//closing all matches and kicking all players from lobby:
		ArrayList<Match> mlist = new ArrayList<Match>();
		for(Match m : matches) {
			mlist.add(m);
		}
		for(Match match : mlist) {
			//colors
			match.undoAllColors();
			//Teleport all remaining players back to lobby:
			for(Player p : match.getAll()) {
				//ist nicht aus minecraft raus:
				if(Lobby.isPlaying(p) || Lobby.isSpectating(p)){
					//lobby
					Lobby.getTeam(p).setWaiting(p);
					//clear inventory
					plugin.clearInv(p);
					//noch im match:
					if(match.isSurvivor(p)){
						//teleport is survivor:
						plugin.joinLobby(p);
					}
				}
			}

			//close match
			plugin.am.setNotActive(match.getArena());
			match.endSchedulers();
			matches.remove(match);	
		}
		//messages:
		plugin.nf.status(plugin.t.getString("ALL_KICKED_FROM_MATCHES"));
		//stop countdown:
		if(countdownStarted) {
			plugin.getServer().getScheduler().cancelTask(taskID);
			countdownStarted = false;
		}
		// Kick all from Lobby:
		plugin.nf.status(plugin.t.getString("ALL_KICKED_FROM_LOBBY"));
		plugin.nf.status(plugin.t.getString("RELOADING_PAINTBALL"));
		ArrayList<Player> list = new ArrayList<Player>();
		for(Player p : Lobby.LOBBY.getMembers()) {
			list.add(p);
		}
		for(Player p : list) {
			plugin.leaveLobby(p, false, true, true);
		}
	}

	public void softCheck() {
		if(plugin.softreload) {
			if(countdownStarted) {
				plugin.getServer().getScheduler().cancelTask(taskID);
				countdownStarted = false;
			}
			if(matches.size() <= 0) {
				plugin.reload();
			}
		}
	}

	public synchronized void gameStart() {
		//auto spec lobby
		if(plugin.autoSpecLobby) {
			for(Player player : Lobby.LOBBY.getMembers()) {
				if(Lobby.getTeam(player).equals(Lobby.LOBBY)) {
					Lobby.SPECTATE.addMember(player);
					HashMap<String, String> vars = new HashMap<String, String>();
					vars.put("color_team", Lobby.SPECTATE.color().toString());
					vars.put("team", Lobby.SPECTATE.getName());
					player.sendMessage(plugin.t.getString("YOU_JOINED_SPECTATORS", vars));
				}
			}
		}

		int players = Lobby.RED.numberWaiting() + Lobby.BLUE.numberWaiting() + Lobby.RANDOM.numberWaiting();
		String info = plugin.nf.getPlayersOverview();

		for(Player player : Lobby.RANDOM.getMembers()) {
			Lobby.RANDOM.setPlaying(player);
		}
		for(Player player : Lobby.BLUE.getMembers()) {
			Lobby.BLUE.setPlaying(player);
		}
		for(Player player : Lobby.RED.getMembers()) {
			Lobby.RED.setPlaying(player);
		}
		for(Player player : Lobby.SPECTATE.getMembers()) {
			Lobby.SPECTATE.setPlaying(player);
		}
		//Arena:
		String arena = plugin.am.getNextArena();
		plugin.am.resetNext();
		plugin.am.setActive(arena);
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("arena", arena);
		plugin.nf.status(plugin.t.getString("MATCH_START_ARENA", vars));
		vars.put("players", String.valueOf(players));
		vars.put("players_overview", info);
		plugin.nf.status(plugin.t.getString("MATCH_START_PLAYERS_OVERVIEW", vars));

		Match match = new Match(plugin, Lobby.RED.getMembers(), Lobby.BLUE.getMembers(), Lobby.SPECTATE.getMembers(), Lobby.RANDOM.getMembers(), arena);
		matches.add(match);
	}
	
	public synchronized void gameEnd(final Match match, boolean draw, HashMap<String, Location> playersLoc, Set<Player> specs, 
			HashMap<String, Integer> shots, HashMap<String, Integer> hits, HashMap<String, Integer> kills, HashMap<String, Integer> deaths,
			HashMap<String, Integer> teamattacks, HashMap<String, Integer> grenades, HashMap<String, Integer> airstrikes) {
		
		//STATS
		HashMap<String, Integer> wins = new HashMap<String, Integer>();
		HashMap<String, Integer> defeats = new HashMap<String, Integer>();
		HashMap<String, Integer> draws = new HashMap<String, Integer>();
		HashMap<String, Integer> points = new HashMap<String, Integer>();
		HashMap<String, Integer> money = new HashMap<String, Integer>();

		for(Player p : match.getAllPlayer()) {
			points.put(p.getName(), plugin.pointsPerRound);
			money.put(p.getName(), plugin.cashPerRound);
			if(draw) {
				draws.put(p.getName(), 1);
				wins.put(p.getName(), 0);
				defeats.put(p.getName(), 0);
			}
			else draws.put(p.getName(), 0);
		}
		
		if(!draw) {
			for(Player p : match.winners) {
				//stats
				wins.put(p.getName(), 1);
				defeats.put(p.getName(), 0);
				//bonus
				points.put(p.getName(), points.get(p.getName())+plugin.pointsPerWin);
				money.put(p.getName(), points.get(p.getName())+plugin.cashPerWin);

			}
			for(Player p :  match.loosers) {
				//stats
				wins.put(p.getName(), 0);
				defeats.put(p.getName(), 1);
			}
		}

		//Teleport all remaining players back to lobby:
		for(Player p : match.getAll()) {
			//if is a remaining player:
			if(Lobby.isPlaying(p) || Lobby.isSpectating(p)){

				//afk detection update on match end
				if(plugin.afkDetection && !match.isSpec(p)) {
					if(p.getLocation().getWorld().equals(playersLoc.get(p.getName()).getWorld()) && p.getLocation().distance(playersLoc.get(p.getName())) <= plugin.afkRadius && shots.get(p.getName()) == 0 && kills.get(p.getName()) == 0) {
						int afkCount;
						if(plugin.afkMatchCount.get(p.getName()) != null) {
							afkCount = plugin.afkMatchCount.get(p.getName());
						} else {
							afkCount = 0;
						}
						plugin.afkMatchCount.put(p.getName(), afkCount+1);
					}else {
						plugin.afkMatchCount.remove(p.getName());
					}
				}

				//teleport is survivor:
				plugin.joinLobby(p);
			}
		}

		//afk detection clean up and consequences:
		if(plugin.afkDetection) {
			//clearing players from hashmap which didn't play the during the last match or can't be found
			ArrayList<String> entries = new ArrayList<String>();
			
			for(String s : plugin.afkMatchCount.keySet()) {
				entries.add(s);
			}
			
			for(String afkP : entries) {
				Player player = plugin.getServer().getPlayer(afkP);
				if(player != null) {
					if(!playersLoc.containsKey(afkP)) {
						plugin.afkMatchCount.remove(afkP);
					} else if(plugin.afkMatchCount.get(afkP) >= plugin.afkMatchAmount){
						//afk detection consequences after being afk:
						plugin.afkMatchCount.remove(afkP);
						Lobby.getTeam(player).removeMember(player);
						plugin.nf.afkLeave(player, match);
						player.sendMessage(plugin.t.getString("YOU_LEFT_TEAM"));
					}
				} else {
					plugin.afkMatchCount.remove(afkP);
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
		for(Entry<String, Integer> e : shots.entrySet()) {
			shotsAll += e.getValue();
		}
		//hits
		for(Entry<String, Integer> e : hits.entrySet()) {
			points.put(e.getKey(), points.get(e.getKey()) + ( e.getValue() * plugin.pointsPerHit ));
			money.put(e.getKey(), money.get(e.getKey()) + ( e.getValue() * plugin.cashPerHit ));
			hitsAll += e.getValue();
		}
		//kills
		for(Entry<String, Integer> e : kills.entrySet()) {
			points.put(e.getKey(), points.get(e.getKey()) + ( e.getValue() * plugin.pointsPerKill ));
			money.put(e.getKey(), money.get(e.getKey()) + ( e.getValue() * plugin.cashPerKill ));
			killsAll += e.getValue();
		}
		//teamattacks
		for(Entry<String, Integer> e : teamattacks.entrySet()) {
			points.put(e.getKey(), points.get(e.getKey()) + ( e.getValue() * plugin.pointsPerTeamattack ));
			teamattacksAll += e.getValue();
		}
		//grenades
		for(Entry<String, Integer> e : grenades.entrySet()) {
			grenadesAll += e.getValue();
		}
		//airstrikes
		for(Entry<String, Integer> e : airstrikes.entrySet()) {
			airstrikesAll += e.getValue();
		}
		
		//PLAYER STATS
		for(Player p : match.getAllPlayer()) {
			final HashMap<String, Integer> pStats = new HashMap<String, Integer>();
			final String name = p.getName();
			pStats.put("shots", shots.get(name));
			pStats.put("hits", hits.get(name));
			pStats.put("kills", kills.get(name));
			pStats.put("deaths", deaths.get(name));
			pStats.put("teamattacks", teamattacks.get(name));
			pStats.put("grenades", grenades.get(name));
			pStats.put("airstrikes", airstrikes.get(name));
			pStats.put("points", points.get(name));
			pStats.put("money", money.get(name));
			pStats.put("wins", wins.get(name));
			pStats.put("defeats", defeats.get(name));
			pStats.put("draws", draws.get(name));
			
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
				
				@Override
				public void run() {
					plugin.pm.addStats(name, pStats);
				}
			});
			//plugin.pm.addStats(name, pStats);
		}
		//ARENA STATS
		final HashMap<String, Integer> aStats = new HashMap<String, Integer>();
		aStats.put("shots", shotsAll);
		aStats.put("kills", killsAll);
		aStats.put("rounds", 1);
		aStats.put("grenades", grenadesAll);
		aStats.put("airstrikes", airstrikesAll);
		
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			
			@Override
			public void run() {
				plugin.am.addStats(match.getArena(), aStats);
			}
		});
		//plugin.am.addStats(match.getArena(), aStats);

		//GENERAL STATS
		final HashMap<String, Integer> gStats = new HashMap<String, Integer>();
		gStats.put("rounds", 1);
		gStats.put("shots", shotsAll);
		gStats.put("kills", killsAll);
		gStats.put("grenades", grenadesAll);
		gStats.put("airstrikes", airstrikesAll);
		
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			
			@Override
			public void run() {
				plugin.stats.matchEndStats(gStats, match.getAllPlayer().size());
			}
		});
		
		//plugin.stats.matchEndStats(gStats, match.getAllPlayer().size());

		//messages:
		HashMap<String, String> vars = new HashMap<String, String>();
		plugin.nf.text("-------------------------------------------------");
		plugin.nf.status("Match is over!");
		if(draw) {
			plugin.nf.text(plugin.t.getString("MATCH_DRAW"));
		} else {
			vars.put("winner_color", Lobby.getTeam(match.win).color().toString());
			vars.put("winner", match.win);
			vars.put("winner_size", String.valueOf(match.winners.size()));
			vars.put("looser_color", Lobby.getTeam(match.loose).color().toString());
			vars.put("looser", match.loose);
			vars.put("looser_size", String.valueOf(match.loosers.size()));
			plugin.nf.text(plugin.t.getString("WINNER_TEAM", vars));

			vars.put("points", String.valueOf(plugin.pointsPerWin));
			vars.put("money", String.valueOf(plugin.cashPerWin));
			plugin.nf.text(plugin.t.getString("WINNER_BONUS", vars));
		}
		
		vars.put("points", String.valueOf(plugin.pointsPerRound));
		vars.put("money", String.valueOf(plugin.cashPerRound));
		plugin.nf.text(plugin.t.getString("ROUND_BONUS", vars));

		plugin.nf.text(plugin.t.getString("MATCH_STATS"));
		vars.put("shots", String.valueOf(shotsAll));
		vars.put("hits", String.valueOf(hitsAll));
		vars.put("teamattacks", String.valueOf(teamattacksAll));
		vars.put("kills", String.valueOf(killsAll));
		plugin.nf.text(plugin.t.getString("MATCH_SHOTS", vars));
		plugin.nf.text(plugin.t.getString("MATCH_HITS", vars));
		plugin.nf.text(plugin.t.getString("MATCH_TEAMATTACKS", vars));
		plugin.nf.text(plugin.t.getString("MATCH_KILLS", vars));
		plugin.nf.text("-------------------------------------------------");

		//close match
		plugin.am.setNotActive(match.getArena());
		matches.remove(match);
		//ready? countdown?
		plugin.nf.status(plugin.t.getString("CHOOSE_TEAM"));

		//players:
		plugin.nf.players();

		if(ready().equalsIgnoreCase(plugin.t.getString("READY"))) {
			countdown(plugin.countdown, plugin.countdownInit);
		} else {
			plugin.nf.status(ready());
		}
	}

	public synchronized Match getMatch(Player player) {
		for(Match m : matches) {
			if(m.inMatch(player)) return m;
		}
		return null;
	}

	public synchronized String ready() {
		//softreload-check:
		softCheck();
		//activated?
		if(!plugin.active) return plugin.t.getString("NEW_MATCHES_DISABLED");
		//no game active
		if(matches.size() > 0) return plugin.t.getString("ACTIVE_MATCH");
		//1 player in each team waiting for game or 2 randoms (or mix) 
		int players = Lobby.RED.numberWaiting() + Lobby.BLUE.numberWaiting() + Lobby.RANDOM.numberWaiting();
		if(players >= plugin.minPlayers && ( (Lobby.BLUE.numberWaiting()>=1 && Lobby.RED.numberWaiting()>=1) || (Lobby.RANDOM.numberWaiting() >= 2) || (Lobby.RANDOM.numberWaiting()>=1 && Lobby.RED.numberWaiting()>=1) || (Lobby.RANDOM.numberWaiting()>=1 && Lobby.BLUE.numberWaiting()>=1) )) {
			if(!plugin.am.isReady()) return plugin.t.getString("NO_ARENA_READY");
			//ready=>
			return plugin.t.getString("READY");
		} else return plugin.t.getString("NOT_ENOUGH_PLAYERS");
	}

	public void countdown(int number, int initial) {
		if(!plugin.mm.countdownStarted && plugin.active) {
			plugin.nf.status(plugin.t.getString("NEW_MATCH_STARTS_SOON"));
			countdownStarted = true;
			count = number;
			taskID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable(){

				@Override
				public void run() {
					if(( count % 10 ) == 0 && count > 10 )
					{
						//if above 10 and divisable by 10 message here
						plugin.nf.counter(count);
					}

					if( count < 10 && count > 0)
					{
						//if below 10 message here (regardless of divisibility)
						plugin.nf.counter(count);
					}
					count--;
					if( count < 1) {
						plugin.getServer().getScheduler().cancelTask(taskID);
						countdownStarted = false;
						String status = ready();
						if(status.equalsIgnoreCase(plugin.t.getString("READY"))) {
							//start match
							gameStart();
						} else {
							plugin.nf.status(status);
						}
					}
				}

			}, (long) (20*initial), 20L);
		}
	}

}
