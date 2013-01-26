package me.blablubbabc.paintball.extras;

import me.blablubbabc.paintball.Paintball;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

public class Pumpgun {	

	public static void shot(Player player, Paintball plugin) {
		Location loc = player.getEyeLocation();
		Vector dir = loc.getDirection().normalize();
		Vector dirY = (new Location(loc.getWorld(), 0, 0, 0, 0, loc.getPitch())).getDirection().normalize();
		player.playSound(loc, Sound.FIRE_IGNITE, 100F, 0F);
		
		moveSnow(loc.getWorld().spawn(loc, Snowball.class), dir.clone().add(rotateYAxis(dirY, 30).clone().subtract(dirY)), player, plugin);
		moveSnow(loc.getWorld().spawn(loc, Snowball.class), dir.clone().add(rotateYAxis(dirY, 15).clone().subtract(dirY)), player, plugin);
		moveSnow(loc.getWorld().spawn(loc, Snowball.class), dir.clone().add(dirY.clone().clone().subtract(dirY)), player, plugin);
		moveSnow(loc.getWorld().spawn(loc, Snowball.class), dir.clone().add(rotateYAxis(dirY, -15).clone().subtract(dirY)), player, plugin);
		moveSnow(loc.getWorld().spawn(loc, Snowball.class), dir.clone().add(rotateYAxis(dirY, -30).clone().subtract(dirY)), player, plugin);
		
	}
	
	private static Vector rotateYAxis(Vector dir, double angleD) {
		double angleR = Math.toRadians(angleD);
		double x = dir.getX();
		double y = 0.0;
		double z = dir.getZ();
		double cos = Math.cos(angleR);
		double sin = Math.sin(angleR);
		return new Vector(x*cos+z*(-sin), y, x*sin+y*cos).normalize();
	}
	
	private static void moveSnow(final Snowball s, Vector v, Player player, Paintball plugin) {
		s.setShooter(player);
		s.setVelocity(v.multiply(1));
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			
			@Override
			public void run() {
				if(!s.isDead() || s.isValid()) s.remove();
			}
		}, 20L);
	}
}
