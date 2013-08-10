package de.blablubbabc.paintball.lobby.settings.old;

public class TDMSettings {

	// match pre game countdown
	public int countdownStart;
	// roundtimer
	public int roundTimer;
	// lives and respawns
	public int lives;
	public int respawns;
	// initial paintballs
	public int balls;
	// whether shop is available
	public boolean shop;

	// points und cash
	public int pointsPerKill;
	public int pointsPerHit;
	public int pointsPerTeamattack;
	public int pointsPerWin;
	public int cashPerKill;
	public int cashPerHit;
	// vault rewards
	public boolean vaultRewardsEnabled;
	public double vaultRewardKill;
	public double vaultRewardHit;
	public double vaultRewardRound;

	// appearance
	public boolean listnames;
	// chat
	public boolean chatMessageColor;
	public boolean chatNameColor;
	// scoreboards
	public boolean scoreboardLobby;
	public boolean scoreboardMatch;
}
