package de.blablubbabc.paintball.lobby;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;

import de.blablubbabc.paintball.api.lobby.LobbyI;
import de.blablubbabc.paintball.api.pplayer.PPlayerI;
import de.blablubbabc.paintball.lobby.settings.LobbySettings;
import de.blablubbabc.paintball.lobby.settings.old.PPlayer;

public class Lobby implements LobbyI {
	private final String lobbyName;
	private final LobbySettings settings;
	// lobby spawns
	private List<Location> lobbyspawns;
	private int currentLobbyspawn;
	// state
	private LobbyState state = LobbyState.CLOSED;
	
	private final Set<PPlayer> pplayers = new HashSet<PPlayer>();
	
	public Lobby(String lobbyName, LobbySettings settings) {
		this.lobbyName = lobbyName;
		this.settings = settings;
		
		// TODO lobbies can be closed persistently
		state = LobbyState.WAITING;
	}
	
	@Override
	public String getName() {
		return lobbyName;
	}
	
	@Override
	public LobbyState getState() {
		return state;
	}
	
	@Override
	public Set<PPlayerI> getPPlayers() {
		return new HashSet<PPlayerI>(pplayers);
	}

	@Override
	public LobbySettings getSettings() {
		return settings;
	}
	
	@Override
	public void close() {
		// TODO translation
		kickAllPlayers("Lobby was closed.");
		state = LobbyState.CLOSED;
	}
	
	@Override
	public void kickAllPlayers(String message) {
		//TODO;
	}
	
}
