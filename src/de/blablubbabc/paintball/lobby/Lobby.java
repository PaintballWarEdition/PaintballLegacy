package de.blablubbabc.paintball.lobby;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.blablubbabc.paintball.api.lobby.LobbyI;
import de.blablubbabc.paintball.api.pplayer.PPlayerI;
import de.blablubbabc.paintball.lobby.settings.LobbySettings;
import de.blablubbabc.paintball.lobby.settings.old.PPlayer;

public class Lobby implements LobbyI {
	private String lobbyName;
	private LobbySettings settings;
	// lobby spawns
	private List<Location> lobbyspawns;
	private int currentLobbyspawn;
	// state
	private LobbyState state = LobbyState.CLOSED;
	
	private final Set<PPlayer> pplayers = new HashSet<PPlayer>();
	
	public Lobby(String lobbyName, LobbySettings settings, List<Location> lobbyspawns) {
		Validate.notNull(lobbyName, "LobbyName is null!");
		Validate.notNull(settings, "LobbySettings is null!");
		Validate.notNull(lobbyspawns, "LobbySpawns is null!");
		
		this.lobbyName = lobbyName;
		this.settings = settings;
		this.lobbyspawns = lobbyspawns;
		
		// TODO lobbies can be closed persistently
		state = LobbyState.WAITING;
	}
	
	@Override
	public String getName() {
		return lobbyName;
	}
	
	@Override
	public List<Location> getSpawns() {
		return lobbyspawns;
	}

	@Override
	public void addSpawn(Location spawn) {
		Validate.notNull(spawn, "Spawn is null!");
		
		lobbyspawns.add(spawn);
		// save lobbies file:
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
	public void setName(String newName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSettings(LobbySettings newSettings) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSpawns(List<Location> newSpawns) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void close() {
		// TODO translation
		kickAllPlayers("Lobby was closed.");
		state = LobbyState.CLOSED;
	}
	
	@Override
	public void kickAllPlayers(String message) {
		Validate.notNull(message, "Message is null!");
		
		//TODO;
	}

	@Override
	public void kickPlayer(String playerName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void join(Player player) {
		// TODO Auto-generated method stub
		
	}
	
}
