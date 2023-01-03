/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.gadgets.handlers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
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

public class AirstrikeHandler extends WeaponHandler {

	private GadgetManager gadgetManager = new GadgetManager();

	private Map<UUID, Integer> taskIds = new HashMap<>();
	private Map<UUID, Block> marks = new HashMap<>();

	public AirstrikeHandler() {
		this(null);
	}

	public AirstrikeHandler(Material customItemType) {
		super("Airstrike", customItemType, new Origin() {
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
	protected Material getDefaultItemType() {
		return Material.STICK;
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
		if (event.getAction() == Action.PHYSICAL || !Paintball.getInstance().airstrike) return;

		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();
		PlayerInventory playerInventory = player.getInventory();
		ItemStack itemInHand = playerInventory.getItemInMainHand();
		if (itemInHand == null) return;

		if (itemInHand.isSimilar(getItem())) {
			Block markedBlock = marks.get(playerId);
			if (markedBlock != null) {
				if (gadgetManager.getMatchGadgetCount(match) < Paintball.getInstance().airstrikeMatchLimit) {
					if (gadgetManager.getPlayerGadgetCount(match, playerId) < Paintball.getInstance().airstrikePlayerLimit) {

						demark(player);

						callAirstrike(match, player, markedBlock.getLocation(), this.getWeaponOrigin());

						// INFORM MATCH
						match.onAirstrike(player);
						// remove stick if not infinite
						if (match.setting_airstrikes != -1) {
							if (itemInHand.getAmount() <= 1) {
								playerInventory.setItemInMainHand(null);
							} else {
								itemInHand.setAmount(itemInHand.getAmount() - 1);
								playerInventory.setItemInMainHand(itemInHand);
							}
							Utils.updatePlayerInventoryLater(Paintball.getInstance(), player);
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
	public void cleanUp(Match match, UUID playerId) {
		gadgetManager.cleanUp(match, playerId);
	}

	@Override
	public void cleanUp(Match match) {
		gadgetManager.cleanUp(match);
	}

	private void mark(Block markedBlock, Player player) {
		UUID playerId = player.getUniqueId();
		marks.put(playerId, markedBlock);
		BlockData blockData = Material.OAK_FENCE.createBlockData();
		Block lastBlock = markedBlock;
		for (int i = 0; i < 10; i++) {
			lastBlock = lastBlock.getRelative(BlockFace.UP);
			player.sendBlockChange(lastBlock.getLocation(), blockData);
		}
	}

	private void demark(Player player) {
		UUID playerId = player.getUniqueId();
		Block markedBlock = marks.get(playerId);
		if (markedBlock != null) {
			Block lastBlock = markedBlock;
			for (int i = 0; i < 10; i++) {
				lastBlock = lastBlock.getRelative(BlockFace.UP);
				player.sendBlockChange(lastBlock.getLocation(), lastBlock.getBlockData());
			}
			marks.remove(playerId);
		}
	}

	private boolean isBlock(Block block, UUID playerId) {
		Block markedBlock = marks.get(playerId);
		return (markedBlock != null && markedBlock.equals(block));
	}

	@Override
	protected void onItemHeld(final Player player, ItemStack newItem) {
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

	public class Airstrike extends Gadget {

		private Entity chick = null;
		private int task = -1;
		private FinalMark finalMark;

		private Airstrike(GadgetManager gadgetHandler, Match match, Player player, Location location, Origin origin) {
			super(gadgetHandler, match, player, origin);
			call(location);
		}

		private void call(Location location) {
			// Place marker:
			Block block = location.getBlock();
			finalMark = new FinalMark(block, player);

			// Spawn airstrike:
			Location playerLoc = player.getLocation();
			Vector pv = new Vector(playerLoc.getX(), location.getY() + Paintball.getInstance().airstrikeHeight, playerLoc.getZ());
			Vector bv = new Vector(location.getX(), location.getY() + Paintball.getInstance().airstrikeHeight, location.getZ());
			Vector bp = new Vector();
			bp.copy(bv);
			bp.subtract(pv).normalize();
			Vector bpr = new Vector(-bp.getZ(), 0, bp.getX()).normalize();
			Location b1 = bv.clone().toLocation(player.getWorld());
			b1.subtract(bpr.clone().multiply(Paintball.getInstance().airstrikeRange));
			// Block b2 = player.getWorld().getBlockAt(block.getLocation().add(bp.multiply(range)));
			double bombDiff = ((2 * Paintball.getInstance().airstrikeRange) / Paintball.getInstance().airstrikeBombs);

			final LinkedList<Location> bombs = new LinkedList<Location>();
			for (int i = 1; i <= Paintball.getInstance().airstrikeBombs; i++) {
				bombs.add(b1.clone().add(bpr.clone().multiply((bombDiff * i))));
			}
			player.sendMessage(Translator.getString("AIRSTRIKE_CALLED"));
			// chicken
			Location lc = new Location(player.getWorld(), bombs.getFirst().getX(), bombs.getFirst().getY(), bombs.getFirst().getZ(), 0, Utils.getLookAtYaw(bpr));
			chick = player.getWorld().spawnEntity(lc.add(new Vector(0, 5, 0)), EntityType.CHICKEN);
			final Vector chickVel = bpr.clone().multiply(bombDiff / 5);

			final GrenadeHandler grenadeHandler = Paintball.getInstance().weaponManager.getGrenadeHandler();

			task = Paintball.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(Paintball.getInstance(), new Runnable() {
				int i = 0;

				@Override
				public void run() {
					Location l = bombs.get(i);
					Egg egg = player.getWorld().spawn(l, Egg.class);
					egg.setShooter(player);
					grenadeHandler.createGrenade(match, player, egg, getGadgetOrigin());
					chick.setVelocity(chickVel);

					i++;
					if (i > (bombs.size() - 1)) {
						dispose(true);
					}
				}
			}, 0L, 5L);
		}

		@Override
		public void dispose(boolean removeFromGadgetHandlerTracking) {
			if (this.task != -1) Paintball.getInstance().getServer().getScheduler().cancelTask(task);
			if (this.chick != null) chick.remove();
			assert finalMark != null;
			finalMark.demark();

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
		private final Player player;

		private FinalMark(Block block, final Player player) {
			this.block = block;
			this.player = player;

			// mark:
			BlockData blockData = Material.OAK_FENCE.createBlockData();
			Block last = block;
			for (int i = 0; i < 10; i++) {
				last = last.getRelative(BlockFace.UP);
				player.sendBlockChange(last.getLocation(), blockData);
			}
			last = last.getRelative(BlockFace.UP);
			player.sendBlockChange(last.getLocation(), Material.TORCH.createBlockData());

			// demark after a certain time:
			Paintball.getInstance().getServer().getScheduler().runTaskLater(Paintball.getInstance(), new Runnable() {

				@Override
				public void run() {
					demark(player);
				}
			}, Paintball.getInstance().airstrikeBombs * 5L);

		}

		private void demark() {
			this.demark(player);
		}

		private void demark(Player player) {
			Block last = block;
			for (int i = 0; i < 11; i++) {
				last = last.getRelative(BlockFace.UP);
				Location loc = last.getLocation();
				player.sendBlockChange(loc, last.getBlockData());
			}
		}
	}
}
