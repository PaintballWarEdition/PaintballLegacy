package me.blablubbabc.paintball.extras;

import java.util.ArrayList;
import java.util.HashMap;

import me.blablubbabc.paintball.Paintball;
import me.blablubbabc.paintball.Source;
import me.blablubbabc.paintball.Utils;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

public class Rocket {
	
	private static int rocketCounter = 0;
	private static HashMap<String, ArrayList<Rocket>> rockets = new HashMap<String, ArrayList<Rocket>>();

	private static void registerRocket(Rocket rocket, String shooterName) {
		ArrayList<Rocket> prockets = rockets.get(shooterName);
		if (prockets == null) {
			prockets = new ArrayList<Rocket>();
			rockets.put(shooterName, prockets);
		}
		prockets.add(rocket);
		rocketCounter++;
	}

	public static Rocket getRocket(int id, String shooterName, boolean remove) {
		ArrayList<Rocket> prockets = rockets.get(shooterName);
		if (prockets == null)
			return null;
		Rocket rocket = getRocketFromList(prockets, id);
		if (remove && rocket != null) {
			if (prockets.remove(rocket)) {
				rocketCounter--;
				if (prockets.size() == 0) rockets.remove(shooterName);
			}
		}
		return rocket;
	}
	
	private static Rocket getRocketFromList(ArrayList<Rocket> prockets, int id) {
		for (Rocket rocket : prockets) {
			if (rocket.getId() == id)
				return rocket;
		}
		return null;
	}
	
	public static int getRocketCountMatch() {
		return rocketCounter;
	}
	
	public static int getRocketCountPlayer(String shooterName) {
		ArrayList<Rocket> prockets = rockets.get(shooterName);
		if (prockets == null) return 0;
		else return prockets.size();
	}
	
	
	/*private static ArrayList<Rocket> rockets = new ArrayList<Rocket>();

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
	
	public static synchronized ArrayList<Rocket> getRockets(Match match) {
		ArrayList<Rocket> list = new ArrayList<Rocket>();
		for (Rocket r : rockets) {
			if (r.match.equals(match)) {
				list.add(r);
			}
		}
		return list;
	}*/

	public final Fireball entity;
	public final Player player;

	private int tickTask = -1;
	private int lives;
	private boolean exploded = false;

	public Rocket(Player player, Fireball rocket) {
		this.entity = rocket;
		this.player = player;
		this.lives = Paintball.instance.rocketRange*10;
		registerRocket(this, player.getName());
		tick();
	}
	
	int getId() {
		return entity.getEntityId();
	}

	private void tick() {
		tickTask = Paintball.instance.getServer().getScheduler()
				.scheduleSyncDelayedTask(Paintball.instance, new Runnable() {

					@Override
					public void run() {
						if (entity.isValid() && !entity.isDead() && lives > 0) {
							lives--;

							if (Paintball.instance.effects) {
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
										6, 50);
								entity.getWorld().playEffect(loc, Effect.SMOKE,
										7);
								entity.getWorld().playEffect(loc, Effect.SMOKE,
										8);
								entity.getWorld().playEffect(loc,
										Effect.MOBSPAWNER_FLAMES, 4);
							}
							tick();
						} else {
							// remove rocket:
							getRocket(entity.getEntityId(), player.getName(), true);
							die();
						}
					}
				}, 2L);
	}

	public void die() {
		if (!exploded) {
			explode();
		}
		if (tickTask != -1)
			Paintball.instance.getServer().getScheduler().cancelTask(tickTask);
		// some effect here:
		if (Paintball.instance.effects) {
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
		loc.getWorld().createExplosion(loc, -1F, false);
		final String shooterName = player.getName();
		for (Vector v : Utils.getDirections()) {
			final Snowball s = loc.getWorld().spawn(loc, Snowball.class);
			s.setShooter(player);
			Ball.registerBall(s, shooterName, Source.ROCKET);
			Vector v2 = v.clone();
			v2.setX(v.getX() + Math.random() - Math.random());
			v2.setY(v.getY() + Math.random() - Math.random());
			v2.setZ(v.getZ() + Math.random() - Math.random());
			s.setVelocity(v2.normalize());
			Paintball.instance.getServer().getScheduler()
					.scheduleSyncDelayedTask(Paintball.instance, new Runnable() {

						@Override
						public void run() {
							if (!s.isDead() || s.isValid())
								Ball.getBall(s.getEntityId(), shooterName, true);
								s.remove();
						}
					}, (long) Paintball.instance.rocketTime);
		}
	}

}
