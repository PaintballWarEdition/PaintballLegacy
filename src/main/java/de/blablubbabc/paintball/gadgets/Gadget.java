/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.gadgets;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.Paintball;

public abstract class Gadget {

	private final GadgetManager gadgetManager;

	protected final Origin origin;
	protected final Match match;
	protected final Player player;
	protected boolean valid = true;

	protected Gadget(GadgetManager gadgetManager, Match match, Player player, Origin origin) {
		this.gadgetManager = gadgetManager;
		this.match = match;
		this.player = player;
		this.origin = origin;
		gadgetManager.addGadget(match, player.getUniqueId(), this);
	}

	/**
	 * Returns false, if this gadget was already removed from the underlying gadgetManager via the dispose-method
	 * 
	 * @return false, if this gadget was already removed from the underlying gadgetManager via the dispose-method
	 */
	protected boolean isValid() {
		return valid;
	}

	public Match getMatch() {
		return match;
	}

	public Player getPlayer() {
		return player;
	}

	/*public UUID getPlayerId() {
		return player.getUniqueId();
	}*/

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
			Plugin plugin = Paintball.getInstance();
			if (plugin.isEnabled()) {
				// Remove delayed:
				// This resolves some issue with the order of the ProjectileHitEvent (which cleans up the gadget) and
				// other events, which also try to check if the involved entity is a gadget, having changed in MC 1.16.
				// The delayed removal allows those other events, after the ProjectileHitEvent, to check if the entity
				// is a gadget.
				// TODO Improve this to not create and schedule a new task for every gadget removal. Maybe use one
				// cleanup task.
				Bukkit.getScheduler().runTask(plugin, () -> {
					gadgetManager.removeGadget(match, player.getUniqueId(), this);
				});
			} else {
				// If the plugin is currently getting disabled, remove immediately:
				gadgetManager.removeGadget(match, player.getUniqueId(), this);
			}
		}
	}
}
