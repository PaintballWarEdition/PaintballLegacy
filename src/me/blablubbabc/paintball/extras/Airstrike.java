package me.blablubbabc.paintball.extras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import me.blablubbabc.paintball.Match;
import me.blablubbabc.paintball.Paintball;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Airstrike{
	private static ArrayList<Airstrike> airstrikes = new ArrayList<Airstrike>();

	public static synchronized void addAirstrike(Airstrike airstrike) {
		airstrikes.add(airstrike);
	}

	public static synchronized void removeAirstrike(Airstrike airstrike) {
		airstrikes.remove(airstrike);
	}

	public static synchronized ArrayList<Airstrike> getAirstrikes(Match match) {
		ArrayList<Airstrike> list = new ArrayList<Airstrike>();
		for (Airstrike a : airstrikes) {
			if (a.match.equals(match)) {
				list.add(a);
			}
		}
		return list;
	}

	public static synchronized ArrayList<Airstrike> getAirstrikes(Player player) {
		ArrayList<Airstrike> list = new ArrayList<Airstrike>();
		for (Airstrike a : airstrikes) {
			if (a.player.equals(player)) {
				list.add(a);
			}
		}
		return list;
	}
	
	public final Player player;
	public final Match match;
	public final Paintball plugin;

	public Airstrike(Player player, Match match, Paintball plugin) {
		this.match = match;
		this.player = player;
		this.plugin = plugin;
		addAirstrike(this);
	}
	
	private static HashMap<Player, Block> marks = new HashMap<Player, Block>();
	private static HashMap<Player, Block> finalmarks = new HashMap<Player, Block>();
	private static int task;
	//public static boolean active = false;
	
	public static void call(final Paintball plugin, final Player player, final Match match) {
		if(marked(player)) {
			//active = true;
			final Airstrike a = new Airstrike(player, match, plugin);
			
			Block block = marks.get(player);
			demark(player);
			finalMark(block, player);
			//airstrike
			int range = plugin.airstrikeRange;
			Vector pv = new Vector(player.getLocation().getX(),block.getLocation().getY()+plugin.airstrikeHeight,player.getLocation().getZ());
			Vector bv =	new Vector(block.getLocation().getX(),block.getLocation().getY()+plugin.airstrikeHeight,block.getLocation().getZ());
			Vector bp = new Vector() ; bp.copy(bv); bp.subtract(pv).normalize();
			final Vector bpr = new Vector(-bp.getZ(),0,bp.getX()); bpr.normalize();
			Location b1 = bv.clone().toLocation(player.getWorld());
			b1.subtract(bpr.clone().multiply(range));
			//Block b2 = player.getWorld().getBlockAt(block.getLocation().add(bp.multiply(range)));
			final double bombDiff = ( (2*range) / plugin.airstrikeBombs );
			
			final LinkedList<Location> bombs = new LinkedList<Location>();
			for(int i = 1; i <= plugin.airstrikeBombs; i++) {
				bombs.add(b1.clone().add(bpr.clone().multiply((bombDiff*i))));
			}
			player.sendMessage(plugin.t.getString("AIRSTRKE_CALLED"));
			//chicken
			Location lc = new Location(player.getWorld(), bombs.getFirst().getX(), bombs.getFirst().getY(), bombs.getFirst().getZ(), 0, getLookAtYaw(bpr));
			final Entity chick = player.getWorld().spawnEntity(lc.add(new Vector(0,5,0)), EntityType.CHICKEN);
			task = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
				int i = 0;
				@Override
				public void run() {
					Location l = bombs.get(i);
					Egg egg = player.launchProjectile(Egg.class);
					egg.teleport(l);
					egg.setVelocity(new Vector(0,0,0));
					//Egg egg = player.getWorld().spawn(l, Egg.class);
					Grenade.eggThrow(player, egg);
					//PlayerEggThrowEvent event = new PlayerEggThrowEvent(player, egg, false, (byte) 0, EntityType.CHICKEN);
					//ProjectileLaunchEvent event = new ProjectileLaunchEvent(egg);
					//event.getEntity().setShooter(player);
					//plugin.getServer().getPluginManager().callEvent(event);
					chick.setVelocity(bpr.clone().multiply(bombDiff/5));
					i++;
					if(i > (bombs.size() - 1)) {
						plugin.getServer().getScheduler().cancelTask(task);
						definalMark(player);
						chick.remove();
						//active = false;
						removeAirstrike(a);		
					}
				}
			}, 0L, 5L);
		}
	}
	
	private static float getLookAtYaw(Vector motion) {
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
	
	private static void finalMark(Block block, Player player) {
		finalmarks.put(player, block);
		LinkedList<Block> blocks = new LinkedList<Block>();
		blocks.add(block.getRelative(BlockFace.UP));
		for(int i = 0; i < 10; i++) {
			blocks.add(blocks.getLast().getRelative(BlockFace.UP));
		}
		for(Block b : blocks) {
			Location loc = b.getLocation();
			player.sendBlockChange(loc, Material.FENCE, (byte) 0);
		}
		player.sendBlockChange(blocks.getLast().getRelative(BlockFace.UP).getLocation(), Material.TORCH, (byte) 0);
	}
	
	private static void definalMark(Player player) {
		if (finalmarks.get(player) != null) {
			Block block = finalmarks.get(player);
			LinkedList<Block> blocks = new LinkedList<Block>();
			blocks.add(block.getRelative(BlockFace.UP));
			for(int i = 0; i < 11; i++) {
				blocks.add(blocks.getLast().getRelative(BlockFace.UP));
			}
			for(Block b : blocks) {
				Location loc = b.getLocation();
				player.sendBlockChange(loc, player.getWorld().getBlockAt(loc).getType(), player.getWorld().getBlockAt(loc).getData());
			}
			finalmarks.remove(player);
		}
	}
	
	public static void mark(Block block, Player player) {
		marks.put(player, block);
		LinkedList<Block> blocks = new LinkedList<Block>();
		blocks.add(block.getRelative(BlockFace.UP));
		for(int i = 0; i < 10; i++) {
			blocks.add(blocks.getLast().getRelative(BlockFace.UP));
		}
		for(Block b : blocks) {
			Location loc = b.getLocation();
			player.sendBlockChange(loc, Material.FENCE, (byte) 0);
		}
	}
	
	public static void demark(Player player) {
		
		if (marked(player)) {
			Block block = marks.get(player);
			LinkedList<Block> blocks = new LinkedList<Block>();
			blocks.add(block.getRelative(BlockFace.UP));
			for(int i = 0; i < 10; i++) {
				blocks.add(blocks.getLast().getRelative(BlockFace.UP));
			}
			for (Block b : blocks) {
				Location loc = b.getLocation();
				player.sendBlockChange(loc, player.getWorld().getBlockAt(loc).getType(), player.getWorld().getBlockAt(loc).getData());
			}
			marks.remove(player);
		}
	}
	
	public static boolean isBlock(Block block, Player player) {
		if(!marked(player)) return false;
		if(marks.get(player).equals(block)) return true;
		return false;
	}
	
	public static boolean marked(Player player) {
		if(marks.get(player) != null) return true;
		return false;
	}
	
}
