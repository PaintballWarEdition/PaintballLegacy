package de.blablubbabc.paintball.extras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.utils.Utils;

public class Grenade {

	public final static ItemStack item = ItemManager.setMeta(new ItemStack(Material.EGG));
	
	private static Map<String, ArrayList<Grenade>> nades = new HashMap<String, ArrayList<Grenade>>();
	
	public static void init() {
		
	}
	
	public static void registerGrenade(Egg egg, String shooterName, Origin source) {
		ArrayList<Grenade> pnades = nades.get(shooterName);
		if (pnades == null) {
			pnades = new ArrayList<Grenade>();
			nades.put(shooterName, pnades);
		}
		pnades.add(new Grenade(egg, source));
	}

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
			s.setVelocity(v2.normalize().multiply(Paintball.instance.grenadeShrapnelSpeed));
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

}
