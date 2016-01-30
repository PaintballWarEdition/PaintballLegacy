/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import de.blablubbabc.paintball.addons.melodies.Instrument;
import de.blablubbabc.paintball.addons.melodies.Musician;
import de.blablubbabc.paintball.commands.CommandManager;
import de.blablubbabc.paintball.features.InSignsFeature;
import de.blablubbabc.paintball.features.TagAPIListener;
import de.blablubbabc.paintball.features.VaultRewardsFeature;
import de.blablubbabc.paintball.features.VoteListener;
import de.blablubbabc.paintball.gadgets.Gift;
import de.blablubbabc.paintball.gadgets.WeaponManager;
import de.blablubbabc.paintball.shop.ShopManager;
import de.blablubbabc.paintball.statistics.arena.ArenaSetting;
import de.blablubbabc.paintball.statistics.arena.ArenaStat;
import de.blablubbabc.paintball.statistics.general.GeneralStat;
import de.blablubbabc.paintball.statistics.player.PlayerStat;
import de.blablubbabc.paintball.statistics.player.match.tdm.TDMMatchStat;
import de.blablubbabc.paintball.thirdparty.util.Metrics;
import de.blablubbabc.paintball.thirdparty.util.Metrics.Graph;
import de.blablubbabc.paintball.thirdparty.util.Updater;
import de.blablubbabc.paintball.thirdparty.util.Updater.UpdateType;
import de.blablubbabc.paintball.utils.Log;
import de.blablubbabc.paintball.utils.Serverlister;
import de.blablubbabc.paintball.utils.Sounds;
import de.blablubbabc.paintball.utils.TeleportManager;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

import de.blablubbabc.BlaDB.BlaSQLite;
import de.blablubbabc.commandsigns.CommandSignsListener;

/**
 * This file is part of blablubbabc's paintball-plugin.
 * Do not redistribute or modify it in any way. Use it as it is.
 * Do not copy, redistribute or give away.
 * Usage on own risk. I give no warranties.
 * Commercial usage in any way is not allowed! Neither direct nor indirect.
 * These terms of use apply to every part of the plugin.
 * 
 * @author blablubbabc
 * 
 */
public class Paintball extends JavaPlugin {

	public static Paintball instance;
	public Thread mainThread;

	public boolean currentlyDisableing = false;

	private final Object asyncLock = new Object();
	private int asyncTaskCounter = 0;

	public void addAsyncTask() {
		synchronized (asyncLock) {
			asyncTaskCounter++;
		}
	}

	public void removeAsyncTask() {
		synchronized (asyncLock) {
			asyncTaskCounter--;
		}
	}

	public int getAsyncTasksCount() {
		synchronized (asyncLock) {
			return asyncTaskCounter;
		}
	}

	public boolean isAsyncTaskRunning() {
		synchronized (asyncLock) {
			return asyncTaskCounter > 0;
		}
	}

	public PlayerManager playerManager;
	public CommandManager commandManager;
	public MatchManager matchManager;
	public EventListener listener;
	public CommandSignsListener commandSignListener;
	public ShopManager shopManager;
	public TagAPIListener tagAPI;
	public VoteListener voteListener;
	public Newsfeeder feeder;
	public ArenaManager arenaManager;
	public Translator translator;
	public Musician musik;
	public Stats statsManager;
	public RankManager rankManager;
	public WeaponManager weaponManager;
	public Serverlister serverList;
	public InSignsFeature insignsFeature;

	private VaultRewardsFeature vaultRewardsFeature;

	public boolean active;
	public boolean happyhour;
	public boolean softreload;
	public boolean needsUpdate = false;

	// LOBBYSPAWNS
	public int lobbyspawn;
	private List<Location> lobbyspawns;

	// afk detection
	private Map<String, Integer> afkMatchCount;

	// CONFIG:
	// general:
	public boolean versioncheck;
	public boolean metrics;
	// language:
	public String local;
	public String languageFileEncoding;

	// uuid conversion:
	public boolean uuidFirstRun = false;
	public boolean uuidOnlineMode;
	public boolean uuidUseLocalPlayerData;
	public boolean uuidUseUUIDCollector;

	// paintball:
	public int countdown;
	public int countdownInit;
	public int countdownStart;
	public int roundTimer;
	public int minPlayers;
	public int maxPlayers;
	public boolean arenaRotationRandom;
	public int lives;
	public int respawns;
	public int balls;
	public double speedmulti;
	public boolean listnames;

	// chat
	public boolean chatMessageColor;
	public boolean chatNameColor;
	public boolean chatFeaturesOnlyForPaintballersVisible;
	public boolean chatReplaceFormat;
	public String chatFormat;

	// ranks
	public boolean ranksLobbyArmor;
	public boolean ranksChatPrefix;
	public boolean ranksAdminBypassShop;

	public boolean shop;
	public boolean shopCloseMenuOnPurchase;
	public List<String> shopGoods;

	public List<String> allowedCommands;
	public List<String> blacklistedCommandsRegex;
	public boolean checkBlacklist;

	public boolean blacklistAdminOverride;
	public boolean saveInventory;
	public boolean onlyRandom;
	public boolean autoRandom;
	public boolean noPerms;
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
	public boolean blockCommandTeleports;
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

	// lobby join checks
	public boolean checkInventory;
	public boolean checkGamemode;
	public boolean checkFlymode;
	public boolean checkBurning;
	public boolean checkHealth;
	public boolean checkFood;
	public boolean checkEffects;

	public int joinDelaySeconds;

	// scoreboards
	public boolean scoreboardLobby;
	public boolean scoreboardMatch;

	// points und cash
	public int pointsPerKill;
	public int pointsPerHit;
	public int pointsPerTeamattack;
	public int pointsPerWin;
	public int pointsPerRound;
	public int cashPerKill;
	public int cashPerHit;
	public int cashPerWin;
	public int cashPerRound;

	// Extras:
	public boolean grenade;
	public int grenadeTime;
	public double grenadeSpeed;
	public int grenadeAmount;
	public double grenadeShrapnelSpeed;

	public boolean airstrike;
	public int airstrikeRange;
	public int airstrikeBombs;
	public int airstrikeAmount;
	public int airstrikeHeight;
	public int airstrikeMatchLimit;
	public int airstrikePlayerLimit;

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
	public boolean mineCheckForHallway;
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

	public BlaSQLite sql;

	// public BlaDB data;

	@SuppressWarnings("unchecked")
	public void onEnable() {
		instance = this;
		mainThread = Thread.currentThread();

		// LOGGER
		Log.init(this);
		// enabled warnings logging:
		Log.logWarnings(true);

		// CONFIG
		ArrayList<String> goodsDef = new ArrayList<String>();

		// <amount>-<name>-<id>-<subid>-<price>-<rank>
		goodsDef.add("10-Balls-" + Material.SNOW_BALL.getId() + "-0-10-0");
		goodsDef.add("50-Balls-" + Material.SNOW_BALL.getId() + "-0-50-0");
		goodsDef.add("100-Balls-" + Material.SNOW_BALL.getId() + "-0-100-0");
		goodsDef.add("1-Grenade Mark 2-" + Material.SLIME_BALL.getId() + "-0-15-1");
		goodsDef.add("1-Grenade-" + Material.EGG.getId() + "-0-20-2");
		goodsDef.add("1-Concussion Nade-" + Material.SPIDER_EYE.getId() + "-0-15-2");
		goodsDef.add("1-Flashbang-" + Material.GHAST_TEAR.getId() + "-0-15-3");
		goodsDef.add("1-Rocket Launcher-" + Material.DIODE.getId() + "-0-20-3");
		goodsDef.add("1-Mine-" + Material.FLOWER_POT_ITEM.getId() + "-0-10-4");
		goodsDef.add("1-Pumpgun-" + Material.STONE_AXE.getId() + "-0-20-4");
		goodsDef.add("1-Speed-" + Material.POTION.getId() + "-16482-20-5");
		goodsDef.add("1-Shotgun-" + Material.SPECKLED_MELON.getId() + "-0-20-5");
		goodsDef.add("1-Airstrike-" + Material.STICK.getId() + "-0-80-6");
		goodsDef.add("1-Orbitalstrike-" + Material.BLAZE_ROD.getId() + "-0-80-7");
		goodsDef.add("1-Turret-" + Material.PUMPKIN.getId() + "-0-180-8");
		goodsDef.add("1-Sniper-" + Material.CARROT_STICK.getId() + "-0-80-9");

		ArrayList<Gift> giftsDef = new ArrayList<Gift>();
		giftsDef.add(new Gift(Material.SNOW_BALL.getId(), (short) 0, 50, 20.0, "Hope you have luck with these balls!"));
		giftsDef.add(new Gift(Material.GHAST_TEAR.getId(), (short) 0, 2, 10.0, "Blind them with these!"));
		giftsDef.add(new Gift(Material.SPIDER_EYE.getId(), (short) 0, 2, 10.0, "Confuse them with these!"));
		giftsDef.add(new Gift(Material.EGG.getId(), (short) 0, 2, 5.0, "May these grenades be with you!"));
		giftsDef.add(new Gift(Material.SLIME_BALL.getId(), (short) 0, 2, 5.0, "Some explosives for you!"));
		giftsDef.add(new Gift(Material.FLOWER_POT_ITEM.getId(), (short) 0, 2, 10.0, "I knew you ever wanted to be a sneaky killer!"));
		giftsDef.add(new Gift(Material.DIODE.getId(), (short) 0, 2, 10.0, "Give them hell with these rocket launchers!"));
		giftsDef.add(new Gift(Material.STICK.getId(), (short) 0, 1, 5.0, "I knew you ever wanted to order a airstrike at least once!"));
		giftsDef.add(new Gift(Material.BLAZE_ROD.getId(), (short) 0, 1, 5.0, "Support from orbit waits for your order!"));
		giftsDef.add(new Gift(Material.STONE_AXE.getId(), (short) 0, 1, 5.0, "Take this weapon!"));
		giftsDef.add(new Gift(Material.CHEST.getId(), (short) 0, 2, 5.0, "I got some more gifts for you!"));
		giftsDef.add(new Gift(Material.PUMPKIN.getId(), (short) 0, 1, 3.0, "This comerade will fight for you!"));
		giftsDef.add(new Gift(Material.SPECKLED_MELON.getId(), (short) 0, 1, 3.0, "This weapon comes fresh from production!"));
		giftsDef.add(new Gift(Material.CARROT_STICK.getId(), (short) 0, 1, 2.0, "No one can hide from your view!"));
		giftsDef.add(new Gift(Material.AIR.getId(), (short) 0, 0, 2.0, "You had no luck this time :("));

		allowedCommands = new ArrayList<String>();
		allowedCommands.add("/list");
		allowedCommands.add("/msg");
		allowedCommands.add("/m");
		allowedCommands.add("/r");
		allowedCommands.add("/whisper");
		allowedCommands.add("/tell");
		allowedCommands.add("/login *");
		allowedCommands.add("/register *");

		List<String> blacklistedCommands = new ArrayList<String>();
		blacklistedCommands.add("/ptp {player}");
		blacklistedCommands.add("/tp {args} {player}");
		blacklistedCommands.add("/tp {player} {args}");
		blacklistedCommands.add("/tphere {player}");

		getConfig().options()
				.header("Use a value of -1 to give the players infinite balls or extras. If you insert a not possible value/wrong value in a section the plugin will use the default value or the nearest possible value (Example: your value at section balls: -3 -> plugin will use -1). 1 Tick = 1/20 seconds.");
		if (getConfig().get("Server.Version Check") == null) getConfig().set("Server.Version Check", true);
		if (getConfig().get("Server.Metrics") == null) getConfig().set("Server.Metrics", true);
		if (getConfig().get("Paintball.AFK Detection.enabled") == null) getConfig().set("Paintball.AFK Detection.enabled", true);
		if (getConfig().get("Paintball.AFK Detection.Movement Radius around Spawn (keep in mind: knockbacks, pushing, waterflows, falling, etc)") == null) getConfig().set("Paintball.AFK Detection.Movement Radius around Spawn (keep in mind: knockbacks, pushing, waterflows, falling, etc)", 5);
		if (getConfig().get("Paintball.AFK Detection.Amount of Matches") == null) getConfig().set("Paintball.AFK Detection.Amount of Matches", 3);

		// language
		if (getConfig().get("Paintball.Language.File Name") == null) getConfig().set("Paintball.Language.File Name", "enUS");
		if (getConfig().get("Paintball.Language.File Encoding") == null) getConfig().set("Paintball.Language.File Encoding", "utf-8");

		// uuid conversion:
		if (!getConfig().isBoolean("Paintball.UUID Conversion.Online Mode")) {
			uuidFirstRun = true;
			getConfig().set("Paintball.UUID Conversion.Online Mode", Bukkit.getOnlineMode());
		}
		if (!getConfig().isBoolean("Paintball.UUID Conversion.Use UUIDCollector data")) getConfig().set("Paintball.UUID Conversion.Use UUIDCollector data", new File(this.getDataFolder().getParentFile(), "UUIDCollector").exists());
		if (!getConfig().isBoolean("Paintball.UUID Conversion.Use local player data")) getConfig().set("Paintball.UUID Conversion.Use local player data", true);

		if (getConfig().get("Paintball.No Permissions") == null) getConfig().set("Paintball.No Permissions", false);
		if (getConfig().get("Paintball.Debug") == null) getConfig().set("Paintball.Debug", false);
		if (getConfig().get("Paintball.VoteListener.enabled") == null) getConfig().set("Paintball.VoteListener.enabled", true);
		if (getConfig().get("Paintball.VoteListener.Cash") == null) getConfig().set("Paintball.VoteListener.Cash", 100);
		if (getConfig().get("Paintball.Auto Lobby") == null) getConfig().set("Paintball.Auto Lobby", false);
		if (getConfig().get("Paintball.Auto Team") == null) getConfig().set("Paintball.Auto Team", false);
		// points and cash:
		if (getConfig().get("Paintball.Points per Kill") == null) getConfig().set("Paintball.Points per Kill", 2);
		if (getConfig().get("Paintball.Points per Hit") == null) getConfig().set("Paintball.Points per Hit", 1);
		if (getConfig().get("Paintball.Points per Team-Attack") == null) getConfig().set("Paintball.Points per Team-Attack", -1);
		if (getConfig().get("Paintball.Points per Win") == null) getConfig().set("Paintball.Points per Win", 5);
		if (getConfig().get("Paintball.Points per Round") == null) getConfig().set("Paintball.Points per Round", 1);
		if (getConfig().get("Paintball.Cash per Kill") == null) getConfig().set("Paintball.Cash per Kill", 10);
		if (getConfig().get("Paintball.Cash per Hit") == null) getConfig().set("Paintball.Cash per Hit", 0);
		if (getConfig().get("Paintball.Cash per Win") == null) getConfig().set("Paintball.Cash per Win", 10);
		if (getConfig().get("Paintball.Cash per Round") == null) getConfig().set("Paintball.Cash per Round", 0);

		// vault rewards:
		if (getConfig().get("Paintball.Vault Rewards.enabled") == null) getConfig().set("Paintball.Vault Rewards.enabled", false);
		if (getConfig().get("Paintball.Vault Rewards.Money per Kill") == null) getConfig().set("Paintball.Vault Rewards.Money per Kill", 5.0);
		if (getConfig().get("Paintball.Vault Rewards.Money per Hit") == null) getConfig().set("Paintball.Vault Rewards.Money per Hit", 1.0);
		if (getConfig().get("Paintball.Vault Rewards.Money per Win") == null) getConfig().set("Paintball.Vault Rewards.Money per Win", 20.0);
		if (getConfig().get("Paintball.Vault Rewards.Money per Round") == null) getConfig().set("Paintball.Vault Rewards.Money per Round", 0.0);

		if (getConfig().get("Paintball.Ball speed multi") == null) getConfig().set("Paintball.Ball speed multi", 2.5);
		if (getConfig().get("Paintball.Colored listnames") == null) getConfig().set("Paintball.Colored listnames", true);
		// chat related
		if (getConfig().get("Paintball.Chat.Colored Message") == null) getConfig().set("Paintball.Chat.Colored Message", true);
		if (getConfig().get("Paintball.Chat.Colored Name") == null) getConfig().set("Paintball.Chat.Colored Name", false);
		if (getConfig().get("Paintball.Chat.Changes only visible for Paintballers") == null) getConfig().set("Paintball.Chat.Changes only visible for Paintballers", true);
		if (getConfig().get("Paintball.Chat.Format.Use alternate chat format") == null) getConfig().set("Paintball.Chat.Format.Use alternate chat format", false);
		if (getConfig().get("Paintball.Chat.Format.Alternate chat format") == null) getConfig().set("Paintball.Chat.Format.Alternate chat format", "&6%s&7: &f%s");

		// ranks
		if (getConfig().get("Paintball.Ranks.Chat Prefix.enabled") == null) getConfig().set("Paintball.Ranks.Chat Prefix.enabled", true);
		if (getConfig().get("Paintball.Ranks.Lobby Armor") == null) getConfig().set("Paintball.Ranks.Lobby Armor", true);

		// scoreboards
		if (getConfig().get("Paintball.Scoreboards.Lobby") == null) getConfig().set("Paintball.Scoreboards.Lobby", true);
		if (getConfig().get("Paintball.Scoreboards.Match") == null) getConfig().set("Paintball.Scoreboards.Match", true);

		// arena selection:
		if (getConfig().get("Paintball.Arena Rotation.Random Rotation") == null) getConfig().set("Paintball.Arena Rotation.Random Rotation", true);
		// arena voting:
		if (getConfig().get("Paintball.Arena Rotation.Arena Voting.enabled") == null) getConfig().set("Paintball.Arena Rotation.Arena Voting.enabled", true);
		// at least 2:
		if (getConfig().get("Paintball.Arena Rotation.Arena Voting.Number of Vote Options (at least 2)") == null) getConfig().set("Paintball.Arena Rotation.Arena Voting.Number of Vote Options (at least 2)", 4);
		if (getConfig().get("Paintball.Arena Rotation.Arena Voting.Random Arena Option") == null) getConfig().set("Paintball.Arena Rotation.Arena Voting.Random Arena Option", true);
		if (getConfig().get("Paintball.Arena Rotation.Arena Voting.Broadcast Options again at Countdown Time") == null) getConfig().set("Paintball.Arena Rotation.Arena Voting.Broadcast Options again at Countdown Time", 15);
		if (getConfig().get("Paintball.Arena Rotation.Arena Voting.End Voting at Countdown Time") == null) getConfig().set("Paintball.Arena Rotation.Arena Voting.End Voting at Countdown Time", 5);

		if (getConfig().get("Paintball.Only Random") == null) getConfig().set("Paintball.Only Random", false);
		if (getConfig().get("Paintball.Auto Random") == null) getConfig().set("Paintball.Auto Random", true);
		if (getConfig().get("Paintball.World Mode.enabled") == null) getConfig().set("Paintball.World Mode.enabled", false);
		if (getConfig().get("Paintball.World Mode.worlds") == null) getConfig().set("Paintball.World Mode.worlds", Arrays.asList("paintball"));
		if (getConfig().get("Paintball.Auto Spec Lobby") == null) getConfig().set("Paintball.Auto Spec Lobby", false);
		if (getConfig().get("Paintball.Effects") == null) getConfig().set("Paintball.Effects", true);
		if (getConfig().get("Paintball.Teleport Fix") == null) getConfig().set("Paintball.Teleport Fix", true);
		if (getConfig().get("Paintball.Block teleports by commands") == null) getConfig().set("Paintball.Block teleports by commands", true);
		if (getConfig().get("Paintball.Use XP Bar") == null) getConfig().set("Paintball.Use XP Bar", true);
		// command signs:
		if (getConfig().get("Paintball.Command Signs.enabled") == null) getConfig().set("Paintball.Command Signs.enabled", true);
		if (getConfig().get("Paintball.Command Signs.Command Sign Identifier") == null) getConfig().set("Paintball.Command Signs.Command Sign Identifier", "[Paintball]");
		if (getConfig().get("Paintball.Command Signs.Ignore Shop Disabled") == null) getConfig().set("Paintball.Command Signs.Ignore Shop Disabled", true);
		// commands black-/whitelist:
		if (getConfig().get("Paintball.Allowed Commands") == null) getConfig().set("Paintball.Allowed Commands", allowedCommands);
		if (getConfig().get("Paintball.Blacklist.Enabled") == null) getConfig().set("Paintball.Blacklist.Enabled", false);
		if (getConfig().get("Paintball.Blacklist.Admin Override") == null) getConfig().set("Paintball.Blacklist.Admin Override", true);
		if (getConfig().get("Paintball.Blacklist.Commands") == null) getConfig().set("Paintball.Blacklist.Commands", blacklistedCommands);
		// player tags
		if (getConfig().get("Paintball.Tags.enabled") == null) getConfig().set("Paintball.Tags.enabled", true);
		if (getConfig().get("Paintball.Tags.colored") == null) getConfig().set("Paintball.Tags.colored", true);
		if (getConfig().get("Paintball.Tags.invisible") == null) getConfig().set("Paintball.Tags.invisible", true);
		if (getConfig().get("Paintball.Tags.remaining invisible") == null) getConfig().set("Paintball.Tags.remaining invisible", true);
		// melody:
		if (getConfig().get("Paintball.Melodies.enable") == null) getConfig().set("Paintball.Melodies.enable", true);
		if (getConfig().get("Paintball.Melodies.delay") == null) getConfig().set("Paintball.Melodies.delay", 20);
		if (getConfig().get("Paintball.Melodies.win.file") == null) getConfig().set("Paintball.Melodies.win.file", "win");
		if (getConfig().get("Paintball.Melodies.win.nbs") == null) getConfig().set("Paintball.Melodies.win.nbs", false);
		if (getConfig().get("Paintball.Melodies.defeat.file") == null) getConfig().set("Paintball.Melodies.defeat.file", "defeat");
		if (getConfig().get("Paintball.Melodies.defeat.nbs") == null) getConfig().set("Paintball.Melodies.defeat.nbs", false);
		if (getConfig().get("Paintball.Melodies.draw.file") == null) getConfig().set("Paintball.Melodies.draw.file", "draw");
		if (getConfig().get("Paintball.Melodies.draw.nbs") == null) getConfig().set("Paintball.Melodies.draw.nbs", false);
		// gifts
		if (getConfig().get("Paintball.Gifts.enabled") == null) getConfig().set("Paintball.Gifts.enabled", true);
		if (getConfig().get("Paintball.Gifts.onSpawnChance") == null) getConfig().set("Paintball.Gifts.onSpawnChance", 5.0);
		if (getConfig().get("Paintball.Gifts.wishes") == null) getConfig().set("Paintball.Gifts.wishes", true);
		if (getConfig().get("Paintball.Gifts.wishes text") == null) getConfig().set("Paintball.Gifts.wishes text", "&cblablubbabc&5, &cAlphaX &5and &cthe server team &5are wishing you a lot of fun!");
		if (getConfig().get("Paintball.Gifts.wishes delay in minutes") == null) getConfig().set("Paintball.Gifts.wishes delay in minutes", 60);
		if (getConfig().get("Paintball.Gifts.gifts") == null) {
			for (Gift g : giftsDef) {
				getConfig().set("Paintball.Gifts.gifts." + giftsDef.indexOf(g) + ".message", g.getMessage());
				getConfig().set("Paintball.Gifts.gifts." + giftsDef.indexOf(g) + ".id", g.getItem(false).getTypeId());
				getConfig().set("Paintball.Gifts.gifts." + giftsDef.indexOf(g) + ".subid", g.getItem(false).getDurability());
				getConfig().set("Paintball.Gifts.gifts." + giftsDef.indexOf(g) + ".amount", g.getItem(false).getAmount());
				getConfig().set("Paintball.Gifts.gifts." + giftsDef.indexOf(g) + ".chance", g.getChance());
			}
		}
		// lobby join
		if (getConfig().get("Paintball.Lobby join.Join Delay.Time in Seconds") == null) getConfig().set("Paintball.Lobby join.Join Delay.Time in Seconds", 5);
		if (getConfig().get("Paintball.Lobby join.Checks.Inventory") == null) getConfig().set("Paintball.Lobby join.Checks.Inventory", false);
		if (getConfig().get("Paintball.Lobby join.Checks.Inventory Save") == null) getConfig().set("Paintball.Lobby join.Checks.Inventory Save", true);
		if (getConfig().get("Paintball.Lobby join.Checks.Gamemode") == null) getConfig().set("Paintball.Lobby join.Checks.Gamemode", false);
		if (getConfig().get("Paintball.Lobby join.Checks.Creative-Fly-Mode") == null) getConfig().set("Paintball.Lobby join.Checks.Creative-Fly-Mode", false);
		if (getConfig().get("Paintball.Lobby join.Checks.Burning, Falling, Immersion") == null) getConfig().set("Paintball.Lobby join.Checks.Burning, Falling, Immersion", true);
		if (getConfig().get("Paintball.Lobby join.Checks.Health") == null) getConfig().set("Paintball.Lobby join.Checks.Health", false);
		if (getConfig().get("Paintball.Lobby join.Checks.FoodLevel") == null) getConfig().set("Paintball.Lobby join.Checks.FoodLevel", false);
		if (getConfig().get("Paintball.Lobby join.Checks.Effects") == null) getConfig().set("Paintball.Lobby join.Checks.Effects", false);

		if (getConfig().get("Paintball.Match.Damage.FallDamage") == null) getConfig().set("Paintball.Match.Damage.FallDamage", false);
		if (getConfig().get("Paintball.Match.Damage.Other Damage") == null) getConfig().set("Paintball.Match.Damage.Other Damage", true);
		if (getConfig().get("Paintball.Match.Allow Melee") == null) getConfig().set("Paintball.Match.Allow Melee", true);
		if (getConfig().get("Paintball.Match.Melee Damage") == null) getConfig().set("Paintball.Match.Melee Damage", 1);
		if (getConfig().get("Paintball.Match.Lives") == null) getConfig().set("Paintball.Match.Lives", 1);
		if (getConfig().get("Paintball.Match.Respawns") == null) getConfig().set("Paintball.Match.Respawns", 3);
		if (getConfig().get("Paintball.Match.Balls") == null) getConfig().set("Paintball.Match.Balls", 50);
		if (getConfig().get("Paintball.Match.Minimum players") == null) getConfig().set("Paintball.Match.Minimum players", 2);
		if (getConfig().get("Paintball.Match.Maximum players") == null) getConfig().set("Paintball.Match.Maximum players", 1000);
		if (getConfig().get("Paintball.Match.Countdown.Time") == null) getConfig().set("Paintball.Match.Countdown.Time", 20);
		if (getConfig().get("Paintball.Match.Countdown.Delay") == null) getConfig().set("Paintball.Match.Countdown.Delay", 10);
		if (getConfig().get("Paintball.Match.Countdown Round Start.Time") == null) getConfig().set("Paintball.Match.Countdown Round Start.Time", 5);
		if (getConfig().get("Paintball.Match.Round Timer.Time (at least 30)") == null) getConfig().set("Paintball.Match.Round Timer.Time (at least 30)", 180);
		if (getConfig().get("Paintball.Match.Spawn Protection Seconds") == null) getConfig().set("Paintball.Match.Spawn Protection Seconds", 3);

		// This node is also used inside the ArenaManager, so if changed -> also change there!
		if (getConfig().get("Paintball.Arena.Disabled Arenas") == null) getConfig().set("Paintball.Arena.Disabled Arenas", new ArrayList<String>());

		if (getConfig().get("Paintball.Extras.Grenade.enabled") == null) getConfig().set("Paintball.Extras.Grenade.enabled", true);
		if (getConfig().get("Paintball.Extras.Grenade.Explosion-Time-Radius in Ticks") == null) getConfig().set("Paintball.Extras.Grenade.Explosion-Time-Radius in Ticks", 60);
		if (getConfig().get("Paintball.Extras.Grenade.Speed multi") == null) getConfig().set("Paintball.Extras.Grenade.Speed multi", 1.5);
		if (getConfig().get("Paintball.Extras.Grenade.Amount") == null) getConfig().set("Paintball.Extras.Grenade.Amount", 0);
		if (getConfig().get("Paintball.Extras.Grenade.Shrapnel Speed") == null) getConfig().set("Paintball.Extras.Grenade.Shrapnel Speed", 2.0);

		if (getConfig().get("Paintball.Extras.Grenade Mark 2.enabled") == null) getConfig().set("Paintball.Extras.Grenade Mark 2.enabled", true);
		if (getConfig().get("Paintball.Extras.Grenade Mark 2.Explosion-Time-Radius in Ticks") == null) getConfig().set("Paintball.Extras.Grenade Mark 2.Explosion-Time-Radius in Ticks", 60);
		if (getConfig().get("Paintball.Extras.Grenade Mark 2.Speed multi") == null) getConfig().set("Paintball.Extras.Grenade Mark 2.Speed multi", 1.5);
		if (getConfig().get("Paintball.Extras.Grenade Mark 2.Seconds Until Explosion") == null) getConfig().set("Paintball.Extras.Grenade Mark 2.Seconds Until Explosion", 2);
		if (getConfig().get("Paintball.Extras.Grenade Mark 2.Shrapnel Speed") == null) getConfig().set("Paintball.Extras.Grenade Mark 2.Shrapnel Speed", 2.0);

		if (getConfig().get("Paintball.Extras.Flashbang.enabled") == null) getConfig().set("Paintball.Extras.Flashbang.enabled", true);
		if (getConfig().get("Paintball.Extras.Flashbang.Speed multi") == null) getConfig().set("Paintball.Extras.Flashbang.Speed multi", 1.5);
		if (getConfig().get("Paintball.Extras.Flashbang.Flash Range") == null) getConfig().set("Paintball.Extras.Flashbang.Flash Range", 5.5);
		if (getConfig().get("Paintball.Extras.Flashbang.Blindness Duration in Seconds") == null) getConfig().set("Paintball.Extras.Flashbang.Blindness Duration in Seconds", 7);
		if (getConfig().get("Paintball.Extras.Flashbang.Confusion Duration in Seconds") == null) getConfig().set("Paintball.Extras.Flashbang.Confusion Duration in Seconds", 8);
		if (getConfig().get("Paintball.Extras.Flashbang.Slowness Duration in Seconds") == null) getConfig().set("Paintball.Extras.Flashbang.Slowness Duration in Seconds", 2);
		if (getConfig().get("Paintball.Extras.Flashbang.Seconds Until Explosion") == null) getConfig().set("Paintball.Extras.Flashbang.Seconds Until Explosion", 2);

		if (getConfig().get("Paintball.Extras.Concussion Nade.enabled") == null) getConfig().set("Paintball.Extras.Concussion Nade.enabled", true);
		if (getConfig().get("Paintball.Extras.Concussion Nade.Speed multi") == null) getConfig().set("Paintball.Extras.Concussion Nade.Speed multi", 1.5);
		if (getConfig().get("Paintball.Extras.Concussion Nade.Concussion Range") == null) getConfig().set("Paintball.Extras.Concussion Nade.Concussion Range", 7.0);
		if (getConfig().get("Paintball.Extras.Concussion Nade.Blindness Duration in Seconds") == null) getConfig().set("Paintball.Extras.Concussion Nade.Blindness Duration in Seconds", 0);
		if (getConfig().get("Paintball.Extras.Concussion Nade.Confusion Duration in Seconds") == null) getConfig().set("Paintball.Extras.Concussion Nade.Confusion Duration in Seconds", 12);
		if (getConfig().get("Paintball.Extras.Concussion Nade.Slowness Duration in Seconds") == null) getConfig().set("Paintball.Extras.Concussion Nade.Slowness Duration in Seconds", 10);
		if (getConfig().get("Paintball.Extras.Concussion Nade.Seconds Until Explosion") == null) getConfig().set("Paintball.Extras.Concussion Nade.Seconds Until Explosion", 2);

		if (getConfig().get("Paintball.Extras.Airstrike.enabled") == null) getConfig().set("Paintball.Extras.Airstrike.enabled", true);
		if (getConfig().get("Paintball.Extras.Airstrike.Height") == null) getConfig().set("Paintball.Extras.Airstrike.Height", 15);
		if (getConfig().get("Paintball.Extras.Airstrike.Range (half)") == null) getConfig().set("Paintball.Extras.Airstrike.Range (half)", 30);
		if (getConfig().get("Paintball.Extras.Airstrike.Bombs") == null) getConfig().set("Paintball.Extras.Airstrike.Bombs", 15);
		if (getConfig().get("Paintball.Extras.Airstrike.Amount") == null) getConfig().set("Paintball.Extras.Airstrike.Amount", 0);
		if (getConfig().get("Paintball.Extras.Airstrike.Match Limit") == null) getConfig().set("Paintball.Extras.Airstrike.Match Limit", 3);
		if (getConfig().get("Paintball.Extras.Airstrike.Player Limit") == null) getConfig().set("Paintball.Extras.Airstrike.Player Limit", 1);

		if (getConfig().get("Paintball.Extras.Turret.enabled") == null) getConfig().set("Paintball.Extras.Turret.enabled", true);
		if (getConfig().get("Paintball.Extras.Turret.angleMin (min -90)") == null) getConfig().set("Paintball.Extras.Turret.angleMin (min -90)", -45);
		if (getConfig().get("Paintball.Extras.Turret.angleMax (max 90)") == null) getConfig().set("Paintball.Extras.Turret.angleMax (max 90)", 45);
		if (getConfig().get("Paintball.Extras.Turret.calculated ticks") == null) getConfig().set("Paintball.Extras.Turret.calculated ticks", 100);
		if (getConfig().get("Paintball.Extras.Turret.calculated range x") == null) getConfig().set("Paintball.Extras.Turret.calculated range x", 100);
		if (getConfig().get("Paintball.Extras.Turret.calculated range y (half)") == null) getConfig().set("Paintball.Extras.Turret.calculated range y (half)", 50);
		if (getConfig().get("Paintball.Extras.Turret.shots per salve") == null) getConfig().set("Paintball.Extras.Turret.shots per salve", 10);
		if (getConfig().get("Paintball.Extras.Turret.cooldown in seconds") == null) getConfig().set("Paintball.Extras.Turret.cooldown in seconds", 2);
		if (getConfig().get("Paintball.Extras.Turret.lives") == null) getConfig().set("Paintball.Extras.Turret.lives", 10);
		if (getConfig().get("Paintball.Extras.Turret.Match Limit") == null) getConfig().set("Paintball.Extras.Turret.Match Limit", 15);
		if (getConfig().get("Paintball.Extras.Turret.Player Limit") == null) getConfig().set("Paintball.Extras.Turret.Player Limit", 3);

		if (getConfig().get("Paintball.Extras.Rocket.enabled") == null) getConfig().set("Paintball.Extras.Rocket.enabled", true);
		if (getConfig().get("Paintball.Extras.Rocket.Range in Seconds") == null) getConfig().set("Paintball.Extras.Rocket.Range in Seconds", 4);
		if (getConfig().get("Paintball.Extras.Rocket.Speed Multi") == null) getConfig().set("Paintball.Extras.Rocket.Speed Multi", 1.5);
		if (getConfig().get("Paintball.Extras.Rocket.Explosion-Time-Radius in Ticks") == null) getConfig().set("Paintball.Extras.Rocket.Explosion-Time-Radius in Ticks", 60);
		if (getConfig().get("Paintball.Extras.Rocket.Match Limit") == null) getConfig().set("Paintball.Extras.Rocket.Match Limit", 100);
		if (getConfig().get("Paintball.Extras.Rocket.Player Limit") == null) getConfig().set("Paintball.Extras.Rocket.Player Limit", 25);

		if (getConfig().get("Paintball.Extras.Mine.enabled") == null) getConfig().set("Paintball.Extras.Mine.enabled", true);
		if (getConfig().get("Paintball.Extras.Mine.Check for Hallways") == null) getConfig().set("Paintball.Extras.Mine.Check for Hallways", true);
		if (getConfig().get("Paintball.Extras.Mine.Range") == null) getConfig().set("Paintball.Extras.Mine.Range", 4.0);
		if (getConfig().get("Paintball.Extras.Mine.Explosion-Time-Radius in Ticks") == null) getConfig().set("Paintball.Extras.Mine.Explosion-Time-Radius in Ticks", 60);
		if (getConfig().get("Paintball.Extras.Mine.Match Limit") == null) getConfig().set("Paintball.Extras.Mine.Match Limit", 50);
		if (getConfig().get("Paintball.Extras.Mine.Player Limit") == null) getConfig().set("Paintball.Extras.Mine.Player Limit", 10);

		if (getConfig().get("Paintball.Extras.Shotgun.enabled") == null) getConfig().set("Paintball.Extras.Shotgun.enabled", true);
		if (getConfig().get("Paintball.Extras.Shotgun.Angle1") == null) getConfig().set("Paintball.Extras.Shotgun.Angle1", 5);
		if (getConfig().get("Paintball.Extras.Shotgun.Angle2") == null) getConfig().set("Paintball.Extras.Shotgun.Angle2", 10);
		if (getConfig().get("Paintball.Extras.Shotgun.AngleVertical") == null) getConfig().set("Paintball.Extras.Shotgun.AngleVertical", 0);
		if (getConfig().get("Paintball.Extras.Shotgun.Speedmulti") == null) getConfig().set("Paintball.Extras.Shotgun.Speedmulti", 1.3);
		if (getConfig().get("Paintball.Extras.Shotgun.Needed Ammo") == null) getConfig().set("Paintball.Extras.Shotgun.Needed Ammo", 5);

		if (getConfig().get("Paintball.Extras.Pumpgun.enabled") == null) getConfig().set("Paintball.Extras.Pumpgun.enabled", true);
		if (getConfig().get("Paintball.Extras.Pumpgun.Bullets") == null) getConfig().set("Paintball.Extras.Pumpgun.Bullets", 10);
		if (getConfig().get("Paintball.Extras.Pumpgun.Spray (higher number means less spray)") == null) getConfig().set("Paintball.Extras.Pumpgun.Spray (higher number means less spray)", 3.5);
		if (getConfig().get("Paintball.Extras.Pumpgun.Speedmulti") == null) getConfig().set("Paintball.Extras.Pumpgun.Speedmulti", 1.3);
		if (getConfig().get("Paintball.Extras.Pumpgun.Needed Ammo") == null) getConfig().set("Paintball.Extras.Pumpgun.Needed Ammo", 10);

		if (getConfig().get("Paintball.Extras.Sniper.enabled") == null) getConfig().set("Paintball.Extras.Sniper.enabled", true);
		if (getConfig().get("Paintball.Extras.Sniper.Speedmulti") == null) getConfig().set("Paintball.Extras.Sniper.Speedmulti", 4.0);
		if (getConfig().get("Paintball.Extras.Sniper.Only useable if zooming") == null) getConfig().set("Paintball.Extras.Sniper.Only useable if zooming", false);
		if (getConfig().get("Paintball.Extras.Sniper.Remove speed potion effect on zoom") == null) getConfig().set("Paintball.Extras.Sniper.Remove speed potion effect on zoom", true);
		if (getConfig().get("Paintball.Extras.Sniper.No gravity on bullets") == null) getConfig().set("Paintball.Extras.Sniper.No gravity on bullets", false);
		if (getConfig().get("Paintball.Extras.Sniper.No gravity duration") == null) getConfig().set("Paintball.Extras.Sniper.No gravity duration", 3);

		if (getConfig().get("Paintball.Extras.Orbitalstrike.enabled") == null) getConfig().set("Paintball.Extras.Orbitalstrike.enabled", true);
		if (getConfig().get("Paintball.Extras.Orbitalstrike.Match Limit") == null) getConfig().set("Paintball.Extras.Orbitalstrike.Match Limit", 3);
		if (getConfig().get("Paintball.Extras.Orbitalstrike.Player Limit") == null) getConfig().set("Paintball.Extras.Orbitalstrike.Player Limit", 1);

		if (getConfig().get("Paintball.Shop.enabled") == null) getConfig().set("Paintball.Shop.enabled", true);
		if (getConfig().get("Paintball.Shop.Close Inventory Menu On Purchase") == null) getConfig().set("Paintball.Shop.Close Inventory Menu On Purchase", false);
		if (getConfig().get("Paintball.Shop.Admins bypass rank restrictions") == null) getConfig().set("Paintball.Shop.Admins bypass rank restrictions", false);
		if (getConfig().get("Paintball.Shop.Goods (amount-name-id-subid-price-rank)") == null) getConfig().set("Paintball.Shop.Goods (amount-name-id-subid-price-rank)", goodsDef);
		saveConfig();

		// server
		versioncheck = getConfig().getBoolean("Server.Version Check", true);
		metrics = getConfig().getBoolean("Server.Metrics", true);

		// uuid conversion:
		uuidOnlineMode = getConfig().getBoolean("Paintball.UUID Conversion.Online Mode");
		uuidUseUUIDCollector = getConfig().getBoolean("Paintball.UUID Conversion.Use UUIDCollector data");
		uuidUseLocalPlayerData = getConfig().getBoolean("Paintball.UUID Conversion.Use local player data");

		// points+cash:
		pointsPerKill = getConfig().getInt("Paintball.Points per Kill", 2);
		pointsPerHit = getConfig().getInt("Paintball.Points per Hit", 1);
		pointsPerTeamattack = getConfig().getInt("Paintball.Points per Team-Attack", -1);
		pointsPerWin = getConfig().getInt("Paintball.Points per Win", 5);
		pointsPerRound = getConfig().getInt("Paintball.Points per Round", 1);
		cashPerKill = getConfig().getInt("Paintball.Cash per Kill", 10);
		cashPerHit = getConfig().getInt("Paintball.Cash per Hit", 0);
		cashPerWin = getConfig().getInt("Paintball.Cash per Win", 10);
		cashPerRound = getConfig().getInt("Paintball.Cash per Round", 0);

		// gerneral:
		falldamage = getConfig().getBoolean("Paintball.Match.Damage.FallDamage", false);
		otherDamage = getConfig().getBoolean("Paintball.Match.Damage.Other Damage", true);
		allowMelee = getConfig().getBoolean("Paintball.Match.Allow Melee", true);
		meleeDamage = getConfig().getInt("Paintball.Match.Melee Damage", 5);
		if (meleeDamage < 1) meleeDamage = 1;

		// language
		local = getConfig().getString("Paintball.Language.File Name", "enUS");
		languageFileEncoding = getConfig().getString("Paintball.Language.File Encoding", "utf-8");

		noPerms = getConfig().getBoolean("Paintball.No Permissions", false);
		debug = getConfig().getBoolean("Paintball.Debug", false);

		vote = getConfig().getBoolean("Paintball.VoteListener.enabled", true);
		voteCash = getConfig().getInt("Paintball.VoteListener.Cash", 100);

		// vault rewards:
		vaultRewardsEnabled = getConfig().getBoolean("Paintball.Vault Rewards.enabled", false);
		vaultRewardKill = getConfig().getDouble("Paintball.Vault Rewards.Money per Kill", 5.0);
		vaultRewardHit = getConfig().getDouble("Paintball.Vault Rewards.Money per Hit", 1.0);
		vaultRewardWin = getConfig().getDouble("Paintball.Vault Rewards.Money per Win", 20.0);
		vaultRewardRound = getConfig().getDouble("Paintball.Vault Rewards.Money per Round", 0.0);

		commandSignEnabled = getConfig().getBoolean("Paintball.Command Signs.enabled", true);
		commandSignIdentifier = getConfig().getString("Paintball.Command Signs.Command Sign Identifier", "[Paintball]");
		commandSignIgnoreShopDisabled = getConfig().getBoolean("Paintball.Command Signs.Ignore Shop Disabled", true);

		teleportFix = getConfig().getBoolean("Paintball.Teleport Fix", true);
		blockCommandTeleports = getConfig().getBoolean("Paintball.Block teleports by commands", true);
		useXPBar = getConfig().getBoolean("Paintball.Use XP Bar", true);
		autoLobby = getConfig().getBoolean("Paintball.Auto Lobby", false);
		autoTeam = getConfig().getBoolean("Paintball.Auto Team", false);
		allowedCommands = (ArrayList<String>) getConfig().getList("Paintball.Allowed Commands", allowedCommands);
		checkBlacklist = getConfig().getBoolean("Paintball.Blacklist.Enabled", false);
		blacklistAdminOverride = getConfig().getBoolean("Paintball.Blacklist.Admin Override", false);
		blacklistedCommands = (ArrayList<String>) getConfig().getList("Paintball.Blacklist.Commands", blacklistedCommands);

		blacklistedCommandsRegex = new ArrayList<String>();
		for (String black : blacklistedCommands) {
			String[] split = black.split(" ");
			if (split.length == 0) continue;
			String regex = Pattern.quote(split[0]);
			for (int i = 1; i < split.length; i++) {
				String s = split[i];
				if (s.equals("{args}")) {
					regex += " \\S*";
				} else if (s.equals("{player}")) {
					regex += " {player}";
				} else {
					regex += Pattern.quote(" " + s);
				}
			}
			blacklistedCommandsRegex.add(regex);
		}

		afkDetection = getConfig().getBoolean("Paintball.AFK Detection.enabled", true);
		afkMatchAmount = getConfig().getInt("Paintball.AFK Detection.Amount of Matches", 3);
		if (afkMatchAmount < 1) afkMatchAmount = 1;
		afkRadius = getConfig().getInt("Paintball.AFK Detection.Movement Radius around Spawn (keep in mind: knockbacks, pushing, waterflows, falling, etc)", 5);
		if (afkRadius < 1) afkRadius = 1;
		afkRadius2 = afkRadius * afkRadius;

		lives = getConfig().getInt("Paintball.Match.Lives", 1);
		if (lives < 1) lives = 1;
		respawns = getConfig().getInt("Paintball.Match.Respawns", 3);
		if (respawns < -1) respawns = -1;
		balls = getConfig().getInt("Paintball.Match.Balls", 50);
		if (balls < -1) balls = -1;
		minPlayers = getConfig().getInt("Paintball.Match.Minimum players", 2);
		if (minPlayers < 2) minPlayers = 2;
		maxPlayers = getConfig().getInt("Paintball.Match.Maximum players", 1000);
		if (maxPlayers < 2) maxPlayers = 2;

		// countdown:
		countdown = getConfig().getInt("Paintball.Match.Countdown.Time", 20);
		if (countdown < 0) countdown = 0;
		countdownInit = getConfig().getInt("Paintball.Match.Countdown.Delay", 10);
		if (countdownInit < 0) countdownInit = 0;

		countdownStart = getConfig().getInt("Paintball.Match.Countdown Round Start.Time", 5);
		if (countdownStart < 0) countdownStart = 0;

		roundTimer = getConfig().getInt("Paintball.Match.Round Timer.Time (at least 30)", 180);
		if (roundTimer < 30) roundTimer = 30;
		// spawn protection
		protectionTime = getConfig().getInt("Paintball.Match.Spawn Protection Seconds", 3);
		if (protectionTime < 0) protectionTime = 0;

		speedmulti = getConfig().getDouble("Paintball.Ball speed multi", 1.5);
		listnames = getConfig().getBoolean("Paintball.Colored listnames", true);
		// chat colors
		chatMessageColor = getConfig().getBoolean("Paintball.Chat.Colored Message", true);
		chatNameColor = getConfig().getBoolean("Paintball.Chat.Colored Name By Displayname Replacing", false);
		chatFeaturesOnlyForPaintballersVisible = getConfig().getBoolean("Paintball.Chat.Changes only visible for Paintballers", true);
		chatReplaceFormat = getConfig().getBoolean("Paintball.Chat.Format.Use alternate chat format", false);
		chatFormat = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Paintball.Chat.Format.Alternate chat format", "&6%s&7: &f%s"));

		// ranks
		ranksChatPrefix = getConfig().getBoolean("Paintball.Ranks.Chat Prefix.enabled", true);
		ranksLobbyArmor = getConfig().getBoolean("Paintball.Ranks.Lobby Armor", true);

		// scoreboards
		scoreboardLobby = getConfig().getBoolean("Paintball.Scoreboards.Lobby", true);
		scoreboardMatch = getConfig().getBoolean("Paintball.Scoreboards.Match", true);

		// arena selection
		arenaRotationRandom = getConfig().getBoolean("Paintball.Arena Rotation.Random Rotation", true);
		// arena voting:
		arenaVoting = getConfig().getBoolean("Paintball.Arena Rotation.Arena Voting.enabled", true);
		// at least 2:
		arenaVotingOptions = Math.max(2, getConfig().getInt("Paintball.Arena Rotation.Arena Voting.Number of Vote Options (at least 2)", 4));
		arenaVotingRandomOption = getConfig().getBoolean("Paintball.Arena Rotation.Arena Voting.Random Arena Option", true);
		arenaVotingBroadcastOptionsAtCountdownTime = getConfig().getInt("Paintball.Arena Rotation.Arena Voting.Broadcast Options again at Countdown Time", 15);
		arenaVotingEndAtCountdownTime = getConfig().getInt("Paintball.Arena Rotation.Arena Voting.End Voting at Countdown Time", 5);

		onlyRandom = getConfig().getBoolean("Paintball.Only Random", false);
		autoRandom = getConfig().getBoolean("Paintball.Auto Random", true);
		worldMode = getConfig().getBoolean("Paintball.World Mode.enabled", false);
		worldModeWorlds = (List<String>) getConfig().getList("Paintball.World Mode.worlds", Arrays.asList("world", "paintball"));
		autoSpecLobby = getConfig().getBoolean("Paintball.Auto Spec Lobby", false);
		effects = getConfig().getBoolean("Paintball.Effects", true);

		// player tags
		tags = getConfig().getBoolean("Paintball.Tags.enabled", true);
		tagsColor = getConfig().getBoolean("Paintball.Tags.colored", true);
		tagsInvis = getConfig().getBoolean("Paintball.Tags.invisible", true);
		tagsRemainingInvis = getConfig().getBoolean("Paintball.Tags.remaining invisible", true);

		// melody
		melody = getConfig().getBoolean("Paintball.Melodies.enable", true);
		melodyDelay = getConfig().getInt("Paintball.Melodies.delay", 20);
		melodyWin = getConfig().getString("Paintball.Melodies.win.file", "win");
		melodyDefeat = getConfig().getString("Paintball.Melodies.defeat.file", "defeat");
		melodyDraw = getConfig().getString("Paintball.Melodies.draw.file", "draw");
		boolean winNbs = getConfig().getBoolean("Paintball.Melodies.win.nbs", false);
		boolean defeatNbs = getConfig().getBoolean("Paintball.Melodies.defeat.nbs", false);
		boolean drawNbs = getConfig().getBoolean("Paintball.Melodies.draw.nbs", false);

		// gifts
		giftsEnabled = getConfig().getBoolean("Paintball.Gifts.enabled", true);
		giftOnSpawnChance = getConfig().getDouble("Paintball.Gifts.onSpawnChance", 5.0);
		giftOnSpawnChance = (giftOnSpawnChance < 0.0 ? 0.0 : giftOnSpawnChance);
		giftOnSpawnChance = (giftOnSpawnChance > 100.0 ? 100.0 : giftOnSpawnChance);
		bWishes = getConfig().getBoolean("Paintball.Gifts.wishes", true);
		wishes = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Paintball.Gifts.wishes text", "&cblablubbabc&5, &cAlphaX &5and &cthe server team &5are wishing you lot of fun!"));
		wishesDelay = getConfig().getInt("Paintball.Gifts.wishes delay in minutes", 60);
		wishesDelay = (wishesDelay < 0 ? 0 : wishesDelay);
		gifts = new ArrayList<Gift>();

		ConfigurationSection giftsEntries = getConfig().getConfigurationSection("Paintball.Gifts.gifts");
		int allChances = 0;
		for (String key : giftsEntries.getKeys(false)) {
			int id = giftsEntries.getConfigurationSection(key).getInt("id", 0);
			id = (id < 0 ? 0 : id);
			int subI = giftsEntries.getConfigurationSection(key).getInt("subid", 0);
			subI = (subI < 0 ? 0 : subI);
			short sub = (subI > Short.MAX_VALUE ? Short.MAX_VALUE : (short) subI);
			int amount = giftsEntries.getConfigurationSection(key).getInt("amount", 0);
			amount = (amount < 0 ? 0 : amount);
			double chance = giftsEntries.getConfigurationSection(key).getDouble("chance", 0.0);
			chance = (chance < 0.0 ? 0.0 : chance);
			chance = (chance > 100.0 ? 100.0 : chance);
			allChances += chance;
			String message = giftsEntries.getConfigurationSection(key).getString("message", "Have fun with this!");
			gifts.add(new Gift(id, sub, amount, chance, message));
		}
		giftChanceFactor = (100 / allChances);

		// shop:
		shop = getConfig().getBoolean("Paintball.Shop.enabled", true);
		shopCloseMenuOnPurchase = getConfig().getBoolean("Paintball.Shop.Close Inventory Menu On Purchase", false);
		ranksAdminBypassShop = getConfig().getBoolean("Paintball.Shop.Admins bypass rank restrictions", false);
		shopGoods = (ArrayList<String>) getConfig().getList("Paintball.Shop.Goods (amount-name-id-subid-price-rank)", goodsDef);

		// disabled arenas
		disabledArenas = (List<String>) getConfig().getList("Paintball.Arena.Disabled Arenas", new ArrayList<String>());

		// lobby join checks
		joinDelaySeconds = getConfig().getInt("Paintball.Lobby join.Join Delay.Time in Seconds", 5);
		checkInventory = getConfig().getBoolean("Paintball.Lobby join.Checks.Inventory", true);
		saveInventory = getConfig().getBoolean("Paintball.Lobby join.Checks.Inventory Save", true);
		checkGamemode = getConfig().getBoolean("Paintball.Lobby join.Checks.Gamemode", true);
		checkFlymode = getConfig().getBoolean("Paintball.Lobby join.Checks.Creative-Fly-Mode", true);
		checkBurning = getConfig().getBoolean("Paintball.Lobby join.Checks.Burning, Falling, Immersion", true);
		checkHealth = getConfig().getBoolean("Paintball.Lobby join.Checks.Health", true);
		checkFood = getConfig().getBoolean("Paintball.Lobby join.Checks.FoodLevel", true);
		checkEffects = getConfig().getBoolean("Paintball.Lobby join.Checks.Effects", true);

		// Extras
		grenade = getConfig().getBoolean("Paintball.Extras.Grenade.enabled", true);
		grenadeTime = getConfig().getInt("Paintball.Extras.Grenade.Explosion-Time-Radius in Ticks", 60);
		if (grenadeTime < 1) grenadeTime = 1;
		grenadeSpeed = getConfig().getDouble("Paintball.Extras.Grenade.Speed multi", 1.5);
		grenadeAmount = getConfig().getInt("Paintball.Extras.Grenade.Amount", 0);
		if (grenadeAmount < -1) grenadeAmount = -1;
		grenadeShrapnelSpeed = getConfig().getDouble("Paintball.Extras.Grenade.Shrapnel Speed", 2.0);

		grenade2 = getConfig().getBoolean("Paintball.Extras.Grenade Mark 2.enabled", true);
		grenade2Time = getConfig().getInt("Paintball.Extras.Grenade Mark 2.Explosion-Time-Radius in Ticks", 60);
		if (grenade2Time < 1) grenade2Time = 1;
		grenade2Speed = getConfig().getDouble("Paintball.Extras.Grenade Mark 2.Speed multi", 1.5);
		grenade2TimeUntilExplosion = getConfig().getInt("Paintball.Extras.Grenade Mark 2.Seconds Until Explosion", 2);
		grenade2ShrapnelSpeed = getConfig().getDouble("Paintball.Extras.Grenade Mark 2.Shrapnel Speed", 2.0);

		flashbang = getConfig().getBoolean("Paintball.Extras.Flashbang.enabled", true);
		flashbangSpeed = getConfig().getDouble("Paintball.Extras.Flashbang.Speed multi", 1.5);
		flashRange = getConfig().getDouble("Paintball.Extras.Flashbang.Flash Range", 5.5);
		flashBlindnessDuration = getConfig().getInt("Paintball.Extras.Flashbang.Blindness Duration in Seconds", 7);
		flashConfusionDuration = getConfig().getInt("Paintball.Extras.Flashbang.Confusion Duration in Seconds", 8);
		flashSlownessDuration = getConfig().getInt("Paintball.Extras.Flashbang.Slowness Duration in Seconds", 2);
		flashbangTimeUntilExplosion = getConfig().getInt("Paintball.Extras.Flashbang.Seconds Until Explosion", 2);

		concussion = getConfig().getBoolean("Paintball.Extras.Concussion Nade.enabled", true);
		concussionSpeed = getConfig().getDouble("Paintball.Extras.Concussion Nade.Speed multi", 1.5);
		concussionRange = getConfig().getDouble("Paintball.Extras.Concussion Nade.Flash Range", 7.0);
		concussionBlindnessDuration = getConfig().getInt("Paintball.Extras.Concussion Nade.Blindness Duration in Seconds", 0);
		concussionConfusionDuration = getConfig().getInt("Paintball.Extras.Concussion Nade.Confusion Duration in Seconds", 12);
		concussionSlownessDuration = getConfig().getInt("Paintball.Extras.Concussion Nade.Slowness Duration in Seconds", 10);
		concussionTimeUntilExplosion = getConfig().getInt("Paintball.Extras.Concussion Nade.Seconds Until Explosion", 2);

		airstrike = getConfig().getBoolean("Paintball.Extras.Airstrike.enabled", true);
		airstrikeHeight = getConfig().getInt("Paintball.Extras.Airstrike.Height", 15);
		if (airstrikeHeight < 2) airstrikeHeight = 2;
		airstrikeRange = getConfig().getInt("Paintball.Extras.Airstrike.Range (half)", 30);
		airstrikeBombs = getConfig().getInt("Paintball.Extras.Airstrike.Bombs", 15);
		if (airstrikeBombs < 0) airstrikeBombs = 0;
		airstrikeAmount = getConfig().getInt("Paintball.Extras.Airstrike.Amount", 0);
		if (airstrikeAmount < -1) airstrikeAmount = -1;
		airstrikeMatchLimit = getConfig().getInt("Paintball.Extras.Airstrike.Match Limit", 3);
		if (airstrikeMatchLimit < 0) airstrikeMatchLimit = 0;
		airstrikePlayerLimit = getConfig().getInt("Paintball.Extras.Airstrike.Player Limit", 1);
		if (airstrikePlayerLimit < 0) airstrikePlayerLimit = 0;

		turret = getConfig().getBoolean("Paintball.Extras.Turret.enabled", true);
		turretAngleMin = getConfig().getInt("Paintball.Extras.Turret.angleMin (min -90)", -45);
		if (turretAngleMin < -90) turretAngleMin = -90;
		if (turretAngleMin > 90) turretAngleMin = 90;
		turretAngleMax = getConfig().getInt("Paintball.Extras.Turret.angleMax (max 90)", 45);
		if (turretAngleMax < -90) turretAngleMax = -90;
		if (turretAngleMax > 90) turretAngleMax = 90;
		turretTicks = getConfig().getInt("Paintball.Extras.Turret.calculated ticks", 100);
		if (turretTicks < 0) turretTicks = 0;
		turretXSize = getConfig().getInt("Paintball.Extras.Turret.calculated range x", 100);
		if (turretXSize < 0) turretXSize = 0;
		turretYSize = getConfig().getInt("Paintball.Extras.Turret.calculated range y (half)", 50);
		if (turretYSize < 0) turretYSize = 0;
		turretSalve = getConfig().getInt("Paintball.Extras.Turret.shots per salve", 15);
		if (turretSalve < 0) turretSalve = 0;
		turretCooldown = getConfig().getInt("Paintball.Extras.Turret.cooldown in seconds", 3);
		if (turretCooldown < 0) turretCooldown = 0;
		turretLives = getConfig().getInt("Paintball.Extras.Turret.lives", 10);
		if (turretLives < 0) turretLives = 0;
		turretMatchLimit = getConfig().getInt("Paintball.Extras.Turret.Match Limit", 15);
		if (turretMatchLimit < 0) turretMatchLimit = 0;
		turretPlayerLimit = getConfig().getInt("Paintball.Extras.Turret.Player Limit", 3);
		if (turretPlayerLimit < 0) turretPlayerLimit = 0;

		rocket = getConfig().getBoolean("Paintball.Extras.Rocket.enabled", true);
		rocketRange = getConfig().getInt("Paintball.Extras.Rocket.Range in Seconds", 4);
		if (rocketRange < 0) rocketRange = 0;
		rocketSpeedMulti = getConfig().getDouble("Paintball.Extras.Rocket.Speed Multi", 1.5);
		rocketTime = getConfig().getInt("Paintball.Extras.Rocket.Explosion-Time-Radius in Ticks", 60);
		if (rocketTime < 1) rocketTime = 1;
		rocketMatchLimit = getConfig().getInt("Paintball.Extras.Rocket.Match Limit", 100);
		if (rocketMatchLimit < 0) rocketMatchLimit = 0;
		rocketPlayerLimit = getConfig().getInt("Paintball.Extras.Rocket.Player Limit", 20);
		if (rocketPlayerLimit < 0) rocketPlayerLimit = 0;

		mine = getConfig().getBoolean("Paintball.Extras.Mine.enabled", true);
		mineCheckForHallway = getConfig().getBoolean("Paintball.Extras.Mine.Check for Hallways", true);
		mineRange = getConfig().getDouble("Paintball.Extras.Mine.Range", 4.0);
		if (mineRange < 0) mineRange = 0;
		mineTime = getConfig().getInt("Paintball.Extras.Mine.Explosion-Time-Radius in Ticks", 60);
		if (mineTime < 1) mineTime = 1;
		mineMatchLimit = getConfig().getInt("Paintball.Extras.Mine.Match Limit", 50);
		if (mineMatchLimit < 0) mineMatchLimit = 0;
		minePlayerLimit = getConfig().getInt("Paintball.Extras.Mine.Player Limit", 10);
		if (minePlayerLimit < 0) minePlayerLimit = 0;

		shotgun = getConfig().getBoolean("Paintball.Extras.Shotgun.enabled", true);
		shotgunAngle1 = getConfig().getInt("Paintball.Extras.Shotgun.Angle1", 5);
		shotgunAngle2 = getConfig().getInt("Paintball.Extras.Shotgun.Angle2", 10);
		shotgunAngleVert = getConfig().getInt("Paintball.Extras.Shotgun.AngleVertical", 3);
		shotgunSpeedmulti = getConfig().getDouble("Paintball.Extras.Shotgun.Speedmulti", 1.3);
		shotgunAmmo = getConfig().getInt("Paintball.Extras.Shotgun.Needed Ammo", 5);
		if (shotgunAmmo < 0) shotgunAmmo = 0;

		pumpgun = getConfig().getBoolean("Paintball.Extras.Pumpgun.enabled", true);
		pumpgunBullets = getConfig().getInt("Paintball.Extras.Pumpgun.Bullets", 15);
		if (pumpgunBullets < 0) pumpgunBullets = 0;
		pumpgunSpray = getConfig().getDouble("Paintball.Extras.Pumpgun.Spray (higher number means less spray)", 2.7);
		pumpgunSpeedmulti = getConfig().getDouble("Paintball.Extras.Pumpgun.Speedmulti", 1.3);
		pumpgunAmmo = getConfig().getInt("Paintball.Extras.Pumpgun.Needed Ammo", 5);
		if (pumpgunAmmo < 0) pumpgunAmmo = 0;

		sniper = getConfig().getBoolean("Paintball.Extras.Sniper.enabled", true);
		sniperSpeedmulti = getConfig().getDouble("Paintball.Extras.Sniper.Speedmulti", 4.0);
		sniperOnlyUseIfZooming = getConfig().getBoolean("Paintball.Extras.Sniper.Only useable if zooming", false);
		sniperRemoveSpeed = getConfig().getBoolean("Paintball.Extras.Sniper.Remove speed potion effect on zoom", true);
		sniperNoGravity = getConfig().getBoolean("Paintball.Extras.Sniper.No gravity on bullets", false);
		sniperNoGravityDuration = getConfig().getInt("Paintball.Extras.Sniper.No gravity duration", 3);
		if (sniperNoGravityDuration < 1) sniperNoGravityDuration = 1;

		orbitalstrike = getConfig().getBoolean("Paintball.Extras.Orbitalstrike.enabled", true);
		orbitalstrikeMatchLimit = getConfig().getInt("Paintball.Extras.Orbitalstrike.Match Limit", 3);
		orbitalstrikePlayerLimit = getConfig().getInt("Paintball.Extras.Orbitalstrike.Player Limit", 1);

		// SQLite database:
		sql = new BlaSQLite(this);
		if (sql.aborted) {
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		// DB
		loadLobbySpawnsFromDB();
		// TRANSLATOR
		translator = new Translator(this, local);
		if (!Translator.success) {
			Log.severe("Couldn't find/load the default language file. Disables now..", true);
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		// MELODIES
		musik = new Musician(this, melodyWin, winNbs, melodyDefeat, defeatNbs, melodyDraw, drawNbs);
		if (!musik.success) {
			Log.severe("Couldn't find/load the default melodies. Disables now..", true);
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		// INIT STATICS
		Utils.init();
		// Log is already init above
		// Translator will be init below
		Sounds.init();

		// init enums:
		Instrument.values();
		Lobby.values();
		ArenaStat.values();
		ArenaSetting.values();
		GeneralStat.values();
		TDMMatchStat.values();
		PlayerStat.values();

		// WEAPON MANAGER
		weaponManager = new WeaponManager();
		// INIT WEAPONS
		weaponManager.initWeaponHandlers();
		// RANKMANAGER
		rankManager = new RankManager(new File(this.getDataFolder().getPath() + File.separator + "ranks.yml"));
		// SHOP MANAGER
		shopManager = new ShopManager(this);
		// COMMANDS
		commandManager = new CommandManager(this);
		CommandExecutor ce = commandManager;
		getCommand("pb").setExecutor(ce);
		// COMMAND SIGNS LISTENER
		commandSignListener = new CommandSignsListener(this);
		// PLAYERMANAGER
		playerManager = new PlayerManager();
		// Newsfeeder
		feeder = new Newsfeeder(this);
		// Listener
		listener = new EventListener(this);
		// ARENAMANAGER
		arenaManager = new ArenaManager(this);
		// MATCHMANAGER
		matchManager = new MatchManager(this);
		// STATS
		statsManager = new Stats(this);
		getServer().getPluginManager().registerEvents(listener, this);
		new TeleportManager();

		active = true;
		happyhour = false;
		softreload = false;
		lobbyspawn = 0;
		afkMatchCount = new HashMap<String, Integer>();

		// autoLobby -> now done by PlayerManager during addAllOnlinePlayers
		/*
		 * if(autoLobby) {
		 * for(Player player : getServer().getOnlinePlayers()) {
		 * if(autoTeam) {
		 * commandManager.joinTeam(player, false, Lobby.RANDOM);
		 * } else {
		 * commandManager.joinLobbyPre(player, false, null);
		 * }
		 * }
		 * }
		 */

		// METRICS
		if (this.metrics) {
			try {
				Metrics metrics = new Metrics(this);
				// Custom Data:

				// Default graph:
				// Actual playing players (Lobby)
				Graph defaultGraph = metrics.createGraph("Default");

				defaultGraph.addPlotter(new Metrics.Plotter("Actual playing (lobby)") {

					@Override
					public int getValue() {
						try {
							return Lobby.LOBBY.number();
						} catch (Exception e) {
							// Failed to get the value :(
							return 0;
						}
					}
				});

				// Maximum playing (lobby) since last update
				defaultGraph.addPlotter(new Metrics.Plotter("Maximum playing (lobby) since last update") {

					@Override
					public int getValue() {
						try {
							// get max:
							int max = Lobby.maxPlayersInLobby();
							// reset max:
							Lobby.resetMaxPlayersInLobby();
							return max;
						} catch (Exception e) {
							// Failed to get the value :(
							// reset max:
							Lobby.resetMaxPlayersInLobby();
							return 0;
						}
					}
				});

				// Graph 2
				Graph graph = metrics.createGraph("Players ever played Paintball");

				// Players ever played Paintball Plotter
				graph.addPlotter(new Metrics.Plotter("Ever played Paintball") {

					@Override
					public int getValue() {
						try {
							return playerManager.getPlayersEverPlayedCount();
						} catch (Exception e) {
							// Failed to get the value :(
							return 0;
						}
					}
				});

				metrics.start();
			} catch (IOException e) {
				Lobby.resetMaxPlayersInLobby();
				// Failed to submit the stats :-(
			}
		} else {
			Log.info("--------- MCStats Tracking ----------");
			Log.info("You denied stats tracking via metrics. :(");
			Log.info("If you want to help me out to measure how many people are using paintball");
			Log.info("and to give me confirmation that this plugin is worth it's development");
			Log.info("-> enable it in the config with the setting 'metrics'. Thanks!");
			Log.info("--------- ---------------- ----------");
		}

		// InSigns sign changer:
		Plugin insignsPlugin = getServer().getPluginManager().getPlugin("InSigns");
		if ((insignsPlugin != null) && insignsPlugin.isEnabled()) {
			insignsFeature = new InSignsFeature(insignsPlugin, this);
			Log.info("Plugin 'InSigns' found. Using it now.");
		} else {
			Log.info("Plugin 'InSigns' not found. Additional sign features disabled.");
		}
		// TagAPI:
		if (tags) {
			Plugin tagAPIPlugin = getServer().getPluginManager().getPlugin("TagAPI");
			if ((tagAPIPlugin != null) && tagAPIPlugin.isEnabled()) {
				tagAPI = new TagAPIListener(this);
				getServer().getPluginManager().registerEvents(tagAPI, this);
				Log.info("Plugin 'TagAPI' found. Using it now.");
			} else {
				Log.info("Plugin 'TagAPI' not found. Additional tag features disabled.");
			}
		}
		// Votifier VoteListener:
		if (vote) {
			Plugin votifierPlugin = getServer().getPluginManager().getPlugin("Votifier");
			if ((votifierPlugin != null) && votifierPlugin.isEnabled()) {
				voteListener = new VoteListener();
				getServer().getPluginManager().registerEvents(voteListener, this);
				Log.info("Plugin 'Votifier' found. Using it now.");
			} else {
				Log.info("Plugin 'Votifier' not found. Additional vote features disabled.");
			}
		}
		// Vault:
		if (vaultRewardsEnabled) {
			Plugin vaultPlugin = getServer().getPluginManager().getPlugin("Vault");
			if (vaultPlugin != null && vaultPlugin.isEnabled()) {
				vaultRewardsFeature = new VaultRewardsFeature(vaultPlugin);
				if (vaultRewardsFeature.isEconomyDetected()) {
					Log.info("Plugin 'Vault' found and economy detected. Using it now.");
				} else {
					vaultRewardsFeature = null;
					Log.info("Plugin 'Vault' found but now economy detected. Additional vault-economy-reward features disabled.");
				}
			} else {
				Log.info("Plugin 'Vault' not found. Additional vault-economy-reward features disabled.");
			}
		}

		// after all plugins are enabled:
		Bukkit.getScheduler().runTaskLaterAsynchronously(this, new Runnable() {

			@Override
			public void run() {
				// SERVERLISTER CONFIG:
				serverList = new Serverlister(Paintball.this);

				// check for updates:
				if (Paintball.this.versioncheck) {
					Updater updater = new Updater(Paintball.this, 41489, Paintball.this.getFile(), UpdateType.NO_DOWNLOAD, true);
					Updater.UpdateResult result = updater.getResult(); // this freezes until the result is available
					Log.info("--------- Checking version ----------");
					switch (result)
					{
					case SUCCESS:
						// Success: The updater found an update, and has readied it to be loaded the next time the
						// server restarts/reloads
						Log.info("A new version of paintball was found and downloaded: " + updater.getLatestName() + "(" + updater.getLatestType() + ")", true);
						Log.info("It should be loaded the next time the server restarts/reloads.");
						break;
					case NO_UPDATE:
						// No Update: The updater did not find an update, and nothing was downloaded.
						Log.info("You are running the latest version (or a dev- or compat-build). :)");
						break;
					case DISABLED:
						// Won't Update: The updater was disabled in its configuration file.
						Log.info("You denied version checking.");
						Log.info("If you want to be informed about a new version of paintball");
						Log.info("-> enable it in plugins/Updater/config.yml");
						break;
					case FAIL_DOWNLOAD:
						// Download Failed: The updater found an update, but was unable to download it.
						Log.warning("Download failed. :(");
						break;
					case FAIL_DBO:
						// dev.bukkit.org Failed: For some reason, the updater was unable to contact DBO to download the
						// file.
						Log.info("Couldn't currently connect to DBO. :(");
						break;
					case FAIL_NOVERSION:
						// No version found: When running the version check, the file on DBO did not contain the a
						// version in the format 'vVersion' such as 'v1.0'.
						Log.warning("Bad version string format. Nag blablubbabc about this.. :/");
						break;
					case FAIL_BADID:
						// Bad id: The id provided by the plugin running the updater was invalid and doesn't exist on
						// DBO.
						Log.warning("Bad project id. Nag blablubbabc about this.. :/");
						break;
					case FAIL_APIKEY:
						// Bad API key: The user provided an invalid API key for the updater to use.
						Log.warning("Bad API key. Check your updater configuration.");
						break;
					case UPDATE_AVAILABLE:
						// There was an update found, but because you had the UpdateType set to NO_DOWNLOAD, it was not
						// downloaded.
						Paintball.this.needsUpdate = true;
						Log.info("There is a new version of paintball available: " + updater.getLatestName() + " (" + updater.getLatestType().name() + ")", true);
						Log.info("Download at the bukkit dev page.");
					}
					Log.info("--------- ---------------- ----------");
				} else {
					Log.info("--------- Checking version ----------");
					Log.info("You denied version checking. :(");
					Log.info("If you want to be informed about a new version of paintball");
					Log.info("-> enable it in the config.");
					Log.info("--------- ---------------- ----------");
				}

				Bukkit.getScheduler().runTaskLater(Paintball.this, new Runnable() {

					@Override
					public void run() {
						Log.printInfo();
						// stop logging of warnings:
						Log.logWarnings(false);
					}
				}, 20L);
			}
		}, 1L);

		Log.info("By blablubbabc enabled.");

	}

	public void onDisable() {
		currentlyDisableing = true;
		if (!sql.aborted) {
			matchManager.forceReload();
		}

		// wait for async tasks to complete:
		final long start = System.currentTimeMillis();
		while (this.isAsyncTaskRunning()) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// wait max 5 seconds then disable anyways..:
			if (System.currentTimeMillis() - start > 5000) {
				Log.warning("Waited over 5 seconds for " + this.getAsyncTasksCount()
						+ " remaining async tasks to complete. Disabling Paintball now anyways..");
				break;
			}
		}
		long timeWaited = System.currentTimeMillis() - start;
		if (timeWaited > 0) Log.info("Waited " + timeWaited + "ms for async (probably stats saving) tasks to finish.");

		sql.closeConnection();
		Bukkit.getScheduler().cancelTasks(this);
		Log.info("Disabled!");
		currentlyDisableing = false;
		mainThread = null;
		instance = null;
	}

	public void reload(CommandSender sender) {
		reloadConfig();
		getServer().getPluginManager().disablePlugin(this);
		getServer().getPluginManager().enablePlugin(this);
		if (sender != null) sender.sendMessage(Translator.getString("REALOAD_FINISHED"));
	}

	// METHODS LOBBYSPAWNS
	private void loadLobbySpawnsFromDB() {
		lobbyspawns = new ArrayList<Location>();
		for (Location loc : sql.sqlArenaLobby.getLobbyspawns()) {
			lobbyspawns.add(loc);
		}
	}

	public void addLobbySpawn(Location loc) {
		lobbyspawns.add(loc);
		sql.sqlArenaLobby.addLobbyspawn(loc);
	}

	public void deleteLobbySpawns() {
		sql.sqlArenaLobby.removeLobbyspawns();
		lobbyspawns = new LinkedList<Location>();
	}

	public int getLobbyspawnsCount() {
		return lobbyspawns.size();
	}

	public Location getNextLobbySpawn() {
		lobbyspawn++;
		if (lobbyspawn > (lobbyspawns.size() - 1)) lobbyspawn = 0;
		return (lobbyspawns.size() > 0 ? lobbyspawns.get(lobbyspawn) : null);
	}

	// //////////////////////////////////
	// UTILS
	// //////////////////////////////////

	// vault reward feature:
	public void givePlayerVaultMoneyInstant(String playerName, double moneyToAdd) {
		if (vaultRewardsEnabled && vaultRewardsFeature != null) vaultRewardsFeature.givePlayerMoneyInstant(playerName, moneyToAdd);
	}

	/*
	 * public void givePlayerVaultMoneyAfterSession(String playerName, double moneyToAdd) {
	 * if (vaultRewardsEnabled && vaultRewardsFeature != null) vaultRewardsFeature.givePlayerMoneyAfterSession(playerName, moneyToAdd);
	 * }
	 */

	public void afkRemove(String player) {
		synchronized (afkMatchCount) {
			afkMatchCount.remove(player);
		}
	}

	public int afkGet(String player) {
		synchronized (afkMatchCount) {
			int amount = 0;
			if (afkMatchCount.get(player) != null) amount = afkMatchCount.get(player);
			return amount;
		}
	}

	public void afkSet(String player, int amount) {
		synchronized (afkMatchCount) {
			afkMatchCount.put(player, amount);
		}
	}

	public List<String> afkGetEntries() {
		synchronized (afkMatchCount) {
			List<String> entries = new ArrayList<String>(afkMatchCount.keySet());
			return entries;
		}
	}

}