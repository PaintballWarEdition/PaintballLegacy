package me.blablubbabc.paintball.extras;

import java.util.ArrayList;
import java.util.HashMap;

import me.blablubbabc.paintball.Paintball;
import org.bukkit.Location;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

public class Grenade{
	
	//private static Random random = new Random();
	private static HashMap<String, ArrayList<Egg>> nades = new HashMap<String, ArrayList<Egg>>();
	
	public static boolean isGrenade(Egg egg, Player shooter) {
		ArrayList<Egg> eggs = nades.get(shooter.getName());
		if (eggs == null || !eggs.contains(egg)) return false;
		else return true;
	}
	
	public static void eggThrow(Player player, Egg egg) {
		String name = player.getName();
		ArrayList<Egg> eggs = nades.get(name);
		if (eggs == null) eggs = new ArrayList<Egg>();
		eggs.add(egg);
		nades.put(name, eggs);
	}
	
	public static void eggHit(Egg egg, Paintball plugin) {
		if (egg.getShooter() instanceof Player) {
			Player player = (Player) egg.getShooter();
			String name = player.getName();
			ArrayList<Egg> eggs = nades.get(name);
			if(eggs != null && eggs.contains(egg)) {
				eggs.remove(egg);
				if (eggs.size() == 0) nades.remove(name);
				else nades.put(name, eggs);
				explode(player, egg, plugin);
			}
		}
	}
	
	private static void explode(Player player, Egg egg, Paintball plugin) {
		Location loc = egg.getLocation();
		loc.getWorld().createExplosion(loc, -1.0F);
		for(Vector v : directions()) {
			moveExpSnow(loc.getWorld().spawn(loc, Snowball.class), v, player, plugin);
		}
	}
	
	/*public static void explodeBlocks(Player player, Projectile nade, Paintball plugin, Material mat) {
		Location loc = nade.getLocation();
		loc.getWorld().createExplosion(loc, -1.0F);
		
		int i;
		for(Vector v : directions()) {
			i = random.nextInt(10);
			moveExpSnow(loc.getWorld().spawn(loc, Snowball.class), v, player, plugin);
			if(i < 5) {
				FallingBlock f = loc.getWorld().spawnFallingBlock(loc, mat, (byte)0);
				f.setDropItem(false);
				FallingBlocks.addFallingBlock(f);
				moveBlock(f, v, plugin);
			}
		}
	}*/
	
	/*private static void moveBlock(final FallingBlock f, Vector v, Paintball plugin) {
		Vector v2 = v.clone();
		v2.setX(v.getX()+ Math.random()- Math.random());
		v2.setY(v.getY()+ Math.random()- Math.random());
		v2.setZ(v.getZ()+ Math.random()- Math.random());
		f.setVelocity(v2.multiply(0.5));
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			
			@Override
			public void run() {
				if(!f.isDead() || f.isValid()) f.remove();
			}
		}, 100L);
	}*/
	
	private static void moveExpSnow(final Snowball s, Vector v, Player player, Paintball plugin) {
		s.setShooter(player);
		Vector v2 = v.clone();
		v2.setX(v.getX()+ Math.random()- Math.random());
		v2.setY(v.getY()+ Math.random()- Math.random());
		v2.setZ(v.getZ()+ Math.random()- Math.random());
		s.setVelocity(v2.multiply(1));
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			
			@Override
			public void run() {
				if(!s.isDead() || s.isValid()) s.remove();
			}
		}, (long) plugin.grenadeTime);
	}
	
	private static ArrayList<Vector> directions() {
		ArrayList<Vector> vectors = new ArrayList<Vector>();
		//alle Richtungen
		vectors.add(new Vector(1,0,0)); vectors.add(new Vector(0,1,0)); vectors.add(new Vector(0,0,1));
		vectors.add(new Vector(1,1,0)); vectors.add(new Vector(1,0,1)); vectors.add(new Vector(0,1,1));
		vectors.add(new Vector(0,0,0));vectors.add(new Vector(1,1,1)); vectors.add(new Vector(-1,-1,-1));
		vectors.add(new Vector(-1,0,0)); vectors.add(new Vector(0,-1,0)); vectors.add(new Vector(0,0,-1));
		vectors.add(new Vector(-1,-1,0)); vectors.add(new Vector(-1,0,-1)); vectors.add(new Vector(0,-1,-1));
		vectors.add(new Vector(1,-1,0)); vectors.add(new Vector(1,0,-1)); vectors.add(new Vector(0,1,-1));
		vectors.add(new Vector(-1,1,0)); vectors.add(new Vector(-1,0,1)); vectors.add(new Vector(0,-1,1));
		vectors.add(new Vector(1,1,-1)); vectors.add(new Vector(1,-1,1)); vectors.add(new Vector(-1,1,1));
		vectors.add(new Vector(1,-1,-1)); vectors.add(new Vector(-1,1,-1)); vectors.add(new Vector(-1,-1,1));
		
		return vectors;
	}
	
}
