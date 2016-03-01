/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.utils;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Sounds {

	public static void init() {

	}

	public static void playProtected(Player shooter, Player target) {
		shooter.playSound(shooter.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 0.3F, 2F);
		target.playSound(shooter.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 0.1F, 0F);
	}

	public static void playHit(Player shooter, Player target) {
		shooter.playSound(shooter.getEyeLocation(), Sound.ENTITY_MAGMACUBE_JUMP, 1F, 1F);
		target.playSound(shooter.getEyeLocation(), Sound.ENTITY_PLAYER_HURT, 1F, 1F);
	}

	public static void playMeleeHit(Player shooter, Player target) {
		shooter.playSound(shooter.getEyeLocation(), Sound.ENTITY_PLAYER_HURT, 1F, 1F);
		target.playSound(shooter.getEyeLocation(), Sound.ENTITY_PLAYER_HURT, 1F, 1F);
	}

	public static void playTeamattack(Player shooter) {
		shooter.playSound(shooter.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 0.3F, 1F);
	}

	public static void playFrag(Player killer, Player target) {
		killer.playSound(killer.getEyeLocation(), Sound.ENTITY_MAGMACUBE_JUMP, 1F, 0F);
		target.playSound(target.getEyeLocation(), Sound.ENTITY_GHAST_SCREAM, 1F, 0F);
	}

	public static void playEquipLoadout(Player player) {
		player.playSound(player.getEyeLocation(), Sound.ENTITY_BAT_TAKEOFF, 1F, 1F);
	}

}
