package de.blablubbabc.paintball.shop;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import de.blablubbabc.paintball.utils.Utils;

public class ShopItem {
	private final String name;
	private final List<String> description;
	private final int id;
	private final short subid;
	
	ShopItem(String name, int id, short subid, List<String> description) {
		this.name = name;
		this.id = id;
		this.subid = subid;
		this.description = description;
	}
	
	public ItemStack createItem(int amount) {
		ItemStack item = new ItemStack(id, amount, subid);
		return Utils.setItemMeta(item, name, description);
	}
	
	public boolean matches(String name) {
		return Utils.equalsIgnoreColor(this.name, name);
	}
	
}
