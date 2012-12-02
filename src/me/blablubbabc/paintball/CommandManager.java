package me.blablubbabc.paintball;

import java.util.HashMap;
import me.blablubbabc.paintball.commands.CmdAdmin;
import me.blablubbabc.paintball.commands.CmdArena;
import me.blablubbabc.paintball.commands.CmdShop;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandManager implements CommandExecutor{
	private Paintball plugin;
	private CmdArena cmdArena;
	private CmdAdmin cmdAdmin;
	private CmdShop cmdShop;


	public CommandManager(Paintball pl) {
		plugin = pl;
		cmdArena = new CmdArena(plugin, plugin.am);
		cmdAdmin = new CmdAdmin(plugin);
		cmdShop = new CmdShop(plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(label.equalsIgnoreCase("pb")) {
			if(args.length == 0) {
				//pbhelp(sender);
				return true;
			} else if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
				//pbhelp(sender);
				return true;
			} else if(args[0].equalsIgnoreCase("info")) {
				//pbinfo(sender);
				return true;
			} else {

				//PERMISSION CHECK
				///pb, pb help, and pb info is allowed for anyone.
				if(!plugin.noPerms) {
					if (!sender.isOp()
							&& !sender.hasPermission("paintball.general")) {
						sender.sendMessage(plugin.t.getString("NO_PERMISSION"));
						return true;
					}
				}

				if(args[0].equalsIgnoreCase("arena")) {
					if(args.length == 1) {
						arenahelp(sender);
						return true;
					} else {
						//executor:
						return cmdArena.command(sender, args);
					}
				} else if(args[0].equalsIgnoreCase("admin")) {
					if(args.length == 1) {
						adminhelp(sender);
						return true;
					} else {
						//executor:
						return cmdAdmin.command(sender, args);
					}
				} else if(sender instanceof Player) {
					Player player = (Player) sender;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					if(args[0].equalsIgnoreCase("lobby")) {
						if(Lobby.LOBBY.isMember(player)) {
							if(Lobby.isPlaying(player)) {
								player.sendMessage(plugin.t.getString("CANNOT_JOIN_LOBBY_PLAYING"));
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
							player.sendMessage(plugin.t.getString("ALREADY_IN_LOBBY"));
							return true;
						} else {
							joinLobbyPre(player);
							return true;
						}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////			
					} else if(args[0].equalsIgnoreCase("blue")) {
						return joinTeam(player, Lobby.BLUE);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					} else if(args[0].equalsIgnoreCase("red")) {
						return joinTeam(player, Lobby.RED);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					} else if(args[0].equalsIgnoreCase("random")) {
						return joinTeam(player, Lobby.RANDOM);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					} else if(args[0].equalsIgnoreCase("spec")) {
						return joinTeam(player, Lobby.SPECTATE);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					} else if(args[0].equalsIgnoreCase("leave")) {
						if(!Lobby.LOBBY.isMember(player)) {
							player.sendMessage(plugin.t.getString("NOT_IN_LOBBY"));
							return true;
						}
						if(Lobby.inTeam(player) || Lobby.SPECTATE.isMember(player)) {
							if(Lobby.isPlaying(player) || Lobby.isSpectating(player)) {
								player.sendMessage(plugin.t.getString("CANNOT_LEAVE_LOBBY_PLAYING"));
								return true;
							}
							Lobby.getTeam(player).removeMember(player);
							player.sendMessage(plugin.t.getString("YOU_LEFT_TEAM"));
							return true;
						} else if(plugin.autoLobby) {
							player.sendMessage(plugin.t.getString("CANNOT_LEAVE_LOBBY"));
							return true;
						}
						plugin.leaveLobby(player, true, true, true);
						return true;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					} else if(args[0].equalsIgnoreCase("toggle")) {
						if(!Lobby.LOBBY.isMember(player)) {
							player.sendMessage(plugin.t.getString("NOT_IN_LOBBY"));
							return true;
						}
						Lobby.toggleFeed(player);
						player.sendMessage(plugin.t.getString("TOGGLED_FEED"));
						return true;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					} else if(args[0].equalsIgnoreCase("rank")) {
						if(args.length == 1) plugin.stats.sendRank(player, player.getName(), "points");
						else plugin.stats.sendRank(player, player.getName(), args[1]);
						return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					} else if(args[0].equalsIgnoreCase("stats")) {
						plugin.stats.sendStats(player, player.getName());
						return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					} else if(args[0].equalsIgnoreCase("cash")) {
						plugin.stats.sendCash(player, player.getName());
						return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					} else if(args[0].equalsIgnoreCase("shop")) {
						//executor
						return cmdShop.command(sender, args);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					}
				}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				//CONSOLE AND PLAYER
				if(args[0].equalsIgnoreCase("top")) {
					if(args.length == 1) plugin.stats.sendTop(sender, "points");
					else plugin.stats.sendTop(sender, args[1]);
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[0].equalsIgnoreCase("list")) {
					sender.sendMessage(plugin.t.getString("PLAYER_OVERVIEW"));
					for(Lobby l : Lobby.values()) {
						sender.sendMessage(l.color()+l.name()+" ( "+l.number()+" ):");
						for(Player p : l.getMembers()) {
							if(l != Lobby.LOBBY) sender.sendMessage(ChatColor.GRAY+p.getName()+ChatColor.WHITE+" : "+(Lobby.isPlaying(p) ? ((l == Lobby.SPECTATE) ? plugin.t.getString("SPECTATING"):plugin.t.getString("PLAYING")):plugin.t.getString("WAITING")));
							if(l == Lobby.LOBBY && Lobby.getTeam(p) == Lobby.LOBBY) sender.sendMessage(ChatColor.GRAY+p.getName()+ChatColor.WHITE+" : "+plugin.t.getString("NOT_IN_TEAM"));
						}
					}
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else {
					if(sender instanceof Player) return false;
					else sender.sendMessage(plugin.t.getString("COMMAND_UNKNOWN_OR_NOT_CONSOLE"));
					return true;
				}
			}
		}
		return false;
	}

	public boolean hasGeneralPerm(CommandSender sender) {
		if(plugin.noPerms) return true;
		if(sender.hasPermission("paintball.general")) return true;
		if(sender.isOp() || sender.hasPermission("paintball.arena") || sender.hasPermission("paintball.admin")) return true;
		return false;
	}
	
	public void pbinfo(CommandSender sender) {
		sender.sendMessage(plugin.aqua+""+ plugin.bold+"[ "+plugin.gold+""+plugin.italic+""+plugin.bold+"Paintball by blablubbabc"+plugin.reset+plugin.aqua+""+ plugin.bold+" ]");
		sender.sendMessage(plugin.dark_green+"Permission: "+plugin.gold+(hasGeneralPerm(sender) ? plugin.t.getString("ALLOWED_TO_PLAY_PAINTBALL"):plugin.t.getString("NOT_ALLOWED_TO_PLAY_PAINTBALL")));
		sender.sendMessage(plugin.dark_green+"Version: "+plugin.gold+plugin.getDescription().getVersion());
		sender.sendMessage(plugin.dark_green+"Website: "+plugin.gold+"dev.bukkit.org/server-mods/paintball_pure_war/");
		sender.sendMessage(plugin.dark_red+"Basic license hints: ");
		sender.sendMessage(plugin.red+"* Commercial usage of this plugin in any kind is not allowed.");
		sender.sendMessage(plugin.red+"* Modifying code is not allowed.");
		sender.sendMessage(plugin.gold+"You can find a complete list of usage condition on the bukkit dev page.");
		//sender.sendMessage(plugin.dark_green+"If you think your servers admins violate against this rule,");
		//sender.sendMessage(plugin.dark_green+"feel free to report them to us so we can take legal action.");
		//sender.sendMessage(plugin.green+"Thank you.");
	}
	
	public void pbhelp(CommandSender sender) {
		sender.sendMessage(plugin.aqua+""+ plugin.bold+"[ "+plugin.gold+""+plugin.italic+""+plugin.bold+"Paintball by blablubbabc"+plugin.reset+plugin.aqua+""+ plugin.bold+" ]");
		sender.sendMessage(plugin.t.getString("COMMAND_GENERAL_HELP"));
		String info = plugin.t.getString("COMMAND_GENERAL_INFO");
		if(!ChatColor.stripColor(info).contains("info")) info = "&c/pb info &b- Showing information about paintball plugin.";
		sender.sendMessage(info);
		sender.sendMessage(plugin.t.getString("COMMAND_GENERAL_ARENA"));
		sender.sendMessage(plugin.t.getString("COMMAND_GENERAL_ADMIN"));
		sender.sendMessage(plugin.t.getString("COMMAND_GENERAL_LIST"));
		sender.sendMessage(plugin.t.getString("COMMAND_GENERAL_LOBBY"));

		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("color_red", Lobby.RED.color().toString());
		vars.put("red", Lobby.RED.getName());
		vars.put("color_blue", Lobby.BLUE.color().toString());
		vars.put("blue", Lobby.BLUE.getName());

		sender.sendMessage(plugin.t.getString("COMMAND_GENERAL_BLUE", vars));
		sender.sendMessage(plugin.t.getString("COMMAND_GENERAL_RED", vars));
		sender.sendMessage(plugin.t.getString("COMMAND_GENERAL_RANDOM"));
		sender.sendMessage(plugin.t.getString("COMMAND_GENERAL_SPEC"));
		sender.sendMessage(plugin.t.getString("COMMAND_GENERAL_LEAVE"));
		sender.sendMessage(plugin.t.getString("COMMAND_GENERAL_TOGGLE"));
		sender.sendMessage(plugin.t.getString("COMMAND_GENERAL_SHOP"));
		sender.sendMessage(plugin.t.getString("COMMAND_GENERAL_STATS"));
		sender.sendMessage(plugin.t.getString("COMMAND_GENERAL_RANK"));
		sender.sendMessage(plugin.t.getString("COMMAND_GENERAL_TOP"));
		sender.sendMessage(plugin.t.getString("COMMAND_GENERAL_CASH"));
	}
	public void arenahelp(CommandSender sender) {
		sender.sendMessage(plugin.aqua+""+ plugin.bold+"[ "+plugin.gold+""+plugin.italic+""+plugin.bold+"Paintball by blablubbabc"+plugin.reset+plugin.aqua+""+ plugin.bold+" ]");
		sender.sendMessage(plugin.t.getString("COMMAND_ARENA_HELP"));
		sender.sendMessage(plugin.t.getString("COMMAND_ARENA_ARENA"));
		sender.sendMessage(plugin.t.getString("COMMAND_ARENA_LIST"));
		sender.sendMessage(plugin.t.getString("COMMAND_ARENA_CREATE"));
		sender.sendMessage(plugin.t.getString("COMMAND_ARENA_INFO"));

		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("color_red", Lobby.RED.color().toString());
		vars.put("red", Lobby.RED.getName());
		vars.put("color_blue", Lobby.BLUE.color().toString());
		vars.put("blue", Lobby.BLUE.getName());
		vars.put("color_spec", Lobby.SPECTATE.color().toString());
		vars.put("spec", Lobby.SPECTATE.getName());

		sender.sendMessage(plugin.t.getString("COMMAND_ARENA_BLUE", vars));
		sender.sendMessage(plugin.t.getString("COMMAND_ARENA_RED", vars));
		sender.sendMessage(plugin.t.getString("COMMAND_ARENA_SPEC", vars));
		sender.sendMessage(plugin.t.getString("COMMAND_ARENA_REMOVE"));
		sender.sendMessage(plugin.t.getString("COMMAND_ARENA_DELBLUE", vars));
		sender.sendMessage(plugin.t.getString("COMMAND_ARENA_DELRED", vars));
		sender.sendMessage(plugin.t.getString("COMMAND_ARENA_DELSPEC", vars));
		sender.sendMessage(plugin.t.getString("COMMAND_ARENA_SET"));
	}
	public void adminhelp(CommandSender sender) {
		sender.sendMessage(plugin.aqua+""+ plugin.bold+"[ "+plugin.gold+""+plugin.italic+""+plugin.bold+"Paintball by blablubbabc"+plugin.reset+plugin.aqua+""+ plugin.bold+" ]");
		sender.sendMessage(plugin.t.getString("COMMAND_ADMIN_HELP"));
		sender.sendMessage(plugin.t.getString("COMMAND_ADMIN_ARENA"));
		sender.sendMessage(plugin.t.getString("COMMAND_ADMIN_ADMIN"));
		sender.sendMessage(plugin.t.getString("COMMAND_ADMIN_RELOAD"));
		sender.sendMessage(plugin.t.getString("COMMAND_ADMIN_SOFTRELOAD"));
		sender.sendMessage(plugin.t.getString("COMMAND_ADMIN_DISABLE"));
		sender.sendMessage(plugin.t.getString("COMMAND_ADMIN_LOBBY_SPAWN"));
		sender.sendMessage(plugin.t.getString("COMMAND_ADMIN_LOBBY_REMOVE"));
		sender.sendMessage(plugin.t.getString("COMMAND_ADMIN_STATS"));
		sender.sendMessage(plugin.t.getString("COMMAND_ADMIN_RESET"));
		sender.sendMessage(plugin.t.getString("COMMAND_ADMIN_SET"));
		sender.sendMessage(plugin.t.getString("COMMAND_ADMIN_ADD"));
		sender.sendMessage(plugin.t.getString("COMMAND_ADMIN_RANK"));
		sender.sendMessage(plugin.t.getString("COMMAND_ADMIN_NEXT"));
		sender.sendMessage(plugin.t.getString("COMMAND_ADMIN_RANDOM"));
	}

	public boolean joinTeam(Player player, Lobby team) {
		boolean rb = false;
		boolean spec = false;
		if(team == Lobby.RED || team == Lobby.BLUE) rb = true;
		else if(team == Lobby.SPECTATE) spec = true;

		if(!Lobby.LOBBY.isMember(player)) {
			if(!joinLobbyPre(player)) {
				return true;
			}
		}
		if(Lobby.isPlaying(player) || Lobby.isSpectating(player)) {
			player.sendMessage(plugin.t.getString("CANNOT_CHANGE_TEAM_PLAYING"));
			return true;
		}
		//Max Players Check:
		if(!spec) {
			if(!Lobby.inTeam(player) || Lobby.SPECTATE.isMember(player)) {
				int players = Lobby.RED.number() + Lobby.BLUE.number() + Lobby.RANDOM.number();
				if(players >= plugin.maxPlayers) {
					player.sendMessage(plugin.t.getString("CANNOT_JOIN_TEAM_FULL"));
					return true;
				}
			}
			if(rb && plugin.onlyRandom) {
				player.sendMessage(plugin.t.getString("ONLY_RANDOM"));
				if (!plugin.autoRandom)
					return true;
			}
		}
		if(Lobby.inTeam(player) || Lobby.SPECTATE.isMember(player)) {
			Lobby.getTeam(player).removeMember(player);
			player.sendMessage(plugin.t.getString("YOU_LEFT_CURRENT_TEAM"));
		}
		//only random + auto random
		if(rb && plugin.onlyRandom && plugin.autoRandom) {
			Lobby.RANDOM.addMember(player);
			HashMap<String, String> vars = new HashMap<String, String>();
			vars.put("color_random", Lobby.RANDOM.color().toString());
			player.sendMessage(plugin.t.getString("AUTO_JOIN_RANDOM", vars));
		} else {
			team.addMember(player);
			HashMap<String, String> vars = new HashMap<String, String>();
			vars.put("color_team", team.color().toString());
			vars.put("team", team.getName());
			if(rb) player.sendMessage(plugin.t.getString("YOU_JOINED_TEAM", vars));
			else if(team.equals(Lobby.RANDOM)) player.sendMessage(plugin.t.getString("YOU_JOINED_RANDOM", vars));
			else if(spec) player.sendMessage(plugin.t.getString("YOU_JOINED_SPECTATORS", vars));
		}
		if(!spec) {
			String ready = plugin.mm.ready();
			if (ready.equalsIgnoreCase(plugin.t.getString("READY"))) {
				plugin.mm.countdown(plugin.countdown, plugin.countdownInit);
			} else {
				plugin.nf.status(player, ready);
			}
		}
		//players:
		plugin.nf.players(player);
		return true;
	}

	public boolean joinLobbyPre(Player player) {
		//Lobby vorhanden?
		if(plugin.getLobbyspawnsCount() == 0) {
			player.sendMessage(plugin.t.getString("NO_LOBBY_FOUND"));
			return false;
		}
		//inventory
		if(!plugin.isEmpty(player) && plugin.checkInventory ) {
			player.sendMessage(plugin.t.getString("NEED_CLEAR_INVENTORY"));
			return false;
		}
		//gamemode an?
		if(!player.getGameMode().equals(GameMode.SURVIVAL) && plugin.checkGamemode ) {
			player.sendMessage(plugin.t.getString("NEED_RIGHT_GAMEMODE"));
			return false;
		}
		//flymode an? (built-in fly mode)
		if( (player.getAllowFlight() || player.isFlying()) && plugin.checkFlymode ) {
			player.sendMessage(plugin.t.getString("NEED_STOP_FLYING"));
			return false;
		}
		//brennt? fällt? taucht?
		if( (player.getFireTicks() > 0 || player.getFallDistance() > 0 || player.getRemainingAir() < player.getMaximumAir()) && plugin.checkBurning ) {
			player.sendMessage(plugin.t.getString("NEED_STOP_FALLING_BURNING_DROWNING"));
			return false;
		}
		//wenig leben
		if(player.getHealth() < 20  && plugin.checkHealth) {
			player.sendMessage(plugin.t.getString("NEED_FULL_HEALTH"));
			return false;
		}
		//hungert
		if(player.getFoodLevel() < 20  && plugin.checkFood) {
			player.sendMessage(plugin.t.getString("NEED_FULL_FOOD"));
			return false;
		}
		//hat effecte auf sich
		if(player.getActivePotionEffects().size() > 0  && plugin.checkEffects) {
			player.sendMessage(plugin.t.getString("NEED_NO_EFFECTS"));
			return false;
		}

		//to be safe..
		//inventory
		if(plugin.saveInventory) {
			plugin.pm.setInv(player, player.getInventory());
			player.sendMessage(plugin.t.getString("INVENTORY_SAVED"));
		}
		//save Location
		plugin.pm.setLoc(player, player.getLocation());
		//lobby add
		Lobby.LOBBY.addMember(player);
		plugin.nf.join(player.getName());

		plugin.joinLobby(player);

		return true;
	}

}
