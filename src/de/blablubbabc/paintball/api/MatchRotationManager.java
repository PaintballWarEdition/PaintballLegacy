package de.blablubbabc.paintball.api;

/**
 * Handles what match shall be played next for a lobby by taking care of things
 * like the playlist, rotation, admin forcing and voting.
 * 
 * @author blablubbabc
 * 
 */
public interface MatchRotationManager {
	
	public void handleForceMatchSelection();
	public void handleVote();
	
}
