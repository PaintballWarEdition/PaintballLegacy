package de.blablubbabc.paintball.api.pplayer;

import de.blablubbabc.paintball.api.lobby.LobbyI;
import de.blablubbabc.paintball.api.lobby.TeamI;

public interface PPlayerI {
	
	public LobbyI getLobby();
	public TeamI getTeam();
	public boolean hasToggledFeedOff();
}
