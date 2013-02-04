package me.blablubbabc.paintball.extras;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.FallingBlock;

public class FallingBlocks {
	private static Set<UUID> fallingBlocks = new HashSet<UUID>();
	
	public static void addFallingBlock(FallingBlock f) {
		fallingBlocks.add(f.getUniqueId());
	}
	
	public static void removeFallingBlock(FallingBlock f) {
		fallingBlocks.remove(f.getUniqueId());
	}
	
	public static boolean containsFallingBlock(FallingBlock f) {
		return fallingBlocks.contains(f.getUniqueId());
	}
}
