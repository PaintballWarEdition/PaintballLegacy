package me.blablubbabc.paintball.extras;

import java.util.ArrayList;
import me.blablubbabc.paintball.Paintball;
import net.minecraft.server.EnumSkyBlock;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class LightEntity {
	private static ArrayList<Location> lights = new ArrayList<Location>();
	private static ArrayList<Entity> entities = new ArrayList<Entity>();
	private static int task = -1;

	public static synchronized void addEntity(Entity entity, Paintball plugin) {
		entities.add(entity);
		if(task == -1) setLights(plugin);
	}

	public static synchronized void removeEntity(Entity entity) {
		entities.remove(entity);
	}

	public static synchronized Entity isLightEntity(Entity entity) {
		for (Entity e : entities) {
			if (e.equals(entity)) {
				return e;
			}
		}
		return null;
	}

	public static synchronized ArrayList<Entity> getEntities() {
		ArrayList<Entity> list = new ArrayList<Entity>();
		for (Entity e : entities) {
			list.add(e);
		}
		return list;
	}
	
	private static synchronized void addLight(Location loc) {
		lights.add(loc);
	}

	/*private static synchronized void removeLight(Location loc) {
		lights.remove(loc);
	}*/
	
	private static synchronized void clearLights() {
		lights = new ArrayList<Location>();
	}

	/*private static synchronized boolean isLight(Location loc) {
		for (Location l : lights) {
			if (l.equals(loc)) {
				return true;
			}
		}
		return false;
	}*/

	private static synchronized ArrayList<Location> getLights() {
		ArrayList<Location> list = new ArrayList<Location>();
		for (Location loc : lights) {
			list.add(loc);
		}
		return list;
	}
	
	private static void setLights(final Paintball plugin) {
		task = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			
			@Override
			public void run() {
				//remove all lights
				ArrayList<Location> lights = getLights();
				for(Location loc : lights) {
					CraftWorld world = (CraftWorld) loc.getWorld();
					world.getHandle().b(EnumSkyBlock.BLOCK, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
					world.getHandle().notify(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
					for(Player p : loc.getWorld().getPlayers()) {
						p.sendBlockChange(loc, loc.getBlock().getType(), loc.getBlock().getData());
					}
				}
				clearLights();
				//new lights
				ArrayList<Entity> entities = getEntities();
				for(Entity e : entities) {
					//remove not exisiting entities
					if(!e.isValid()) {
						removeEntity(e);
						continue;
					}
					Location loc = e.getLocation();
					addLight(loc);
					CraftWorld world = (CraftWorld) loc.getWorld();
					world.getHandle().b(EnumSkyBlock.BLOCK, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), 15);
					world.getHandle().notify(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
					for(Player p : loc.getWorld().getPlayers()) {
						p.sendBlockChange(loc, loc.getBlock().getType(), loc.getBlock().getData());
					}
				}
				//restart:
				if(!getEntities().isEmpty()) {
					setLights(plugin);
				}
				//task end:
				else task = -1;
				
			}
		}, 1L);
	}
}
