package de.blablubbabc.paintball.extras.weapons.impl;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.extras.weapons.Ball;
import de.blablubbabc.paintball.extras.weapons.Gadget;
import de.blablubbabc.paintball.extras.weapons.GadgetManager;
import de.blablubbabc.paintball.extras.weapons.WeaponHandler;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class GrenadeM2Handler extends WeaponHandler {

	private GadgetManager gadgetHandler = new GadgetManager();
	private int next = 0;
	
	public GrenadeM2Handler(int customItemTypeID, boolean useDefaultType) {
		super(customItemTypeID, useDefaultType);
	}
	
	public GrenadeM2 createGrenadeM2(Match match, Player player, Item nade, Origin origin) {
		return new GrenadeM2(gadgetHandler, match, player, nade, origin);
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
		meta.setDisplayName(Translator.getString("WEAPON_GRENADE2"));
		itemStack.setItemMeta(meta);
		return itemStack;
	}
	
	@Override
	protected void onInteract(PlayerInteractEvent event, Match match) {
		Player player = event.getPlayer();
		ItemStack itemInHand = player.getItemInHand();
		
		if (Paintball.instance.grenade2 && itemInHand.isSimilar(getItem())) {
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

	
	public class GrenadeM2 extends Gadget {
		
		private final Item entity;
		private final Origin origin;

		private GrenadeM2(GadgetManager gadgetHandler, Match match, Player player, Item nade, Origin origin) {
			super(gadgetHandler, match, player.getName());
			this.entity = nade;
			this.origin = origin;
			
			Paintball.instance.getServer().getScheduler().runTaskLater(Paintball.instance, new Runnable() {
				
				@Override
				public void run() {
					explode();
				}
			}, 20L * Paintball.instance.grenade2TimeUntilExplosion);
		}
		
		private void explode() {
			if (entity.isValid()) {
				Location location = entity.getLocation();
				location.getWorld().createExplosion(location, -1F);
				Player player = Paintball.instance.getServer().getPlayerExact(playerName);
				if (player != null) {
					for (Vector v : Utils.getDirections()) {
						final Snowball snowball = location.getWorld().spawn(location, Snowball.class);
						snowball.setShooter(player);
						final Ball ball = new Ball(match, player, snowball, origin);
						Paintball.instance.weaponManager.getBallManager().addGadget(match, playerName, ball);
						Vector v2 = v.clone();
						v2.setX(v.getX() + Math.random() - Math.random());
						v2.setY(v.getY() + Math.random() - Math.random());
						v2.setZ(v.getZ() + Math.random() - Math.random());
						snowball.setVelocity(v2.normalize().multiply(Paintball.instance.grenade2ShrapnelSpeed));
						Paintball.instance.getServer().getScheduler().scheduleSyncDelayedTask(Paintball.instance, new Runnable() {

							@Override
							public void run() {
								ball.dispose(true, true);
							}
						}, (long) Paintball.instance.grenade2Time);
					}
				}
			}
			dispose(true, false);
		}
		
		@Override
		public void dispose(boolean removeFromGadgetHandlerTracking, boolean cheapEffects) {
			entity.remove();
			super.dispose(removeFromGadgetHandlerTracking, cheapEffects);
		}

		@Override
		protected boolean isSimiliar(Entity entity) {
			return entity.getEntityId() == this.entity.getEntityId();
		}

		@Override
		public Origin getOrigin() {
			return origin;
		}
		
	}

}
