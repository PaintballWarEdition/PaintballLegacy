package de.blablubbabc.paintball.api.lobby;

import de.blablubbabc.paintball.api.arena.ArenaI;
import de.blablubbabc.paintball.api.gamemode.GamemodeI;

/**
 * A match consists of an arena, a gamemode and a set of settings for that
 * gametype.
 */
public interface MatchSelectionI {

	public ArenaI getArena();

	public GamemodeI getGamemode();

	public String getGamemodeSettingsName();

}
