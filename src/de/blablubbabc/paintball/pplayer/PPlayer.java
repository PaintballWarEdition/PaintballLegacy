package de.blablubbabc.paintball.pplayer;

import org.bukkit.entity.Player;

import de.blablubbabc.paintball.api.lobby.LobbyI;
import de.blablubbabc.paintball.api.pplayer.PPlayerI;

public class PPlayer implements PPlayerI {
	private int afkCounter;

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Player getPlayer() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public LobbyI getLobby() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasToggledFeedOff() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSpectator() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInMatch() {
		// TODO Auto-generated method stub
		return false;
	}
}