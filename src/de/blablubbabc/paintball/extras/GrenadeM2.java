package de.blablubbabc.paintball.extras;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.utils.Utils;

public class GrenadeM2 {

	public final static ItemStack item = ItemManager.setMeta(new ItemStack(Material.SLIME_BALL));
	private static int next = 0;
	
	public static int getNext() {
		return ++next;
	}
	
	
	private static HashMap<String, ArrayList<GrenadeM2>> nades = new HashMap<String, ArrayList<GrenadeM2>>();
	
	public static void registerNade(Item nade, String shooterName, Origin source) {
		ArrayList<GrenadeM2> pnades = nades.get(shooterName);
		if (pnades == null) {
			pnades = new ArrayList<GrenadeM2>();
			nades.put(shooterName, pnades);
		}
		pnades.add(new GrenadeM2(shooterName, nade, source));
	}

	public static boolean isNade(int id) {
		for (ArrayList<GrenadeM2> pnades : nades.values()) {
			if (getNadeFromList(pnades, id) != null) return true; 
		}
		return false;
	}
	
	public static GrenadeM2 getNade(int id, String shooterName, boolean remove) {
		ArrayList<GrenadeM2> pnades = nades.get(shooterName);
		if (pnades == null)
			return null;
		GrenadeM2 nade = getNadeFromList(pnades, id);
		if (remove && nade != null) {
			if (pnades.remove(nade)) {
				if (pnades.size() == 0) nades.remove(shooterName);
			}
		}
		return nade;
	}
	
	private static GrenadeM2 getNadeFromList(ArrayList<GrenadeM2> pnades, int id) {
		for (GrenadeM2 nade : pnades) {
			if (nade.getId() == id)
				return nade;
		}
		return null;
	}
	
	public static void clear() {
		for (String playerName : nades.keySet()) {
			ArrayList<GrenadeM2> pnades = new ArrayList<GrenadeM2>(nades.get(playerName));
			for (GrenadeM2 g : pnades) {
				g.remove();
			}
		}
		nades.clear();
		next = 0;
	}

	private final Item entity;
	private final String shooterName;
	private final Origin source;

	public GrenadeM2(String shooterName, Item entity, Origin source) {
		this.entity = entity;
		this.shooterName = shooterName;
		this.source = source;
		
		Paintball.instance.getServer().getScheduler().runTaskLater(Paintball.instance, new Runnable() {
			
			@Override
			public void run() {
				explode();
			}
		}, 20L * Paintball.instance.grenade2TimeUntilExplosion);
	}

	public Origin getSource() {
		return source;
	}
	
	int getId() {
		return entity.getEntityId();
	}
	
	public void explode() {
		if (!entity.isDead() && entity.isValid()) {
			Location location = entity.getLocation();
			location.getWorld().createExplosion(location, -1F);
			Player player = Paintball.instance.getServer().getPlayerExact(shooterName);
			if (player != null) {
				for (Vector v : Utils.getDirections()) {
					final Snowball s  = location.getWorld().spawn(location, Snowball.class);
					s.setShooter(player);
					Ball.registerBall(s, shooterName, source);
					Vector v2 = v.clone();
					v2.setX(v.getX() + Math.random() - Math.random());
					v2.setY(v.getY() + Math.random() - Math.random());
					v2.setZ(v.getZ() + Math.random() - Math.random());
					s.setVelocity(v2.normalize().multiply(Paintball.instance.grenade2ShrapnelSpeed));
					Paintball.instance.getServer().getScheduler().scheduleSyncDelayedTask(Paintball.instance, new Runnable() {

						@Override
						public void run() {
							if (!s.isDead() || s.isValid()) {
								Ball.getBall(s.getEntityId(), shooterName, true);
								s.remove();
							}
						}
					}, (long) Paintball.instance.grenade2Time);
				}
			}
		}
		getNade(entity.getEntityId(), shooterName, true);
		entity.remove();
	}
	
	void remove() {
		entity.remove();
	}

}
