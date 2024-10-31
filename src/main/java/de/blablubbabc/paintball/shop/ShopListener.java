/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.shop;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.utils.Translator;

public class ShopListener implements Listener {

	private Paintball plugin;
	private String shopSign;

	public ShopListener(Paintball plugin) {
		this.plugin = plugin;
		shopSign = Translator.getString("SHOP_SIGN");
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			Player player = (Player) event.getWhoClicked();
			ShopMenu menu = plugin.shopManager.getShopMenu(player.getUniqueId());
			if (menu != null) {
				event.setCancelled(true);
				if (event.getSlotType() == SlotType.CONTAINER && event.isLeftClick() && event.getRawSlot() < event.getView().getTopInventory().getSize()) {
					int slot = event.getSlot();
					menu.onClick(player, slot);
				}
				player.updateInventory();
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onInventoryClose(InventoryCloseEvent event) {
		UUID playerId = event.getPlayer().getUniqueId();
		ShopMenu menu = plugin.shopManager.getShopMenu(playerId);
		if (menu != null) {
			menu.onClose(playerId);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void onSignClick(PlayerInteractEvent event) {
		// Ignore off-hand interactions:
		if (event.getHand() != EquipmentSlot.HAND) return;

		Block block = event.getClickedBlock();
		if (block != null) {
			BlockState state = block.getState();
			if (state instanceof Sign) {
				Sign sign = (Sign) state;
				SignSide signFront = sign.getSide(Side.FRONT);
				String line1 = ChatColor.stripColor(signFront.getLine(0));
				if (line1.equalsIgnoreCase(shopSign)) {
					plugin.shopManager.getShopMenu().open(event.getPlayer());
				}
			}
		}
	}
}
