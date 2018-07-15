/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.gadgets;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.Paintball;

public abstract class WeaponHandler {
	protected final String weaponName;
	protected final ItemStack item;
	protected final Origin origin;

	public WeaponHandler(String weaponName, Origin origin) {
		this(weaponName, null, origin);
	}

	public WeaponHandler(String weaponName, Material customItemType, Origin origin) {
		this.weaponName = weaponName;
		Paintball.getInstance().weaponManager.registerWeaponHandler(weaponName, this);
		item = setItemMeta(new ItemStack(customItemType == null ? getDefaultItemType() : customItemType));
		this.origin = origin != null ? origin : new Origin();
	}

	public String getWeaponName() {
		return weaponName;
	}

	protected abstract Material getDefaultItemType();

	protected abstract ItemStack setItemMeta(ItemStack itemStack);

	public Material getItemType() {
		return item.getType();
	}

	public ItemStack getItem() {
		return item;
	}

	public Origin getWeaponOrigin() {
		return origin;
	}

	public abstract void cleanUp(Match match, UUID playerId);

	public abstract void cleanUp(Match match);

	// Events
	protected abstract void onInteract(PlayerInteractEvent event, Match match);

	protected void onBlockPlace(BlockPlaceEvent event, Match match) {

	}

	/**
	 * Note: This is ALSO called for players / entities which are NOT playing paintball.
	 * 
	 * @param event
	 *            the EntityPickupItemEvent
	 */
	protected void onItemPickup(EntityPickupItemEvent event) {

	}

	protected void onItemHeld(Player player, ItemStack newItem) {

	}

	protected void onDamagedByEntity(EntityDamageByEntityEvent event, Match match, Player attacker) {

	}

	protected void onProjectileHit(ProjectileHitEvent event, Projectile projectile, Match match, Player shooter) {

	}

}
