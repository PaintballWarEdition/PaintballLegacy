package me.blablubbabc.paintball.extras;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import me.blablubbabc.paintball.Paintball;

public class NoGravity {
	private static Map<Entity, Vector> entitiesVec = new HashMap<Entity, Vector>();
	private static Map<Entity, Integer> entitiesDur = new HashMap<Entity, Integer>();

	public static void addEntity(Entity e, Vector v, int duration) {
		entitiesVec.put(e, v);
		entitiesDur.put(e, duration);
	}

	public static void removeEntity(Entity e) {
		entitiesVec.remove(e);
		entitiesDur.remove(e);
	}

	public static boolean containsEntity(Entity e) {
		return entitiesVec.containsKey(e);
	}

	public static void run() {
		Paintball.instance.getServer().getScheduler().runTaskTimer(Paintball.instance, new Runnable() {
			
			@Override
			public void run() {
				if(!entitiesVec.isEmpty()) {
					Iterator<Entity> iterator = entitiesVec.keySet().iterator();
					while(iterator.hasNext()) {
						Entity e = iterator.next();
						int dur = entitiesDur.get(e);
						if(e.isDead() || dur <= 0 || !e.isValid()) {
							iterator.remove();
							entitiesDur.remove(e);
						} else {
							e.setVelocity(entitiesVec.get(e));
							entitiesDur.put(e, dur-1);
						}
					}
				}
			}
		}, 20L, 1L);
	}
}
