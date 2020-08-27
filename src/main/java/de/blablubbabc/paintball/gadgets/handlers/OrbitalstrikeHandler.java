/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.gadgets.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
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

	private Map<UUID, Integer> taskIds = new HashMap<>();
	private Map<UUID, Block> marks = new HashMap<>();

	private final Vector[] directions = new Vector[36];

	public OrbitalstrikeHandler() {
		this(null);
	}

	public OrbitalstrikeHandler(Material customItemType) {
		super("Orbitalstrike", customItemType, new Origin() {

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
		return new Orbitalstrike(gadgetManager, player, match, location, origin);
	}

	@Override
	protected Material getDefaultItemType() {
		return Material.BLAZE_ROD;
	}

	@Override
	protected ItemStack setItemMeta(ItemStack itemStack) {
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(Translator.getString("WEAPON_ORBITALSTRIKE"));
		itemStack.setItemMeta(meta);
		return itemStack;
	}

	@Override
	public void cleanUp(Match match, UUID playerId) {
		gadgetManager.cleanUp(match, playerId);
	}

	@Override
	public void cleanUp(Match match) {
		gadgetManager.cleanUp(match);
	}

	@Override
	protected void onInteract(PlayerInteractEvent event, Match match) {
		if (event.getAction() == Action.PHYSICAL || !Paintball.getInstance().orbitalstrike) return;

		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();
		PlayerInventory playerInventory = player.getInventory();
		ItemStack itemInHand = playerInventory.getItemInMainHand();
		if (itemInHand == null) return;

		if (itemInHand.isSimilar(getItem())) {
			Block block = marks.get(playerId);
			if (block != null) {
				if (gadgetManager.getMatchGadgetCount(match) < Paintball.getInstance().orbitalstrikeMatchLimit) {
					if (gadgetManager.getPlayerGadgetCount(match, playerId) < Paintball.getInstance().orbitalstrikePlayerLimit) {

						demark(player);

						orderOrbitalstrike(match, player, block.getLocation(), this.getWeaponOrigin());

						// remove item
						if (itemInHand.getAmount() <= 1) {
							playerInventory.setItemInMainHand(null);
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

		final UUID playerId = player.getUniqueId();
		if (getItem().isSimilar(newItem)) {
			if (!taskIds.containsKey(playerId)) {
				int taskId = Paintball.getInstance().getServer().getScheduler().runTaskTimer(Paintball.getInstance(), new Runnable() {

					@Override
					public void run() {
						PlayerInventory playerInventory = player.getInventory();
						ItemStack itemInHand = playerInventory.getItemInMainHand();
						if (getItem().isSimilar(itemInHand)) {
							Block block = player.getTargetBlock(Utils.getTransparentBlocks(), 120);
							if (!isBlock(block, playerId)) {
								demark(player);
								Material blockType = block.getType();
								if (blockType != Material.AIR && blockType != Material.VOID_AIR && blockType != Material.CAVE_AIR) {
									mark(block, player);
								}
							}
						} else {
							Paintball.getInstance().getServer().getScheduler().cancelTask(taskIds.get(playerId));
							taskIds.remove(playerId);
							demark(player);
						}
					}
				}, 0L, 1L).getTaskId();
				taskIds.put(playerId, taskId);
			}
		} else {
			Integer id = taskIds.get(playerId);
			if (id != null) {
				Paintball.getInstance().getServer().getScheduler().cancelTask(id);
				taskIds.remove(playerId);
				demark(player);
			}
		}
	}

	private void mark(Block markedBlock, Player player) {
		UUID playerId = player.getUniqueId();
		marks.put(playerId, markedBlock);
		Block last = markedBlock;
		for (int i = 0; i < 10; i++) {
			last = last.getRelative(BlockFace.UP);
			player.sendBlockChange(last.getLocation(), Material.NETHER_BRICK_FENCE.createBlockData());
		}
		last = last.getRelative(BlockFace.UP);
		player.sendBlockChange(last.getLocation(), Material.REDSTONE_BLOCK.createBlockData());
	}

	private void demark(Player player) {
		UUID playerId = player.getUniqueId();
		Block markedBlock = marks.get(playerId);
		if (markedBlock != null) {
			Block last = marks.get(playerId);
			for (int i = 0; i < 11; i++) {
				last = last.getRelative(BlockFace.UP);
				Location loc = last.getLocation();
				player.sendBlockChange(loc, last.getBlockData());
			}
			marks.remove(playerId);
		}
	}

	private boolean isBlock(Block block, UUID playerId) {
		Block markedBlock = marks.get(playerId);
		return (markedBlock != null && markedBlock.equals(block));
	}

	public class Orbitalstrike extends Gadget {
		private FinalMark finalMark;
		private int task = -1;

		private Orbitalstrike(GadgetManager gadgetManager, Player player, Match match, Location location, Origin origin) {
			super(gadgetManager, match, player, origin);
			order(location);
		}

		private void order(final Location location) {
			// Place final marker:
			this.finalMark = new FinalMark(location.getBlock(), player, match);

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
					BlockData blockData = Material.REDSTONE_BLOCK.createBlockData();
					for (Player p : match.getAll()) {
						p.sendBlockChange(oldLoc, blockData);
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
			assert finalMark != null;
			finalMark.demark(player.getUniqueId(), match);

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
			BlockData blockData = Material.NETHER_BRICK_FENCE.createBlockData();
			Block last = block;
			for (int i = 0; i < 10; i++) {
				last = last.getRelative(BlockFace.UP);
				player.sendBlockChange(last.getLocation(), blockData);
			}
			last = last.getRelative(BlockFace.UP);
			player.sendBlockChange(last.getLocation(), Material.REDSTONE_BLOCK.createBlockData());
		}

		private void demark(UUID playerId, Match match) {
			Block last = block;
			for (int i = 0; i < 40; i++) {
				last = last.getRelative(BlockFace.UP);
				Location loc = last.getLocation();

				for (Player p : match.getAll()) {
					p.sendBlockChange(loc, last.getBlockData());
				}
			}
		}
	}
}
