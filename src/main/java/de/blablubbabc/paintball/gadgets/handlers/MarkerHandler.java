/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.gadgets.handlers;

import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import de.blablubbabc.paintball.FragInformations;
import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.gadgets.Gadget;
import de.blablubbabc.paintball.gadgets.WeaponHandler;
import de.blablubbabc.paintball.gadgets.events.PaintballHitEvent;
import de.blablubbabc.paintball.statistics.player.PlayerStat;
import de.blablubbabc.paintball.statistics.player.PlayerStats;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class MarkerHandler extends WeaponHandler {

	public MarkerHandler() {
		this(null);
	}

	public MarkerHandler(Material customItemType) {
		super("Marker", customItemType, new Origin() {

			@Override
			public String getKillMessage(FragInformations fragInfo) {
				return Translator.getString("WEAPON_FEED_MARKER", getDefaultVariablesMap(fragInfo));
			}
		});
	}

	@Override
	protected Material getDefaultItemType() {
		return Material.SNOWBALL;
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
		if (event.getAction() == Action.PHYSICAL) return;
		Player player = event.getPlayer();
		PlayerInventory playerInventory = player.getInventory();
		ItemStack itemInHand = playerInventory.getItemInMainHand();
		if (itemInHand == null) return;

		if (itemInHand.isSimilar(getItem())) {
			PlayerInventory inv = player.getInventory();
			if (match.setting_balls == -1 || inv.contains(Material.SNOWBALL, 1)) {
				World world = player.getWorld();
				Vector direction = player.getLocation().getDirection().normalize();
				Location spawnLoc = Utils.getRightHeadLocation(direction, player.getEyeLocation());

				// SOUND EFFECT
				player.playSound(spawnLoc, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0F, 2F);
				world.playSound(spawnLoc, Sound.ENTITY_CHICKEN_EGG, 2.0F, 2F);

				// SHOOT SNOWBALL
				Snowball snowball = (Snowball) world.spawnEntity(spawnLoc, EntityType.SNOWBALL);
				snowball.setShooter(player);
				// REGISTER:
				Paintball.getInstance().weaponManager.getBallHandler().createBall(match, player, snowball, this.getWeaponOrigin());
				// BOOST:
				snowball.setVelocity(direction.multiply(Paintball.getInstance().speedmulti));
				// STATS
				// PLAYERSTATS
				PlayerStats playerStats = Paintball.getInstance().playerManager.getPlayerStats(player.getUniqueId());
				playerStats.addStat(PlayerStat.SHOTS, 1);
				// INFORM MATCH
				match.onShot(player);

				if (match.setting_balls != -1) {
					// -1 ball
					Utils.removeInventoryItems(inv, getItem(), 1);
				}

			} else {
				player.playSound(player.getEyeLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1F, 2F);
			}
			Utils.updatePlayerInventoryLater(Paintball.getInstance(), player);
		}
	}

	@Override
	protected void onProjectileHit(ProjectileHitEvent event, Projectile projectile, Match match, Player shooter) {
		if (projectile.getType() == EntityType.SNOWBALL) {
			Gadget ball = Paintball.getInstance().weaponManager.getBallHandler().getBall(projectile, match, shooter.getUniqueId());
			// is paintball ?
			if (ball != null) {
				Location location = projectile.getLocation();

				// effect
				if (Paintball.getInstance().effects) {
					if (match.isBlue(shooter)) {
						location.getWorld().playEffect(location, Effect.POTION_BREAK, Color.BLUE.asRGB());
					} else if (match.isRed(shooter)) {
						location.getWorld().playEffect(location, Effect.POTION_BREAK, Color.RED.asRGB());
					}
				}

				// call event for others:
				Paintball.getInstance().getServer().getPluginManager().callEvent(new PaintballHitEvent(event, match, shooter));

				// remove ball from tracking:
				ball.dispose(true);
			}
		}
	}

	@Override
	public void cleanUp(Match match, UUID playerId) {
		// nothing to do here
	}

	@Override
	public void cleanUp(Match match) {
		// nothing to do here
	}
}
