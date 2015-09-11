package de.blablubbabc.paintball.gadgets;

import org.bukkit.inventory.ItemStack;

public class Gift {
	private final String message;
	private final ItemStack item;
	private final double chance;

	public Gift(int id, short sub, int amount, double chance, String message) {
		this.message = message;
		this.chance = chance;
		this.item = new ItemStack(id, amount, sub);
	}
	
	public ItemStack getItem(boolean clone) {
		return (clone ? item.clone():item);
	}
	
	public String getMessage() {
		return message;
	}
	
	public double getChance() {
		return chance;
	}
}
