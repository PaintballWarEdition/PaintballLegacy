package de.blablubbabc.paintball.api;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Color;

public interface Team {
	public String getName();
	public Color getColor();
	public ChatColor getChatColor();
	public Set<PPlayer> getPPlayers();
}
