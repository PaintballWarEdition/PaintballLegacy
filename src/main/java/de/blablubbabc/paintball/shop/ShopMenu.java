/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.utils.KeyValuePair;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class ShopMenu {
	private static final ItemStack nextIcon = new ItemStack(Material.BOOK_AND_QUILL);
	private static final ItemStack prevIcon = new ItemStack(Material.BOOK_AND_QUILL);
	private static final ItemStack pageIcon = new ItemStack(Material.PAPER, 0);
	
	private final Paintball plugin;
	private final ShopGood[] goods;
	
	private final int contentSlots;

	private final int nextSlot;
	private final int previousSlot;
	
	private final boolean singlePage;
	
	private final Inventory[] inventories;
	private final Map<String, Integer> viewers = new HashMap<String, Integer>();
	
	public ShopMenu(Paintball plugin, ShopGood[] goods) {
		this.plugin = plugin;
		this.goods = goods;

		Utils.setItemMeta(nextIcon, Translator.getString("SHOP_NEXT_PAGE"), null);
		Utils.setItemMeta(prevIcon, Translator.getString("SHOP_PREVIOUS_PAGE"), null);

		// calculate needed rows:
		int neededRows = (int) Math.ceil(goods.length / 9D);
		// multiple pages needed?
		if (neededRows > 6) {
			singlePage = false;
			int neededPages = (int) Math.ceil(neededRows / 5D);
			contentSlots = 45; // (9*5)
			nextSlot = 53; //(9*6 - 1)
			previousSlot = nextSlot - 8;
			int pageSlot = previousSlot + 4;
			
			inventories = new Inventory[neededPages];
			// init pages:
			int left = goods.length;
			for (int i = 0; i < neededPages; i++) {
				Inventory inventory = Bukkit.createInventory(null, 54, Translator.getString("SHOP_NAME"));
				inventories[i] = inventory;
				int size = Math.min(left, contentSlots);
				left -= size;
				int startIndex = i * contentSlots;
				for (int j = 0; j < size; j++) {
					inventory.setItem(j, goods[startIndex + j].getIcon());
				}
				// buttons:
				ItemStack pageItem = pageIcon.clone();
				pageItem.setAmount(i + 1);
				Utils.setItemMeta(pageItem, Translator.getString("SHOP_PAGE", new KeyValuePair("page", String.valueOf(i + 1))), null);
				inventories[i].setItem(previousSlot, prevIcon);
				inventories[i].setItem(pageSlot, pageItem);
				inventories[i].setItem(nextSlot, nextIcon);
			}
			
		} else {
			singlePage = true;
			contentSlots = neededRows * 9;
			// un-needed but final:
			nextSlot = 0;
			previousSlot = 0;
			
			inventories = new Inventory[1];
			// init single page:
			Inventory inventory = Bukkit.createInventory(null, contentSlots, Translator.getString("SHOP_NAME"));
			inventories[0] = inventory;
			int left = goods.length;
			for (int j = 0; j < left; j++) {
				inventory.setItem(j, goods[j].getIcon());
			}
		}
	}
	
	public void onClick(Player player, int slot) {
		if (isSelecting(player.getName())) {
			if (singlePage || slot < previousSlot) {
				int page = viewers.get(player.getName());
				int goodNumber = getGoodNumber(page, slot);
				buy(player, getGood(goodNumber), page);
			} else {
				if (slot == previousSlot) {
					openPreviousPage(player);
				} else if (slot == nextSlot) {
					openNextPage(player);
				}
			}
		}
	}
	
	private int getGoodNumber(int page, int slot) {
		return (page * contentSlots) + slot;
	}
	
	private ShopGood getGood(int goodNumber) {
		if (goodNumber >= 0 && goodNumber < goods.length) return goods[goodNumber];
		else return null;
	}
	
	private void buy(final Player player, ShopGood good, final int page) {
		if (good != null) {
			plugin.shopManager.handleBuy(player, good, false);
		}
	}
	
	private void openNextPage(final Player player) {
		String name = player.getName();
		Integer vPage = viewers.get(name);
		if (vPage != null) {
			final int page = Math.min(vPage + 1, inventories.length - 1);
			if (page != vPage) {
				openPageLater(player, page);
			}
		}
	}
	
	private void openPreviousPage(final Player player) {
		String name = player.getName();
		Integer vPage = viewers.get(name);
		if (vPage != null) {
			final int page = Math.max(vPage - 1, 0);
			if (page != vPage) {
				openPageLater(player, page);
			}
		}
	}
	
	private void openPageLater(final Player player, final int page) {
		Paintball.getInstance().getServer().getScheduler().runTaskLater(plugin, new Runnable() {
			
			@Override
			public void run() {
				openPage(player, page);
			}
		}, 1L);
	}
	
	private void openPage(Player player, int page) {
		player.closeInventory();
		if (player.openInventory(inventories[page]) != null) {
			viewers.put(player.getName(), page);
		}
	}
	
	public void open(Player player) {
		openPage(player, 0);
	}
	
	public void onClose(String playerName) {
		viewers.remove(playerName);
	}
	
	public boolean isSelecting(String playerName) {
		return viewers.containsKey(playerName);
	}
	
	void shutdown() {
		List<String> viewerNames = new ArrayList<String>(viewers.keySet());
		for (String playerName : viewerNames) {
			Player player = Bukkit.getPlayerExact(playerName);
			assert player != null;
			if (player != null) {
				player.closeInventory();
			}
		}
		// theoretically clear shouldn't be necessarry..
		assert viewers.isEmpty();
		viewers.clear();
	}
	
}
