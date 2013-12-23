package de.blablubbabc.paintball;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import de.blablubbabc.paintball.statistics.arena.ArenaStat;
import de.blablubbabc.paintball.statistics.general.GeneralStat;
import de.blablubbabc.paintball.statistics.player.PlayerStat;
import de.blablubbabc.paintball.statistics.player.PlayerStats;
import de.blablubbabc.paintball.statistics.player.match.tdm.TDMMatchStat;
import de.blablubbabc.paintball.statistics.player.match.tdm.TDMMatchStats;
import de.blablubbabc.paintball.utils.KeyValuePair;
import de.blablubbabc.paintball.utils.Timer;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class MatchManager {

	private final Paintball plugin;
	private final List<Match> matches;
	
	private Timer countdown;
	private VoteManager voteManager;

	public MatchManager(Paintball pl) {
		plugin = pl;
		matches = new ArrayList<Match>();
		if (plugin.arenaVoting) voteManager = new VoteManager(plugin.arenaVotingOptions, plugin.arenaVotingRandomOption);
	}

	public void handleArenaVote(Player player, String vote) {
		if (voteManager.isOver()) {
			player.sendMessage(Translator.getString("GAME_VOTE_IS_OVER"));
		} else if (!voteManager.isValid()) {
			player.sendMessage(Translator.getString("GAME_VOTE_DISABLED"));
		} else {
			Integer voteID = Utils.parseInteger(vote);
			
			if (voteID != null) {
				voteManager.handleVote(player, voteID);
			} else {
				//player.sendMessage(Translator.getString("INVALID_ID"));
				voteManager.handleVote(player, vote);
			}
		}
	}
	
	public void sendVoteOptions(Player player) {
		if (voteManager.isOver()) {
			player.sendMessage(Translator.getString("GAME_VOTE_IS_OVER"));
		} else if (!voteManager.isValid()) {
			player.sendMessage(Translator.getString("GAME_VOTE_DISABLED"));
		} else {
			voteManager.sendVoteOptions(player);
		}
	}
	
	public void onLobbyLeave(Player player) {
		if (!voteManager.isOver() && voteManager.isValid()) voteManager.handleVoteUndo(player.getName());
	}
	
	/*public boolean isVotingOver() {
		return voteManager.isOver();
	}*/
	
	public synchronized void forceReload() {
		//closing all matches and kicking all players from lobby:
		List<Match> mlist = new ArrayList<Match>(matches);
		
		for (Match match : mlist) {
			//colors
			match.undoAllColors();
			//Teleport all remaining players back to lobby:
			match.resetWeaponStuffEnd();
			for (Player p : match.getAll()) {
				if (Lobby.isPlaying(p) || Lobby.isSpectating(p)) {
					match.resetWeaponStuff(p);
					plugin.playerManager.enterLobby(p);
				}
			}

			//close match
			plugin.arenaManager.setNotActive(match.getArena());
			match.endTimers();
			match.updateTags();
			matches.remove(match);	
		}
		//messages:
		plugin.feeder.status(Translator.getString("ALL_KICKED_FROM_MATCHES"));
		//stop countdown:
		if (countdown != null) {
			countdown.end();
			countdown = null;
		}
		// Kick all from Lobby:
		plugin.feeder.status(Translator.getString("ALL_KICKED_FROM_LOBBY"));
		plugin.feeder.status(Translator.getString("RELOADING_PAINTBALL"));
		List<Player> list = new ArrayList<Player>();
		
		for (Player p : Lobby.LOBBY.getMembers()) {
			list.add(p);
		}
		
		for (Player p : list) {
			plugin.playerManager.leaveLobby(p, false);
		}
	}

	public synchronized void gameStart(String arena) {
		//auto spec lobby
		if (plugin.autoSpecLobby) {
			for (Player player : Lobby.LOBBY.getMembers()) {
				if (Lobby.getTeam(player).equals(Lobby.LOBBY)) {
					Lobby.SPECTATE.addMember(player);
					Map<String, String> vars = new HashMap<String, String>();
					vars.put("color_team", Lobby.SPECTATE.color().toString());
					vars.put("team", Lobby.SPECTATE.getName());
					player.sendMessage(Translator.getString("YOU_JOINED_SPECTATORS", vars));
				}
			}
		}

		int players = Lobby.RED.numberWaiting() + Lobby.BLUE.numberWaiting() + Lobby.RANDOM.numberWaiting();
		String info = plugin.feeder.getPlayersOverview();

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
		
		//Arena: assume the given arena is ready
		plugin.arenaManager.resetNext();
		plugin.arenaManager.setActive(arena);
		
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("arena", arena);
		plugin.feeder.status(Translator.getString("MATCH_START_ARENA", vars));
		vars.put("players", String.valueOf(players));
		vars.put("players_overview", info);
		plugin.feeder.status(Translator.getString("MATCH_START_PLAYERS_OVERVIEW", vars));

		Match match = new Match(plugin, Lobby.RED.getMembers(), Lobby.BLUE.getMembers(), Lobby.SPECTATE.getMembers(), Lobby.RANDOM.getMembers(), arena);
		matches.add(match);
	}
	
	public synchronized void gameEnd(final Match match, boolean draw, Map<String, Location> playersLoc, Set<Player> specs, Map<String, TDMMatchStats> matchStats) {
		// TIME
		final long time1 = System.nanoTime();

		// STATS and TELEPORT TO LOBBY for the players:
		Map<Player, String> vaultRewards = new HashMap<Player, String>();
		
		for (Player player : match.getAll()) {
			String playerName = player.getName();
			
			// player ?
			if (match.getAllPlayer().contains(player)) {
				// STATS and vault rewards
				PlayerStats stats = plugin.playerManager.getPlayerStats(playerName);
				stats.addStat(PlayerStat.POINTS, plugin.pointsPerRound);
				stats.addStat(PlayerStat.MONEY, plugin.cashPerRound);

				stats.addStat(PlayerStat.ROUNDS, 1);
				
				double vaultReward = plugin.vaultRewardRound;
				
				TDMMatchStats mStats = matchStats.get(playerName);
				vaultReward += mStats.getStat(TDMMatchStat.HITS) * plugin.vaultRewardHit;
				vaultReward += mStats.getStat(TDMMatchStat.KILLS) * plugin.vaultRewardKill;
				
				if (draw) {
					stats.addStat(PlayerStat.DRAWS, 1);
				} else {
					if (match.winners.contains(player)) {
						stats.addStat(PlayerStat.WINS, 1);
						stats.addStat(PlayerStat.POINTS, plugin.pointsPerWin);
						stats.addStat(PlayerStat.MONEY, plugin.cashPerWin);
						
						vaultReward += plugin.vaultRewardWin;
					} else {
						stats.addStat(PlayerStat.DEFEATS, 1);
					}
				}
				
				// vault reward
				// if left lobby already -> directly give reward: -> now given directly
				/*if (Lobby.LOBBY.isMember(player)) {
					plugin.givePlayerVaultMoneyAfterSession(playerName, vaultReward);
					// inform player later:
					vaultRewards.put(player, String.valueOf(vaultReward));
				} else {
					plugin.givePlayerVaultMoneyInstant(playerName, vaultReward);
				}*/
				
				plugin.givePlayerVaultMoneyInstant(playerName, vaultReward);
				// inform player later:
				vaultRewards.put(player, String.valueOf(vaultReward));
				
				// AFK DETECTION
				if (Lobby.isPlaying(player)) {
					//afk detection update on match end
					TDMMatchStats playerMatchStats = matchStats.get(playerName);
					if (plugin.afkDetection && !match.isSpec(player)) {
						if (player.getLocation().getWorld().equals(playersLoc.get(playerName).getWorld()) 
								&& player.getLocation().distanceSquared(playersLoc.get(playerName)) <= plugin.afkRadius2
								&& playerMatchStats.getStat(TDMMatchStat.SHOTS) == 0 && playerMatchStats.getStat(TDMMatchStat.KILLS) == 0) {
							plugin.afkSet(playerName, plugin.afkGet(playerName) + 1);
						} else {
							plugin.afkRemove(playerName);
						}
					}
					
					// TELEPORT SURVIVOR TO LOBBY:
					plugin.playerManager.enterLobby(player);
				}
				
			} else {
				// SPECTATOR:
				if (Lobby.isSpectating(player)) {
					// TELEPORT SPECTATOR TO LOBBY:
					plugin.playerManager.enterLobby(player);
				}
			}
		}

		//afk detection clean up and consequences:
		if (plugin.afkDetection) {
			//clearing players from map which didn't play the during the last match or can't be found
			List<String> entries = plugin.afkGetEntries();
			
			for (String afkP : entries) {
				Player player = plugin.getServer().getPlayerExact(afkP);
				if (player != null) {
					if (!playersLoc.containsKey(afkP)) {
						plugin.afkRemove(afkP);
					} else if (plugin.afkGet(afkP) >= plugin.afkMatchAmount){
						//afk detection consequences after being afk:
						plugin.afkRemove(afkP);
						Lobby.getTeam(player).removeMember(player);
						plugin.feeder.afkLeave(player, match);
						player.sendMessage(Translator.getString("YOU_LEFT_TEAM"));
					}
				} else {
					plugin.afkRemove(afkP);
				}
			}
		}

		// MATCH STATS SUMMARY
		int shotsAll = 0;
		int hitsAll = 0;
		int killsAll = 0;
		int teamattacksAll = 0;
		int grenadesAll = 0;
		int airstrikesAll = 0;

		for (TDMMatchStats stats : matchStats.values()) {
			shotsAll += stats.getStat(TDMMatchStat.SHOTS);
			hitsAll += stats.getStat(TDMMatchStat.HITS);
			killsAll += stats.getStat(TDMMatchStat.KILLS);
			teamattacksAll += stats.getStat(TDMMatchStat.TEAMATTACKS);
			grenadesAll += stats.getStat(TDMMatchStat.GRENADES);
			airstrikesAll += stats.getStat(TDMMatchStat.AIRSTRIKES);
		}
		
		// SAVE PLAYER STATS TO DATABASE
		Paintball.instance.addAsyncTask();
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			
			@Override
			public void run() {
				boolean auto = plugin.sql.getAutoCommit();
				plugin.sql.setAutoCommit(false);
				
				for (Player player : match.getAllPlayer()) {
					String playerName = player.getName();
					PlayerStats stats = plugin.playerManager.getPlayerStats(playerName);
					stats.save();
					// if player not in lobby and not in match -> stats no longer needed:
					if (!Lobby.LOBBY.isMember(player) && getMatch(player) == null) plugin.playerManager.unloadPlayerStats(playerName);
				}
				plugin.sql.commit();
				plugin.sql.setAutoCommit(auto);
				
				//Finished. Let's go on:
				plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
					public void run() {
						//close match
						plugin.arenaManager.setNotActive(match.getArena());
						match.updateTags();
						matches.remove(match);
						//ready? countdown?
						plugin.feeder.status(Translator.getString("CHOOSE_TEAM"));

						//players:
						plugin.feeder.players();

						if (ready().equalsIgnoreCase(Translator.getString("READY"))) {
							countdown(plugin.countdown, plugin.countdownInit);
						} else {
							plugin.feeder.status(ready());
						}
						//TIME
						if (plugin.debug) {
							double delta = (System.nanoTime() - time1) / 10E6;
							DecimalFormat dec = new DecimalFormat("#.###");
							plugin.feeder.text("+Async Stats Saving: Took " + dec.format(delta) + " ms");
						}
					}
				});
				Paintball.instance.removeAsyncTask();
			}
		});
		
		//ARENA STATS
		Map<ArenaStat, Integer> arenaStats = new HashMap<ArenaStat, Integer>();
		arenaStats.put(ArenaStat.SHOTS, shotsAll);
		arenaStats.put(ArenaStat.KILLS, killsAll);
		arenaStats.put(ArenaStat.ROUNDS, 1);
		arenaStats.put(ArenaStat.GRENADES, grenadesAll);
		arenaStats.put(ArenaStat.AIRSTRIKES, airstrikesAll);
		
		plugin.arenaManager.addStats(match.getArena(), arenaStats);

		//GENERAL STATS
		Map<GeneralStat, Integer> generalStats = new HashMap<GeneralStat, Integer>();
		generalStats.put(GeneralStat.ROUNDS, 1);
		generalStats.put(GeneralStat.SHOTS, shotsAll);
		generalStats.put(GeneralStat.KILLS, killsAll);
		generalStats.put(GeneralStat.GRENADES, grenadesAll);
		generalStats.put(GeneralStat.AIRSTRIKES, airstrikesAll);
		
		plugin.statsManager.matchEndStats(generalStats, match.getAllPlayer().size());

		//messages:
		Map<String, String> vars = new HashMap<String, String>();
		plugin.feeder.text("-------------------------------------------------");
		plugin.feeder.status(Translator.getString("MATCH_IS_OVER"));
		if (draw) {
			plugin.feeder.text(Translator.getString("MATCH_DRAW"));
		} else {
			vars.put("winner_color", match.win.color().toString());
			vars.put("winner", match.win.getName());
			vars.put("winner_size", String.valueOf(match.winners.size()));
			vars.put("looser_color", match.loose.color().toString());
			vars.put("looser", match.loose.getName());
			vars.put("looser_size", String.valueOf(match.loosers.size()));
			plugin.feeder.text(Translator.getString("WINNER_TEAM", vars));

			vars.put("points", String.valueOf(plugin.pointsPerWin));
			vars.put("money", String.valueOf(plugin.cashPerWin));
			plugin.feeder.text(Translator.getString("WINNER_BONUS", vars));
		}
		
		vars.put("points", String.valueOf(plugin.pointsPerRound));
		vars.put("money", String.valueOf(plugin.cashPerRound));
		plugin.feeder.text(Translator.getString("ROUND_BONUS", vars));

		plugin.feeder.text(Translator.getString("MATCH_STATS"));
		vars.put("shots", String.valueOf(shotsAll));
		vars.put("hits", String.valueOf(hitsAll));
		vars.put("teamattacks", String.valueOf(teamattacksAll));
		vars.put("kills", String.valueOf(killsAll));
		plugin.feeder.text(Translator.getString("MATCH_SHOTS", vars));
		plugin.feeder.text(Translator.getString("MATCH_HITS", vars));
		plugin.feeder.text(Translator.getString("MATCH_TEAMATTACKS", vars));
		plugin.feeder.text(Translator.getString("MATCH_KILLS", vars));
		plugin.feeder.text("-------------------------------------------------");
		if (!draw) {
			for (final Player p : match.winners) {
				if (Lobby.LOBBY.isMember(p)) {
					plugin.feeder.status(p, Translator.getString("YOU_WON"));
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
			for (final Player p :  match.loosers) {
				if (Lobby.LOBBY.isMember(p)) {
					plugin.feeder.status(p, Translator.getString("YOU_LOST"));
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
				if (Lobby.LOBBY.isMember(p)) {
					plugin.feeder.status(p, Translator.getString("YOU_DRAW"));
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
		
		// print vault reward:
		if (plugin.vaultRewardsEnabled) {
			for (Entry<Player, String> reward : vaultRewards.entrySet()) {
				plugin.feeder.status(reward.getKey(), Translator.getString("YOU_RECEIVED_MATCH_VAULT_REWARD", new KeyValuePair("money", reward.getValue())));
			}
		}
		
		//TIME
		long time2 = System.nanoTime();
		Double delta = (time2 - time1)/10E6;
		DecimalFormat dec = new DecimalFormat("#.###");
		if (plugin.debug) plugin.feeder.text("Main-Thread: Took " + dec.format(delta) + " ms");
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
		for (Match match : matches) {
			if (match.inMatch(player)) return match;
		}
		return null;
	}
	
	/*public synchronized Match getActiveMatch(Player player) {
		for (Match match : matches) {
			if (match.isSurvivor(player)) return match;
		}
		return null;
	}*/

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
			if (!plugin.arenaManager.isReady()) return Translator.getString("NO_ARENA_READY");
			//ready=>
			return Translator.getString("READY");
		} else return Translator.getString("NOT_ENOUGH_PLAYERS");
	}

	public void countdown(int time, int initial) {
		if (countdown == null && plugin.active) {
			plugin.feeder.status(Translator.getString("NEW_MATCH_STARTS_SOON"));
			
			if (plugin.arenaVoting && voteManager.isValid()) {
				// broadcast options:
				voteManager.broadcastVoteOptions();
			}
			
			countdown = new Timer(plugin, 20 * initial, 20L, time, new Runnable() {
				
				@Override
				public void run() {
					if (plugin.useXPBar) {
						for (Player player : Lobby.LOBBY.getMembers()) {
							player.setLevel(countdown.getTime());	
						}
					}
					
					if (plugin.arenaVoting && !voteManager.isOver() && voteManager.isValid()) {
						// broadcast options again
						if (countdown.getTime() == plugin.arenaVotingBroadcastOptionsAtCountdownTime) {
							voteManager.broadcastVoteOptions();
						}
						
						// end voting
						if (countdown.getTime() == plugin.arenaVotingEndAtCountdownTime) {
							voteManager.endVoting();
							if (voteManager.didSomebodyVote()) plugin.feeder.textUntoggled(Translator.getString("GAME_VOTE_MOST_VOTES", new KeyValuePair("arena", voteManager.getHighestVotedArena())));
							else plugin.feeder.textUntoggled(Translator.getString("GAME_VOTE_NOBODY_VOTED"));
						}
						
					}
					
					if (countdown.getTime() <= 5) {
						for (Player player : Lobby.LOBBY.getMembers()) {
							player.playSound(player.getLocation(), Sound.ORB_PICKUP, 0.5F, 1.0F);	
						}
					}
				}
			}, new Runnable() {
				
				@Override
				public void run() {
					plugin.feeder.counter(countdown.getTime());
				}
			}, new Runnable() {
				
				@Override
				public void run() {
					if (plugin.useXPBar) {
						for (Player player : Lobby.LOBBY.getMembers()) {
							player.setLevel(countdown.getTime());	
						}
					}
					countdown = null;
					
					String status = ready();
					if(status.equalsIgnoreCase(Translator.getString("READY"))) {
						for (Player player : Lobby.LOBBY.getMembers()) {
							player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1.0F, 2.0F);	
						}
						
						// get next arena, but check for forced or voted arena:
						String selectedArena = plugin.arenaManager.getNextArena(voteManager);
						
						//start match
						gameStart(selectedArena);
					} else {
						for (Player player : Lobby.LOBBY.getMembers()) {
							player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1.0F, 0.0F);	
						}
						
						plugin.feeder.status(status);
					}
					
					// reset arena voteManager:
					if (plugin.arenaVoting) voteManager = new VoteManager(plugin.arenaVotingOptions, plugin.arenaVotingRandomOption);
				}
			});
		}
	}

}
