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
		Vector dirY = (new Location(loc.getWorld(), 0, 0, 0, loc.getYaw(), 0)).getDirection().normalize();
		//Vector dirXZU = rotateXZAxis(dir, 3);
		//Vector dirXZD = rotateXZAxis(dir, -3);
		player.playSound(loc, Sound.FIRE_IGNITE, 100F, 0F);
		
		//moveSnow(loc.getWorld().spawn(loc, Snowball.class), dirXZU.clone().add(rotateYAxis(dirY, 3).subtract(dirY)).normalize(), player, plugin);
		//moveSnow(loc.getWorld().spawn(loc, Snowball.class), dirXZU.clone(), player, plugin);
		//moveSnow(loc.getWorld().spawn(loc, Snowball.class), dirXZU.clone().add(rotateYAxis(dirY, -3).subtract(dirY)).normalize(), player, plugin);
		
		moveSnow(loc.getWorld().spawn(loc, Snowball.class), dir.clone().add(rotateYAxis(dirY, plugin.pumpgunAngle2).subtract(dirY)).normalize(), player, plugin);
		moveSnow(loc.getWorld().spawn(loc, Snowball.class), dir.clone().add(rotateYAxis(dirY, plugin.pumpgunAngle1).subtract(dirY)).normalize(), player, plugin);
		moveSnow(loc.getWorld().spawn(loc, Snowball.class), dir.clone(), player, plugin);
		moveSnow(loc.getWorld().spawn(loc, Snowball.class), dir.clone().add(rotateYAxis(dirY, -plugin.pumpgunAngle1).subtract(dirY)).normalize(), player, plugin);
		moveSnow(loc.getWorld().spawn(loc, Snowball.class), dir.clone().add(rotateYAxis(dirY, -plugin.pumpgunAngle2).subtract(dirY)).normalize(), player, plugin);
		
		//moveSnow(loc.getWorld().spawn(loc, Snowball.class), dirXZD.clone().add(rotateYAxis(dirY, 3).subtract(dirY)).normalize(), player, plugin);
		//moveSnow(loc.getWorld().spawn(loc, Snowball.class), dirXZD.clone(), player, plugin);
		//moveSnow(loc.getWorld().spawn(loc, Snowball.class), dirXZD.clone().add(rotateYAxis(dirY, -3).subtract(dirY)).normalize(), player, plugin);
		
	}
	
	private static Vector rotateYAxis(Vector dir, double angleD) {
		double angleR = Math.toRadians(angleD);
		double x = dir.getX();
		double z = dir.getZ();
		double cos = Math.cos(angleR);
		double sin = Math.sin(angleR);
		return (new Vector(x*cos+z*(-sin), 0.0, x*sin+z*cos)).normalize();
	}
	
	/*private static Vector rotateXZAxis(Vector dir, double angleD) {
		double angleR = Math.toRadians(angleD);
		double x = dir.getX();
		double y = dir.getY();
		double z = dir.getZ();
		double xz = Math.sqrt(x*x + z*z);
		
		double cos = Math.cos(angleR);
		double sin = Math.sin(angleR);
		return (new Vector(x, xz*cos, z)).normalize();
	}*/
	
	private static void moveSnow(final Snowball s, Vector v, Player player, Paintball plugin) {
		s.setShooter(player);
		s.setVelocity(v.multiply(plugin.pumpgunSpeedmulti));
	}
}
