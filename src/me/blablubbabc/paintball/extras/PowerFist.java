package me.blablubbabc.paintball.extras;

import me.blablubbabc.paintball.Utils;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PowerFist {
	
	private static Vector add = new Vector(0,0.15,0);
	
	public static void use(Player player, Entity entity) {
		Location loc = player.getEyeLocation();
		int yaw = (int)loc.getYaw();
		Vector dir = loc.getDirection().normalize();
		
		//Effect
		loc.getWorld().playEffect(loc, Effect.SMOKE, Utils.yawToFace(yaw));
		
		entity.setVelocity(dir.clone().add(add).normalize().multiply(3.0));
	}
}
