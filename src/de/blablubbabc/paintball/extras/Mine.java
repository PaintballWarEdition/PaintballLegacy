package de.blablubbabc.paintball.extras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.utils.Utils;

public class Mine {
	
	public final static ItemStack item = ItemManager.setMeta(new ItemStack(Material.FLOWER_POT_ITEM));
	
	private static int mineCounter = 0;
	private static Map<String, ArrayList<Mine>> mines = new HashMap<String, ArrayList<Mine>>();
	
	public static void init() {
		
	}
	
	private static void addMine(String shooterName, Mine mine) {
		ArrayList<Mine> pmines = mines.get(shooterName);
		if (pmines == null) {
			pmines = new ArrayList<Mine>();
			mines.put(shooterName, pmines);
		}
		pmines.add(mine);
		mineCounter++;
	}
	
	private static void removeMine(String shooterName, Mine mine) {
		ArrayList<Mine> pmines = mines.get(shooterName);
		if (pmines != null) {
			if (pmines.remove(mine)) {
				mineCounter--;
				if (pmines.size() == 0) mines.remove(shooterName);
			}
		}
	}
	
	public static int getMineCountMatch() {
		return mineCounter;
	}
	
	public static ArrayList<Mine> getMines(String playerName) {
		ArrayList<Mine> pmines = mines.get(playerName);
		if (pmines == null) {
			pmines = new ArrayList<Mine>();
		}
		return pmines;
	}
	
	public static Mine getIsMine(Block mine) {
		for (ArrayList<Mine> pmines : mines.values()) {
			for (Mine m : pmines) {
				if (m.block.equals(mine)) {
					return m;
				}
			}
		}
		return null;
	}

	public final Block block;
	public final BlockState oldState;
	public final Location loc;
	public final Player player;
	public final Match match;

	private int tickTask = -1;
	private boolean exploded = false;

	public Mine(Player player, Block mine, BlockState oldState, Match match) {
		this.block = mine;
		this.oldState = oldState;
		this.loc = block.getLocation();
		this.match = match;
		this.player = player;
		addMine(player.getName(), this);
		tick();
	}

	private void tick() {
		tickTask = Paintball.instance.getServer().getScheduler()
				.scheduleSyncDelayedTask(Paintball.instance, new Runnable() {

					@Override
					public void run() {
						if (!exploded) {
							if (block.getType() == Material.FLOWER_POT) {
								if (Paintball.instance.effects) {
									// effect
									for (Player p : match.getAllPlayer()) {
										if (match.isSurvivor(p)) {
											Location ploc = p.getLocation();
											if (ploc.getWorld().equals(
													block.getWorld())) {
												double dist = ploc
														.distance(loc);
												if (dist < 15) {
													float vol = (float) (0.18 - (dist * 0.012));
													p.playSound(loc,
															Sound.CLICK, vol,
															2F);
												}
											}
										}
									}
								}
								if (nearEnemy()) {
									explode(true);
								} else {
									tick();
								}
							} else {
								explode(false);
							}
						}
					}
				}, 10L);
	}

	private boolean nearEnemy() {
		for (Player p : match.getEnemyTeam(player)) {
			if (match.isSurvivor(p)) {
				if (loc.distance(p.getLocation()) < Paintball.instance.mineRange
						&& canSeeMine(p)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean canSeeMine(Player player) {
		Vector dir = (player.getEyeLocation().toVector().subtract(loc.toVector())).normalize();
		BlockIterator iterator = new BlockIterator(loc.getWorld(),
				loc.toVector(), dir, 0, (int) Math.ceil(player.getEyeLocation().distance(loc)));
		
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

	public void explode(final boolean effect) {
		if (!exploded) {
			exploded = true;
			if (tickTask != -1)
				Paintball.instance.getServer().getScheduler().cancelTask(tickTask);
			// remove flower pot and reset to old block:
			oldState.update(true);
			
			final String playerName = player.getName();
			removeMine(playerName, this);

			if (effect) {
				// some effect here:
				if (Paintball.instance.effects) {
					// effect
					loc.getWorld().playEffect(loc, Effect.SMOKE, 1);
					loc.getWorld().playEffect(loc, Effect.SMOKE, 2);
					loc.getWorld().playEffect(loc, Effect.SMOKE, 3);
					loc.getWorld().playEffect(loc, Effect.SMOKE, 4);
					loc.getWorld().playEffect(loc, Effect.SMOKE, 5);
					loc.getWorld().playEffect(loc, Effect.SMOKE, 6);
					loc.getWorld().playEffect(loc, Effect.SMOKE, 7);
					loc.getWorld().playEffect(loc, Effect.SMOKE, 8);
					loc.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 4);
				}

				loc.getWorld().createExplosion(loc, 0.0F);
				for (Vector v : Utils.getUpVectors()) {
					final Snowball s = loc.getWorld().spawn(loc, Snowball.class);
					s.setShooter(player);
					Ball.registerBall(s, playerName, Origin.MINE);
					
					Vector v2 = v.clone();
					v2.setX(v.getX() + Math.random() - Math.random());
					v2.setY(v.getY() + Math.random() - Math.random());
					v2.setZ(v.getZ() + Math.random() - Math.random());
					s.setVelocity(v2.normalize().multiply(0.5));
					Paintball.instance.getServer().getScheduler()
							.scheduleSyncDelayedTask(Paintball.instance, new Runnable() {

								@Override
								public void run() {
									if (!s.isDead() || s.isValid())
										Ball.getBall(s.getEntityId(), playerName, true);
										s.remove();
								}
							}, (long) Paintball.instance.mineTime);
				}
			}
		}
	}

}
