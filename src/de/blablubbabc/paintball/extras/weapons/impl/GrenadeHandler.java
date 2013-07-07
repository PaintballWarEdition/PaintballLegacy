package de.blablubbabc.paintball.extras.weapons.impl;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
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

public class GrenadeHandler extends WeaponHandler implements Listener {

	private GadgetManager gadgetHandler = new GadgetManager();
	
	public GrenadeHandler(int customItemTypeID, boolean useDefaultType) {
		super(customItemTypeID, useDefaultType);
		Paintball.instance.getServer().getPluginManager().registerEvents(this, Paintball.instance);
	}
	
	public Grenade createGrenade(Match match, Player player, Egg nade, Origin origin) {
		return new Grenade(gadgetHandler, match, player, nade, Origin.GRENADE);
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEggThrow(PlayerEggThrowEvent event) {
		Egg egg = event.getEgg();
		if (egg.getShooter() instanceof Player) {
			Player player = (Player) egg.getShooter();
			Match match = Paintball.instance.matchManager.getMatch(player);
			if (match != null && gadgetHandler.isGadget(egg, match, player.getName())) {
				event.setHatching(false);
			}
		}
	}
	
	@Override
	protected int getDefaultItemTypeID() {
		return Material.EGG.getId();
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
		
		if (Paintball.instance.grenade && itemInHand.isSimilar(getItem())) {
			PlayerInventory inv = player.getInventory();
			if (match.setting_grenades == -1 || inv.containsAtLeast(getItem(),  1)) {
				player.sendMessage(Translator.getString("GRENADE_THROW"));
				player.getWorld().playSound(player.getLocation(), Sound.SILVERFISH_IDLE, 2.0F, 1F);
				Egg egg = (Egg) player.getWorld().spawnEntity(player.getEyeLocation(), EntityType.EGG);
				egg.setShooter(player);
				// boosting:
				egg.setVelocity(player.getLocation().getDirection().multiply(Paintball.instance.grenadeSpeed));
				createGrenade(match, player, egg, Origin.GRENADE);
				// INFORM MATCH
				match.onGrenade(player);
				if (match.setting_grenades != -1) {
					// -1 egg
					Utils.removeInventoryItems(inv, getItem(), 1);
				}
			} else {
				player.playSound(player.getEyeLocation(), Sound.FIRE_IGNITE, 1F, 2F);
			}
		}
		Utils.updatePlayerInventoryLater(Paintball.instance, player);
	}
	
	@Override
	protected void onProjectileHit(ProjectileHitEvent event, Projectile projectile, Match match, Player shooter) {
		if (Paintball.instance.grenade && projectile.getType() == EntityType.EGG) {
			Gadget nadeGadget = gadgetHandler.getGadget(projectile, match, shooter.getName(), true);
			if (nadeGadget != null) {
				Grenade grenade = (Grenade) nadeGadget;
				grenade.explode(projectile.getLocation(), shooter);
			}
		}
	}

	
	public class Grenade extends Gadget {
		
		private final Egg entity;
		private final Origin origin;

		private Grenade(GadgetManager gadgetHandler, Match match, Player player, Egg nade, Origin origin) {
			super(gadgetHandler, match, player.getName());
			this.entity = nade;
			this.origin = origin;
		}
		
		private void explode(Location location, Player shooter) {
			location.getWorld().createExplosion(location, -1F);
			for (Vector v : Utils.getDirections()) {
				final Snowball snowball  = location.getWorld().spawn(location, Snowball.class);
				snowball.setShooter(shooter);
				final Ball ball = new Ball(match, shooter, snowball, origin);
				Vector v2 = v.clone();
				v2.setX(v.getX() + Math.random() - Math.random());
				v2.setY(v.getY() + Math.random() - Math.random());
				v2.setZ(v.getZ() + Math.random() - Math.random());
				snowball.setVelocity(v2.normalize().multiply(Paintball.instance.grenadeShrapnelSpeed));
				Paintball.instance.getServer().getScheduler().scheduleSyncDelayedTask(Paintball.instance, new Runnable() {

					@Override
					public void run() {
						ball.dispose(true, true);
					}
				}, (long) Paintball.instance.grenadeTime);
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
