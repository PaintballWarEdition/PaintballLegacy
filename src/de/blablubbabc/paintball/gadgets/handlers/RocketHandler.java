package de.blablubbabc.paintball.gadgets.handlers;

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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

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
	
	public RocketHandler(int customItemTypeID, boolean useDefaultType) {
		super(customItemTypeID, useDefaultType);
	}
	
	public Rocket createRocket(Match match, Player player, Entity rocket, Origin origin) {
		return new Rocket(gadgetManager, match, player, rocket, origin);
	}

	@Override
	protected int getDefaultItemTypeID() {
		return Material.DIODE.getId();
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
		if (event.getAction() == Action.PHYSICAL || !Paintball.instance.rocket) return;
		Player player = event.getPlayer();
		String playerName = player.getName();
		ItemStack itemInHand = player.getItemInHand();
		if (itemInHand == null) return;
		
		if (itemInHand.isSimilar(getItem())) {
			if (gadgetManager.getMatchGadgetCount(match) < Paintball.instance.rocketMatchLimit) {
				if (gadgetManager.getPlayerGadgetCount(match, playerName) < Paintball.instance.rocketPlayerLimit) {
					
					World world = player.getWorld();
					Vector direction = player.getLocation().getDirection().normalize();
					Location spawnLoc = player.getEyeLocation();
					
					world.playSound(spawnLoc, Sound.SILVERFISH_IDLE, 2.0F, 1F);
					Fireball rocket = (Fireball) world.spawnEntity(spawnLoc, EntityType.FIREBALL);
					rocket.setIsIncendiary(false);
					rocket.setYield(0F);
					rocket.setShooter(player);
					rocket.setVelocity(direction.multiply(Paintball.instance.rocketSpeedMulti));
					
					createRocket(match, player, rocket, Origin.ROCKET);
					
					if (itemInHand.getAmount() <= 1) {
						player.setItemInHand(null);
					} else {
						itemInHand.setAmount(itemInHand.getAmount() - 1);
						player.setItemInHand(itemInHand);
					}
					Utils.updatePlayerInventoryLater(Paintball.instance, player);
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
		if (Paintball.instance.rocket && projectile.getType() == EntityType.FIREBALL) {
			Gadget rocketGadget = gadgetManager.getGadget(projectile, match, shooter.getName());
			if (rocketGadget != null) {
				Rocket rocket = (Rocket) rocketGadget;
				rocket.explode();
			}
		}
	}
	
	@Override
	public void cleanUp(Match match, String playerName) {
		gadgetManager.cleanUp(match, playerName);
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
			super(gadgetManager, match, player.getName(), origin);
			
			this.entity = rocket;
			this.player = player;
			this.lives = Paintball.instance.rocketRange * 10;
			tick();
		}

		private void tick() {
			tickTask = Paintball.instance.getServer().getScheduler().runTaskLater(Paintball.instance, new Runnable() {

				@Override
				public void run() {
					if (entity.isValid() && lives > 0) {
						lives--;

						// some effects:
						if (Paintball.instance.effects) {
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

					final Ball ball = Paintball.instance.weaponManager.getBallHandler().createBall(match, player, snowball, getOrigin());
					
					Vector v2 = v.clone();
					v2.setX(v.getX() + Utils.random.nextDouble() - Utils.random.nextDouble());
					v2.setY(v.getY() + Utils.random.nextDouble() - Utils.random.nextDouble());
					v2.setZ(v.getZ() + Utils.random.nextDouble() - Utils.random.nextDouble());
					snowball.setVelocity(v2.normalize());
					
					Paintball.instance.getServer().getScheduler().runTaskLater(Paintball.instance, new Runnable() {

						@Override
						public void run() {
							ball.dispose(true);
						}
					}, (long) Paintball.instance.rocketTime);
				}
			}
			
			// remove from tracking:
			dispose(true);
		}

		@Override
		public void dispose(boolean removeFromGadgetHandlerTracking) {
			if (tickTask != -1) {
				Paintball.instance.getServer().getScheduler().cancelTask(tickTask);
			}
			
			// some effect here:
			if (Paintball.instance.effects) {
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

		@Override
		public Origin getOrigin() {
			return Origin.ROCKET;
		}
	}

}
