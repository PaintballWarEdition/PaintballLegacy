package me.blablubbabc.paintball;

import java.util.ArrayList;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

public class PlayerDataStore {
	private Player player;
	private Paintball plugin;

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
	private float exhaustion;
	private float saturation;
	private int foodlevel;
	private int health;
	private int fireTicks;
	private int remainingAir;
	private int ticksLived;
	private int noDamageTicks;
	private float fallDistance;
	private GameMode gamemode;
	private int lastDamage;
	private EntityDamageEvent lastDamageCause;
	// Level / exp
	private int level;
	private float exp;

	public PlayerDataStore(Player player, Paintball plugin) {
		this.player = player;
	}

	public void storePlayer() {
		// STORE DATA
		// Names
		listname = player.getPlayerListName();
		// Location
		location = player.getLocation();
		// Inventory
		if (plugin.saveInventory) {
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
		exhaustion = player.getExhaustion();
		saturation = player.getSaturation();
		foodlevel = player.getFoodLevel();
		health = player.getHealth();
		fireTicks = player.getFireTicks();
		remainingAir = player.getRemainingAir();
		ticksLived = player.getTicksLived();
		noDamageTicks = player.getNoDamageTicks();
		fallDistance = player.getFallDistance();
		gamemode = player.getGameMode();
		lastDamage = player.getLastDamage();
		lastDamageCause = player.getLastDamageCause();
		// vehicle
		// Level / exp
		level = player.getLevel();
		exp = player.getExp();

		// CLEAR
		clearPlayer(true);
	}

	public void clearPlayer(boolean checkListname) {
		// CLEAR PLAYER
		// Names
		// listname
		if (checkListname && plugin.listnames)
			player.setPlayerListName(null);
		// displayname
		// location
		// Inventory
		player.closeInventory();
		player.getInventory().clear(-1, -1);
		// PotionEffects
		for (PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}
		// Flying
		player.setAllowFlight(false);
		player.setFlying(false);
		// Status
		// exhaustion
		// saturation
		player.setFoodLevel(20);
		player.setHealth(20);
		player.setFireTicks(0);
		// remainingAir
		// ticksLived
		// noDamageTicks
		// fallDistance
		player.setGameMode(GameMode.SURVIVAL);
		// lastDamage
		// lastDamageCause
		player.leaveVehicle();
		// Level / exp
		player.setLevel(0);
		player.setExp(0F);
	}

	public void restorePlayer() {
		clearPlayer(true);
		// RESTORE PLAYER
		// Names
		if(plugin.listnames) player.setPlayerListName(listname);
		//player.setDisplayName(displayname);
		// Inventory
		if (plugin.saveInventory) {
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
		player.setExhaustion(exhaustion);
		player.setSaturation(saturation);
		player.setFoodLevel(foodlevel);
		player.setHealth(health);
		player.setFireTicks(fireTicks);
		player.setRemainingAir(remainingAir);
		player.setTicksLived(ticksLived);
		player.setNoDamageTicks(noDamageTicks);
		player.setFallDistance(fallDistance);
		player.setGameMode(gamemode);
		player.setLastDamage(lastDamage);
		player.setLastDamageCause(lastDamageCause);
		// Level / exp
		player.setLevel(level);
		player.setExp(exp);
		// location
		player.teleport(location);
	}
}
