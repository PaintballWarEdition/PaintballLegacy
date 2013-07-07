package de.blablubbabc.paintball.extras.weapons;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Origin;

public class Ball extends Gadget {
	
	private final Snowball entity;
	private final Origin origin;

	public Ball(GadgetManager gadgetHandler, Match match, Player player, Snowball entity, Origin origin) {
		super(gadgetHandler, match, player.getName());
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
	
}
