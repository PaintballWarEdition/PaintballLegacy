package de.blablubbabc.paintball.extras;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.Paintball;

public class Shotgun {	
	
	public final static ItemStack item = ItemManager.setMeta(new ItemStack(Material.SPECKLED_MELON));
	
	public static int[] angles = new int[5];
	
	public static void init() {
		angles[0] = Paintball.instance.shotgunAngle2;
		angles[1] = Paintball.instance.shotgunAngle1;
		angles[2] = 0;
		angles[3] = -Paintball.instance.shotgunAngle1;
		angles[4] = -Paintball.instance.shotgunAngle2;
	}

	public static void shoot(Player player) {
		Location loc = player.getEyeLocation();
		Vector dir = loc.getDirection().normalize();
		Vector dirY = (new Location(loc.getWorld(), 0, 0, 0, loc.getYaw(), 0)).getDirection().normalize();
		player.getWorld().playSound(loc, Sound.FIRE_IGNITE, 1.5F, 0F);
		
		String playerName = player.getName();
		
		Vector n = new Vector(dirY.getZ(), 0.0, -dirY.getX());
		
		boolean alreadyAngleNull = false;
		for (int angle : angles) {
			Vector vec;
			if (angle != 0) {
				vec = rotateYAxis(dirY, angle);
				vec.multiply(Math.sqrt(vec.getX() * vec.getX() + vec.getZ() * vec.getZ())).subtract(dirY);
				vec = dir.clone().add(vec).normalize();
			} else {
				if (alreadyAngleNull) continue;
				else {
					alreadyAngleNull = true;
					vec = dir.clone();
				}
			}
			
			if (Paintball.instance.shotgunAngleVert == 0) {
				Snowball s = loc.getWorld().spawn(loc, Snowball.class);
				s.setShooter(player);
				Ball.registerBall(s, playerName, Origin.SHOTGUN);
				s.setVelocity(vec.clone().multiply(Paintball.instance.shotgunSpeedmulti));
			} else {
				for (int i = -Paintball.instance.shotgunAngleVert; i <= Paintball.instance.shotgunAngleVert; i += Paintball.instance.shotgunAngleVert) {
					Snowball s = loc.getWorld().spawn(loc, Snowball.class);
					s.setShooter(player);
					Ball.registerBall(s, playerName, Origin.SHOTGUN);
					s.setVelocity(rotateAxis(vec, n, i).multiply(Paintball.instance.shotgunSpeedmulti));
				}
			}
		}
	}
	
	private static Vector rotateAxis(Vector dir, Vector n, int angleD) {
		double angleR = Math.toRadians(angleD);
		double x = dir.getX();
		double y = dir.getY();
		double z = dir.getZ();
		
		double n1 = n.getX();
		double n2 = n.getY();
		double n3 = n.getZ();
		
		double cos = Math.cos(angleR);
		double sin = Math.sin(angleR);
		return new Vector(x*(n1*n1*(1-cos)+cos) + y*(n2*n1*(1-cos)+n3*sin) + z*(n3*n1*(1-cos)-n2*sin), 
				x*(n1*n2*(1-cos)-n3*sin) + y*(n2*n2*(1-cos)+cos) + z*(n3*n2*(1-cos)+n1*sin),
				x*(n1*n3*(1-cos)+n2*sin) + y*(n2*n3*(1-cos)-n1*sin) + z*(n3*n3*(1-cos)+cos));
	}
	
	private static Vector rotateYAxis(Vector dir, double angleD) {
		double angleR = Math.toRadians(angleD);
		double x = dir.getX();
		double z = dir.getZ();
		double cos = Math.cos(angleR);
		double sin = Math.sin(angleR);
		return (new Vector(x*cos+z*(-sin), 0.0, x*sin+z*cos)).normalize();
	}
}
