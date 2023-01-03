/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.utils.KeyValuePair;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class ShopGood {

	/*
	 * - 10-Balls-SNOWBALL-0-10-0
	 * - 50-Balls-SNOWBALL-0-50-0
	 * - 100-Balls-SNOWBALL-0-100-0
	 * - 1-Grenade Mark 2-SLIME_BALL-0-15-1
	 */

	private String name = "empty";
	private ItemStack itemstack = null;
	private int price = 0;
	private int neededRank = 0;
	private String slot = "empty";
	private boolean empty = false;

	private ItemStack icon;

	public ShopGood(String slot) {
		String[] split = slot.split("-");
		if (split.length == 5 || split.length == 6) {
			this.name = split[1];
			int amount = isInteger(split[0]);
			Material type = Material.matchMaterial(split[2]);
			int damage = isInteger(split[3]);
			this.price = isInteger(split[4]);

			if (split.length == 6) {
				this.neededRank = isInteger(split[5]);
				if (this.neededRank < 0) this.neededRank = 0;
			}

			if (amount <= 0 || type == null || damage < 0 || price < 0 || this.name == null || this.name.isEmpty()) {
				this.empty = true;
			} else {
				ItemStack item = new ItemStack(type, amount);
				if (damage != 0) {
					Utils.setDamage(item, damage);
				}
				this.itemstack = Paintball.getInstance().weaponManager.setMeta(item);

				Map<String, String> vars = new HashMap<String, String>();
				vars.put("amount", split[0]);
				vars.put("good", split[1]);
				vars.put("price", split[4]);
				this.slot = Translator.getString("SHOP_GOOD", vars);
			}
		} else {
			this.empty = true;
		}

		// ICON:
		if (this.empty) {
			this.slot = Translator.getString("SHOP_EMPTY");
			this.icon = new ItemStack(Material.OAK_SIGN);
			List<String> desc = new ArrayList<String>();
			desc.add(ChatColor.RED + this.slot);

			this.icon = Utils.setItemMeta(this.icon, name, desc);

		} else {
			this.icon = itemstack.clone();
			if (itemstack.getAmount() > 64) this.icon.setAmount(1);

			List<String> desc = new ArrayList<String>();
			desc.add(Translator.getString("SHOP_MENU_PRICE", new KeyValuePair("price", String.valueOf(price))));
			desc.add(Translator.getString("SHOP_MENU_ITEM"));

			ItemMeta meta = itemstack.getItemMeta();

			KeyValuePair item_amount = new KeyValuePair("item_amount", String.valueOf(itemstack.getAmount()));

			if (meta.hasDisplayName()) {
				KeyValuePair item_name = new KeyValuePair("item_name", meta.getDisplayName());
				desc.add(Translator.getString("SHOP_MENU_ITEM_PAINTBALL", item_amount, item_name));
			} else {
				KeyValuePair item_name = new KeyValuePair("item_name", itemstack.getType().toString());
				desc.add(Translator.getString("SHOP_MENU_ITEM_OTHER", item_amount, item_name,
						new KeyValuePair("item_type", itemstack.getType().name()),
						new KeyValuePair("item_damage", String.valueOf(Utils.getDamage(itemstack)))));
			}

			this.icon = Utils.setItemMeta(this.icon, name, desc);
		}
	}

	private int isInteger(String s) {
		try {
			Integer a = Integer.parseInt(s);
			return a;
		} catch (Exception e) {
			return 0;
		}
	}

	public boolean isEmpty() {
		return this.empty;
	}

	public String getName() {
		return this.name;
	}

	public ItemStack getItemStack() {
		return this.itemstack.clone();
	}

	public String getSlot() {
		return this.slot;
	}

	public int getPrice() {
		return this.price;
	}

	public int getNeededRank() {
		return neededRank;
	}

	public ItemStack getIcon() {
		return icon;
	}
}
