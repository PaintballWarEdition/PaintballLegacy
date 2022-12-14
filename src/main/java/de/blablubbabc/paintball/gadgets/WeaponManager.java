/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.gadgets;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.gadgets.handlers.AirstrikeHandler;
import de.blablubbabc.paintball.gadgets.handlers.BallHandler;
import de.blablubbabc.paintball.gadgets.handlers.ConcussionHandler;
import de.blablubbabc.paintball.gadgets.handlers.FlashbangHandler;
import de.blablubbabc.paintball.gadgets.handlers.GiftHandler;
import de.blablubbabc.paintball.gadgets.handlers.GrenadeHandler;
import de.blablubbabc.paintball.gadgets.handlers.GrenadeM2Handler;
import de.blablubbabc.paintball.gadgets.handlers.MarkerHandler;
import de.blablubbabc.paintball.gadgets.handlers.MineHandler;
import de.blablubbabc.paintball.gadgets.handlers.OrbitalstrikeHandler;
import de.blablubbabc.paintball.gadgets.handlers.PumpgunHandler;
import de.blablubbabc.paintball.gadgets.handlers.RocketHandler;
import de.blablubbabc.paintball.gadgets.handlers.ShotgunHandler;
import de.blablubbabc.paintball.gadgets.handlers.SniperHandler;
import de.blablubbabc.paintball.gadgets.handlers.TurretHandler;
import de.blablubbabc.paintball.utils.Translator;

public class WeaponManager {

	private Map<String, WeaponHandler> weaponHandlers = new HashMap<String, WeaponHandler>();

	private GiftHandler giftHandler;
	private NoGravityHandler noGravityHandler;

	private BallHandler ballHandler;

	private MarkerHandler markerHandler;
	private AirstrikeHandler airstrikeHandler;
	private FlashbangHandler flashbangHandler;
	private ConcussionHandler concussionHandler;
	private GrenadeHandler grenadeHandler;
	private GrenadeM2Handler grenadeM2Handler;
	private TurretHandler turretHandler;
	private MineHandler mineHandler;
	private OrbitalstrikeHandler orbitalstrikeHandler;
	private PumpgunHandler pumpgunHandler;
	private RocketHandler rocketHandler;
	private ShotgunHandler shotgunHandler;
	private SniperHandler sniperHandler;

	public WeaponManager() {

	}

	public void initWeaponHandlers() {
		giftHandler = new GiftHandler();
		noGravityHandler = new NoGravityHandler();

		ballHandler = new BallHandler();

		// init all default weapons and gadgets:
		markerHandler = new MarkerHandler();
		airstrikeHandler = new AirstrikeHandler();
		flashbangHandler = new FlashbangHandler();
		concussionHandler = new ConcussionHandler();
		grenadeHandler = new GrenadeHandler();
		grenadeM2Handler = new GrenadeM2Handler();
		turretHandler = new TurretHandler();
		mineHandler = new MineHandler();
		orbitalstrikeHandler = new OrbitalstrikeHandler();
		pumpgunHandler = new PumpgunHandler();
		rocketHandler = new RocketHandler();
		shotgunHandler = new ShotgunHandler();
		sniperHandler = new SniperHandler();
	}

	// ////// Default weapon handlers /////////////

	public MarkerHandler getMarkerHandler() {
		return markerHandler;
	}

	public GrenadeHandler getGrenadeHandler() {
		return grenadeHandler;
	}

	public GrenadeM2Handler getGrenadeM2Handler() {
		return grenadeM2Handler;
	}

	public FlashbangHandler getFlashbangHandler() {
		return flashbangHandler;
	}

	public ConcussionHandler getConcussionHandler() {
		return concussionHandler;
	}

	public AirstrikeHandler getAirstrikeHandler() {
		return airstrikeHandler;
	}

	public TurretHandler getTurretHandler() {
		return turretHandler;
	}

	public MineHandler getMineHandler() {
		return mineHandler;
	}

	public OrbitalstrikeHandler getOrbitalstrikeHandler() {
		return orbitalstrikeHandler;
	}

	public PumpgunHandler getPumpgunHandler() {
		return pumpgunHandler;
	}

	public ShotgunHandler getShotgunHandler() {
		return shotgunHandler;
	}

	public RocketHandler getRocketHandler() {
		return rocketHandler;
	}

	public SniperHandler getSniperHandler() {
		return sniperHandler;
	}

	// ////////////////////////////////////////////

	public void giveWeapon(Player player, String weaponName) {
		giveWeapon(player, weaponName, 1);
	}

	public void giveWeapon(Player player, WeaponHandler weapon) {
		giveWeapon(player, weapon, 1);
	}

	public void giveWeapon(Player player, String weaponName, int amount) {
		giveWeapon(player, getWeaponHandler(weaponName), amount);
	}

	public void giveWeapon(Player player, WeaponHandler weapon, int amount) {
		giveWeapon(player, weapon, amount, false);
	}

	public void giveWeapon(Player player, String weaponName, int amount, boolean updateInventory) {
		giveWeapon(player, getWeaponHandler(weaponName), amount, updateInventory);
	}

	public void giveWeapon(Player player, WeaponHandler weapon, int amount, boolean updateInventory) {
		if (player != null && weapon != null) {
			while (amount > 0) {
				ItemStack item = weapon.getItem().clone();
				if (amount > 64) {
					item.setAmount(64);
					amount -= 64;
				} else {
					item.setAmount(amount);
					amount = 0;
				}
				player.getInventory().addItem(item);
			}

			if (updateInventory) player.updateInventory();
		}
	}

	/*public Collection<WeaponHandler> getRegisteredWeaponHandlers() {
		return weaponHandlers.values();
	}*/

	// ////////////////////////////////////////////

	public WeaponHandler getWeaponHandler(String weaponName) {
		if (weaponName == null || weaponName.isEmpty()) return null;
		for (Entry<String, WeaponHandler> entry : weaponHandlers.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(weaponName)) return entry.getValue();
		}
		return null;
	}

	public void registerWeaponHandler(String weaponName, WeaponHandler weaponHandler) {
		if (weaponName == null || weaponName.isEmpty() || weaponHandler == null) throw new IllegalArgumentException();
		weaponName = weaponName.toLowerCase();
		if (weaponHandlers.containsKey(weaponName)) throw new IllegalArgumentException("Weapon with name '" + weaponName + "' is already registered!");
		weaponHandlers.put(weaponName, weaponHandler);
	}

	public GiftHandler getGiftManager() {
		return giftHandler;
	}

	public NoGravityHandler getNoGravityHandler() {
		return noGravityHandler;
	}

	public BallHandler getBallHandler() {
		return ballHandler;
	}

	// EVENTS

	public void onInteract(PlayerInteractEvent event, Match match) {
		for (WeaponHandler weaponHandler : weaponHandlers.values()) {
			weaponHandler.onInteract(event, match);
		}
	}

	public void onToggleSneak(PlayerToggleSneakEvent event, Match match) {
		for (WeaponHandler weaponHandler : weaponHandlers.values()) {
			weaponHandler.onToggleSneak(event, match);
		}
	}

	public void onBlockPlace(BlockPlaceEvent event, Match match) {
		for (WeaponHandler weaponHandler : weaponHandlers.values()) {
			weaponHandler.onBlockPlace(event, match);
		}
	}

	public void onItemPickup(EntityPickupItemEvent event) {
		for (WeaponHandler weaponHandler : weaponHandlers.values()) {
			weaponHandler.onItemPickup(event);
		}
	}

	public void onDamagedByEntity(EntityDamageByEntityEvent event, Match match, Player attacker) {
		for (WeaponHandler weaponHandler : weaponHandlers.values()) {
			weaponHandler.onDamagedByEntity(event, match, attacker);
		}
	}

	public void onProjectileHit(ProjectileHitEvent event, Projectile projectile, Match match, Player shooter) {
		for (WeaponHandler weaponHandler : weaponHandlers.values()) {
			weaponHandler.onProjectileHit(event, projectile, match, shooter);
		}
	}

	public void onItemHeld(Player player, ItemStack newItem) {
		for (WeaponHandler weaponHandler : weaponHandlers.values()) {
			weaponHandler.onItemHeld(player, newItem);
		}
	}

	// /////////////////////////////////////////////////////////////////////////

	public void cleanUp(Match match, UUID playerId) {
		for (WeaponHandler weaponHandler : weaponHandlers.values()) {
			weaponHandler.cleanUp(match, playerId);
		}
	}

	public void cleanUp(Match match) {
		for (WeaponHandler weaponHandler : weaponHandlers.values()) {
			weaponHandler.cleanUp(match);
		}
	}

	public ItemStack setMeta(ItemStack itemStack) {
		Material type = itemStack.getType();

		// Shop book:
		if (type == Material.BOOK) {
			ItemMeta meta = itemStack.getItemMeta();
			meta.setDisplayName(Translator.getString("SHOP_ITEM"));
			itemStack.setItemMeta(meta);
			return itemStack;
		}

		for (WeaponHandler weaponHandler : weaponHandlers.values()) {
			if (weaponHandler.getItemType() == type) {
				return weaponHandler.setItemMeta(itemStack);
			}
		}

		return itemStack;
	}
}
