package me.blablubbabc.paintball.extras;

import me.blablubbabc.paintball.Paintball;
import me.blablubbabc.paintball.Source;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

public class Pumpgun {	

	public static void shot(Player player) {
		Location loc = player.getEyeLocation();
		Vector dir = loc.getDirection().normalize();
		Vector dirY = (new Location(loc.getWorld(), 0, 0, 0, loc.getYaw(), 0)).getDirection().normalize();
		player.playSound(loc, Sound.FIRE_IGNITE, 100F, 0F);
		
		String playerName = player.getName();
		moveSnow(loc.getWorld().spawn(loc, Snowball.class), dir.clone().add(rotateYAxis(dirY, Paintball.instance.pumpgunAngle2).subtract(dirY)).normalize(), player, playerName);
		moveSnow(loc.getWorld().spawn(loc, Snowball.class), dir.clone().add(rotateYAxis(dirY, Paintball.instance.pumpgunAngle1).subtract(dirY)).normalize(), player, playerName);
		moveSnow(loc.getWorld().spawn(loc, Snowball.class), dir.clone(), player, playerName);
		moveSnow(loc.getWorld().spawn(loc, Snowball.class), dir.clone().add(rotateYAxis(dirY, -Paintball.instance.pumpgunAngle1).subtract(dirY)).normalize(), player, playerName);
		moveSnow(loc.getWorld().spawn(loc, Snowball.class), dir.clone().add(rotateYAxis(dirY, -Paintball.instance.pumpgunAngle2).subtract(dirY)).normalize(), player, playerName);
	}
	
	private static Vector rotateYAxis(Vector dir, double angleD) {
		double angleR = Math.toRadians(angleD);
		double x = dir.getX();
		double z = dir.getZ();
		double cos = Math.cos(angleR);
		double sin = Math.sin(angleR);
		return (new Vector(x*cos+z*(-sin), 0.0, x*sin+z*cos)).normalize();
	}
	
	private static void moveSnow(final Snowball s, Vector v, Player player, String playerName) {
		s.setShooter(player);
		Ball.registerBall(s.getEntityId(), playerName, Source.PUMPGUN);
		s.setVelocity(v.multiply(Paintball.instance.pumpgunSpeedmulti));
	}
}
