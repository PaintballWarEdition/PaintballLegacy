package de.blablubbabc.paintball.lobby.settings.old;

import java.util.List;

public class Config {
	public static Config instance = null;
	
	public Config() {
		instance = this;
		
		// load config:
		
		//TODO
	}
	
	// general:
	public boolean versioncheck;
	public boolean noPerms;
	public boolean debug;
	public boolean effects;
	// Language
	public String local;
	public String languageFileEncoding;
	
	// lobby settings
	
	public String defaultLobbySettingsName;
	public String defaultLobbyName;
	
	// lobby join checks
	public boolean checkInventory;
	public boolean checkGamemode;
	public boolean checkFlymode;
	public boolean checkBurning;
	public boolean checkHealth;
	public boolean checkFood;
	public boolean checkEffects;
	
	public boolean saveInventory;
	public int joinDelaySeconds;
	
	public boolean autoLobby;
	//public String defaultLobbyName;
	public boolean autoTeam;
	public boolean worldMode;
	public List<String> worldModeWorlds;

	public boolean commandSignEnabled;
	public String commandSignIdentifier;
	public boolean commandSignIgnoreShopDisabled;

	public boolean vote;
	public int voteCash;

	// vault rewards
	public boolean vaultRewardsEnabled;
	public double vaultRewardWin;
	public double vaultRewardRound;
	
	// points und cash
	public int pointsPerWin;
	public int pointsPerRound;
	public int cashPerWin;
	public int cashPerRound;

	// invisible after teleport workaround via hide + delayed unhide
	public boolean teleportFix;
	public boolean useXPBar;
	public int protectionTime;
	// should be moved into arena ymls
	//public List<String> disabledArenas;

	// player tags; defaults -> gamemodes can override these
	public boolean tags;
	public boolean tagsColor;
	public boolean tagsInvis;
	public boolean tagsRemainingInvis;

	// melody
	public boolean melody;
	public int melodyDelay;
	public String melodyWin;
	public String melodyDefeat;
	public String melodyDraw;
	// unused
	// public boolean autoSpecDeadPlayers;

	

}
