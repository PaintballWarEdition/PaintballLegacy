package de.blablubbabc.paintball;

import java.util.ArrayList;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

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

	public PlayerDataStore(Player player, Location to) {
		teleportStoreClearPlayer(player, to);
	}

	public void teleportStoreClearPlayer(Player player, Location to) {
		// PREPARE
		player.closeInventory();
		player.leaveVehicle();
		// LOCATION
		location = player.getLocation();
		player.teleport(to);
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

		// CLEAR COMPLETE
		clearPlayer(player, true, true);
	}

	@SuppressWarnings("deprecation")
	public void restoreTeleportPlayer(Player player) {
		// PREPARE
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
		player.setExhaustion(exhaustion);
		player.setSaturation(saturation);
		player.setFoodLevel(foodlevel);
		player.setMaxHealth(maxHealth);
		player.setHealth(health);
		player.setFireTicks(fireTicks);
		player.setRemainingAir(remainingAir);
		player.setTicksLived(ticksLived);
		player.setNoDamageTicks(noDamageTicks);
		player.setFallDistance(fallDistance);
		player.setLastDamage(lastDamage);
		player.setLastDamageCause(lastDamageCause);
		// Level / exp
		player.setLevel(level);
		player.setExp(exp);
		
		player.setGameMode(gamemode);
		player.updateInventory();
		
		// TELEPORT BACK
		player.teleport(location);
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
