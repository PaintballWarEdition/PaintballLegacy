/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class SoftLocation {
	private String worldName;
	private double x;
	private double y;
	private double z;
	private float yaw;
	private float pitch;
	
	public SoftLocation(Location location) {
		this(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	}
	
	public SoftLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
		this.worldName = worldName;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public String getWorldName() {
		return worldName;
	}

	public void setWorldName(String worldName) {
		this.worldName = worldName;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}
	
	public Location getBukkitLocation() {
		World world = Bukkit.getServer().getWorld(worldName);
		if (world == null) return null;
		return new Location(world, x, y, z, pitch, yaw);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final SoftLocation other = (SoftLocation) obj;

		if (this.worldName != other.worldName && (this.worldName == null || !this.worldName.equals(other.worldName))) {
			return false;
		}
		if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
			return false;
		}
		if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
			return false;
		}
		if (Double.doubleToLongBits(this.z) != Double.doubleToLongBits(other.z)) {
			return false;
		}
		if (Float.floatToIntBits(this.yaw) != Float.floatToIntBits(other.yaw)) {
			return false;
		}
		if (Float.floatToIntBits(this.pitch) != Float.floatToIntBits(other.pitch)) {
			return false;
		}
		return true;

		
		//return location.getWorld().getName().equals(worldName) && location.getBlockX() == Location.locToBlock(x) && location.getBlockY() == Location.locToBlock(y) && location.getBlockZ() == Location.locToBlock(z);
	}
	
	@Override
	public int hashCode() {
		int hash = 3;

		hash = 19 * hash + (this.worldName != null ? this.worldName.hashCode() : 0);
		hash = 19 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
		hash = 19 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
		hash = 19 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
		hash = 19 * hash + Float.floatToIntBits(this.yaw);
		hash = 19 * hash + Float.floatToIntBits(this.pitch);
		return hash;
	}
	
	@Override
	public String toString() {
		return worldName + ";" + x + ";" + y + ";" + z + ";" + yaw + ";" + pitch;
	}
	
	// statics
	
	public static List<SoftLocation> getFromStringList(List<String> strings) {
		List<SoftLocation> softLocs = new ArrayList<SoftLocation>();
		for (String s : strings) {
			SoftLocation soft = getFromString(s);
			if (soft != null) softLocs.add(soft);
		}
		return softLocs;
	}
	
	public static List<String> toStringList(List<SoftLocation> softLocs) {
		List<String> strings = new ArrayList<String>();
		for (SoftLocation soft : softLocs) {
			if (soft != null) strings.add(soft.toString());
		}
		return strings;
	}
	
	public static SoftLocation getFromString(String string) {
		if (string == null) return null;
		String[] split = string.split(";");
		if (split.length != 4 && split.length != 6) return null;
		
		String worldName = split[0];
		if (worldName == null) return null;
		Double x = Utils.parseDouble(split[1]);
		if (x == null) return null;
		Double y = Utils.parseDouble(split[2]);
		if (y == null) return null;
		Double z = Utils.parseDouble(split[3]);
		if (z == null) return null;
		
		// optional
		Float yaw = 0.0F;
		Float pitch = 0.0F;
		if (split.length == 6) {
			yaw = Utils.parseFloat(split[4]);
			if (yaw == null) yaw = 0.0F;
			pitch = Utils.parseFloat(split[5]);
			if (pitch == null) pitch = 0.0F;
		}
		
		return new SoftLocation(worldName, x, y, z, yaw, pitch);
	}
	
}
