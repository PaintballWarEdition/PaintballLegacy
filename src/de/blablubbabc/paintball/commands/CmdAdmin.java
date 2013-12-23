package de.blablubbabc.paintball.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import de.blablubbabc.paintball.Lobby;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.statistics.player.PlayerStat;
import de.blablubbabc.paintball.statistics.player.PlayerStats;
import de.blablubbabc.paintball.utils.KeyValuePair;
import de.blablubbabc.paintball.utils.Translator;


public class CmdAdmin {
	private Paintball plugin;
	private int happyTaskId;
	private int happyhour;
	
	private void setHappyhour(int seconds) {
		happyhour = seconds;
	}
	private int getHappyhour() {
		return happyhour;
	}
	private void happyhourMinus() {
		happyhour--;
	}

	public CmdAdmin(Paintball pl) {
		plugin = pl;
		happyhour = 0;
		happyTaskId = -1;
	}

	public boolean command(final CommandSender sender, String[] args) {
		if(sender instanceof Player) {
			//PERMISSION CHECK
			/*if(!sender.isOp() && !sender.hasPermission("paintball.admin")) {
				sender.sendMessage(plugin.t.getString("NO_PERMISSION"));
				return true;
			}*/
			Player player = (Player) sender;
			//player commands:
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			if(args[1].equalsIgnoreCase("lobby")) {
				if(args.length == 3) {
					if(args[2].equalsIgnoreCase("spawn")) {
						plugin.addLobbySpawn(player.getLocation());
						HashMap<String, String> vars = new HashMap<String, String>();
						vars.put("lobby_color", Lobby.LOBBY.color().toString());
						player.sendMessage(Translator.getString("LOBBY_NEW_SPAWN", vars));
						return true;
					} else if(args[2].equalsIgnoreCase("remove")) {
						plugin.deleteLobbySpawns();
						HashMap<String, String> vars = new HashMap<String, String>();
						vars.put("lobby_color", Lobby.LOBBY.color().toString());
						player.sendMessage(Translator.getString("LOBBY_SPAWNS_REMOVED", vars));
						return true;
					}
				}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			} else if(args[1].equalsIgnoreCase("check")) {
				List<Entity> entities = player.getWorld().getEntities();
				player.sendMessage("Entities: " + entities.size());
				int snowballs = 0;
				for(Entity e : entities) {
					if(e.getType() == EntityType.SNOWBALL) {
						snowballs++;
					}
				}
				player.sendMessage("Snowballs: "+snowballs);
				return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			} else if (args[1].equalsIgnoreCase("play")) {
				if(args.length == 3) {
					Player p = (Player) sender;
					if(args[2].equalsIgnoreCase("defeat")) {
						plugin.musik.playDefeat(p);
						return true;
					}
					else if(args[2].equalsIgnoreCase("win")) {
						plugin.musik.playWin(p);
						return true;
					}
					else if(args[2].equalsIgnoreCase("draw")) {
						plugin.musik.playDraw(p);
						return true;
					}
					else return false;
				} else return false;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
			} else if(args[1].equalsIgnoreCase("set")) {
				if(args.length == 5) {
					String playerName = args[2];
					PlayerStats stats = plugin.playerManager.getPlayerStats(playerName);
					// stats for this player even exist ?
					if(stats != null) {
						String key = args[3];
						PlayerStat stat = PlayerStat.getFromKey(key);
						if(stat != null) {
							try {
								// get value if number, else catch exception:
								int value = Integer.parseInt(args[4]);
								// set the players stats:
								stats.setStat(stat, value);
								// calculate quotes that could have changed:
								stats.calculateQuotes();
								// save stats asynchron:
								stats.saveAsync();
								// print messages:
								Map<String,String> vars = new HashMap<String, String>();
								vars.put("player", playerName);
								vars.put("stat", key);
								vars.put("value", String.valueOf(value));
								sender.sendMessage(Translator.getString("PLAYER_STAT_SET", vars));
							} catch(Exception e) {
								sender.sendMessage(Translator.getString("INVALID_NUMBER"));
							}
						} else {
							Map<String, String> vars = new HashMap<String, String>();
							vars.put("values", PlayerStat.getKeysAsString());
							sender.sendMessage(Translator.getString("VALUE_NOT_FOUND", vars));
						}
					} else {
						Map<String, String> vars = new HashMap<String, String>();
						vars.put("player", playerName);
						sender.sendMessage(Translator.getString("PLAYER_NOT_FOUND", vars));
					}
					return true;
				}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			} else if(args[1].equalsIgnoreCase("add")) {
				if(args.length == 5) {
					String playerName = args[2];
					PlayerStats stats = plugin.playerManager.getPlayerStats(playerName);
					// stats for this player even exist ?
					if(stats != null) {
						String key = args[3];
						PlayerStat stat = PlayerStat.getFromKey(key);
						if(stat != null) {
							try {
								// get value if number, else catch exception:
								int value = Integer.parseInt(args[4]);
								// add to the players stats:
								stats.addStat(stat, value);
								// calculate quotes that could have changed:
								stats.calculateQuotes();
								// save stats asynchron:
								stats.saveAsync();
								// print messages:
								HashMap<String,String> vars = new HashMap<String, String>();
								vars.put("player", playerName);
								vars.put("stat", key);
								vars.put("value", String.valueOf(value));
								sender.sendMessage(Translator.getString("PLAYER_STAT_ADDED", vars));
							} catch(Exception e) {
								sender.sendMessage(Translator.getString("INVALID_NUMBER"));
							}
						} else {
							Map<String, String> vars = new HashMap<String, String>();
							vars.put("values", PlayerStat.getKeysAsString());
							sender.sendMessage(Translator.getString("VALUE_NOT_FOUND", vars));
						}
					} else {
						Map<String, String> vars = new HashMap<String, String>();
						vars.put("player", playerName);
						sender.sendMessage(Translator.getString("PLAYER_NOT_FOUND", vars));
					}
					return true;
				}
			}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//console_AND_player-commands:
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		if(args[1].equalsIgnoreCase("reset")) {
			if(args.length == 3 && args[2].equalsIgnoreCase("all")) {
				sender.sendMessage(Translator.getString("THIS_NEEDS_TIME"));
				Paintball.instance.addAsyncTask();
				plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
					
					@Override
					public void run() {
						long time1 = System.currentTimeMillis();
						// reset all playerdata:
						plugin.playerManager.resetAllData();
						long time2 = System.currentTimeMillis();
						long delta = time2 - time1;
						
						int amount = plugin.playerManager.getPlayerCount();
						
						Map<String, String> vars = new HashMap<String, String>();
						vars.put("time", String.valueOf(delta));
						vars.put("amount", String.valueOf(amount));
						sender.sendMessage(Translator.getString("ALL_STATS_RESET", vars));
						Paintball.instance.removeAsyncTask();
					}
				});
				return true;
			} else if(args.length == 3) {
				String playerName = args[2];
				PlayerStats stats = plugin.playerManager.getPlayerStats(playerName);
				// stats for this player even exist ?
				if(stats != null) {
					// reset the players stats:
					stats.resetStats();
					// save asynchron:
					stats.saveAsync();
					// send messages
					Map<String, String> vars = new HashMap<String, String>();
					vars.put("player", playerName);
					sender.sendMessage(Translator.getString("PLAYER_ALL_STATS_RESET", vars));
				} else {
					Map<String, String> vars = new HashMap<String, String>();
					vars.put("player", args[2]);
					sender.sendMessage(Translator.getString("PLAYER_NOT_FOUND", vars));
				}
				return true;
			} else if(args.length == 4) {
				String playerName = args[2];
				PlayerStats stats = plugin.playerManager.getPlayerStats(playerName);
				// stats for this player even exist ?
				if(stats != null) {
					String key = args[3];
					PlayerStat stat = PlayerStat.getFromKey(key);
					if(stat != null) {
						// reset the players stat:
						stats.setStat(stat, 0);
						// save asynchron:
						stats.saveAsync();
						// send messages
						Map<String, String> vars = new HashMap<String, String>();
						vars.put("player", playerName);
						vars.put("stat", key);
						sender.sendMessage(Translator.getString("PLAYER_STAT_RESET", vars));
					} else {
						Map<String, String> vars = new HashMap<String, String>();
						vars.put("values", PlayerStat.getKeysAsString());
						sender.sendMessage(Translator.getString("VALUE_NOT_FOUND", vars));
					}
				} else {
					Map<String, String> vars = new HashMap<String, String>();
					vars.put("player", playerName);
					sender.sendMessage(Translator.getString("PLAYER_NOT_FOUND", vars));
				}
				return true;
			}
			//moved add and set up
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else if(args[1].equalsIgnoreCase("stats")) {
			if(args.length == 3) {
				plugin.statsManager.sendStats(sender, args[2]);
				return true;
			}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else if(args[1].equalsIgnoreCase("rank")) {
			if (args.length >= 3) {
				String playerName = args[2];
				// stats for this player even exist ?
				if(plugin.playerManager.exists(playerName)) {
					if (args.length == 3) plugin.statsManager.sendRank(sender, playerName, PlayerStat.POINTS);
					else if (args.length == 4) plugin.statsManager.sendRank(sender, playerName, args[3]);
					else return false;
				} else {
					Map<String, String> vars = new HashMap<String, String>();
					vars.put("player", args[2]);
					sender.sendMessage(Translator.getString("PLAYER_NOT_FOUND", vars));
				}
				return true;
			}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else if(args[1].equalsIgnoreCase("next")) {
			if (args.length == 3) {
				String arena = args[2];
				if (!plugin.arenaManager.existing(arena)) {
					Map<String, String> vars = new HashMap<String, String>();
					vars.put("arena", arena);
					sender.sendMessage(Translator.getString("ARENA_NOT_FOUND", vars));
					return true;
				}
				if (!plugin.arenaManager.isReady(arena)) {
					sender.sendMessage(Translator.getString("ARENA_NOT_READY"));
					return true;
				}
				plugin.arenaManager.setNext(arena);
				plugin.feeder.text(sender, Translator.getString("NEXT_ARENA_SET", new KeyValuePair("arena", arena)));
				return true;
			}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else if(args[1].equalsIgnoreCase("disable")) {
			String status = "";
			if (plugin.active) {
				plugin.active = false;
				status = Translator.getString("OFF");
			} else {
				plugin.active = true;
				status = Translator.getString("ON");
			}
			Map<String, String> vars = new HashMap<String, String>();
			vars.put("status", status);
			sender.sendMessage(Translator.getString("PLUGIN_STATUS", vars));
			return true;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else if(args[1].equalsIgnoreCase("reload")) {
			//neue matches verhindern
			plugin.active = false;
			plugin.reload(sender);
			return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else if(args[1].equalsIgnoreCase("softreload")) {
			//neue matches verhindern
			plugin.active = false;
			plugin.softreload = true;
			//message:
			plugin.feeder.status(Translator.getString("SOFTRELOAD"));
			//check
			if (plugin.matchManager.softCheck()) plugin.reload(sender);
			else sender.sendMessage(Translator.getString("RELOAD_SOON"));
			return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else if(args[1].equalsIgnoreCase("random")) {
			//Toggle only random:
			if(plugin.onlyRandom) {
				plugin.onlyRandom = false;
				sender.sendMessage(Translator.getString("ONLY_RANDOM_OFF"));
			} else {
				plugin.onlyRandom = true;
				sender.sendMessage(Translator.getString("ONLY_RANDOM_ON"));
			}
			return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else if (args[1].equalsIgnoreCase("happy")) {
			if(args.length == 3) {
				try {
					int seconds = Integer.parseInt(args[2]);
					if(seconds < 0) seconds = 0;
					// Set happyhour:
					plugin.happyhour = true;
					setHappyhour(seconds);
					//sender nicht in der lobby?
					if(!(sender instanceof Player) || Lobby.getTeam((Player) sender) == null) {
						sender.sendMessage(plugin.feeder.happyhour(seconds));
					}
					plugin.feeder.textUntoggled(plugin.feeder.happyhour(seconds));
					if(happyTaskId == -1) happyTask();
				} catch(Exception e) {	
					sender.sendMessage(Translator.getString("INVALID_NUMBER"));
				}
				return true;
			}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else if (args[1].equalsIgnoreCase("gifts") || args[1].equalsIgnoreCase("gift")) {
			if (args.length == 2 || args.length == 3) {
				int amount = 1;
				if(args.length == 3) {
					try {
						amount = Integer.parseInt(args[2]);
						if (amount < 0) amount = 0;
					} catch (Exception e) {
						sender.sendMessage(Translator.getString("INVALID_NUMBER"));
					}
				}
				
				if (amount > 0) {
					//sender nicht in der lobby?
					if(!(sender instanceof Player) || !Lobby.isPlaying((Player)sender)) {
						sender.sendMessage(Translator.getString("YOU_GAVE_ALL_GIFT"));
					}
					
					for(Player p : Lobby.LOBBY.getMembers()) {
						if(Lobby.isPlaying(p)) {
							plugin.weaponManager.getGiftManager().giveGift(p, amount, true);
						}
					}	
				} else {
					sender.sendMessage(Translator.getString("INVALID_NUMBER"));
				}
				
				return true;
			}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else {
			if(sender instanceof Player) return false;
			else sender.sendMessage(Translator.getString("COMMAND_UNKNOWN_OR_NOT_CONSOLE"));
			return true;
		}
		return false;
	}
	
	private void happyTask() {
		happyTaskId = plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
			
			@Override
			public void run() {
				happyhourMinus();
				int happy = getHappyhour();
				if( happy < 1) {
					setHappyhour(0);
					plugin.happyhour = false;
					happyTaskId = -1;
					plugin.feeder.textUntoggled(Translator.getString("HAPPYHOUR_END"));
					return;
				}
				if(( happy % 10 ) == 0)
				{
					plugin.feeder.text(plugin.feeder.happyhour(happy));
				}
				
				if( happy < 6 && happy > 0)
				{
					//if below 6 message here (regardless of divisibility)
					plugin.feeder.text(plugin.feeder.happyhour(happy));
				}
				//start again:
				happyTask();
				
			}
		}, 20L).getTaskId();	
	}
}