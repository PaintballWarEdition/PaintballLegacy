package me.blablubbabc.paintball.extras;

import java.util.ArrayList;
import me.blablubbabc.paintball.Match;
import me.blablubbabc.paintball.Paintball;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.util.Vector;

public class Turret {

	private static Double[][] table;

	private static ArrayList<Turret> turrets = new ArrayList<Turret>();

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
	}

	public final Match match;
	public final Snowman entity;
	public final Player player;
	public final Paintball plugin;
	public final Location loc;

	private int tickTask = -1;
	private int salveTask = -1;

	private int cooldown = 1;
	private Player target = null;
	private int salve = 30;

	public Turret(Player player, Snowman turret, Match match, Paintball plugin) {
		this.entity = turret;
		this.player = player;
		this.match = match;
		this.loc = entity.getLocation();
		this.plugin = plugin;
		addTurret(this);
		this.tick();
	}

	private void tick() {
		tickTask = plugin.getServer().getScheduler()
				.scheduleSyncDelayedTask(this.plugin, new Runnable() {

					@Override
					public void run() {
						if (target == null) {
							target = searchTarget(40,10);
						}

						if (cooldown == 0) {
							if (target != null) {
								tickTask = -1;
								shoot();
							} else {
								cooldown = 1;
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
				Vector targetVec = target.getLocation().toVector().add(new Vector(0, 1, 0));
				Vector dir = targetVec.subtract(entVec).normalize();
				Vector dir2 = new Vector(dir.getX(), 0, dir.getZ()).normalize();
				if (entity.hasLineOfSight(p) && canBeShoot(entVec.clone().add(new Vector(0, 2, 0)).add(dir2), targetVec.clone(), dir2)) {
					double dist2 = entity.getLocation().distance(
							p.getLocation());
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
		return nearest;
	}
	
	private void shoot() {
		salveTask = plugin.getServer().getScheduler()
				.scheduleSyncDelayedTask(this.plugin, new Runnable() {

					@Override
					public void run() {
						if (target != null && match.isSurvivor(target)) {
							if (salve > 0) {
								Vector targetVec = target.getLocation().toVector().add(new Vector(0, 1, 0));
								Vector entVec = entity.getLocation().toVector();
								Vector dir = targetVec.subtract(entVec).normalize();

								double x = dir.getX();
								double y = dir.getY();
								double z = dir.getZ();

								// Now change the angle
								Location changed = entity.getLocation().clone();
								changed.setYaw(180 - (float) Math.toDegrees(Math.atan2(x, z)));
								changed.setPitch(90 - (float) Math.toDegrees(Math.acos(y)));
								entity.teleport(changed);

								Vector dir2 = new Vector(dir.getX(), 0, dir.getZ()).normalize();

								if(canBeShoot(entVec.clone().add(new Vector(0, 2, 0)).add(dir2), targetVec.clone(), dir2)) {
									Snowball s = entity.getLocation().getWorld().spawn(entity.getLocation().add(new Vector(0, 2, 0)).add(dir2), Snowball.class);
									s.setShooter(player);

									// Vector aim =
									// calculateVector(entVec.getY()+2.0,
									// targetVec.getY(), plugin.speedmulti,
									// entity.getLocation().distance(target.getLocation()),
									// dir2.clone());

									s.setVelocity(getAimVector(entVec.clone().add(new Vector(0, 2, 0)).add(dir2), targetVec.clone(), dir2.clone()));

									salve--;
								}
								shoot();
							} else {
								if (!entity.hasLineOfSight(target))
									target = null;
								salve = 7;
								salveTask = -1;
								tick();
							}
						} else {
							target = null;
							salve = 7;
							salveTask = -1;
							tick();
						}
					}
				}, 5L);
	}

	public void die() {
		if (tickTask != -1)
			plugin.getServer().getScheduler().cancelTask(tickTask);
		if (salveTask != -1)
			plugin.getServer().getScheduler().cancelTask(salveTask);
		removeTurret(this);
		// some effect here:
		entity.getWorld().playEffect(entity.getLocation().add(0, 1, 0),
				Effect.SMOKE, 1);
		entity.getWorld().playEffect(entity.getLocation().add(0, 1, 0),
				Effect.SMOKE, 2);
		entity.getWorld().playEffect(entity.getLocation().add(0, 1, 0),
				Effect.SMOKE, 3);
		entity.getWorld().playEffect(entity.getLocation().add(0, 1, 0),
				Effect.SMOKE, 5);
		entity.getWorld().playEffect(entity.getLocation().add(0, 1, 0),
				Effect.SMOKE, 6);
		entity.getWorld().playEffect(entity.getLocation().add(0, 1, 0),
				Effect.SMOKE, 7);
		entity.getWorld().playEffect(entity.getLocation().add(0, 1, 0),
				Effect.SMOKE, 8);
		entity.getWorld().playEffect(entity.getLocation().add(0, 1, 0),
				Effect.MOBSPAWNER_FLAMES, 4);

		if (entity.isValid() && !entity.isDead())
			entity.remove();
	}
	
	private boolean canBeShoot(Vector pos, Vector target, Vector dir) {
		int x = ((Double) target.clone().setY(0).distance(pos.clone().setY(0))).intValue();
		int y = ((Double) (target.getY() - pos.getY()) ).intValue();
		
		return (table[x][y] != null);
	}
	
	private Vector getAimVector(Vector pos, Vector target, Vector dir) {
		Vector aim = new Vector(dir.getX(),0,dir.getZ()).normalize();
		if(canBeShoot(pos, target, dir)) {
			int x = ((Double) target.clone().setY(0).distance(pos.clone().setY(0))).intValue();
			int y = ((Double) (target.getY() - pos.getY()) ).intValue();
			Double tan = table[x][y];
			aim.setY(tan).normalize().multiply(plugin.speedmulti);
		}
		return aim;
	}

	public static void calculateTable(int angleMin, int angleMax, int ticks,
			int xSize, int ySize, Paintball plugin) {
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
				if (tx < table.length && (ty + ySize) < 2 * ySize)

					table[tx][ty] = tan;
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
