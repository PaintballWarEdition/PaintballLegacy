package de.blablubbabc.paintball.lobby;

import java.util.List;

public class OtherSettings {
	// WEAPONS and GADGET
	// Marker
	public double speedmulti;
	// Grenade
	public boolean grenade;
	public int grenadeTime;
	public double grenadeSpeed;
	public int grenadeAmount;
	public double grenadeShrapnelSpeed;
	// Airstrike
	public boolean airstrike;
	public int airstrikeRange;
	public int airstrikeBombs;
	public int airstrikeAmount;
	public int airstrikeHeight;
	public int airstrikeMatchLimit;
	public int airstrikePlayerLimit;
	// Turret
	public boolean turret;
	public int turretAngleMin;
	public int turretAngleMax;
	public int turretTicks;
	public int turretXSize;
	public int turretYSize;
	public int turretSalve;
	public int turretCooldown;
	public int turretLives;
	public int turretMatchLimit;
	public int turretPlayerLimit;

	public boolean rocket;
	public int rocketRange;
	public double rocketSpeedMulti;
	public int rocketTime;
	public int rocketMatchLimit;
	public int rocketPlayerLimit;

	public boolean mine;
	public double mineRange;
	public int mineTime;
	public int mineMatchLimit;
	public int minePlayerLimit;

	public boolean pumpgun;
	public int pumpgunBullets;
	public double pumpgunSpray;
	public double pumpgunSpeedmulti;
	public int pumpgunAmmo;

	public boolean shotgun;
	public int shotgunAngle1;
	public int shotgunAngle2;
	public int shotgunAngleVert;
	public double shotgunSpeedmulti;
	public int shotgunAmmo;

	public boolean sniper;
	public double sniperSpeedmulti;
	public boolean sniperOnlyUseIfZooming;
	public boolean sniperRemoveSpeed;
	public boolean sniperNoGravity;
	public int sniperNoGravityDuration;

	public boolean orbitalstrike;
	public int orbitalstrikeMatchLimit;
	public int orbitalstrikePlayerLimit;

	public boolean flashbang;
	public double flashbangSpeed;
	public double flashRange;
	public int flashConfusionDuration;
	public int flashBlindnessDuration;
	public int flashSlownessDuration;
	public int flashbangTimeUntilExplosion;

	public boolean concussion;
	public double concussionSpeed;
	public double concussionRange;
	public int concussionConfusionDuration;
	public int concussionBlindnessDuration;
	public int concussionSlownessDuration;
	public int concussionTimeUntilExplosion;

	public boolean grenade2;
	public double grenade2Time;
	public int grenade2TimeUntilExplosion;
	public double grenade2Speed;
	public double grenade2ShrapnelSpeed;

	// Shop book
	public boolean shopCloseMenuOnPurchase;

	// SHOPS
	public List<String> shopGoods;
}
