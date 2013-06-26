package de.blablubbabc.paintball.utils;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Sounds {
	
	public static void init() {
		
	}
	
	public static void playProtected(Player shooter, Player target) {
		shooter.playSound(shooter.getLocation(), Sound.ANVIL_LAND, 70F, 2F);
		target.playSound(shooter.getLocation(), Sound.ANVIL_LAND, 60F, 0F);
	}
	
	public static void playHit(Player shooter, Player target) {
		shooter.playSound(shooter.getLocation(), Sound.MAGMACUBE_WALK, 100F, 1F);
		target.playSound(shooter.getLocation(), Sound.BAT_HURT, 60F, 0F);
	}
	
	public static void playTeamattack(Player shooter) {
		shooter.playSound(shooter.getLocation(), Sound.ANVIL_LAND, 70F, 1F);
	}
	
	public static void playFrag(Player shooter, Player target) {
		shooter.playSound(shooter.getLocation(), Sound.MAGMACUBE_WALK, 100F, 0F);
		target.playSound(target.getLocation(), Sound.GHAST_SCREAM2, 100F, 0F);
	}
	
}
