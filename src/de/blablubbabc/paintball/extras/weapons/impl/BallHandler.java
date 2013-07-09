package de.blablubbabc.paintball.extras.weapons.impl;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.extras.weapons.Gadget;
import de.blablubbabc.paintball.extras.weapons.GadgetManager;
import de.blablubbabc.paintball.extras.weapons.WeaponHandler;
import de.blablubbabc.paintball.utils.Translator;

public class BallHandler extends WeaponHandler {

	private GadgetManager gadgetManager = new GadgetManager();
	
	public BallHandler(int customItemTypeID, boolean useDefaultType) {
		super(customItemTypeID, useDefaultType);
	}
	
	public Ball createBall(Match match, Player player, Snowball entity, Origin origin) {
		return new Ball(match, player, entity, origin);
	}
	
	public boolean isBall(Entity entity) {
		return gadgetManager.isGadget(entity);
	}
	
	public boolean isBall(Entity entity, String playerName) {
		return gadgetManager.isGadget(entity, playerName);
	}
	
	public boolean isBall(Entity entity, Match match, String playerName) {
		return gadgetManager.isGadget(entity, match, playerName);
	}
	
	public Gadget getBall(Entity entity, boolean removeWhenFound) {
		return gadgetManager.getGadget(entity, removeWhenFound);
	}
	
	public Gadget getBall(Entity entity, String playerName, boolean removeWhenFound) {
		return gadgetManager.getGadget(entity, playerName, removeWhenFound);
	}
	
	public Gadget getBall(Entity entity, Match match, String playerName, boolean removeWhenFound) {
		return gadgetManager.getGadget(entity, match, playerName, removeWhenFound);
	}

	@Override
	protected int getDefaultItemTypeID() {
		return Material.SNOW_BALL.getId();
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
		// Done by MarkerHandler
	}

	public class Ball extends Gadget {
		
		private final Snowball entity;
		private final Origin origin;

		private Ball(Match match, Player player, Snowball entity, Origin origin) {
			super(gadgetManager, match, player.getName());
			this.entity = entity;
			this.origin = origin;
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
		
		public Snowball getSnowball() {
			return entity;
		}
		
	}
	
}
