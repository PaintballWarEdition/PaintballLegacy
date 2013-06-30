package de.blablubbabc.paintball.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.utils.Utils;

public class ShopMenu {
	private static final ItemStack nextIcon = new ItemStack(Material.BOOK_AND_QUILL);
	private static final ItemStack prevIcon = new ItemStack(Material.BOOK_AND_QUILL);
	private static final ItemStack pageIcon = new ItemStack(Material.PAPER, 0);
	
	
	private final ShopGood[] goods;
	
	private final int inventorySlots;
	private final int contentSlots;
	private final int nextSlot;
	private final int previousSlot;
	
	private boolean singlePage = false;
	
	private Inventory[] inventories;
	private Map<String, Integer> viewers;
	
	private Paintball plugin;
	
	public ShopMenu(Paintball plugin, ShopGood[] goods) {
		this.plugin = plugin;
		this.goods = goods;
		
		//TODO translation support
		Utils.setItemMeta(nextIcon, ChatColor.RED + "Next Page ->", null);
		Utils.setItemMeta(prevIcon, ChatColor.RED + "<- Previous Page", null);
		
		// calculate needed rows:
		int neededRows = (int) Math.ceil((int) (goods.length / 9));
		// min 2, max 6 !
		int rows = Math.min(Math.max(neededRows, 2), 6);
		int contentRows = rows - 1;
		inventorySlots = rows * 9;
		contentSlots = contentRows * 9;
		nextSlot = inventorySlots - 1;
		previousSlot = nextSlot - 8;
		
		viewers = new HashMap<String, Integer>();
		
		int sizeY = 1 + (goods.length / 9);
		int num = 1 + (sizeY / contentRows);
		inventories = new Inventory[num];
		
		int left = goods.length;
		
		int pageSlot = previousSlot + 4;
		
		// one inventory page enough ? -> no page buttons needed
		if (left <= inventorySlots) {
			singlePage = true;
			inventories[0] = Bukkit.createInventory(null, inventorySlots, ChatColor.GREEN + "Paintball" + ChatColor.WHITE + " - " + ChatColor.GREEN + "Shop");
			int size = Math.min(left, contentSlots);
			left -= size;
			for (int j = 0; j < size; j++) {
				inventories[0].setItem(j, goods[j].getIcon());
			}
		} else {
			singlePage = false;
			for (int i = 0; i < num; i++) {
				inventories[i] = Bukkit.createInventory(null, inventorySlots, ChatColor.GREEN + "Paintball" + ChatColor.WHITE + " - " + ChatColor.GREEN + "Shop");
				int size = Math.min(left, contentSlots);
				left -= size;
				for (int j = 0; j < size; j++) {
					inventories[i].setItem(j, goods[j + i * contentSlots].getIcon());
				}
				// buttons:
				// only needed if num of inventories > 1
				ItemStack page = pageIcon.clone();
				page.setAmount(i + 1);
				Utils.setItemMeta(page, "Page " + (i + 1), null);
				inventories[i].setItem(previousSlot, prevIcon);
				inventories[i].setItem(pageSlot, page);
				inventories[i].setItem(nextSlot, nextIcon);
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
		return (page * (singlePage ? inventorySlots : contentSlots)) + slot;
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
		Paintball.instance.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
			
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
