package me.blablubbabc.paintball.extras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import me.blablubbabc.paintball.Paintball;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Egg;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

public class Grenade{
	
	private static Random random = new Random();
	private static HashMap<Player, ArrayList<Egg>> nades = new HashMap<Player, ArrayList<Egg>>();
	
	public static synchronized void eggThrow(Player player, Egg nade) {
		Egg id = (Egg) nade;
		ArrayList<Egg> n = new ArrayList<Egg>();
		if(nades.get(player) == null) nades.put(player, n);
		else n.addAll(nades.get(player));
		n.add(id);
		nades.put(player, n);
	}
	
	public static synchronized void hit(Projectile nade, Paintball plugin, Material mat) {
		Egg id = (Egg) nade;
		for(Player player : nades.keySet()) {
			ArrayList<Egg> n = new ArrayList<Egg>();
			n.addAll(nades.get(player));
			if(n.contains(id)) {
				n.remove(id);
				nades.put(player, n);
				explodeBlocks(player, nade, plugin, mat);
				break;
			}
		}
	}
	
	/*public static void explode(Player player, Projectile nade, Paintball plugin) {
		Location loc = nade.getLocation();
		loc.getWorld().createExplosion(loc, -1.0F);
		for(Vector v : directions()) {
			moveExpSnow(loc.getWorld().spawn(loc, Snowball.class), v, player, plugin);
		}
	}*/
	
	public static void explodeBlocks(Player player, Projectile nade, Paintball plugin, Material mat) {
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
	}
	
	private static void moveBlock(final FallingBlock f, Vector v, Paintball plugin) {
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
	}
	
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
