package me.blablubbabc.paintball.extras;

import me.blablubbabc.paintball.Paintball;
import me.blablubbabc.paintball.Origin;
import me.blablubbabc.paintball.Utils;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

public class Pumpgun {	
	
	public static int[] angles = new int[5];
	
	public static void init() {
		angles[0] = Paintball.instance.shotgunAngle2;
		angles[1] = Paintball.instance.shotgunAngle1;
		angles[2] = 0;
		angles[3] = -Paintball.instance.shotgunAngle1;
		angles[4] = -Paintball.instance.shotgunAngle2;
	}

	public static void shotShotgun(Player player) {
		Location loc = player.getEyeLocation();
		Vector dir = loc.getDirection().normalize();
		Vector dirY = (new Location(loc.getWorld(), 0, 0, 0, loc.getYaw(), 0)).getDirection().normalize();
		player.playSound(loc, Sound.FIRE_IGNITE, 100F, 0F);
		
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
		
		
		/*moveSnow(loc.getWorld().spawn(loc, Snowball.class), dir.clone().add(rotateYAxis(dirY, Paintball.instance.pumpgunAngle2).subtract(dirY)).normalize(), player, playerName);
		moveSnow(loc.getWorld().spawn(loc, Snowball.class), dir.clone().add(rotateYAxis(dirY, Paintball.instance.pumpgunAngle1).subtract(dirY)).normalize(), player, playerName);
		moveSnow(loc.getWorld().spawn(loc, Snowball.class), dir.clone(), player, playerName);
		moveSnow(loc.getWorld().spawn(loc, Snowball.class), dir.clone().add(rotateYAxis(dirY, -Paintball.instance.pumpgunAngle1).subtract(dirY)).normalize(), player, playerName);
		moveSnow(loc.getWorld().spawn(loc, Snowball.class), dir.clone().add(rotateYAxis(dirY, -Paintball.instance.pumpgunAngle2).subtract(dirY)).normalize(), player, playerName);
		*/
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
	
	public static void shotPumpgun(Player player) {
		Location loc = player.getEyeLocation();
		
		Vector dir = loc.getDirection().normalize();
		
		player.playSound(loc, Sound.FIRE_IGNITE, 100F, 0F);
		String playerName = player.getName();
		
		for (int i = 0; i < Paintball.instance.pumpgunBullets ; i++) {
			Snowball s = loc.getWorld().spawn(loc, Snowball.class);
			s.setShooter(player);
			Ball.registerBall(s, playerName, Origin.PUMPGUN);
			Vector v = new Vector(dir.getX() + (Utils.random.nextDouble()-0.45)/Paintball.instance.pumpgunSpray, dir.getY() + (Utils.random.nextDouble()-0.45)/Paintball.instance.pumpgunSpray, dir.getZ() + (Utils.random.nextDouble()-0.45)/Paintball.instance.pumpgunSpray).normalize();
			s.setVelocity(v.multiply(Paintball.instance.pumpgunSpeedmulti));
		}
		
	}
	
	private static Vector rotateYAxis(Vector dir, double angleD) {
		double angleR = Math.toRadians(angleD);
		double x = dir.getX();
		double z = dir.getZ();
		double cos = Math.cos(angleR);
		double sin = Math.sin(angleR);
		return (new Vector(x*cos+z*(-sin), 0.0, x*sin+z*cos)).normalize();
	}
	
	
	/*private static Vector rotateYAxis2(Vector dir, double angleD) {
		double angleR = Math.toRadians(angleD);
		double x = dir.getX();
		double y = dir.getY();
		double z = dir.getZ();
		double cos = Math.cos(angleR);
		double sin = Math.sin(angleR);
		return (new Vector(x*cos+z*(-sin), y, x*sin+z*cos)).normalize();
	}
	
	private static Vector rotateXAxis2(Vector dir, double angleD) {
		double angleR = Math.toRadians(angleD);
		double x = dir.getX();
		double y = dir.getY();
		double z = dir.getZ();
		double cos = Math.cos(angleR);
		double sin = Math.sin(angleR);
		return (new Vector(x, y*cos+z*sin, y*(-sin)+z*cos)).normalize();
	}
	
	private static Vector rotateZAxis2(Vector dir, double angleD) {
		double angleR = Math.toRadians(angleD);
		double x = dir.getX();
		double y = dir.getY();
		double z = dir.getZ();
		double cos = Math.cos(angleR);
		double sin = Math.sin(angleR);
		return (new Vector(x*cos+y*sin, x*(-sin)+y*cos, z)).normalize();
	}*/
}
