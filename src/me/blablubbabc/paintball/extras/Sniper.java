package me.blablubbabc.paintball.extras;

import java.util.ArrayList;

import me.blablubbabc.paintball.Paintball;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

public class Sniper {
	private static ArrayList<Player> zooming = new ArrayList<Player>();

	public static void toggleZoom(Player player) {
		if (isZooming(player)) {
			zooming.remove(player);
			player.setWalkSpeed(0.2F);
		} else {
			zooming.add(player);
			player.setWalkSpeed(-0.15F);
		}
	}

	public static boolean isZooming(Player player) {
		return zooming.contains(player);
	}

	public static void setZooming(Player player) {
		if (!isZooming(player))
			zooming.add(player);
		player.setWalkSpeed(-0.15F);
	}

	public static void setNotZooming(Player player) {
		if (isZooming(player))
			zooming.remove(player);
		player.setWalkSpeed(0.2F);
	}

	public static void shoot(Player player, Paintball plugin) {
		Location loc = player.getEyeLocation();
		Vector dir = loc.getDirection().normalize();
		player.playSound(loc, Sound.FIRE_IGNITE, 100F, 0F);

		moveSnow(loc.getWorld().spawn(loc, Snowball.class), dir, player, plugin);

	}

	private static void moveSnow(final Snowball s, Vector v, Player player, Paintball plugin) {
		s.setShooter(player);
		s.setVelocity(v.multiply(plugin.sniperSpeedmulti));
	}
}
