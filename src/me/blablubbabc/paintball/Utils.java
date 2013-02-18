package me.blablubbabc.paintball;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Utils {
	public static boolean isEmptyInventory(Player p) {
		for (ItemStack i : p.getInventory()) {
			if (i == null)
				continue;
			if (i.getTypeId() != 0)
				return false;
		}
		for (ItemStack i : p.getInventory().getArmorContents()) {
			if (i == null)
				continue;
			if (i.getTypeId() != 0)
				return false;
		}
		return true;
	}

	public static void clearInv(Player p) {
		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
	}

	public static void removeInventoryItems(Inventory inv, ItemStack item) {
		removeInventoryItems(inv, item.getType(), item.getAmount());
	}

	public static void removeInventoryItems(Inventory inv, Material type, int amount) {
		for (ItemStack is : inv.getContents()) {
			if (is != null && is.getType() == type) {
				int newamount = is.getAmount() - amount;
				if (newamount > 0) {
					is.setAmount(newamount);
					break;
				} else {
					inv.remove(is);
					amount = -newamount;
					if (amount == 0)
						break;
				}
			}
		}
	}

	// ///////////////////////////
	public static final BlockFace[] axis = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH,
			BlockFace.WEST };
	public static final BlockFace[] radial = { BlockFace.NORTH, BlockFace.NORTH_EAST,
			BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST,
			BlockFace.WEST, BlockFace.NORTH_WEST };

	/**
	 * Gets the horizontal Block Face from a given yaw angle<br>
	 * This includes the NORTH_WEST faces
	 * 
	 * @param yaw
	 *            angle
	 * @return The Block Face of the angle
	 */
	public static BlockFace yawToFace(float yaw) {
		return yawToFace(yaw, true);
	}

	/**
	 * Gets the horizontal Block Face from a given yaw angle
	 * 
	 * @param yaw
	 *            angle
	 * @param useSubCardinalDirections
	 *            setting, True to allow NORTH_WEST to be returned
	 * @return The Block Face of the angle
	 */
	public static BlockFace yawToFace(float yaw, boolean useSubCardinalDirections) {
		if (useSubCardinalDirections) {
			return radial[Math.round(yaw / 45f) & 0x7];
		} else {
			return axis[Math.round(yaw / 90f) & 0x3];
		}
	}
}
