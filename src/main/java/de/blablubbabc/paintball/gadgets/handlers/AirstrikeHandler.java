/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.gadgets.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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

public class AirstrikeHandler extends WeaponHandler {

	private GadgetManager gadgetManager = new GadgetManager();
	
	private ConcurrentHashMap<String, Integer> taskIds = new ConcurrentHashMap<String, Integer>();
	private Map<String, Block> marks = new HashMap<String, Block>();
	private Map<String, List<FinalMark>> finalMarks = new HashMap<String, List<FinalMark>>();
	
	public AirstrikeHandler(int customItemTypeID, boolean useDefaultType) {
		super("Airstrike", customItemTypeID, useDefaultType, new Origin() {
			
			@Override
			public String getKillMessage(FragInformations fragInfo) {
				return Translator.getString("WEAPON_FEED_AIRSTRIKE", getDefaultVariablesMap(fragInfo));
			}
		});
	}

	public Airstrike callAirstrike(Match match, Player player, Location location, Origin origin) {
		return new Airstrike(gadgetManager, match, player, location, origin);
	}
	
	@Override
	protected int getDefaultItemTypeID() {
		return Material.STICK.getId();
	}

	@Override
	protected ItemStack setItemMeta(ItemStack itemStack) {
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(Translator.getString("WEAPON_AIRSTRIKE"));
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	
	@Override
	protected void onInteract(PlayerInteractEvent event, Match match) {
		if (event.getAction() == Action.PHYSICAL || !Paintball.instance.airstrike) return;
		
		final Player player = event.getPlayer();
		String playerName = player.getName();
		ItemStack itemInHand = player.getItemInHand();
		if (itemInHand == null) return;
		
		if (itemInHand.isSimilar(getItem())) {
			Block block = marks.get(playerName);
			if (block != null) {
				if (gadgetManager.getMatchGadgetCount(match) < Paintball.instance.airstrikeMatchLimit) {
					if (gadgetManager.getPlayerGadgetCount(match, playerName) < Paintball.instance.airstrikePlayerLimit) {
						
						demark(player);
						addFinalMark(block, player);
						
						callAirstrike(match, player, block.getLocation(), this.getWeaponOrigin());
						
						// INFORM MATCH
						match.onAirstrike(player);
						// remove stick if not infinite
						if (match.setting_airstrikes != -1) {
							if (itemInHand.getAmount() <= 1)
								player.setItemInHand(null);
							else {
								itemInHand.setAmount(itemInHand.getAmount() - 1);
							}
							Utils.updatePlayerInventoryLater(Paintball.instance, player);
						}
					} else {
						player.sendMessage(Translator.getString("AIRSTRIKE_PLAYER_LIMIT_REACHED"));
					}

				} else {
					player.sendMessage(Translator.getString("AIRSTRIKE_MATCH_LIMIT_REACHED"));
				}
			}
		}
	}
	
	@Override
	public void cleanUp(Match match, String playerName) {
		gadgetManager.cleanUp(match, playerName);
	}

	@Override
	public void cleanUp(Match match) {
		gadgetManager.cleanUp(match);
	}
	
	
	private void addFinalMark(Block block, Player player) {
		String playerName = player.getName();
		List<FinalMark> playerFinalMarks = finalMarks.get(playerName);
		if (playerFinalMarks == null) {
			playerFinalMarks = new ArrayList<FinalMark>();
			finalMarks.put(playerName, playerFinalMarks);
		}
		playerFinalMarks.add(new FinalMark(block, player));
	}
	
	private void mark(Block block, Player player) {
		String name = player.getName();
		marks.put(name, block);
		Block last = block;
		for (int i = 0; i < 10; i++) {
			last = last.getRelative(BlockFace.UP);
			player.sendBlockChange(last.getLocation(), Material.FENCE, (byte) 0);
		}
	}
	
	private void demark(Player player) {
		String name = player.getName();
		Block block = marks.get(name);
		if (block != null) {
			Block last = marks.get(name);
			for (int i = 0; i < 10; i++) {
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
	
	@Override
	protected void onItemHeld(final Player player, ItemStack newItem) {
		final String name = player.getName();
		
		if (getItem().isSimilar(newItem)) {
			if (!taskIds.containsKey(name)) {
				int taskId = Paintball.instance.getServer().getScheduler().runTaskTimer(Paintball.instance, new Runnable() {

					@Override
					public void run() {
						if (getItem().isSimilar(player.getItemInHand())) {
							Block block = player.getTargetBlock(Utils.getTransparentBlocks(), 1000);
							if (!isBlock(block, name)) {
								demark(player);
								mark(block, player);
							}
						} else {
							Paintball.instance.getServer().getScheduler().cancelTask(taskIds.get(name));
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
				Paintball.instance.getServer().getScheduler().cancelTask(id);
				taskIds.remove(name);
				demark(player);
			}
		}
	}
	
	public class Airstrike extends Gadget {
		
		private final Player player;
		private Entity chick = null;
		private int task = -1;

		private Airstrike(GadgetManager gadgetHandler, Match match, Player player, Location location, Origin origin) {
			super(gadgetHandler, match, player.getName(), origin);
			this.player = player;
			
			call(location);
		}
		
		private void call(Location location) {
			Location playerLoc = player.getLocation();
			//airstrike
			Vector pv = new Vector(playerLoc.getX(), location.getY() + Paintball.instance.airstrikeHeight, playerLoc.getZ());
			Vector bv =	new Vector(location.getX(), location.getY() + Paintball.instance.airstrikeHeight, location.getZ());
			Vector bp = new Vector() ; bp.copy(bv); bp.subtract(pv).normalize();
			Vector bpr = new Vector(-bp.getZ(), 0, bp.getX()).normalize(); 
			Location b1 = bv.clone().toLocation(player.getWorld());
			b1.subtract(bpr.clone().multiply(Paintball.instance.airstrikeRange));
			//Block b2 = player.getWorld().getBlockAt(block.getLocation().add(bp.multiply(range)));
			double bombDiff = ( (2*Paintball.instance.airstrikeRange) / Paintball.instance.airstrikeBombs );
			
			final LinkedList<Location> bombs = new LinkedList<Location>();
			for (int i = 1; i <= Paintball.instance.airstrikeBombs; i++) {
				bombs.add(b1.clone().add(bpr.clone().multiply((bombDiff * i))));
			}
			player.sendMessage(Translator.getString("AIRSTRIKE_CALLED"));
			//chicken
			Location lc = new Location(player.getWorld(), bombs.getFirst().getX(), bombs.getFirst().getY(), bombs.getFirst().getZ(), 0, Utils.getLookAtYaw(bpr));
			chick = player.getWorld().spawnEntity(lc.add(new Vector(0, 5, 0)), EntityType.CHICKEN);
			final Vector chickVel = bpr.clone().multiply(bombDiff / 5);
			
			final GrenadeHandler grenadeHandler = Paintball.instance.weaponManager.getGrenadeHandler();
			
			task = Paintball.instance.getServer().getScheduler().scheduleSyncRepeatingTask(Paintball.instance, new Runnable() {
				int i = 0;
				@Override
				public void run() {
					Location l = bombs.get(i);
					Egg egg = player.getWorld().spawn(l, Egg.class);
					egg.setShooter(player);
					grenadeHandler.createGrenade(match, player, egg, getGadgetOrigin());
					chick.setVelocity(chickVel);
					
					i++;
					if(i > (bombs.size() - 1)) {
						dispose(true);
					}
				}
			}, 0L, 5L);
		}
		
		@Override
		public void dispose(boolean removeFromGadgetHandlerTracking) {
			if (this.task != -1) Paintball.instance.getServer().getScheduler().cancelTask(task);
			if (this.chick != null) chick.remove();
			
			super.dispose(removeFromGadgetHandlerTracking);
		}

		@Override
		public boolean isSimiliar(Entity entity) {
			return chick.getEntityId() == entity.getEntityId();
		}

		@Override
		public boolean isSimiliar(Location location) {
			return false;
		}
		
	}
	
	private class FinalMark {
		private final Block block;
		
		private FinalMark(Block block, final Player player) {
			this.block = block;
			
			// mark:
			Block last = block;
			for (int i = 0; i < 10; i++) {
				last = last.getRelative(BlockFace.UP);
				player.sendBlockChange(last.getLocation(), Material.FENCE, (byte) 0);
			}
			last = last.getRelative(BlockFace.UP);
			player.sendBlockChange(last.getLocation(), Material.TORCH, (byte) 0);
			
			// demark after a certain time:
			Paintball.instance.getServer().getScheduler().runTaskLater(Paintball.instance, new Runnable() {
				
				@Override
				public void run() {
					demark(player);
				}
			}, Paintball.instance.airstrikeBombs * 5L);
			
		}
		
		private void demark(Player player) {
			Block last = block;
			for (int i = 0; i < 11; i++) {
				last = last.getRelative(BlockFace.UP);
				Location loc = last.getLocation();
				player.sendBlockChange(loc, last.getType(), last.getData());
			}
			
			// remove from map:
			String playerName = player.getName();
			List<FinalMark> playerFinalMarks = finalMarks.get(playerName);
			playerFinalMarks.remove(this);
			if (playerFinalMarks.size() == 0) finalMarks.remove(playerName);
		}
		
	}
	
}
