package de.blablubbabc.paintball.extras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.Paintball;

public class Turret {

	public final static ItemStack item = ItemManager.setMeta(new ItemStack(Material.PUMPKIN));
	
	private static Double[][] table;
	private static int ySize;

	private static int turretCounter = 0;
	private static Map<String, ArrayList<Turret>> turrets = new HashMap<String, ArrayList<Turret>>();
	
	private static void addTurret(String shooterName, Turret turret) {
		ArrayList<Turret> pturrets = turrets.get(shooterName);
		if (pturrets == null) {
			pturrets = new ArrayList<Turret>();
			turrets.put(shooterName, pturrets);
		}
		pturrets.add(turret);
		turretCounter++;
	}
	
	private static void removeTurret(String shooterName, Turret turret) {
		ArrayList<Turret> pturrets = turrets.get(shooterName);
		if (pturrets != null) {
			if (pturrets.remove(turret)) {
				turretCounter--;
				if (pturrets.size() == 0) turrets.remove(shooterName);
			}
		}
	}
	
	public static int getTurretCountMatch() {
		return turretCounter;
	}
	
	public static ArrayList<Turret> getTurrets(String playerName) {
		ArrayList<Turret> pturrets = turrets.get(playerName);
		if (pturrets == null) {
			pturrets = new ArrayList<Turret>();
		}
		return pturrets;
	}
	
	public static Turret getIsTurret(Snowman snowman) {
		for (ArrayList<Turret> pturrets : turrets.values()) {
			for (Turret t : pturrets) {
				if (t.entity.equals(snowman)) {
					return t;
				}
			}
		}
		return null;
	}
	
	
	
	/*private static ArrayList<Turret> turrets = new ArrayList<Turret>();

	public static synchronized void addTurret(Turret turret) {
		turrets.add(turret);
	}

	public static synchronized void removeTurret(Turret turret) {
		turrets.remove(turret);
	}

	public static synchronized Turret isTurret(Snowman snowman) {
		for (Turret t : turrets) {
			if (t.entity.equals(snowman)) {
				return t;
			}
		}
		return null;
	}

	public static synchronized ArrayList<Turret> getTurrets(Match match) {
		ArrayList<Turret> list = new ArrayList<Turret>();
		for (Turret t : turrets) {
			if (t.match.equals(match)) {
				list.add(t);
			}
		}
		return list;
	}

	public static synchronized ArrayList<Turret> getTurrets(Player player) {
		ArrayList<Turret> list = new ArrayList<Turret>();
		for (Turret t : turrets) {
			if (t.player.equals(player)) {
				list.add(t);
			}
		}
		return list;
	}*/

	public final Match match;
	public final Snowman entity;
	public final Player player;
	public final String playerName;
	//public final Location loc;

	private int tickTask = -1;
	private int salveTask = -1;

	private int cooldown;
	private Player target = null;
	private int salve;
	private int lives;

	public Turret(Player player, Snowman turret, Match match) {
		this.entity = turret;
		this.player = player;
		this.playerName = player.getName();
		this.match = match;
		//this.loc = entity.getLocation();
		this.cooldown = Paintball.instance.turretCooldown;
		this.salve = Paintball.instance.turretSalve;
		this.lives = Paintball.instance.turretLives;
		addTurret(playerName, this);
		this.tick();
	}

	private void tick() {
		tickTask = Paintball.instance.getServer().getScheduler()
				.scheduleSyncDelayedTask(Paintball.instance, new Runnable() {

					@Override
					public void run() {
						if (target == null) {
							target = searchTarget(50, 15);
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

	private Player searchTarget(int maxRadius, int instantRadius) {
		Vector entVec = entity.getLocation().toVector();
		Player nearest = null;
		double distance = 0;
		for (Player p : match.getEnemyTeam(player)) {
			if (match.isSurvivor(p)) {
				Location ploc = p.getLocation();
				if(ploc.getWorld().equals(entity.getWorld())) {
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

								entity.getWorld().playSound(entity.getEyeLocation(), Sound.IRONGOLEM_THROW, 1.5F, 1F);
								entity.getWorld().playSound(entity.getEyeLocation(), Sound.CHICKEN_EGG_POP, 1.5F, 1F);
								Snowball s = entity
										.getLocation()
										.getWorld()
										.spawn(entity.getLocation()
												.add(new Vector(0, 2, 0))
												.add(dir2), Snowball.class);
								s.setShooter(player);
								Ball.registerBall(s, playerName, Origin.TURRET);

								s.setVelocity(getAimVector(
										entVec.clone().add(new Vector(0, 2, 0))
												.add(dir2), targetVec.clone(),
										dir2.clone()));

								salve--;
								shoot();
							} else {
								if (!entity.hasLineOfSight(target) || !canBeShoot(entVec.clone().add(new Vector(0, 2, 0))
										.add(dir2), targetVec.clone(), dir2.clone()))
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

	public void die(boolean effect) {
		if (tickTask != -1)
			Paintball.instance.getServer().getScheduler().cancelTask(tickTask);
		if (salveTask != -1)
			Paintball.instance.getServer().getScheduler().cancelTask(salveTask);
		removeTurret(playerName, this);
		// some effect here:
		if(effect) {
			Location loc = entity.getLocation().add(0, 1, 0);
			entity.getWorld().playSound(loc, Sound.IRONGOLEM_DEATH, 100L, 2L);
			entity.getWorld().playEffect(loc, Effect.SMOKE, 1);
			entity.getWorld().playEffect(loc, Effect.SMOKE, 2);
			entity.getWorld().playEffect(loc, Effect.SMOKE, 3);
			entity.getWorld().playEffect(loc, Effect.SMOKE, 5);
			entity.getWorld().playEffect(loc, Effect.SMOKE, 6);
			entity.getWorld().playEffect(loc, Effect.SMOKE, 7);
			entity.getWorld().playEffect(loc, Effect.SMOKE, 8);
			entity.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 4);
		}

		if (entity.isValid() && !entity.isDead())
			entity.remove();
	}
	
	public void hit() {
		this.lives--;
		if(this.lives <= 0) {
			this.die(true);
		} else {
			entity.getWorld().playSound(entity.getEyeLocation(), Sound.IRONGOLEM_HIT, 100L, 2L);
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

	public static void calculateTable(int angleMin, int angleMax, int ticks,
			int xSize, int ySize, Paintball plugin) {
		Turret.ySize = ySize;

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
			Double vx = Math.cos(a * Math.PI / 180.0) * plugin.speedmulti;
			Double vy = Math.sin(a * Math.PI / 180.0) * plugin.speedmulti;
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

}
