package me.blablubbabc.paintball.extras;

import java.util.ArrayList;
import java.util.HashMap;

import me.blablubbabc.paintball.Match;
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

	/**
	 * Registers a new grenade.
	 * 
	 * @param fireball
	 * @param player
	 * @param source
	 */
	private static void registerRocket(Rocket rocket, String shooterName, Source source) {
		ArrayList<Rocket> prockets = rockets.get(shooterName);
		if (prockets == null) {
			prockets = new ArrayList<Rocket>();
			rockets.put(shooterName, prockets);
		}
		prockets.add(rocket);
		rocketCounter++;
	}

	/**
	 * Returns a Rocket object if the given Fireball is a grenade of the player OR null if not.
	 * @param fireball
	 * @param shooterName
	 * @param remove
	 * @return
	 */
	public static Rocket getRocket(Fireball fireball, String shooterName, boolean remove) {
		ArrayList<Rocket> prockets = rockets.get(shooterName);
		if (prockets == null)
			return null;
		Integer id = fireball.getEntityId();
		Rocket nade = getRocketFromList(prockets, id);
		if (remove && nade != null) {
			if (prockets.size() == 1) rockets.remove(shooterName);
			else prockets.remove(nade);
			rocketCounter--;
		}
		return nade;
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
	public final Match match;

	private int tickTask = -1;
	private int lives;
	private boolean exploded = false;

	public Rocket(Player player, Fireball rocket, Match match) {
		this.entity = rocket;
		this.player = player;
		this.match = match;
		this.lives = Paintball.instance.rocketRange*10;
		registerRocket(this, player.getName(), Source.ROCKET);
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
							die();
						}
					}
				}, 2L);
	}

	public synchronized void die() {
		if (!exploded)
			explode();
		if (tickTask != -1)
			Paintball.instance.getServer().getScheduler().cancelTask(tickTask);
		removeRocket(this);
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
		loc.getWorld().createExplosion(loc, 0.0F);
		for (Vector v : Utils.getDirections()) {
			moveExpSnow(loc.getWorld().spawn(loc, Snowball.class), v, player);
			moveExpSnow(loc.getWorld().spawn(loc, Snowball.class), v, player);
		}
	}

	private static void moveExpSnow(final Snowball s, Vector v, Player player) {
		s.setShooter(player);
		Vector v2 = v;
		v2.setX(v.getX() + Math.random() - Math.random());
		v2.setY(v.getY() + Math.random() - Math.random());
		v2.setZ(v.getZ() + Math.random() - Math.random());
		s.setVelocity(v2.normalize());
		Paintball.instance.getServer().getScheduler()
				.scheduleSyncDelayedTask(Paintball.instance, new Runnable() {

					@Override
					public void run() {
						if (!s.isDead() || s.isValid())
							s.remove();
					}
				}, (long) Paintball.instance.rocketTime);
	}

}
