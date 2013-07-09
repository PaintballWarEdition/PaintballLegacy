package de.blablubbabc.paintball.extras.weapons;

import org.bukkit.entity.Entity;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Origin;

public abstract class Gadget {

	protected final GadgetManager gadgetHandler;
	protected final Match match;
	protected final String playerName;
	
	protected Gadget(GadgetManager gadgetHandler, Match match, String playerName) {
		this.gadgetHandler = gadgetHandler;
		this.match = match;
		this.playerName = playerName;
		gadgetHandler.addGadget(match, playerName, this);
	}
	
	protected GadgetManager getGadgetManager() {
		return gadgetHandler;
	}
	
	public Match getMatch() {
		return match;
	}
	
	public String getPlayerName() {
		return playerName;
	}
	
	protected abstract boolean isSimiliar(Entity entity);
	public abstract Origin getOrigin();
	
	public void dispose(boolean removeFromGadgetHandlerTracking, boolean cheapEffects) {
		if (removeFromGadgetHandlerTracking) gadgetHandler.removeGadget(match, playerName, this);
	}

}
