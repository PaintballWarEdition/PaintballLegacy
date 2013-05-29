package de.blablubbabc.paintball.extras;

import java.util.ArrayList;
import java.util.HashMap;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.Utils;

public class Grenade {

	public final static ItemStack item = ItemManager.setMeta(new ItemStack(Material.EGG));
	
	private static HashMap<String, ArrayList<Grenade>> nades = new HashMap<String, ArrayList<Grenade>>();
	
	public static void registerGrenade(Egg egg, String shooterName, Origin source) {
		ArrayList<Grenade> pnades = nades.get(shooterName);
		if (pnades == null) {
			pnades = new ArrayList<Grenade>();
			nades.put(shooterName, pnades);
		}
		pnades.add(new Grenade(egg, source));
	}

	/**
	 * Returns a Grenade object if the given Egg is a grenade of the player OR null if not.
	 * @param egg
	 * @param shooterName
	 * @param remove
	 * @return
	 */
	public static Grenade getGrenade(int id, String shooterName, boolean remove) {
		ArrayList<Grenade> pnades = nades.get(shooterName);
		if (pnades == null)
			return null;
		Grenade nade = getGrenadeFromList(pnades, id);
		if (remove && nade != null) {
			if (pnades.remove(nade)) {
				if (pnades.size() == 0) nades.remove(shooterName);
			}
		}
		return nade;
	}
	
	private static Grenade getGrenadeFromList(ArrayList<Grenade> pnades, int id) {
		for (Grenade nade : pnades) {
			if (nade.getId() == id)
				return nade;
		}
		return null;
	}
	
	public static void clear() {
		for (String playerName : nades.keySet()) {
			ArrayList<Grenade> pnades = new ArrayList<Grenade>(nades.get(playerName));
			for (Grenade g : pnades) {
				g.remove();
			}
		}
		nades.clear();
	}

	/*public static void eggThrow(Player player, Egg egg) {
		String name = player.getName();
		ArrayList<Egg> eggs = nades.get(name);
		if (eggs == null)
			eggs = new ArrayList<Egg>();
		eggs.add(egg);
		nades.put(name, eggs);
	}

	public static void eggHit(Egg egg, Paintball plugin) {
		if (egg.getShooter() instanceof Player) {
			Player player = (Player) egg.getShooter();
			String name = player.getName();
			ArrayList<Egg> eggs = nades.get(name);
			if (eggs != null && eggs.contains(egg)) {
				eggs.remove(egg);
				if (eggs.size() == 0)
					nades.remove(name);
				else
					nades.put(name, eggs);
				explode(player, egg, plugin);
			}
		}
	}*/

	/*private static void explode(Player player, Egg egg) {
		Location loc = egg.getLocation();
		loc.getWorld().createExplosion(loc, -1.0F);
		for (Vector v : Utils.getDirections()) {
			moveExpSnow(loc.getWorld().spawn(loc, Snowball.class), v, player, Paintball.instance);
		}
	}*/

	/*
	 * public static void explodeBlocks(Player player, Projectile nade,
	 * Paintball plugin, Material mat) { Location loc = nade.getLocation();
	 * loc.getWorld().createExplosion(loc, -1.0F);
	 * 
	 * int i; for(Vector v : directions()) { i = random.nextInt(10);
	 * moveExpSnow(loc.getWorld().spawn(loc, Snowball.class), v, player,
	 * plugin); if(i < 5) { FallingBlock f =
	 * loc.getWorld().spawnFallingBlock(loc, mat, (byte)0);
	 * f.setDropItem(false); FallingBlocks.addFallingBlock(f); moveBlock(f, v,
	 * plugin); } } }
	 */

	/*
	 * private static void moveBlock(final FallingBlock f, Vector v, Paintball
	 * plugin) { Vector v2 = v.clone(); v2.setX(v.getX()+ Math.random()-
	 * Math.random()); v2.setY(v.getY()+ Math.random()- Math.random());
	 * v2.setZ(v.getZ()+ Math.random()- Math.random());
	 * f.setVelocity(v2.multiply(0.5));
	 * plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new
	 * Runnable() {
	 * 
	 * @Override public void run() { if(!f.isDead() || f.isValid()) f.remove();
	 * } }, 100L); }
	 */

	private final Egg entity;
	private final Origin source;

	public Grenade(Egg entity, Origin source) {
		this.entity = entity;
		this.source = source;
	}

	int getId() {
		return entity.getEntityId();
	}

	public Origin getSource() {
		return source;
	}
	
	public void explode(Location location, Player shooter) {
		location.getWorld().createExplosion(location, -1F);
		final String shooterName = shooter.getName();
		for (Vector v : Utils.getDirections()) {
			final Snowball s  = location.getWorld().spawn(location, Snowball.class);
			s.setShooter(shooter);
			Ball.registerBall(s, shooterName, source);
			Vector v2 = v.clone();
			v2.setX(v.getX() + Math.random() - Math.random());
			v2.setY(v.getY() + Math.random() - Math.random());
			v2.setZ(v.getZ() + Math.random() - Math.random());
			s.setVelocity(v2);
			Paintball.instance.getServer().getScheduler().scheduleSyncDelayedTask(Paintball.instance, new Runnable() {

				@Override
				public void run() {
					if (!s.isDead() || s.isValid()) {
						Ball.getBall(s.getEntityId(), shooterName, true);
						s.remove();
					}
				}
			}, (long) Paintball.instance.grenadeTime);
		}
	}
	
	void remove() {
		entity.remove();
	}
	
	/*private void snow(final Snowball s, Vector v, Player player) {
		s.setShooter(player);
		Vector v2 = v.clone();
		v2.setX(v.getX() + Math.random() - Math.random());
		v2.setY(v.getY() + Math.random() - Math.random());
		v2.setZ(v.getZ() + Math.random() - Math.random());
		s.setVelocity(v2.normalize().multiply(Paintball.instance.grenadeSpeed));
		Paintball.instance.getServer().getScheduler().scheduleSyncDelayedTask(Paintball.instance, new Runnable() {

			@Override
			public void run() {
				if (!s.isDead() || s.isValid()) {
					Ball.getBall(s, ((Player)s.getShooter()).getName(), true);
					s.remove();
				}
			}
		}, (long) Paintball.instance.grenadeTime);
	}*/

}
