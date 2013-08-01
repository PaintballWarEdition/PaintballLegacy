package de.blablubbabc.paintball.api;

/**
 * A match consists of an arena, a gametype and a set of settings for that gametype.
 * 
 * @author blablubbabc
 *
 */
public interface MatchSelection {
	
	public Arena getArena();
	public Gametype getGametype();
	public String getSettingsName();
	
}
