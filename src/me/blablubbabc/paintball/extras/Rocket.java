package me.blablubbabc.paintball.extras;

import java.util.ArrayList;
import me.blablubbabc.paintball.Paintball;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

public class Rocket {
	private static ArrayList<Rocket> rockets = new ArrayList<Rocket>();

	public static synchronized void addRocket(Rocket rocket) {
		rockets.add(rocket);
	}

	public static synchronized void removeRocket(Rocket rocket) {
		rockets.remove(rocket);
	}

	public static synchronized Rocket isRocket(Fireball fireball) {
		for (Rocket r : rockets) {
			if (r.entity.equals(fireball)) {
				return r;
			}
		}
		return null;
	}

	public static synchronized ArrayList<Rocket> getRockets(Player player) {
		ArrayList<Rocket> list = new ArrayList<Rocket>();
		for (Rocket r : rockets) {
			if (r.player.equals(player)) {
				list.add(r);
			}
		}
		return list;
	}

	public final Fireball entity;
	public final Player player;
	public final Paintball plugin;

	private int tickTask = -1;
	private int lives;
	private boolean exploded = false;

	public Rocket(Player player, Fireball rocket, Paintball plugin) {
		this.entity = rocket;
		this.player = player;
		this.plugin = plugin;
		this.lives = plugin.rocketRange*10;
		addRocket(this);
		tick();
	}

	private void tick() {
		tickTask = plugin.getServer().getScheduler()
				.scheduleSyncDelayedTask(this.plugin, new Runnable() {

					@Override
					public void run() {
						if (entity.isValid() && !entity.isDead() && lives > 0) {
							lives--;

							if (plugin.effects && lives < plugin.rocketRange-1) {
								Location loc = entity.getLocation();
								// effect
								entity.getWorld().playEffect(loc, Effect.SMOKE,
										1);
								entity.getWorld().playEffect(loc, Effect.SMOKE,
										2);
								entity.getWorld().playEffect(loc, Effect.SMOKE,
										3);
								entity.getWorld().playEffect(loc, Effect.SMOKE,
										4);
								entity.getWorld().playEffect(loc, Effect.SMOKE,
										5);
								entity.getWorld().playEffect(loc, Effect.SMOKE,
										6);
								entity.getWorld().playEffect(loc, Effect.SMOKE,
										7);
								entity.getWorld().playEffect(loc, Effect.SMOKE,
										8);
								entity.getWorld().playEffect(loc,
										Effect.MOBSPAWNER_FLAMES, 4);
							}
							tick();
						} else {
							die();
						}
					}
				}, 2L);
	}

	public synchronized void die() {
		if (!exploded)
			explode();
		if (tickTask != -1)
			plugin.getServer().getScheduler().cancelTask(tickTask);
		removeRocket(this);
		// some effect here:
		if (plugin.effects) {
			Location loc = entity.getLocation();
			// effect
			entity.getWorld().playEffect(loc, Effect.SMOKE, 1);
			entity.getWorld().playEffect(loc, Effect.SMOKE, 2);
			entity.getWorld().playEffect(loc, Effect.SMOKE, 3);
			entity.getWorld().playEffect(loc, Effect.SMOKE, 4);
			entity.getWorld().playEffect(loc, Effect.SMOKE, 5);
			entity.getWorld().playEffect(loc, Effect.SMOKE, 6);
			entity.getWorld().playEffect(loc, Effect.SMOKE, 7);
			entity.getWorld().playEffect(loc, Effect.SMOKE, 8);
			entity.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 1);
			entity.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 2);
			entity.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 3);
			entity.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 4);
			entity.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 5);
			entity.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 6);
			entity.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 7);
			entity.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 8);
		}

		if (entity.isValid() && !entity.isDead())
			entity.remove();
	}

	private void explode() {
		exploded = true;
		Location loc = entity.getLocation();
		loc.getWorld().createExplosion(loc, 0.0F);
		for (Vector v : directions()) {
			moveExpSnow(loc.getWorld().spawn(loc, Snowball.class), v, player,
					plugin);
			moveExpSnow(loc.getWorld().spawn(loc, Snowball.class), v, player,
					plugin);
		}
	}

	private static void moveExpSnow(final Snowball s, Vector v, Player player,
			Paintball plugin) {
		s.setShooter(player);
		Vector v2 = v;
		v2.setX(v.getX() + Math.random() - Math.random());
		v2.setY(v.getY() + Math.random() - Math.random());
		v2.setZ(v.getZ() + Math.random() - Math.random());
		s.setVelocity(v2.normalize().multiply(1));
		plugin.getServer().getScheduler()
				.scheduleSyncDelayedTask(plugin, new Runnable() {

					@Override
					public void run() {
						if (!s.isDead())
							s.remove();
					}
				}, (long) plugin.grenadeTime);
	}

	private static ArrayList<Vector> directions() {
		ArrayList<Vector> vectors = new ArrayList<Vector>();
		// alle Richtungen
		vectors.add(new Vector(1, 0, 0));
		vectors.add(new Vector(0, 1, 0));
		vectors.add(new Vector(0, 0, 1));
		vectors.add(new Vector(1, 1, 0));
		vectors.add(new Vector(1, 0, 1));
		vectors.add(new Vector(0, 1, 1));
		vectors.add(new Vector(0, 0, 0));
		vectors.add(new Vector(1, 1, 1));
		vectors.add(new Vector(-1, -1, -1));
		vectors.add(new Vector(-1, 0, 0));
		vectors.add(new Vector(0, -1, 0));
		vectors.add(new Vector(0, 0, -1));
		vectors.add(new Vector(-1, -1, 0));
		vectors.add(new Vector(-1, 0, -1));
		vectors.add(new Vector(0, -1, -1));
		vectors.add(new Vector(1, -1, 0));
		vectors.add(new Vector(1, 0, -1));
		vectors.add(new Vector(0, 1, -1));
		vectors.add(new Vector(-1, 1, 0));
		vectors.add(new Vector(-1, 0, 1));
		vectors.add(new Vector(0, -1, 1));
		vectors.add(new Vector(1, 1, -1));
		vectors.add(new Vector(1, -1, 1));
		vectors.add(new Vector(-1, 1, 1));
		vectors.add(new Vector(1, -1, -1));
		vectors.add(new Vector(-1, 1, -1));
		vectors.add(new Vector(-1, -1, 1));

		return vectors;
	}

}
