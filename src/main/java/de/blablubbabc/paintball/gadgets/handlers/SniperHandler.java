/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.gadgets.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import de.blablubbabc.paintball.FragInformations;
import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.gadgets.WeaponHandler;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class SniperHandler extends WeaponHandler {

	private List<String> zooming = new ArrayList<String>();

	public SniperHandler() {
		this(null);
	}

	public SniperHandler(Material customItemType) {
		super("Sniper", customItemType, new Origin() {

			@Override
			public String getKillMessage(FragInformations fragInfo) {
				return Translator.getString("WEAPON_FEED_SNIPER", getDefaultVariablesMap(fragInfo));
			}
		});
	}

	public void shoot(Player player, Match match, Location location, Vector direction, double speed, Origin origin) {
		player.getWorld().playSound(location, Sound.ITEM_FLINTANDSTEEL_USE, 2.0F, 0F);
		direction.normalize();

		Snowball snowball = location.getWorld().spawn(location, Snowball.class);
		snowball.setShooter(player);
		Paintball.getInstance().weaponManager.getBallHandler().createBall(match, player, snowball, origin);
		if (Paintball.getInstance().sniperNoGravity) {
			Paintball.getInstance().weaponManager.getNoGravityHandler().addEntity(snowball, direction.multiply(speed), Paintball.getInstance().sniperNoGravityDuration * 20);
		} else {
			snowball.setVelocity(direction.multiply(speed));
		}
	}

	@Override
	protected Material getDefaultItemType() {
		return Material.CARROT_ON_A_STICK;
	}

	@Override
	protected ItemStack setItemMeta(ItemStack itemStack) {
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(Translator.getString("WEAPON_SNIPER"));
		itemStack.setItemMeta(meta);
		return itemStack;
	}

	@Override
	protected void onInteract(PlayerInteractEvent event, Match match) {
		if (event.getAction() != Action.RIGHT_CLICK_AIR || !Paintball.getInstance().sniper) return;
		Player player = event.getPlayer();
		PlayerInventory playerInventory = player.getInventory();
		ItemStack itemInHand = playerInventory.getItemInMainHand();
		if (itemInHand == null || !itemInHand.isSimilar(getItem())) return;

		boolean canShoot = true;
		if (Paintball.getInstance().sniperOnlyUseIfZooming && !isZooming(player)) {
			canShoot = false;
		} else if (match.setting_balls != -1 && !playerInventory.containsAtLeast(Paintball.getInstance().weaponManager.getBallHandler().getItem(), 1)) {
			canShoot = false;
		}

		if (!canShoot) {
			player.playSound(player.getEyeLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1F, 2F);
			return;
		}

		// INFORM MATCH
		match.onShot(player);

		Vector direction = player.getLocation().getDirection();
		Location spawnLoc = Utils.getRightHeadLocation(direction, player.getEyeLocation());
		shoot(player, match, spawnLoc, direction, Paintball.getInstance().sniperSpeedmulti, this.getWeaponOrigin());

		if (match.setting_balls != -1) {
			// -1 ball
			Utils.removeInventoryItems(playerInventory, Paintball.getInstance().weaponManager.getBallHandler().getItem(), 1);
			Utils.updatePlayerInventoryLater(Paintball.getInstance(), player);
		}
	}

	@Override
	protected void onToggleSneak(PlayerToggleSneakEvent event, Match match) {
		if (!Paintball.getInstance().sniper) return;
		Player player = event.getPlayer();
		ItemStack itemInHand = player.getInventory().getItemInMainHand();
		if (itemInHand == null || !itemInHand.isSimilar(getItem())) return;

		boolean sneaking = event.isSneaking();
		if (sneaking) {
			setZooming(player);
		} else {
			setNotZooming(player);
		}
	}

	@Override
	protected void onItemHeld(Player player, ItemStack newItem) {
		if (isZooming(player)) {
			setNotZooming(player);
		}
	}

	private void setZoom(Player player) {
		player.setWalkSpeed(-0.15F);
		if (Paintball.getInstance().sniperRemoveSpeed) player.removePotionEffect(PotionEffectType.SPEED);
	}

	private void setNoZoom(Player player) {
		player.setWalkSpeed(0.2F);
	}

	public boolean isZooming(Player player) {
		return zooming.contains(player.getName());
	}

	public void setZooming(Player player) {
		if (!isZooming(player)) {
			zooming.add(player.getName());
		}

		setZoom(player);
	}

	public void setNotZooming(Player player) {
		if (isZooming(player)) {
			zooming.remove(player.getName());
		}

		setNoZoom(player);
	}

	@Override
	public void cleanUp(Match match, UUID playerId) {
		Player player = Bukkit.getPlayer(playerId);
		if (player != null && isZooming(player)) {
			setNotZooming(player);
		}
	}

	@Override
	public void cleanUp(Match match) {
		// nothing to do here
	}
}
