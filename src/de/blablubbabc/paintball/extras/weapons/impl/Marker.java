package de.blablubbabc.paintball.extras.weapons.impl;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.extras.weapons.Ball;
import de.blablubbabc.paintball.extras.weapons.WeaponHandler;
import de.blablubbabc.paintball.statistics.player.PlayerStat;
import de.blablubbabc.paintball.statistics.player.PlayerStats;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class Marker extends WeaponHandler {
	
	public Marker(Paintball plugin, int customItemTypeID, boolean useDefaultType) {
		super(plugin, customItemTypeID, useDefaultType);
	}

	@Override
	protected int getDefaultItemTypeID() {
		return Material.SNOW_BALL.getId();
	}

	@Override
	protected ItemStack setItemMeta(ItemStack itemStack) {
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(Translator.getString("WEAPON_PAINTBALL"));
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	
	@Override
	protected void onInteract(PlayerInteractEvent event, Match match) {
		Player player = event.getPlayer();
		String playerName = player.getName();
		ItemStack itemInHand = player.getItemInHand();
		
		if (itemInHand.isSimilar(getItem())) {
			PlayerInventory inv = player.getInventory();
			if (match.setting_balls == -1 || inv.contains(Material.SNOW_BALL, 1)) {
				// SOUND EFFECT
				Location eyeLoc = player.getEyeLocation();
				World world = player.getWorld();
				world.playSound(eyeLoc, Sound.WOOD_CLICK, 2.0F, 0F);
				world.playSound(eyeLoc, Sound.CHICKEN_EGG_POP, 2.0F, 2F);
				
				// SHOOT SNOWBALL
				Snowball snowball = (Snowball) world.spawnEntity(eyeLoc, EntityType.SNOWBALL);
				snowball.setShooter(player);
				// REGISTER:
				new Ball(plugin.weaponManager.getBallManager(), match, player, snowball, Origin.MARKER);
				// BOOST:
				snowball.setVelocity(player.getLocation().getDirection().normalize().multiply(plugin.speedmulti));
				// STATS
				// PLAYERSTATS
				PlayerStats playerStats = plugin.playerManager.getPlayerStats(playerName);
				playerStats.addStat(PlayerStat.SHOTS, 1);
				// INFORM MATCH
				match.onShot(player);
				
				if (match.setting_balls != -1) {
					// -1 ball
					Utils.removeInventoryItems(inv, getItem(), 1);
				}
				
			} else {
				player.playSound(player.getEyeLocation(), Sound.FIRE_IGNITE, 1F, 2F);
			}
			Utils.updatePlayerInventoryLater(plugin, player);
		}
	}
	
}
