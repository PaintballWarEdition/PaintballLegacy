package de.blablubbabc.paintball.api.lobby;

/**
 * Handles what match shall be played next for a lobby by taking care of things
 * like the playlist, rotation, admin forcing and voting.
 * 
 * @author blablubbabc
 * 
 */
public interface MatchRotationManagerI {
	
	public void handleForceMatchSelection();
	public void handleVote();
	
}
