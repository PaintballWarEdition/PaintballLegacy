package de.blablubbabc.paintball.gadgets.handlers;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import de.blablubbabc.paintball.FragInformations;
import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.gadgets.Gadget;
import de.blablubbabc.paintball.gadgets.GadgetManager;
import de.blablubbabc.paintball.gadgets.WeaponHandler;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class TurretHandler extends WeaponHandler implements Listener {
	
	private GadgetManager gadgetManager = new GadgetManager();
	
	private static Double[][] table;
	private static int ySize;
	
	private Location nextTurretSpawn = null;
	
	public TurretHandler(int customItemTypeID, boolean useDefaultType) {
		super("Turret", customItemTypeID, useDefaultType, new Origin() {
			
			@Override
			public String getKillMessage(FragInformations fragInfo) {
				return Translator.getString("WEAPON_FEED_TURRET", getDefaultVariablesMap(fragInfo));
			}
		});
		
		calculateTable(Paintball.instance.turretAngleMin, Paintball.instance.turretAngleMax, Paintball.instance.turretTicks, Paintball.instance.turretXSize, Paintball.instance.turretYSize, Paintball.instance.speedmulti);
		Paintball.instance.getServer().getPluginManager().registerEvents(this, Paintball.instance);
	}
	
	public Turret createTurret(Match match, Player player, LivingEntity entity, Origin origin) {
		return new Turret(gadgetManager, match, player, entity, origin);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if (nextTurretSpawn != null && event.getLocation().equals(nextTurretSpawn)) {
			event.setCancelled(false);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onSnowmanTrail(EntityBlockFormEvent event) {
		if (gadgetManager.isGadget(event.getEntity())) {
			// client might still show snow..
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDeath(EntityDeathEvent event) {
		Gadget turret = gadgetManager.getGadget(event.getEntity());
		if (turret != null) {
			((Turret)turret).die();
		}
	}

	@Override
	protected int getDefaultItemTypeID() {
		return Material.PUMPKIN.getId();
	}

	@Override
	protected ItemStack setItemMeta(ItemStack itemStack) {
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(Translator.getString("WEAPON_TURRET"));
		itemStack.setItemMeta(meta);
		return itemStack;
	}

	@Override
	public void cleanUp(Match match, String playerName) {
		gadgetManager.cleanUp(match, playerName);
	}

	@Override
	public void cleanUp(Match match) {
		gadgetManager.cleanUp(match);
	}

	@Override
	protected void onInteract(PlayerInteractEvent event, Match match) {
		
	}
	
	@Override
	protected void onBlockPlace(BlockPlaceEvent event, Match match) {
		Block block = event.getBlockPlaced();
		if (Paintball.instance.turret && block.getType() == Material.PUMPKIN) {
			Player player = event.getPlayer();
			ItemStack itemInHand = player.getItemInHand();
			if (itemInHand == null) return;
			
			if (itemInHand.isSimilar(getItem())) {
				String playerName = player.getName();
				if (gadgetManager.getMatchGadgetCount(match) < Paintball.instance.turretMatchLimit) {
					if (gadgetManager.getPlayerGadgetCount(match, playerName) < Paintball.instance.turretPlayerLimit) {
						
						// check space:
						if (block.getRelative(BlockFace.UP).getType() != Material.AIR) {
							player.sendMessage(Translator.getString("GADGET_NOT_ENOUGH_SPACE"));
							return;
						}
						
						Location spawnLoc = block.getLocation();
						nextTurretSpawn = spawnLoc;
						Snowman snowman = (Snowman) block.getLocation().getWorld().spawnEntity(spawnLoc, EntityType.SNOWMAN);
						nextTurretSpawn = null;
						new Turret(gadgetManager, match, player, snowman, this.getWeaponOrigin());
						
						if (itemInHand.getAmount() <= 1) {
							player.setItemInHand(null);
						} else {
							itemInHand.setAmount(itemInHand.getAmount() - 1);
							player.setItemInHand(itemInHand);
						}
						Utils.updatePlayerInventoryLater(Paintball.instance, player);
					} else {
						player.sendMessage(Translator.getString("TURRET_PLAYER_LIMIT_REACHED"));
					}
				} else {
					player.sendMessage(Translator.getString("TURRET_MATCH_LIMIT_REACHED"));
				}
			}	
		}
	}
	
	@Override
	protected void onDamagedByEntity(EntityDamageByEntityEvent event, Match match, Player attacker) {
		Gadget turretGadget = gadgetManager.getGadget(event.getEntity());
		if (turretGadget != null) {
			event.setCancelled(true);
			if (match == turretGadget.getMatch()) {
				Turret turret = (Turret) turretGadget;
				if (match.enemys(attacker, turret.getOwner())) {
					turret.hit();
				}
			}
		}
	}
	
	private void calculateTable(int angleMin, int angleMax, int ticks,
			int xSize, int ySize, double speedmulti) {
		TurretHandler.ySize = ySize;

		// if ySize = 50 -> size = 2*50: y-Size in both directions, up and down:
		// 0=> 50
		table = new Double[xSize][2 * ySize];

		Double drag = 0.01D;
		Double g = 0.03D;
		Double vyMin = -3.0D;

		// default values
		for (int i = 0; i < table.length; i++) {
			for (int j = 0; j < table[i].length; j++) {
				table[i][j] = null;
			}
		}

		for (int a = angleMin; a <= angleMax; a++) {
			Double tan = Math.tan(a * Math.PI / 180.0);
			// t=0:
			Double vx = Math.cos(a * Math.PI / 180.0) * speedmulti;
			Double vy = Math.sin(a * Math.PI / 180.0) * speedmulti;
			Double x = 0.0D;
			Double y = 0.0D;

			for (int t = 1; t <= ticks; t++) {
				x += vx;
				y += vy;
				vx = vx * (1 - drag);
				if (vy > vyMin)
					vy = vy - g;
				vy = vy * (1 - drag);

				int tx = x.intValue();
				int ty = y.intValue();
				if (tx < table.length && (ty + ySize) < 2 * ySize
						&& (ty + ySize) >= 0) {
					table[tx][ty + ySize] = tan;
				}
			}
		}
		// 0 Location gets angle 0
		table[0][ySize] = 0.0D;

		// fill empty squares between max and min angle curve:
		for (int x = 0; x < xSize; x++) {
			int yMin = 0;
			int yMax = 2 * ySize - 1;
			// Find min:
			for (int y = 0; y < 2 * ySize; y++) {
				if (table[x][y] != null) {
					yMin = y;
					break;
				}
			}
			// Find max:
			for (int y = 2 * ySize - 1; y >= 0; y--) {
				if (table[x][y] != null) {
					yMax = y;
					break;
				}
			}
			// Fill empty squares between:
			for (int y = yMax - 1; y > yMin; y--) {
				if (table[x][y] == null) {
					// setze oberen Wert ein
					table[x][y] = table[x][y + 1];
				}
			}
		}
	}
	

	public class Turret extends Gadget {

		private final LivingEntity entity;
		private final Player player;
		
		private int tickTask = -1;
		private int salveTask = -1;

		private int cooldown;
		private Player target = null;
		private int salve;
		private int lives;
		
		private Turret(GadgetManager gadgetManager, Match match, Player player, LivingEntity entity, Origin origin) {
			super(gadgetManager, match, player.getName(), origin);
			
			this.entity = entity;
			this.player = player;
			this.cooldown = Paintball.instance.turretCooldown;
			this.salve = Paintball.instance.turretSalve;
			this.lives = Paintball.instance.turretLives;
			this.tick();
		}

		@Override
		public boolean isSimiliar(Entity entity) {
			return entity.getEntityId() == this.entity.getEntityId();
		}
		
		@Override
		public boolean isSimiliar(Location location) {
			return false;
		}
		
		private void tick() {
			tickTask = Paintball.instance.getServer().getScheduler().runTaskLater(Paintball.instance, new Runnable() {

				@Override
				public void run() {
					if (target == null) {
						target = searchTarget(Paintball.instance.turretXSize, 15);
					}

					if (cooldown == 0) {
						if (target != null) {
							tickTask = -1;
							shoot();
						} else {
							cooldown = Paintball.instance.turretCooldown;
							tick();
						}
					} else {
						cooldown--;
						tick();
					}
				}
			}, 20L).getTaskId();
		}
		
		/**
		 * Returns the creator of this turret.
		 * 
		 * @return the creator of the turret
		 */
		public Player getOwner() {
			return player;
		}

		//TODO less square roots
		private Player searchTarget(int maxRadius, int instantRadius) {
			Vector entVec = entity.getLocation().toVector();
			Player nearest = null;
			double distance = 0;
			int instantRadius2 = instantRadius * instantRadius;
			int maxRadius2 = maxRadius * maxRadius;
			for (Player p : match.getEnemyTeam(player)) {
				if (match.isSurvivor(p)) {
					Location ploc = p.getLocation();
					if (ploc.getWorld().equals(entity.getWorld())) {
						Vector targetVec = ploc.toVector().add(new Vector(0, 1, 0));
						Vector dir = targetVec.clone().subtract(entVec).normalize();
						Vector dir2 = new Vector(dir.getX(), 0, dir.getZ()).normalize();
						if (entity.hasLineOfSight(p) && canBeShoot(entVec.clone().add(new Vector(0, 2, 0)).add(dir2), targetVec.clone(), dir2.clone())) {
							double dist2 = entity.getLocation().distanceSquared(ploc);
							if (dist2 <= instantRadius2) {
								nearest = p;
								distance = dist2;
								break;
							} else if (dist2 <= maxRadius2) {
								if (nearest != null) {
									if (dist2 < distance) {
										nearest = p;
										distance = dist2;
									}
								} else {
									nearest = p;
									distance = dist2;
								}
							}
						}
					}
				}
			}
			return nearest;
		}

		private void shoot() {
			salveTask = Paintball.instance.getServer().getScheduler().runTaskLater(Paintball.instance, new Runnable() {

				@Override
				public void run() {
					if (target != null && match.isSurvivor(target)) {
						Vector targetVec = target.getLocation().toVector().add(new Vector(0, 1, 0));
						Vector entVec = entity.getLocation().toVector();
						Vector dir = targetVec.clone().subtract(entVec).normalize();
						Vector dir2 = new Vector(dir.getX(), 0, dir.getZ()).normalize();
						if (salve > 0) {

							double x = dir.getX();
							double y = dir.getY();
							double z = dir.getZ();

							// Now change the angle
							Location changed = entity.getLocation().clone();
							changed.setYaw(180 - (float) Math.toDegrees(Math.atan2(x, z)));
							changed.setPitch(90 - (float) Math.toDegrees(Math.acos(y)));
							entity.teleport(changed);

							entity.getWorld().playSound(entity.getEyeLocation(), Sound.IRONGOLEM_THROW, 2.0F, 1F);
							entity.getWorld().playSound(entity.getEyeLocation(), Sound.CHICKEN_EGG_POP, 2.0F, 1F);
							Snowball ball = (Snowball) entity.getLocation().getWorld().spawnEntity(entity.getLocation().add(new Vector(0, 2, 0)).add(dir2), EntityType.SNOWBALL);
							Player player = getOwner();
							ball.setShooter(player);

							Paintball.instance.weaponManager.getBallHandler().createBall(match, player, ball, getGadgetOrigin());

							ball.setVelocity(getAimVector(entVec.clone().add(new Vector(0, 2, 0)).add(dir2), targetVec.clone(), dir2.clone()));

							salve--;
							shoot();
						} else {
							if (!entity.hasLineOfSight(target)
									|| !canBeShoot(entVec.clone().add(new Vector(0, 2, 0)).add(dir2), targetVec.clone(), dir2.clone()))
								target = null;
							salve = Paintball.instance.turretSalve;
							salveTask = -1;
							tick();
						}
					} else {
						target = null;
						salve = Paintball.instance.turretSalve;
						salveTask = -1;
						tick();
					}
				}
			}, 5L).getTaskId();
		}
		
		public void die() {
			// some effect here:
			if (Paintball.instance.effects) {
				Location loc = entity.getLocation().add(0, 1, 0);
				World world = entity.getWorld();
				world.playSound(loc, Sound.IRONGOLEM_DEATH, 3L, 2L);
				for (int i = 1; i <= 8; i++) {
					world.playEffect(loc, Effect.SMOKE, i);
				}
				world.playEffect(loc, Effect.MOBSPAWNER_FLAMES, 4);
			}
			
			dispose(true);
		}
		
		@Override
		public void dispose(boolean removeFromGadgetHandlerTracking) {
			if (tickTask != -1) {
				Paintball.instance.getServer().getScheduler().cancelTask(tickTask);
			}
			
			if (salveTask != -1) {
				Paintball.instance.getServer().getScheduler().cancelTask(salveTask);
			}
			
			entity.remove();
			
			super.dispose(removeFromGadgetHandlerTracking);
		}
		
		public void hit() {
			this.lives--;
			if (this.lives <= 0) {
				die();
			} else {
				entity.getWorld().playSound(entity.getEyeLocation(), Sound.IRONGOLEM_HIT, 3L, 2L);
			}
		}

		private boolean canBeShoot(Vector pos, Vector target, Vector dir) {
			int x = ((Double) target.clone().setY(0).distance(pos.clone().setY(0))).intValue();
			int y = ((Double) (target.getY() - pos.getY())).intValue();
			if (x < table.length && (y + ySize) < 2 * ySize && (y + ySize) >= 0) {
				return (table[x][y + ySize] != null);
			} else {
				return false;
			}
		}

		private Vector getAimVector(Vector pos, Vector target, Vector dir) {
			Vector aim = dir.clone().normalize();
			int x = ((Double) target.clone().setY(0).distance(pos.clone().setY(0))).intValue();
			int y = ((Double) (target.getY() - pos.getY())).intValue();
			Double tan = null;

			if (canBeShoot(pos, target, dir)) {
				tan = table[x][y + ySize];
			} else {
				int yTarget;
				int xTarget;
				// find nearest y
				if ((y + ySize) >= 2 * ySize) {
					yTarget = 2 * ySize - 1;
				} else if ((y + ySize) < 0) {
					yTarget = 0;
				} else {
					yTarget = (y + ySize);
				}
				// find nearest x (x is always positiv because of direction change)
				if (x > table.length) {
					xTarget = 0;
				} else {
					xTarget = x;
				}

				// find max X for this y
				for (int tablex = table.length - 1; tablex >= 0; tablex--) {
					if (table[tablex][yTarget] != null) {
						tan = table[tablex][yTarget];
						break;
					}
				}
				if (tan == null) {
					// find max y for this x
					for (int tabley = 2 * ySize - 1; tabley >= 0; tabley--) {
						if (table[xTarget][tabley] != null) {
							tan = table[xTarget][tabley];
							break;
						}
					}
				}
				// shoot with nearly nearest anlge:
			}
			if (tan == null) {
				// default angle 45; tan 45 = 1.619
				tan = 1.619D;
			}
			return aim.setY(tan).normalize().multiply(Paintball.instance.speedmulti);
		}
		
	}

}
