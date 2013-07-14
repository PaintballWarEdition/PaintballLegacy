package de.blablubbabc.paintball.gadgets;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Paintball;

public abstract class WeaponHandler {
	protected final ItemStack item;
	
	public WeaponHandler(int customItemTypeID, boolean useDefaultType) {
		Paintball.instance.weaponManager.registerWeaponHandler(this);
		item = setItemMeta(new ItemStack(useDefaultType ? getDefaultItemTypeID() : customItemTypeID));
	}
	
	protected abstract int getDefaultItemTypeID();
	protected abstract ItemStack setItemMeta(ItemStack itemStack);
	
	public int getItemTypeID() {
		return item.getTypeId();
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public abstract void cleanUp(Match match, String playerName);
	public abstract void cleanUp(Match match);
	
	// Events
	protected abstract void onInteract(PlayerInteractEvent event, Match match);
	
	protected void onBlockPlace(BlockPlaceEvent event, Match match) {
		
	}
	
	/**
	 * Note: this is ALSO called for players which are NOT playing paintball.
	 * 
	 * @param event the PlayerPickUpEvent
	 */
	protected void onItemPickup(PlayerPickupItemEvent event) {
		
	}
	
	protected void onItemHeld(Player player, ItemStack newItem) {
		
	}
	
	protected void onDamagedByEntity(EntityDamageByEntityEvent event, Match match, Player attacker) {
		
	}
	
	protected void onProjectileHit(ProjectileHitEvent event, Projectile projectile, Match match, Player shooter) {
		
	}
	
}