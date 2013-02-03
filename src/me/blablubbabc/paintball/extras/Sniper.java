package me.blablubbabc.paintball.extras;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Sniper {
	private static boolean removeSpeed = false;
	private static double speedmulti = 4.0;
	private static ArrayList<Player> zooming = new ArrayList<Player>();

	public static void setRemoveSpeed(boolean remove) {
		removeSpeed = remove;
	}
	public static void setSpeedmulti(double speed) {
		speedmulti = speed;
	}
	
	private static void setZoom(Player player) {
		player.setWalkSpeed(-0.15F);
		if(removeSpeed) player.removePotionEffect(PotionEffectType.SPEED);
	}
	
	private static void setNoZoom(Player player) {
		player.setWalkSpeed(0.2F);
	}
	
	public static void toggleZoom(Player player) {
		if (isZooming(player)) {
			zooming.remove(player);
			setNoZoom(player);
		} else {
			zooming.add(player);
			setZoom(player);
		}
	}

	public static boolean isZooming(Player player) {
		return zooming.contains(player);
	}

	public static void setZooming(Player player) {
		if (!isZooming(player))
			zooming.add(player);
		setZoom(player);
	}

	public static void setNotZooming(Player player) {
		if (isZooming(player))
			zooming.remove(player);
		setNoZoom(player);
	}

	public static void shoot(Player player) {
		Location loc = player.getEyeLocation();
		Vector dir = loc.getDirection().normalize();
		player.playSound(loc, Sound.FIRE_IGNITE, 100F, 0F);

		moveSnow(loc.getWorld().spawn(loc, Snowball.class), dir, player);

	}

	private static void moveSnow(final Snowball s, Vector v, Player player) {
		s.setShooter(player);
		s.setVelocity(v.multiply(speedmulti));
	}
}
