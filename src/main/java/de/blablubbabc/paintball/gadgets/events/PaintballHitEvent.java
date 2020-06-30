/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.gadgets.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.ProjectileHitEvent;

import de.blablubbabc.paintball.Match;

public class PaintballHitEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final ProjectileHitEvent event;
	private final Match match;
	private final Player shooter;

	public PaintballHitEvent(ProjectileHitEvent event, Match match, Player shooter) {
		this.event = event;
		this.match = match;
		this.shooter = shooter;
	}

	public ProjectileHitEvent getProjectileHitEvent() {
		return event;
	}

	public Match getMatch() {
		return match;
	}

	public Player getShooter() {
		return shooter;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
