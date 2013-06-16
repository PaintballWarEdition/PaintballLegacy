package de.blablubbabc.paintball;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Rank {
	private final int neededPoints;
	private final String prefix;
	private final ChatColor messageColor;
	private final ItemStack helmet;
	private final ItemStack chestplate;
	private final ItemStack leggings;
	private final ItemStack boots;
	
	public Rank(int neededPoints, String prefix, ChatColor messageColor, ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
		this.neededPoints = neededPoints;
		this.prefix = prefix;
		this.messageColor = messageColor;
		this.helmet = helmet;
		this.chestplate = chestplate;
		this.leggings = leggings;
		this.boots = boots;
	}
	
	@SuppressWarnings("deprecation")
	public void assignArmorToPlayer(Player player) {
		if (player != null) {
			PlayerInventory inv = player.getInventory();
			if (helmet != null) inv.setHelmet(helmet);
			if (chestplate != null) inv.setHelmet(chestplate);
			if (leggings != null) inv.setHelmet(leggings);
			if (boots != null) inv.setHelmet(boots);
			player.updateInventory();
		}
	}

	public String getPrefix() {
		return prefix;
	}

	public ChatColor getMessageColor() {
		return messageColor;
	}

	public int getNeededPoints() {
		return neededPoints;
	}
	
}
