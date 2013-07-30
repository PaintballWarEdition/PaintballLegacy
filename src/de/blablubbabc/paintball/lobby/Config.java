package de.blablubbabc.paintball.lobby;

import java.util.ArrayList;
import java.util.List;

import de.blablubbabc.paintball.gadgets.Gift;

public class Config {
	// general:
	public boolean versioncheck;
	public boolean noPerms;
	// Language
	public String local;
	public String languageFileEncoding;
	
	
	
	public boolean saveInventory;
	public boolean onlyRandom;
	public boolean autoRandom;
	
	public boolean otherDamage;
	public boolean falldamage;
	public boolean allowMelee;
	public int meleeDamage;
	public boolean autoLobby;
	public boolean autoTeam;
	public boolean worldMode;
	public List<String> worldModeWorlds;
	public boolean afkDetection;
	public int afkRadius;
	public int afkRadius2;
	public int afkMatchAmount;
	public boolean autoSpecLobby;
	public boolean effects;
	public boolean debug;

	// arena voting
	public boolean arenaVoting;
	// between 2 and 8:
	public int arenaVotingOptions;
	public boolean arenaVotingRandomOption;
	public int arenaVotingBroadcastOptionsAtCountdownTime;
	public int arenaVotingEndAtCountdownTime;

	public boolean commandSignEnabled;
	public String commandSignIdentifier;
	public boolean commandSignIgnoreShopDisabled;

	public boolean vote;
	public int voteCash;

	// vault rewards
	public boolean vaultRewardsEnabled;
	public double vaultRewardKill;
	public double vaultRewardHit;
	public double vaultRewardWin;
	public double vaultRewardRound;

	public boolean teleportFix;
	public boolean useXPBar;
	public int protectionTime;
	public List<String> disabledArenas;

	// gifts
	public boolean giftsEnabled;
	public double giftOnSpawnChance;
	public ArrayList<Gift> gifts;
	public double giftChanceFactor;
	public boolean bWishes;
	public String wishes;
	public int wishesDelay;

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
