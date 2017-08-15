/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import de.blablubbabc.paintball.joindelay.JoinWaitRunnable;
import de.blablubbabc.paintball.joindelay.WaitTimer;
import de.blablubbabc.paintball.statistics.player.PlayerStat;
import de.blablubbabc.paintball.statistics.player.PlayerStats;
import de.blablubbabc.paintball.utils.Callback;
import de.blablubbabc.paintball.utils.KeyValuePair;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class PlayerManager {

	private Map<UUID, PlayerDataStore> playerStore;
	private Map<UUID, PlayerStats> playerStats;
	private Set<UUID> playersToAdd;

	public PlayerManager() {
		playerStore = new HashMap<UUID, PlayerDataStore>();
		playerStats = new HashMap<UUID, PlayerStats>();
		playersToAdd = new HashSet<UUID>();

		Paintball.getInstance().getServer().getScheduler().runTaskLater(Paintball.getInstance(), new Runnable() {

			@Override
			public void run() {
				initAllOnlinePlayers();
			}
		}, 1L);
	}

	// LOBBY JOINING

	public void joinTeam(final Player player, boolean withDelay, final Lobby team) {
		if (!Lobby.LOBBY.isMember(player)) {
			joinLobbyPre(player, withDelay, new Runnable() {

				@Override
				public void run() {
					handleJoinTeam(player, team);
				}
			});
		} else {
			handleJoinTeam(player, team);
		}

	}

	public void joinLobbyPre(Player player, boolean withDelay, Runnable runOnSuccess) {
		if (joinLobbyPreChecks(player)) joinLobbyFresh(player, withDelay, runOnSuccess);
	}

	private void handleJoinTeam(Player player, Lobby team) {
		if (Lobby.isPlaying(player) || Lobby.isSpectating(player)) {
			player.sendMessage(Translator.getString("CANNOT_CHANGE_TEAM_PLAYING"));
			return;
		}

		boolean rb = false;
		boolean spec = false;
		if (team == Lobby.RED || team == Lobby.BLUE) rb = true;
		else if (team == Lobby.SPECTATE) spec = true;

		// Max Players Check:
		if (!spec) {
			if (!Lobby.inTeam(player) || Lobby.SPECTATE.isMember(player)) {
				int players = Lobby.RED.number() + Lobby.BLUE.number() + Lobby.RANDOM.number();
				if (players >= Paintball.getInstance().maxPlayers) {
					player.sendMessage(Translator.getString("CANNOT_JOIN_TEAM_FULL"));
					return;
				}
			}
			if (rb && Paintball.getInstance().onlyRandom) {
				player.sendMessage(Translator.getString("ONLY_RANDOM"));
				if (!Paintball.getInstance().autoRandom)
													return;
			}
		}
		if (Lobby.inTeam(player) || Lobby.SPECTATE.isMember(player)) {
			Lobby.getTeam(player).removeMember(player);
			player.sendMessage(Translator.getString("YOU_LEFT_CURRENT_TEAM"));
		}
		// only random + auto random
		if (rb && Paintball.getInstance().onlyRandom && Paintball.getInstance().autoRandom) {
			Lobby.RANDOM.addMember(player);
			Map<String, String> vars = new HashMap<String, String>();
			vars.put("color_random", Lobby.RANDOM.color().toString());
			player.sendMessage(Translator.getString("AUTO_JOIN_RANDOM", vars));
		} else {
			team.addMember(player);
			Map<String, String> vars = new HashMap<String, String>();
			vars.put("color_team", team.color().toString());
			vars.put("team", team.getName());
			if (rb) player.sendMessage(Translator.getString("YOU_JOINED_TEAM", vars));
			else if (team.equals(Lobby.RANDOM)) player.sendMessage(Translator.getString("YOU_JOINED_RANDOM", vars));
			else if (spec) player.sendMessage(Translator.getString("YOU_JOINED_SPECTATORS", vars));
		}
		if (!spec) {
			String ready = Paintball.getInstance().matchManager.ready();
			if (ready.equalsIgnoreCase(Translator.getString("READY"))) {
				Paintball.getInstance().matchManager.countdown(Paintball.getInstance().countdown, Paintball.getInstance().countdownInit);
			} else {
				Paintball.getInstance().feeder.status(player, ready);
			}
		}
		// players:
		Paintball.getInstance().feeder.players(player);
	}

	private boolean joinLobbyPreChecks(Player player) {
		// Lobby vorhanden?
		if (Paintball.getInstance().getLobbyspawnsCount() == 0) {
			player.sendMessage(Translator.getString("NO_LOBBY_FOUND"));
			return false;
		}
		// inventory
		if (!Utils.isEmptyInventory(player) && Paintball.getInstance().checkInventory) {
			player.sendMessage(Translator.getString("NEED_CLEAR_INVENTORY"));
			return false;
		}
		// gamemode an?
		if (!player.getGameMode().equals(GameMode.SURVIVAL) && Paintball.getInstance().checkGamemode) {
			player.sendMessage(Translator.getString("NEED_RIGHT_GAMEMODE"));
			return false;
		}
		// flymode an? (built-in fly mode)
		if ((player.getAllowFlight() || player.isFlying()) && Paintball.getInstance().checkFlymode) {
			player.sendMessage(Translator.getString("NEED_STOP_FLYING"));
			return false;
		}
		// brennt? f�llt? taucht?
		if ((player.getFireTicks() > 0 || player.getFallDistance() > 0 || player.getRemainingAir() < player.getMaximumAir()) && Paintball.getInstance().checkBurning) {
			player.sendMessage(Translator.getString("NEED_STOP_FALLING_BURNING_DROWNING"));
			return false;
		}
		// wenig leben
		if (player.getHealth() < player.getMaxHealth() && Paintball.getInstance().checkHealth) {
			player.sendMessage(Translator.getString("NEED_FULL_HEALTH"));
			return false;
		}
		// hungert
		if (player.getFoodLevel() < 20 && Paintball.getInstance().checkFood) {
			player.sendMessage(Translator.getString("NEED_FULL_FOOD"));
			return false;
		}
		// hat effekte auf sich
		if (player.getActivePotionEffects().size() > 0 && Paintball.getInstance().checkEffects) {
			player.sendMessage(Translator.getString("NEED_NO_EFFECTS"));
			return false;
		}

		// check, if player-adding is yet finished:
		if (Paintball.getInstance().playerManager.isPlayerStillLocked(player.getUniqueId())) {
			player.sendMessage(Translator.getString("NEED_BE_ADDED_TO_DATABASE_FIRST"));
			return false;
		}

		return true;
	}

	private Map<UUID, Scoreboard> lobbyScoreboards = new HashMap<UUID, Scoreboard>();

	private Map<UUID, WaitTimer> currentlyWaiting = new HashMap<UUID, WaitTimer>();
	private List<UUID> currentlyLoading = new ArrayList<UUID>();

	private void joinLobbyFresh(final Player player, boolean withDelay, final Runnable runOnSuccess) {
		final UUID playerUUID = player.getUniqueId();

		// join delay:
		if (withDelay && Paintball.getInstance().joinDelaySeconds > 0) {
			// is the player already waiting for join or waiting for stats loading -> ignore:
			WaitTimer waitTimer = currentlyWaiting.get(playerUUID);
			if (waitTimer == null && !currentlyLoading.contains(playerUUID)) {
				// let the player know:
				player.sendMessage(Translator.getString("DO_NOT_MOVE", new KeyValuePair("seconds", String.valueOf(Paintball.getInstance().joinDelaySeconds))));
				// wait:
				joinLater(player, new Runnable() {

					@Override
					public void run() {
						// waiting is over:
						currentlyWaiting.remove(playerUUID);
						// load and then join:
						handleLoadingAndJoin(player, runOnSuccess);
					}
				});

			}
		} else {
			// load and then join:
			handleLoadingAndJoin(player, runOnSuccess);
		}
	}

	private void handleLoadingAndJoin(final Player player, final Runnable runOnSuccess) {
		final UUID playerUUID = player.getUniqueId();
		// is player already in the process of joining ?
		if (!currentlyLoading.contains(playerUUID)) {
			// load player stats and continue after loading:
			currentlyLoading.add(playerUUID);
			loadPlayerStatsAsync(playerUUID, new Runnable() {

				@Override
				public void run() {
					// loading is finished:
					currentlyLoading.remove(playerUUID);

					// did the player leave in the mean time?
					if (player.isOnline()) {
						// join lobby:
						Lobby.LOBBY.addMember(player);
						Paintball.getInstance().feeder.join(player.getName());
						if (Paintball.getInstance().worldMode) storeClearPlayer(player, Paintball.getInstance().getNextLobbySpawn());
						else teleportStoreClearPlayer(player, Paintball.getInstance().getNextLobbySpawn());
						// ASSIGN RANK
						if (Paintball.getInstance().ranksLobbyArmor) Paintball.getInstance().rankManager.getRank(playerUUID).assignArmorToPlayer(player);
						// ASSIGN SCOREBOARD (after teleport)
						if (Paintball.getInstance().scoreboardLobby) {
							initLobbyScoreboard(player);
						}

						// continue afterwards:
						if (runOnSuccess != null) runOnSuccess.run();
					} else {
						// unload playerStats again..
						unloadPlayerStats(playerUUID);
					}
				}
			});
		}
	}

	private void initLobbyScoreboard(Player player) {
		UUID playerUUID = player.getUniqueId();
		Scoreboard lobbyBoard = lobbyScoreboards.get(playerUUID);
		if (lobbyBoard == null) {
			lobbyBoard = Bukkit.getScoreboardManager().getNewScoreboard();
			lobbyScoreboards.put(playerUUID, lobbyBoard);

			String header = Translator.getString("SCOREBOARD_LOBBY_HEADER");
			Objective objective = lobbyBoard.registerNewObjective(header.length() > 16 ? header.substring(0, 16) : header, "dummy");
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		}

		updateLobbyScoreboard(playerUUID);
		player.setScoreboard(lobbyBoard);
	}

	public void updateLobbyScoreboard(UUID playerUUID) {
		Scoreboard lobbyBoard = lobbyScoreboards.get(playerUUID);
		if (lobbyBoard != null) {
			Objective objective = lobbyBoard.getObjective(DisplaySlot.SIDEBAR);
			if (objective == null) return;
			PlayerStats stats = getPlayerStats(playerUUID);
			for (PlayerStat stat : PlayerStat.values()) {
				// skip airstrikes and grenades count:
				if (stat == PlayerStat.AIRSTRIKES || stat == PlayerStat.GRENADES) continue;
				String scoreName = Translator.getString("SCOREBOARD_LOBBY_" + stat.getKey().toUpperCase());
				Score score = objective.getScore(scoreName.length() > 16 ? scoreName.substring(0, 16) : scoreName);
				score.setScore(stats.getStat(stat));
			}
		}
	}

	public void abortingJoinWaiting(Player player) {
		WaitTimer waitTimer = currentlyWaiting.remove(player.getUniqueId());
		if (waitTimer != null) {
			// abort and end Timer:
			player.sendMessage(Translator.getString("JOINING_ABORTED"));
			waitTimer.onAbort();
		}
	}

	private void joinLater(final Player player, final Runnable runAfterWaiting) {
		final JoinWaitRunnable waitRunnable = new JoinWaitRunnable(runAfterWaiting, player.getLocation());
		WaitTimer waitTimer = new WaitTimer(Paintball.getInstance(), player, 0L, 20L, Paintball.getInstance().joinDelaySeconds, waitRunnable);
		currentlyWaiting.put(player.getUniqueId(), waitTimer);
	}

	public synchronized void enterLobby(Player player) {
		PlayerDataStore.clearPlayer(player, true, true);
		// set waiting:
		if (Lobby.isPlaying(player) || Lobby.isSpectating(player)) Lobby.getTeam(player).setWaiting(player);
		// teleport to lobby:
		player.teleport(Paintball.getInstance().getNextLobbySpawn());
		// assign rank armor:
		if (Paintball.getInstance().ranksLobbyArmor) Paintball.getInstance().rankManager.getRank(player.getUniqueId()).assignArmorToPlayer(player);
		// assign lobby scoreboard:
		if (Paintball.getInstance().scoreboardLobby) {
			initLobbyScoreboard(player);
		}
	}

	public synchronized boolean leaveLobby(Player player, boolean messages) {
		UUID playerUUID = player.getUniqueId();
		if (Lobby.LOBBY.isMember(player)) {
			if (Lobby.isPlaying(player) || Lobby.isSpectating(player)) {
				Paintball.getInstance().matchManager.getMatch(player).left(player);
			}

			// lobby remove:
			Lobby.remove(player);

			// undo arena voting:
			if (Paintball.getInstance().arenaVoting) Paintball.getInstance().matchManager.onLobbyLeave(player);

			// restore and teleport back:
			if (Paintball.getInstance().worldMode) {
				clearRestorePlayer(player);
			} else {
				clearRestoreTeleportPlayer(player);
			}

			// if player not in lobby and not in match -> stats no longer needed:
			if (!Lobby.LOBBY.isMember(player) && Paintball.getInstance().matchManager.getMatch(player) == null) unloadPlayerStats(playerUUID);

			// remove scoreboard for this player
			if (Paintball.getInstance().scoreboardLobby) {
				lobbyScoreboards.remove(playerUUID);
			}

			// messages:
			if (messages) {
				Paintball.getInstance().feeder.leave(player.getName());
				player.sendMessage(Translator.getString("YOU_LEFT_LOBBY"));
			}

			// vault rewards after session: -> now given directly after match end
			/*
			 * if (vaultRewardsEnabled && vaultRewardsFeature != null) {
			 * double reward = vaultRewardsFeature.getSessionMoney(playerName);
			 * if (vaultRewardsFeature.transferCurrentSession(playerName)) player.sendMessage(Translator.getString("YOU_RECEIVED_SESSION_VAULT_REWARD", new KeyValuePair("money",
			 * String.valueOf(reward))));
			 * }
			 */

			return true;
		}

		return false;
	}

	// ////////////////////////////////////////////////////////////

	private boolean isPlayerStillLocked(UUID playerUUID) {
		return playersToAdd.contains(playerUUID);
	}

	// PLAYERSTATS

	public void loadPlayerStatsAsync(final UUID playerUUID, final Runnable runAfterwards) {
		if (playerStats.get(playerUUID) == null) {
			Paintball.getInstance().addAsyncTask();
			Paintball.getInstance().getServer().getScheduler().runTaskAsynchronously(Paintball.getInstance(), new Runnable() {

				@Override
				public void run() {
					playerStats.put(playerUUID, new PlayerStats(playerUUID));
					if (runAfterwards != null) {
						// run afterwards-task sync:
						Paintball.getInstance().getServer().getScheduler().runTask(Paintball.getInstance(), runAfterwards);
					}
					Paintball.getInstance().removeAsyncTask();
				}
			});
		} else {
			// run afterwards-task sync:
			runAfterwards.run();
		}
	}

	// is done sync if Paintball is currently disabling, else async
	public void unloadPlayerStats(UUID playerUUID) {
		PlayerStats stats = playerStats.remove(playerUUID);
		if (stats != null) {
			if (!Paintball.getInstance().currentlyDisabling) stats.saveAsync();
			else stats.save();
		}
	}

	public PlayerStats getPlayerStats(UUID playerUUID) {
		if (playerUUID == null) return null;
		PlayerStats stats = playerStats.get(playerUUID);
		if (stats == null) {
			// check if in cache, if not return temporary retrieved PlayerStats or null, if no stats exist (yet) for
			// this player:
			if (!isPlayerStillLocked(playerUUID)) {
				if (exists(playerUUID)) stats = new PlayerStats(playerUUID);
			} // else: return null
		}
		return stats;
	}

	// METHODS
	// SETTER
	// adding all players on plugin start is done sync, because it reduces possible problems and it's a one-time-thing
	// on plugin start only:
	private void initAllOnlinePlayers() {
		// locking is not needed, because it's done sync:
		for (Player player : Paintball.getInstance().getServer().getOnlinePlayers()) {
			// add player to database
			initPlayer(player);

			// autoLobby and worldMode
			if (Paintball.getInstance().autoLobby || (Paintball.getInstance().worldMode && Paintball.getInstance().worldModeWorlds.contains(player.getWorld().getName()))) {
				if (Paintball.getInstance().autoTeam) {
					joinTeam(player, false, Lobby.RANDOM);
				} else {
					joinLobbyPre(player, false, null);
				}
			}
		}
	}

	public void initPlayerAsync(final Player player) {
		final UUID playerUUID = player.getUniqueId();
		// lock player
		playersToAdd.add(playerUUID);

		Paintball.getInstance().addAsyncTask();
		Paintball.getInstance().getServer().getScheduler().runTaskAsynchronously(Paintball.getInstance(), new Runnable() {

			@Override
			public void run() {
				initPlayer(player);

				Paintball.getInstance().getServer().getScheduler().runTask(Paintball.getInstance(), new Runnable() {

					@Override
					public void run() {
						// Remove player lock in sync environment:
						playersToAdd.remove(playerUUID);

						// autoLobby
						Paintball.getInstance().getServer().getScheduler().runTaskLater(Paintball.getInstance(), new Runnable() {

							@Override
							public void run() {
								Player player = Paintball.getInstance().getServer().getPlayer(playerUUID);
								if (player != null) {
									// autoLobby and worldMode
									if (Paintball.getInstance().autoLobby || (Paintball.getInstance().worldMode && Paintball.getInstance().worldModeWorlds.contains(player.getWorld().getName()))) {
										if (Paintball.getInstance().autoTeam) {
											joinTeam(player, false, Lobby.RANDOM);
										} else {
											joinLobbyPre(player, false, null);
										}
									}
								} else {
									// do nothing
								}
							}
						}, 1L);
					}
				});
				Paintball.getInstance().removeAsyncTask();
			}
		});
	}

	/**
	 * This method should only be used inside this class and surrounded by locking the player by adding and removing it
	 * from the playersToAdd list.
	 * 
	 * @param player
	 */
	private void initPlayer(Player player) {
		Paintball.getInstance().sql.sqlPlayers.initPlayer(player.getUniqueId(), player.getName());
	}

	public void resetAllDataAsync() {
		Paintball.getInstance().addAsyncTask();
		Paintball.getInstance().getServer().getScheduler().runTaskAsynchronously(Paintball.getInstance(), new Runnable() {

			@Override
			public void run() {
				resetAllData();
				Paintball.getInstance().removeAsyncTask();
			}
		});
	}

	public void resetAllData() {
		// reset stats in cache:
		for (PlayerStats stats : playerStats.values()) {
			stats.resetStats();
		}
		// reset in databse:
		Paintball.getInstance().sql.sqlPlayers.resetAllPlayerStats();
	}

	/*
	 * public void resetDataOfPlayerAsync(final String player) {
	 * Paintball.instance.getServer().getScheduler()
	 * .runTaskAsynchronously(Paintball.instance, new Runnable() {
	 * @Override
	 * public void run() {
	 * Paintball.instance.sql.sqlPlayers.resetPlayerStats(player);
	 * }
	 * });
	 * }
	 */

	public boolean exists(UUID playerUUID) {
		if (isPlayerStillLocked(playerUUID)) return true;
		return Paintball.getInstance().sql.sqlPlayers.isPlayerExisting(playerUUID);
	}

	// STATS
	/*
	 * public void addStatsAsync(final String player, final Map<PlayerStat, Integer> stats) {
	 * Paintball.instance.getServer().getScheduler()
	 * .runTaskAsynchronously(Paintball.instance, new Runnable() {
	 * @Override
	 * public void run() {
	 * addStats(player, stats);
	 * }
	 * });
	 * }
	 * public void addStats(final String player, final Map<PlayerStat, Integer> stats) {
	 * Paintball.instance.sql.sqlPlayers.addPlayerStats(player, stats);
	 * Paintball.instance.sql.sqlPlayers.calculateStats(player);
	 * }
	 * public void setStatsAsync(final String player, final Map<PlayerStat, Integer> stats) {
	 * Paintball.instance.getServer().getScheduler()
	 * .runTaskAsynchronously(Paintball.instance, new Runnable() {
	 * @Override
	 * public void run() {
	 * setStats(player, stats);
	 * }
	 * });
	 * }
	 * public void setStats(final String player, final Map<PlayerStat, Integer> stats) {
	 * Paintball.instance.sql.sqlPlayers.setPlayerStats(player, stats);
	 * Paintball.instance.sql.sqlPlayers.calculateStats(player);
	 * }
	 */

	// GETTER

	public int getPlayersEverPlayedCount() {
		return Paintball.getInstance().sql.sqlPlayers.getPlayersEverPlayedCount();
	}

	public int getPlayerCount() {
		return Paintball.getInstance().sql.sqlPlayers.getPlayerCount();
	}

	/*
	 * public Map<PlayerStat, Integer> getStats(String player) {
	 * return Paintball.instance.sql.sqlPlayers.getPlayerStats(player);
	 * }
	 */

	public void teleportStoreClearPlayer(Player player, Location to) {
		playerStore.put(player.getUniqueId(), new PlayerDataStore(player, to));
	}

	public void storeClearPlayer(Player player, Location to) {
		playerStore.put(player.getUniqueId(), new PlayerDataStore(player));
	}

	public void clearRestoreTeleportPlayer(Player player) {
		PlayerDataStore playerData = playerStore.remove(player.getUniqueId());
		if (playerData != null) {
			playerData.restoreTeleportPlayer(player, false);
		}
	}

	public void clearRestorePlayer(Player player) {
		PlayerDataStore playerData = playerStore.remove(player.getUniqueId());
		if (playerData != null) {
			playerData.restoreTeleportPlayer(player, true);
		}
	}

	// this will run async and call the provided callback when done
	public static void lookupPlayerUUIDForName(final String playerName, final Callback<UUID> runWhenDone) {
		assert playerName != null;
		assert runWhenDone != null;
		assert Paintball.getInstance() != null;

		// fast check for online players:
		Player player = Bukkit.getPlayerExact(playerName);
		if (player != null) {
			runWhenDone.setResult(player.getUniqueId()).run();
			return;
		}

		Paintball.getInstance().addAsyncTask();
		Bukkit.getScheduler().runTaskAsynchronously(Paintball.getInstance(), new Runnable() {

			@Override
			public void run() {
				UUID uuid = getPlayerUUID(playerName);
				Paintball.getInstance().removeAsyncTask();
				Bukkit.getScheduler().runTask(Paintball.getInstance(), runWhenDone.setResult(uuid));
			}
		});
	}

	/**
	 * Might be slow! Run async if possible.
	 * 
	 * @param playerName
	 *            the player name
	 * @return the uuid, or null
	 */
	public static UUID getPlayerUUID(String playerName) {
		final OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(playerName);
		return offlinePlayer.getUniqueId();
	}
}
