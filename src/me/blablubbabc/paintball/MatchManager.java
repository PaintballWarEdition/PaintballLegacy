package me.blablubbabc.paintball;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
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
	
	public void forceReload() {
		//closing all matches and kicking all players from lobby:
		ArrayList<Match> mlist = new ArrayList<Match>();
		for(Match m : matches) {
			mlist.add(m);
		}
		for(Match match : mlist) {
			//match.unhideAll();
			//teleport Lobby:
			ArrayList<LinkedHashMap<String, Object>> lobbyspawns = plugin.getLobbySpawns();
			
			//colors
			match.undoAllColors();
			//Teleport all remaining players back to lobby:
			int spawn = 0;
			for(Player p : match.getAllPlayers()) {
				//ist nicht aus minecraft raus:
				if(!match.hasLeft(p)){
					//lobby
					Lobby.getTeam(p).setWaiting(p);
					//clear inventory
					plugin.clearInv(p);
					//noch im match:
					if(match.isSurvivor(p)){
						//teleport is survivor:
						if (spawn > (lobbyspawns.size() - 1)) spawn = 0;
						p.teleport(plugin.transformLocation(lobbyspawns.get(spawn)));
						spawn++;
					}
				}
				//no stats saving here
			}
			
			//close match
			plugin.am.toggleReady(match.getArena());
			matches.remove(match);	
		}
		//messages:
		plugin.nf.status(plugin.light_purple+"All players kicked from their matches.");
		//stop countdown:
		if(countdownStarted) {
			plugin.getServer().getScheduler().cancelTask(taskID);
	    	countdownStarted = false;
		}
		// Kick all from Lobby:
		plugin.nf.status(plugin.light_purple+"All players kicked from the paintball lobby.");
		plugin.nf.status(plugin.light_purple+"Reloading the paintball plugin..");
		ArrayList<Player> list = new ArrayList<Player>();
		for(Player p : Lobby.LOBBY.getMembers()) {
			list.add(p);
		}
		for(Player p : list) {
			//Lobby.remove(p);
			//p.teleport(plugin.pm.getLoc(p));
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
	
	public void gameStart() {
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
		plugin.am.toggleReady(arena);
		plugin.nf.status("Match starts! Arena: "+plugin.gray+arena);
		plugin.nf.status(plugin.gray+""+players+plugin.gold+" players: "+info);
		
		Match match = new Match(plugin, plugin.lives, Lobby.RED.getMembers(), Lobby.BLUE.getMembers(), Lobby.SPECTATE.getMembers(), Lobby.RANDOM.getMembers(), arena);
		matches.add(match);
	}
	
	public void gameEnd(Match match, Set<Player> winners, String win, Set<Player> loosers, String loose, Set<Player> specs, LinkedHashMap<Player, Integer> shots,
			LinkedHashMap<Player, Integer> hits, LinkedHashMap<Player, Integer> deaths, LinkedHashMap<Player, Integer> kills, LinkedHashMap<Player, Integer> teamattacks) {
		//teleport Lobby:
		ArrayList<LinkedHashMap<String, Object>> lobbyspawns = plugin.getLobbySpawns();
		//stats etc
		for(Player p : winners) {
			//stats
			plugin.pm.addWins(p.getName(), 1);
			plugin.pm.addPoints(p.getName(), (plugin.pointsPerRound + plugin.pointsPerWin));
			plugin.pm.addMoney(p.getName(), (plugin.cashPerRound + plugin.cashPerWin));
			
		}
		for(Player p : loosers) {
			//stats
			plugin.pm.addLooses(p.getName(), 1);
			plugin.pm.addPoints(p.getName(), plugin.pointsPerRound);
			plugin.pm.addMoney(p.getName(), plugin.cashPerRound);
			
		}
		//Teleport all remaining players back to lobby:
		int spawn = 0;
		for(Player p : match.getAllPlayers()) {
			//if is a remaining player:
			if(!match.hasLeft(p)){
				//lobby
				Lobby.getTeam(p).setWaiting(p);
				//clear inventory
				plugin.clearInv(p);
				//noch im match:
				if(match.isSurvivor(p)){
					//teleport is survivor:
					if (spawn > (lobbyspawns.size() - 1)) spawn = 0;
					p.teleport(plugin.transformLocation(lobbyspawns.get(spawn)));
					spawn++;
				}
			}
		}
		
		//shots
		int shotsAll = 0;
		for(Entry<Player, Integer> e : shots.entrySet()) {
			plugin.pm.addShots(e.getKey().getName(), e.getValue());
			shotsAll += e.getValue();
		}
		//hits
		int hitsAll = 0;
		for(Entry<Player, Integer> e : hits.entrySet()) {
			plugin.pm.addHits(e.getKey().getName(), e.getValue());
			plugin.pm.addPoints(e.getKey().getName(), ( e.getValue() * plugin.pointsPerHit ));
			plugin.pm.addMoney(e.getKey().getName(), ( e.getValue() * plugin.cashPerHit ));
			hitsAll += e.getValue();
		}
		//kills
		int killsAll = 0;
		for(Entry<Player, Integer> e : kills.entrySet()) {
			plugin.pm.addkills(e.getKey().getName(), e.getValue());
			plugin.pm.addPoints(e.getKey().getName(), ( e.getValue() * plugin.pointsPerKill ));
			plugin.pm.addMoney(e.getKey().getName(), ( e.getValue() * plugin.cashPerKill ));
			killsAll += e.getValue();
		}
		//deaths
		//int deathsAll = 0;
		for(Entry<Player, Integer> e : deaths.entrySet()) {
			plugin.pm.addDeaths(e.getKey().getName(), e.getValue());
			//deathsAll += e.getValue();
		}
		//teamattacks
		int teamattacksAll = 0;
		for(Entry<Player, Integer> e : teamattacks.entrySet()) {
			plugin.pm.addTeamattacks(e.getKey().getName(), e.getValue());
			plugin.pm.addPoints(e.getKey().getName(), ( e.getValue() * plugin.pointsPerTeamattack ));
			teamattacksAll += e.getValue();
		}
		
		//arena stats:
		plugin.am.addShots(match.getArena(), shotsAll);
		plugin.am.addRounds(match.getArena(), 1);
		plugin.am.addKills(match.getArena(), killsAll);
		//general stats:
		plugin.stats.addRounds(1);
		plugin.stats.addShots(shotsAll);
		plugin.stats.addKills(killsAll);
		
		//save:
		plugin.pm.saveData();
		plugin.stats.saveData();
		plugin.am.saveData();
		//messages:
		plugin.nf.status("Match is over!");
		plugin.nf.text("Team "+Lobby.getTeam(win).color()+win+" ("+winners.size()+" players) "+plugin.aqua+"has won against team "+Lobby.getTeam(loose).color()+loose+" ("+loosers.size()+" players)");
		plugin.nf.text(Lobby.getTeam(win).color()+"Winner"+plugin.aqua+" bonus: "+plugin.green+plugin.pointsPerWin+plugin.aqua+" points and "+plugin.green+plugin.cashPerWin+plugin.aqua+" cash!");
		plugin.nf.text("Round bonus for everyone: "+plugin.green+plugin.pointsPerRound+plugin.aqua+" points and "+plugin.green+plugin.cashPerRound+plugin.aqua+" cash!");
		plugin.nf.text(plugin.dark_green+"Some match stats: ");
		plugin.nf.text(plugin.light_purple+"Shots fired: "+plugin.green+shotsAll);
		plugin.nf.text(plugin.light_purple+"Hits: "+plugin.green+hitsAll);
		plugin.nf.text(plugin.light_purple+"Teamattacks: "+plugin.green+teamattacksAll);
		plugin.nf.text(plugin.light_purple+"Kills: "+plugin.green+killsAll);
		plugin.nf.text(plugin.dark_green+"General stats: ");
		plugin.nf.text(plugin.light_purple+"Shots fired: "+plugin.green+plugin.stats.getShots());
		plugin.nf.text(plugin.light_purple+"Kills: "+plugin.green+plugin.stats.getKills());
		plugin.nf.text(plugin.light_purple+"Played rounds: "+plugin.green+plugin.stats.getRounds());
		plugin.nf.text(plugin.light_purple+"Money spent in shop: "+plugin.green+plugin.stats.getMoney());
		plugin.nf.text("-------------------------------------------------");
		
		//close match
		plugin.am.toggleReady(match.getArena());
		matches.remove(match);
		//ready? countdown?
		plugin.nf.status("Choose you team now!");
		
		//players:
		plugin.nf.players();
		
		if(ready().equalsIgnoreCase("ready")) {
			countdown(plugin.countdown, plugin.countdownInit);
		} else {
			plugin.nf.status(ready());
		}
	}
	
	public Match getMatch(Player player) {
		for(Match m : matches) {
			if(m.inMatch(player)) return m;
		}
		return null;
	}
	
	public String ready() {
		//softreload-check:
		softCheck();
		//activated?
		if(!plugin.active) return "new matches disabled";
		//no game active
		if(matches.size() > 0) return "active game";
		//1 player in each team waiting for game or 2 randoms (or mix) 
		int players = Lobby.RED.numberWaiting() + Lobby.BLUE.numberWaiting() + Lobby.RANDOM.numberWaiting();
		if(players >= plugin.minPlayers && ( (Lobby.BLUE.numberWaiting()>=1 && Lobby.RED.numberWaiting()>=1) || (Lobby.RANDOM.numberWaiting() >= 2) || (Lobby.RANDOM.numberWaiting()>=1 && Lobby.RED.numberWaiting()>=1) || (Lobby.RANDOM.numberWaiting()>=1 && Lobby.BLUE.numberWaiting()>=1) )) {
			if(!plugin.am.isReady()) return "no arena ready";
			//ready=>
			return "ready";
		} else return "not enough waiting players";
	}
	
	public void countdown(int number, int initial) {
		if(!plugin.mm.countdownStarted && plugin.active) {
			plugin.nf.status("A new match starts soon!");
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
				    	if(status.equalsIgnoreCase("ready")) {
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
