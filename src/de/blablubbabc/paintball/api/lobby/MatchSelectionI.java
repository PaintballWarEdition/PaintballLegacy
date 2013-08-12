package de.blablubbabc.paintball.api.lobby;

import de.blablubbabc.paintball.api.arena.ArenaI;
import de.blablubbabc.paintball.api.gametype.GametypeI;

/**
 * A match consists of an arena, a gametype and a set of settings for that gametype.
 * 
 * @author blablubbabc
 *
 */
public interface MatchSelectionI {
	
	public ArenaI getArena();
	public GametypeI getGametype();
	public String getSettingsName();
	
}
