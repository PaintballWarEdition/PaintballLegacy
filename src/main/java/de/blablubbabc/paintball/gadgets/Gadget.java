/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.gadgets;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Origin;

public abstract class Gadget {

	private final GadgetManager gadgetManager;
	
	protected final Origin origin;
	protected final Match match;
	protected final String playerName;
	protected boolean valid = true;
	
	protected Gadget(GadgetManager gadgetManager, Match match, String playerName, Origin origin) {
		this.gadgetManager = gadgetManager;
		this.match = match;
		this.playerName = playerName;
		this.origin = origin;
		gadgetManager.addGadget(match, playerName, this);
	}
	
	/**
	 * Returns false, if this gadget was already removed from the underlying gadgetManager via the dispose-method
	 * @return false, if this gadget was already removed from the underlying gadgetManager via the dispose-method
	 */
	protected boolean isValid() {
		return valid;
	}
	
	public Match getMatch() {
		return match;
	}
	
	public String getPlayerName() {
		return playerName;
	}
	
	public abstract boolean isSimiliar(Entity entity);
	public abstract boolean isSimiliar(Location location);
	
	public Origin getGadgetOrigin() {
		return origin;
	}
	
	/**
	 * Used to remove this gadget from the underlying GadgetManager's tracking
	 * and lets WeaponHandlers handle removal.
	 * 
	 * @param removeFromGadgetHandlerTracking
	 *            whether or not to remove this gadget from the underlying
	 *            GadgetManager. After the first time this method is called the gadgets valid flag is set to false.
	 */
	public void dispose(boolean removeFromGadgetHandlerTracking) {
		if (removeFromGadgetHandlerTracking && valid) {
			valid = false;
			gadgetManager.removeGadget(match, playerName, this);
		}
	}

}
