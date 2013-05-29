package de.blablubbabc.paintball.shop;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Shop {
	private static final ItemStack nextIcon = new ItemStack(Material.BOOK_AND_QUILL);
	private static final ItemStack prevIcon = new ItemStack(Material.BOOK_AND_QUILL);
	private static final ItemStack pageIcon = new ItemStack(Material.PAPER);
	
	static void init() {
		Utils.setItemMeta(nextIcon, ChatColor.RED + "Next Page ->", null);
		Utils.setItemMeta(prevIcon, ChatColor.RED + "<- Previous Page", null);
		Utils.setItemMeta(pageIcon, ChatColor.RED + "Page", null);
	}
	
	private final String name;
	private final Inventory[] inventories;
	private final ShopGood[] goods;
	private final Map<String, Integer> viewers = new HashMap<String, Integer>();
	
	Shop(String name, ShopGood[] goods) {
		this.name = name;
		this.goods = goods;
		
		int sizeY = 1 + (goods.length / 9);
		int num = 1 + (sizeY / 5);
		inventories = new Inventory[num];
		
		int left = goods.length;
		for (int i = 0; i < num; i++) {
			inventories[i] = Bukkit.createInventory(null, 54, name);
			int size = left;
			if (left >= 45) size = 45;
			left -= size;
			for (int j = 0; j < size; j++) {
				inventories[i].setItem(j, goods[j + i * 45].getIcon());
			}
			// buttons:
			ItemStack page = Shop.pageIcon.clone();
			page.setAmount(i + 1);
			inventories[i].setItem(45, Shop.prevIcon);
			inventories[i].setItem(49, page);
			inventories[i].setItem(53, Shop.nextIcon);
		}
	}
	
	public void click(PPlayer pplayer, int slot) {
		switch (slot) {
		case 45:
			openPreviousPage(pplayer.getPlayer());
			break;
		case 53:
			openNextPage(pplayer.getPlayer());
			break;
		default:
			buy(pplayer, getGood(viewers.get(pplayer.getName()), slot));
			break;
		}
	}
	
	private ShopGood getGood(int page, int slot) {
		return goods[page * 54 + slot];
	}
	
	@SuppressWarnings("deprecation")
	private void buy(PPlayer pplayer, ShopGood good) {
		Player player = pplayer.getPlayer();
		// INVENTORY FULL ?
		if(player.getInventory().firstEmpty() == -1) {
			player.sendMessage(Translator.getString("INVENTORY_FULL"));
			return;
		}
		// MESSAGE
		String message;
		// CASH
		if (!pplayer.hasHappyHour()) {
			PlayerStats pstats = pplayer.getStats().getPlayerStats();
			if (pstats.getStat(PlayerStat.CASH) < good.getPrice()) {
				pplayer.sendMessage(Translator.getString("NOT_ENOUGH_MONEY"));
				return;
			}
			pstats.addStat(PlayerStat.CASH, -good.getPrice());
			pstats.addStat(PlayerStat.CASH_SPENT, good.getPrice());
			// TODO GeneralStats ! cash_spent
			message = ChatColor.GREEN + "You bought '" + ChatColor.YELLOW + good.getName() + ChatColor.GREEN 
					+ "' for " + ChatColor.RED + good.getPrice() + "$";
		} else {
			message = ChatColor.GREEN + "You got '" + ChatColor.YELLOW + good.getName() + ChatColor.GREEN 
					+ "' for " + ChatColor.RED + "free" + ChatColor.GREEN + "!";
		}
		// ITEMS
		player.getInventory().addItem(good.getItems(true));
		player.updateInventory();
		//MESSAGE
		//TODO Message Translator
		player.sendMessage(message);	
	}
	
	private void openNextPage(Player player) {
		String name = player.getName();
		if (viewers.containsKey(name)) {
			int page = Math.min(viewers.get(name) + 1, inventories.length - 1);
			player.closeInventory();
			openPage(player, page);
		} else {
			open(player);
		}
	}
	
	private void openPreviousPage(Player player) {
		String name = player.getName();
		if (viewers.containsKey(name)) {
			int page = Math.max(viewers.get(name) - 1, 0);
			player.closeInventory();
			openPage(player, page);
		} else {
			open(player);
		}
	}
	
	private void openPage(Player player, int page) {
		if (player.openInventory(inventories[page]) != null) {
			viewers.put(player.getName(), page);
		}
	}
	
	public void open(Player player) {
		openPage(player, 0);
	}
	
	public void close(String playerName) {
		if (viewers.containsKey(playerName)) {
			viewers.remove(playerName);
		}
	}
	
	public boolean isShopping(String playerName) {
		return viewers.containsKey(playerName);
	}
	
	public String getName() {
		return name;
	}
	
}
