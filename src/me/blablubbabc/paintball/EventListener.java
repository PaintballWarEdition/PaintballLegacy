package me.blablubbabc.paintball;

import java.util.HashMap;
import java.util.HashSet;
import me.blablubbabc.paintball.extras.Airstrike;
import me.blablubbabc.paintball.extras.Grenade;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class EventListener implements Listener{
	private Paintball plugin;
	private MatchManager mm;

	private HashMap<Player, Integer> taskIds;
	private HashSet<Byte> transparent;
	private HashMap<Player, String> chatMessages;

	public EventListener(Paintball pl) {
		plugin = pl;
		mm = plugin.mm;
		taskIds = new HashMap<Player, Integer>();
		chatMessages = new HashMap<Player, String>();

		transparent = new HashSet<Byte>();
		transparent.add((byte) 0);
		transparent.add((byte) 8);
		transparent.add((byte) 10);
		transparent.add((byte) 51);
		transparent.add((byte) 90);
		transparent.add((byte) 119);
		transparent.add((byte) 321);
		transparent.add((byte) 85);
		
	}

	///////////////////////////////////////////
	//EVENTS

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onPlayerHit(EntityDamageByEntityEvent event) {
		if(event.getDamager() instanceof Projectile) {
			Projectile shot = (Projectile) event.getDamager();
			if(shot.getShooter() instanceof Player && event.getEntity() instanceof Player) {
				Player shooter = (Player) shot.getShooter();
				Player target = (Player) event.getEntity();
				if(!shooter.equals(target)) {
					if(mm.getMatch(shooter) != null && mm.getMatch(target) != null) {
						if(mm.getMatch(shooter).equals(mm.getMatch(target))) {
							Match match = mm.getMatch(shooter);
							//Geschoss?
							if(shot instanceof Snowball) {
								//hit by snowball
								match.hitSnow(target, shooter);
							}
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerShoot(ProjectileLaunchEvent event) {
		if(event.getEntity().getShooter() instanceof Player) {
			Player player = (Player) event.getEntity().getShooter();
			if(Lobby.getTeam(player) != null) {
				Projectile shot = (Projectile) event.getEntity();
				Vector v = shot.getVelocity();
				//Geschoss?
				if(shot instanceof Snowball) {
					//zählen wenn in-match
					if(mm.getMatch(player) != null) {
						mm.getMatch(player).shot(player);
					}
					if(plugin.balls == -1) {
						//+1 ball
						player.getInventory().addItem(new ItemStack(Material.SNOW_BALL, 1));
					}
					//Vector v = player.getLocation().getDirection();
					//boosting:
					shot.setVelocity(v.multiply(plugin.speedmulti));
				} else if(shot instanceof Egg) {
					if(plugin.grenades) {
						Grenade.eggThrow(player, (Egg) shot);
						if(plugin.grenadeAmount == -1) {
							//+1grenade
							player.getInventory().addItem(new ItemStack(Material.EGG, 1));
						}
						//Vector v = player.getLocation().getDirection();
						//boosting:
						shot.setVelocity(v.multiply(plugin.grenadeSpeed));
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEggThrow(PlayerEggThrowEvent event) {
		if(event.getEgg().getShooter() instanceof Player) {
			Player player = (Player) event.getEgg().getShooter();
			if(Lobby.getTeam(player) != null) {
				event.setHatching(false);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInventory(InventoryClickEvent event) {
		if(event.getWhoClicked() instanceof Player) {
			Player player = (Player) event.getWhoClicked();
			if(Lobby.getTeam(player) != null) {
				if(!event.getSlotType().equals(SlotType.CONTAINER) && !event.getSlotType().equals(SlotType.QUICKBAR) && !event.getSlotType().equals(SlotType.OUTSIDE)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = (Player) event.getPlayer();
		if(Lobby.getTeam(player) != null) {
			if(player.getItemInHand().getTypeId() == 280) {
				if(event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
					if(Airstrike.marked(player)) {
						if(!Airstrike.active) {
							Airstrike.call(plugin, player);
							//remove stick if not infinite
							if(plugin.airstrikeAmount != -1) {
								int amount = (player.getInventory().getItemInHand()
										.getAmount() - 1);
								if (amount > 0)
									player.getInventory().setItemInHand(
											new ItemStack(280, amount));
								else
									player.getInventory().setItemInHand(null);
							}
						} else {
							player.sendMessage(plugin.gray+"There is already one airstrike going on.");
						}
					}
				}
			} else if(player.getItemInHand().getTypeId() == 344) {
				if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
					player.sendMessage(plugin.green +"Fire in the hole!");
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onItemInHand(PlayerItemHeldEvent event) {
		final Player player = event.getPlayer();
		if(Lobby.getTeam(player) != null) {
			if(player.getInventory().getItem(event.getNewSlot()) != null && player.getInventory().getItem(event.getNewSlot()).getTypeId() == 280) {
				if (!taskIds.containsKey(player)) {
					int taskId = plugin.getServer().getScheduler()
							.scheduleSyncRepeatingTask(plugin, new Runnable() {

								@Override
								public void run() {
									if (player.getItemInHand().getTypeId() == 280) {
										Block block = player.getTargetBlock(
												transparent, 1000);
										if (!Airstrike.isBlock(block, player)) {
											Airstrike.demark(player);
											Airstrike.mark(block, player);
										}
									} else {
										plugin.getServer().getScheduler().cancelTask(taskIds.get(player));
										taskIds.remove(player);
										Airstrike.demark(player);
									}
								}
							}, 0L, 1L);
					taskIds.put(player, taskId);
				}
			} else {
				if (taskIds.containsKey(player)) {
					plugin.getServer().getScheduler().cancelTask(taskIds.get(player));
					taskIds.remove(player);
					Airstrike.demark(player);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onProjectileHit(ProjectileHitEvent event) {
		Projectile shot = event.getEntity();
		if(shot instanceof Snowball) {
			//some effect maybe?
		} else if(shot instanceof Egg) {
			if(plugin.grenades) {
				Grenade.hit(shot, plugin);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof Player) {
			Player target = (Player) event.getEntity();
			if(Lobby.getTeam(target) != null) {
				event.setDamage(0);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if(Lobby.getTeam(player) != null) {
			if(!player.isOp() && !player.hasPermission("paintball.admin")) event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if(Lobby.getTeam(player) != null) {
			if(!player.isOp() && !player.hasPermission("paintball.admin")) event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerHunger(FoodLevelChangeEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(Lobby.getTeam(player) != null) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerItemsI(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();
		if(Lobby.getTeam(player) != null) {
			if(!player.isOp() && !player.hasPermission("paintball.admin")) event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerItemsII(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		if(Lobby.getTeam(player) != null) {
			if(!player.isOp() && !player.hasPermission("paintball.admin")) event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		if(Lobby.getTeam(event.getPlayer()) != null && !event.getMessage().startsWith("/pb") && !event.getPlayer().hasPermission("paintball.admin") && !event.getPlayer().isOp() ) {
			event.getPlayer().sendMessage(plugin.gray + "This command is not allowed while playing paintball.");
			event.setCancelled(true);
		}
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChat1(PlayerChatEvent event) {
		Player player = event.getPlayer();
		if(Lobby.getTeam(player) != null) {
			if(plugin.chatnames) {
				String message = "3zpaintball3z"+event.getMessage()+"3zpaintball3z";
				chatMessages.put(player, message);
			}
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat2(PlayerChatEvent event) {
		Player player = event.getPlayer();
		if(Lobby.getTeam(player) != null) {
			if(plugin.chatnames) {
				if (chatMessages.containsKey(player)) {
					String message = chatMessages.get(player);
					chatMessages.remove(player);
					//filter "3zpaintball3z" raus
					String[] split = message.split("3zpaintball3z");
					message = split[1];
					
					if (plugin.mm.getMatch(player) != null) {
						//noch im match?
						if (!plugin.mm.getMatch(player).hasLeft(player)) {
							
							//nur textfarbe:
							ChatColor farbe = Lobby.LOBBY.color();
							
							if(plugin.mm.getMatch(player).isRed(player)) farbe = Lobby.RED.color();
							else if(plugin.mm.getMatch(player).isBlue(player)) farbe = Lobby.BLUE.color();
							else if(plugin.mm.getMatch(player).isSpec(player)) farbe = Lobby.SPECTATE.color();
							
							event.setMessage(farbe + message);
							
							/*String team = plugin.mm.getMatch(player).getTeamName(player);
							if (team.equalsIgnoreCase("red")) {

								farbe = ChatColor.RED;

								/*String n = ChatColor.RED + player.getName();
								if (n.length() > 16)
									n = (String) n.subSequence(0,
											n.length() - (n.length() - 16));

								event.setMessage(n + ": " + message);
							} else if (team.equalsIgnoreCase("blue")) {

								farbe = ChatColor.BLUE;

								/*String n = ChatColor.BLUE + player.getName();
								if (n.length() > 16)
									n = (String) n.subSequence(0,
											n.length() - (n.length() - 16));

								event.setMessage(n + ": " + message);
							}*/
							
						}
					}
				}
			}
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDead(PlayerDeathEvent event) {
		Player player = (Player) event.getEntity();
		if(Lobby.getTeam(player) != null) {
			//plugin.nf.leave(player.getName());
			//exit game
			if(Lobby.isPlaying(player) || Lobby.isSpectating(player)) mm.getMatch(player).left(player);
			plugin.leaveLobby(player, true, false, false);
			//drops?
			event.getDrops().removeAll(event.getDrops());
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event) {
		plugin.pm.addPlayer(event.getPlayer().getName());
	}
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent event) {
		this.onPlayerDisconnect(event.getPlayer());
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerKick(PlayerKickEvent event) {
		// EVENT IS CANCELLED? => RETURN
		if (event.isCancelled())
			return;
		this.onPlayerDisconnect(event.getPlayer());
	}
	private void onPlayerDisconnect(final Player player) {
		if(Lobby.getTeam(player) != null) {
			//plugin.nf.leave(player.getName());
			//exit game
			if(Lobby.isPlaying(player) || Lobby.isSpectating(player)) mm.getMatch(player).left(player);
			plugin.leaveLobby(player, true, true, true);
			
			/*//clear inventory
			plugin.clearInv(player);
			//Exit lobby
			Lobby.remove(player);
			//Teleport back
			player.teleport(plugin.pm.getLoc(player));*/
			
			
		}

	}

}
