package de.blablubbabc.paintball.lobby;

import java.util.List;

import org.bukkit.Location;

public class Lobby {
	private int currentLobbyspawn;

	// SETTINGS:
	// lobby spawns
	public List<Location> lobbyspawns;

	
	// countdown
	public int countdown;
	public int countdownInit;
	public int countdownStart;
	// when to start searching for a game
	public int minPlayers;
	// when to stop players from joining a team
	public int maxPlayers;
	// team selection
	public boolean onlyRandom;
	public boolean autoRandom;
	// damage
	public boolean otherDamage;
	public boolean falldamage;
	// melee
	public boolean allowMelee;
	public int meleeDamage;
	// arena rotation
	public boolean arenaRotationRandom;
	// whether shop shall be disable through out all games in this lobby
	public boolean shop;
	// the by default suggested shop for games in this lobby. Gamemode settings
	// can decide if they want to use this.
	// public Shop shop;

	// arena voting
	public boolean arenaVoting;
	// between 2 and 8:
	public int arenaVotingOptions;
	public boolean arenaVotingRandomOption;
	public int arenaVotingBroadcastOptionsAtCountdownTime;
	public int arenaVotingEndAtCountdownTime;
	
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
