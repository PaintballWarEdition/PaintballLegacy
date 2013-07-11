package de.blablubbabc.paintball.gadgets.handlers;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.gadgets.Gadget;
import de.blablubbabc.paintball.gadgets.GadgetManager;
import de.blablubbabc.paintball.gadgets.WeaponHandler;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class FlashbangHandler extends WeaponHandler implements Listener {

	private GadgetManager gadgetManager = new GadgetManager();
	private int next = 0;
	
	public FlashbangHandler(int customItemTypeID, boolean useDefaultType) {
		super(customItemTypeID, useDefaultType);
		Paintball.instance.getServer().getPluginManager().registerEvents(this, Paintball.instance);
	}
	
	public Flashbang createFlashbang(Match match, Player player, Item nade, Origin origin) {
		return new Flashbang(gadgetManager, match, player, nade, origin);
	}
	
	private int getNext() {
		return ++next;
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onHopperPickupItem(InventoryPickupItemEvent event) {
		if (gadgetManager.isGadget(event.getItem())) {
			event.setCancelled(true);
		}
	}
	
	@Override
	protected int getDefaultItemTypeID() {
		return Material.GHAST_TEAR.getId();
	}

	@Override
	protected ItemStack setItemMeta(ItemStack itemStack) {
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(Translator.getString("WEAPON_FLASHBANG"));
		itemStack.setItemMeta(meta);
		return itemStack;
	}

	@Override
	protected void onInteract(PlayerInteractEvent event, Match match) {
		if (event.getAction() == Action.PHYSICAL || !Paintball.instance.flashbang) return;
		Player player = event.getPlayer();
		ItemStack itemInHand = player.getItemInHand();
		
		if (itemInHand.isSimilar(getItem())) {
			player.getWorld().playSound(player.getLocation(), Sound.IRONGOLEM_THROW, 2.0F, 1F);
			ItemStack nadeItem = getItem().clone();
			ItemMeta meta = nadeItem.getItemMeta();
			meta.setDisplayName("Flashbang " + getNext());
			nadeItem.setItemMeta(meta);
			Item nade = player.getWorld().dropItem(player.getEyeLocation(), nadeItem);
			nade.setVelocity(player.getLocation().getDirection().normalize().multiply(Paintball.instance.flashbangSpeed));
			
			createFlashbang(match, player, nade, Origin.FLASHBANG);
			
			if (itemInHand.getAmount() <= 1) {
				player.setItemInHand(null);
			} else {
				itemInHand.setAmount(itemInHand.getAmount() - 1);
				player.setItemInHand(itemInHand);
			}
			Utils.updatePlayerInventoryLater(Paintball.instance, player);
		}
	}
	
	@Override
	protected void onItemPickup(PlayerPickupItemEvent event) {
		if (gadgetManager.isGadget(event.getItem())) {
			event.setCancelled(true);
		}
	}

	@Override
	public void cleanUp(Match match, String playerName) {
		gadgetManager.cleanUp(match, playerName);
	}

	@Override
	public void cleanUp(Match match) {
		gadgetManager.cleanUp(match);
		next = 0;
	}

	public class Flashbang extends Gadget {
		
		private final Item entity;

		private Flashbang(GadgetManager gadgetHandler, Match match, Player player, Item nade, Origin origin) {
			super(gadgetHandler, match, player.getName(), origin);
			this.entity = nade;
			
			Paintball.instance.getServer().getScheduler().runTaskLater(Paintball.instance, new Runnable() {
				
				@Override
				public void run() {
					explode();
				}
			}, 20L * Paintball.instance.flashbangTimeUntilExplosion);
		}
		
		public void explode() {
			if (entity.isValid()) {
				Location location = entity.getLocation();
				// EFFECTS
				// small explosion
				location.getWorld().createExplosion(location, -1F);
				/*
				// TODO
				// firework particles:
				// effects:
				FireworkEffect effectWhite = FireworkEffect.builder().withColor(Color.WHITE).withFlicker().with(Type.BURST).build();
				FireworkEffect effectSilver = FireworkEffect.builder().withColor(Color.SILVER).withFlicker().with(Type.BALL).build();
				
				for (int i = 0; i < 5; i++) {
					int x = Utils.random.nextInt(5) - 2;
					int y = Utils.random.nextInt(5) - 2;
					int z = Utils.random.nextInt(5) - 2;
					
					Block block = location.add(1, 1, 1).getBlock();
					if (block.getType() == Material.AIR) {
						Firework firework = location.getWorld().spawn(location, Firework.class);
						FireworkMeta meta = (FireworkMeta) firework.getFireworkMeta();
						
						meta.addEffects(effectWhite, effectSilver);
						meta.setPower(1);
						firework.setFireworkMeta(meta);	
					}
				}*/
				
				
				// blindness to near enemies:
				Player player = Paintball.instance.getServer().getPlayerExact(playerName);
				if (player != null) {
					Match match = Paintball.instance.matchManager.getMatch(player);
					if (match != null) {
						List<Entity> near = entity.getNearbyEntities(Paintball.instance.flashRange, Paintball.instance.flashRange, Paintball.instance.flashRange);
						for (Entity e : near) {
							if (e.getType() == EntityType.PLAYER) {
								Player p = (Player) e;
								Match m = Paintball.instance.matchManager.getMatch(p);
								if (match == m && match.enemys(player, p)) {
									p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * Paintball.instance.flashSlownessDuration, 3), true);
									p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * Paintball.instance.flashConfusionDuration, 3), true);
									p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * Paintball.instance.flashBlindnessDuration, 3), true);
								}
								
							}
						}
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
