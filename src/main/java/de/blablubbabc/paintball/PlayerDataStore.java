/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;

import de.blablubbabc.paintball.utils.TeleportManager;
import de.blablubbabc.paintball.utils.Translator;

public class PlayerDataStore {

	private static class AttributeData {

		public final double baseValue;
		public final Collection<AttributeModifier> modifiers;

		public AttributeData(double baseValue, Collection<AttributeModifier> modifiers) {
			this.baseValue = baseValue;
			this.modifiers = modifiers;
		}
	}

	// DATA
	// Names
	private String listname;
	// Location
	private Location location;
	// Inventory
	private ItemStack[] invContent;
	// PotionEffects
	private List<PotionEffect> potionEffects = new ArrayList<PotionEffect>();
	// attributes:
	private Map<Attribute, AttributeData> attributes = new EnumMap<>(Attribute.class);
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
		// LOCATION + TELEPORT
		location = player.getLocation();
		TeleportManager.teleport(player, to);
	}

	private void storeClearPlayer(Player player) {
		// GAMEMODE
		gamemode = player.getGameMode();
		player.setGameMode(GameMode.SURVIVAL);

		// PotionEffects
		Collection<PotionEffect> activePotionEffects = player.getActivePotionEffects();
		potionEffects.clear();
		potionEffects.addAll(activePotionEffects);
		for (PotionEffect effect : activePotionEffects) {
			player.removePotionEffect(effect.getType());
		}

		// Names
		listname = player.getPlayerListName();

		// Inventory
		if (Paintball.getInstance().saveInventory) {
			player.closeInventory();
			PlayerInventory inv = player.getInventory();
			invContent = inv.getContents();
			player.sendMessage(Translator.getString("INVENTORY_SAVED"));
		}

		// attributes:
		attributes.clear();
		for (Attribute attribute : Attribute.values()) {
			AttributeInstance attributeInstance = player.getAttribute(attribute);
			if (attributeInstance != null) {
				attributes.put(attribute, new AttributeData(attributeInstance.getBaseValue(), attributeInstance.getModifiers()));
			}
		}

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
		if (Paintball.getInstance().listnames) player.setPlayerListName(listname);
		// player.setDisplayName(displayname);
		// Inventory
		if (Paintball.getInstance().saveInventory) {
			if (invContent != null) {
				player.getInventory().setContents(invContent);
			}
			player.sendMessage(Translator.getString("INVENTORY_RESTORED"));
		}

		// Flying
		player.setAllowFlight(allowFlight);
		player.setFlying(isFlying);

		// restore attributes:
		for (Entry<Attribute, AttributeData> entry : attributes.entrySet()) {
			AttributeInstance attributeInstance = player.getAttribute(entry.getKey());
			if (attributeInstance != null) {
				AttributeData attributeData = entry.getValue();
				attributeInstance.setBaseValue(attributeData.baseValue);
				for (AttributeModifier modifier : attributeData.modifiers) {
					attributeInstance.addModifier(modifier);
				}
			}
		}

		// Status
		player.setWalkSpeed(walkspeed);
		player.setFlySpeed(flyspeed);
		player.setExhaustion(exhaustion);
		player.setSaturation(saturation);
		player.setFoodLevel(foodlevel);
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
		if (Paintball.getInstance().scoreboardLobby) {
			player.setScoreboard(scoreboard != null ? scoreboard : Bukkit.getScoreboardManager().getMainScoreboard());
		}

		// PotionEffects
		for (PotionEffect effect : potionEffects) {
			player.addPotionEffect(effect);
		}

		player.setGameMode(gamemode);
		player.updateInventory();

		// TELEPORT BACK
		if (!withoutTeleport) TeleportManager.teleport(player, location);
	}

	@SuppressWarnings("deprecation")
	public static void clearPlayer(Player player, boolean checkListname, boolean changeLevel) {
		// PREPARE
		player.closeInventory();
		player.leaveVehicle();
		player.setGameMode(GameMode.SURVIVAL);
		// CLEAR PLAYER
		player.getInventory().clear();
		if (checkListname && Paintball.getInstance().listnames) {
			player.setPlayerListName(null);
		}
		if (player.getActivePotionEffects().size() > 0) {
			for (PotionEffect effect : player.getActivePotionEffects()) {
				player.removePotionEffect(effect.getType());
			}
		}
		if (player.getAllowFlight()) player.setAllowFlight(false);
		if (player.isFlying()) player.setFlying(false);

		// reset attributes:
		for (Attribute attribute : Attribute.values()) {
			AttributeInstance attributeInstance = player.getAttribute(attribute);
			if (attributeInstance != null) {
				for (AttributeModifier modifier : attributeInstance.getModifiers()) {
					attributeInstance.removeModifier(modifier);
				}
				attributeInstance.setBaseValue(attributeInstance.getDefaultValue());
			}
		}

		if (player.getWalkSpeed() != 0.2F) player.setWalkSpeed(0.2F);
		if (player.getFlySpeed() != 0.1F) player.setFlySpeed(0.1F);
		if (player.getFoodLevel() != 20) player.setFoodLevel(20);
		if (player.getHealth() != 20) player.setHealth(20);
		if (player.getFireTicks() != 0) player.setFireTicks(0);
		if (Paintball.getInstance().useXPBar) {
			if (changeLevel && player.getLevel() != 0) player.setLevel(0);
			if (player.getExp() != 1F) player.setExp(1F);
		}

		player.updateInventory();
	}
}
