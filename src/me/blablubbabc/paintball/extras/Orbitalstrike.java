package me.blablubbabc.paintball.extras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import me.blablubbabc.paintball.Match;
import me.blablubbabc.paintball.Paintball;
import me.blablubbabc.paintball.Origin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Orbitalstrike {

	private static ConcurrentHashMap<String, Integer> taskIds;
	private static HashSet<Byte> transparent = null;
	final static Vector[] vectors = new Vector[36];

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
		
		for (int j = 0; j < 36; j += 1) {
			double x = Math.cos(j * 10.0D * 0.01856444444444445D) * 3.0;
			double z = Math.sin(j * 10.0D * 0.01856444444444445D) * 3.0;
			vectors[j] = new Vector(x, 0.0D, z);
		}

	}

	private static int orbitalstrikeCounter = 0;
	private static Map<String, ArrayList<Orbitalstrike>> orbitalstrikes = new HashMap<String, ArrayList<Orbitalstrike>>();

	private static void addOrbitalstrike(Orbitalstrike strike, String shooterName) {
		ArrayList<Orbitalstrike> pstrikes = orbitalstrikes.get(shooterName);
		if (pstrikes == null) {
			pstrikes = new ArrayList<Orbitalstrike>();
			orbitalstrikes.put(shooterName, pstrikes);
		}
		pstrikes.add(strike);
		orbitalstrikeCounter++;
	}

	private static void removeOrbitalstrike(Orbitalstrike strike, String shooterName) {
		ArrayList<Orbitalstrike> pstrikes = orbitalstrikes.get(shooterName);
		if (pstrikes != null) {
			if (pstrikes.remove(strike)) {
				if (pstrikes.size() == 0)
					orbitalstrikes.remove(shooterName);
				orbitalstrikeCounter--;
			}
		}
	}

	public static int getOrbitalstrikeCountMatch() {
		return orbitalstrikeCounter;
	}

	public static int getOrbitalstrikeCountPlayer(String shooterName) {
		ArrayList<Orbitalstrike> pstrikes = orbitalstrikes.get(shooterName);
		return pstrikes == null ? 0 : pstrikes.size();
	}

	public static void clear() {
		for (String playerName : orbitalstrikes.keySet()) {
			ArrayList<Orbitalstrike> pstrikes = new ArrayList<Orbitalstrike>(orbitalstrikes.get(playerName));
			for (Orbitalstrike strike : pstrikes) {
				strike.remove(false);
			}
		}
		orbitalstrikes.clear();
		orbitalstrikeCounter = 0;
	}

	/*
	 * private static List<Airstrike> orbitalstrikes = new
	 * ArrayList<Airstrike>();
	 * 
	 * public static synchronized void addAirstrike(Airstrike orbitalstrike) {
	 * orbitalstrikes.add(orbitalstrike); }
	 * 
	 * public static synchronized void removeAirstrike(Airstrike orbitalstrike)
	 * { orbitalstrikes.remove(orbitalstrike); }
	 * 
	 * public static synchronized List<Airstrike> getAirstrikes(Match match) {
	 * List<Airstrike> list = new ArrayList<Airstrike>(); for (Airstrike a :
	 * orbitalstrikes) { if (a.match.equals(match)) { list.add(a); } } return
	 * list; }
	 * 
	 * public static synchronized List<Airstrike> getAirstrikes(Player player) {
	 * List<Airstrike> list = new ArrayList<Airstrike>(); for (Airstrike a :
	 * orbitalstrikes) { if (a.player.equals(player)) { list.add(a); } } return
	 * list; }
	 */

	private final Player player;
	private final String playerName;
	private final Match match;
	private int task = -1;

	public Orbitalstrike(Player player, Match match) {
		this.player = player;
		this.playerName = player.getName();
		this.match = match;
		addOrbitalstrike(this, player.getName());
		call();
	}

	private void call() {
		Block block = marks.get(playerName);
		demark(player);
		finalMark(block, player);
		// orbitalstrike
		player.sendMessage(Paintball.instance.t.getString("ORBITALSTRIKE_CALLED"));
		// chicken
		final Location loc = block.getLocation();

		task = Paintball.instance.getServer().getScheduler().scheduleSyncRepeatingTask(Paintball.instance, new Runnable() {
			int i = 41;
			Location oldLoc;

			@Override
			public void run() {
				/*if (oldLoc != null)
					player.sendBlockChange(oldLoc, player.getWorld().getBlockAt(oldLoc).getType(), player.getWorld().getBlockAt(oldLoc).getData());*/
				i--;
				oldLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() + i, loc.getBlockZ());
				for (Player p : match.getAll()) {
					p.sendBlockChange(oldLoc, Material.REDSTONE_BLOCK, (byte) 0);
				}

				if (i <= 1) {
					
					remove(true);
					
					final Location loc1 = loc.clone().add(0, 1, 0);
					final Location loc2 = loc.clone().add(0, 2, 0);
					final Location loc3 = loc.clone().add(0, 3, 0);
					final Location loc4 = loc.clone().add(0, 4, 0);
					
					Paintball.instance.getServer().getScheduler().runTaskLater(Paintball.instance, new Runnable() {

						@Override
						public void run() {
							loc1.getWorld().createExplosion(loc1, -1);
							for (Vector v : vectors) {
								Snowball s = player.getWorld().spawn(loc1, Snowball.class);
								s.setShooter(player);
								Ball.registerBall(s, playerName, Origin.ORBITALSTRIKE);
								s.setVelocity(v.clone().setY(2));
							}
						}
					}, 1L);
					
					Paintball.instance.getServer().getScheduler().runTaskLater(Paintball.instance, new Runnable() {

						@Override
						public void run() {
							for (Vector v : vectors) {
								Snowball s = player.getWorld().spawn(loc1, Snowball.class);
								s.setShooter(player);
								Ball.registerBall(s, playerName, Origin.ORBITALSTRIKE);
								s.setVelocity(v);
							}
						}
					}, 2L);

					Paintball.instance.getServer().getScheduler().runTaskLater(Paintball.instance, new Runnable() {

						@Override
						public void run() {
							for (Vector v : vectors) {
								Snowball s = player.getWorld().spawn(loc2, Snowball.class);
								s.setShooter(player);
								Ball.registerBall(s, playerName, Origin.ORBITALSTRIKE);
								s.setVelocity(v);
							}
						}
					}, 5L);

					Paintball.instance.getServer().getScheduler().runTaskLater(Paintball.instance, new Runnable() {

						@Override
						public void run() {
							for (Vector v : vectors) {
								Snowball s = player.getWorld().spawn(loc3, Snowball.class);
								s.setShooter(player);
								Ball.registerBall(s, playerName, Origin.ORBITALSTRIKE);
								s.setVelocity(v);
							}
						}
					}, 10L);

					Paintball.instance.getServer().getScheduler().runTaskLater(Paintball.instance, new Runnable() {

						@Override
						public void run() {
							for (Vector v : vectors) {
								Snowball s = player.getWorld().spawn(loc4, Snowball.class);
								s.setShooter(player);
								Ball.registerBall(s, playerName, Origin.ORBITALSTRIKE);
								s.setVelocity(v);
							}
						}
					}, 15L);
					
				}
			}
		}, 0L, 2L);
	}
	
	private void remove(boolean removeFromList) {
		if (this.task != -1)
			Paintball.instance.getServer().getScheduler().cancelTask(task);
		definalMark(player, match);
		if (removeFromList) removeOrbitalstrike(this, playerName);
	}

	private static HashMap<String, Block> marks = new HashMap<String, Block>();
	private static HashMap<String, Block> finalmarks = new HashMap<String, Block>();

	private static void finalMark(Block block, Player player) {
		String name = player.getName();
		finalmarks.put(name, block);
		Block last = block;
		for (int i = 0; i < 10; i++) {
			last = last.getRelative(BlockFace.UP);
			player.sendBlockChange(last.getLocation(), Material.NETHER_FENCE, (byte) 0);
		}
		last = last.getRelative(BlockFace.UP);
		player.sendBlockChange(last.getLocation(), Material.REDSTONE_BLOCK, (byte) 0);
	}

	private static void definalMark(Player player, Match match) {
		String name = player.getName();
		if (finalmarks.get(name) != null) {
			Block last = finalmarks.get(name);
			for (int i = 0; i < 40; i++) {
				last = last.getRelative(BlockFace.UP);
				Location loc = last.getLocation();
				
				for (Player p : match.getAll()) {
					p.sendBlockChange(loc, last.getType(), last.getData());
				}
			}
			finalmarks.remove(name);
		}
	}

	public static void mark(Block block, Player player) {
		String name = player.getName();
		marks.put(name, block);
		Block last = block;
		for (int i = 0; i < 10; i++) {
			last = last.getRelative(BlockFace.UP);
			player.sendBlockChange(last.getLocation(), Material.NETHER_FENCE, (byte) 0);
		}
		last = last.getRelative(BlockFace.UP);
		player.sendBlockChange(last.getLocation(), Material.REDSTONE_BLOCK, (byte) 0);
	}

	public static void demark(Player player) {
		String name = player.getName();
		if (marked(name)) {
			Block last = marks.get(name);
			for (int i = 0; i < 11; i++) {
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
			if (item.getType() == Material.BLAZE_ROD) {
				if (!taskIds.containsKey(name)) {
					int taskId = Paintball.instance.getServer().getScheduler().scheduleSyncRepeatingTask(Paintball.instance, new Runnable() {

						@Override
						public void run() {
							if (player.getItemInHand().getType() == Material.BLAZE_ROD) {
								Block block = player.getTargetBlock(transparent, 1000);
								if (!Orbitalstrike.isBlock(block, name)) {
									Orbitalstrike.demark(player);
									Orbitalstrike.mark(block, player);
								}
							} else {
								Paintball.instance.getServer().getScheduler().cancelTask(taskIds.get(name));
								taskIds.remove(name);
								Orbitalstrike.demark(player);
							}
						}
					}, 0L, 1L);
					taskIds.put(name, taskId);
				}
			} else {
				if (taskIds.containsKey(name)) {
					Paintball.instance.getServer().getScheduler().cancelTask(taskIds.get(name));
					taskIds.remove(name);
					Orbitalstrike.demark(player);
				}
			}
		}
	}

}
