package de.blablubbabc.paintball.extras.weapons.impl;

import java.util.HashMap;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.extras.Grenade;
import de.blablubbabc.paintball.extras.weapons.Gadget;
import de.blablubbabc.paintball.extras.weapons.GadgetManager;
import de.blablubbabc.paintball.extras.weapons.WeaponHandler;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class AirstrikeHandler extends WeaponHandler {

	private ConcurrentHashMap<String, Integer> taskIds = new ConcurrentHashMap<String, Integer>();;
	private Map<String, Block> marks = new HashMap<String, Block>();
	private Map<String, Block> finalmarks = new HashMap<String, Block>();
	private GadgetManager gadgetHandler = new GadgetManager();
	
	public AirstrikeHandler(Paintball plugin, int customItemTypeID, boolean useDefaultType) {
		super(plugin, customItemTypeID, useDefaultType);
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
		Player player = event.getPlayer();
		String playerName = player.getName();
		ItemStack itemInHand = player.getItemInHand();
		
		if (plugin.airstrike && itemInHand.isSimilar(this.item)) {
			if (marked(player.getName())) {
				if (gadgetHandler.getMatchGadgetCount(match) < plugin.airstrikeMatchLimit) {
					if (gadgetHandler.getPlayerGadgetCount(match, playerName) < plugin.airstrikePlayerLimit) {
						new AirstrikeCall(gadgetHandler, match, player);
						// INFORM MATCH
						match.onAirstrike(player);
						// remove stick if not infinite
						if (match.setting_airstrikes != -1) {
							if (itemInHand.getAmount() <= 1)
								player.setItemInHand(null);
							else {
								itemInHand.setAmount(itemInHand.getAmount() - 1);
							}
							Utils.updatePlayerInventoryLater(plugin, player);
						}
					} else {
						player.sendMessage(Translator.getString("AIRSTRIKE_PLAYER_LIMIT_REACHED"));
					}

				} else {
					player.sendMessage(Translator.getString("AIRSTRIK_MATCH_LIMIT_REACHED"));
				}
			}
		}
	}
	
	@Override
	protected void cleanUp(Match match, String playerName) {
		gadgetHandler.cleanUp(match, playerName);
	}

	@Override
	protected void cleanUp(Match match) {
		gadgetHandler.cleanUp(match);
	}
	
	
	private void finalMark(Block block, Player player) {
		String name = player.getName();
		finalmarks.put(name, block);
		Block last = block;
		for (int i = 0; i < 10; i++) {
			last = last.getRelative(BlockFace.UP);
			player.sendBlockChange(last.getLocation(), Material.FENCE, (byte) 0);
		}
		last = last.getRelative(BlockFace.UP);
		player.sendBlockChange(last.getLocation(), Material.TORCH, (byte) 0);
	}
	
	private void definalMark(Player player) {
		String name = player.getName();
		if (finalmarks.get(name) != null) {
			Block last = finalmarks.get(name);
			for (int i = 0; i < 11; i++) {
				last = last.getRelative(BlockFace.UP);
				Location loc = last.getLocation();
				player.sendBlockChange(loc, player.getWorld().getBlockAt(loc).getType(), player.getWorld().getBlockAt(loc).getData());
			}
			finalmarks.remove(name);
		}
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
		if (marked(name)) {
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
		Block b = marks.get(name);
		return b == null ? false : b.equals(block);
	}
	
	private boolean marked(String name) {
		return marks.get(name) != null;
	}
	
	@Override
	protected void onItemHeld(final Player player) {
		final String name = player.getName();
		if (player.getItemInHand().isSimilar(getItem())) {
			if (!taskIds.containsKey(name)) {
				int taskId = Paintball.instance.getServer().getScheduler().scheduleSyncRepeatingTask(Paintball.instance, new Runnable() {

					@Override
					public void run() {
						if (player.getItemInHand().isSimilar(getItem())) {
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
				}, 0L, 1L);
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
	
	
	private class AirstrikeCall extends Gadget {
		
		private final Player player;
		private Entity chick = null;
		private int task = -1;

		private AirstrikeCall(GadgetManager gadgetHandler, Match match, Player player) {
			super(gadgetHandler, match, player.getName());
			this.player = player;
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
			Location lc = new Location(player.getWorld(), bombs.getFirst().getX(), bombs.getFirst().getY(), bombs.getFirst().getZ(), 0, Utils.getLookAtYaw(bpr));
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
						dispose(true, false);
					}
				}
			}, 0L, 5L);
		}
		
		@Override
		public void dispose(boolean removeFromGadgetHandlerTracking, boolean cheapEffects) {
			if (this.task != -1) Paintball.instance.getServer().getScheduler().cancelTask(task);
			definalMark(player);
			if (this.chick != null) chick.remove();
			super.dispose(removeFromGadgetHandlerTracking, cheapEffects);
		}

		@Override
		protected boolean isSimiliar(Entity entity) {
			return false;
		}

		@Override
		public Origin getOrigin() {
			return Origin.AIRSTRIKE;
		}
		
	}
	
}
