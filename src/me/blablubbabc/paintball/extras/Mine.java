package me.blablubbabc.paintball.extras;

import java.util.ArrayList;
import me.blablubbabc.paintball.Match;
import me.blablubbabc.paintball.Paintball;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public class Mine {
	private static ArrayList<Mine> mines = new ArrayList<Mine>();

	public static synchronized void addMine(Mine mine) {
		mines.add(mine);
	}

	public static synchronized void removeMine(Mine mine) {
		mines.remove(mine);
	}

	public static synchronized Mine isMine(Block mine) {
		for (Mine m : mines) {
			if (m.block.equals(mine)) {
				return m;
			}
		}
		return null;
	}

	public static synchronized ArrayList<Mine> getMines(Match match) {
		ArrayList<Mine> list = new ArrayList<Mine>();
		for (Mine m : mines) {
			if (m.match.equals(match)) {
				list.add(m);
			}
		}
		return list;
	}

	public static synchronized ArrayList<Mine> getMines(Player player) {
		ArrayList<Mine> list = new ArrayList<Mine>();
		for (Mine m : mines) {
			if (m.player.equals(player)) {
				list.add(m);
			}
		}
		return list;
	}

	public final Block block;
	public final Location loc;
	public final Player player;
	public final Match match;
	public final Paintball plugin;

	private int tickTask = -1;
	private boolean exploded = false;

	public Mine(Player player, Block mine, Match match, Paintball plugin) {
		this.block = mine;
		this.loc = block.getLocation();
		this.match = match;
		this.player = player;
		this.plugin = plugin;
		addMine(this);
		tick();
	}

	private void tick() {
		tickTask = plugin.getServer().getScheduler()
				.scheduleSyncDelayedTask(this.plugin, new Runnable() {

					@Override
					public void run() {
						if (!exploded) {
							if (block.getType() == Material.FLOWER_POT) {
								if (plugin.effects) {
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
				if (loc.distance(p.getLocation()) < plugin.mineRange
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

	public synchronized void explode(final boolean effect) {
		if (!exploded) {
			exploded = true;
			if (tickTask != -1)
				plugin.getServer().getScheduler().cancelTask(tickTask);
			if (block.getType() == Material.FLOWER_POT)
				block.setType(Material.AIR);
			removeMine(this);

			if (effect) {
				// some effect here:
				if (plugin.effects) {
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
				for (Vector v : directions()) {
					moveExpSnow(loc.getWorld().spawn(loc, Snowball.class), v,
							player, plugin);
				}
			}
		}
	}

	private static void moveExpSnow(final Snowball s, Vector v, Player player,
			Paintball plugin) {
		s.setShooter(player);
		Vector v2 = v;
		v2.setX(v.getX() + Math.random() - Math.random());
		v2.setY(v.getY() + Math.random() - Math.random());
		v2.setZ(v.getZ() + Math.random() - Math.random());
		s.setVelocity(v2.normalize().multiply(0.5));
		plugin.getServer().getScheduler()
				.scheduleSyncDelayedTask(plugin, new Runnable() {

					@Override
					public void run() {
						if (!s.isDead() || s.isValid())
							s.remove();
					}
				}, (long) plugin.mineTime);
	}

	private static ArrayList<Vector> directions() {
		ArrayList<Vector> vectors = new ArrayList<Vector>();
		// alle Richtungen
		vectors.add(new Vector(1, 0, 0));
		vectors.add(new Vector(0, 1, 0));
		vectors.add(new Vector(0, 0, 1));
		vectors.add(new Vector(1, 1, 0));
		vectors.add(new Vector(1, 0, 1));
		vectors.add(new Vector(0, 1, 1));
		vectors.add(new Vector(0, 0, 0));
		vectors.add(new Vector(1, 1, 1));
		vectors.add(new Vector(-1, -1, -1));
		vectors.add(new Vector(-1, 0, 0));
		vectors.add(new Vector(0, -1, 0));
		vectors.add(new Vector(0, 0, -1));
		vectors.add(new Vector(-1, -1, 0));
		vectors.add(new Vector(-1, 0, -1));
		vectors.add(new Vector(0, -1, -1));
		vectors.add(new Vector(1, -1, 0));
		vectors.add(new Vector(1, 0, -1));
		vectors.add(new Vector(0, 1, -1));
		vectors.add(new Vector(-1, 1, 0));
		vectors.add(new Vector(-1, 0, 1));
		vectors.add(new Vector(0, -1, 1));
		vectors.add(new Vector(1, 1, -1));
		vectors.add(new Vector(1, -1, 1));
		vectors.add(new Vector(-1, 1, 1));
		vectors.add(new Vector(1, -1, -1));
		vectors.add(new Vector(-1, 1, -1));
		vectors.add(new Vector(-1, -1, 1));

		return vectors;
	}

}
