package de.blablubbabc.paintball;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;


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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.BlockIterator;

import de.blablubbabc.paintball.extras.Airstrike;
import de.blablubbabc.paintball.extras.Ball;
import de.blablubbabc.paintball.extras.Flashbang;
import de.blablubbabc.paintball.extras.Gifts;
import de.blablubbabc.paintball.extras.Grenade;
import de.blablubbabc.paintball.extras.GrenadeM2;
import de.blablubbabc.paintball.extras.Mine;
import de.blablubbabc.paintball.extras.Orbitalstrike;
import de.blablubbabc.paintball.extras.Pumpgun;
import de.blablubbabc.paintball.extras.Rocket;
import de.blablubbabc.paintball.extras.Shotgun;
import de.blablubbabc.paintball.extras.Sniper;
import de.blablubbabc.paintball.extras.Turret;
import de.blablubbabc.paintball.utils.Log;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class EventListener implements Listener {
	private Paintball plugin;
	private MatchManager mm;
	
	private long lastSignUpdate = 0;

	// private HashMap<Player, String> chatMessages;

	public EventListener(Paintball pl) {
		plugin = pl;
		mm = plugin.mm;
		// chatMessages = new HashMap<Player, String>();
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
			else if (s.equals("hitquote"))
				s = "hq";
			else if (s.equals("airstrikes"))
				s = "as";
			else if (s.equals("money_spent"))
				s = "spent";

			if (l.equalsIgnoreCase("[PB " + s.toUpperCase() + "]") || l.equalsIgnoreCase("[PB R " + s.toUpperCase() + "]") || l.equalsIgnoreCase("[PB RANK]")) {
				if (!player.isOp() && !player.hasPermission("paintball.admin")) {
					event.setCancelled(true);
					player.sendMessage(Translator.getString("NO_PERMISSION"));
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onWorldChange(PlayerChangedWorldEvent event) {
		if (plugin.worldMode) {
			Player player = event.getPlayer();
			boolean fromPb = plugin.worldModeWorlds.contains(event.getFrom().getName());
			boolean toPb = plugin.worldModeWorlds.contains(event.getPlayer().getWorld().getName());
			if (!fromPb && toPb) {
				if (!Lobby.LOBBY.isMember(player)) {
					if (plugin.autoTeam)
						plugin.cm.joinTeam(player, Lobby.RANDOM);
					else plugin.cm.joinLobbyPre(player);
				}
			} else if (fromPb && !toPb) {
				plugin.leaveLobby(player, true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onSnowmanTrail(EntityBlockFormEvent event) {
		if (event.getEntity().getType() == EntityType.SNOWMAN) {
			if (Turret.getIsTurret((Snowman) event.getEntity()) != null) {
				event.setCancelled(true);
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
		Block block = event.getClickedBlock();
		if (block != null) {
			BlockState state = block.getState();
			if (state instanceof Sign) {
				Sign sign = (Sign) state;
				String l = ChatColor.stripColor(sign.getLine(0));

				if (l.equalsIgnoreCase("[PB RANK]")) {
					changeSign(event.getPlayer().getName(), sign, "points", true);
				} else {
					for (String stat : plugin.sql.sqlPlayers.statsList) {
						String s = stat;
						if (s.equals("teamattacks"))
							s = "ta";
						else if (s.equals("hitquote"))
							s = "hq";
						else if (s.equals("airstrikes"))
							s = "as";
						else if (s.equals("money_spent"))
							s = "spent";

						if (l.equalsIgnoreCase("[PB " + s.toUpperCase() + "]")) {
							changeSign(event.getPlayer().getName(), sign, stat, false);
							break;
						} else if (l.equalsIgnoreCase("[PB R " + s.toUpperCase() + "]")) {
							changeSign(event.getPlayer().getName(), sign, stat, true);
							break;
						}
					}
				}
			}
		}
	}

	private void changeSign(String player, Sign sign, String stat, boolean rank) {
		if ((System.currentTimeMillis() - lastSignUpdate) > (250)) {
			HashMap<String, String> vars = new HashMap<String, String>();
			vars.put("player", player);
			if (plugin.pm.exists(player)) {
				if (rank) {
					vars.put("value", "" + plugin.stats.getRank(player, stat));
				} else {
					if (stat.equals("hitquote") || stat.equals("kd")) {
						DecimalFormat dec = new DecimalFormat("###.##");
						float statF = (float) (Integer) plugin.pm.getStats(player).get(stat) / 100;
						vars.put("value", dec.format(statF));
					} else {
						vars.put("value", "" + plugin.pm.getStats(player).get(stat));
					}
				}
			} else
				vars.put("value", Translator.getString("NOT_FOUND"));
			sign.setLine(1, Translator.getString("SIGN_LINE_TWO", vars));
			sign.setLine(2, Translator.getString("SIGN_LINE_THREE", vars));
			sign.setLine(3, Translator.getString("SIGN_LINE_FOUR", vars));
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
				Match matchA = mm.getMatch(shooter);
				if (matchA != null) {
					if (event.getEntity() instanceof Player) {
						Player target = (Player) event.getEntity();
						if (shooter != target) {
							Match matchB = mm.getMatch(target);
							if (matchB != null ) {
								if (matchA == matchB) {
									if (!matchA.isSpec(shooter) && !matchA.isSpec(target) && matchA.isSurvivor(shooter) && matchA.isSurvivor(target)
											&& matchA.started) {
										// Geschoss?
										if (shot instanceof Snowball) {
											// match
											Ball ball = Ball.getBall(shot.getEntityId(), shooter.getName(), false);
											if (ball != null) {
												matchA.hitSnow(target, shooter, ball.getSource());
											}
										}
									}
								}
							}
						}
					} else if (event.getEntityType() == EntityType.SNOWMAN) {
						Snowman snowman = (Snowman) event.getEntity();
						Turret turret = Turret.getIsTurret(snowman);
						if (turret != null && matchA == turret.match && matchA.enemys(shooter, turret.player)) {
							turret.hit();
						}
					}
				}
			}
		} else if (plugin.allowMelee && event.getDamager() instanceof Player && event.getEntity() instanceof Player && event.getCause() == DamageCause.ENTITY_ATTACK) {
			Player attacker = (Player) event.getDamager();
			Player target = (Player) event.getEntity();
			if (attacker != target) {
				Match matchA = mm.getMatch(attacker);
				if (matchA != null) {
					Match matchT = mm.getMatch(target);
					if (matchT != null) {
						if (matchA == matchT) {
							if (matchA.enemys(attacker, target) && matchA.isSurvivor(attacker) && matchA.isSurvivor(target) && matchA.started) {
								if (target.getHealth() > plugin.meleeDamage)
									target.setHealth(target.getHealth() - plugin.meleeDamage);
								else {
									matchA.frag(target, attacker, Origin.MELEE);
								}
							}
						}	
					}
				}
			}
		}
	}

	/*@EventHandler(priority = EventPriority.NORMAL)
	public void onFireballExplosion(ExplosionPrimeEvent event) {
		Entity entity = event.getEntity();
		if (entity != null && entity.getType() == EntityType.FIREBALL) {
			Fireball fireball = (Fireball) entity;
			if (fireball.getShooter() instanceof Player) {
				if (Rocket.getRocket(fireball, ((Player)fireball.getShooter()).getName(), false) != null) {
					event.setCancelled(true);
				}
			}
		}
	}*/

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerShoot(ProjectileLaunchEvent event) {
		if (event.getEntity().getShooter() instanceof Player) {
			Player player = (Player) event.getEntity().getShooter();
			if (Lobby.LOBBY.isMember(player)) {
				if (event.getEntity().getType() != EntityType.SPLASH_POTION) event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEggThrow(PlayerEggThrowEvent event) {
		Egg egg = event.getEgg();
		if (egg.getShooter() instanceof Player) {
			Player player = (Player) egg.getShooter();
			if (Grenade.getGrenade(egg.getEntityId(), player.getName(), false) != null) {
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
						Gifts.giveGift(player, receiver);
					}
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		if (player.getGameMode() == GameMode.CREATIVE) return;
		ItemStack item = player.getItemInHand();
		if (item == null)
			return;
		
		if (Lobby.LOBBY.isMember(player)) {
			Match match = mm.getMatch(player);
			if (match != null && Lobby.isPlaying(player) && match.isSurvivor(player)) {
				if (item.getType() != Material.POTION) event.setUseItemInHand(Result.DENY);
				if (!match.started || match.isJustRespawned(player.getName())) return;
				Action action = event.getAction();
				
				switch (item.getType()) {
				case SNOW_BALL:
					//MARKER
					if (item.isSimilar(Ball.item)) {
						PlayerInventory inv = player.getInventory();
						if (match.setting_balls == -1 || inv.contains(Material.SNOW_BALL, 1)) {
							Snowball ball = (Snowball) player.getWorld().spawnEntity(player.getEyeLocation(), EntityType.SNOWBALL);
							player.getWorld().playSound(player.getEyeLocation(), Sound.CHICKEN_EGG_POP, 1.5F, 2F);
							ball.setShooter(player);
							// register snowball
							Ball.registerBall(ball, player.getName(), Origin.MARKER);
							// boosting:
							// test: no normalizing
							ball.setVelocity(player.getLocation().getDirection().normalize().multiply(plugin.speedmulti));
							// zählen
							match.addShots(player, 1);
							
							if (match.setting_balls != -1) {
								// -1 ball
								Utils.removeInventoryItems(inv, Ball.item, 1);
							}
						} else {
							player.playSound(player.getEyeLocation(), Sound.FIRE_IGNITE, 1F, 2F);
						}
					}
					break;
					
				case STICK:
					// AIRSTRIKE
					if (plugin.airstrike && item.isSimilar(Airstrike.item)) {
						if (Airstrike.marked(player.getName())) {
							if (Airstrike.getAirstrikeCountMatch() < plugin.airstrikeMatchLimit) {
								if (Airstrike.getAirstrikeCountPlayer(player.getName()) < plugin.airstrikePlayerLimit) {
									new Airstrike(player);
									// zählen
									match.airstrike(player);
									// remove stick if not infinite
									if (match.setting_airstrikes != -1) {
										if (item.getAmount() <= 1)
											player.setItemInHand(null);
										else {
											item.setAmount(item.getAmount() - 1);
										}
									}
								} else {
									player.sendMessage(Translator.getString("AIRSTRIKE_PLAYER_LIMIT_REACHED"));
								}

							} else {
								player.sendMessage(Translator.getString("AIRSTRIK_MATCH_LIMIT_REACHED"));
							}
						}
					}
					break;

				case BLAZE_ROD:
					// ORBITALSTRIKE
					if (plugin.orbitalstrike && item.isSimilar(Orbitalstrike.item)) {
						if (Orbitalstrike.marked(player.getName())) {
							if (Orbitalstrike.getOrbitalstrikeCountMatch() < plugin.orbitalstrikeMatchLimit) {
								if (Orbitalstrike.getOrbitalstrikeCountPlayer(player.getName()) < plugin.orbitalstrikePlayerLimit) {
									new Orbitalstrike(player, match);
									// remove stick if not infinite
									if (item.getAmount() <= 1)
										player.setItemInHand(null);
									else {
										item.setAmount(item.getAmount() - 1);
									}
								} else {
									player.sendMessage(Translator.getString("ORBITALSTRIKE_PLAYER_LIMIT_REACHED"));
								}

							} else {
								player.sendMessage(Translator.getString("ORBITALSTRIKE_MATCH_LIMIT_REACHED"));
							}
						}
					}
					break;

				case EGG:
					// GRENADE
					if (plugin.grenade && item.isSimilar(Grenade.item)) {
						PlayerInventory inv = player.getInventory();
						if (match.setting_grenades == -1 || inv.containsAtLeast(Grenade.item,  1)) {
							player.sendMessage(Translator.getString("GRENADE_THROW"));
							player.getWorld().playSound(player.getLocation(), Sound.SILVERFISH_IDLE, 1.5F, 1F);
							Egg egg = (Egg) player.getWorld().spawnEntity(player.getEyeLocation(), EntityType.EGG);
							egg.setShooter(player);
							// boosting:
							egg.setVelocity(player.getLocation().getDirection().multiply(plugin.grenadeSpeed));
							Grenade.registerGrenade(egg, player.getName(), Origin.GRENADE);
							// zählen
							match.grenade(player);
							if (match.setting_grenades != -1) {
								// -1 egg
								Utils.removeInventoryItems(inv, Grenade.item, 1);
							}
						} else {
							player.playSound(player.getEyeLocation(), Sound.FIRE_IGNITE, 1F, 2F);
						}
					}
					break;
					
				case SLIME_BALL:
					// GRENADE 2
					if (plugin.grenade2 && item.isSimilar(GrenadeM2.item)) {
						player.getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_THROW, 1.5F, 1F);
						player.sendMessage(Translator.getString("GRENADE_THROW"));
						ItemStack nadeItem = GrenadeM2.item.clone();
						ItemMeta meta = nadeItem.getItemMeta();
						meta.setDisplayName("GrenadeM2 " + Flashbang.getNext());
						nadeItem.setItemMeta(meta);
						Item nade = player.getWorld().dropItem(player.getEyeLocation(), nadeItem);
						nade.setVelocity(player.getLocation().getDirection().normalize().multiply(plugin.grenade2Speed));
						GrenadeM2.registerNade(nade, player.getName(), Origin.GRENADE2);
						if (item.getAmount() <= 1)
							player.setItemInHand(null);
						else {
							item.setAmount(item.getAmount() - 1);
							player.setItemInHand(item);
						}
					}
					break;
					
				case GHAST_TEAR:
					// FLASHBANG
					if (plugin.flashbang && item.isSimilar(Flashbang.item)) {
						player.getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_THROW, 1.5F, 1F);
						ItemStack nadeItem = Flashbang.item.clone();
						ItemMeta meta = nadeItem.getItemMeta();
						meta.setDisplayName("Flashbang " + Flashbang.getNext());
						nadeItem.setItemMeta(meta);
						Item nade = player.getWorld().dropItem(player.getEyeLocation(), nadeItem);
						nade.setVelocity(player.getLocation().getDirection().normalize().multiply(plugin.flashbangSpeed));
						Flashbang.registerNade(nade, player.getName(), Origin.FLASHBANG);
						if (item.getAmount() <= 1)
							player.setItemInHand(null);
						else {
							item.setAmount(item.getAmount() - 1);
							player.setItemInHand(item);
						}
					}
					break;

				case SPECKLED_MELON:
					// SHOTGUN
					if (plugin.shotgun && item.isSimilar(Shotgun.item)) {
						PlayerInventory inv = player.getInventory();
						if (inv.containsAtLeast(Ball.item, plugin.shotgunAmmo)) {
							Utils.removeInventoryItems(inv, Ball.item, plugin.shotgunAmmo);
							match.addShots(player, 15);
							Shotgun.shoot(player);
						} else {
							player.playSound(player.getEyeLocation(), Sound.FIRE_IGNITE, 1F, 2F);
						}
					}
					break;
					
				case STONE_AXE:
					// PUMPGUN
					if (plugin.pumpgun && item.isSimilar(Pumpgun.item)) {
						PlayerInventory inv = player.getInventory();
						if (inv.containsAtLeast(Ball.item, plugin.pumpgunAmmo)) {
							Utils.removeInventoryItems(inv, Ball.item, plugin.pumpgunAmmo);
							match.addShots(player, plugin.pumpgunBullets);
							Pumpgun.shoot(player);
						} else {
							player.playSound(player.getEyeLocation(), Sound.FIRE_IGNITE, 1F, 2F);
						}
					}
					break;

				case DIODE:
					// ROCKET LAUNCHER
					if (plugin.rocket && item.isSimilar(Rocket.item)) {
						if (Rocket.getRocketCountMatch() < plugin.rocketMatchLimit) {
							if (Rocket.getRocketCountPlayer(player.getName()) < plugin.rocketPlayerLimit) {
								player.getWorld().playSound(player.getLocation(), Sound.SILVERFISH_IDLE, 1.5F, 1F);
								Fireball rocket = (Fireball) player.getWorld().spawnEntity(player.getEyeLocation(), EntityType.FIREBALL);
								rocket.setIsIncendiary(false);
								rocket.setYield(0F);
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
								player.sendMessage(Translator.getString("ROCKET_PLAYER_LIMIT_REACHED"));
							}
						} else {
							player.sendMessage(Translator.getString("ROCKET_MATCH_LIMIT_REACHED"));
						}
					}
					break;

				case CARROT_STICK:
					// SNIPER
					if (plugin.sniper && item.isSimilar(Sniper.item)) {
						if (action == Action.LEFT_CLICK_AIR) {
							Sniper.toggleZoom(player);
						} else if (action == Action.RIGHT_CLICK_AIR) {
							PlayerInventory inv = player.getInventory();
							if ((!plugin.sniperOnlyUseIfZooming || Sniper.isZooming(player))
								&& (match.setting_balls == -1 || inv.contains(Material.SNOW_BALL, 1))) {
								match.addShots(player, 1);
								Sniper.shoot(player);
								
								if (match.setting_balls != -1) {
									// -1 ball
									Utils.removeInventoryItems(inv, Ball.item, 1);
								}
							} else {
								player.playSound(player.getEyeLocation(), Sound.FIRE_IGNITE, 1F, 2F);
							}
						}
					}
					break;

				case CHEST:
					// GIFT
					if (plugin.giftsEnabled && item.isSimilar(Gifts.item)) {
						plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
							
							@Override
							public void run() {
								Gifts.unwrapGift(player);
							}
						}, 1L);
					}
					break;

				default:
					// no special item in hand
					break;
				}
				
				plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
					
					@Override
					public void run() {
						player.updateInventory();
					}
				}, 1L);

			}
		}
	}

	/*private boolean isAirClick(Action action) {
		return (action == Action.RIGHT_CLICK_AIR || action == Action.LEFT_CLICK_AIR);
	}*/
	
	/*@EventHandler(priority = EventPriority.NORMAL)
	public void onChunkUnload(ChunkUnloadEvent event) {
		for (Entity e : event.getChunk().getEntities()) {
			if (e.getType() == EntityType.SNOWBALL) {
				Snowball s = (Snowball) e;
				if (s.getShooter() instanceof Player) {
					Ball.getBall(s.getEntityId(), ((Player)s.getShooter()).getName(), true);
				}
			} else if (e.getType() == EntityType.EGG) {
				Egg egg = (Egg) e;
				if (egg.getShooter() instanceof Player) {
					Grenade.getGrenade(egg.getEntityId(), ((Player)egg.getShooter()).getName(), true);
				}
			}.
		}
	}*/

	@EventHandler(priority = EventPriority.NORMAL)
	public void onItemInHand(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();
		if (Lobby.LOBBY.isMember(player)) {
			// zooming?
			if (Sniper.isZooming(player))
				Sniper.setNotZooming(player);

			ItemStack item = player.getInventory().getItem(event.getNewSlot());
			
			if (plugin.airstrike) {
				Airstrike.handleItemInHand(player, item);
			}
			
			if (plugin.orbitalstrike) {
				Orbitalstrike.handleItemInHand(player, item);
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
				Ball ball = Ball.getBall(shot.getEntityId(), shooterName, true);
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
				Grenade nade = Grenade.getGrenade(shot.getEntityId(), shooter.getName(), true);
				if (nade != null) {
					Match match = mm.getMatch(shooter);
					if (match != null) {
						nade.explode(shot.getLocation(), shooter);	
					}
				}
			} else if (plugin.rocket && shot instanceof Fireball) {
				Rocket rocket = Rocket.getRocket(shot.getEntityId(), shooterName, true);
				if (rocket != null)
					rocket.die();
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player target = (Player) event.getEntity();
			Lobby team = Lobby.getTeam(target);
			if (team != null) {
				int damage = event.getDamage();
				event.setDamage(0);
				event.setCancelled(true);
				Match match = plugin.mm.getMatch(target);
				if (match != null && team != Lobby.SPECTATE && match.isSurvivor(target) && match.started) {
					if ((plugin.falldamage && event.getCause() == DamageCause.FALL) 
							|| (plugin.otherDamage && event.getCause() != DamageCause.FALL 
							&& event.getCause() != DamageCause.ENTITY_ATTACK && event.getCause() != DamageCause.PROJECTILE)) {
						if (target.getHealth() <= damage) {
							match.death(target);
						} else {
							event.setDamage(damage);
							event.setCancelled(false);
							
							//heal armor
							PlayerInventory inventory = target.getInventory();
							ItemStack[] armor = inventory.getArmorContents();

							for(int i = 0; i < armor.length; i++)
							{
							    if(armor[i] != null)
							        armor[i].setDurability((short) 0);
							}

							inventory.setArmorContents(armor);
							target.updateInventory();
							
						}
					}
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

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
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
				ItemStack item = player.getItemInHand();
				if (plugin.turret && block.getType() == Material.PUMPKIN && item.hasItemMeta() && item.getItemMeta().getDisplayName().equals(Translator.getString("WEAPON_TURRET"))) {
					// turret:
					if (Turret.getTurretCountMatch() < plugin.turretMatchLimit) {
						if (Turret.getTurrets(player.getName()).size() < plugin.turretPlayerLimit) {
							Snowman snowman = (Snowman) block.getLocation().getWorld().spawnEntity(block.getLocation(), EntityType.SNOWMAN);
							new Turret(player, snowman, plugin.mm.getMatch(player));
							if (item.getAmount() <= 1)
								player.setItemInHand(null);
							else {
								item.setAmount(item.getAmount() - 1);
								player.setItemInHand(item);
							}
						} else {
							player.sendMessage(Translator.getString("TURRET_PLAYER_LIMIT_REACHED"));
						}
					} else {
						player.sendMessage(Translator.getString("TURRET_MATCH_LIMIT_REACHED"));
					}

				} else if (plugin.mine && block.getType() == Material.FLOWER_POT && item.hasItemMeta() && item.getItemMeta().getDisplayName().equals(Translator.getString("WEAPON_MINE"))) {
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
							player.sendMessage(Translator.getString("MINE_PLAYER_LIMIT_REACHED"));
						}
					} else {
						player.sendMessage(Translator.getString("MINE_MATCH_LIMIT_REACHED"));
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
		int id = event.getItem().getEntityId();
		if (Flashbang.isNade(id) || GrenadeM2.isNade(id)) {
			event.setCancelled(true);
			return;
		}
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

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		// always allow pb commands:
		if (event.getMessage().startsWith("/pb")) {
			if (event.isCancelled()) event.setCancelled(false);
		} else {
			if (Lobby.LOBBY.isMember(event.getPlayer()) && (!plugin.allowedCommands.isEmpty() ? !isAllowedCommand(event.getMessage()) : true)) {
				if (!event.getPlayer().hasPermission("paintball.admin") && !event.getPlayer().isOp()) {
					event.getPlayer().sendMessage(Translator.getString("COMMAND_NOT_ALLOWED"));
					event.setCancelled(true);
				}
			} else if (plugin.checkBlacklist && isBlacklistedCommand(event.getMessage())) {
				if (!plugin.blacklistAdminOverride && !event.getPlayer().hasPermission("paintball.admin") && !event.getPlayer().isOp()) {
					event.getPlayer().sendMessage(Translator.getString("COMMAND_BLACKLISTED"));
					event.setCancelled(true);
				}
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

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = (Player) event.getEntity();
		if (plugin.leaveLobby(player, true)) {
			// drops?
			event.setDroppedExp(0);
			event.setKeepLevel(false);
			event.getDrops().clear();
			Log.severe("WARNING: IllegalState! A player died while playing paintball. Report this to blablubbabc");
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

		// notify admins on update:
		if (plugin.needsUpdate && player.hasPermission("paintball.admin")) {
			player.sendMessage(ChatColor.DARK_PURPLE + "There is a new version of Paintball available! Check out the bukkit dev page: " + ChatColor.WHITE + "http://dev.bukkit.org/bukkit-mods/paintball_pure_war/");
		}
		
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
		plugin.leaveLobby(player, true);

	}

}
