package de.blablubbabc.paintball.api.lobby;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Color;

import de.blablubbabc.paintball.api.pplayer.PPlayerI;

public interface TeamI {
	public String getName();
	public Color getColor();
	public ChatColor getChatColor();
	public Set<PPlayerI> getPPlayers();
}
