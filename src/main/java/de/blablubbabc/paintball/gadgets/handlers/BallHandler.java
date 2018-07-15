/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.gadgets.handlers;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.gadgets.Gadget;
import de.blablubbabc.paintball.gadgets.GadgetManager;
import de.blablubbabc.paintball.gadgets.WeaponHandler;
import de.blablubbabc.paintball.utils.Translator;

public class BallHandler extends WeaponHandler {

	private GadgetManager gadgetManager = new GadgetManager();

	public BallHandler() {
		this(null);
	}

	public BallHandler(Material customItemType) {
		super("Paintball", customItemType, null);
	}

	public Ball createBall(Match match, Player player, Snowball entity, Origin origin) {
		return new Ball(match, player, entity, origin);
	}

	public boolean isBall(Entity entity) {
		return gadgetManager.isGadget(entity);
	}

	public boolean isBall(Entity entity, UUID playerId) {
		return gadgetManager.isGadget(entity, playerId);
	}

	public boolean isBall(Entity entity, Match match, UUID playerId) {
		return gadgetManager.isGadget(entity, match, playerId);
	}

	public Gadget getBall(Entity entity) {
		return gadgetManager.getGadget(entity);
	}

	public Gadget getBall(Entity entity, UUID playerId) {
		return gadgetManager.getGadget(entity, playerId);
	}

	public Gadget getBall(Entity entity, Match match, UUID playerId) {
		return gadgetManager.getGadget(entity, match, playerId);
	}

	@Override
	protected Material getDefaultItemType() {
		return Material.SNOWBALL;
	}

	@Override
	protected ItemStack setItemMeta(ItemStack itemStack) {
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(Translator.getString("WEAPON_PAINTBALL"));
		itemStack.setItemMeta(meta);
		return itemStack;
	}

	@Override
	protected void onInteract(PlayerInteractEvent event, Match match) {
		// done by MarkerHandler
	}

	@Override
	public void cleanUp(Match match, UUID playerId) {
		gadgetManager.cleanUp(match, playerId);
	}

	@Override
	public void cleanUp(Match match) {
		gadgetManager.cleanUp(match);
	}

	public class Ball extends Gadget {

		private final Snowball entity;

		private Ball(Match match, Player player, Snowball entity, Origin origin) {
			super(Paintball.getInstance().weaponManager.getBallHandler().gadgetManager, match, player, origin);
			this.entity = entity;
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

		public Snowball getSnowball() {
			return entity;
		}
	}
}
