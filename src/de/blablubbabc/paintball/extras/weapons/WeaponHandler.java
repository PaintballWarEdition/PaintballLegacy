package de.blablubbabc.paintball.extras.weapons;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
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
	
	protected int getItemTypeID() {
		return item.getTypeId();
	}
	
	protected ItemStack getItem() {
		return item;
	}
	
	protected void cleanUp(Match match, String playerName) {
		
	}
	protected void cleanUp(Match match) {
		
	}
	
	// Events
	protected abstract void onInteract(PlayerInteractEvent event, Match match);
	
	protected void onBlockPlace(Player player, Block block, Match match) {
		
	}
	
	protected void onItemPickup(PlayerPickupItemEvent event) {
		
	}
	
	protected void onItemHeld(Player player) {
		
	}
	
	protected void onDamagedByEntity(EntityDamageByEntityEvent event, Entity damagedEntity, Match match, Player attacker) {
		
	}
	
	protected void onProjectileHit(ProjectileHitEvent event, Projectile projectile, Match match, Player shooter) {
		
	}
	
}
