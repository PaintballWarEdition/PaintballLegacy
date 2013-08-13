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
import de.blablubbabc.paintball.pplayer.PPlayer;

public class Lobby implements LobbyI {
	private String lobbyName;
	private LobbySettings settings;
	// lobby spawns
	private List<Location> lobbyspawns;
	private int currentLobbyspawn = 0;
	// state
	private LobbyState state = LobbyState.CLOSED;
	
	private final Set<PPlayer> pplayers = new HashSet<PPlayer>();
	
	public Lobby(String lobbyName, LobbySettings settings, List<Location> lobbyspawns) {
		Validate.notNull(lobbyName, "Invalid LobbyName: null");
		Validate.notNull(settings, "Invalid LobbySettings: null");
		Validate.notNull(lobbyspawns, "Invalid LobbySpawns: null");
		
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
		Validate.notNull(spawn, "Invalid spawn location: null");
		
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
	public void open() {
		state = LobbyState.WAITING;
	}
	
	@Override
	public void kickAllPlayers(String message) {
		Validate.notNull(message, "Invalid message: null");
		
		for (PPlayer pplayer : pplayers) {
			kickPlayer(pplayer, message);
		}
	}

	@Override
	public void kickPlayer(String playerName, String message) {
		
	}
	
	@Override
	public void kickPlayer(PPlayerI pplayer, String message) {
		Validate.notNull(pplayer, "Invalid pplayer: null");
		Validate.notNull(message, "Invalid message: null");
		
		// reset player when he really was in this lobby
		if (pplayers.remove(pplayer)) {
			// playerManager.clearRestoreTeleportPlayer(pplayer.getPlayer());
		}
	}

	@Override
	public void join(Player player, Runnable runAfterwards) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Location getNextSpawn() {
		if(currentLobbyspawn >= lobbyspawns.size()) currentLobbyspawn = 0;
		return (lobbyspawns.size() > 0 ? lobbyspawns.get(currentLobbyspawn) : null);
	}

	
	
}
