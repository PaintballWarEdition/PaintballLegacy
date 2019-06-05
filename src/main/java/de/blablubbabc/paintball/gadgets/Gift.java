/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.gadgets;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import de.blablubbabc.paintball.utils.Utils;

public class Gift {

	private final String message;
	private final ItemStack item;
	private final double chance;

	public Gift(Material itemType, int damage, int amount, double chance, String message) {
		this.message = message;
		this.chance = chance;
		this.item = new ItemStack(itemType, amount);
		if (damage != 0) {
			Utils.setDamage(item, damage);
		}
	}

	public ItemStack getItem(boolean clone) {
		return (clone ? item.clone() : item);
	}

	public String getMessage() {
		return message;
	}

	public double getChance() {
		return chance;
	}
}
