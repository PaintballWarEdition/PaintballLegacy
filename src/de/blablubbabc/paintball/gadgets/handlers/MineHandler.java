package de.blablubbabc.paintball.gadgets.handlers;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.gadgets.Gadget;
import de.blablubbabc.paintball.gadgets.GadgetManager;
import de.blablubbabc.paintball.gadgets.WeaponHandler;
import de.blablubbabc.paintball.gadgets.events.PaintballHitEvent;
import de.blablubbabc.paintball.gadgets.handlers.BallHandler.Ball;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class MineHandler extends WeaponHandler implements Listener {
	
	private GadgetManager gadgetManager = new GadgetManager();
	
	public MineHandler(int customItemTypeID, boolean useDefaultType) {
		super(customItemTypeID, useDefaultType);
		Paintball.instance.getServer().getPluginManager().registerEvents(this, Paintball.instance);
	}
	
	public Mine plantMine(Match match, Player player, Block block, Material type, BlockState oldState, Origin origin) {
		return new Mine(gadgetManager, match, player, block, type, oldState, origin);
	}
	
	@EventHandler
	public void onPaintballHit(PaintballHitEvent event) {
		if (Paintball.instance.mine) {
			Player shooter= event.getShooter();
			String shooterName = event.getShooter().getName();
			Match match = event.getMatch();
			Projectile ball = event.getProjectileHitEvent().getEntity();
			Location location = ball.getLocation();
			
			Gadget mineGadget = gadgetManager.getGadget(location, event.getMatch(), shooterName);
			if (mineGadget != null && match == mineGadget.getMatch()) {
				Mine mine = (Mine) mineGadget;
				Player owner = mine.getOwner();
				if (match.enemys(shooter, owner) || shooter.equals(owner)) {
					mine.explode();
				}
			}

			BlockIterator iterator = new BlockIterator(location.getWorld(), location.toVector(), ball.getVelocity().normalize(), 0, 2);
			while (iterator.hasNext()) {
				mineGadget = gadgetManager.getGadget(iterator.next().getLocation(), match, shooterName);
				if (mineGadget != null && match == mineGadget.getMatch()) {
					Mine mine = (Mine) mineGadget;
					Player owner = mine.getOwner();
					if (match.enemys(shooter, owner) || shooter.equals(owner)) {
						mine.explode();
					}
				}
			}
		}
	}
	
	@Override
	protected int getDefaultItemTypeID() {
		return Material.FLOWER_POT_ITEM.getId();
	}

	@Override
	protected ItemStack setItemMeta(ItemStack itemStack) {
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(Translator.getString("WEAPON_MINE"));
		itemStack.setItemMeta(meta);
		return itemStack;
	}

	@Override
	protected void onInteract(PlayerInteractEvent event, Match match) {
		
	}
	
	@Override
	protected void onBlockPlace(BlockPlaceEvent event, Match match) {
		final Block block = event.getBlockPlaced();
		if (Paintball.instance.mine && block.getType() == Material.FLOWER_POT) {
			Player player = event.getPlayer();
			ItemStack itemInHand = player.getItemInHand();
			if (itemInHand.isSimilar(getItem())) {
				String playerName = player.getName();
				if (gadgetManager.getMatchGadgetCount(match) < Paintball.instance.mineMatchLimit) {
					if (gadgetManager.getPlayerGadgetCount(match, playerName) < Paintball.instance.minePlayerLimit) {
						
						Paintball.instance.getServer().getScheduler().runTaskLater(Paintball.instance, new Runnable() {

							@Override
							public void run() {
								
								block.setType(Material.FLOWER_POT);
								block.setData((byte) 0);
							}
						}, 1L);
						
						plantMine(match, player, block, Material.FLOWER_POT, event.getBlockReplacedState(), Origin.MINE);
						
						if (itemInHand.getAmount() <= 1) {
							player.setItemInHand(null);
						} else {
							itemInHand.setAmount(itemInHand.getAmount() - 1);
							player.setItemInHand(itemInHand);
						}
						Utils.updatePlayerInventoryLater(Paintball.instance, player);
					} else {
						player.sendMessage(Translator.getString("MINE_PLAYER_LIMIT_REACHED"));
					}
				} else {
					player.sendMessage(Translator.getString("MINE_MATCH_LIMIT_REACHED"));
				}
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

	public class Mine extends Gadget {
		
		private final Player player;
		private final Block block;
		private final Material type;
		private final BlockState oldState;
		private final Location location;

		private int tickTask = -1;
		private boolean exploded = false;
		
		private Mine(GadgetManager gadgetManager, Match match, Player player, Block block, Material type, BlockState oldState, Origin origin) {
			super(gadgetManager, match, player.getName(), origin);
			
			this.player = player;
			this.block = block;
			this.type = type;
			this.oldState = oldState;
			this.location = block.getLocation();
			tick();
		}
		
		public void explode() {
			if (!exploded) {
				exploded = true;

				// some effect here:
				if (Paintball.instance.effects) {
					// effect
					location.getWorld().playEffect(location, Effect.SMOKE, 1);
					location.getWorld().playEffect(location, Effect.SMOKE, 2);
					location.getWorld().playEffect(location, Effect.SMOKE, 3);
					location.getWorld().playEffect(location, Effect.SMOKE, 4);
					location.getWorld().playEffect(location, Effect.SMOKE, 5);
					location.getWorld().playEffect(location, Effect.SMOKE, 6);
					location.getWorld().playEffect(location, Effect.SMOKE, 7);
					location.getWorld().playEffect(location, Effect.SMOKE, 8);
					location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 4);
				}

				location.getWorld().createExplosion(location, -1.0F);
				for (Vector v : Utils.getUpVectors()) {
					final Snowball snowball = location.getWorld().spawn(location, Snowball.class);
					snowball.setShooter(player);
					final Ball ball = Paintball.instance.weaponManager.getBallHandler().createBall(match, player, snowball, Origin.MINE);
					
					Vector v2 = v.clone();
					v2.setX(v.getX() + Utils.random.nextDouble() - Utils.random.nextDouble());
					v2.setY(v.getY() + Utils.random.nextDouble() - Utils.random.nextDouble());
					v2.setZ(v.getZ() + Utils.random.nextDouble() - Utils.random.nextDouble());
					snowball.setVelocity(v2.normalize().multiply(0.5));
					Paintball.instance.getServer().getScheduler().runTaskLater(Paintball.instance, new Runnable() {
						@Override
						public void run() {
							ball.dispose(true);
						}
					}, (long) Paintball.instance.mineTime);
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
			// reset to old block:
			if (oldState != null) oldState.update(true);
			
			super.dispose(removeFromGadgetHandlerTracking);
		}
		
		/**
		 * Returns the creator of this mine.
		 * 
		 * @return the creator of the mine
		 */
		public Player getOwner() {
			return player;
		}

		private void tick() {
			tickTask = Paintball.instance.getServer().getScheduler().runTaskLater(Paintball.instance, new Runnable() {

				@Override
				public void run() {
					if (!exploded) {
						if (block.getType() == type) {
							if (Paintball.instance.effects) {
								// effect
								for (Player p : match.getAllPlayer()) {
									if (match.isSurvivor(p)) {
										Location ploc = p.getLocation();
										if (ploc.getWorld().equals(block.getWorld())) {
											double dist = ploc.distance(location);
											if (dist < 15) {
												float vol = (float) (0.18 - (dist * 0.012));
												p.playSound(location, Sound.CLICK, vol, 2F);
											}
										}
									}
								}
							}
							if (nearEnemy()) {
								explode();
							} else {
								tick();
							}
						} else {
							dispose(true);
						}
					}
				}
			}, 10L).getTaskId();
		}

		private boolean nearEnemy() {
			for (Player p : match.getEnemyTeam(getOwner())) {
				if (match.isSurvivor(p)) {
					if (location.distance(p.getLocation()) < Paintball.instance.mineRange && canSeeMine(p)) {
						return true;
					}
				}
			}
			return false;
		}

		private boolean canSeeMine(Player player) {
			Vector dir = (player.getEyeLocation().toVector().subtract(location.toVector())).normalize();
			BlockIterator iterator = new BlockIterator(location.getWorld(), location.toVector(), dir, 0, (int) Math.ceil(player.getEyeLocation().distance(location)));
			
			while(iterator.hasNext()) {
				Block b = iterator.next();
				if(!b.equals(block) && b.getType() != Material.AIR) return false;
			}
			return true;
		}
		
		/*private boolean canSee(Player player, Location loc2) {
			Location loc1 = player.getLocation();
			return ((CraftWorld) loc1.getWorld()).getHandle().a(
					Vec3D.a(loc1.getX(), loc1.getY() + player.getEyeHeight(),
							loc1.getZ()),
					Vec3D.a(loc2.getX(), loc2.getY(), loc2.getZ())) == null;
		}*/

		@Override
		public boolean isSimiliar(Entity entity) {
			return false;
		}
		
		@Override
		public boolean isSimiliar(Location location) {
			return location.equals(this.location);
		}
		
	}

}
