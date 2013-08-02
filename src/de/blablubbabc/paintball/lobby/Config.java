package de.blablubbabc.paintball.lobby;

import java.util.List;

import de.blablubbabc.paintball.gadgets.Gift;

public class Config {
	// general:
	public boolean versioncheck;
	public boolean noPerms;
	public boolean debug;
	public boolean effects;
	// Language
	public String local;
	public String languageFileEncoding;
	
	
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
	
	public boolean afkDetection;
	public int afkRadius;
	public int afkRadius2;
	public int afkMatchAmount;
	
	public boolean autoSpecLobby;

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

	public boolean teleportFix;
	public boolean useXPBar;
	public int protectionTime;
	public List<String> disabledArenas;

	// player tags
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
