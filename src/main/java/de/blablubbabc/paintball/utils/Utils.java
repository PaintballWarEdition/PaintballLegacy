/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class Utils {

	public static Random random = new Random();
	private static HashSet<Material> transparentBlocks = new HashSet<Material>();

	public static void init() {
		transparentBlocks.add(Material.AIR);
		transparentBlocks.add(Material.WATER);
		transparentBlocks.add(Material.LAVA);
		transparentBlocks.add(Material.FIRE);
		transparentBlocks.add(Material.NETHER_PORTAL);
		transparentBlocks.add(Material.END_PORTAL);
		transparentBlocks.add(Material.PAINTING);

		// used by airstrike and orbital strike:
		transparentBlocks.add(Material.OAK_FENCE);
		transparentBlocks.add(Material.NETHER_BRICK_FENCE);

		// all directions
		upVectors.add(new Vector(1, 0, 0));
		upVectors.add(new Vector(0, 1, 0));
		upVectors.add(new Vector(0, 0, 1));
		upVectors.add(new Vector(1, 1, 0));
		upVectors.add(new Vector(1, 0, 1));
		upVectors.add(new Vector(0, 1, 1));
		upVectors.add(new Vector(1, 1, 1));
		upVectors.add(new Vector(-1, 0, 0));
		upVectors.add(new Vector(0, 0, -1));
		upVectors.add(new Vector(-1, 0, -1));
		upVectors.add(new Vector(1, 0, -1));
		upVectors.add(new Vector(0, 1, -1));
		upVectors.add(new Vector(-1, 1, 0));
		upVectors.add(new Vector(-1, 0, 1));
		upVectors.add(new Vector(1, 1, -1));
		upVectors.add(new Vector(-1, 1, 1));
		upVectors.add(new Vector(-1, 1, -1));

		downVectors.add(new Vector(-1, -1, 1));
		downVectors.add(new Vector(-1, -1, -1));
		downVectors.add(new Vector(1, -1, -1));
		downVectors.add(new Vector(1, -1, 1));
		downVectors.add(new Vector(0, -1, 1));
		downVectors.add(new Vector(0, -1, -1));
		downVectors.add(new Vector(1, -1, 0));
		downVectors.add(new Vector(-1, -1, 0));
		downVectors.add(new Vector(0, -1, 0));
	}

	public static HashSet<Material> getTransparentBlocks() {
		return transparentBlocks;
	}

	public static String translateColorCodesToAlternative(char altColorChar, String textToTranslate) {
		char[] b = textToTranslate.toCharArray();
		for (int i = 0; i < b.length - 1; i++) {
			if (b[i] == ChatColor.COLOR_CHAR) {
				ChatColor color = ChatColor.getByChar(b[i + 1]);
				if (color != null) {
					b[i] = altColorChar;
					// needed?
					b[i + 1] = color.getChar();
				}
			}
		}
		return new String(b);
	}

	public static Location getRightHeadLocation(Vector viewDirection, Location eyeLocation) {
		return eyeLocation.add(new Vector(-viewDirection.getZ(), 0.0, viewDirection.getX()).normalize().multiply(0.2));
	}

	public static void forceShowPlayer(Player player, Player to) {
		to.showPlayer(player); // legacy: no plugin argument
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
			to.showPlayer(plugin, player);
		}
		// TODO this might not work if there are unloaded plugins which have hidden the player and
		// for which a reference still exist somewhere
	}

	public static boolean isSign(Material material) {
		if (material == null) return false;
		return material.data == org.bukkit.block.data.type.Sign.class || material.data == org.bukkit.block.data.type.WallSign.class;
	}

	public static void setDamage(ItemStack item, int damage) {
		ItemMeta itemMeta = item.getItemMeta();
		if (itemMeta instanceof Damageable) {
			((Damageable) itemMeta).setDamage(damage);
			item.setItemMeta(itemMeta);
		}
	}

	public static int getDamage(ItemStack item) {
		int damage = 0;
		if (item.hasItemMeta()) {
			ItemMeta itemMeta = item.getItemMeta();
			if (itemMeta instanceof Damageable) {
				damage = ((Damageable) itemMeta).getDamage();
			}
		}
		return damage;
	}

	// /////////////////////////////////////////////////////////////

	// LOCATIONS TO / FROM STRING

	public static String LocationToString(Location loc) {
		return loc.getWorld().getName() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getYaw() + ";" + loc.getPitch();
	}

	public static Location StringToLocation(String string) {
		if (string == null) return null;
		String[] split = string.split(";");
		if (split.length != 4 && split.length != 6) return null;

		World world = Bukkit.getWorld(split[0]);
		if (world == null) return null;
		Double x = parseDouble(split[1]);
		if (x == null) return null;
		Double y = parseDouble(split[2]);
		if (y == null) return null;
		Double z = parseDouble(split[3]);
		if (z == null) return null;

		Float yaw = 0.0F;
		Float pitch = 0.0F;
		if (split.length == 6) {
			yaw = parseFloat(split[4]);
			if (yaw == null) yaw = 0.0F;
			pitch = parseFloat(split[5]);
			if (pitch == null) pitch = 0.0F;
		}

		return new Location(world, x, y, z, yaw, pitch);
	}

	public static List<Location> StringsToLocations(List<String> strings) {
		List<Location> locs = new ArrayList<Location>();
		for (String s : strings) {
			Location loc = StringToLocation(s);
			if (loc != null) locs.add(loc);
		}
		return locs;
	}

	public static List<String> LocationsToStrings(List<Location> locs) {
		List<String> strings = new ArrayList<String>();
		for (Location loc : locs) {
			if (loc != null) strings.add(LocationToString(loc));
		}
		return strings;
	}

	public static Integer parseInteger(String string) {
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static Float parseFloat(String string) {
		try {
			return Float.parseFloat(string);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static Double parseDouble(String string) {
		try {
			return Double.parseDouble(string);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	// /////////////////////////////////////////////////////////////

	public static boolean isEmptyInventory(Player player) {
		PlayerInventory inv = player.getInventory();
		int size = inv.getSize();
		for (int i = 0; i < size; i++) {
			ItemStack item = inv.getItem(i);
			if (item == null) continue;
			if (item.getType() != Material.AIR) {
				return false;
			}
		}
		return true;
	}

	public static void clearInv(Player player) {
		player.closeInventory();
		player.getInventory().clear();
	}

	public static void updatePlayerInventoryLater(Plugin plugin, final Player player) {
		plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
			@Override
			public void run() {
				player.updateInventory();
			}
		});
	}

	/*
	 * public static void removeInventoryItems(Inventory inv, ItemStack item) {
	 * removeInventoryItems(inv, item, item.getAmount()); }
	 */

	// removes items from storage slots
	public static void removeInventoryItems(Inventory inv, ItemStack item, int amount) {
		for (ItemStack is : inv.getStorageContents()) {
			if (is != null && is.isSimilar(item)) {
				int newamount = is.getAmount() - amount;
				if (newamount > 0) {
					is.setAmount(newamount);
					break;
				} else {
					inv.remove(is);
					amount = -newamount;
					if (amount == 0) break;
				}
			}
		}
	}

	public static ItemStack setLeatherArmorColor(ItemStack item, Color color) {
		if (item != null && color != null) {
			ItemMeta itemMeta = item.getItemMeta();
			if (itemMeta instanceof LeatherArmorMeta) {
				LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
				meta.setColor(color);
				item.setItemMeta(meta);
			}
		}
		return item;
	}

	public static ItemStack setItemMeta(ItemStack item, String name, List<String> description) {
		ItemMeta meta = item.getItemMeta();
		if (name != null) meta.setDisplayName(name);
		if (description != null) meta.setLore(description);
		item.setItemMeta(meta);
		return item;
	}

	private static List<Vector> upVectors = new ArrayList<Vector>();
	private static List<Vector> downVectors = new ArrayList<Vector>();

	public static List<Vector> getUpVectors() {
		return upVectors;
	}

	public static List<Vector> getDownVectors() {
		return downVectors;
	}

	public static List<Vector> getDirections() {
		ArrayList<Vector> vectors = new ArrayList<Vector>();
		vectors.addAll(upVectors);
		vectors.addAll(downVectors);
		return vectors;
	}

	// ////////////////////////////

	public static int calculateQuote(int top, int bottom) {
		return (int) ((top != 0 ? top : 1) * 100) / (bottom != 0 ? bottom : 1);
	}

	// ///////////////////////////

	public static float getLookAtYaw(Vector motion) {
		double dx = motion.getX();
		double dz = motion.getZ();
		double yaw = 0;
		// Set yaw
		if (dx != 0) {
			// Set yaw start value based on dx
			if (dx < 0) {
				yaw = 1.5 * Math.PI;
			} else {
				yaw = 0.5 * Math.PI;
			}
			yaw -= Math.atan(dz / dx);
		} else if (dz < 0) {
			yaw = Math.PI;
		}
		return (float) (-yaw * 180 / Math.PI);
	}

	public static Vector rotateAxis(Vector dir, Vector n, int angleD) {
		double angleR = Math.toRadians(angleD);
		double x = dir.getX();
		double y = dir.getY();
		double z = dir.getZ();

		double n1 = n.getX();
		double n2 = n.getY();
		double n3 = n.getZ();

		double cos = Math.cos(angleR);
		double sin = Math.sin(angleR);
		return new Vector(x * (n1 * n1 * (1 - cos) + cos) + y * (n2 * n1 * (1 - cos) + n3 * sin) + z * (n3 * n1 * (1 - cos) - n2 * sin),
				x * (n1 * n2 * (1 - cos) - n3 * sin) + y * (n2 * n2 * (1 - cos) + cos) + z * (n3 * n2 * (1 - cos) + n1 * sin),
				x * (n1 * n3 * (1 - cos) + n2 * sin) + y * (n2 * n3 * (1 - cos) - n1 * sin) + z * (n3 * n3 * (1 - cos) + cos));
	}

	public static Vector rotateYAxis(Vector dir, double angleD) {
		double angleR = Math.toRadians(angleD);
		double x = dir.getX();
		double z = dir.getZ();
		double cos = Math.cos(angleR);
		double sin = Math.sin(angleR);
		return (new Vector(x * cos + z * (-sin), 0.0, x * sin + z * cos)).normalize();
	}

	// loading chunks? if yes -> issues with that?
	public Set<Entity> getNearbyEntities(Location location, int radius) {
		int radius2 = radius * radius;
		int chunkRadius = radius < 16 ? 1 : (int) (radius / 16);
		Set<Entity> entities = new HashSet<Entity>();
		for (int chunkX = -chunkRadius; chunkX <= chunkRadius; chunkX++) {
			for (int chunkZ = -chunkRadius; chunkZ <= chunkRadius; chunkZ++) {
				Chunk chunk = location.getWorld().getChunkAt(chunkX, chunkZ);
				for (Entity entity : chunk.getEntities()) {
					if (entity.getLocation().distanceSquared(location) <= radius2) {
						entities.add(entity);
					}
				}
			}
		}
		return entities;
	}

	// ///////////////////////////
	public static final BlockFace[] axis = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
	public static final BlockFace[] radial = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH,
			BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };

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

	// ////////

	// THREAD-SAFE SCHEDULER SHORTCUTS

	public static boolean runTask(Plugin plugin, Runnable task) {
		return runTaskLater(plugin, task, 0L);
	}

	public static boolean runTaskLater(Plugin plugin, Runnable task, long delay) {
		assert plugin != null && task != null;
		// reduces the chance for the following task registration to fail:
		if (!plugin.isEnabled()) return false;
		try {
			Bukkit.getScheduler().runTaskLater(plugin, task, delay);
			return true;
		} catch (IllegalPluginAccessException e) {
			// couldn't register task: the plugin got disabled just now
			return false;
		}
	}

	public static boolean runAsyncTask(Plugin plugin, Runnable task) {
		return runAsyncTaskLater(plugin, task, 0L);
	}

	public static boolean runAsyncTaskLater(Plugin plugin, Runnable task, long delay) {
		assert plugin != null && task != null;
		// reduces the chance for the following task registration to fail:
		if (!plugin.isEnabled()) return false;
		try {
			Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay);
			return true;
		} catch (IllegalPluginAccessException e) {
			// couldn't register task: the plugin got disabled just now
			return false;
		}
	}
}
