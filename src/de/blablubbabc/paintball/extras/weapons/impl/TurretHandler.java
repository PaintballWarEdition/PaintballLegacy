package de.blablubbabc.paintball.extras.weapons.impl;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.extras.weapons.Ball;
import de.blablubbabc.paintball.extras.weapons.Gadget;
import de.blablubbabc.paintball.extras.weapons.GadgetManager;
import de.blablubbabc.paintball.extras.weapons.WeaponHandler;
import de.blablubbabc.paintball.utils.Translator;

public class TurretHandler extends WeaponHandler implements Listener {
	
	private GadgetManager gadgetHandler = new GadgetManager();
	private static Double[][] table;
	private static int ySize;
	
	private Location nextTurretSpawn = null;
	
	public TurretHandler(int customItemTypeID, boolean useDefaultType) {
		super(customItemTypeID, useDefaultType);
		calculateTable(Paintball.instance.turretAngleMin, Paintball.instance.turretAngleMax, Paintball.instance.turretTicks, Paintball.instance.turretXSize, Paintball.instance.turretYSize, Paintball.instance.speedmulti);
		Paintball.instance.getServer().getPluginManager().registerEvents(this, Paintball.instance);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if (nextTurretSpawn != null && event.getEntityType() == EntityType.SNOWMAN && event.getLocation().equals(nextTurretSpawn)) {
			event.setCancelled(false);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onSnowmanTrail(EntityBlockFormEvent event) {
		if (event.getEntity().getType() == EntityType.SNOWMAN) {
			if (gadgetHandler.isGadget(event.getEntity())) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntityType() == EntityType.SNOWMAN) {
			Gadget turret = gadgetHandler.getGadget(event.getEntity(), true);
			if (turret != null) {
				turret.dispose(false, false);
			}
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
	protected void cleanUp(Match match, String playerName) {
		gadgetHandler.cleanUp(match, playerName);
	}

	@Override
	protected void cleanUp(Match match) {
		gadgetHandler.cleanUp(match);
	}

	@Override
	protected void onBlockPlace(Player player, Block block, Match match) {
		if (Paintball.instance.turret && block.getType() == Material.PUMPKIN) {
			ItemStack itemInHand = player.getItemInHand();
			if (itemInHand.isSimilar(getItem())) {
				String playerName = player.getName();
				if (gadgetHandler.getMatchGadgetCount(match) < Paintball.instance.turretMatchLimit) {
					if (gadgetHandler.getPlayerGadgetCount(match, playerName) < Paintball.instance.turretPlayerLimit) {
						Location spawnLoc = block.getLocation();
						nextTurretSpawn = spawnLoc;
						Snowman snowman = (Snowman) block.getLocation().getWorld().spawnEntity(spawnLoc, EntityType.SNOWMAN);
						nextTurretSpawn = null;
						new Turret(gadgetHandler, match, player, snowman);
						if (itemInHand.getAmount() <= 1) {
							player.setItemInHand(null);
						} else {
							itemInHand.setAmount(itemInHand.getAmount() - 1);
							player.setItemInHand(itemInHand);
						}
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
	protected void onDamagedByEntity(EntityDamageByEntityEvent event, Entity damagedEntity, Match match, Player attacker) {
		if (damagedEntity.getType() == EntityType.SNOWMAN) {
			Gadget turretGadget = gadgetHandler.getGadget(damagedEntity, false);
			if (turretGadget != null && match == turretGadget.getMatch()) {
				Turret turret = (Turret) turretGadget;
				if (match.enemys(attacker, turret.getOwner())) {
					turret.hit();
					event.setCancelled(true);
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
	

	private class Turret extends Gadget {

		private final Snowman entity;
		private final Player player;
		
		private int tickTask = -1;
		private int salveTask = -1;

		private int cooldown;
		private Player target = null;
		private int salve;
		private int lives;
		
		private Turret(GadgetManager gadgetHandler, Match match, Player player, Snowman entity) {
			super(gadgetHandler, match, player.getName());
			this.entity = entity;
			this.player = player;
			this.cooldown = Paintball.instance.turretCooldown;
			this.salve = Paintball.instance.turretSalve;
			this.lives = Paintball.instance.turretLives;
			this.tick();
		}

		@Override
		protected boolean isSimiliar(Entity entity) {
			return entity.getEntityId() == this.entity.getEntityId();
		}

		@Override
		public Origin getOrigin() {
			return Origin.TURRET;
		}
		
		private void tick() {
			tickTask = Paintball.instance.getServer().getScheduler()
					.scheduleSyncDelayedTask(Paintball.instance, new Runnable() {

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
					}, 20L);
		}
		
		public Player getOwner() {
			return player;
		}

		private Player searchTarget(int maxRadius, int instantRadius) {
			Vector entVec = entity.getLocation().toVector();
			Player nearest = null;
			double distance = 0;
			for (Player p : match.getEnemyTeam(player)) {
				if (match.isSurvivor(p)) {
					Location ploc = p.getLocation();
					if (ploc.getWorld().equals(entity.getWorld())) {
						Vector targetVec = ploc.toVector()
								.add(new Vector(0, 1, 0));
						Vector dir = targetVec.clone().subtract(entVec).normalize();
						Vector dir2 = new Vector(dir.getX(), 0, dir.getZ()).normalize();
						if (entity.hasLineOfSight(p)
								&& canBeShoot(entVec.clone().add(new Vector(0, 2, 0))
										.add(dir2), targetVec.clone(), dir2.clone())) {
							double dist2 = entity.getLocation().distance(
									ploc);
							if (dist2 <= instantRadius) {
								nearest = p;
								distance = dist2;
								break;
							} else if (dist2 <= maxRadius) {
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
			salveTask = Paintball.instance.getServer().getScheduler()
					.scheduleSyncDelayedTask(Paintball.instance, new Runnable() {

						@Override
						public void run() {
							if (target != null && match.isSurvivor(target) ) {
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
									changed.setYaw(180 - (float) Math
											.toDegrees(Math.atan2(x, z)));
									changed.setPitch(90 - (float) Math
											.toDegrees(Math.acos(y)));
									entity.teleport(changed);

									entity.getWorld().playSound(entity.getEyeLocation(), Sound.IRONGOLEM_THROW, 2.0F, 1F);
									entity.getWorld().playSound(entity.getEyeLocation(), Sound.CHICKEN_EGG_POP, 2.0F, 1F);
									Snowball ball = entity.getLocation().getWorld().spawn(entity.getLocation().add(new Vector(0, 2, 0)).add(dir2), Snowball.class);
									ball.setShooter(player);
									
									Paintball.instance.weaponManager.getBallManager().addGadget(match, playerName, new Ball(Paintball.instance.weaponManager.getBallManager(), match, player, ball, Origin.TURRET));

									ball.setVelocity(getAimVector(entVec.clone().add(new Vector(0, 2, 0)).add(dir2), targetVec.clone(),dir2.clone()));

									salve--;
									shoot();
								} else {
									if (!entity.hasLineOfSight(target) || !canBeShoot(entVec.clone().add(new Vector(0, 2, 0)).add(dir2), targetVec.clone(), dir2.clone()))
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
					}, 5L);
		}

		@Override
		public void dispose(boolean removeFromGadgetHandlerTracking, boolean cheapEffects) {
			if (tickTask != -1) {
				Paintball.instance.getServer().getScheduler().cancelTask(tickTask);
			}
			if (salveTask != -1) {
				Paintball.instance.getServer().getScheduler().cancelTask(salveTask);
			}
			gadgetHandler.removeGadget(match, playerName, this);
			// some effects here:
			if (!cheapEffects && Paintball.instance.effects) {
				Location loc = entity.getLocation().add(0, 1, 0);
				World world = entity.getWorld();
				world.playSound(loc, Sound.IRONGOLEM_DEATH, 3L, 2L);
				world.playEffect(loc, Effect.SMOKE, 1);
				world.playEffect(loc, Effect.SMOKE, 2);
				world.playEffect(loc, Effect.SMOKE, 3);
				world.playEffect(loc, Effect.SMOKE, 5);
				world.playEffect(loc, Effect.SMOKE, 6);
				world.playEffect(loc, Effect.SMOKE, 7);
				world.playEffect(loc, Effect.SMOKE, 8);
				world.playEffect(loc, Effect.MOBSPAWNER_FLAMES, 4);
			}

			if (entity.isValid()) {
				entity.remove();
			}
			
			super.dispose(removeFromGadgetHandlerTracking, cheapEffects);
		}
		
		public void hit() {
			this.lives--;
			if (this.lives <= 0) {
				this.dispose(true, false);
			} else {
				entity.getWorld().playSound(entity.getEyeLocation(), Sound.IRONGOLEM_HIT, 3L, 2L);
			}
		}

		private boolean canBeShoot(Vector pos, Vector target, Vector dir) {
			int x = ((Double) target.clone().setY(0).distance(pos.clone().setY(0)))
					.intValue();
			int y = ((Double) (target.getY() - pos.getY())).intValue();
			if (x < table.length && (y + ySize) < 2 * ySize && (y + ySize) >= 0) {
				return (table[x][y + ySize] != null);
			} else
				return false;
		}

		private Vector getAimVector(Vector pos, Vector target, Vector dir) {
			Vector aim = dir.clone().normalize();
			int x = ((Double) target.clone().setY(0).distance(pos.clone().setY(0)))
					.intValue();
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
					yTarget = (y+ySize);
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
				// default angle 45°; tan 45 = 1.619
				tan = 1.619D;
			}
			return aim.setY(tan).normalize().multiply(Paintball.instance.speedmulti);
		}
		
	}

}
