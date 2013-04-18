package me.blablubbabc.paintball;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import me.blablubbabc.BlaDB.BlaSQLite;
import me.blablubbabc.paintball.Metrics.Graph;
import me.blablubbabc.paintball.extras.Ball;
import me.blablubbabc.paintball.extras.NoGravity;
import me.blablubbabc.paintball.extras.Pumpgun;
import me.blablubbabc.paintball.extras.Rocket;
import me.blablubbabc.paintball.extras.Turret;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

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
public class Paintball extends JavaPlugin{
	public static Paintball instance;
	
	public PlayerManager pm;
	public CommandManager cm;
	public MatchManager mm;
	public EventListener listener;
	public TagAPIListener tagAPI;
	public Newsfeeder nf;
	public ArenaManager am;
	public Translator t;
	public Musiker musik;
	public Christmas christmas;
	public Stats stats;
	public Serverlister slist;
	public InSignsFeature isf;
	public boolean active;
	public boolean happyhour;
	public boolean softreload;
	public boolean nometrics = false;

	//LOBBYSPAWNS
	public int lobbyspawn;
	private LinkedList<Location> lobbyspawns;

	//Public afk detection
	public HashMap <String, Integer> afkMatchCount;

	//ChatColors
	public ChatColor gray = ChatColor.GRAY;
	public ChatColor gold = ChatColor.GOLD;
	public ChatColor green = ChatColor.GREEN;
	public ChatColor aqua = ChatColor.AQUA;
	public ChatColor red = ChatColor.RED;
	public ChatColor blue = ChatColor.BLUE;
	public ChatColor yellow = ChatColor.YELLOW;
	public ChatColor light_purple = ChatColor.LIGHT_PURPLE;
	public ChatColor dark_green = ChatColor.DARK_GREEN;
	public ChatColor dark_red = ChatColor.DARK_RED;
	public ChatColor white = ChatColor.WHITE;

	public ChatColor bold = ChatColor.BOLD;
	public ChatColor italic = ChatColor.ITALIC;
	public ChatColor reset = ChatColor.RESET;
	
	//CONFIG:
	//general:
	public boolean versioncheck;
	public String local;
	public int countdown;
	public int countdownInit;
	public int countdownStart;
	public int roundTimer;
	public int minPlayers;
	public int maxPlayers;
	public int lives;
	public int respawns;
	public int balls;
	public double speedmulti;
	public boolean listnames;
	public boolean chatnames;
	public boolean shop;
	public ArrayList<String> shopGoods;
	public ArrayList<String> allowedCommands;
	public ArrayList<String> blacklistedCommandsRegex;
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
	public boolean afkDetection;
	public int afkRadius;
	public int afkMatchAmount;
	public boolean autoSpecLobby;
	public boolean effects;
	public boolean debug;
	public boolean teleportFix;
	public boolean useXPBar;
	public int protectionTime;
	public List<String> disabledArenas;
	
	//gifts
	public boolean giftsEnabled;
	public double giftOnSpawnChance;
	public ArrayList<Gift> gifts;
	public double giftChanceFactor;
	public boolean bWishes;
	public String wishes;
	public int wishesDelay;
	
	//player tags
	public boolean tags;
	public boolean tagsColor;
	public boolean tagsInvis;
	public boolean tagsRemainingInvis;
	
	//melody
	public boolean melody;
	public int melodyDelay;
	public String melodyWin;
	public String melodyDefeat;
	public String melodyDraw;
	//unused
	//public boolean autoSpecDeadPlayers;

	//lobby join checks
	public boolean checkInventory;
	public boolean checkGamemode;
	public boolean checkFlymode;
	public boolean checkBurning;
	public boolean checkHealth;
	public boolean checkFood;
	public boolean checkEffects;

	//points und cash
	public int pointsPerKill;
	public int pointsPerHit;
	public int pointsPerTeamattack;
	public int pointsPerWin;
	public int pointsPerRound;
	public int cashPerKill;
	public int cashPerHit;
	public int cashPerWin;
	public int cashPerRound;

	//Extras:
	public boolean grenade;
	public int grenadeTime;
	public double grenadeSpeed;
	public int grenadeAmount;

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
	
	
	//TODO
	//shop-items
	//Extras: Luftschlag, verschiedene munition, airdrops, punktesystem abhängig von spieler im match, evt. bester spieler der runde, mehr extras :D
	//schneemann-geschütz
	//Lobby schilder für points und rank und tops
	//map rotation with size-matters
	//map vote
	//map restriction
	//config in-game ändern
	//grenade
	//luftschlag
	//friendly-fire on/off
	//friendly-fire-detection on/off
	//mySQL support für online stats
	//- arena disable/enable
	//- airstrike finalMark bleibt


	//Permissions:
	//paintball.general
	//paintball.arena
	//paintball.admin
	//paintball.shop.not<id> (starting with 1) // removed


	public BlaSQLite sql;
	//public BlaDB data;

	@SuppressWarnings("unchecked")
	public void onEnable(){	
		instance = this;
		//CONFIG
		ArrayList<String> goodsDef = new ArrayList<String>();
		
		goodsDef.add("10-Balls-332-0-10");
		goodsDef.add("50-Balls-332-0-50");
		goodsDef.add("100-Balls-332-0-100");
		goodsDef.add("1-Grenade-344-0-20");
		goodsDef.add("1-Mine-390-0-10");
		goodsDef.add("1-Rocket Launcher-356-0-20");
		goodsDef.add("1-Airstrike-280-0-60");
		goodsDef.add("1-Turret-86-0-180");
		goodsDef.add("1-Speed-373-16482-20");
		goodsDef.add("1-Shotgun-382-0-20");
		goodsDef.add("1-Pumpgun-275-0-20");
		goodsDef.add("1-Sniper-398-0-80");
		
		ArrayList<Gift> giftsDef = new ArrayList<Gift>();
		giftsDef.add(new Gift(332, (short)0, 50, 30.0, "Hope you have luck with these balls!"));
		giftsDef.add(new Gift(344, (short)0, 2, 15.0, "May these grenades be with you!"));
		giftsDef.add(new Gift(390, (short)0, 2, 15.0, "I knew you ever wanted to be a sneaky killer!"));
		giftsDef.add(new Gift(356, (short)0, 2, 15.0, "Heat them with these rocket launchers!"));
		giftsDef.add(new Gift(280, (short)0, 1, 15.0, "I knew you ever wanted to order a airstrike at least once!"));
		giftsDef.add(new Gift(54, (short)0, 2, 5.0, "I got some more gifts for you!"));
		giftsDef.add(new Gift(86, (short)0, 1, 3.0, "They survived the apocalypse? But the will not survive this!"));
		giftsDef.add(new Gift(0, (short)0, 0, 2.0, "You had no luck this time :("));
		
		allowedCommands = new ArrayList<String>();
		allowedCommands.add("/list");
		allowedCommands.add("/login *");
		allowedCommands.add("/register *");
		
		List<String> blacklistedCommands = new ArrayList<String>();
		blacklistedCommands.add("/ptp {player}");
		blacklistedCommands.add("/tp {args} {player}");
		blacklistedCommands.add("/tp {player} {args}");
		blacklistedCommands.add("/tphere {player}");

		getConfig().options().header("Use a value of -1 to give the players infinite balls or extras. If you insert a not possible value/wrong value in a section the plugin will use the default value or the nearest possible value (Example: your value at section balls: -3 -> plugin will use -1). 1 Tick = 1/20 seconds.");
		if(getConfig().get("Server.Version Check") == null)getConfig().set("Server.Version Check", true);
		if(getConfig().get("Paintball.AFK Detection.enabled") == null)getConfig().set("Paintball.AFK Detection.enabled", true);
		if(getConfig().get("Paintball.AFK Detection.Movement Radius around Spawn (keep in mind: knockbacks, pushing, waterflows, falling, etc)") == null)getConfig().set("Paintball.AFK Detection.Movement Radius around Spawn (keep in mind: knockbacks, pushing, waterflows, falling, etc)", 5);
		if(getConfig().get("Paintball.AFK Detection.Amount of Matches") == null)getConfig().set("Paintball.AFK Detection.Amount of Matches", 3);
		if(getConfig().get("Paintball.Language") == null)getConfig().set("Paintball.Language", "enUS");
		if(getConfig().get("Paintball.No Permissions") == null)getConfig().set("Paintball.No Permissions", false);
		if(getConfig().get("Paintball.Debug") == null)getConfig().set("Paintball.Debug", false);
		if(getConfig().get("Paintball.Auto Lobby") == null)getConfig().set("Paintball.Auto Lobby", false);
		if(getConfig().get("Paintball.Auto Team") == null)getConfig().set("Paintball.Auto Team", false);
		if(getConfig().get("Paintball.Points per Kill") == null)getConfig().set("Paintball.Points per Kill", 2);
		if(getConfig().get("Paintball.Points per Hit") == null)getConfig().set("Paintball.Points per Hit", 1);
		if(getConfig().get("Paintball.Points per Team-Attack") == null)getConfig().set("Paintball.Points per Team-Attack", -1);
		if(getConfig().get("Paintball.Points per Win") == null)getConfig().set("Paintball.Points per Win", 5);
		if(getConfig().get("Paintball.Points per Round") == null)getConfig().set("Paintball.Points per Round", 1);
		if(getConfig().get("Paintball.Cash per Kill") == null)getConfig().set("Paintball.Cash per Kill", 10);
		if(getConfig().get("Paintball.Cash per Hit") == null)getConfig().set("Paintball.Cash per Hit", 0);
		if(getConfig().get("Paintball.Cash per Win") == null)getConfig().set("Paintball.Cash per Win", 10);
		if(getConfig().get("Paintball.Cash per Round") == null)getConfig().set("Paintball.Cash per Round", 0);
		if(getConfig().get("Paintball.Ball speed multi") == null)getConfig().set("Paintball.Ball speed multi", 1.7);
		if(getConfig().get("Paintball.Colored listnames") == null)getConfig().set("Paintball.Colored listnames", true);
		if(getConfig().get("Paintball.Colored chatnames") == null)getConfig().set("Paintball.Colored chatnames", true);
		if(getConfig().get("Paintball.Only Random") == null)getConfig().set("Paintball.Only Random", false);
		if(getConfig().get("Paintball.Auto Random") == null)getConfig().set("Paintball.Auto Random", true);
		if(getConfig().get("Paintball.Auto Spec Lobby") == null)getConfig().set("Paintball.Auto Spec Lobby", false);
		if(getConfig().get("Paintball.Effects") == null)getConfig().set("Paintball.Effects", true);
		if(getConfig().get("Paintball.Teleport Fix") == null)getConfig().set("Paintball.Teleport Fix", true);
		if(getConfig().get("Paintball.Use XP Bar") == null)getConfig().set("Paintball.Blacklist.Use XP Bar", true);
		if(getConfig().get("Paintball.Allowed Commands") == null)getConfig().set("Paintball.Allowed Commands", allowedCommands);
		if(getConfig().get("Paintball.Blacklist.Enabled") == null)getConfig().set("Paintball.Blacklist.Enabled", false);
		if(getConfig().get("Paintball.Blacklist.Admin Override") == null)getConfig().set("Paintball.Blacklist.Admin Override", true);
		if(getConfig().get("Paintball.Blacklist.Commands") == null)getConfig().set("Paintball.Blacklist.Commands", blacklistedCommands);
		//player tags
		if(getConfig().get("Paintball.Tags.enabled") == null)getConfig().set("Paintball.Tags.enabled", true);
		if(getConfig().get("Paintball.Tags.colored") == null)getConfig().set("Paintball.Tags.colored", true);
		if(getConfig().get("Paintball.Tags.invisible") == null)getConfig().set("Paintball.Tags.invisible", true);
		if(getConfig().get("Paintball.Tags.remaining invisible") == null)getConfig().set("Paintball.Tags.remaining invisible", true);
		//melody:
		if(getConfig().get("Paintball.Melodies.enable") == null)getConfig().set("Paintball.Melodies.enable", true);
		if(getConfig().get("Paintball.Melodies.delay") == null)getConfig().set("Paintball.Melodies.delay", 20);
		if(getConfig().get("Paintball.Melodies.win.file") == null)getConfig().set("Paintball.Melodies.win.file", "win");
		if(getConfig().get("Paintball.Melodies.win.nbs") == null)getConfig().set("Paintball.Melodies.win.nbs", false);
		if(getConfig().get("Paintball.Melodies.defeat.file") == null)getConfig().set("Paintball.Melodies.defeat.file", "defeat");
		if(getConfig().get("Paintball.Melodies.defeat.nbs") == null)getConfig().set("Paintball.Melodies.defeat.nbs", false);
		if(getConfig().get("Paintball.Melodies.draw.file") == null)getConfig().set("Paintball.Melodies.draw.file", "draw");
		if(getConfig().get("Paintball.Melodies.draw.nbs") == null)getConfig().set("Paintball.Melodies.draw.nbs", false);
		//gifts
		if(getConfig().get("Paintball.Gifts.enabled") == null)getConfig().set("Paintball.Gifts.enabled", true);
		if(getConfig().get("Paintball.Gifts.onSpawnChance") == null)getConfig().set("Paintball.Gifts.onSpawnChance", 5.0);
		if(getConfig().get("Paintball.Gifts.wishes") == null)getConfig().set("Paintball.Gifts.wishes", true);
		if(getConfig().get("Paintball.Gifts.wishes text") == null)getConfig().set("Paintball.Gifts.wishes text", "&cblablubbabc&5, &cAlphaX &5and &cthe server team &5are wishing you a lot of fun!");
		if(getConfig().get("Paintball.Gifts.wishes delay in minutes") == null)getConfig().set("Paintball.Gifts.wishes delay in minutes", 60);
		if(getConfig().get("Paintball.Gifts.gifts") == null) {
			for(Gift g : giftsDef) {
				getConfig().set("Paintball.Gifts.gifts."+giftsDef.indexOf(g)+".message", g.getMessage());
				getConfig().set("Paintball.Gifts.gifts."+giftsDef.indexOf(g)+".id", g.getItem(false).getTypeId());
				getConfig().set("Paintball.Gifts.gifts."+giftsDef.indexOf(g)+".subid", g.getItem(false).getDurability());
				getConfig().set("Paintball.Gifts.gifts."+giftsDef.indexOf(g)+".amount", g.getItem(false).getAmount());
				getConfig().set("Paintball.Gifts.gifts."+giftsDef.indexOf(g)+".chance", g.getChance());
			}
		}
		//lobby join checks
		if(getConfig().get("Paintball.Lobby join.Checks.Inventory") == null)getConfig().set("Paintball.Lobby join.Checks.Inventory", true);
		if(getConfig().get("Paintball.Lobby join.Checks.Inventory Save") == null)getConfig().set("Paintball.Lobby join.Checks.Inventory Save", true);
		if(getConfig().get("Paintball.Lobby join.Checks.Gamemode") == null)getConfig().set("Paintball.Lobby join.Checks.Gamemode", true);
		if(getConfig().get("Paintball.Lobby join.Checks.Creative-Fly-Mode") == null)getConfig().set("Paintball.Lobby join.Checks.Creative-Fly-Mode", true);
		if(getConfig().get("Paintball.Lobby join.Checks.Burning, Falling, Immersion") == null)getConfig().set("Paintball.Lobby join.Checks.Burning, Falling, Immersion", true);
		if(getConfig().get("Paintball.Lobby join.Checks.Health") == null)getConfig().set("Paintball.Lobby join.Checks.Health", true);
		if(getConfig().get("Paintball.Lobby join.Checks.FoodLevel") == null)getConfig().set("Paintball.Lobby join.Checks.FoodLevel", true);
		if(getConfig().get("Paintball.Lobby join.Checks.Effects") == null)getConfig().set("Paintball.Lobby join.Checks.Effects", true);

		if(getConfig().get("Paintball.Match.Damage.FallDamage") == null)getConfig().set("Paintball.Match.Damage.FallDamage", false);
		if(getConfig().get("Paintball.Match.Damage.Other Damage") == null)getConfig().set("Paintball.Match.Damage.Other Damage", true);
		if(getConfig().get("Paintball.Match.Allow Melee") == null)getConfig().set("Paintball.Match.Allow Melee", true);
		if(getConfig().get("Paintball.Match.Melee Damage") == null)getConfig().set("Paintball.Match.Melee Damage", 1);
		if(getConfig().get("Paintball.Match.Lives") == null)getConfig().set("Paintball.Match.Lives", 1);
		if(getConfig().get("Paintball.Match.Respawns") == null)getConfig().set("Paintball.Match.Respawns", 0);
		if(getConfig().get("Paintball.Match.Balls") == null)getConfig().set("Paintball.Match.Balls", 50);
		if(getConfig().get("Paintball.Match.Minimum players") == null)getConfig().set("Paintball.Match.Minimum players", 2);
		if(getConfig().get("Paintball.Match.Maximum players") == null)getConfig().set("Paintball.Match.Maximum players", 1000);
		if(getConfig().get("Paintball.Match.Countdown.Time") == null)getConfig().set("Paintball.Match.Countdown.Time", 20);
		if(getConfig().get("Paintball.Match.Countdown.Delay") == null)getConfig().set("Paintball.Match.Countdown.Delay", 10);
		if(getConfig().get("Paintball.Match.Countdown Round Start.Time") == null)getConfig().set("Paintball.Match.Countdown Round Start.Time", 5);
		if(getConfig().get("Paintball.Match.Round Timer.Time (at least 30)") == null)getConfig().set("Paintball.Match.Round Timer.Time (at least 30)", 120);
		if(getConfig().get("Paintball.Match.Spawn Protection Seconds") == null)getConfig().set("Paintball.Match.Spawn Protection Seconds", 3);
		
		//This node is also used inside the ArenaManager, so if changed -> also change there!
		if(getConfig().get("Paintball.Arena.Disabled Arenas") == null)getConfig().set("Paintball.Arena.Disabled Arenas", new ArrayList<String>());
		
		if(getConfig().get("Paintball.Extras.Grenades.enabled") == null)getConfig().set("Paintball.Extras.Grenades.enabled", true);
		if(getConfig().get("Paintball.Extras.Grenades.Explosion-Time-Radius in Ticks") == null)getConfig().set("Paintball.Extras.Grenades.Explosion-Time-Radius in Ticks", 60);
		if(getConfig().get("Paintball.Extras.Grenades.Speed multi") == null)getConfig().set("Paintball.Extras.Grenades.Speed multi", 1.0);
		if(getConfig().get("Paintball.Extras.Grenades.Amount") == null)getConfig().set("Paintball.Extras.Grenades.Amount", 0);
		
		if(getConfig().get("Paintball.Extras.Airstrike.enabled") == null)getConfig().set("Paintball.Extras.Airstrike.enabled", true);
		if(getConfig().get("Paintball.Extras.Airstrike.Height") == null)getConfig().set("Paintball.Extras.Airstrike.Height", 15);
		if(getConfig().get("Paintball.Extras.Airstrike.Range (half)") == null)getConfig().set("Paintball.Extras.Airstrike.Range (half)", 30);
		if(getConfig().get("Paintball.Extras.Airstrike.Bombs") == null)getConfig().set("Paintball.Extras.Airstrike.Bombs", 15);
		if(getConfig().get("Paintball.Extras.Airstrike.Amount") == null)getConfig().set("Paintball.Extras.Airstrike.Amount", 0);
		if(getConfig().get("Paintball.Extras.Airstrike.Match Limit") == null)getConfig().set("Paintball.Extras.Airstrike.Match Limit", 3);
		if(getConfig().get("Paintball.Extras.Airstrike.Player Limit") == null)getConfig().set("Paintball.Extras.Airstrike.Player Limit", 1);
		
		if(getConfig().get("Paintball.Extras.Turret.enabled") == null)getConfig().set("Paintball.Extras.Turret.enabled", true);
		if(getConfig().get("Paintball.Extras.Turret.angleMin (min -90)") == null)getConfig().set("Paintball.Extras.Turret.angleMin (min -90)", -45);
		if(getConfig().get("Paintball.Extras.Turret.angleMax (max 90)") == null)getConfig().set("Paintball.Extras.Turret.angleMax (max 90)", 45);
		if(getConfig().get("Paintball.Extras.Turret.calculated ticks") == null)getConfig().set("Paintball.Extras.Turret.calculated ticks", 100);
		if(getConfig().get("Paintball.Extras.Turret.calculated range x") == null)getConfig().set("Paintball.Extras.Turret.calculated range x", 100);
		if(getConfig().get("Paintball.Extras.Turret.calculated range y (half)") == null)getConfig().set("Paintball.Extras.Turret.calculated range y (half)", 50);
		if(getConfig().get("Paintball.Extras.Turret.shots per salve") == null)getConfig().set("Paintball.Extras.Turret.shots per salve", 10);
		if(getConfig().get("Paintball.Extras.Turret.cooldown in seconds") == null)getConfig().set("Paintball.Extras.Turret.cooldown in seconds", 2);
		if(getConfig().get("Paintball.Extras.Turret.lives") == null)getConfig().set("Paintball.Extras.Turret.lives", 10);
		if(getConfig().get("Paintball.Extras.Turret.Match Limit") == null)getConfig().set("Paintball.Extras.Turret.Match Limit", 15);
		if(getConfig().get("Paintball.Extras.Turret.Player Limit") == null)getConfig().set("Paintball.Extras.Turret.Player Limit", 3);
		
		if(getConfig().get("Paintball.Extras.Rocket.enabled") == null)getConfig().set("Paintball.Extras.Rocket.enabled", true);
		if(getConfig().get("Paintball.Extras.Rocket.Range in Seconds") == null)getConfig().set("Paintball.Extras.Airstrike.Range in Seconds", 4);
		if(getConfig().get("Paintball.Extras.Rocket.Speed Multi") == null)getConfig().set("Paintball.Extras.Rocket.Speed Multi", 1.5);
		if(getConfig().get("Paintball.Extras.Rocket.Explosion-Time-Radius in Ticks") == null)getConfig().set("Paintball.Extras.Rocket.Explosion-Time-Radius in Ticks", 60);
		if(getConfig().get("Paintball.Extras.Rocket.Match Limit") == null)getConfig().set("Paintball.Extras.Rocket.Match Limit", 100);
		if(getConfig().get("Paintball.Extras.Rocket.Player Limit") == null)getConfig().set("Paintball.Extras.Rocket.Player Limit", 25);
		
		if(getConfig().get("Paintball.Extras.Mine.enabled") == null)getConfig().set("Paintball.Extras.Mine.enabled", true);
		if(getConfig().get("Paintball.Extras.Mine.Range") == null)getConfig().set("Paintball.Extras.Mine.Range", 4.0);
		if(getConfig().get("Paintball.Extras.Mine.Explosion-Time-Radius in Ticks") == null)getConfig().set("Paintball.Extras.Mine.Explosion-Time-Radius in Ticks", 60);
		if(getConfig().get("Paintball.Extras.Mine.Match Limit") == null)getConfig().set("Paintball.Extras.Mine.Match Limit", 50);
		if(getConfig().get("Paintball.Extras.Mine.Player Limit") == null)getConfig().set("Paintball.Extras.Mine.Player Limit", 10);
		
		if(getConfig().get("Paintball.Extras.Shotgun.enabled") == null)getConfig().set("Paintball.Extras.Shotgun.enabled", true);
		if(getConfig().get("Paintball.Extras.Shotgun.Angle1") == null)getConfig().set("Paintball.Extras.Shotgun.Angle1", 5);
		if(getConfig().get("Paintball.Extras.Shotgun.Angle2") == null)getConfig().set("Paintball.Extras.Shotgun.Angle2", 10);
		if(getConfig().get("Paintball.Extras.Shotgun.AngleVertical") == null)getConfig().set("Paintball.Extras.Shotgun.AngleVertical", 3);
		if(getConfig().get("Paintball.Extras.Shotgun.Speedmulti") == null)getConfig().set("Paintball.Extras.Shotgun.Speedmulti", 1.5);
		if(getConfig().get("Paintball.Extras.Shotgun.Needed Ammo") == null)getConfig().set("Paintball.Extras.Shotgun.Needed Ammo", 15);
		
		if(getConfig().get("Paintball.Extras.Pumpgun.enabled") == null)getConfig().set("Paintball.Extras.Pumpgun.enabled", true);
		if(getConfig().get("Paintball.Extras.Pumpgun.Bullets") == null)getConfig().set("Paintball.Extras.Pumpgun.Bullets", 10);
		if(getConfig().get("Paintball.Extras.Pumpgun.Spray (higher number means less spray)") == null)getConfig().set("Paintball.Extras.Pumpgun.Spray (higher number means less spray)", 3.5);
		if(getConfig().get("Paintball.Extras.Pumpgun.Speedmulti") == null)getConfig().set("Paintball.Extras.Pumpgun.Speedmulti", 1.2);
		if(getConfig().get("Paintball.Extras.Pumpgun.Needed Ammo") == null)getConfig().set("Paintball.Extras.Pumpgun.Needed Ammo", 10);
		
		if(getConfig().get("Paintball.Extras.Sniper.enabled") == null)getConfig().set("Paintball.Extras.Sniper.enabled", true);
		if(getConfig().get("Paintball.Extras.Sniper.Speedmulti") == null)getConfig().set("Paintball.Extras.Sniper.Speedmulti", 4.0);
		if(getConfig().get("Paintball.Extras.Sniper.Only useable if zooming") == null)getConfig().set("Paintball.Extras.Sniper.Only useable if zooming", true);
		if(getConfig().get("Paintball.Extras.Sniper.Remove speed potion effect on zoom") == null)getConfig().set("Paintball.Extras.Sniper.Remove speed potion effect on zoom", true);
		if(getConfig().get("Paintball.Extras.Sniper.No gravity on bullets") == null)getConfig().set("Paintball.Extras.Sniper.No gravity on bullets", false);
		if(getConfig().get("Paintball.Extras.Sniper.No gravity duration") == null)getConfig().set("Paintball.Extras.Sniper.No gravity duration", 10);
		if(getConfig().get("Paintball.Shop.enabled") == null)getConfig().set("Paintball.Shop.enabled", true);
		if(getConfig().get("Paintball.Shop.Goods (amount-name-id-subid-price)") == null)getConfig().set("Paintball.Shop.Goods (amount-name-id-subid-price)", goodsDef);
		saveConfig();


		//server
		versioncheck = getConfig().getBoolean("Server.Version Check", true);
		
		//points+cash:
		pointsPerKill = getConfig().getInt("Paintball.Points per Kill", 2);
		pointsPerHit = getConfig().getInt("Paintball.Points per Hit", 1);
		pointsPerTeamattack = getConfig().getInt("Paintball.Points per Team-Attack", -1);
		pointsPerWin = getConfig().getInt("Paintball.Points per Win", 5);
		pointsPerRound = getConfig().getInt("Paintball.Points per Round", 1);
		cashPerKill = getConfig().getInt("Paintball.Cash per Kill", 10);
		cashPerHit = getConfig().getInt("Paintball.Cash per Hit", 0);
		cashPerWin = getConfig().getInt("Paintball.Cash per Win", 10);
		cashPerRound = getConfig().getInt("Paintball.Cash per Round", 0);

		//gerneral:
		falldamage = getConfig().getBoolean("Paintball.Match.Damage.FallDamage", false);
		otherDamage = getConfig().getBoolean("Paintball.Match.Damage.Other Damage", true);
		allowMelee = getConfig().getBoolean("Paintball.Match.Allow Melee", true);
		meleeDamage = getConfig().getInt("Paintball.Match.Melee Damage", 1);
		if(meleeDamage < 1) meleeDamage = 1;
		local = getConfig().getString("Paintball.Language", "enUS");
		noPerms = getConfig().getBoolean("Paintball.No Permissions", false);
		debug = getConfig().getBoolean("Paintball.Debug", false);
		teleportFix = getConfig().getBoolean("Paintball.Teleport Fix", true);
		useXPBar = getConfig().getBoolean("Paintball.Blacklist.Use XP Bar", true);
		autoLobby = getConfig().getBoolean("Paintball.Auto Lobby", false);
		autoTeam = getConfig().getBoolean("Paintball.Auto Team", false);
		allowedCommands = (ArrayList<String>) getConfig().getList("Paintball.Allowed Commands", allowedCommands);
		checkBlacklist = getConfig().getBoolean("Paintball.Blacklist.Enabled", false);
		blacklistAdminOverride = getConfig().getBoolean("Paintball.Blacklist.Admin Override", false);
		blacklistedCommands = (ArrayList<String>) getConfig().getList("Paintball.Blacklist.Commands", blacklistedCommands);
		
		blacklistedCommandsRegex = new ArrayList<String>();
		for(String black : blacklistedCommands) {
			String[] split = black.split(" ");
			if(split.length == 0) continue;
			String regex = Pattern.quote(split[0]);
			for (int i = 1; i < split.length; i++) {
				String s = split[i];
				if(s.equals("{args}")) {
					regex += " \\S*";
				} else if(s.equals("{player}")) {
					regex += " {player}";
				} else {
					regex += Pattern.quote(" "+s);
				}
			}
			blacklistedCommandsRegex.add(regex);
		}
		
		afkDetection = getConfig().getBoolean("Paintball.AFK Detection.enabled", true);
		afkMatchAmount = getConfig().getInt("Paintball.AFK Detection.Amount of Matches", 3);
		if(afkMatchAmount < 1) afkMatchAmount = 1;
		afkRadius = getConfig().getInt("Paintball.AFK Detection.Movement Radius around Spawn (keep in mind: knockbacks, pushing, waterflows, falling, etc)", 5);
		if(afkRadius < 1) afkRadius = 1;

		lives = getConfig().getInt("Paintball.Match.Lives", 1);
		if(lives < 1) lives = 1;
		respawns = getConfig().getInt("Paintball.Match.Respawns", 0);
		if(respawns < -1) respawns = -1;
		balls = getConfig().getInt("Paintball.Match.Balls", 50);
		if(balls < -1) balls = -1;
		minPlayers = getConfig().getInt("Paintball.Match.Minimum players", 2);
		if(minPlayers < 2) minPlayers = 2;
		maxPlayers = getConfig().getInt("Paintball.Match.Maximum players", 1000);
		if(maxPlayers < 2) maxPlayers = 2;
		//countdown:
		countdown = getConfig().getInt("Paintball.Match.Countdown.Time", 20);
		if(countdown < 0) countdown = 0;
		countdownInit = getConfig().getInt("Paintball.Match.Countdown.Delay", 10);
		if(countdownInit < 0) countdownInit = 0;
		countdownStart = getConfig().getInt("Paintball.Match.Countdown Round Start.Time", 5);
		if(countdownStart < 0) countdownStart = 0;
		roundTimer = getConfig().getInt("Paintball.Match.Round Timer.Time (at least 30)", 120);
		if(roundTimer < 30) roundTimer = 30;
		//spawn protection
		protectionTime = getConfig().getInt("Paintball.Match.Spawn Protection Seconds", 3);
		if(protectionTime < 0) protectionTime = 0;
		

		speedmulti = getConfig().getDouble("Paintball.Ball speed multi", 1.5);
		listnames = getConfig().getBoolean("Paintball.Colored listnames", true);
		chatnames = getConfig().getBoolean("Paintball.Colored chatnames", true);
		onlyRandom = getConfig().getBoolean("Paintball.Only Random", false);
		autoRandom = getConfig().getBoolean("Paintball.Auto Random", true);
		autoSpecLobby = getConfig().getBoolean("Paintball.Auto Spec Lobby", false);
		effects = getConfig().getBoolean("Paintball.Effects", true);
		
		//player tags
		tags = getConfig().getBoolean("Paintball.Tags.enabled", true);
		tagsColor = getConfig().getBoolean("Paintball.Tags.colored", true);
		tagsInvis = getConfig().getBoolean("Paintball.Tags.invisible", true);
		tagsRemainingInvis = getConfig().getBoolean("Paintball.Tags.remaining invisible", true);

		//melody
		melody = getConfig().getBoolean("Paintball.Melodies.enable", true);
		melodyDelay = getConfig().getInt("Paintball.Melodies.delay", 20);
		melodyWin = getConfig().getString("Paintball.Melodies.win.file", "win");
		melodyDefeat = getConfig().getString("Paintball.Melodies.defeat.file", "defeat");
		melodyDraw = getConfig().getString("Paintball.Melodies.draw.file", "draw");
		boolean winNbs = getConfig().getBoolean("Paintball.Melodies.win.nbs", false);
		boolean defeatNbs = getConfig().getBoolean("Paintball.Melodies.defeat.nbs", false);
		boolean drawNbs = getConfig().getBoolean("Paintball.Melodies.draw.nbs", false);
		
		//gifts
		giftsEnabled = getConfig().getBoolean("Paintball.Gifts.enabled", true);
		giftOnSpawnChance = getConfig().getDouble("Paintball.Gifts.onSpawnChance", 5.0);
		giftOnSpawnChance = (giftOnSpawnChance < 0.0 ? 0.0 : giftOnSpawnChance);
		giftOnSpawnChance = (giftOnSpawnChance > 100.0 ? 100.0 : giftOnSpawnChance);
		bWishes = getConfig().getBoolean("Paintball.Gifts.wishes", true);
		wishes = getConfig().getString("Paintball.Gifts.wishes text", "&cblablubbabc&5, &cAlphaX &5and &cthe server team &5are wishing you a merry christmas and a happy new year!");
		wishesDelay = getConfig().getInt("Paintball.Gifts.wishes delay in minutes", 60);
		wishesDelay = (wishesDelay < 0 ? 0 : wishesDelay);
		gifts = new ArrayList<Gift>();
		
		ConfigurationSection giftsEntries = getConfig().getConfigurationSection("Paintball.Gifts.gifts");
		int allChances = 0;
		for(String key : giftsEntries.getKeys(false)) {
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
		giftChanceFactor = (100/allChances);
		
		//shop:
		shop = getConfig().getBoolean("Paintball.Shop.enabled", true);
		shopGoods = (ArrayList<String>) getConfig().getList("Paintball.Shop.Goods (amount-name-id-subid-price)", goodsDef);

		//disabled arenas
		disabledArenas = (List<String>) getConfig().getList("Paintball.Arena.Disabled Arenas", new ArrayList<String>());
		
		//lobby join checks
		checkInventory = getConfig().getBoolean("Paintball.Lobby join.Checks.Inventory", true);
		saveInventory = getConfig().getBoolean("Paintball.Lobby join.Checks.Inventory Save", true);
		checkGamemode = getConfig().getBoolean("Paintball.Lobby join.Checks.Gamemode", true);
		checkFlymode = getConfig().getBoolean("Paintball.Lobby join.Checks.Creative-Fly-Mode", true);
		checkBurning = getConfig().getBoolean("Paintball.Lobby join.Checks.Burning, Falling, Immersion", true);
		checkHealth = getConfig().getBoolean("Paintball.Lobby join.Checks.Health", true);
		checkFood = getConfig().getBoolean("Paintball.Lobby join.Checks.FoodLevel", true);
		checkEffects = getConfig().getBoolean("Paintball.Lobby join.Checks.Effects", true);

		//Extras
		grenade = getConfig().getBoolean("Paintball.Extras.Grenades.enabled", true);
		grenadeTime = getConfig().getInt("Paintball.Extras.Grenades.Explosion-Time-Radius in Ticks", 60);
		if(grenadeTime < 1) grenadeTime = 1;
		grenadeSpeed = getConfig().getDouble("Paintball.Extras.Grenades.Speed multi", 1.0);
		grenadeAmount = getConfig().getInt("Paintball.Extras.Grenades.Amount", 0);
		if(grenadeAmount < -1) grenadeAmount = -1;

		airstrike = getConfig().getBoolean("Paintball.Extras.Airstrike.enabled", true);
		airstrikeHeight = getConfig().getInt("Paintball.Extras.Airstrike.Height", 15);
		if(airstrikeHeight < 2) airstrikeHeight = 2;
		airstrikeRange = getConfig().getInt("Paintball.Extras.Airstrike.Range (half)", 30);
		airstrikeBombs = getConfig().getInt("Paintball.Extras.Airstrike.Bombs", 15);
		if(airstrikeBombs < 0) airstrikeBombs = 0;
		airstrikeAmount = getConfig().getInt("Paintball.Extras.Airstrike.Amount", 0);
		if(airstrikeAmount < -1) airstrikeAmount = -1;
		airstrikeMatchLimit = getConfig().getInt("Paintball.Extras.Airstrike.Match Limit", 3);
		if(airstrikeMatchLimit < 0) airstrikeMatchLimit = 0;
		airstrikePlayerLimit = getConfig().getInt("Paintball.Extras.Airstrike.Player Limit", 1);
		if(airstrikePlayerLimit < 0) airstrikePlayerLimit = 0;
		
		turret = getConfig().getBoolean("Paintball.Extras.Turret.enabled", true);
		turretAngleMin = getConfig().getInt("Paintball.Extras.Turret.angleMin (min -90)", -45);
		if(turretAngleMin < -90) turretAngleMin = -90;
		if(turretAngleMin > 90) turretAngleMin = 90;
		turretAngleMax = getConfig().getInt("Paintball.Extras.Turret.angleMax (max 90)", 45);
		if(turretAngleMax < -90) turretAngleMax = -90;
		if(turretAngleMax > 90) turretAngleMax = 90;
		turretTicks = getConfig().getInt("Paintball.Extras.Turret.calculated ticks", 100);
		if(turretTicks < 0) turretTicks = 0;
		turretXSize = getConfig().getInt("Paintball.Extras.Turret.calculated range x", 100);
		if(turretXSize < 0) turretXSize = 0;
		turretYSize = getConfig().getInt("Paintball.Extras.Turret.calculated range y (half)", 50);
		if(turretYSize < 0) turretYSize = 0;
		turretSalve = getConfig().getInt("Paintball.Extras.Turret.shots per salve", 15);
		if(turretSalve < 0) turretSalve = 0;
		turretCooldown = getConfig().getInt("Paintball.Extras.Turret.cooldown in seconds", 3);
		if(turretCooldown < 0) turretCooldown = 0;
		turretLives = getConfig().getInt("Paintball.Extras.Turret.lives", 10);
		if(turretLives < 0) turretLives = 0;
		turretMatchLimit = getConfig().getInt("Paintball.Extras.Turret.Match Limit", 15);
		if(turretMatchLimit < 0) turretMatchLimit = 0;
		turretPlayerLimit = getConfig().getInt("Paintball.Extras.Turret.Player Limit", 3);
		if(turretPlayerLimit < 0) turretPlayerLimit = 0;
		
		rocket = getConfig().getBoolean("Paintball.Extras.Rocket.enabled", true);
		rocketRange = getConfig().getInt("Paintball.Extras.Rocket.Range in Seconds", 4);
		if(rocketRange < 0) rocketRange = 0;
		rocketSpeedMulti = getConfig().getDouble("Paintball.Extras.Rocket.Speed Multi", 1.5);
		rocketTime = getConfig().getInt("Paintball.Extras.Rocket.Explosion-Time-Radius in Ticks", 60);
		if(rocketTime < 1) rocketTime = 1;
		rocketMatchLimit = getConfig().getInt("Paintball.Extras.Rocket.Match Limit", 100);
		if(rocketMatchLimit < 0) rocketMatchLimit = 0;
		rocketPlayerLimit = getConfig().getInt("Paintball.Extras.Rocket.Player Limit", 20);
		if(rocketPlayerLimit < 0) rocketPlayerLimit = 0;
		
		mine = getConfig().getBoolean("Paintball.Extras.Mine.enabled", true);
		mineRange = getConfig().getDouble("Paintball.Extras.Mine.Range", 4.0);
		if(mineRange < 0) mineRange = 0;
		mineTime = getConfig().getInt("Paintball.Extras.Mine.Explosion-Time-Radius in Ticks", 60);
		if(mineTime < 1) mineTime = 1;
		mineMatchLimit = getConfig().getInt("Paintball.Extras.Mine.Match Limit", 50);
		if(mineMatchLimit < 0) mineMatchLimit = 0;
		minePlayerLimit = getConfig().getInt("Paintball.Extras.Mine.Player Limit", 10);
		if(minePlayerLimit < 0) minePlayerLimit = 0;
		
		shotgun = getConfig().getBoolean("Paintball.Extras.Shotgun.enabled", true);
		shotgunAngle1 = getConfig().getInt("Paintball.Extras.Shotgun.Angle1", 5);
		shotgunAngle2 = getConfig().getInt("Paintball.Extras.Shotgun.Angle2", 10);
		shotgunAngleVert = getConfig().getInt("Paintball.Extras.Shotgun.AngleVertical", 3);
		shotgunSpeedmulti = getConfig().getDouble("Paintball.Extras.Shotgun.Speedmulti", 1.5);
		shotgunAmmo = getConfig().getInt("Paintball.Extras.Shotgun.Needed Ammo", 5);
		if(shotgunAmmo < 0) shotgunAmmo = 0;
		
		pumpgun = getConfig().getBoolean("Paintball.Extras.Pumpgun.enabled", true);
		pumpgunBullets = getConfig().getInt("Paintball.Extras.Pumpgun.Bullets", 15);
		if(pumpgunBullets < 0) pumpgunBullets = 0;
		pumpgunSpray = getConfig().getDouble("Paintball.Extras.Pumpgun.Spray (higher number means less spray)", 2.7);
		pumpgunSpeedmulti = getConfig().getDouble("Paintball.Extras.Pumpgun.Speedmulti", 1.5);
		pumpgunAmmo = getConfig().getInt("Paintball.Extras.Pumpgun.Needed Ammo", 5);
		if(pumpgunAmmo < 0) pumpgunAmmo = 0;
		
		
		sniper = getConfig().getBoolean("Paintball.Extras.Sniper.enabled", true);
		sniperSpeedmulti = getConfig().getDouble("Paintball.Extras.Sniper.Speedmulti", 4.0);
		sniperOnlyUseIfZooming = getConfig().getBoolean("Paintball.Extras.Sniper.Only useable if zooming", true);
		sniperRemoveSpeed = getConfig().getBoolean("Paintball.Extras.Sniper.Remove speed potion effect on zoom", true);
		sniperNoGravity = getConfig().getBoolean("Paintball.Extras.Sniper.No gravity on bullets", false);
		sniperNoGravityDuration = getConfig().getInt("Paintball.Extras.Sniper.No gravity duration", 10);
		if (sniperNoGravityDuration < 1) sniperNoGravityDuration = 1;
		
		

		//SQLite with version: 110
		sql = new BlaSQLite(new File(this.getDataFolder().toString()+"/"+"pbdata_110"+".db"), this);
		//DB
		loadDB();
		//TRANSLATOR
		t = new Translator(this, local);
		if(!t.success) {
			log("ERROR: Couldn't find/load the default language file. Disables now..");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		//MELODIES
		musik = new Musiker(this, melodyWin, winNbs, melodyDefeat, defeatNbs, melodyDraw, drawNbs);
		if(!musik.success) {
			log("ERROR: Couldn't find/load the default melodies. Disables now..");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		//SERVERLISTER CONFIG:
		slist = new Serverlister();
		//WAKE TEAM-ENUMS
		Lobby.values();
		//PLAYERMANAGER
		pm = new PlayerManager(this);
		//Newsfeeder
		nf = new Newsfeeder(this);
		//MATCHMANAGER|LISTENER
		mm = new MatchManager(this);
		listener = new EventListener(this);
		//ARENAMANAGER
		am = new ArenaManager(this);
		//STATS
		stats = new Stats(this);
		getServer().getPluginManager().registerEvents(listener, this);
		getServer().getPluginManager().registerEvents(new TeleportFix(this), this);
		//GIFTS
		christmas = new Christmas(this);
		//COMMANDS
		cm = new CommandManager(this);
		CommandExecutor ce = cm;
		getCommand("pb").setExecutor(ce);

		active = true;
		happyhour = false;
		softreload = false;
		lobbyspawn = 0;
		afkMatchCount = new HashMap<String, Integer>();

		//autoLobby
		if(autoLobby) {
			for(Player player : getServer().getOnlinePlayers()) {
				if(autoTeam) cm.joinTeam(player, Lobby.RANDOM);
				else cm.joinLobbyPre(player);
			}
		}
		
		//weapons
		Pumpgun.init();
		
		//start no gravity task
		if (sniperNoGravity) NoGravity.run();

		//METRICS
		try {
			Metrics metrics = new Metrics(this);

			nometrics = metrics.isOptOut();
			//Custom Data:

			//Default graph:
			//Actual playing players (Lobby)
			metrics.addCustomData(new Metrics.Plotter("Actual playing (lobby)") {

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

			//Maximum playing (lobby) since last update
			metrics.addCustomData(new Metrics.Plotter("Maximum playing (lobby) since last update") {

				@Override
				public int getValue() {
					try {
						//get max:
						int max = Lobby.LOBBY.maxNumber();
						//reset max:
						Lobby.resetMaxPlayers();
						return max;
					} catch (Exception e) {
						// Failed to get the value :(
						//reset max:
						Lobby.resetMaxPlayers();
						return 0;
					}
				}
			});

			//Graph 2
			Graph graph = metrics.createGraph("Players ever played Paintball");

			//Players ever played Paintball Plotter
			graph.addPlotter(new Metrics.Plotter("Ever played Paintball") {

				@Override
				public int getValue() {
					try {
						int number = 0;
						for(String name : pm.getAllPlayerNames()) {
							if(pm.getStats(name).get("rounds") > 0) number++;
						}
						return number;
					} catch (Exception e) {
						// Failed to get the value :(
						return 0;
					}
				}
			});

			metrics.start();
		} catch (IOException e) {
			Lobby.resetMaxPlayers();
			// Failed to submit the stats :-(
		}

		//InSigns sign changer:
		Plugin insignsPlugin = getServer().getPluginManager().getPlugin("InSigns");
		if((insignsPlugin != null) && insignsPlugin.isEnabled()) {
			isf = new InSignsFeature(insignsPlugin, this);
			log("Plugin 'InSigns' found. Using it now.");
		} else {
			log("Plugin 'InSigns' not found. Additional sign features disabled.");
		}
		//TagAPI:
		Plugin tagAPIPlugin = getServer().getPluginManager().getPlugin("TagAPI");
		if((tagAPIPlugin != null) && tagAPIPlugin.isEnabled()) {
			tagAPI = new TagAPIListener(this);
			getServer().getPluginManager().registerEvents(tagAPI, this);
			log("Plugin 'TagAPI' found. Using it now.");
		} else {
			log("Plugin 'TagAPI' not found. Additional tag features disabled.");
		}
		
		//calculating turret angles:
		log("Calculating turret angles...");
		Turret.calculateTable(turretAngleMin, turretAngleMax, turretTicks, turretXSize, turretYSize, this);
		log("Calculating done.");

		//Some license stuff: Usage on own risk, no warranties, do not modify the code, do not redistribute, do not copy, and do not use for commercial purposes! Neither direct nor indirect. So this also applies to add-ons made for this plugin! 
		log("By blablubbabc enabled.");
		
		final Paintball plugin = this;
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

			@Override
			public void run() {
				delayedInfo();
				new Poster(plugin);
			}
		}, 1L);
		
		// TEST
		
		getServer().getScheduler().runTaskTimer(this, new Runnable() {
			
			@Override
			public void run() {
				
				log("Balls: "+ Ball.count + " ; Rockets: " + Rocket.getRocketCountMatch());
				for (String playerName : Ball.balls.keySet()) {
					ArrayList<Ball> pballs = Ball.balls.get(playerName);
					log ("Balls " + playerName +": " + pballs.size());
					String ids = "";
					for (Ball b : pballs) {
						ids += b.getId() + ", ";
					}
					log("IDs: " + ids);
				}
			}
		}, 100L, 60L);

	}

	public void delayedInfo() {
		getServer().getScheduler().runTaskLaterAsynchronously(this, new Runnable() {

			@Override
			public void run() {

				logBlank(" ");
				logBlank(ChatColor.YELLOW+" **************************************************");
				logBlank(ChatColor.YELLOW+" ----------------- PAINTBALL INFO -----------------");
				logBlank(" ");
				logBlank(ChatColor.RED+" License stuff:");
				logBlank(ChatColor.GOLD+"   - Usage on own risk. I give no warranties for anything.");
				logBlank(ChatColor.GOLD+"   - Do not modify. Use it as it is!");
				logBlank(ChatColor.GOLD+"   - Do not redistribute/upload/use parts of it/copy/give away.");
				logBlank(ChatColor.GOLD+"   - Do not use for commercial purposes!");
				logBlank(ChatColor.GOLD+"     -> No benefits for paying players/donors!");
				logBlank(ChatColor.GOLD+"     -> This also applies to any kind of add-on you are using");
				logBlank(ChatColor.GOLD+"        related to this plugin!");
				logBlank(" ");
				logBlank(ChatColor.DARK_GREEN+" If you like this plugin: Give feedback and donate at");
				logBlank(ChatColor.DARK_GREEN+" ->http://dev.bukkit.org/server-mods/paintball_pure_war/ ");
				logBlank(" ");
				logBlank(ChatColor.GREEN+" Thank you and good shooting!");
				logBlank(ChatColor.GREEN+"   - blablubbabc");
				logBlank(" ");
				logBlank(ChatColor.YELLOW+" **************************************************");
				logBlank(" ");
			}
		}, 20L);
	}
	
	public void onDisable(){
		if(mm != null) mm.forceReload();
		sql.closeConnection();
		getServer().getScheduler().cancelTasks(this);
		log("Disabled!");
	}

	public void log(String message) {
		System.out.println("["+this.getName()+"] "+message);
	}

	public void logBlank(String message) {
		getServer().getConsoleSender().sendMessage(message);
		//System.out.println(message);
	}

	public void reload(CommandSender sender) {
		reloadConfig();
		getServer().getPluginManager().disablePlugin(this);
		getServer().getPluginManager().enablePlugin(this);
		if (sender != null) sender.sendMessage(t.getString("REALOAD_FINISHED"));
	}

	
	//METHODS LOBBYSPAWNS
	private synchronized void loadDB() {
		lobbyspawns = new LinkedList<Location>();
		for(Location loc : sql.sqlArenaLobby.getLobbyspawns()) {
			lobbyspawns.add(loc);
		}
	}

	public synchronized void addLobbySpawn(Location loc) {
		lobbyspawns.add(loc);
		sql.sqlArenaLobby.addLobbyspawn(loc);
	}
	public synchronized void deleteLobbySpawns() {
		sql.sqlArenaLobby.removeLobbyspawns();
		lobbyspawns = new LinkedList<Location>();
	}

	public synchronized int getLobbyspawnsCount() {
		return lobbyspawns.size();
	}

	public synchronized Location getNextLobbySpawn() {
		lobbyspawn++;
		if(lobbyspawn > (lobbyspawns.size()-1)) lobbyspawn = 0;
		return (lobbyspawns.size() > 0 ? lobbyspawns.get(lobbyspawn) : null);
	}

	////////////////////////////////////
	//UTILS
	////////////////////////////////////
	
	public synchronized void afkRemove(String player) {
		afkMatchCount.remove(player);
	}
	
	public synchronized int afkGet(String player) {
		int amount = 0;
		if(afkMatchCount.get(player) != null) amount = afkMatchCount.get(player);
		return amount;
	}
	
	public synchronized void afkSet(String player, int amount) {
		afkMatchCount.put(player, amount);
	}
	
	public synchronized ArrayList<String> afkGetEntries() {
		ArrayList<String> entries = new ArrayList<String>();
		
		for(String s : afkMatchCount.keySet()) {
			entries.add(s);
		}
		
		return entries;
	}

	public void checks(Player player, boolean checkListname, boolean changeLevel) {
		if(!Utils.isEmptyInventory(player)) Utils.clearInv(player);
		//gamemode
		if(!player.getGameMode().equals(GameMode.SURVIVAL)) player.setGameMode(GameMode.SURVIVAL);
		//flymode (built-in)
		if(player.getAllowFlight()) player.setAllowFlight(false);
		if(player.isFlying()) player.setFlying(false);
		//feuer
		if(player.getFireTicks() > 0) player.setFireTicks(0);
		//Health + Food
		if(player.getHealth() < 20) player.setHealth(20);
		if(player.getFoodLevel() < 20) player.setFoodLevel(20);
		//effekte entfernen
		if(player.getActivePotionEffects().size() > 0) {
			ArrayList<PotionEffect> effects = new ArrayList<PotionEffect>();
			for(PotionEffect eff : player.getActivePotionEffects()) {
				effects.add(eff);
			}
			for(PotionEffect eff : effects) {
				player.removePotionEffect(eff.getType());
			}	
		}
		//Vehicle
		if(player.isInsideVehicle()) player.leaveVehicle();
		//walkspeed
		if(player.getWalkSpeed() != 0.2) player.setWalkSpeed(0.2F);
		//listname
		if(checkListname && listnames) player.setPlayerListName(null);
		//xp bar
		if (useXPBar) {
			if (changeLevel) player.setLevel(0);
			player.setExp(1F);
		}
	}

	public synchronized void joinLobby(Player player) {
		checks(player, true, false);
		enterLobby(player);
	}
	
	public synchronized void joinLobbyFresh(Player player) {
		enterLobby(player);
		//inventory
		if(saveInventory) {
			pm.storeInventory(player);
			player.sendMessage(t.getString("INVENTORY_SAVED"));
		}
		//exp und level:
		if (useXPBar) pm.storeExp(player);
		
		checks(player, true, true);
	}
	
	private void enterLobby(Player player) {
		//set waiting
		if(Lobby.isPlaying(player) || Lobby.isSpectating(player)) Lobby.getTeam(player).setWaiting(player);
		//Lobbyteleport
		//Vehicle
		if(player.isInsideVehicle()) player.leaveVehicle();
		player.teleport(getNextLobbySpawn());
	}

	public synchronized void leaveLobby(Player player, boolean messages, boolean teleport, boolean restoreInventory) {
		//lobby remove:
		Lobby.remove(player);
		checks(player, true, true);
		//restore saved inventory
		if(restoreInventory && saveInventory) {
			pm.restoreInventory(player);
		}
		//restore xp und level
		if (useXPBar) pm.restoreExp(player);
		//teleport:
		if(teleport) player.teleport(pm.getLoc(player));
		if(messages) {
			//messages:
			player.sendMessage(t.getString("YOU_LEFT_LOBBY"));
			nf.leave(player.getName());
		}
	}
	
}
