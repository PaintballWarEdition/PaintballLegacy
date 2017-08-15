/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.gadgets.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import de.blablubbabc.paintball.FragInformations;
import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.gadgets.Gadget;
import de.blablubbabc.paintball.gadgets.GadgetManager;
import de.blablubbabc.paintball.gadgets.WeaponHandler;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class OrbitalstrikeHandler extends WeaponHandler {

	private GadgetManager gadgetManager = new GadgetManager();
	
	private ConcurrentHashMap<String, Integer> taskIds = new ConcurrentHashMap<String, Integer>();
	private Map<String, Block> marks = new HashMap<String, Block>();
	private Map<String, List<FinalMark>> finalMarks = new HashMap<String, List<FinalMark>>();
	
	private final Vector[] directions = new Vector[36];
	
	public OrbitalstrikeHandler(int customItemTypeID, boolean useDefaultType) {
		super("Orbitalstrike", customItemTypeID, useDefaultType, new Origin() {
			
			@Override
			public String getKillMessage(FragInformations fragInfo) {
				return Translator.getString("WEAPON_FEED_ORBITALSTRIKE", getDefaultVariablesMap(fragInfo));
			}
		});
		
		for (int j = 0; j < 36; j += 1) {
			double x = Math.cos(j * 10.0D * 0.01856444444444445D) * 3.0;
			double z = Math.sin(j * 10.0D * 0.01856444444444445D) * 3.0;
			directions[j] = new Vector(x, 0.0D, z);
		}
	}
	
	public Orbitalstrike orderOrbitalstrike(Match match, Player player, Location location, Origin origin) {
		return orderOrbitalstrike(match, player, location, origin, null);
	}
	
	private Orbitalstrike orderOrbitalstrike(Match match, Player player, Location location, Origin origin, FinalMark finalMark) {
		return new Orbitalstrike(gadgetManager, player, match, location, origin, finalMark);
	}
	
	@Override
	protected int getDefaultItemTypeID() {
		return Material.BLAZE_ROD.getId();
	}

	@Override
	protected ItemStack setItemMeta(ItemStack itemStack) {
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(Translator.getString("WEAPON_ORBITALSTRIKE"));
		itemStack.setItemMeta(meta);
		return itemStack;
	}

	@Override
	public void cleanUp(Match match, String playerName) {
		gadgetManager.cleanUp(match, playerName);
	}

	@Override
	public void cleanUp(Match match) {
		gadgetManager.cleanUp(match);
	}
	
	@Override
	protected void onInteract(PlayerInteractEvent event, Match match) {
		if (event.getAction() == Action.PHYSICAL || !Paintball.getInstance().orbitalstrike) return;
		
		final Player player = event.getPlayer();
		String playerName = player.getName();
		ItemStack itemInHand = player.getItemInHand();
		if (itemInHand == null) return;
		
		if (itemInHand.isSimilar(getItem())) {
			Block block = marks.get(playerName);
			if (block != null) {
				if (gadgetManager.getMatchGadgetCount(match) < Paintball.getInstance().orbitalstrikeMatchLimit) {
					if (gadgetManager.getPlayerGadgetCount(match, playerName) < Paintball.getInstance().orbitalstrikePlayerLimit) {
						
						demark(player);
						FinalMark finalMark = addFinalMark(block, player, match);
						
						orderOrbitalstrike(match, player, block.getLocation(), this.getWeaponOrigin(), finalMark);
						
						// remove item
						if (itemInHand.getAmount() <= 1) {
							player.setItemInHand(null);
						} else {
							itemInHand.setAmount(itemInHand.getAmount() - 1);
						}
						Utils.updatePlayerInventoryLater(Paintball.getInstance(), player);
					} else {
						player.sendMessage(Translator.getString("ORBITALSTRIKE_PLAYER_LIMIT_REACHED"));
					}

				} else {
					player.sendMessage(Translator.getString("ORBITALSTRIKE_MATCH_LIMIT_REACHED"));
				}
			}
		}
	}
	
	@Override
	protected void onItemHeld(final Player player, ItemStack newItem) {
		if (newItem == null) return;
		
		final String name = player.getName();
		if (getItem().isSimilar(newItem)) {
			if (!taskIds.containsKey(name)) {
				int taskId = Paintball.getInstance().getServer().getScheduler().runTaskTimer(Paintball.getInstance(), new Runnable() {

					@Override
					public void run() {
						if (getItem().isSimilar(player.getItemInHand())) {
							Block block = player.getTargetBlock(Utils.getTransparentBlocks(), 1000);
							if (!isBlock(block, name)) {
								demark(player);
								mark(block, player);
							}
						} else {
							Paintball.getInstance().getServer().getScheduler().cancelTask(taskIds.get(name));
							taskIds.remove(name);
							demark(player);
						}
					}
				}, 0L, 1L).getTaskId();
				taskIds.put(name, taskId);
			}
		} else {
			Integer id = taskIds.get(name);
			if (id != null) {
				Paintball.getInstance().getServer().getScheduler().cancelTask(id);
				taskIds.remove(name);
				demark(player);
			}
		}
	}
	
	private FinalMark addFinalMark(Block block, Player player, Match match) {
		String playerName = player.getName();
		List<FinalMark> playerFinalMarks = finalMarks.get(playerName);
		if (playerFinalMarks == null) {
			playerFinalMarks = new ArrayList<FinalMark>();
			finalMarks.put(playerName, playerFinalMarks);
		}
		FinalMark finalMark = new FinalMark(block, player, match);
		playerFinalMarks.add(finalMark);
		
		return finalMark;
	}
	
	private void mark(Block block, Player player) {
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
	
	private void demark(Player player) {
		String name = player.getName();
		Block block = marks.get(name);
		if (block != null) {
			Block last = marks.get(name);
			for (int i = 0; i < 11; i++) {
				last = last.getRelative(BlockFace.UP);
				Location loc = last.getLocation();
				player.sendBlockChange(loc, player.getWorld().getBlockAt(loc).getType(), player.getWorld().getBlockAt(loc).getData());
			}
			marks.remove(name);
		}
	}
	
	private boolean isBlock(Block block, String name) {
		Block markedBlock = marks.get(name);
		return markedBlock == null ? false : markedBlock.equals(block);
	}
	
	
	public class Orbitalstrike extends Gadget {
		private final FinalMark finalMark;
		private final Player player;
		
		private int task = -1;

		private Orbitalstrike(GadgetManager gadgetManager, Player player, Match match, Location location, Origin origin, FinalMark finalMark) {
			super(gadgetManager, match, player.getName(), origin);
			this.player = player;
			this.finalMark = finalMark;
			
			order(location);
		}

		private void order(final Location location) {
			// orbitalstrike message
			player.sendMessage(Translator.getString("ORBITALSTRIKE_CALLED"));

			task = Paintball.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(Paintball.getInstance(), new Runnable() {
				int i = 41;
				Location oldLoc;

				@Override
				public void run() {
					/*if (oldLoc != null)
						player.sendBlockChange(oldLoc, player.getWorld().getBlockAt(oldLoc).getType(), player.getWorld().getBlockAt(oldLoc).getData());*/
					i--;
					oldLoc = new Location(location.getWorld(), location.getBlockX(), location.getBlockY() + i, location.getBlockZ());
					for (Player p : match.getAll()) {
						p.sendBlockChange(oldLoc, Material.REDSTONE_BLOCK, (byte) 0);
					}

					if (i <= 1) {
						
						dispose(true);
						
						final Location loc1 = location.clone().add(0, 1, 0);
						final Location loc2 = location.clone().add(0, 2, 0);
						final Location loc3 = location.clone().add(0, 3, 0);
						final Location loc4 = location.clone().add(0, 4, 0);
						
						Paintball.getInstance().getServer().getScheduler().runTaskLater(Paintball.getInstance(), new Runnable() {

							@Override
							public void run() {
								if (!match.isOver()) {
									loc1.getWorld().createExplosion(loc1, -1);
									shootCircle(loc1, 2);
								}
							}
						}, 1L);
						
						Paintball.getInstance().getServer().getScheduler().runTaskLater(Paintball.getInstance(), new Runnable() {

							@Override
							public void run() {
								if (!match.isOver()) {
									shootCircle(loc1);
								}
							}
						}, 2L);

						Paintball.getInstance().getServer().getScheduler().runTaskLater(Paintball.getInstance(), new Runnable() {

							@Override
							public void run() {
								if (!match.isOver()) {
									shootCircle(loc2);
								}
							}
						}, 5L);

						Paintball.getInstance().getServer().getScheduler().runTaskLater(Paintball.getInstance(), new Runnable() {

							@Override
							public void run() {
								if (!match.isOver()) {
									shootCircle(loc3);
								}
							}
						}, 10L);

						Paintball.getInstance().getServer().getScheduler().runTaskLater(Paintball.getInstance(), new Runnable() {

							@Override
							public void run() {
								if (!match.isOver()) {
									shootCircle(loc4);
								}
							}
						}, 15L);
						
					}
				}
			}, 0L, 2L);
		}
		
		private void shootCircle(Location loc) {
			shootCircle(loc, 0);
		}
		
		private void shootCircle(Location loc, double yValue) {
			for (Vector direction : directions) {
				Snowball snowball = player.getWorld().spawn(loc, Snowball.class);
				snowball.setShooter(player);
				if (yValue == 0) snowball.setVelocity(direction);
				else snowball.setVelocity(direction.clone().setY(yValue));
				
				Paintball.getInstance().weaponManager.getBallHandler().createBall(match, player, snowball, getGadgetOrigin());
			}
		}
		
		@Override
		public void dispose(boolean removeFromGadgetHandlerTracking) {
			if (this.task != -1) Paintball.getInstance().getServer().getScheduler().cancelTask(task);
			if (finalMark != null) finalMark.demark(playerName, match);
			
			super.dispose(removeFromGadgetHandlerTracking);
		}

		@Override
		public boolean isSimiliar(Entity entity) {
			return false;
		}
		
		@Override
		public boolean isSimiliar(Location location) {
			return false;
		}
		
	}

	private class FinalMark {
		private final Block block;
		
		private FinalMark(Block block, final Player player, final Match match) {
			this.block = block;
			
			// mark:
			Block last = block;
			for (int i = 0; i < 10; i++) {
				last = last.getRelative(BlockFace.UP);
				player.sendBlockChange(last.getLocation(), Material.NETHER_FENCE, (byte) 0);
			}
			last = last.getRelative(BlockFace.UP);
			player.sendBlockChange(last.getLocation(), Material.REDSTONE_BLOCK, (byte) 0);
		}
		
		private void demark(String playerName, Match match) {
			Block last = block;
			for (int i = 0; i < 40; i++) {
				last = last.getRelative(BlockFace.UP);
				Location loc = last.getLocation();
				
				for (Player p : match.getAll()) {
					p.sendBlockChange(loc, last.getType(), last.getData());
				}
			}
			
			// remove from map:
			List<FinalMark> playerFinalMarks = finalMarks.get(playerName);
			playerFinalMarks.remove(this);
			if (playerFinalMarks.size() == 0) finalMarks.remove(playerName);
		}
		
	}

}
