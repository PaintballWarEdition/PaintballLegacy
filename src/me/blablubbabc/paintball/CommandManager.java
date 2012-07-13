package me.blablubbabc.paintball;

import java.util.ArrayList;

import me.blablubbabc.paintball.commands.CmdAdmin;
import me.blablubbabc.paintball.commands.CmdArena;
import me.blablubbabc.paintball.commands.CmdShop;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

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
			if(!sender.isOp() && !sender.hasPermission("paintball.general")) {
				sender.sendMessage(plugin.red+"No permission.");
				return true;
			}
			if(args.length == 0) {
				//TESTINGS
				if(sender.getName().equals("blablubbabc")) {
					plugin.mm.getMatch((Player) sender).hitSnow(plugin.getServer().getPlayer("AlphaX96"), plugin.getServer().getPlayer("blablubbabc"));
					plugin.mm.getMatch((Player) sender).hitSnow(plugin.getServer().getPlayer("blablubbabc"), plugin.getServer().getPlayer("AlphaX96"));
					plugin.getServer().broadcastMessage("WAZZUUUUP! BLABLUBB HERE!");
					return true;
				}
				//TESTINGS
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
							player.sendMessage(plugin.gray + "You can't join the lobby while playing!");
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
						player.sendMessage(plugin.gray + "You are already in the lobby. /pb leave to leave.");
						return true;
					} else {
						joinLobby(player);
						return true;
					}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////			
				} else if(args[0].equalsIgnoreCase("blue")) {
					if(!Lobby.LOBBY.isMember(player)) {
						if(!joinLobby(player)) {
							return true;
						}
					}
					if(Lobby.isPlaying(player) || Lobby.isSpectating(player)) {
						player.sendMessage(plugin.gray + "You can't change your team while playing or spectating!");
						return true;
					}
					//Max Players Check:
					if(!Lobby.inTeam(player)) {
						int players = Lobby.RED.number() + Lobby.BLUE.number() + Lobby.RANDOM.number();
						if(players > plugin.maxPlayers) {
							player.sendMessage(plugin.gray + "Maximal number of paintball players is already reached!");
							return true;
						}
					}
					if(Lobby.inTeam(player) || Lobby.SPECTATE.isMember(player)) {
						Lobby.getTeam(player).removeMember(player);
						player.sendMessage(plugin.gray + "You left your current team.");
					}
					Lobby.BLUE.addMember(player);
					player.sendMessage(plugin.green + "You joined team "+Lobby.BLUE.color()+"blue!");
					//players:
					player.sendMessage(plugin.aqua+""+"Waiting players: " + plugin.nf.getPlayers());
					if(plugin.mm.ready().equalsIgnoreCase("ready")) {
						plugin.mm.countdown(plugin.countdown, plugin.countdownInit);
					} else {
						plugin.nf.status(plugin.mm.ready());
					}
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[0].equalsIgnoreCase("red")) {
					if(!Lobby.LOBBY.isMember(player)) {
						if(!joinLobby(player)) {
							return true;
						}
					}
					if(Lobby.isPlaying(player) || Lobby.isSpectating(player)) {
						player.sendMessage(plugin.gray + "You can't change your team while playing or spectating!");
						return true;
					}
					//Max Players Check:
					if(!Lobby.inTeam(player)) {
						int players = Lobby.RED.number() + Lobby.BLUE.number() + Lobby.RANDOM.number();
						if(players > plugin.maxPlayers) {
							player.sendMessage(plugin.gray + "Maximal number of paintball players is already reached!");
							return true;
						}
					}
					if(Lobby.inTeam(player) || Lobby.SPECTATE.isMember(player)) {
						Lobby.getTeam(player).removeMember(player);
						player.sendMessage(plugin.gray + "You left your current team.");
					}
					Lobby.RED.addMember(player);
					player.sendMessage(plugin.green + "You joined team "+Lobby.RED.color()+"plugin.red!");
					//players:
					player.sendMessage(plugin.aqua+""+"Waiting players: " + plugin.nf.getPlayers());
					if(plugin.mm.ready().equalsIgnoreCase("ready")) {
						plugin.mm.countdown(plugin.countdown, plugin.countdownInit);
					} else {
						plugin.nf.status(plugin.mm.ready());
					}
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[0].equalsIgnoreCase("random")) {
					if(!Lobby.LOBBY.isMember(player)) {
						if(!joinLobby(player)) {
							return true;
						}
					}
					if(Lobby.isPlaying(player) || Lobby.isSpectating(player)) {
						player.sendMessage(plugin.gray + "You can't change your team while playing or spectating!");
						return true;
					}
					//Max Players Check:
					if(!Lobby.inTeam(player)) {
						int players = Lobby.RED.number() + Lobby.BLUE.number() + Lobby.RANDOM.number();
						if(players > plugin.maxPlayers) {
							player.sendMessage(plugin.gray + "Maximal number of paintball players is already reached!");
							return true;
						}
					}
					if(Lobby.inTeam(player) || Lobby.SPECTATE.isMember(player)) {
						Lobby.getTeam(player).removeMember(player);
						player.sendMessage(plugin.gray + "You left your current team.");
					}
					Lobby.RANDOM.addMember(player);
					player.sendMessage(plugin.green + "You joined a "+Lobby.RANDOM.color()+"random team!");
					//players:
					player.sendMessage(plugin.aqua+""+"Waiting players: " + plugin.nf.getPlayers());
					if(plugin.mm.ready().equalsIgnoreCase("ready")) {
						plugin.mm.countdown(plugin.countdown, plugin.countdownInit);
					} else {
						plugin.nf.status(plugin.mm.ready());
					}
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[0].equalsIgnoreCase("spec")) {
					if(!Lobby.LOBBY.isMember(player)) {
						if(!joinLobby(player)) {
							return true;
						}
					}
					if(Lobby.isPlaying(player) || Lobby.isSpectating(player)) {
						player.sendMessage(plugin.gray + "You can't change your team while playing or spectating!");
						return true;
					}
					if(Lobby.inTeam(player) || Lobby.SPECTATE.isMember(player)) {
						Lobby.getTeam(player).removeMember(player);
						player.sendMessage(plugin.gray + "You left your current team.");
					}
					Lobby.SPECTATE.addMember(player);
					player.sendMessage(plugin.green + "You joined the "+Lobby.SPECTATE.color()+"spectators!");
					//players:
					player.sendMessage(plugin.aqua+""+"Waiting players: " + plugin.nf.getPlayers());
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[0].equalsIgnoreCase("leave")) {
					if(!Lobby.LOBBY.isMember(player)) {
						player.sendMessage(plugin.gray + "You are not in the paintball lobby.");
						return true;
					}
					if(Lobby.inTeam(player) || Lobby.SPECTATE.isMember(player)) {
						if(Lobby.isPlaying(player) || Lobby.isSpectating(player)) {
							player.sendMessage(plugin.gray + "You can't leave while playing or spectating!");
							return true;
						}
						Lobby.getTeam(player).removeMember(player);
						player.sendMessage(plugin.gray + "You left your team.");
						return true;
					}
					plugin.leaveLobby(player, true, true, true);
					return true;
					
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[0].equalsIgnoreCase("toggle")) {
					if(!Lobby.LOBBY.isMember(player)) {
						player.sendMessage(plugin.gray + "You are not in the paintball lobby.");
						return true;
					}
					Lobby.toggleFeed(player);
					player.sendMessage(plugin.gray + "You toggled the paintball news feed.");
					return true;
					
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[0].equalsIgnoreCase("rank")) {
					plugin.stats.sendRank(player, player.getName());
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[0].equalsIgnoreCase("top")) {
					plugin.stats.sendTop(player);
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
			} else {
				plugin.log("This command cannot be used in console.");
				return true;
			}
		}
		return false;
	}
	
	public void pbhelp(CommandSender sender) {
		sender.sendMessage(plugin.aqua+""+ plugin.bold+"[ "+plugin.yellow+""+ plugin.bold+"Paintball by blablubbabc"+plugin.aqua+""+ plugin.bold+" ]");
		sender.sendMessage(plugin.green+"/pb [help|?] "+plugin.aqua+"- Shows this help page.");
		sender.sendMessage(plugin.yellow+"/pb arena "+plugin.aqua+"- Arena commands.");
		sender.sendMessage(plugin.yellow+"/pb admin "+plugin.aqua+"- Admin commands.");
		sender.sendMessage(plugin.green+"/pb lobby "+plugin.aqua+"- Join the lobby.");
		sender.sendMessage(plugin.green+"/pb blue "+plugin.aqua+"- Join team blue.");
		sender.sendMessage(plugin.green+"/pb plugin.red "+plugin.aqua+"- Join team plugin.red.");
		sender.sendMessage(plugin.green+"/pb random "+plugin.aqua+"- Join random team.");
		sender.sendMessage(plugin.green+"/pb spec "+plugin.aqua+"- Join the spectators.");
		sender.sendMessage(plugin.green+"/pb leave "+plugin.aqua+"- Leave current team. If not in team: leave lobby.");
		sender.sendMessage(plugin.green+"/pb toggle "+plugin.aqua+"- Toggles some paintball messages.");
		sender.sendMessage(plugin.green+"/pb shop [id] "+plugin.aqua+"- Paintball-Shop.");
		sender.sendMessage(plugin.green+"/pb stats "+plugin.aqua+"- Shows some stats.");
		sender.sendMessage(plugin.green+"/pb rank "+plugin.aqua+"- Shows the players rank.");
		sender.sendMessage(plugin.green+"/pb top "+plugin.aqua+"- Shows the top 10 fraggers.");
		sender.sendMessage(plugin.green+"/pb cash "+plugin.aqua+"- Shows money.");
	}
	public void arenahelp(CommandSender sender) {
		sender.sendMessage(plugin.aqua+""+ plugin.bold+"[ "+plugin.yellow+""+ plugin.bold+"Paintball by blablubbabc"+plugin.aqua+""+ plugin.bold+" ]");
		sender.sendMessage(plugin.yellow+"/pb [help|?] "+plugin.aqua+"- Shows help page.");
		sender.sendMessage(plugin.yellow+"/pb arena "+plugin.aqua+"- Shows this arena commands.");
		sender.sendMessage(plugin.yellow+"/pb arena list "+plugin.aqua+"- Lists all arenas.");
		sender.sendMessage(plugin.yellow+"/pb arena <name> "+plugin.aqua+"- Create a new arena.");
		sender.sendMessage(plugin.yellow+"/pb arena <name> info "+plugin.aqua+"- Shows arena infos.");
		sender.sendMessage(plugin.yellow+"/pb arena <name> blue "+plugin.aqua+"- Adds a blue spawn.");
		sender.sendMessage(plugin.yellow+"/pb arena <name> plugin.red "+plugin.aqua+"- Adds a plugin.red spawn.");
		sender.sendMessage(plugin.yellow+"/pb arena <name> spec "+plugin.aqua+"- Adds a spectator spawn.");
		sender.sendMessage(plugin.yellow+"/pb arena <name> remove "+plugin.aqua+"- Removes arena.");
		sender.sendMessage(plugin.yellow+"/pb arena <name> delblue "+plugin.aqua+"- Deletes blue spawns.");
		sender.sendMessage(plugin.yellow+"/pb arena <name> delplugin.red "+plugin.aqua+"- Deletes plugin.red spawns.");
		sender.sendMessage(plugin.yellow+"/pb arena <name> delspec "+plugin.aqua+"- Deletes spectator spawns.");
		sender.sendMessage(plugin.yellow+"/pb arena <name> size <number> "+plugin.aqua+"- Specifiy the size-categorie of the arena.");
	}
	public void adminhelp(CommandSender sender) {
		sender.sendMessage(plugin.aqua+""+ plugin.bold+"[ "+plugin.yellow+""+ plugin.bold+"Paintball by blablubbabc"+plugin.aqua+""+ plugin.bold+" ]");
		sender.sendMessage(plugin.yellow+"/pb [help|?] "+plugin.aqua+"- Shows help page.");
		sender.sendMessage(plugin.yellow+"/pb arena "+plugin.aqua+"- Arena commands.");
		sender.sendMessage(plugin.yellow+"/pb admin "+plugin.aqua+"- Shows this admin commands.");
		sender.sendMessage(plugin.yellow+"/pb admin reload "+plugin.aqua+"- Reload the plugin, kicking all players out of the lobby.");
		sender.sendMessage(plugin.yellow+"/pb admin softreload "+plugin.aqua+"- Reload the plugin, waiting for all matches to finish.");
		sender.sendMessage(plugin.yellow+"/pb admin disable "+plugin.aqua+"- Toggles if new paintball matches are allowed or not.");
		sender.sendMessage(plugin.yellow+"/pb admin lobby spawn "+plugin.aqua+"- Adds a lobby spawn.");
		sender.sendMessage(plugin.yellow+"/pb admin lobby remove "+plugin.aqua+"- Deletes the lobby spawns.");
		sender.sendMessage(plugin.yellow+"/pb admin cash <player> [amount] "+plugin.aqua+"- Shows the player's cash or gives him money.");
		sender.sendMessage(plugin.yellow+"/pb admin rank <player> [amount] "+plugin.aqua+"- Shows the player's points and rank or gives him points.");
		sender.sendMessage(plugin.yellow+"/pb admin reset <all|player> [value] "+plugin.aqua+"- Resets all stats of all or one player or a specified value of the players stats.");
		sender.sendMessage(plugin.yellow+"/pb admin helmet <blue|plugin.red|spec> "+plugin.aqua+"- Sets the helmet to the item holding in hand.");
		sender.sendMessage(plugin.yellow+"/pb admin next <arena> "+plugin.aqua+"- Tries to force the next arena to the specified arena.");
	}
	private boolean joinLobby(Player player) {
		//Lobby vorhanden?
		if(plugin.getLobbySpawns().size() == 0) {
			player.sendMessage(plugin.gray+"No paintball lobby found!");
			return false;
		}
		//inventory
		if(!plugin.isEmpty(player) && plugin.checkInventory ) {
			player.sendMessage(plugin.gray+"You have to clear your inventory first to enter the lobby!");
			return false;
		}
		//gamemode an?
		if(!player.getGameMode().equals(GameMode.SURVIVAL) && plugin.checkGamemode ) {
			player.sendMessage(plugin.gray+"You have to change your gamemode to 'survival' first to enter the lobby!");
			return false;
		}
		//flymode an? (built-in fly mode)
		if( (player.getAllowFlight() || player.isFlying()) && plugin.checkFlymode ) {
			player.sendMessage(plugin.gray+"You have to disable your fly mode / stop flying to enter the lobby!");
			return false;
		}
		//brennt? fällt? taucht?
		if( (player.getFireTicks() > 0 || player.getFallDistance() > 0 || player.getRemainingAir() < player.getMaximumAir()) && plugin.checkBurning ) {
			player.sendMessage(plugin.gray+"You can't join the lobby while falling, being on fire or drowning!");
			return false;
		}
		//wenig leben
		if(player.getHealth() < 20  && plugin.checkHealth) {
			player.sendMessage(plugin.gray+"You need full health to enter the lobby!");
			return false;
		}
		//hungert
		if(player.getFoodLevel() < 20  && plugin.checkFood) {
			player.sendMessage(plugin.gray+"You need a full food bar to enter the lobby!");
			return false;
		}
		//hat effecte auf sich
		if(player.getActivePotionEffects().size() > 0  && plugin.checkEffects) {
			player.sendMessage(plugin.gray+"You can't enter the lobby with active potion effects!");
			return false;
		}
		
		//to be safe..
		//inventory
		if(plugin.saveInventory) {
			plugin.pm.setInv(player, player.getInventory());
			player.sendMessage(plugin.gray+"Inventory saved.");
		}
		if(!plugin.isEmpty(player)) plugin.clearInv(player);
		//gamemode
		if(!player.getGameMode().equals(GameMode.SURVIVAL)) player.setGameMode(GameMode.SURVIVAL);
		//flymode (built-in)
		if(player.getAllowFlight()) player.setAllowFlight(false);
		if(player.isFlying()) player.setFlying(false);
		//feuer
		if(player.getFireTicks() > 0) player.setFireTicks(0);
		//Health + Food
		if(player.getHealth() < 20) player.setHealth(20);
		if(player.getFoodLevel() < 20) player.setFoodLevel(20);
		//effekte entfernen
		if(player.getActivePotionEffects().size() > 0) {
			ArrayList<PotionEffect> effects = new ArrayList<PotionEffect>();
			for(PotionEffect eff : player.getActivePotionEffects()) {
				effects.add(eff);
			}
			for(PotionEffect eff :effects) {
				player.removePotionEffect(eff.getType());
			}	
		}
		
		//save Location
		plugin.pm.setLoc(player, player.getLocation());
		//Lobbyteleport
		player.teleport(plugin.transformLocation(plugin.getLobbySpawns().get(0)));
		//lobby add
		Lobby.LOBBY.addMember(player);
		plugin.nf.join(player.getName());
		return true;
	}
	
}
