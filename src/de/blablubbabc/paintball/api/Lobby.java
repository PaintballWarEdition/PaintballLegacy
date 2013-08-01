package de.blablubbabc.paintball.api;

import java.util.Set;

public interface Lobby {
	public String getName();
	public LobbyState getState();
	public Set<PPlayer> getPPlayers();
	public Set<Team> getTeams();
	
	
	
	
	
	
	public enum LobbyState {
		WAITING,
		COUNTDOWN,
		RUNNING;
	}
}
