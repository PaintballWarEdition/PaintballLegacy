package de.blablubbabc.paintball.extras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


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

public class Flashbang {

	public final static ItemStack item = ItemManager.setMeta(new ItemStack(Material.GHAST_TEAR));
	
	private static HashMap<String, ArrayList<Flashbang>> nades = new HashMap<String, ArrayList<Flashbang>>();
	
	public static void registerNade(Item nade, String shooterName, Origin source) {
		ArrayList<Flashbang> pnades = nades.get(shooterName);
		if (pnades == null) {
			pnades = new ArrayList<Flashbang>();
			nades.put(shooterName, pnades);
		}
		pnades.add(new Flashbang(shooterName, nade, source));
	}

	public static Flashbang getNade(int id, String shooterName, boolean remove) {
		ArrayList<Flashbang> pnades = nades.get(shooterName);
		if (pnades == null)
			return null;
		Flashbang nade = getNadeFromList(pnades, id);
		if (remove && nade != null) {
			if (pnades.remove(nade)) {
				if (pnades.size() == 0) nades.remove(shooterName);
			}
		}
		return nade;
	}
	
	private static Flashbang getNadeFromList(ArrayList<Flashbang> pnades, int id) {
		for (Flashbang nade : pnades) {
			if (nade.getId() == id)
				return nade;
		}
		return null;
	}
	
	public static void clear() {
		for (String playerName : nades.keySet()) {
			ArrayList<Flashbang> pnades = new ArrayList<Flashbang>(nades.get(playerName));
			for (Flashbang g : pnades) {
				g.remove();
			}
		}
		nades.clear();
	}

	private final Item entity;
	private final String shooterName;
	private final Origin source;

	public Flashbang(String shooterName, Item entity, Origin source) {
		this.entity = entity;
		this.shooterName = shooterName;
		this.source = source;
	}

	int getId() {
		return entity.getEntityId();
	}
	
	public Origin getSource() {
		return source;
	}
	
	public void explode(Location location) {
		location.getWorld().createExplosion(location, -1F);
		Player player = Paintball.instance.getServer().getPlayerExact(shooterName);
		if (player != null) {
			Match match = Paintball.instance.mm.getMatch(player);
			if (match != null) {
				List<Entity> near = entity.getNearbyEntities(Paintball.instance.flashRange, Paintball.instance.flashRange, Paintball.instance.flashRange);
				for (Entity e : near) {
					if (e.getType() == EntityType.PLAYER) {
						Player p = (Player) e;
						Match m = Paintball.instance.mm.getMatch(p);
						if (match == m && match.enemys(player, p)) {
							p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Paintball.instance.flashDuration, 3), true);
							p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, Paintball.instance.flashDuration, 3), true);
						}
						
					}
				}
			}
		}
	}
	
	void remove() {
		entity.remove();
	}

}
