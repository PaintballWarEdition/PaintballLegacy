package de.blablubbabc.paintball.extras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Origin;
import de.blablubbabc.paintball.Paintball;

public class Concussion {

	public final static ItemStack item = ItemManager.setMeta(new ItemStack(Material.SPIDER_EYE));
	private static int next = 0;
	
	public static void init() {
		
	}
	
	public static int getNext() {
		return ++next;
	}
	
	private static Map<String, ArrayList<Concussion>> nades = new HashMap<String, ArrayList<Concussion>>();
	
	public static void registerNade(Item nade, String shooterName, Origin source) {
		ArrayList<Concussion> pnades = nades.get(shooterName);
		if (pnades == null) {
			pnades = new ArrayList<Concussion>();
			nades.put(shooterName, pnades);
		}
		pnades.add(new Concussion(shooterName, nade, source));
	}

	public static boolean isNade(int id) {
		for (ArrayList<Concussion> pnades : nades.values()) {
			if (getNadeFromList(pnades, id) != null) return true; 
		}
		return false;
	}
	
	public static Concussion getNade(int id, String shooterName, boolean remove) {
		ArrayList<Concussion> pnades = nades.get(shooterName);
		if (pnades == null)
			return null;
		Concussion nade = getNadeFromList(pnades, id);
		if (remove && nade != null) {
			if (pnades.remove(nade)) {
				if (pnades.size() == 0) nades.remove(shooterName);
			}
		}
		return nade;
	}
	
	private static Concussion getNadeFromList(ArrayList<Concussion> pnades, int id) {
		for (Concussion nade : pnades) {
			if (nade.getId() == id)
				return nade;
		}
		return null;
	}
	
	public static void clear() {
		for (String playerName : nades.keySet()) {
			ArrayList<Concussion> pnades = new ArrayList<Concussion>(nades.get(playerName));
			for (Concussion g : pnades) {
				g.remove();
			}
		}
		nades.clear();
		next = 0;
	}

	private final Item entity;
	private final String shooterName;
	private final Origin source;

	public Concussion(String shooterName, Item entity, Origin source) {
		this.entity = entity;
		this.shooterName = shooterName;
		this.source = source;
		
		Paintball.instance.getServer().getScheduler().runTaskLater(Paintball.instance, new Runnable() {
			
			@Override
			public void run() {
				explode();
			}
		}, 20L * Paintball.instance.concussionTimeUntilExplosion);
	}

	int getId() {
		return entity.getEntityId();
	}
	
	public Origin getSource() {
		return source;
	}
	
	public void explode() {
		if (!entity.isDead() && entity.isValid()) {
			Location location = entity.getLocation();
			// EFFECTS
			// small explosion
			location.getWorld().createExplosion(location, -1F);
			/*
			// TODO
			// firework particles:
			// effects:
			FireworkEffect effectWhite = FireworkEffect.builder().withColor(Color.WHITE).withFlicker().with(Type.BURST).build();
			FireworkEffect effectSilver = FireworkEffect.builder().withColor(Color.SILVER).withFlicker().with(Type.BALL).build();
			
			for (int i = 0; i < 5; i++) {
				int x = Utils.random.nextInt(5) - 2;
				int y = Utils.random.nextInt(5) - 2;
				int z = Utils.random.nextInt(5) - 2;
				
				Block block = location.add(1, 1, 1).getBlock();
				if (block.getType() == Material.AIR) {
					Firework firework = location.getWorld().spawn(location, Firework.class);
					FireworkMeta meta = (FireworkMeta) firework.getFireworkMeta();
					
					meta.addEffects(effectWhite, effectSilver);
					meta.setPower(1);
					firework.setFireworkMeta(meta);	
				}
			}*/
			
			
			// blindness to near enemies:
			Player player = Paintball.instance.getServer().getPlayerExact(shooterName);
			if (player != null) {
				Match match = Paintball.instance.matchManager.getMatch(player);
				if (match != null) {
					List<Entity> near = entity.getNearbyEntities(Paintball.instance.concussionRange, Paintball.instance.concussionRange, Paintball.instance.concussionRange);
					for (Entity e : near) {
						if (e.getType() == EntityType.PLAYER) {
							Player p = (Player) e;
							Match m = Paintball.instance.matchManager.getMatch(p);
							if (match == m && match.enemys(player, p)) {
								p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * Paintball.instance.concussionSlownessDuration, 3), true);
								p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * Paintball.instance.concussionConfusionDuration, 3), true);
								p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * Paintball.instance.concussionBlindnessDuration, 3), true);
							}
							
						}
					}
				}
			}
		}
		getNade(entity.getEntityId(), shooterName, true);
		entity.remove();
	}
	
	void remove() {
		entity.remove();
	}

}
