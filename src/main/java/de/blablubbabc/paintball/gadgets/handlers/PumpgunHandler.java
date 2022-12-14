/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.gadgets.handlers;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import de.blablubbabc.paintball.FragInformations;
import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.gadgets.WeaponHandler;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class PumpgunHandler extends WeaponHandler {

	public PumpgunHandler() {
		this(null);
	}

	public PumpgunHandler(Material customItemType) {
		super("Pumpgun", customItemType, new Origin() {

			@Override
			public String getKillMessage(FragInformations fragInfo) {
				return Translator.getString("WEAPON_FEED_PUMPGUN", getDefaultVariablesMap(fragInfo));
			}
		});
	}

	public void shoot(Player player, Match match, Location location, Vector direction, double speed, Origin origin) {
		player.getWorld().playSound(location, Sound.ITEM_FLINTANDSTEEL_USE, 2.0F, 0F);
		direction.normalize();

		for (int i = 0; i < Paintball.getInstance().pumpgunBullets; i++) {
			Snowball snowball = location.getWorld().spawn(location, Snowball.class);
			snowball.setShooter(player);
			Paintball.getInstance().weaponManager.getBallHandler().createBall(match, player, snowball, origin);
			Vector vel = new Vector(direction.getX() + (Utils.random.nextDouble() - 0.45) / Paintball.getInstance().pumpgunSpray,
					direction.getY() + (Utils.random.nextDouble() - 0.45) / Paintball.getInstance().pumpgunSpray,
					direction.getZ() + (Utils.random.nextDouble() - 0.45) / Paintball.getInstance().pumpgunSpray).normalize();
			snowball.setVelocity(vel.multiply(speed));
		}

	}

	@Override
	protected Material getDefaultItemType() {
		return Material.STONE_AXE;
	}

	@Override
	protected ItemStack setItemMeta(ItemStack itemStack) {
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(Translator.getString("WEAPON_PUMPGUN"));
		itemStack.setItemMeta(meta);
		return itemStack;
	}

	@Override
	protected void onInteract(PlayerInteractEvent event, Match match) {
		if (event.getAction() == Action.PHYSICAL || !Paintball.getInstance().pumpgun) return;
		Player player = event.getPlayer();
		PlayerInventory playerInventory = player.getInventory();
		ItemStack itemInHand = playerInventory.getItemInMainHand();
		if (itemInHand == null) return;

		if (itemInHand.isSimilar(getItem())) {
			PlayerInventory inv = player.getInventory();
			if ((match.setting_balls == -1 || inv.containsAtLeast(Paintball.getInstance().weaponManager.getBallHandler().getItem(), Paintball.getInstance().pumpgunAmmo))) {
				Utils.removeInventoryItems(inv, Paintball.getInstance().weaponManager.getBallHandler().getItem(), Paintball.getInstance().pumpgunAmmo);
				Utils.updatePlayerInventoryLater(Paintball.getInstance(), player);
				// INFORM MATCH
				match.onShot(player);

				Location location = player.getEyeLocation();
				shoot(player, match, location, location.getDirection(), Paintball.getInstance().pumpgunSpeedmulti, this.getWeaponOrigin());

			} else {
				player.playSound(player.getEyeLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1F, 2F);
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
