/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.gadgets.handlers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
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
	
	public SniperHandler(int customItemTypeID, boolean useDefaultType) {
		super("Sniper", customItemTypeID, useDefaultType, new Origin() {
			
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
	protected int getDefaultItemTypeID() {
		return Material.CARROT_STICK.getId();
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
		if (event.getAction() == Action.PHYSICAL || !Paintball.getInstance().sniper) return;
		Player player = event.getPlayer();
		ItemStack itemInHand = player.getItemInHand();
		if (itemInHand == null) return;
		
		if (itemInHand.isSimilar(getItem())) {
			Action action = event.getAction();
			if (action == Action.LEFT_CLICK_AIR) {
				toggleZoom(player);
			} else if (action == Action.RIGHT_CLICK_AIR) {
				PlayerInventory inv = player.getInventory();
				if ((!Paintball.getInstance().sniperOnlyUseIfZooming || isZooming(player))
					&& (match.setting_balls == -1 || inv.containsAtLeast(Paintball.getInstance().weaponManager.getBallHandler().getItem(), 1))) {
					// INFORM MATCH
					match.onShot(player);
					
					Vector direction = player.getLocation().getDirection();
					Location spawnLoc = Utils.getRightHeadLocation(direction, player.getEyeLocation());
					shoot(player, match, spawnLoc, direction, Paintball.getInstance().sniperSpeedmulti, this.getWeaponOrigin());
					
					if (match.setting_balls != -1) {
						// -1 ball
						Utils.removeInventoryItems(inv, Paintball.getInstance().weaponManager.getBallHandler().getItem(), 1);
						Utils.updatePlayerInventoryLater(Paintball.getInstance(), player);
					}
				} else {
					player.playSound(player.getEyeLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1F, 2F);
				}
			}
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
		if(Paintball.getInstance().sniperRemoveSpeed) player.removePotionEffect(PotionEffectType.SPEED);
	}
	
	private void setNoZoom(Player player) {
		player.setWalkSpeed(0.2F);
	}
	
	public void toggleZoom(Player player) {
		if (isZooming(player)) {
			zooming.remove(player.getName());
			setNoZoom(player);
		} else {
			zooming.add(player.getName());
			setZoom(player);
		}
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
	public void cleanUp(Match match, String playerName) {
		Player player = Bukkit.getPlayerExact(playerName);
		if (player != null && isZooming(player)) {
			setNotZooming(player);
		}
	}

	@Override
	public void cleanUp(Match match) {
		// nothing to do here
	}
}
