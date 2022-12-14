/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.gadgets.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.gadgets.Gift;
import de.blablubbabc.paintball.gadgets.WeaponHandler;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class GiftHandler extends WeaponHandler {

	private final Map<String, Long> wishes = new HashMap<String, Long>();
	private long time;

	public GiftHandler() {
		this(null);
	}

	public GiftHandler(Material customItemType) {
		super("Gift", customItemType, null);
		time = 1000 * 60 * Paintball.getInstance().wishesDelay;
	}

	@Override
	protected Material getDefaultItemType() {
		return Material.CHEST;
	}

	@Override
	protected ItemStack setItemMeta(ItemStack itemStack) {
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(Translator.getString("GIFT_ITEM"));
		itemStack.setItemMeta(meta);
		return itemStack;
	}

	@Override
	protected void onInteract(PlayerInteractEvent event, Match match) {
		final Player player = event.getPlayer();
		PlayerInventory playerInventory = player.getInventory();
		ItemStack itemInHand = playerInventory.getItemInMainHand();
		if (itemInHand == null) return;

		if (Paintball.getInstance().giftsEnabled && itemInHand.isSimilar(getItem())) {
			Paintball.getInstance().getServer().getScheduler().runTask(Paintball.getInstance(), new Runnable() {

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
		for (String n : wishes.keySet()) {
			names.add(n);
		}
		for (String player : names) {
			if ((wishes.get(player) + time) < System.currentTimeMillis()) wishes.remove(player);
		}
	}

	public void giveGift(Player player, int amount, boolean all) {
		if (all) player.sendMessage(Translator.getString("ALL_RECEIVED_GIFT"));
		else player.sendMessage(Translator.getString("RECEIVED_GIFT"));

		player.getInventory().addItem(Paintball.getInstance().weaponManager.setMeta(new ItemStack(getItem().getType(), amount)));
		/*if(player.getInventory().firstEmpty() != -1) {
			player.getInventory().addItem(new ItemStack(Material.CHEST, amount));
		} else {
			plugin.t.getString("INVENTORY_FULL");
		}*/
	}

	private void unwrapGift(Player player) {
		player.playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 1F);
		// remove chest from hand
		PlayerInventory playerInventory = player.getInventory();
		ItemStack itemInHand = playerInventory.getItemInMainHand();
		if (itemInHand.getAmount() <= 1) {
			playerInventory.setItemInMainHand(null);
		} else {
			itemInHand.setAmount(itemInHand.getAmount() - 1);
			playerInventory.setItemInMainHand(itemInHand);
		}
		if (!Paintball.getInstance().gifts.isEmpty()) {
			// gift:
			double r = (Utils.random.nextInt(1000) / 10);
			double chance = 0.0;
			for (Gift g : Paintball.getInstance().gifts) {
				chance += (g.getChance() * Paintball.getInstance().giftChanceFactor);
				if (r < chance) {
					player.sendMessage(ChatColor.GREEN + g.getMessage());
					ItemStack item = Paintball.getInstance().weaponManager.setMeta(g.getItem(true));
					player.getInventory().addItem(item);

					// item in hand update
					Paintball.getInstance().weaponManager.onItemHeld(player, item);
					break;
				}
			}
		}
		Utils.updatePlayerInventoryLater(Paintball.getInstance(), player);

		// wishes
		String name = player.getName();
		if (Paintball.getInstance().bWishes && !alreadyWished(name)) {
			player.sendMessage(Paintball.getInstance().wishes);
			setWishes(name);
		}
	}

	public void transferGift(Player goodGuy, Player receiver) {
		Map<String, String> vars = new HashMap<>();
		vars.put("from", goodGuy.getDisplayName());
		vars.put("to", receiver.getDisplayName());
		receiver.sendMessage(Translator.getString("RECEIVED_GIFT_FROM", vars));
		goodGuy.sendMessage(Translator.getString("GAVE_GIFT_TO", vars));

		receiver.getInventory().addItem(Paintball.getInstance().weaponManager.setMeta(new ItemStack(getItemType(), 1)));

		PlayerInventory goodGuyInventory = goodGuy.getInventory();
		ItemStack goodGuyItemInHand = goodGuyInventory.getItemInMainHand();
		if (goodGuyItemInHand.getAmount() <= 1) {
			goodGuyInventory.setItemInMainHand(null);
		} else {
			goodGuyItemInHand.setAmount(goodGuyItemInHand.getAmount() - 1);
			goodGuyInventory.setItemInMainHand(goodGuyItemInHand);
		}
		Utils.updatePlayerInventoryLater(Paintball.getInstance(), goodGuy);
	}

	@Override
	public void cleanUp(Match match, UUID playerId) {
		// nothing to do here
	}

	@Override
	public void cleanUp(Match match) {
		// nothing to do here
	}
}
