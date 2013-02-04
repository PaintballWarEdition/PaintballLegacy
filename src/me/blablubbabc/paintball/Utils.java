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
	///////////////////////
	/**
	    * Gets entities inside a cone.
	    * @see Utilities#getPlayersInCone(List, Location, int, int, int)
	    *
	    * @param entities - {@code List<Entity>}, list of nearby entities
	    * @param startpoint - {@code Location}, center point
	    * @param radius - {@code int}, radius of the circle
	    * @param degrees - {@code int}, angle of the cone
	    * @param direction - {@code int}, direction of the cone
	    * @return {@code List<Entity>} - entities in the cone
	    */
	    public static List<Entity> getEntitiesInCone(List<Entity> entities, Location startpoint, int radius, int degrees, int direction) {
	        List<Entity> newEntities = new ArrayList<Entity>();
	     
	        int[] startPos = new int[] { (int)startpoint.getX(), (int)startpoint.getZ() };
	     
	        int[] endA = new int[] { (int)(radius * Math.cos(direction - (degrees / 2))), (int)(radius * Math.sin(direction - (degrees / 2))) };
	   
	        for(Entity e : entities)
	        {
	            Location l = e.getLocation();       
	            int[] entityVector = getVectorForPoints(startPos[0], startPos[1], l.getBlockX(), l.getBlockY());
	 
	            double angle = getAngleBetweenVectors(endA, entityVector);
	            if(Math.toDegrees(angle) < degrees && Math.toDegrees(angle) > 0)
	                newEntities.add(e);
	        }
	        return newEntities;
	    }
	    /**
	    * Created an integer vector in 2d between two points
	    *
	    * @param x1 - {@code int}, X pos 1
	    * @param y1 - {@code int}, Y pos 1
	    * @param x2 - {@code int}, X pos 2
	    * @param y2 - {@code int}, Y pos 2
	    * @return {@code int[]} - vector
	    */
	    public static int[] getVectorForPoints(int x1, int y1, int x2, int y2) {
	        return new int[] { x2 - x1, y2 - y1 };
	    }
	    /**
	    * Get the angle between two vectors.
	    *
	    * @param vector1 - {@code int[]}, vector 1
	    * @param vector2 - {@code int[]}, vector 2
	    * @return {@code double} - angle
	    */
	    public static double getAngleBetweenVectors(int[] vector1, int[] vector2) {
	        return Math.atan2(vector2[1], vector2[0]) - Math.atan2(vector1[1], vector1[0]);
	    }
	    
	    /////////////////////////////
	    public static final BlockFace[] axis = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
	    public static final BlockFace[] radial = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };
	   
	    /**
	    * Gets the horizontal Block Face from a given yaw angle<br>
	    * This includes the NORTH_WEST faces
	    *
	    * @param yaw angle
	    * @return The Block Face of the angle
	    */
	    public static BlockFace yawToFace(float yaw) {
	        return yawToFace(yaw, true);
	    }
	 
	    /**
	    * Gets the horizontal Block Face from a given yaw angle
	    *
	    * @param yaw angle
	    * @param useSubCardinalDirections setting, True to allow NORTH_WEST to be returned
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
