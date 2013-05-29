package de.blablubbabc.paintball.extras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.Translator;
import de.blablubbabc.paintball.Utils;

public class Gifts {
	
	public final static ItemStack item = ItemManager.setMeta(new ItemStack(Material.CHEST));
	
	private static final Map<String, Long> wishes = new HashMap<String, Long>();
	private static long time;
	
	public static void init() {
		time = 1000*60*Paintball.instance.wishesDelay;
	}
	
	public static void setWishes(String player) {
		wishes.put(player, System.currentTimeMillis());
	}
	
	public static boolean alreadyWished(String player) {
		update();
		return wishes.containsKey(player);
	}
	
	private static void update() {
		ArrayList<String> names = new ArrayList<String>();
		for(String n : wishes.keySet()) {
			names.add(n);
		}
		for(String player: names) {
			if((wishes.get(player)+time) < System.currentTimeMillis()) wishes.remove(player);
		}
	}
	
	public static void receiveGift(Player player, int amount, boolean all) {
		if(all) player.sendMessage(Translator.getString("ALL_RECEIVED_GIFT"));
		else player.sendMessage(Translator.getString("RECEIVED_GIFT")) ;
		
		player.getInventory().addItem(ItemManager.setMeta(new ItemStack(item.getType(), amount)));
		/*if(player.getInventory().firstEmpty() != -1) {
			player.getInventory().addItem(new ItemStack(Material.CHEST, amount));
		} else {
			plugin.t.getString("INVENTORY_FULL");
		}*/
	}
	
	@SuppressWarnings("deprecation")
	public static void unwrapGift(Player player) {
		//remove chest from hand
		ItemStack i = player.getItemInHand();
		if (i.getAmount() <= 1)
			player.setItemInHand(null);
		else {
			i.setAmount(i.getAmount() - 1);
			player.setItemInHand(i);
		}
		if(!Paintball.instance.gifts.isEmpty()) {
			//gift:
			double r = (Utils.random.nextInt(1000)/10);
			double chance = 0.0;
			for(Gift g : Paintball.instance.gifts) {
				chance += (g.getChance()*Paintball.instance.giftChanceFactor);
				if(r < chance) {
					player.sendMessage(ChatColor.GREEN+g.getMessage());
					ItemStack item = ItemManager.setMeta(g.getItem(true));
					player.getInventory().addItem(item);
					//airstrike item in hand update
					if (Paintball.instance.airstrike) {
						Airstrike.handleItemInHand(player, item);
					}
					
					if (Paintball.instance.orbitalstrike) {
						Orbitalstrike.handleItemInHand(player, item);
					}
					player.updateInventory();
					break;
				}
			}
		}
		//wishes
		String name = player.getName();
		if (Paintball.instance.bWishes && !alreadyWished(name)) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', Paintball.instance.wishes));
			setWishes(name);
		}
	}
	
	public static void giveGift(Player goodGuy, Player receiver) {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("from", goodGuy.getDisplayName());
		vars.put("to", receiver.getDisplayName());
		receiver.sendMessage(Translator.getString("RECEIVED_GIFT_FROM", vars)) ;
		goodGuy.sendMessage(Translator.getString("GAVE_GIFT_TO", vars)) ;
		
		receiver.getInventory().addItem(ItemManager.setMeta(new ItemStack(item.getType(), 1)));
		
		ItemStack i = goodGuy.getItemInHand();
		if (i.getAmount() <= 1)
			goodGuy.setItemInHand(null);
		else {
			i.setAmount(i.getAmount() - 1);
			goodGuy.setItemInHand(i);
		}
	}
}
