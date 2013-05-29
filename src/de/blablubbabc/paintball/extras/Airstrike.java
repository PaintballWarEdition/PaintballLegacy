package de.blablubbabc.paintball.extras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.Translator;

public class Airstrike {
	
	public final static ItemStack item = ItemManager.setMeta(new ItemStack(Material.STICK));
	
	private static ConcurrentHashMap<String, Integer> taskIds;
	private static HashSet<Byte> transparent = null;
	
	public static void init() {
		taskIds = new ConcurrentHashMap<String, Integer>();
		
		transparent = new HashSet<Byte>();
		transparent.add((byte) 0);
		transparent.add((byte) 8);
		transparent.add((byte) 10);
		transparent.add((byte) 51);
		transparent.add((byte) 90);
		transparent.add((byte) 119);
		transparent.add((byte) 321);
		transparent.add((byte) 85);
		
		
	}
	
	
	private static int airstrikeCounter = 0;
	private static Map<String, ArrayList<Airstrike>> airstrikes = new HashMap<String, ArrayList<Airstrike>>();
	
	private static void addAirstrike(Airstrike strike, String shooterName) {
		ArrayList<Airstrike> pstrikes = airstrikes.get(shooterName);
		if (pstrikes == null) {
			pstrikes = new ArrayList<Airstrike>();
			airstrikes.put(shooterName, pstrikes);
		}
		pstrikes.add(strike);
		airstrikeCounter++;
	}
	
	private static void removeAirstrike(Airstrike strike, String shooterName) {
		ArrayList<Airstrike> pstrikes = airstrikes.get(shooterName);
		if (pstrikes != null) {
			if (pstrikes.remove(strike)) {
				if (pstrikes.size() == 0) airstrikes.remove(shooterName);
				airstrikeCounter--;
			}
		}
	}
	
	public static int getAirstrikeCountMatch() {
		return airstrikeCounter;
	}
	
	public static int getAirstrikeCountPlayer(String shooterName) {
		ArrayList<Airstrike> pstrikes = airstrikes.get(shooterName);
		return pstrikes == null ? 0 : pstrikes.size();
	}
	
	public static void clear() {
		for (String playerName : airstrikes.keySet()) {
			ArrayList<Airstrike> pstrikes = new ArrayList<Airstrike>(airstrikes.get(playerName));
			for (Airstrike strike : pstrikes) {
				strike.remove(false);
			}
		}
		airstrikes.clear();
		airstrikeCounter = 0;
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
	
	private final Player player;
	private final String playerName;
	private Entity chick = null;
	private int task = -1;

	public Airstrike(Player player) {
		this.player = player;
		this.playerName = player.getName();
		addAirstrike(this, player.getName());
		call();
	}
	
	private void call() {
		Block block = marks.get(playerName);
		demark(player);
		finalMark(block, player);
		//airstrike
		Vector pv = new Vector(player.getLocation().getX(),block.getLocation().getY()+Paintball.instance.airstrikeHeight,player.getLocation().getZ());
		Vector bv =	new Vector(block.getLocation().getX(),block.getLocation().getY()+Paintball.instance.airstrikeHeight,block.getLocation().getZ());
		Vector bp = new Vector() ; bp.copy(bv); bp.subtract(pv).normalize();
		Vector bpr = new Vector(-bp.getZ(),0,bp.getX()); bpr.normalize();
		Location b1 = bv.clone().toLocation(player.getWorld());
		b1.subtract(bpr.clone().multiply(Paintball.instance.airstrikeRange));
		//Block b2 = player.getWorld().getBlockAt(block.getLocation().add(bp.multiply(range)));
		double bombDiff = ( (2*Paintball.instance.airstrikeRange) / Paintball.instance.airstrikeBombs );
		
		final LinkedList<Location> bombs = new LinkedList<Location>();
		for(int i = 1; i <= Paintball.instance.airstrikeBombs; i++) {
			bombs.add(b1.clone().add(bpr.clone().multiply((bombDiff*i))));
		}
		player.sendMessage(Translator.getString("AIRSTRIKE_CALLED"));
		//chicken
		Location lc = new Location(player.getWorld(), bombs.getFirst().getX(), bombs.getFirst().getY(), bombs.getFirst().getZ(), 0, getLookAtYaw(bpr));
		chick = player.getWorld().spawnEntity(lc.add(new Vector(0,5,0)), EntityType.CHICKEN);
		final Vector chickVel = bpr.clone().multiply(bombDiff/5);
		
		task = Paintball.instance.getServer().getScheduler().scheduleSyncRepeatingTask(Paintball.instance, new Runnable() {
			int i = 0;
			@Override
			public void run() {
				Location l = bombs.get(i);
				Egg egg = player.getWorld().spawn(l, Egg.class);
				egg.setShooter(player);
				Grenade.registerGrenade(egg, playerName, Origin.AIRSTRIKE);
				chick.setVelocity(chickVel);
				i++;
				if(i > (bombs.size() - 1)) {
					remove(true);
				}
			}
		}, 0L, 5L);
	}
	
	public void remove(boolean removeFromList) {
		if (this.task != -1) Paintball.instance.getServer().getScheduler().cancelTask(task);
		definalMark(player);
		if (this.chick != null) chick.remove();
		if (removeFromList) removeAirstrike(this, playerName);
	}
	
	private float getLookAtYaw(Vector motion) {
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
	
	private static HashMap<String, Block> marks = new HashMap<String, Block>();
	private static HashMap<String, Block> finalmarks = new HashMap<String, Block>();
	
	private static void finalMark(Block block, Player player) {
		String name = player.getName();
		finalmarks.put(name, block);
		Block last = block;
		for(int i = 0; i < 10; i++) {
			last = last.getRelative(BlockFace.UP);
			player.sendBlockChange(last.getLocation(), Material.FENCE, (byte) 0);
		}
		last = last.getRelative(BlockFace.UP);
		player.sendBlockChange(last.getLocation(), Material.TORCH, (byte) 0);
	}
	
	private static void definalMark(Player player) {
		String name = player.getName();
		if (finalmarks.get(name) != null) {
			Block last = finalmarks.get(name);
			for(int i = 0; i < 11; i++) {
				last = last.getRelative(BlockFace.UP);
				Location loc = last.getLocation();
				player.sendBlockChange(loc, player.getWorld().getBlockAt(loc).getType(), player.getWorld().getBlockAt(loc).getData());
			}
			finalmarks.remove(name);
		}
	}
	
	public static void mark(Block block, Player player) {
		String name = player.getName();
		marks.put(name, block);
		Block last = block;
		for(int i = 0; i < 10; i++) {
			last = last.getRelative(BlockFace.UP);
			player.sendBlockChange(last.getLocation(), Material.FENCE, (byte) 0);
		}
	}
	
	public static void demark(Player player) {
		String name = player.getName();
		if (marked(name)) {
			Block last = marks.get(name);
			for(int i = 0; i < 10; i++) {
				last = last.getRelative(BlockFace.UP);
				Location loc = last.getLocation();
				player.sendBlockChange(loc, player.getWorld().getBlockAt(loc).getType(), player.getWorld().getBlockAt(loc).getData());
			}
			marks.remove(name);
		}
	}
	
	public static boolean isBlock(Block block, String name) {
		Block b = marks.get(name);
		return b == null ? false : b.equals(block);
	}
	
	public static boolean marked(String name) {
		return marks.get(name) != null;
	}
	
	public static void handleItemInHand(final Player player, ItemStack item) {
		final String name = player.getName();
		if (item != null) {
			if (item.isSimilar(Airstrike.item)) {
				if (!taskIds.containsKey(name)) {
					int taskId = Paintball.instance.getServer().getScheduler().scheduleSyncRepeatingTask(Paintball.instance, new Runnable() {

						@Override
						public void run() {
							if (player.getItemInHand().isSimilar(Airstrike.item)) {
								Block block = player.getTargetBlock(transparent, 1000);
								if (!Airstrike.isBlock(block, name)) {
									Airstrike.demark(player);
									Airstrike.mark(block, player);
								}
							} else {
								Paintball.instance.getServer().getScheduler().cancelTask(taskIds.get(name));
								taskIds.remove(name);
								Airstrike.demark(player);
							}
						}
					}, 0L, 1L);
					taskIds.put(name, taskId);
				}
			} else {
				if (taskIds.containsKey(name)) {
					Paintball.instance.getServer().getScheduler().cancelTask(taskIds.get(name));
					taskIds.remove(name);
					Airstrike.demark(player);
				}
			}
		}
	}
	
}
