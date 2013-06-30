package de.blablubbabc.paintball.utils;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Sounds {
	
	public static void init() {
		
	}
	
	public static void playProtected(Player shooter, Player target) {
		shooter.playSound(shooter.getEyeLocation(), Sound.ANVIL_LAND, 0.7F, 2F);
		target.playSound(shooter.getEyeLocation(), Sound.ANVIL_LAND, 0.6F, 0F);
	}
	
	public static void playHit(Player shooter, Player target) {
		shooter.playSound(shooter.getEyeLocation(), Sound.MAGMACUBE_WALK, 1F, 1F);
		target.playSound(shooter.getEyeLocation(), Sound.HURT_FLESH, 1F, 1F);
	}
	
	public static void playTeamattack(Player shooter) {
		shooter.playSound(shooter.getEyeLocation(), Sound.ANVIL_LAND, 0.7F, 1F);
	}
	
	public static void playFrag(Player killer, Player target) {
		killer.playSound(killer.getEyeLocation(), Sound.MAGMACUBE_WALK, 1F, 0F);
		target.playSound(target.getEyeLocation(), Sound.GHAST_SCREAM2, 1F, 0F);
	}
	
	public static void playEquipLoadout(Player player) {
		player.playSound(player.getEyeLocation(), Sound.BAT_TAKEOFF, 1F, 1F);
	}
	
}
