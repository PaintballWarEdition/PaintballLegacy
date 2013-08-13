package de.blablubbabc.paintball.api.pplayer;

import org.bukkit.entity.Player;

import de.blablubbabc.paintball.api.lobby.LobbyI;

public interface PPlayerI {
	
	public String getName();
	public Player getPlayer();
	
	public LobbyI getLobby();
	public boolean hasToggledFeedOff();
	public boolean isSpectator();
	public boolean isInMatch();
}
