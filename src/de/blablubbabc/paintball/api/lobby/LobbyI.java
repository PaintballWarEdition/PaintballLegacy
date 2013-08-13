package de.blablubbabc.paintball.api.lobby;

import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.blablubbabc.paintball.api.pplayer.PPlayerI;
import de.blablubbabc.paintball.lobby.LobbyState;
import de.blablubbabc.paintball.lobby.settings.LobbySettings;

public interface LobbyI {
	public String getName();
	public LobbyState getState();
	public Set<PPlayerI> getPPlayers();
	public LobbySettings getSettings();
	public List<Location> getSpawns();
	
	public void setName(String newName);
	public void setSettings(LobbySettings newSettings);
	public void setSpawns(List<Location> newSpawns);
	
	public void addSpawn(Location spawn);
	public void close();
	public void kickPlayer(String playerName);
	public void kickAllPlayers(String message);
	public void join(Player player);
}
