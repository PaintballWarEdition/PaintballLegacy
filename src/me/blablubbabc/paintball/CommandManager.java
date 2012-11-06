package me.blablubbabc.paintball;

import java.util.HashMap;
import me.blablubbabc.paintball.commands.CmdAdmin;
import me.blablubbabc.paintball.commands.CmdArena;
import me.blablubbabc.paintball.commands.CmdShop;
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
			//PERMISSION CHECK
			if(!plugin.noPerms) {
				if (!sender.isOp()
						&& !sender.hasPermission("paintball.general")) {
					sender.sendMessage(plugin.t.getString("NO_PERMISSION"));
					return true;
				}
			}
			// else anyone is allowed..
			
			if(args.length == 0) {
				pbhelp(sender);
				return true;
			} else if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
				pbhelp(sender);
				return true;
			} else if(args[0].equalsIgnoreCase("arena")) {
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
			}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else {
				if(sender instanceof Player) return false;
				else sender.sendMessage(plugin.t.getString("COMMAND_UNKNOWN_OR_NOT_CONSOLE"));
				return true;
			}
		}
		return false;
	}
	
	public void pbhelp(CommandSender sender) {
		sender.sendMessage(plugin.aqua+""+ plugin.bold+"[ "+plugin.yellow+""+ plugin.bold+"Paintball by blablubbabc"+plugin.aqua+""+ plugin.bold+" ]");
		sender.sendMessage(plugin.t.getString("COMMAND_GENERAL_HELP"));
		sender.sendMessage(plugin.t.getString("COMMAND_GENERAL_ARENA"));
		sender.sendMessage(plugin.t.getString("COMMAND_GENERAL_ADMIN"));
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
		sender.sendMessage(plugin.aqua+""+ plugin.bold+"[ "+plugin.yellow+""+ plugin.bold+"Paintball by blablubbabc"+plugin.aqua+""+ plugin.bold+" ]");
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
		sender.sendMessage(plugin.aqua+""+ plugin.bold+"[ "+plugin.yellow+""+ plugin.bold+"Paintball by blablubbabc"+plugin.aqua+""+ plugin.bold+" ]");
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
		sender.sendMessage(plugin.t.getString("COMMAND_ADMIN_LIST"));
	}
	
	public boolean joinTeam(Player player, Lobby team) {
		boolean rb = false;
		boolean spec = false;
		if(team.equals(Lobby.RED) || team.equals(Lobby.BLUE)) rb = true;
		else if(team.equals(Lobby.SPECTATE)) spec = true;
		
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
