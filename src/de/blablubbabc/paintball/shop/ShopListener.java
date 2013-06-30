package de.blablubbabc.paintball.shop;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;

import de.blablubbabc.paintball.Paintball;

public class ShopListener implements Listener {
	
	private Paintball plugin;
	
	public ShopListener(Paintball plugin) {
		this.plugin = plugin;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			Player player = (Player) event.getWhoClicked();
			String playerName = player.getName();
			ShopMenu menu = plugin.shopManager.getShopMenu(playerName);
			if (menu != null) {
				event.setCancelled(true);
				if (event.getSlotType() == SlotType.CONTAINER && event.isLeftClick()) {
					int slot = event.getSlot();
					menu.onClick(player, slot);
				}
				player.updateInventory();
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onInventoryClose(InventoryCloseEvent event) {
		String playerName = event.getPlayer().getName();
		ShopMenu menu = plugin.shopManager.getShopMenu(playerName);
		if (menu != null) {
			menu.onClose(playerName);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void onSignClick(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		if (block != null) {
			BlockState state = block.getState();
			if (state instanceof Sign) {
				Sign sign = (Sign) state;
				String line1 = ChatColor.stripColor(sign.getLine(0));
				// TODO config node?
				if (line1.equalsIgnoreCase("[PB SHOP]")) {
					plugin.shopManager.getShopMenu().open(event.getPlayer());
				}
			}
		}
	}
}
