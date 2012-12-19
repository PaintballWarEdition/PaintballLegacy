package me.blablubbabc.paintball;

import org.bukkit.inventory.ItemStack;

public class Gift {
	public final String message;
	public final ItemStack item;
	public final double chance;

	public Gift(int id, short sub, int amount, double chance, String message) {
		this.message = message;
		this.chance = chance;
		this.item = new ItemStack(id, amount, sub);
	}
}
