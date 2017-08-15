/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.gadgets.handlers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
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
import de.blablubbabc.paintball.gadgets.handlers.BallHandler.Ball;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class GrenadeM2Handler extends WeaponHandler implements Listener {

	private GadgetManager gadgetManager = new GadgetManager();
	private int next = 0;
	
	public GrenadeM2Handler(int customItemTypeID, boolean useDefaultType) {
		super("Grenade Mark 2", customItemTypeID, useDefaultType, new Origin() {
			
			@Override
			public String getKillMessage(FragInformations fragInfo) {
				return Translator.getString("WEAPON_FEED_GRENADEM2", getDefaultVariablesMap(fragInfo));
			}
		});

		Paintball.getInstance().getServer().getPluginManager().registerEvents(this, Paintball.getInstance());
	}
	
	public GrenadeM2 createGrenadeM2(Match match, Player player, Item nade, Origin origin) {
		return new GrenadeM2(gadgetManager, match, player, nade, origin);
	}
	
	private int getNext() {
		return ++next;
	}
	
	@Override
	protected int getDefaultItemTypeID() {
		return Material.SLIME_BALL.getId();
	}

	@Override
	protected ItemStack setItemMeta(ItemStack itemStack) {
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(Translator.getString("WEAPON_GRENADEM2"));
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onHopperPickupItem(InventoryPickupItemEvent event) {
		if (gadgetManager.isGadget(event.getItem())) {
			event.setCancelled(true);
		}
	}
	
	@Override
	protected void onInteract(PlayerInteractEvent event, Match match) {
		if (event.getAction() == Action.PHYSICAL || !Paintball.getInstance().grenade2) return;
		Player player = event.getPlayer();
		ItemStack itemInHand = player.getItemInHand();
		if (itemInHand == null) return;
		
		if (itemInHand.isSimilar(getItem())) {
			World world = player.getWorld();
			Vector direction = player.getLocation().getDirection().normalize();
			Location spawnLoc = Utils.getRightHeadLocation(direction, player.getEyeLocation());
			
			world.playSound(player.getLocation(), Sound.ENTITY_IRONGOLEM_ATTACK, 2.0F, 1F);
			player.sendMessage(Translator.getString("GRENADE_THROW"));
			
			ItemStack nadeItem = getItem().clone();
			ItemMeta meta = nadeItem.getItemMeta();
			meta.setDisplayName("GrenadeM2 " + getNext());
			nadeItem.setItemMeta(meta);
			Item nade = world.dropItem(spawnLoc, nadeItem);
			nade.setVelocity(direction.multiply(Paintball.getInstance().grenade2Speed));
			
			createGrenadeM2(match, player, nade, this.getWeaponOrigin());
			
			if (itemInHand.getAmount() <= 1) {
				player.setItemInHand(null);
			} else {
				itemInHand.setAmount(itemInHand.getAmount() - 1);
				player.setItemInHand(itemInHand);
			}
			Utils.updatePlayerInventoryLater(Paintball.getInstance(), player);
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

	
	public class GrenadeM2 extends Gadget {
		
		private final Item entity;
		private boolean exploded = false;

		private GrenadeM2(GadgetManager gadgetHandler, Match match, Player player, Item nade, Origin origin) {
			super(gadgetHandler, match, player.getName(), origin);
			this.entity = nade;
			
			Paintball.getInstance().getServer().getScheduler().runTaskLater(Paintball.getInstance(), new Runnable() {
				
				@Override
				public void run() {
					explode();
				}
			}, 20L * Paintball.getInstance().grenade2TimeUntilExplosion);
		}
		
		public void explode() {
			if (!exploded) {
				exploded = true;
				Location location = entity.getLocation();
				location.getWorld().createExplosion(location, -1F);
				Player player = Paintball.getInstance().getServer().getPlayerExact(playerName);
				if (player != null) {
					for (Vector v : Utils.getDirections()) {
						final Snowball snowball = location.getWorld().spawn(location, Snowball.class);
						snowball.setShooter(player);
						final Ball ball = Paintball.getInstance().weaponManager.getBallHandler().createBall(match, player, snowball, getGadgetOrigin());
						Vector v2 = v.clone();
						v2.setX(v.getX() + Math.random() - Math.random());
						v2.setY(v.getY() + Math.random() - Math.random());
						v2.setZ(v.getZ() + Math.random() - Math.random());
						snowball.setVelocity(v2.normalize().multiply(Paintball.getInstance().grenade2ShrapnelSpeed));
						Paintball.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(Paintball.getInstance(), new Runnable() {

							@Override
							public void run() {
								ball.dispose(true);
							}
						}, (long) Paintball.getInstance().grenade2Time);
					}
				}
			}
			
			// remove from tracking:
			dispose(true);
		}
		
		@Override
		public void dispose(boolean removeFromGadgetHandlerTracking) {
			entity.remove();
			super.dispose(removeFromGadgetHandlerTracking);
		}

		@Override
		public boolean isSimiliar(Entity entity) {
			return entity.getEntityId() == this.entity.getEntityId();
		}
		
		@Override
		public boolean isSimiliar(Location location) {
			return false;
		}
		
	}

}
