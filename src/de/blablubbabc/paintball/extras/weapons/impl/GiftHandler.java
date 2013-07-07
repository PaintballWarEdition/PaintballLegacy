package de.blablubbabc.paintball.extras.weapons.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.extras.weapons.Gift;
import de.blablubbabc.paintball.extras.weapons.WeaponHandler;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class GiftHandler extends WeaponHandler {
	
	private final Map<String, Long> wishes = new HashMap<String, Long>();
	private long time;
	
	public GiftHandler(Paintball plugin, int customItemTypeID, boolean useDefaultType) {
		super(plugin, customItemTypeID, useDefaultType);
		time = 1000 * 60 * Paintball.instance.wishesDelay;
	}
	
	@Override
	protected int getDefaultItemTypeID() {
		return Material.CHEST.getId();
	}

	@Override
	protected ItemStack setItemMeta(ItemStack itemStack) {
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(Translator.getString("WEAPON_GIFT"));
		itemStack.setItemMeta(meta);
		return itemStack;
	}

	@Override
	protected void onInteract(PlayerInteractEvent event, Match match) {
		final Player player = event.getPlayer();
		ItemStack itemInHand = player.getItemInHand();
		
		if (plugin.giftsEnabled && itemInHand.isSimilar(getItem())) {
			plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
				
				@Override
				public void run() {
					unwrapGift(player);
				}
			});
		}
	}
	
	private void setWishes(String player) {
		wishes.put(player, System.currentTimeMillis());
	}
	
	private boolean alreadyWished(String player) {
		updateWishes();
		return wishes.containsKey(player);
	}
	
	private void updateWishes() {
		List<String> names = new ArrayList<String>();
		for(String n : wishes.keySet()) {
			names.add(n);
		}
		for(String player: names) {
			if((wishes.get(player) + time) < System.currentTimeMillis()) wishes.remove(player);
		}
	}
	
	public void giveGift(Player player, int amount, boolean all) {
		if(all) player.sendMessage(Translator.getString("ALL_RECEIVED_GIFT"));
		else player.sendMessage(Translator.getString("RECEIVED_GIFT")) ;
		
		player.getInventory().addItem(plugin.weaponManager.setMeta(new ItemStack(getItem().getType(), amount)));
		/*if(player.getInventory().firstEmpty() != -1) {
			player.getInventory().addItem(new ItemStack(Material.CHEST, amount));
		} else {
			plugin.t.getString("INVENTORY_FULL");
		}*/
	}
	
	@SuppressWarnings("deprecation")
	private void unwrapGift(Player player) {
		player.playSound(player.getEyeLocation(), Sound.LEVEL_UP, 0.5F, 1F);
		//remove chest from hand
		ItemStack i = player.getItemInHand();
		if (i.getAmount() <= 1) {
			player.setItemInHand(null);
		} else {
			i.setAmount(i.getAmount() - 1);
			player.setItemInHand(i);
		}
		if (!Paintball.instance.gifts.isEmpty()) {
			//gift:
			double r = (Utils.random.nextInt(1000) / 10);
			double chance = 0.0;
			for (Gift g : Paintball.instance.gifts) {
				chance += (g.getChance() * Paintball.instance.giftChanceFactor);
				if (r < chance) {
					player.sendMessage(ChatColor.GREEN + g.getMessage());
					ItemStack item = plugin.weaponManager.setMeta(g.getItem(true));
					player.getInventory().addItem(item);
					
					// item in hand update
					plugin.weaponManager.onItemHeld(player);
					
					player.updateInventory();
					break;
				}
			}
		}
		//wishes
		String name = player.getName();
		if (Paintball.instance.bWishes && !alreadyWished(name)) {
			player.sendMessage(Paintball.instance.wishes);
			setWishes(name);
		}
	}
	
	public void transferGift(Player goodGuy, Player receiver) {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("from", goodGuy.getDisplayName());
		vars.put("to", receiver.getDisplayName());
		receiver.sendMessage(Translator.getString("RECEIVED_GIFT_FROM", vars)) ;
		goodGuy.sendMessage(Translator.getString("GAVE_GIFT_TO", vars)) ;
		
		receiver.getInventory().addItem(plugin.weaponManager.setMeta(new ItemStack(getItemTypeID(), 1)));
		
		ItemStack i = goodGuy.getItemInHand();
		if (i.getAmount() <= 1) {
			goodGuy.setItemInHand(null);
		} else {
			i.setAmount(i.getAmount() - 1);
			goodGuy.setItemInHand(i);
		}
	}
	
}
