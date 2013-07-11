package de.blablubbabc.paintball.gadgets.handlers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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
		super(customItemTypeID, useDefaultType);
		Paintball.instance.getServer().getPluginManager().registerEvents(this, Paintball.instance);
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
		if (event.getAction() == Action.PHYSICAL || !Paintball.instance.grenade2) return;
		Player player = event.getPlayer();
		ItemStack itemInHand = player.getItemInHand();
		
		if (itemInHand.isSimilar(getItem())) {
			player.getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_THROW, 2.0F, 1F);
			player.sendMessage(Translator.getString("GRENADE_THROW"));
			ItemStack nadeItem = getItem().clone();
			ItemMeta meta = nadeItem.getItemMeta();
			meta.setDisplayName("GrenadeM2 " + getNext());
			nadeItem.setItemMeta(meta);
			Item nade = player.getWorld().dropItem(player.getEyeLocation(), nadeItem);
			nade.setVelocity(player.getLocation().getDirection().normalize().multiply(Paintball.instance.grenade2Speed));
			
			createGrenadeM2(match, player, nade, Origin.GRENADEM2);
			
			if (itemInHand.getAmount() <= 1)
				player.setItemInHand(null);
			else {
				itemInHand.setAmount(itemInHand.getAmount() - 1);
				player.setItemInHand(itemInHand);
			}
			Utils.updatePlayerInventoryLater(Paintball.instance, player);
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
			
			Paintball.instance.getServer().getScheduler().runTaskLater(Paintball.instance, new Runnable() {
				
				@Override
				public void run() {
					explode();
				}
			}, 20L * Paintball.instance.grenade2TimeUntilExplosion);
		}
		
		public void explode() {
			if (!exploded) {
				exploded = true;
				Location location = entity.getLocation();
				location.getWorld().createExplosion(location, -1F);
				Player player = Paintball.instance.getServer().getPlayerExact(playerName);
				if (player != null) {
					for (Vector v : Utils.getDirections()) {
						final Snowball snowball = location.getWorld().spawn(location, Snowball.class);
						snowball.setShooter(player);
						final Ball ball = Paintball.instance.weaponManager.getBallHandler().createBall(match, player, snowball, getOrigin());
						Vector v2 = v.clone();
						v2.setX(v.getX() + Math.random() - Math.random());
						v2.setY(v.getY() + Math.random() - Math.random());
						v2.setZ(v.getZ() + Math.random() - Math.random());
						snowball.setVelocity(v2.normalize().multiply(Paintball.instance.grenade2ShrapnelSpeed));
						Paintball.instance.getServer().getScheduler().scheduleSyncDelayedTask(Paintball.instance, new Runnable() {

							@Override
							public void run() {
								ball.dispose(true);
							}
						}, (long) Paintball.instance.grenade2Time);
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
