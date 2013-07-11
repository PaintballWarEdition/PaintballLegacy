package de.blablubbabc.paintball.gadgets.handlers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.gadgets.WeaponHandler;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class ShotgunHandler extends WeaponHandler {	
	
	private int[] angles = new int[5];
	
	public ShotgunHandler(int customItemTypeID, boolean useDefaultType) {
		super(customItemTypeID, useDefaultType);
		angles[0] = Paintball.instance.shotgunAngle2;
		angles[1] = Paintball.instance.shotgunAngle1;
		angles[2] = 0;
		angles[3] = -Paintball.instance.shotgunAngle1;
		angles[4] = -Paintball.instance.shotgunAngle2;
	}

	public void shoot(Player player, Match match, Location location, Vector direction, double speed) {
		player.getWorld().playSound(location, Sound.FIRE_IGNITE, 2.0F, 0F);
		direction.normalize();
		
		Vector dirY = (new Location(location.getWorld(), 0, 0, 0, location.getYaw(), 0)).getDirection().normalize();
		Vector n = new Vector(dirY.getZ(), 0.0, -dirY.getX());
		
		boolean alreadyAngleNull = false;
		for (int angle : angles) {
			Vector vec;
			if (angle != 0) {
				vec = Utils.rotateYAxis(dirY, angle);
				vec.multiply(Math.sqrt(vec.getX() * vec.getX() + vec.getZ() * vec.getZ())).subtract(dirY);
				vec = direction.clone().add(vec).normalize();
			} else {
				if (alreadyAngleNull) continue;
				else {
					alreadyAngleNull = true;
					vec = direction.clone();
				}
			}
			
			if (Paintball.instance.shotgunAngleVert == 0) {
				Snowball snowball = location.getWorld().spawn(location, Snowball.class);
				snowball.setShooter(player);
				Paintball.instance.weaponManager.getBallHandler().createBall(match, player, snowball, Origin.SHOTGUN);
				snowball.setVelocity(vec.clone().multiply(speed));
			} else {
				for (int i = -Paintball.instance.shotgunAngleVert; i <= Paintball.instance.shotgunAngleVert; i += Paintball.instance.shotgunAngleVert) {
					Snowball snowball = location.getWorld().spawn(location, Snowball.class);
					snowball.setShooter(player);
					Paintball.instance.weaponManager.getBallHandler().createBall(match, player, snowball, Origin.SHOTGUN);
					snowball.setVelocity(Utils.rotateAxis(vec, n, i).multiply(speed));
				}
			}
		}
	}
	
	@Override
	protected int getDefaultItemTypeID() {
		return Material.SPECKLED_MELON.getId();
	}

	@Override
	protected ItemStack setItemMeta(ItemStack itemStack) {
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(Translator.getString("WEAPON_SHOTGUN"));
		itemStack.setItemMeta(meta);
		return itemStack;
	}

	@Override
	protected void onInteract(PlayerInteractEvent event, Match match) {
		if (event.getAction() == Action.PHYSICAL || !Paintball.instance.shotgun) return;
		Player player = event.getPlayer();
		ItemStack itemInHand = player.getItemInHand();
		if (itemInHand == null) return;
		
		if (itemInHand.isSimilar(getItem())) {
			PlayerInventory inv = player.getInventory();
			if ((match.setting_balls == -1 || inv.containsAtLeast(Paintball.instance.weaponManager.getBallHandler().getItem(), Paintball.instance.shotgunAmmo))) {
				Utils.removeInventoryItems(inv, Paintball.instance.weaponManager.getBallHandler().getItem(), Paintball.instance.shotgunAmmo);
				Utils.updatePlayerInventoryLater(Paintball.instance, player);
				// INFORM MATCH
				match.onShot(player);
				
				Location location = player.getEyeLocation();
				shoot(player, match, location, location.getDirection(), Paintball.instance.shotgunSpeedmulti);
				
			} else {
				player.playSound(player.getEyeLocation(), Sound.FIRE_IGNITE, 1F, 2F);
			}
		}
	}
	
	@Override
	public void cleanUp(Match match, String playerName) {
		// nothing to do here
	}

	@Override
	public void cleanUp(Match match) {
		// nothing to do here
	}
	
}
