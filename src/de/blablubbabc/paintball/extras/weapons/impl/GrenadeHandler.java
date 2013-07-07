package de.blablubbabc.paintball.extras.weapons.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.extras.Grenade;
import de.blablubbabc.paintball.extras.weapons.Ball;
import de.blablubbabc.paintball.extras.weapons.Gadget;
import de.blablubbabc.paintball.extras.weapons.GadgetManager;
import de.blablubbabc.paintball.extras.weapons.WeaponHandler;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class GrenadeHandler extends WeaponHandler implements Listener {

	private GadgetManager gadgetHander = new GadgetManager();
	
	public GrenadeHandler(int customItemTypeID, boolean useDefaultType) {
		super(customItemTypeID, useDefaultType);
		Paintball.instance.getServer().getPluginManager().registerEvents(this, Paintball.instance);
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onEggThrow(PlayerEggThrowEvent event) {
		Egg egg = event.getEgg();
		if (egg.getShooter() instanceof Player) {
			Player player = (Player) egg.getShooter();
			if (Grenade.getGrenade(egg.getEntityId(), player.getName(), false) != null) {
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

	
	private class Grenade extends Gadget {
		
		private final Egg entity;
		private final Origin origin;

		private Grenade(GadgetManager gadgetHandler, Match match, Player player, Egg nade, Origin origin) {
			super(gadgetHandler, match, player.getName());
			this.entity = nade;
			this.origin = origin;
		}
		
		private void explode(Location location, Player shooter) {
			location.getWorld().createExplosion(location, -1F);
			final String shooterName = shooter.getName();
			for (Vector v : Utils.getDirections()) {
				final Snowball s  = location.getWorld().spawn(location, Snowball.class);
				s.setShooter(shooter);
				Ball.registerBall(s, shooterName, source);
				Vector v2 = v.clone();
				v2.setX(v.getX() + Math.random() - Math.random());
				v2.setY(v.getY() + Math.random() - Math.random());
				v2.setZ(v.getZ() + Math.random() - Math.random());
				s.setVelocity(v2.normalize().multiply(Paintball.instance.grenadeShrapnelSpeed));
				Paintball.instance.getServer().getScheduler().scheduleSyncDelayedTask(Paintball.instance, new Runnable() {

					@Override
					public void run() {
						if (!s.isDead() || s.isValid()) {
							Ball.getBall(s.getEntityId(), shooterName, true);
							s.remove();
						}
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
	
	
	
	private final Egg entity;
	private final Origin source;

	public Grenade(Egg entity, Origin source) {
		this.entity = entity;
		this.source = source;
	}

	int getId() {
		return entity.getEntityId();
	}

	public Origin getSource() {
		return source;
	}
	
	public void explode(Location location, Player shooter) {
		location.getWorld().createExplosion(location, -1F);
		final String shooterName = shooter.getName();
		for (Vector v : Utils.getDirections()) {
			final Snowball s  = location.getWorld().spawn(location, Snowball.class);
			s.setShooter(shooter);
			Ball.registerBall(s, shooterName, source);
			Vector v2 = v.clone();
			v2.setX(v.getX() + Math.random() - Math.random());
			v2.setY(v.getY() + Math.random() - Math.random());
			v2.setZ(v.getZ() + Math.random() - Math.random());
			s.setVelocity(v2.normalize().multiply(Paintball.instance.grenadeShrapnelSpeed));
			Paintball.instance.getServer().getScheduler().scheduleSyncDelayedTask(Paintball.instance, new Runnable() {

				@Override
				public void run() {
					if (!s.isDead() || s.isValid()) {
						Ball.getBall(s.getEntityId(), shooterName, true);
						s.remove();
					}
				}
			}, (long) Paintball.instance.grenadeTime);
		}
	}
	
	void remove() {
		entity.remove();
	}

}
