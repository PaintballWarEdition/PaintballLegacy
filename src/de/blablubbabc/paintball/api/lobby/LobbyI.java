package de.blablubbabc.paintball.api.lobby;

import java.util.Set;

import de.blablubbabc.paintball.api.pplayer.PPlayerI;
import de.blablubbabc.paintball.lobby.LobbyState;
import de.blablubbabc.paintball.lobby.settings.LobbySettings;

public interface LobbyI {
	public String getName();
	public LobbyState getState();
	public Set<PPlayerI> getPPlayers();
	public LobbySettings getSettings();
	public void close();
	public void kickAllPlayers(String message);
}
