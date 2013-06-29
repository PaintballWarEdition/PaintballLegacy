package de.blablubbabc.paintball.commands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.blablubbabc.paintball.Lobby;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.statistics.player.PlayerStat;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class CommandManager implements CommandExecutor{
	private Paintball plugin;
	public CmdArena cmdArena;
	public CmdAdmin cmdAdmin;
	public CmdShop cmdShop;
	private String blablubbabc;


	public CommandManager(Paintball pl) {
		plugin = pl;
		blablubbabc = ChatColor.AQUA + "" + ChatColor.BOLD + "[ " + ChatColor.GOLD + "" + ChatColor.ITALIC + "" + ChatColor.BOLD + "Paintball by blablubbabc" + ChatColor.RESET + ChatColor.AQUA + "" + ChatColor.BOLD + " ]";
		cmdArena = new CmdArena(plugin, plugin.arenaManager);
		cmdAdmin = new CmdAdmin(plugin);
		cmdShop = new CmdShop(plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("pb")) {
			if (args.length == 0) {
				pbhelp(sender);
				return true;
			} else if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
				pbhelp(sender);
				return true;
			} else if (args[0].equalsIgnoreCase("info")) {
				pbinfo(sender);
				return true;
			} else {

				//PERMISSION CHECK
				///pb, pb help, and pb info is allowed for anyone.
				if (!hasGeneralPerm(sender)) {
					sender.sendMessage(Translator.getString("NO_PERMISSION"));
					return true;
				}

				if (args[0].equalsIgnoreCase("arena")) {
					//if(!sender.isOp() && !sender.hasPermission("paintball.arena")) {
					if (!sender.isOp() && !sender.hasPermission("paintball.admin")) {
						sender.sendMessage(Translator.getString("NO_PERMISSION"));
						return true;
					}
					if (args.length == 1) {
						arenahelp(sender);
						return true;
					} else {
						//executor:
						return cmdArena.command(sender, args);
					}
				} else if (args[0].equalsIgnoreCase("admin")) {
					if (!sender.isOp() && !sender.hasPermission("paintball.admin")) {
						sender.sendMessage(Translator.getString("NO_PERMISSION"));
						return true;
					}
					if (args.length == 1) {
						adminhelp(sender);
						return true;
					} else {
						//executor:
						return cmdAdmin.command(sender, args);
					}
				} else if (sender instanceof Player) {
					Player player = (Player) sender;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					if (args[0].equalsIgnoreCase("lobby")) {
						if (Lobby.LOBBY.isMember(player)) {
							if (Lobby.isPlaying(player)) {
								player.sendMessage(Translator.getString("CANNOT_JOIN_LOBBY_PLAYING"));
								return true;
							}
							/*if(Lobby.isSpectating(player)) {
							//Lobbyteleport
							player.teleport(plugin.transformLocation(plugin.getLobbySpawns().get(0)));
							Lobby.SPECTATE.setWaiting(player);
							player.getInventory().clear();
							player.getInventory().setHelmet(null);
							player.sendMessage(plugin.green + "You enteplugin.red the lobby!");
							return true;
						}*/
							player.sendMessage(Translator.getString("ALREADY_IN_LOBBY"));
							return true;
						} else {
							joinLobbyPre(player, null);
							return true;
						}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////			
					} else if (args[0].equalsIgnoreCase("blue")) {
						joinTeam(player, Lobby.BLUE);
						return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					} else if (args[0].equalsIgnoreCase("red")) {
						joinTeam(player, Lobby.RED);
						return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					} else if (args[0].equalsIgnoreCase("random") || args[0].equalsIgnoreCase("join")) {
						joinTeam(player, Lobby.RANDOM);
						return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					} else if (args[0].equalsIgnoreCase("spec")) {
						joinTeam(player, Lobby.SPECTATE);
						return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					} else if (args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("exit") || args[0].equalsIgnoreCase("quit")) {
						if (args.length == 2 && args[1].equalsIgnoreCase("team")) {
							if(!Lobby.LOBBY.isMember(player) || !(Lobby.inTeam(player) || Lobby.SPECTATE.isMember(player))) {
								player.sendMessage(Translator.getString("BE_IN_NO_TEAM"));
								return true;
							}
							/*if(Lobby.isPlaying(player) || Lobby.isSpectating(player)) {
								player.sendMessage(plugin.t.getString("CANNOT_LEAVE_LOBBY_PLAYING"));
								return true;
							}*/
							if (Lobby.isPlaying(player) || Lobby.isSpectating(player)) {
								plugin.matchManager.getMatch(player).left(player);
								plugin.joinLobby(player);
							}
							Lobby.getTeam(player).removeMember(player);
							player.sendMessage(Translator.getString("YOU_LEFT_TEAM"));
						} else {
							if(!Lobby.LOBBY.isMember(player)) {
								player.sendMessage(Translator.getString("NOT_IN_LOBBY"));
								return true;
							}
							/*if(Lobby.inTeam(player) || Lobby.SPECTATE.isMember(player)) {
								if(Lobby.isPlaying(player) || Lobby.isSpectating(player)) {
									player.sendMessage(plugin.t.getString("CANNOT_LEAVE_LOBBY_PLAYING"));
									return true;
								}
								Lobby.getTeam(player).removeMember(player);
								player.sendMessage(plugin.t.getString("YOU_LEFT_TEAM"));
								return true;
							} else if(plugin.autoLobby && !player.hasPermission("paintball.admin")) {
								player.sendMessage(plugin.t.getString("CANNOT_LEAVE_LOBBY"));
								return true;
							}*/
							if (plugin.autoLobby && !player.hasPermission("paintball.admin")) {
								player.sendMessage(Translator.getString("CANNOT_LEAVE_LOBBY"));
							} else {
								plugin.leaveLobby(player, true);
							}
						}
						return true;
						
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					} else if (args[0].equalsIgnoreCase("toggle")) {
						if (!Lobby.LOBBY.isMember(player)) {
							player.sendMessage(Translator.getString("NOT_IN_LOBBY"));
							return true;
						}
						Lobby.toggleFeed(player);
						player.sendMessage(Translator.getString("TOGGLED_FEED"));
						return true;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					} else if (args[0].equalsIgnoreCase("rank")) {
						if (args.length == 1) plugin.statsManager.sendRank(player, player.getName(), PlayerStat.POINTS);
						else plugin.statsManager.sendRank(player, player.getName(), args[1]);
						return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					} else if (args[0].equalsIgnoreCase("stats")) {
						plugin.statsManager.sendStats(player, player.getName());
						return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					} else if (args[0].equalsIgnoreCase("cash")) {
						plugin.statsManager.sendCash(player, player.getName());
						return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					} else if (args[0].equalsIgnoreCase("shop")) {
						//executor
						return cmdShop.command(sender, args, false);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					}
				}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				//CONSOLE AND PLAYER
				if (args[0].equalsIgnoreCase("top")) {
					if(args.length == 1) plugin.statsManager.sendTop(sender, PlayerStat.POINTS);
					else plugin.statsManager.sendTop(sender, args[1]);
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if (args[0].equalsIgnoreCase("list")) {
					sender.sendMessage(Translator.getString("PLAYER_OVERVIEW"));
					for (Lobby l : Lobby.values()) {
						sender.sendMessage(l.color() + l.name() + " ( " + l.number() + " ):");
						for (Player p : l.getMembers()) {
							if (l != Lobby.LOBBY) sender.sendMessage(ChatColor.GRAY + p.getName() + ChatColor.WHITE + " : " + (Lobby.isPlaying(p) ? ((l == Lobby.SPECTATE) ? Translator.getString("SPECTATING") : Translator.getString("PLAYING")) : Translator.getString("WAITING")));
							if (l == Lobby.LOBBY && Lobby.getTeam(p) == Lobby.LOBBY) sender.sendMessage(ChatColor.GRAY + p.getName() + ChatColor.WHITE + " : " + Translator.getString("NOT_IN_TEAM"));
						}
					}
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else {
					if (sender instanceof Player) return false;
					else sender.sendMessage(Translator.getString("COMMAND_UNKNOWN_OR_NOT_CONSOLE"));
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean hasGeneralPerm(CommandSender sender) {
		return plugin.noPerms || sender.hasPermission("paintball.general") || sender.isOp() || sender.hasPermission("paintball.admin");
	}
	
	public void pbinfo(CommandSender sender) {
		sender.sendMessage(blablubbabc);
		sender.sendMessage(ChatColor.DARK_GREEN + "Permission: " + ChatColor.GOLD + (hasGeneralPerm(sender) ? Translator.getString("ALLOWED_TO_PLAY_PAINTBALL") : Translator.getString("NOT_ALLOWED_TO_PLAY_PAINTBALL")));
		sender.sendMessage(ChatColor.DARK_GREEN + "Version: " + ChatColor.GOLD + plugin.getDescription().getVersion());
		sender.sendMessage(ChatColor.DARK_GREEN + "Website: " + ChatColor.GOLD + "dev.bukkit.org/server-mods/paintball_pure_war/");
		sender.sendMessage(ChatColor.DARK_RED + "Basic license hints: ");
		sender.sendMessage(ChatColor.RED + "* Commercial usage of this plugin in any kind is not allowed.");
		sender.sendMessage(ChatColor.RED + "* Example: No benefits for payed ranks/vip and donors.");
		sender.sendMessage(ChatColor.RED + "* Modifying code is not allowed.");
		sender.sendMessage(ChatColor.GOLD + "You can find a complete list of usage condition on the bukkit dev page.");
	}
	
	public void pbhelp(CommandSender sender) {
		sender.sendMessage(blablubbabc);
		sender.sendMessage(Translator.getString("COMMAND_GENERAL_HELP"));
		String info = Translator.getString("COMMAND_GENERAL_INFO");
		if(!ChatColor.stripColor(info).contains("info")) info = "&c/pb info &b- Showing information about paintball plugin.";
		sender.sendMessage(info);
		sender.sendMessage(Translator.getString("COMMAND_GENERAL_ARENA"));
		sender.sendMessage(Translator.getString("COMMAND_GENERAL_ADMIN"));
		sender.sendMessage(Translator.getString("COMMAND_GENERAL_LIST"));
		sender.sendMessage(Translator.getString("COMMAND_GENERAL_LOBBY"));

		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("color_red", Lobby.RED.color().toString());
		vars.put("red", Lobby.RED.getName());
		vars.put("color_blue", Lobby.BLUE.color().toString());
		vars.put("blue", Lobby.BLUE.getName());

		sender.sendMessage(Translator.getString("COMMAND_GENERAL_BLUE", vars));
		sender.sendMessage(Translator.getString("COMMAND_GENERAL_RED", vars));
		sender.sendMessage(Translator.getString("COMMAND_GENERAL_RANDOM"));
		sender.sendMessage(Translator.getString("COMMAND_GENERAL_SPEC"));
		sender.sendMessage(Translator.getString("COMMAND_GENERAL_LEAVE"));
		sender.sendMessage(Translator.getString("COMMAND_GENERAL_TOGGLE"));
		sender.sendMessage(Translator.getString("COMMAND_GENERAL_SHOP"));
		sender.sendMessage(Translator.getString("COMMAND_GENERAL_STATS"));
		sender.sendMessage(Translator.getString("COMMAND_GENERAL_RANK"));
		sender.sendMessage(Translator.getString("COMMAND_GENERAL_TOP"));
		sender.sendMessage(Translator.getString("COMMAND_GENERAL_CASH"));
	}
	public void arenahelp(CommandSender sender) {
		sender.sendMessage(blablubbabc);
		sender.sendMessage(Translator.getString("COMMAND_ARENA_HELP"));
		sender.sendMessage(Translator.getString("COMMAND_ARENA_ARENA"));
		sender.sendMessage(Translator.getString("COMMAND_ARENA_LIST"));
		sender.sendMessage(Translator.getString("COMMAND_ARENA_CREATE"));
		sender.sendMessage(Translator.getString("COMMAND_ARENA_INFO"));

		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("color_red", Lobby.RED.color().toString());
		vars.put("red", Lobby.RED.getName());
		vars.put("color_blue", Lobby.BLUE.color().toString());
		vars.put("blue", Lobby.BLUE.getName());
		vars.put("color_spec", Lobby.SPECTATE.color().toString());
		vars.put("spec", Lobby.SPECTATE.getName());

		sender.sendMessage(Translator.getString("COMMAND_ARENA_BLUE", vars));
		sender.sendMessage(Translator.getString("COMMAND_ARENA_RED", vars));
		sender.sendMessage(Translator.getString("COMMAND_ARENA_SPEC", vars));
		sender.sendMessage(Translator.getString("COMMAND_ARENA_REMOVE"));
		sender.sendMessage(Translator.getString("COMMAND_ARENA_DELBLUE", vars));
		sender.sendMessage(Translator.getString("COMMAND_ARENA_DELRED", vars));
		sender.sendMessage(Translator.getString("COMMAND_ARENA_DELSPEC", vars));
		sender.sendMessage(Translator.getString("COMMAND_ARENA_SET"));
		sender.sendMessage(Translator.getString("COMMAND_ARENA_DISABLE"));
		sender.sendMessage(Translator.getString("COMMAND_ARENA_ENABLE"));
	}
	public void adminhelp(CommandSender sender) {
		sender.sendMessage(blablubbabc);
		sender.sendMessage(Translator.getString("COMMAND_ADMIN_HELP"));
		sender.sendMessage(Translator.getString("COMMAND_ADMIN_ARENA"));
		sender.sendMessage(Translator.getString("COMMAND_ADMIN_ADMIN"));
		sender.sendMessage(Translator.getString("COMMAND_ADMIN_RELOAD"));
		sender.sendMessage(Translator.getString("COMMAND_ADMIN_SOFTRELOAD"));
		sender.sendMessage(Translator.getString("COMMAND_ADMIN_DISABLE"));
		sender.sendMessage(Translator.getString("COMMAND_ADMIN_LOBBY_SPAWN"));
		sender.sendMessage(Translator.getString("COMMAND_ADMIN_LOBBY_REMOVE"));
		sender.sendMessage(Translator.getString("COMMAND_ADMIN_STATS"));
		sender.sendMessage(Translator.getString("COMMAND_ADMIN_RESET"));
		sender.sendMessage(Translator.getString("COMMAND_ADMIN_SET"));
		sender.sendMessage(Translator.getString("COMMAND_ADMIN_ADD"));
		sender.sendMessage(Translator.getString("COMMAND_ADMIN_RANK"));
		sender.sendMessage(Translator.getString("COMMAND_ADMIN_NEXT"));
		sender.sendMessage(Translator.getString("COMMAND_ADMIN_RANDOM"));
		sender.sendMessage(Translator.getString("COMMAND_ADMIN_HAPPY"));
		sender.sendMessage(Translator.getString("COMMAND_ADMIN_PLAY"));
		sender.sendMessage(Translator.getString("COMMAND_ADMIN_GIFTS"));
	}

	public void joinTeam(final Player player, final Lobby team) {
		if(!Lobby.LOBBY.isMember(player)) {
			joinLobbyPre(player, new Runnable() {
				
				@Override
				public void run() {	
					handleJoinTeam(player, team);
				}
			});
		} else {
			handleJoinTeam(player, team);
		}
		
	}
	
	private void handleJoinTeam(Player player, Lobby team) {
		if(Lobby.isPlaying(player) || Lobby.isSpectating(player)) {
			player.sendMessage(Translator.getString("CANNOT_CHANGE_TEAM_PLAYING"));
			return;
		}
		
		boolean rb = false;
		boolean spec = false;
		if (team == Lobby.RED || team == Lobby.BLUE) rb = true;
		else if (team == Lobby.SPECTATE) spec = true;
		
		//Max Players Check:
		if (!spec) {
			if (!Lobby.inTeam(player) || Lobby.SPECTATE.isMember(player)) {
				int players = Lobby.RED.number() + Lobby.BLUE.number() + Lobby.RANDOM.number();
				if (players >= plugin.maxPlayers) {
					player.sendMessage(Translator.getString("CANNOT_JOIN_TEAM_FULL"));
					return;
				}
			}
			if (rb && plugin.onlyRandom) {
				player.sendMessage(Translator.getString("ONLY_RANDOM"));
				if (!plugin.autoRandom)
					return;
			}
		}
		if (Lobby.inTeam(player) || Lobby.SPECTATE.isMember(player)) {
			Lobby.getTeam(player).removeMember(player);
			player.sendMessage(Translator.getString("YOU_LEFT_CURRENT_TEAM"));
		}
		//only random + auto random
		if (rb && plugin.onlyRandom && plugin.autoRandom) {
			Lobby.RANDOM.addMember(player);
			Map<String, String> vars = new HashMap<String, String>();
			vars.put("color_random", Lobby.RANDOM.color().toString());
			player.sendMessage(Translator.getString("AUTO_JOIN_RANDOM", vars));
		} else {
			team.addMember(player);
			Map<String, String> vars = new HashMap<String, String>();
			vars.put("color_team", team.color().toString());
			vars.put("team", team.getName());
			if(rb) player.sendMessage(Translator.getString("YOU_JOINED_TEAM", vars));
			else if (team.equals(Lobby.RANDOM)) player.sendMessage(Translator.getString("YOU_JOINED_RANDOM", vars));
			else if (spec) player.sendMessage(Translator.getString("YOU_JOINED_SPECTATORS", vars));
		}
		if (!spec) {
			String ready = plugin.matchManager.ready();
			if (ready.equalsIgnoreCase(Translator.getString("READY"))) {
				plugin.matchManager.countdown(plugin.countdown, plugin.countdownInit);
			} else {
				plugin.feeder.status(player, ready);
			}
		}
		//players:
		plugin.feeder.players(player);
	}

	public void joinLobbyPre(Player player, Runnable runOnSuccess) {
		//Lobby vorhanden?
		if(plugin.getLobbyspawnsCount() == 0) {
			player.sendMessage(Translator.getString("NO_LOBBY_FOUND"));
			return;
		}
		//inventory
		if(!Utils.isEmptyInventory(player) && plugin.checkInventory ) {
			player.sendMessage(Translator.getString("NEED_CLEAR_INVENTORY"));
			return;
		}
		//gamemode an?
		if(!player.getGameMode().equals(GameMode.SURVIVAL) && plugin.checkGamemode ) {
			player.sendMessage(Translator.getString("NEED_RIGHT_GAMEMODE"));
			return;
		}
		//flymode an? (built-in fly mode)
		if( (player.getAllowFlight() || player.isFlying()) && plugin.checkFlymode ) {
			player.sendMessage(Translator.getString("NEED_STOP_FLYING"));
			return;
		}
		//brennt? f�llt? taucht?
		if( (player.getFireTicks() > 0 || player.getFallDistance() > 0 || player.getRemainingAir() < player.getMaximumAir()) && plugin.checkBurning ) {
			player.sendMessage(Translator.getString("NEED_STOP_FALLING_BURNING_DROWNING"));
			return;
		}
		//wenig leben
		if(player.getHealth() < player.getMaxHealth()  && plugin.checkHealth) {
			player.sendMessage(Translator.getString("NEED_FULL_HEALTH"));
			return;
		}
		//hungert
		if(player.getFoodLevel() < 20 && plugin.checkFood) {
			player.sendMessage(Translator.getString("NEED_FULL_FOOD"));
			return;
		}
		//hat effekte auf sich
		if(player.getActivePotionEffects().size() > 0  && plugin.checkEffects) {
			player.sendMessage(Translator.getString("NEED_NO_EFFECTS"));
			return;
		}

		plugin.joinLobbyFresh(player, runOnSuccess);
	}

}