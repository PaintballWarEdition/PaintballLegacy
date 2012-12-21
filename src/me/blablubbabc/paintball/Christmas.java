package me.blablubbabc.paintball;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Christmas {
	private HashMap<String, Long> wishes;
	private final long time;
	private Paintball plugin;
	private Random random;
	
	public Christmas(Paintball plugin) {
		this.plugin = plugin;
		wishes = new HashMap<String, Long>();
		time = 1000*60*plugin.wishesDelay;
		this.random = new Random();
	}
	
	public synchronized void setWishes(String player) {
		wishes.put(player, System.currentTimeMillis());
	}
	
	public synchronized boolean alreadyWished(String player) {
		update();
		return wishes.containsKey(player);
	}
	
	private synchronized void update() {
		ArrayList<String> names = new ArrayList<String>();
		for(String n : wishes.keySet()) {
			names.add(n);
		}
		for(String player: names) {
			if((wishes.get(player)+time) < System.currentTimeMillis()) wishes.remove(player);
		}
	}
	
	public void receiveGift(Player player, int amount, boolean all) {
		if(all) player.sendMessage(plugin.t.getString("ALL_RECEIVED_GIFT"));
		else player.sendMessage(plugin.t.getString("RECEIVED_GIFT")) ;
		
		player.getInventory().addItem(new ItemStack(Material.CHEST, amount));
		/*if(player.getInventory().firstEmpty() != -1) {
			player.getInventory().addItem(new ItemStack(Material.CHEST, amount));
		} else {
			plugin.t.getString("INVENTORY_FULL");
		}*/
	}
	
	@SuppressWarnings("deprecation")
	public void unwrapGift(Player player) {
		//remove chest from hand
		ItemStack i = player.getItemInHand();
		if (i.getAmount() <= 1)
			player.setItemInHand(null);
		else {
			i.setAmount(i.getAmount() - 1);
			player.setItemInHand(i);
		}
		if(!plugin.gifts.isEmpty()) {
			//gift:
			double r = (random.nextInt(1000)/10);
			int chance = 0;
			for(Gift g : plugin.gifts) {
				chance += (g.chance*plugin.giftChanceFactor);
				if(r < chance) {
					player.sendMessage(ChatColor.GREEN+g.message);
					player.getInventory().addItem(g.item);
					player.updateInventory();
					break;
				}
			}
		}
		//wishes
		String name = player.getName();
		if (plugin.wishes && !plugin.christmas.alreadyWished(name)) {
			player.sendMessage(plugin.t.getString("MERRY_CHRISTMAS"));
			plugin.christmas.setWishes(name);
		}
	}
	
	public void giveGift(Player goodGuy, Player receiver) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("from", goodGuy.getDisplayName());
		vars.put("to", receiver.getDisplayName());
		receiver.sendMessage(plugin.t.getString("RECEIVED_GIFT_FROM", vars)) ;
		goodGuy.sendMessage(plugin.t.getString("GAVE_GIFT_TO", vars)) ;
		
		receiver.getInventory().addItem(new ItemStack(Material.CHEST, 1));
		
		ItemStack i = goodGuy.getItemInHand();
		if (i.getAmount() <= 1)
			goodGuy.setItemInHand(null);
		else {
			i.setAmount(i.getAmount() - 1);
			goodGuy.setItemInHand(i);
		}
	}
}
