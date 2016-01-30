/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Rank implements Comparable<Rank> {
	private final String name;
	private final int neededPoints;
	private final String prefix;
	private final ItemStack helmet;
	private final ItemStack chestplate;
	private final ItemStack leggings;
	private final ItemStack boots;
	
	// will be set after sorting of all ranks:
	private int rankIndex = 0;
	
	public Rank(String name, int neededPoints, String prefix, ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
		this.name = name;
		this.neededPoints = neededPoints;
		this.prefix = prefix;
		this.helmet = helmet;
		this.chestplate = chestplate;
		this.leggings = leggings;
		this.boots = boots;
	}
	
	@SuppressWarnings("deprecation")
	public void assignArmorToPlayer(Player player) {
		if (player != null) {
			PlayerInventory inv = player.getInventory();
			if (helmet != null) inv.setHelmet(helmet.clone());
			if (chestplate != null) inv.setChestplate(chestplate.clone());
			if (leggings != null) inv.setLeggings(leggings.clone());
			if (boots != null) inv.setBoots(boots.clone());
			player.updateInventory();
		}
	}
	
	public ItemStack getHelmet() {
		return helmet;
	}
	
	public ItemStack getChestplate() {
		return chestplate;
	}
	
	public ItemStack getLeggings() {
		return leggings;
	}
	
	public ItemStack getBoots() {
		return boots;
	}
	
	public String getName() {
		return name;
	}

	public String getPrefix() {
		return prefix;
	}

	public int getNeededPoints() {
		return neededPoints;
	}

	@Override
	public int compareTo(Rank other) {
		if (other == null) {
			throw new IllegalArgumentException();
		}
		return this.getNeededPoints() - other.getNeededPoints();
		
	}

	public int getRankIndex() {
		return rankIndex;
	}

	void setRankIndex(int rankIndex) {
		this.rankIndex = rankIndex;
	}
	
}
