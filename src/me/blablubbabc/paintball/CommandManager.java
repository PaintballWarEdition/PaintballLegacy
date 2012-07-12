package me.blablubbabc.paintball;

import java.util.ArrayList;

import me.blablubbabc.paintball.commands.CmdAdmin;
import me.blablubbabc.paintball.commands.CmdArena;
import me.blablubbabc.paintball.commands.CmdShop;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class CommandManager implements CommandExecutor{
	private Paintball plugin;
	private CmdArena cmdArena;
	private CmdAdmin cmdAdmin;
	private CmdShop cmdShop;
	
	private ChatColor gray = ChatColor.GRAY;
	private ChatColor green = ChatColor.GREEN;
	private ChatColor aqua = ChatColor.AQUA;
	private ChatColor yellow = ChatColor.YELLOW;
	private ChatColor red = ChatColor.RED;
	
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
				sender.sendMessage(red+"No permission.");
				return true;
			}
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
							player.sendMessage(gray + "You can't join the lobby while playing!");
							return true;
						}
						/*if(Lobby.isSpectating(player)) {
							//Lobbyteleport
							player.teleport(plugin.transformLocation(plugin.getLobbySpawns().get(0)));
							Lobby.SPECTATE.setWaiting(player);
							player.getInventory().clear();
							player.getInventory().setHelmet(null);
							player.sendMessage(green + "You entered the lobby!");
							return true;
						}*/
						player.sendMessage(gray + "You are already in the lobby. /pb leave to leave.");
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
						player.sendMessage(gray + "You can't change your team while playing or spectating!");
						return true;
					}
					//Max Players Check:
					if(!Lobby.inTeam(player)) {
						int players = Lobby.RED.number() + Lobby.BLUE.number() + Lobby.RANDOM.number();
						if(players > plugin.maxPlayers) {
							player.sendMessage(gray + "Maximal number of paintball players is already reached!");
							return true;
						}
					}
					if(Lobby.inTeam(player) || Lobby.SPECTATE.isMember(player)) {
						Lobby.getTeam(player).removeMember(player);
						player.sendMessage(gray + "You left your current team.");
					}
					Lobby.BLUE.addMember(player);
					player.sendMessage(green + "You joined team "+Lobby.BLUE.color()+"blue!");
					//players:
					player.sendMessage(aqua+""+"Waiting players: " + plugin.nf.getPlayers());
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
						player.sendMessage(gray + "You can't change your team while playing or spectating!");
						return true;
					}
					//Max Players Check:
					if(!Lobby.inTeam(player)) {
						int players = Lobby.RED.number() + Lobby.BLUE.number() + Lobby.RANDOM.number();
						if(players > plugin.maxPlayers) {
							player.sendMessage(gray + "Maximal number of paintball players is already reached!");
							return true;
						}
					}
					if(Lobby.inTeam(player) || Lobby.SPECTATE.isMember(player)) {
						Lobby.getTeam(player).removeMember(player);
						player.sendMessage(gray + "You left your current team.");
					}
					Lobby.RED.addMember(player);
					player.sendMessage(green + "You joined team "+Lobby.RED.color()+"red!");
					//players:
					player.sendMessage(aqua+""+"Waiting players: " + plugin.nf.getPlayers());
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
						player.sendMessage(gray + "You can't change your team while playing or spectating!");
						return true;
					}
					//Max Players Check:
					if(!Lobby.inTeam(player)) {
						int players = Lobby.RED.number() + Lobby.BLUE.number() + Lobby.RANDOM.number();
						if(players > plugin.maxPlayers) {
							player.sendMessage(gray + "Maximal number of paintball players is already reached!");
							return true;
						}
					}
					if(Lobby.inTeam(player) || Lobby.SPECTATE.isMember(player)) {
						Lobby.getTeam(player).removeMember(player);
						player.sendMessage(gray + "You left your current team.");
					}
					Lobby.RANDOM.addMember(player);
					player.sendMessage(green + "You joined a "+Lobby.RANDOM.color()+"random team!");
					//players:
					player.sendMessage(aqua+""+"Waiting players: " + plugin.nf.getPlayers());
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
						player.sendMessage(gray + "You can't change your team while playing or spectating!");
						return true;
					}
					if(Lobby.inTeam(player) || Lobby.SPECTATE.isMember(player)) {
						Lobby.getTeam(player).removeMember(player);
						player.sendMessage(gray + "You left your current team.");
					}
					Lobby.SPECTATE.addMember(player);
					player.sendMessage(green + "You joined the "+Lobby.SPECTATE.color()+"spectators!");
					//players:
					player.sendMessage(aqua+""+"Waiting players: " + plugin.nf.getPlayers());
					return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[0].equalsIgnoreCase("leave")) {
					if(!Lobby.LOBBY.isMember(player)) {
						player.sendMessage(gray + "You are not in the paintball lobby.");
						return true;
					}
					if(Lobby.inTeam(player) || Lobby.SPECTATE.isMember(player)) {
						if(Lobby.isPlaying(player) || Lobby.isSpectating(player)) {
							player.sendMessage(gray + "You can't leave while playing or spectating!");
							return true;
						}
						Lobby.getTeam(player).removeMember(player);
						player.sendMessage(gray + "You left your team.");
						return true;
					}
					Lobby.remove(player);
					sendBack(player);
					player.sendMessage(gray + "You left the lobby.");
					plugin.nf.leave(player.getName());
					return true;
					
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				} else if(args[0].equalsIgnoreCase("toggle")) {
					if(!Lobby.LOBBY.isMember(player)) {
						player.sendMessage(gray + "You are not in the paintball lobby.");
						return true;
					}
					Lobby.toggleFeed(player);
					player.sendMessage(gray + "You toggled the paintball news feed.");
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
		sender.sendMessage(aqua+""+ ChatColor.BOLD+"[ "+yellow+""+ ChatColor.BOLD+"Paintball by blablubbabc"+aqua+""+ ChatColor.BOLD+" ]");
		sender.sendMessage(green+"/pb [help|?] "+aqua+"- Shows this help page.");
		sender.sendMessage(yellow+"/pb arena "+aqua+"- Arena commands.");
		sender.sendMessage(yellow+"/pb admin "+aqua+"- Admin commands.");
		sender.sendMessage(green+"/pb lobby "+aqua+"- Join the lobby.");
		sender.sendMessage(green+"/pb blue "+aqua+"- Join team blue.");
		sender.sendMessage(green+"/pb red "+aqua+"- Join team red.");
		sender.sendMessage(green+"/pb random "+aqua+"- Join random team.");
		sender.sendMessage(green+"/pb spec "+aqua+"- Join the spectators.");
		sender.sendMessage(green+"/pb leave "+aqua+"- Leave current team. If not in team: leave lobby.");
		sender.sendMessage(green+"/pb toggle "+aqua+"- Toggles some paintball messages.");
		sender.sendMessage(green+"/pb shop [id] "+aqua+"- Paintball-Shop.");
		sender.sendMessage(green+"/pb stats "+aqua+"- Shows some stats.");
		sender.sendMessage(green+"/pb rank "+aqua+"- Shows the players rank.");
		sender.sendMessage(green+"/pb top "+aqua+"- Shows the top 10 fraggers.");
		sender.sendMessage(green+"/pb cash "+aqua+"- Shows money.");
	}
	public void arenahelp(CommandSender sender) {
		sender.sendMessage(aqua+""+ ChatColor.BOLD+"[ "+yellow+""+ ChatColor.BOLD+"Paintball by blablubbabc"+aqua+""+ ChatColor.BOLD+" ]");
		sender.sendMessage(yellow+"/pb [help|?] "+aqua+"- Shows help page.");
		sender.sendMessage(yellow+"/pb arena "+aqua+"- Shows this arena commands.");
		sender.sendMessage(yellow+"/pb arena list "+aqua+"- Lists all arenas.");
		sender.sendMessage(yellow+"/pb arena <name> "+aqua+"- Create a new arena.");
		sender.sendMessage(yellow+"/pb arena <name> info "+aqua+"- Shows arena infos.");
		sender.sendMessage(yellow+"/pb arena <name> blue "+aqua+"- Adds a blue spawn.");
		sender.sendMessage(yellow+"/pb arena <name> red "+aqua+"- Adds a red spawn.");
		sender.sendMessage(yellow+"/pb arena <name> spec "+aqua+"- Adds a spectator spawn.");
		sender.sendMessage(yellow+"/pb arena <name> remove "+aqua+"- Removes arena.");
		sender.sendMessage(yellow+"/pb arena <name> delblue "+aqua+"- Deletes blue spawns.");
		sender.sendMessage(yellow+"/pb arena <name> delred "+aqua+"- Deletes red spawns.");
		sender.sendMessage(yellow+"/pb arena <name> delspec "+aqua+"- Deletes spectator spawns.");
		sender.sendMessage(yellow+"/pb arena <name> size <number> "+aqua+"- Specifiy the size-categorie of the arena.");
	}
	public void adminhelp(CommandSender sender) {
		sender.sendMessage(aqua+""+ ChatColor.BOLD+"[ "+yellow+""+ ChatColor.BOLD+"Paintball by blablubbabc"+aqua+""+ ChatColor.BOLD+" ]");
		sender.sendMessage(yellow+"/pb [help|?] "+aqua+"- Shows help page.");
		sender.sendMessage(yellow+"/pb arena "+aqua+"- Arena commands.");
		sender.sendMessage(yellow+"/pb admin "+aqua+"- Shows this admin commands.");
		sender.sendMessage(yellow+"/pb admin reload "+aqua+"- Reload the plugin, kicking all players out of the lobby.");
		sender.sendMessage(yellow+"/pb admin softreload "+aqua+"- Reload the plugin, waiting for all matches to finish.");
		sender.sendMessage(yellow+"/pb admin disable "+aqua+"- Toggles if new paintball matches are allowed or not.");
		sender.sendMessage(yellow+"/pb admin lobby spawn "+aqua+"- Adds a lobby spawn.");
		sender.sendMessage(yellow+"/pb admin lobby remove "+aqua+"- Deletes the lobby spawns.");
		sender.sendMessage(yellow+"/pb admin cash <player> [amount] "+aqua+"- Shows the player's cash or gives him money.");
		sender.sendMessage(yellow+"/pb admin rank <player> [amount] "+aqua+"- Shows the player's points and rank or gives him points.");
		sender.sendMessage(yellow+"/pb admin reset <all|player> [value] "+aqua+"- Resets all stats of all or one player or a specified value of the players stats.");
		sender.sendMessage(yellow+"/pb admin helmet <blue|red|spec> "+aqua+"- Sets the helmet to the item holding in hand.");
		sender.sendMessage(yellow+"/pb admin next <arena> "+aqua+"- Tries to force the next arena to the specified arena.");
	}
	private boolean joinLobby(Player player) {
		//Lobby vorhanden?
		if(plugin.getLobbySpawns().size() == 0) {
			player.sendMessage(gray+"No paintball lobby found!");
			return false;
		}
		//inventory
		if(!isEmpty(player) && plugin.checkInventory ) {
			player.sendMessage(gray+"You have to clear your inventory first to enter the lobby!");
			return false;
		}
		//gamemode an?
		if(!player.getGameMode().equals(GameMode.SURVIVAL) && plugin.checkGamemode ) {
			player.sendMessage(gray+"You have to change your gamemode to 'survival' first to enter the lobby!");
			return false;
		}
		//flymode an? (built-in fly mode)
		if( (player.getAllowFlight() || player.isFlying()) && plugin.checkFlymode ) {
			player.sendMessage(gray+"You have to disable your fly mode / stop flying to enter the lobby!");
			return false;
		}
		//brennt? fällt? taucht?
		if( (player.getFireTicks() > 0 || player.getFallDistance() > 0 || player.getRemainingAir() < player.getMaximumAir()) && plugin.checkBurning ) {
			player.sendMessage(gray+"You can't join the lobby while falling, being on fire or drowning!");
			return false;
		}
		//wenig leben
		if(player.getHealth() < 20  && plugin.checkHealth) {
			player.sendMessage(gray+"You need full health to enter the lobby!");
			return false;
		}
		//hungert
		if(player.getFoodLevel() < 20  && plugin.checkFood) {
			player.sendMessage(gray+"You need a full food bar to enter the lobby!");
			return false;
		}
		//hat effecte auf sich
		if(player.getActivePotionEffects().size() > 0  && plugin.checkEffects) {
			player.sendMessage(gray+"You can't enter the lobby with active potion effects!");
			return false;
		}
		
		//to be safe..
		//inventory
		if(!isEmpty(player)) plugin.mm.clearInv(player);
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
		plugin.pm.setLoc(player.getName(), player.getLocation());
		//Lobbyteleport
		player.teleport(plugin.transformLocation(plugin.getLobbySpawns().get(0)));
		//lobby add
		Lobby.LOBBY.addMember(player);
		plugin.nf.join(player.getName());
		return true;
	}
	
	public void sendBack(Player player) {
		player.teleport(plugin.pm.getLoc(player.getName()));
	}
	
	public static boolean isEmpty(Player p) {
		for(ItemStack i : p.getInventory()) {
			if(i == null) continue;
			if(i.getTypeId() != 0) return false;
		}
		for(ItemStack i : p.getInventory().getArmorContents()) {
			if(i == null) continue;
			if(i.getTypeId() != 0) return false;
		}
		return true;
	}
}
