package de.blablubbabc.paintball.extras.weapons;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.extras.weapons.impl.AirstrikeHandler;
import de.blablubbabc.paintball.extras.weapons.impl.ConcussionHandler;
import de.blablubbabc.paintball.extras.weapons.impl.FlashbangHandler;
import de.blablubbabc.paintball.extras.weapons.impl.GiftHandler;
import de.blablubbabc.paintball.extras.weapons.impl.Marker;
import de.blablubbabc.paintball.utils.Translator;

public class WeaponManager {
	private List<WeaponHandler> weaponHandlers = new ArrayList<WeaponHandler>();
	private GadgetManager ballHandler;
	private GiftHandler giftHandler;
	
	public WeaponManager(Paintball plugin) {
		ballHandler = new GadgetManager();
		giftHandler = new GiftHandler(plugin, Material.CHEST.getId(), false);
		// init all default weapons and gadgets:
		new Marker(plugin, Material.SNOW_BALL.getId(), false);
		new AirstrikeHandler(plugin, Material.STICK.getId(), false);
		new FlashbangHandler(plugin, Material.GHAST_TEAR.getId(), false);
		new ConcussionHandler(plugin, Material.SPIDER_EYE.getId(), false);
		
		//TODO
		
	}
	
	public void registerWeaponHandler(WeaponHandler weaponHandler) {
		weaponHandlers.add(weaponHandler);
	}
	
	public GadgetManager getBallManager() {
		return ballHandler;
	}
	
	public GiftHandler getGiftManager() {
		return giftHandler;
	}
	
	public void onInteract(PlayerInteractEvent event, Match match) {
		for (WeaponHandler weaponHandler : weaponHandlers) {
			weaponHandler.onInteract(event, match);
		}
	}
	
	public void onBlockPlace(Player player, Block block, Match match) {
		for (WeaponHandler weaponHandler : weaponHandlers) {
			weaponHandler.onBlockPlace(player, block, match);
		}
	}
	
	public void onItemPickup(PlayerPickupItemEvent event) {
		for (WeaponHandler weaponHandler : weaponHandlers) {
			weaponHandler.onItemPickup(event);
		}
	}
	
	public void onDamagedByEntity(EntityDamageByEntityEvent event, Entity damagedEntity, Match match, Player attacker) {
		for (WeaponHandler weaponHandler : weaponHandlers) {
			weaponHandler.onDamagedByEntity(event, damagedEntity, match, attacker);
		}
	}
	
	public void onItemHeld(Player player) {
		for (WeaponHandler weaponHandler : weaponHandlers) {
			weaponHandler.onItemHeld(player);
		}
	}
	
	public void cleanUp(Match match, String playerName) {
		for (WeaponHandler weaponHandler : weaponHandlers) {
			weaponHandler.cleanUp(match, playerName);
		}
	}
	
	public void cleanUp(Match match) {
		for (WeaponHandler weaponHandler : weaponHandlers) {
			weaponHandler.cleanUp(match);
		}
		ballHandler.cleanUp(match);
	}

	public ItemStack setMeta(ItemStack itemStack) {
		int typeID = itemStack.getTypeId();
		
		// Team colored wool:
		if (typeID == Material.WOOL.getId()) {
			ItemMeta meta = itemStack.getItemMeta();
			byte data = itemStack.getData().getData();
			if (data == DyeColor.RED.getWoolData()) {
				meta.setDisplayName(Translator.getString("TEAM_RED"));
			} else if (data == DyeColor.BLUE.getWoolData()) {
				meta.setDisplayName(Translator.getString("TEAM_BLUE"));
				itemStack.setItemMeta(meta);
			} else if (data == DyeColor.YELLOW.getWoolData()) {
				meta.setDisplayName(Translator.getString("TEAM_SPECTATOR"));
				itemStack.setItemMeta(meta);
			}
			itemStack.setItemMeta(meta);
			return itemStack;
		}
		
		// Shop book:
		if (typeID == Material.BOOK.getId()) {
			ItemMeta meta = itemStack.getItemMeta();
			meta.setDisplayName(Translator.getString("SHOP_ITEM"));
			itemStack.setItemMeta(meta);
			return itemStack;
		}
		
		
		for (WeaponHandler weaponHandler : weaponHandlers) {
			if (weaponHandler.getItemTypeID() == typeID) {
				return weaponHandler.setItemMeta(itemStack);
			}
		}
		
		return itemStack;
	}
}
