package de.blablubbabc.paintball.shop;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.blablubbabc.paintball.utils.Utils;

public class ShopGood {
	private final String name;
	private final ItemStack icon;
	private final int price;
	private final ItemStack[] items;
	
	ShopGood(String name, List<String> description, ItemStack icon, int price, ItemStack[] items) {
		this.name = name;
		List<String> desc = new ArrayList<String>();
		desc.add(ChatColor.RED + "__Price: " + ChatColor.GREEN + price + "$");
		desc.addAll(description);
		desc.add(ChatColor.RED + "__Items:");
		for (ItemStack item : items) {
			ItemMeta meta = item.getItemMeta();
			if (meta.hasDisplayName()) {
				desc.add(ChatColor.GREEN + "- " + ChatColor.WHITE + item.getAmount() + "x " + ChatColor.AQUA + meta.getDisplayName());
			} else {
				desc.add(ChatColor.GREEN + "- " + ChatColor.WHITE + item.getAmount() + "x " 
						+ ChatColor.AQUA + item.getType().toString() + " (" + item.getTypeId() + ":" + item.getDurability() + ")");
			}
		}
		this.icon = Utils.setItemMeta(icon, name, desc);
		this.price = price;
		this.items = items;
	}
	
	public ItemStack getIcon() {
		return icon;
	}
	
	public void givePlayer(Player player) {
		player.getInventory().addItem(items.clone());
	}
	
	public String getName() {
		return name;
	}
	
	public int getPrice() {
		return this.price;
	}
	
	public ItemStack[] getItems(boolean clone) {
		return (clone ? this.items.clone() : this.items);
	}
}
