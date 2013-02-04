package me.blablubbabc.paintball.extras;

import me.blablubbabc.paintball.Utils;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PowerFist {
	
	private static Vector add = new Vector(0,0.2,0);
	
	public static void use(Player player) {
		Location loc = player.getEyeLocation();
		int yaw = (int)loc.getYaw();
		Vector dir = loc.getDirection().normalize();
		
		//Effect
		loc.getWorld().playEffect(loc, Effect.SMOKE, Utils.yawToFace(yaw));
		
		for(Entity e : Utils.getEntitiesInCone(player.getNearbyEntities(50, 50, 50), loc, 30, 120, yaw)) {
			e.setVelocity(dir.clone().add(add).normalize().multiply(3.0));
		}
	}
}
