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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;

import com.google.common.collect.Multimap;

import de.blablubbabc.paintball.utils.TeleportManager;
import de.blablubbabc.paintball.utils.Translator;

public class PlayerDataStore {

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
	private Map<Attribute, Double> attributeBaseValues = new EnumMap<>(Attribute.class);
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
		attributeBaseValues.clear();
		for (Attribute attribute : Attribute.values()) {
			AttributeInstance attributeInstance = player.getAttribute(attribute);
			if (attributeInstance != null) {
				attributeBaseValues.put(attribute, attributeInstance.getBaseValue());
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
		// the scoreboard will be set by the lobby / match if necessary
		clearPlayer(player, true, true);
	}

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
		// base values:
		for (Entry<Attribute, Double> entry : attributeBaseValues.entrySet()) {
			AttributeInstance attributeInstance = player.getAttribute(entry.getKey());
			if (attributeInstance != null) {
				attributeInstance.setBaseValue(entry.getValue());
			}
		}
		// apply item attribute modifiers:
		updateItemAttributes(player);

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

	private static void updateItemAttributes(LivingEntity entity) {
		if (entity == null) return;
		EntityEquipment equipment = entity.getEquipment();
		if (equipment == null) return;

		for (EquipmentSlot slot : EquipmentSlot.values()) {
			// get item by slot:
			ItemStack item = null;
			switch (slot) {
			case HAND:
				item = equipment.getItemInMainHand();
				break;
			case OFF_HAND:
				item = equipment.getItemInOffHand();
				break;
			case FEET:
				item = equipment.getBoots();
				break;
			case LEGS:
				item = equipment.getLeggings();
				break;
			case CHEST:
				item = equipment.getChestplate();
				break;
			case HEAD:
				item = equipment.getHelmet();
				break;
			default:
				break;
			}
			if (item == null || !item.hasItemMeta()) continue;

			Multimap<Attribute, AttributeModifier> modifiers = item.getItemMeta().getAttributeModifiers(slot);
			if (modifiers == null) continue;
			for (Entry<Attribute, Collection<AttributeModifier>> entry : modifiers.asMap().entrySet()) {
				AttributeInstance attributeInstance = entity.getAttribute(entry.getKey());
				if (attributeInstance == null) continue;
				for (AttributeModifier modifier : entry.getValue()) {
					// duplicate modifiers are not allowed and there is no method to check if the
					// modifier is already applied, so: remove first, in case the modifier is
					// already active:
					attributeInstance.removeModifier(modifier);
					attributeInstance.addModifier(modifier);
				}
			}
		}
	}

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
				// TODO This doesn't actually return the entity type specific default attribute
				// value (i.e. the default attribute base value for players), the the global
				// attribute default value.
				// See https://hub.spigotmc.org/jira/browse/SPIGOT-5890
				attributeInstance.setBaseValue(attributeInstance.getDefaultValue());
			}
		}

		// TODO Since MC 1.16 the walk speed is determined by the movement speed attribute as well.
		// Since our code above does not set the correct default value for this attribute, we also
		// always set the walk speed here. This will then also automatically update the movement
		// speed attribute accordingly.
		player.setWalkSpeed(0.2F);
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
