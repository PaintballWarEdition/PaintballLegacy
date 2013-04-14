package me.blablubbabc.paintball;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import me.blablubbabc.paintball.extras.Airstrike;
import me.blablubbabc.paintball.extras.Ball;
import me.blablubbabc.paintball.extras.Grenade;
import me.blablubbabc.paintball.extras.Mine;
import me.blablubbabc.paintball.extras.Pumpgun;
import me.blablubbabc.paintball.extras.Rocket;
import me.blablubbabc.paintball.extras.Sniper;
import me.blablubbabc.paintball.extras.Turret;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.BlockIterator;

public class EventListener implements Listener {
	private Paintball plugin;
	private MatchManager mm;

	private ConcurrentHashMap<String, Integer> taskIds;
	private HashSet<Byte> transparent;
	private long lastSignUpdate = 0;

	// private HashMap<Player, String> chatMessages;

	public EventListener(Paintball pl) {
		plugin = pl;
		mm = plugin.mm;
		taskIds = new ConcurrentHashMap<String, Integer>();
		// chatMessages = new HashMap<Player, String>();

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

	// /////////////////////////////////////////
	// EVENTS

	/*
	 * @EventHandler(ignoreCancelled = true) public void
	 * onPlayerMove(PlayerMoveEvent event) { Player player = event.getPlayer();
	 * Match match = mm.getMatch(player); if (match != null) { if
	 * (!match.started) { if (event.getFrom().getX() != event.getTo().getX() ||
	 * event.getFrom().getZ() != event.getTo().getZ()) {
	 * event.setCancelled(true); } } } }
	 */

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onSignCreate(SignChangeEvent event) {
		Player player = event.getPlayer();
		String l = ChatColor.stripColor(event.getLine(0));

		for (String s : plugin.sql.sqlPlayers.statsList) {
			if (s.equals("teamattacks"))
				s = "ta";
			if (s.equals("hitquote"))
				s = "hq";
			if (s.equals("airstrikes"))
				s = "as";
			if (s.equals("money_spent"))
				s = "spent";

			if (l.equalsIgnoreCase("[PB " + s.toUpperCase() + "]")) {
				if (!player.isOp() && !player.hasPermission("paintball.admin")) {
					event.setCancelled(true);
					player.sendMessage(plugin.t.getString("NO_PERMISSION"));
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntityType() == EntityType.SNOWMAN) {
			Snowman snowman = (Snowman) event.getEntity();
			Turret turret = Turret.getIsTurret(snowman);
			if (turret != null) {
				turret.die(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onInteract(PlayerInteractEvent event) {
		if (event.getClickedBlock() != null) {
			Block block = event.getClickedBlock();
			BlockState state = block.getState();
			if (state instanceof Sign) {
				Sign sign = (Sign) state;
				String l = ChatColor.stripColor(sign.getLine(0));

				for (String stat : plugin.sql.sqlPlayers.statsList) {
					String s = stat;
					if (s.equals("teamattacks"))
						s = "ta";
					if (s.equals("hitquote"))
						s = "hq";
					if (s.equals("airstrikes"))
						s = "as";
					if (s.equals("money_spent"))
						s = "spent";

					if (l.equalsIgnoreCase("[PB " + s.toUpperCase() + "]")) {
						changeSign(event.getPlayer().getName(), sign, stat);
						break;
					}
				}
			}
		}
	}

	private void changeSign(String player, Sign sign, String stat) {
		if ((System.currentTimeMillis() - lastSignUpdate) > (500)) {
			HashMap<String, String> vars = new HashMap<String, String>();
			vars.put("player", player);
			if (plugin.pm.exists(player)) {
				if (stat.equals("hitquote") || stat.equals("kd")) {
					DecimalFormat dec = new DecimalFormat("###.##");
					float statF = (float) (Integer) plugin.pm.getStats(player).get(stat) / 100;
					vars.put("value", dec.format(statF));
				} else
					vars.put("value", "" + plugin.pm.getStats(player).get(stat));
			} else
				vars.put("value", plugin.t.getString("NOT_FOUND"));
			sign.setLine(1, plugin.t.getString("SIGN_LINE_TWO", vars));
			sign.setLine(2, plugin.t.getString("SIGN_LINE_THREE", vars));
			sign.setLine(3, plugin.t.getString("SIGN_LINE_FOUR", vars));
			sign.update();
			lastSignUpdate = System.currentTimeMillis();
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onPlayerHit(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Projectile) {
			Projectile shot = (Projectile) event.getDamager();
			if (shot.getShooter() instanceof Player) {
				Player shooter = (Player) shot.getShooter();
				Match match = mm.getMatch(shooter);
				if (match != null) {
					if (event.getEntity() instanceof Player) {
						Player target = (Player) event.getEntity();
						if (shooter != target) {
							Match matchTarget = mm.getMatch(target);
							if (match != null && matchTarget != null) {
								if (match == matchTarget) {
									if (!match.isSpec(shooter) && !match.isSpec(target) && match.isSurvivor(shooter) && match.isSurvivor(target)
											&& match.started) {
										// Geschoss?
										if (shot instanceof Snowball) {
											// match
											//TODO
											if (shot.hasMetadata("Paintba11")) match.hitSnow(target, shooter);
										}
									}
								}
							}
						}
					} else if (event.getEntityType() == EntityType.SNOWMAN) {
						Snowman snowman = (Snowman) event.getEntity();
						Turret turret = Turret.getIsTurret(snowman);
						if (turret != null && match == turret.match && match.enemys(shooter, turret.player)) {
							turret.hit();
						}
					}
				}
			}
		} else if (event.getDamager() instanceof Player && event.getEntity() instanceof Player && event.getCause() == DamageCause.ENTITY_ATTACK) {
			Player attacker = (Player) event.getDamager();
			Player target = (Player) event.getEntity();
			if (attacker != target) {
				Match matchA = mm.getMatch(attacker);
				if (matchA != null) {
					Match matchT = mm.getMatch(target);
					if (matchT != null) {
						if (matchA == matchT) {
							if (matchA.enemys(attacker, target) && matchA.isSurvivor(attacker) && matchA.isSurvivor(target) && matchA.started) {
								if (plugin.allowMelee) {
									if (target.getHealth() > plugin.meleeDamage)
										target.setHealth(target.getHealth() - plugin.meleeDamage);
									else {
										matchA.frag(target, attacker);
									}
								}
							}
						}	
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onFireballExplosion(EntityExplodeEvent event) {
		Entity entity = event.getEntity();
		if (entity != null && entity.getType() == EntityType.FIREBALL) {
			Fireball fireball = (Fireball) entity;
			if (fireball.getShooter() instanceof Player) {
				if (Rocket.getRocket(fireball, ((Player)fireball.getShooter()).getName(), false) != null) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerShoot(ProjectileLaunchEvent event) {
		if (event.getEntity().getShooter() instanceof Player) {
			Player player = (Player) event.getEntity().getShooter();
			if (Lobby.LOBBY.isMember(player)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEggThrow(PlayerEggThrowEvent event) {
		Egg egg = event.getEgg();
		if (egg.getShooter() instanceof Player) {
			Player player = (Player) egg.getShooter();
			if (Grenade.getGrenade(egg, player.getName(), false) != null) {
				event.setHatching(false);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInventory(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			Player player = (Player) event.getWhoClicked();
			if (Lobby.LOBBY.isMember(player)) {
				if (event.getSlotType() != SlotType.CONTAINER && event.getSlotType() != SlotType.QUICKBAR && event.getSlotType() != SlotType.OUTSIDE) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteractPlayer(PlayerInteractEntityEvent event) {
		Player player = (Player) event.getPlayer();
		if (Lobby.LOBBY.isMember(player)) {
			if (plugin.giftsEnabled && player.getItemInHand().getType() == Material.CHEST) {
				if (event.getRightClicked() instanceof Player) {
					Player receiver = (Player) event.getRightClicked();
					if (Lobby.getTeam(receiver) != null) {
						plugin.christmas.giveGift(player, receiver);
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = player.getItemInHand();
		if (item == null)
			return;
		
		if (Lobby.LOBBY.isMember(player)) {
			Match match = mm.getMatch(player);
			if (match != null && Lobby.isPlaying(player) && match.isSurvivor(player)) {
				if (!match.started || match.isJustRespawned(player.getName())) return;
				Action action = event.getAction();
				
				switch (item.getType()) {
				case SNOW_BALL:
					//MARKER
					if (isAirClick(action)) {
						Snowball ball = (Snowball) player.getWorld().spawnEntity(player.getEyeLocation(), EntityType.SNOWBALL);
						// register snowball
						Ball.registerBall(ball, player.getName(), Source.MARKER);
						// z�hlen
						match.addShots(player, 1);
						if (match.setting_balls != -1) {
							// -1 ball
							if (item.getAmount() <= 1)
								player.setItemInHand(null);
							else {
								item.setAmount(item.getAmount() - 1);
								player.setItemInHand(item);
							}
						}
						// boosting:
						ball.setVelocity(player.getLocation().getDirection().normalize().multiply(plugin.speedmulti));
					}
					break;
					
				case STICK:
					// AIRSTRIKE
					if (plugin.airstrike && isAirClick(action)) {
						if (Airstrike.marked(player.getName())) {
							if (Airstrike.getAirstrikeCountMatch() < plugin.airstrikeMatchLimit) {
								if (Airstrike.getAirstrikeCountPlayer(player.getName()) < plugin.airstrikePlayerLimit) {
									Airstrike.call(player, match);
									// z�hlen
									match.airstrike(player);
									// remove stick if not infinite
									if (match.setting_airstrikes != -1) {
										if (item.getAmount() <= 1)
											player.setItemInHand(null);
										else {
											item.setAmount(item.getAmount() - 1);
											player.setItemInHand(item);
										}
									}
								} else {
									player.sendMessage(plugin.t.getString("AIRSTRIKE_PLAYER_LIMIT_REACHED"));
								}

							} else {
								player.sendMessage(plugin.t.getString("AIRSTRIK_MATCH_LIMIT_REACHED"));
							}
						}
					}
					break;

				case EGG:
					// GRENADE
					if (plugin.grenade && isAirClick(action)) {
						player.sendMessage(plugin.t.getString("GRENADE_THROW"));
						player.playSound(player.getLocation(), Sound.SILVERFISH_IDLE, 100L, 1L);
						Egg egg = (Egg) player.getWorld().spawnEntity(player.getEyeLocation(), EntityType.EGG);
						Grenade.registerGrenade(egg, player.getName(), Source.GRENADE);
						// z�hlen
						match.grenade(player);
						if (match.setting_grenades != -1) {
							if (item.getAmount() <= 1)
								player.setItemInHand(null);
							else {
								item.setAmount(item.getAmount() - 1);
								player.setItemInHand(item);
							}
						}
						// boosting:
						egg.setVelocity(player.getLocation().getDirection().multiply(plugin.grenadeSpeed));
					}
					break;

				case SPECKLED_MELON:
					// PUMPGUN
					if (plugin.pumpgun && isAirClick(action)) {
						PlayerInventory inv = player.getInventory();
						if (inv.contains(Material.SNOW_BALL, plugin.pumpgunAmmo)) {
							Utils.removeInventoryItems(inv, Material.SNOW_BALL, plugin.pumpgunAmmo);
							player.updateInventory();
							match.addShots(player, 5);
							Pumpgun.shot(player);
						} else {
							player.playSound(player.getEyeLocation(), Sound.FIRE_IGNITE, 100F, 2F);
						}
					}
					break;

				case DIODE:
					// ROCKET LAUNCHER
					if (plugin.rocket && isAirClick(action)) {
						if (Rocket.getRocketCountMatch() < plugin.rocketMatchLimit) {
							if (Rocket.getRocketCountPlayer(player.getName()) < plugin.rocketPlayerLimit) {
								player.playSound(player.getLocation(), Sound.SILVERFISH_IDLE, 100L, 1L);
								Fireball rocket = (Fireball) player.getWorld().spawnEntity(player.getEyeLocation(), EntityType.FIREBALL);
								rocket.setShooter(player);
								rocket.setVelocity(player.getLocation().getDirection().normalize().multiply(plugin.rocketSpeedMulti));
								new Rocket(player, rocket);
								if (item.getAmount() <= 1)
									player.setItemInHand(null);
								else {
									item.setAmount(item.getAmount() - 1);
									player.setItemInHand(item);
								}
							} else {
								player.sendMessage(plugin.t.getString("ROCKET_PLAYER_LIMIT_REACHED"));
							}
						} else {
							player.sendMessage(plugin.t.getString("ROCKET_MATCH_LIMIT_REACHED"));
						}
					}
					break;

				case CARROT_STICK:
					// SNIPER
					if (plugin.sniper) {
						if (action == Action.LEFT_CLICK_AIR) {
							Sniper.toggleZoom(player);
						} else if (action == Action.RIGHT_CLICK_AIR) {
							if (!plugin.sniperOnlyUseIfZooming || Sniper.isZooming(player)) {
								PlayerInventory inv = player.getInventory();
								if (inv.contains(Material.SNOW_BALL, 1)) {
									Utils.removeInventoryItems(inv, Material.SNOW_BALL, 1);
									player.updateInventory();
									match.addShots(player, 1);
									Sniper.shoot(player);
								} else {
									player.playSound(player.getEyeLocation(), Sound.FIRE_IGNITE, 100F, 2F);
								}
							} else {
								player.playSound(player.getEyeLocation(), Sound.FIRE_IGNITE, 100F, 2F);
							}
						}
					}
					break;

				case CHEST:
					// GIFT
					if (plugin.giftsEnabled) {
						plugin.christmas.unwrapGift(player);
					}
					break;

				default:
					// no special item in hand
					break;
				}

			}
		}
	}

	private boolean isAirClick(Action action) {
		return (action == Action.RIGHT_CLICK_AIR || action == Action.LEFT_CLICK_AIR);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onItemInHand(PlayerItemHeldEvent event) {
		final Player player = event.getPlayer();
		final String name = player.getName();
		if (Lobby.LOBBY.isMember(player)) {
			// zooming?
			if (Sniper.isZooming(player))
				Sniper.setNotZooming(player);

			ItemStack item = player.getInventory().getItem(event.getNewSlot());
			if (item != null) {
				if (plugin.airstrike && item.getType() == Material.STICK) {
					if (!taskIds.containsKey(name)) {
						int taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

							@Override
							public void run() {
								if (player.getItemInHand().getTypeId() == 280) {
									Block block = player.getTargetBlock(transparent, 1000);
									if (!Airstrike.isBlock(block, name)) {
										Airstrike.demark(player);
										Airstrike.mark(block, player);
									}
								} else {
									plugin.getServer().getScheduler().cancelTask(taskIds.get(name));
									taskIds.remove(name);
									Airstrike.demark(player);
								}
							}
						}, 0L, 1L);
						taskIds.put(name, taskId);
					}
				} else {
					if (taskIds.containsKey(name)) {
						plugin.getServer().getScheduler().cancelTask(taskIds.get(name));
						taskIds.remove(name);
						Airstrike.demark(player);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onProjectileHit(ProjectileHitEvent event) {
		Projectile shot = event.getEntity();
		if (shot.getShooter() instanceof Player) {
			Player shooter = (Player) shot.getShooter();
			String shooterName = shooter.getName();
			
			if (shot instanceof Snowball) {
				Ball ball = Ball.getBall((Snowball)shot, shooterName, true);
				// is ball
				if (ball != null) {
					Match match = mm.getMatch(shooter);
					if (match != null) {
						Location loc = shot.getLocation();
						// mine
						if (plugin.mine) {
							Block block = loc.getBlock();
							Mine mine = Mine.getIsMine(block);
							if (mine != null && match == mine.match && (match.enemys(shooter, mine.player) || shooter.equals(mine.player))) {
								mine.explode(true);
							}

							BlockIterator iterator = new BlockIterator(loc.getWorld(), loc.toVector(), shot.getVelocity().normalize(), 0, 2);
							while (iterator.hasNext()) {
								Mine m = Mine.getIsMine(iterator.next());
								if (m != null) {
									if (match == m.match && (match.enemys(shooter, m.player) || shooter.equals(m.player))) {
										m.explode(true);
									}
								}
							}
						}
						// effect
						if (plugin.effects) {
							if (match.isBlue(shooter)) {
								loc.getWorld().playEffect(loc, Effect.POTION_BREAK, 0);
							} else if (match.isRed(shooter)) {
								loc.getWorld().playEffect(loc, Effect.POTION_BREAK, 5);
							}
						}
					}
				}
			} else if (plugin.grenade && shot instanceof Egg) {
				Grenade nade = Grenade.getGrenade((Egg)shot, shooter.getName(), true);
				if (nade != null) {
					Match match = mm.getMatch(shooter);
					if (match != null) {
						nade.explode(shot.getLocation(), shooter);	
					}
				}
			} else if (plugin.rocket && shot instanceof Fireball) {
				Rocket rocket = Rocket.getRocket((Fireball)shot, shooterName, true);
				if (rocket != null)
					rocket.die();
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player target = (Player) event.getEntity();
			Lobby team = Lobby.getTeam(target);
			if (team != null) {
				Match match = plugin.mm.getMatch(target);
				if (match != null && team != Lobby.SPECTATE && match.isSurvivor(target) && match.started) {
					if ((plugin.falldamage && event.getCause() == DamageCause.FALL) || (plugin.otherDamage && event.getCause() != DamageCause.FALL)) {
						if (target.getHealth() <= event.getDamage()) {
							event.setDamage(0);
							event.setCancelled(true);
							match.death(target);
						}
					} else {
						event.setDamage(0);
						event.setCancelled(true);
					}
				} else {
					event.setDamage(0);
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (Lobby.LOBBY.isMember(player)) {
			if (player.getGameMode() != GameMode.CREATIVE)
				event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if (event.getEntityType() == EntityType.SNOWMAN) {
			if (Turret.getIsTurret((Snowman) event.getEntity()) != null) {
				event.setCancelled(false);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if (Lobby.LOBBY.isMember(player)) {
			// if (!player.isOp() && !player.hasPermission("paintball.admin")) {
			if (player.getGameMode() == GameMode.CREATIVE) return;
			event.setCancelled(true);
			// }
			final Block block = event.getBlockPlaced();
			Match m = plugin.mm.getMatch(player);
			if (m != null && m.started && m.isSurvivor(player)) {
				if (plugin.turret && block.getType() == Material.PUMPKIN) {
					// turret:
					if (Turret.getTurretCountMatch() < plugin.turretMatchLimit) {
						if (Turret.getTurrets(player.getName()).size() < plugin.turretPlayerLimit) {
							Snowman snowman = (Snowman) block.getLocation().getWorld().spawnEntity(block.getLocation(), EntityType.SNOWMAN);
							new Turret(player, snowman, plugin.mm.getMatch(player));
							ItemStack i = player.getItemInHand();
							if (i.getAmount() <= 1)
								player.setItemInHand(null);
							else {
								i.setAmount(i.getAmount() - 1);
								player.setItemInHand(i);
							}
						} else {
							player.sendMessage(plugin.t.getString("TURRET_PLAYER_LIMIT_REACHED"));
						}
					} else {
						player.sendMessage(plugin.t.getString("TURRET_MATCH_LIMIT_REACHED"));
					}

				} else if (plugin.mine && block.getType() == Material.FLOWER_POT) {
					// mine:
					if (Mine.getMineCountMatch() < plugin.mineMatchLimit) {
						if (Mine.getMines(player.getName()).size() < plugin.minePlayerLimit) {
							plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {

								@Override
								public void run() {
									block.setType(Material.FLOWER_POT);
								}
							}, 1L);
							new Mine(player, block, plugin.mm.getMatch(player));
							ItemStack i = player.getItemInHand();
							if (i.getAmount() <= 1)
								player.setItemInHand(null);
							else {
								i.setAmount(i.getAmount() - 1);
								player.setItemInHand(i);
							}
						} else {
							player.sendMessage(plugin.t.getString("MINE_PLAYER_LIMIT_REACHED"));
						}
					} else {
						player.sendMessage(plugin.t.getString("MINE_MATCH_LIMIT_REACHED"));
					}

				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerHunger(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if (Lobby.LOBBY.isMember(player)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerItemsI(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();
		if (Lobby.LOBBY.isMember(player)) {
			// if (!player.isOp() && !player.hasPermission("paintball.admin"))
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerItemsII(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		if (Lobby.LOBBY.isMember(player)) {
			// if (!player.isOp() && !player.hasPermission("paintball.admin"))
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		if (Lobby.LOBBY.isMember(event.getPlayer()) && !event.getMessage().startsWith("/pb")
				&& (!plugin.allowedCommands.isEmpty() ? !isAllowedCommand(event.getMessage()) : true)) {
			if (!event.getPlayer().hasPermission("paintball.admin") && !event.getPlayer().isOp()) {
				event.getPlayer().sendMessage(plugin.t.getString("COMMAND_NOT_ALLOWED"));
				event.setCancelled(true);
			}
		} else if (!event.getMessage().startsWith("/pb") && plugin.checkBlacklist && isBlacklistedCommand(event.getMessage())) {
			if (!plugin.blacklistAdminOverride && !event.getPlayer().hasPermission("paintball.admin") && !event.getPlayer().isOp()) {
				event.getPlayer().sendMessage(plugin.t.getString("COMMAND_BLACKLISTED"));
				event.setCancelled(true);
			}
		}
	}

	private boolean isBlacklistedCommand(String cmd) {
		Set<Player> players = Lobby.LOBBY.getMembers();
		List<String> playernames = new ArrayList<String>();
		for (Player p : players) {
			playernames.add(p.getName());
		}
		for (String regex : plugin.blacklistedCommandsRegex) {
			for (String name : playernames) {
				if (cmd.matches(regex.replace("{player}", Pattern.quote(name)))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isAllowedCommand(String cmd) {
		if (plugin.allowedCommands.contains(cmd))
			return true;
		String[] split = cmd.split(" ");
		String cmds = "";
		for (int i = 0; i < split.length; i++) {
			cmds += split[i];
			if (plugin.allowedCommands.contains(cmds) || plugin.allowedCommands.contains(cmds + " *"))
				return true;
			cmds += " ";
		}
		return false;
	}

	/*
	 * @EventHandler(priority = EventPriority.HIGHEST) public void
	 * onPbCommands(PlayerCommandPreprocessEvent event) { Player player =
	 * event.getPlayer(); String[] m = event.getMessage().split(" "); // basic
	 * commands if (m[0].equalsIgnoreCase("/pb")) { if (m.length == 1) {
	 * plugin.cm.pbhelp(player); } else if (m[1].equalsIgnoreCase("help") ||
	 * m[1].equalsIgnoreCase("?")) { plugin.cm.pbhelp(player); } else if
	 * (m[1].equalsIgnoreCase("info")) { plugin.cm.pbinfo(player); } } }
	 */

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (plugin.chatnames) {
			Player player = event.getPlayer();
			if (Lobby.LOBBY.isMember(player)) {
				ChatColor farbe = Lobby.LOBBY.color();
				if (Lobby.isPlaying(player) || Lobby.isSpectating(player)) {
					Match match = plugin.mm.getMatch(player);
					// Color:
					if (match.isRed(player))
						farbe = Lobby.RED.color();
					else if (match.isBlue(player))
						farbe = Lobby.BLUE.color();
					else if (match.isSpec(player))
						farbe = Lobby.SPECTATE.color();
				}
				event.setMessage(farbe + event.getMessage());
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDead(PlayerDeathEvent event) {
		Player player = (Player) event.getEntity();
		if (Lobby.LOBBY.isMember(player)) {
			if (Lobby.isPlaying(player) || Lobby.isSpectating(player))
				mm.getMatch(player).left(player);
			plugin.leaveLobby(player, true, false, false);
			// drops?
			event.setDroppedExp(0);
			event.setKeepLevel(true);
			event.getDrops().removeAll(event.getDrops());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = (Player) event.getPlayer();
		plugin.pm.addPlayerAsync(player.getName());

		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

			@Override
			public void run() {
				if (plugin.autoLobby && plugin.autoTeam)
					plugin.cm.joinTeam(player, Lobby.RANDOM);
				else if (plugin.autoLobby)
					plugin.cm.joinLobbyPre(player);
			}
		}, 1L);

	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent event) {
		this.onPlayerDisconnect(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerKick(PlayerKickEvent event) {
		this.onPlayerDisconnect(event.getPlayer());
	}

	private void onPlayerDisconnect(Player player) {
		if (Lobby.LOBBY.isMember(player)) {
			if (Lobby.isPlaying(player) || Lobby.isSpectating(player))
				mm.getMatch(player).left(player);
			plugin.leaveLobby(player, true, true, true);
		}

	}

}
