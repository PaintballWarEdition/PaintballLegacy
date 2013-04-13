package me.blablubbabc.paintball.extras;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import me.blablubbabc.paintball.Match;
import me.blablubbabc.paintball.Paintball;
import me.blablubbabc.paintball.Source;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Airstrike {
	
	private static int airstrikeCounter = 0;
	private static Map<String, Integer> airstrikes = new HashMap<String, Integer>();
	
	private static void addAirstrike(String shooterName) {
		Integer pcount = airstrikes.get(shooterName);
		if (pcount == null) pcount = 0;
		airstrikes.put(shooterName, pcount + 1);
	}
	
	private static void removeAirstrike(String shooterName) {
		Integer pstrikes = airstrikes.get(shooterName);
		if (pstrikes != null) {
			if (pstrikes == 1) airstrikes.remove(shooterName);
			else airstrikes.put(shooterName, pstrikes - 1);
			airstrikeCounter--;
		}
	}
	
	public static int getAirstrikeCountMatch() {
		return airstrikeCounter;
	}
	
	public static int getAirstrikeCountPlayer(String shooterName) {
		Integer pstrikes = airstrikes.get(shooterName);
		return pstrikes == null ? 0 : pstrikes;
	}
	
	/*private static List<Airstrike> airstrikes = new ArrayList<Airstrike>();

	public static synchronized void addAirstrike(Airstrike airstrike) {
		airstrikes.add(airstrike);
	}

	public static synchronized void removeAirstrike(Airstrike airstrike) {
		airstrikes.remove(airstrike);
	}

	public static synchronized List<Airstrike> getAirstrikes(Match match) {
		List<Airstrike> list = new ArrayList<Airstrike>();
		for (Airstrike a : airstrikes) {
			if (a.match.equals(match)) {
				list.add(a);
			}
		}
		return list;
	}

	public static synchronized List<Airstrike> getAirstrikes(Player player) {
		List<Airstrike> list = new ArrayList<Airstrike>();
		for (Airstrike a : airstrikes) {
			if (a.player.equals(player)) {
				list.add(a);
			}
		}
		return list;
	}*/
	
	public final Player player;
	public final Match match;
	public int task;

	private Airstrike(Player player, Match match) {
		this.match = match;
		this.player = player;
		addAirstrike(player.getName());
	}
	
	private static HashMap<String, Block> marks = new HashMap<String, Block>();
	private static HashMap<String, Block> finalmarks = new HashMap<String, Block>();
	
	public static void call(final Player player, final Match match) {
		final String name = player.getName();
		if(marked(name)) {
			final Airstrike a = new Airstrike(player, match);
			
			Block block = marks.get(name);
			demark(player);
			finalMark(block, player);
			//airstrike
			Vector pv = new Vector(player.getLocation().getX(),block.getLocation().getY()+Paintball.instance.airstrikeHeight,player.getLocation().getZ());
			Vector bv =	new Vector(block.getLocation().getX(),block.getLocation().getY()+Paintball.instance.airstrikeHeight,block.getLocation().getZ());
			Vector bp = new Vector() ; bp.copy(bv); bp.subtract(pv).normalize();
			final Vector bpr = new Vector(-bp.getZ(),0,bp.getX()); bpr.normalize();
			Location b1 = bv.clone().toLocation(player.getWorld());
			b1.subtract(bpr.clone().multiply(Paintball.instance.airstrikeRange));
			//Block b2 = player.getWorld().getBlockAt(block.getLocation().add(bp.multiply(range)));
			final double bombDiff = ( (2*Paintball.instance.airstrikeRange) / Paintball.instance.airstrikeBombs );
			
			final LinkedList<Location> bombs = new LinkedList<Location>();
			for(int i = 1; i <= Paintball.instance.airstrikeBombs; i++) {
				bombs.add(b1.clone().add(bpr.clone().multiply((bombDiff*i))));
			}
			player.sendMessage(Paintball.instance.t.getString("AIRSTRKE_CALLED"));
			//chicken
			Location lc = new Location(player.getWorld(), bombs.getFirst().getX(), bombs.getFirst().getY(), bombs.getFirst().getZ(), 0, getLookAtYaw(bpr));
			final Entity chick = player.getWorld().spawnEntity(lc.add(new Vector(0,5,0)), EntityType.CHICKEN);
			final String shooterName = player.getName();
			a.task = Paintball.instance.getServer().getScheduler().scheduleSyncRepeatingTask(Paintball.instance, new Runnable() {
				int i = 0;
				@Override
				public void run() {
					Location l = bombs.get(i);
					Egg egg = player.getWorld().spawn(l, Egg.class);
					egg.setShooter(player);
					Grenade.registerGrenade(egg, shooterName, Source.AIRSTRIKE);
					chick.setVelocity(bpr.clone().multiply(bombDiff/5));
					i++;
					if(i > (bombs.size() - 1)) {
						Paintball.instance.getServer().getScheduler().cancelTask(a.task);
						definalMark(player);
						chick.remove();
						removeAirstrike(shooterName);
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
		String name = player.getName();
		finalmarks.put(name, block);
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
		String name = player.getName();
		if (finalmarks.get(name) != null) {
			Block block = finalmarks.get(name);
			LinkedList<Block> blocks = new LinkedList<Block>();
			blocks.add(block.getRelative(BlockFace.UP));
			for(int i = 0; i < 11; i++) {
				blocks.add(blocks.getLast().getRelative(BlockFace.UP));
			}
			for(Block b : blocks) {
				Location loc = b.getLocation();
				player.sendBlockChange(loc, player.getWorld().getBlockAt(loc).getType(), player.getWorld().getBlockAt(loc).getData());
			}
			finalmarks.remove(name);
		}
	}
	
	public static void mark(Block block, Player player) {
		String name = player.getName();
		marks.put(name, block);
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
		String name = player.getName();
		if (marked(name)) {
			Block block = marks.get(name);
			LinkedList<Block> blocks = new LinkedList<Block>();
			blocks.add(block.getRelative(BlockFace.UP));
			for(int i = 0; i < 10; i++) {
				blocks.add(blocks.getLast().getRelative(BlockFace.UP));
			}
			for (Block b : blocks) {
				Location loc = b.getLocation();
				player.sendBlockChange(loc, player.getWorld().getBlockAt(loc).getType(), player.getWorld().getBlockAt(loc).getData());
			}
			marks.remove(name);
		}
	}
	
	public static boolean isBlock(Block block, String name) {
		if(!marked(name)) return false;
		if(marks.get(name).equals(block)) return true;
		return false;
	}
	
	public static boolean marked(String name) {
		if(marks.get(name) != null) return true;
		return false;
	}
	
	/*public static boolean isBomb(Egg egg, Player player) {
		List<Airstrike> airstrikes = getAirstrikes(player);
		for (Airstrike a : airstrikes) {
			for (Egg e : a.bombs) {
				if (egg.equals(e)) return true;
			}
		}
		return false;
		
	}*/
	
}
