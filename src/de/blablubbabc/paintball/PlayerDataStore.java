package de.blablubbabc.paintball;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;

import de.blablubbabc.paintball.utils.Translator;


public class PlayerDataStore {
	
	// DATA
	// Names
	private String listname;
	// Location
	private Location location;
	// Inventory
	private ItemStack[] invContent;
	private ItemStack[] invArmor;
	// PotionEffects
	private ArrayList<PotionEffect> potionEffects = new ArrayList<PotionEffect>();
	// Flying
	private boolean allowFlight;
	private boolean isFlying;
	// Status
	private float walkspeed;
	private float flyspeed;
	private float exhaustion;
	private float saturation;
	private int foodlevel;
	private double health;
	private double maxHealth;
	private int fireTicks;
	private int remainingAir;
	private int ticksLived;
	private int noDamageTicks;
	private float fallDistance;
	private GameMode gamemode;
	private double lastDamage;
	private EntityDamageEvent lastDamageCause;
	// Level / exp
	private int level;
	private float exp;
	// Scoreboard
	private Scoreboard scoreboard;

	// teleport, store, clear
	public PlayerDataStore(Player player, Location to) {
		teleportPlayer(player, to);
		storeClearPlayer(player);
	}
	
	// only store, clear
	public PlayerDataStore(Player player) {
		// make sure location is not null..
		location = player.getLocation();
		
		storeClearPlayer(player);
	}

	private void teleportPlayer(Player player, Location to) {
		// PREPARE
		player.closeInventory();
		player.leaveVehicle();
		// LOCATION
		location = player.getLocation();
		player.teleport(to);
	}
	
	private void storeClearPlayer(Player player) {
		// GAMEMODE
		gamemode = player.getGameMode();
		player.setGameMode(GameMode.SURVIVAL);
		
		// STORE DATA
		// Names
		listname = player.getPlayerListName();
		// Inventory
		if (Paintball.instance.saveInventory) {
			player.closeInventory();
			PlayerInventory inv = player.getInventory();
			invContent = inv.getContents();
			invArmor = inv.getArmorContents();
			player.sendMessage(Translator.getString("INVENTORY_SAVED"));
		}
		// PotionEffects
		potionEffects.addAll(player.getActivePotionEffects());
		// Flying
		allowFlight = player.getAllowFlight();
		isFlying = player.isFlying();
		// Status
		walkspeed = player.getWalkSpeed();
		flyspeed = player.getFlySpeed();
		exhaustion = player.getExhaustion();
		saturation = player.getSaturation();
		foodlevel = player.getFoodLevel();
		health = player.getHealth();
		maxHealth = player.getMaxHealth();
		fireTicks = player.getFireTicks();
		remainingAir = player.getRemainingAir();
		ticksLived = player.getTicksLived();
		noDamageTicks = player.getNoDamageTicks();
		fallDistance = player.getFallDistance();
		lastDamage = player.getLastDamage();
		lastDamageCause = player.getLastDamageCause();
		// vehicle
		// Level / exp
		level = player.getLevel();
		exp = player.getExp();
		// scoreboard
		scoreboard = player.getScoreboard();

		// CLEAR COMPLETE
		// the scoreboard will be set by the lobby / match if necesarry
		clearPlayer(player, true, true);
	}

	@SuppressWarnings("deprecation")
	public void restoreTeleportPlayer(Player player, boolean withoutTeleport) {
		// PREPARE
		// scoreboard will be reset anyway:
		clearPlayer(player, true, true);
		// RESTORE PLAYER
		
		// Names
		if(Paintball.instance.listnames) player.setPlayerListName(listname);
		//player.setDisplayName(displayname);
		// Inventory
		if (Paintball.instance.saveInventory) {
			if (invContent != null) {
				player.getInventory().setContents(invContent);
			}
			if (invArmor != null) {
				player.getInventory().setArmorContents(invArmor);
			}
			player.sendMessage(Translator.getString("INVENTORY_RESTORED"));
		}
		// PotionEffects
		for (PotionEffect effect : potionEffects) {
			player.addPotionEffect(effect);
		}
		// Flying
		player.setAllowFlight(allowFlight);
		player.setFlying(isFlying);
		// Status
		player.setWalkSpeed(walkspeed);
		player.setFlySpeed(flyspeed);
		player.setExhaustion(exhaustion);
		player.setSaturation(saturation);
		player.setFoodLevel(foodlevel);
		player.setMaxHealth(maxHealth);
		player.setHealth(health);
		player.setFireTicks(fireTicks);
		player.setRemainingAir(remainingAir);
		// don't know for sure, if this is still needed..
		player.setTicksLived(ticksLived >= 1 ? ticksLived : 1);
		player.setNoDamageTicks(noDamageTicks);
		player.setFallDistance(fallDistance);
		player.setLastDamage(lastDamage);
		player.setLastDamageCause(lastDamageCause);
		// Level / exp
		player.setLevel(level);
		player.setExp(exp);
		// scoreboard
		if (Paintball.instance.scoreboardLobby) {
			player.setScoreboard(scoreboard != null ? scoreboard : Bukkit.getScoreboardManager().getMainScoreboard());
		}
		
		player.setGameMode(gamemode);
		player.updateInventory();
		
		// TELEPORT BACK
		if (!withoutTeleport) player.teleport(location);
	}
	
	@SuppressWarnings("deprecation")
	public static void clearPlayer(Player player, boolean checkListname, boolean changeLevel) {
		// PREPARE
		player.closeInventory();
		player.leaveVehicle();
		player.setGameMode(GameMode.SURVIVAL);
		// CLEAR PLAYER
		player.getInventory().clear(-1, -1);
		if (checkListname && Paintball.instance.listnames) {
			player.setPlayerListName(null);
		}
		if (player.getActivePotionEffects().size() > 0) {
			for (PotionEffect effect : player.getActivePotionEffects()) {
				player.removePotionEffect(effect.getType());
			}
		}
		if (player.getAllowFlight()) player.setAllowFlight(false);
		if (player.isFlying()) player.setFlying(false);
		if (player.getWalkSpeed() != 0.2F) player.setWalkSpeed(0.2F);
		if (player.getFlySpeed() != 0.1F) player.setFlySpeed(0.1F);
		if (player.getFoodLevel() != 20) player.setFoodLevel(20);
		if (player.getHealth() != 20) player.setHealth(20);
		if (player.getFireTicks() != 0) player.setFireTicks(0);
		if (Paintball.instance.useXPBar) {
			if (changeLevel && player.getLevel() != 0) player.setLevel(0);
			if (player.getExp() != 1F) player.setExp(1F);
		}
		
		player.updateInventory();
	}
}
