package de.blablubbabc.paintball.gadgets;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.gadgets.handlers.AirstrikeHandler;
import de.blablubbabc.paintball.gadgets.handlers.BallHandler;
import de.blablubbabc.paintball.gadgets.handlers.ConcussionHandler;
import de.blablubbabc.paintball.gadgets.handlers.FlashbangHandler;
import de.blablubbabc.paintball.gadgets.handlers.GiftHandler;
import de.blablubbabc.paintball.gadgets.handlers.GrenadeHandler;
import de.blablubbabc.paintball.gadgets.handlers.GrenadeM2Handler;
import de.blablubbabc.paintball.gadgets.handlers.MarkerHandler;
import de.blablubbabc.paintball.gadgets.handlers.TurretHandler;
import de.blablubbabc.paintball.utils.Translator;

public class WeaponManager {
	private List<WeaponHandler> weaponHandlers = new ArrayList<WeaponHandler>();
	
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
	
	public WeaponManager(Paintball plugin) {
		giftHandler = new GiftHandler(Material.CHEST.getId(), false);
		noGravityHandler = new NoGravityHandler();
		
		ballHandler = new BallHandler(Material.SNOW_BALL.getId(), false);
		
		// init all default weapons and gadgets:
		markerHandler = new MarkerHandler(Material.SNOW_BALL.getId(), false);
		airstrikeHandler = new AirstrikeHandler(Material.STICK.getId(), false);
		flashbangHandler = new FlashbangHandler(Material.GHAST_TEAR.getId(), false);
		concussionHandler = new ConcussionHandler(Material.SPIDER_EYE.getId(), false);
		grenadeHandler = new GrenadeHandler(Material.EGG.getId(), false);
		grenadeM2Handler = new GrenadeM2Handler(Material.SLIME_BALL.getId(), false);
		turretHandler = new TurretHandler(Material.PUMPKIN.getId(), false);
		
		//TODO
		
	}
	
	//////// Default weapon handlers /////////////
	
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
	
	
	//////////////////////////////////////////////
	
	public void registerWeaponHandler(WeaponHandler weaponHandler) {
		weaponHandlers.add(weaponHandler);
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
	
	public void onInteract(PlayerInteractEvent event, Match match) {
		for (WeaponHandler weaponHandler : weaponHandlers) {
			weaponHandler.onInteract(event, match);
		}
	}
	
	public void onBlockPlace(BlockPlaceEvent event, Match match) {
		for (WeaponHandler weaponHandler : weaponHandlers) {
			weaponHandler.onBlockPlace(event, match);
		}
	}
	
	public void onItemPickup(PlayerPickupItemEvent event) {
		for (WeaponHandler weaponHandler : weaponHandlers) {
			weaponHandler.onItemPickup(event);
		}
	}
	
	public void onDamagedByEntity(EntityDamageByEntityEvent event, Match match, Player attacker) {
		for (WeaponHandler weaponHandler : weaponHandlers) {
			weaponHandler.onDamagedByEntity(event, match, attacker);
		}
	}
	
	public void onProjectileHit(ProjectileHitEvent event, Projectile projectile, Match match, Player shooter) {
		for (WeaponHandler weaponHandler : weaponHandlers) {
			weaponHandler.onProjectileHit(event, projectile, match, shooter);
		}
	}
	
	public void onItemHeld(Player player) {
		for (WeaponHandler weaponHandler : weaponHandlers) {
			weaponHandler.onItemHeld(player);
		}
	}
	
	public void cleanUp(Match match, String playerName) {
		for (WeaponHandler weaponHandler : weaponHandlers) {
			weaponHandler.cleanUp(match, playerName);
		}
	}
	
	public void cleanUp(Match match) {
		for (WeaponHandler weaponHandler : weaponHandlers) {
			weaponHandler.cleanUp(match);
		}
		ballHandler.cleanUp(match);
	}

	public ItemStack setMeta(ItemStack itemStack) {
		int typeID = itemStack.getTypeId();
		
		// Team colored wool:
		if (typeID == Material.WOOL.getId()) {
			ItemMeta meta = itemStack.getItemMeta();
			byte data = itemStack.getData().getData();
			if (data == DyeColor.RED.getWoolData()) {
				meta.setDisplayName(Translator.getString("TEAM_RED"));
			} else if (data == DyeColor.BLUE.getWoolData()) {
				meta.setDisplayName(Translator.getString("TEAM_BLUE"));
				itemStack.setItemMeta(meta);
			} else if (data == DyeColor.YELLOW.getWoolData()) {
				meta.setDisplayName(Translator.getString("TEAM_SPECTATOR"));
				itemStack.setItemMeta(meta);
			}
			itemStack.setItemMeta(meta);
			return itemStack;
		}
		
		// Shop book:
		if (typeID == Material.BOOK.getId()) {
			ItemMeta meta = itemStack.getItemMeta();
			meta.setDisplayName(Translator.getString("SHOP_ITEM"));
			itemStack.setItemMeta(meta);
			return itemStack;
		}
		
		
		for (WeaponHandler weaponHandler : weaponHandlers) {
			if (weaponHandler.getItemTypeID() == typeID) {
				return weaponHandler.setItemMeta(itemStack);
			}
		}
		
		return itemStack;
	}
}
