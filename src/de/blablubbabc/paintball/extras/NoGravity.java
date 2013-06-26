package de.blablubbabc.paintball.extras;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

import de.blablubbabc.paintball.Paintball;


public class NoGravity {
	private static Map<Projectile, Vector> entitiesVec = new HashMap<Projectile, Vector>();
	private static Map<Projectile, Integer> entitiesDur = new HashMap<Projectile, Integer>();

	public static void init() {
		
	}
	
	public static void addEntity(Projectile s, Vector v, int duration) {
		entitiesVec.put(s, v);
		entitiesDur.put(s, duration);
	}

	public static void removeEntity(Projectile s) {
		entitiesVec.remove(s);
		entitiesDur.remove(s);
	}

	public static boolean containsEntity(Projectile s) {
		return entitiesVec.containsKey(s);
	}

	public static void run() {
		Paintball.instance.getServer().getScheduler().runTaskTimer(Paintball.instance, new Runnable() {
			
			@Override
			public void run() {
				if(!entitiesVec.isEmpty()) {
					Iterator<Projectile> iterator = entitiesVec.keySet().iterator();
					while(iterator.hasNext()) {
						Projectile s = iterator.next();
						int dur = entitiesDur.get(s);
						if(s.isDead() || dur <= 0 || !s.isValid()) {
							iterator.remove();
							entitiesDur.remove(s);
							if (dur <= 0) {
								if(s instanceof Snowball) Ball.getBall(s.getEntityId(), ((Player) s.getShooter()).getName(), true);
								else if(s instanceof Egg) Grenade.getGrenade(s.getEntityId(), ((Player) s.getShooter()).getName(), true);
								s.remove();
							}
						} else {
							s.setVelocity(entitiesVec.get(s));
							entitiesDur.put(s, dur-1);
						}
					}
				}
			}
		}, 20L, 1L);
	}
}
