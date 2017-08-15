/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.gadgets.handlers;

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

public class ShotgunHandler extends WeaponHandler {	
	
	private int[] angles = new int[5];
	
	public ShotgunHandler(int customItemTypeID, boolean useDefaultType) {
		super("Shotgun", customItemTypeID, useDefaultType, new Origin() {
			
			@Override
			public String getKillMessage(FragInformations fragInfo) {
				return Translator.getString("WEAPON_FEED_SHOTGUN", getDefaultVariablesMap(fragInfo));
			}
		});
		
		angles[0] = Paintball.getInstance().shotgunAngle2;
		angles[1] = Paintball.getInstance().shotgunAngle1;
		angles[2] = 0;
		angles[3] = -Paintball.getInstance().shotgunAngle1;
		angles[4] = -Paintball.getInstance().shotgunAngle2;
	}

	public void shoot(Player player, Match match, Location location, Vector direction, double speed, Origin origin) {
		player.getWorld().playSound(location, Sound.ITEM_FLINTANDSTEEL_USE, 2.0F, 0F);
		direction.normalize();
		
		Vector dirY = (new Location(location.getWorld(), 0, 0, 0, location.getYaw(), 0)).getDirection().normalize();
		Vector n = new Vector(dirY.getZ(), 0.0, -dirY.getX());
		
		boolean alreadyAngleNull = false;
		for (int angle : angles) {
			Vector vec;
			if (angle != 0) {
				vec = Utils.rotateYAxis(dirY, angle);
				vec.multiply(Math.sqrt(vec.getX() * vec.getX() + vec.getZ() * vec.getZ())).subtract(dirY);
				vec = direction.clone().add(vec).normalize();
			} else {
				if (alreadyAngleNull) continue;
				else {
					alreadyAngleNull = true;
					vec = direction.clone();
				}
			}
			
			if (Paintball.getInstance().shotgunAngleVert == 0) {
				Snowball snowball = location.getWorld().spawn(location, Snowball.class);
				snowball.setShooter(player);
				Paintball.getInstance().weaponManager.getBallHandler().createBall(match, player, snowball, origin);
				snowball.setVelocity(vec.clone().multiply(speed));
			} else {
				for (int i = -Paintball.getInstance().shotgunAngleVert; i <= Paintball.getInstance().shotgunAngleVert; i += Paintball.getInstance().shotgunAngleVert) {
					Snowball snowball = location.getWorld().spawn(location, Snowball.class);
					snowball.setShooter(player);
					Paintball.getInstance().weaponManager.getBallHandler().createBall(match, player, snowball, origin);
					snowball.setVelocity(Utils.rotateAxis(vec, n, i).multiply(speed));
				}
			}
		}
	}
	
	@Override
	protected int getDefaultItemTypeID() {
		return Material.SPECKLED_MELON.getId();
	}

	@Override
	protected ItemStack setItemMeta(ItemStack itemStack) {
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(Translator.getString("WEAPON_SHOTGUN"));
		itemStack.setItemMeta(meta);
		return itemStack;
	}

	@Override
	protected void onInteract(PlayerInteractEvent event, Match match) {
		if (event.getAction() == Action.PHYSICAL || !Paintball.getInstance().shotgun) return;
		Player player = event.getPlayer();
		ItemStack itemInHand = player.getItemInHand();
		if (itemInHand == null) return;
		
		if (itemInHand.isSimilar(getItem())) {
			PlayerInventory inv = player.getInventory();
			if ((match.setting_balls == -1 || inv.containsAtLeast(Paintball.getInstance().weaponManager.getBallHandler().getItem(), Paintball.getInstance().shotgunAmmo))) {
				Utils.removeInventoryItems(inv, Paintball.getInstance().weaponManager.getBallHandler().getItem(), Paintball.getInstance().shotgunAmmo);
				Utils.updatePlayerInventoryLater(Paintball.getInstance(), player);
				// INFORM MATCH
				match.onShot(player);
				
				Location location = player.getEyeLocation();
				shoot(player, match, location, location.getDirection(), Paintball.getInstance().shotgunSpeedmulti, this.getWeaponOrigin());
				
			} else {
				player.playSound(player.getEyeLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1F, 2F);
			}
		}
	}
	
	@Override
	public void cleanUp(Match match, String playerName) {
		// nothing to do here
	}

	@Override
	public void cleanUp(Match match) {
		// nothing to do here
	}
	
}
