/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.gadgets.handlers;

import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import de.blablubbabc.paintball.FragInformations;
import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.gadgets.Gadget;
import de.blablubbabc.paintball.gadgets.GadgetManager;
import de.blablubbabc.paintball.gadgets.WeaponHandler;
import de.blablubbabc.paintball.gadgets.handlers.BallHandler.Ball;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class RocketHandler extends WeaponHandler {

	private GadgetManager gadgetManager = new GadgetManager();

	public RocketHandler() {
		this(null);
	}

	public RocketHandler(Material customItemType) {
		super("Rocket Launcher", customItemType, new Origin() {

			@Override
			public String getKillMessage(FragInformations fragInfo) {
				return Translator.getString("WEAPON_FEED_ROCKET", getDefaultVariablesMap(fragInfo));
			}
		});
	}

	public Rocket createRocket(Match match, Player player, Entity rocket, Origin origin) {
		return new Rocket(gadgetManager, match, player, rocket, origin);
	}

	@Override
	protected Material getDefaultItemType() {
		return Material.REPEATER;
	}

	@Override
	protected ItemStack setItemMeta(ItemStack itemStack) {
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(Translator.getString("WEAPON_ROCKET"));
		itemStack.setItemMeta(meta);
		return itemStack;
	}

	@Override
	protected void onInteract(PlayerInteractEvent event, Match match) {
		if (event.getAction() == Action.PHYSICAL || !Paintball.getInstance().rocket) return;
		Player player = event.getPlayer();
		PlayerInventory playerInventory = player.getInventory();
		ItemStack itemInHand = playerInventory.getItemInMainHand();
		if (itemInHand == null) return;

		if (itemInHand.isSimilar(getItem())) {
			if (gadgetManager.getMatchGadgetCount(match) < Paintball.getInstance().rocketMatchLimit) {
				if (gadgetManager.getPlayerGadgetCount(match, player.getUniqueId()) < Paintball.getInstance().rocketPlayerLimit) {

					World world = player.getWorld();
					Vector direction = player.getLocation().getDirection().normalize();
					Location spawnLoc = player.getEyeLocation();

					world.playSound(spawnLoc, Sound.ENTITY_SILVERFISH_AMBIENT, 2.0F, 1F);
					Fireball rocket = (Fireball) world.spawnEntity(spawnLoc, EntityType.FIREBALL);
					rocket.setIsIncendiary(false);
					rocket.setYield(0F);
					rocket.setShooter(player);
					rocket.setVelocity(direction.multiply(Paintball.getInstance().rocketSpeedMulti));

					createRocket(match, player, rocket, this.getWeaponOrigin());

					if (itemInHand.getAmount() <= 1) {
						playerInventory.setItemInMainHand(null);
					} else {
						itemInHand.setAmount(itemInHand.getAmount() - 1);
						playerInventory.setItemInMainHand(itemInHand);
					}
					Utils.updatePlayerInventoryLater(Paintball.getInstance(), player);
				} else {
					player.sendMessage(Translator.getString("ROCKET_PLAYER_LIMIT_REACHED"));
				}
			} else {
				player.sendMessage(Translator.getString("ROCKET_MATCH_LIMIT_REACHED"));
			}
		}
	}

	@Override
	protected void onProjectileHit(ProjectileHitEvent event, Projectile projectile, Match match, Player shooter) {
		if (Paintball.getInstance().rocket && projectile.getType() == EntityType.FIREBALL) {
			Gadget rocketGadget = gadgetManager.getGadget(projectile, match, shooter.getUniqueId());
			if (rocketGadget != null) {
				Rocket rocket = (Rocket) rocketGadget;
				rocket.explode();
			}
		}
	}

	@Override
	public void cleanUp(Match match, UUID playerId) {
		gadgetManager.cleanUp(match, playerId);
	}

	@Override
	public void cleanUp(Match match) {
		gadgetManager.cleanUp(match);
	}

	public class Rocket extends Gadget {
		private final Entity entity;
		private final Player player;

		private int tickTask = -1;
		private int lives;

		private boolean exploded = false;

		private Rocket(GadgetManager gadgetManager, Match match, Player player, Entity rocket, Origin origin) {
			super(gadgetManager, match, player, origin);

			this.entity = rocket;
			this.player = player;
			this.lives = Paintball.getInstance().rocketRange * 10;
			tick();
		}

		private void tick() {
			tickTask = Paintball.getInstance().getServer().getScheduler().runTaskLater(Paintball.getInstance(), new Runnable() {

				@Override
				public void run() {
					if (entity.isValid() && lives > 0) {
						lives--;

						// some effects:
						if (Paintball.getInstance().effects) {
							Location loc = entity.getLocation();
							World world = entity.getWorld();
							for (int i = 1; i <= 8; i++) {
								world.playEffect(loc, Effect.SMOKE, i);
							}
							world.playEffect(loc, Effect.MOBSPAWNER_FLAMES, 4);
						}
						tick();
					} else {
						// remove rocket:
						explode();
					}
				}
			}, 2L).getTaskId();
		}

		public void explode() {
			if (!exploded) {
				exploded = true;
				Location loc = entity.getLocation();
				loc.getWorld().createExplosion(loc, -1F, false);
				for (Vector v : Utils.getDirections()) {
					final Snowball snowball = loc.getWorld().spawn(loc, Snowball.class);
					snowball.setShooter(player);

					final Ball ball = Paintball.getInstance().weaponManager.getBallHandler().createBall(match, player, snowball, getGadgetOrigin());

					Vector v2 = v.clone();
					v2.setX(v.getX() + Utils.random.nextDouble() - Utils.random.nextDouble());
					v2.setY(v.getY() + Utils.random.nextDouble() - Utils.random.nextDouble());
					v2.setZ(v.getZ() + Utils.random.nextDouble() - Utils.random.nextDouble());
					snowball.setVelocity(v2.normalize());

					Paintball.getInstance().getServer().getScheduler().runTaskLater(Paintball.getInstance(), new Runnable() {

						@Override
						public void run() {
							ball.dispose(true);
						}
					}, (long) Paintball.getInstance().rocketTime);
				}
			}

			// remove from tracking:
			dispose(true);
		}

		@Override
		public void dispose(boolean removeFromGadgetHandlerTracking) {
			if (tickTask != -1) {
				Paintball.getInstance().getServer().getScheduler().cancelTask(tickTask);
			}

			// some effect here:
			if (Paintball.getInstance().effects) {
				Location loc = entity.getLocation();
				World world = entity.getWorld();
				for (int i = 1; i <= 8; i++) {
					world.playEffect(loc, Effect.SMOKE, i);
					world.playEffect(loc, Effect.MOBSPAWNER_FLAMES, i);
				}
			}

			entity.remove();

			super.dispose(removeFromGadgetHandlerTracking);
		}

		@Override
		public boolean isSimiliar(Entity entity) {
			return entity.getEntityId() == this.entity.getEntityId();
		}

		@Override
		public boolean isSimiliar(Location location) {
			return false;
		}
	}
}
