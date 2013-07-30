package de.blablubbabc.paintball.lobby;

import java.util.List;

import org.bukkit.Location;

public class Lobby {
	private int currentLobbyspawn;

	// SETTINGS:
	// lobby spawns
	public List<Location> lobbyspawns;
	// lobby join checks
	public boolean checkInventory;
	public boolean checkGamemode;
	public boolean checkFlymode;
	public boolean checkBurning;
	public boolean checkHealth;
	public boolean checkFood;
	public boolean checkEffects;

	public int joinDelaySeconds;

	// countdown
	public int countdown;
	public int countdownInit;
	public int countdownStart;
	// lobby size
	public int minPlayers;
	public int maxPlayers;
	// arena rotation
	public boolean arenaRotationRandom;
	// whether shop shall be disable through out all games in this lobby
	public boolean shop;
	// the by default suggested shop for games in this lobby. Gamemode settings
	// can decide if they want to use this.
	// public Shop shop;

	// command blocking
	public List<String> allowedCommands;
	public List<String> blacklistedCommandsRegex;
	public boolean checkBlacklist;
	public boolean blacklistAdminOverride;

	// ranks
	public boolean ranksLobbyArmor;
	public boolean ranksChatPrefix;
	public boolean ranksChatPrefixOnlyForPaintballers;
	public boolean ranksAdminBypassShop;

}
