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
import de.blablubbabc.paintball.utils.Utils;

public class Pumpgun {	
	
	public final static ItemStack item = ItemManager.setMeta(new ItemStack(Material.STONE_AXE));
	
	public static void shoot(Player player) {
		Location loc = player.getEyeLocation();
		
		Vector dir = loc.getDirection().normalize();
		
		player.getWorld().playSound(loc, Sound.FIRE_IGNITE, 1.5F, 0F);
		String playerName = player.getName();
		
		for (int i = 0; i < Paintball.instance.pumpgunBullets ; i++) {
			Snowball s = loc.getWorld().spawn(loc, Snowball.class);
			s.setShooter(player);
			Ball.registerBall(s, playerName, Origin.PUMPGUN);
			Vector v = new Vector(dir.getX() + (Utils.random.nextDouble()-0.45)/Paintball.instance.pumpgunSpray, dir.getY() + (Utils.random.nextDouble()-0.45)/Paintball.instance.pumpgunSpray, dir.getZ() + (Utils.random.nextDouble()-0.45)/Paintball.instance.pumpgunSpray).normalize();
			s.setVelocity(v.multiply(Paintball.instance.pumpgunSpeedmulti));
		}
		
	}
}
