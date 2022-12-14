/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.Rank;
import de.blablubbabc.paintball.statistics.general.GeneralStat;
import de.blablubbabc.paintball.statistics.player.PlayerStat;
import de.blablubbabc.paintball.statistics.player.PlayerStats;
import de.blablubbabc.paintball.utils.Sounds;
import de.blablubbabc.paintball.utils.Translator;

public class ShopManager {

	public final ItemStack item = Paintball.getInstance().weaponManager.setMeta(new ItemStack(Material.BOOK));

	private Paintball plugin;
	private ShopGood[] goods;
	private List<ShopMenu> shops = new ArrayList<ShopMenu>();

	public ShopManager(Paintball plugin) {
		this.plugin = plugin;

		// init shop goods:
		goods = new ShopGood[plugin.shopGoods.size()];
		for (int i = 0; i < plugin.shopGoods.size(); i++) {
			goods[i] = new ShopGood(plugin.shopGoods.get(i));
		}

		// init shops:
		shops.add(new ShopMenu(plugin, goods));

		// register shop listener:
		plugin.getServer().getPluginManager().registerEvents(new ShopListener(plugin), plugin);
	}

	public ShopGood[] getGoods() {
		return goods;
	}

	public void shutdown() {
		for (ShopMenu shop : shops) {
			shop.shutdown();
		}
	}

	// for now, as there is only 1 shop
	public ShopMenu getShopMenu() {
		return shops.get(0);
	}

	public ShopMenu getShopMenu(UUID playerId) {
		for (ShopMenu shop : shops) {
			if (shop.isSelecting(playerId)) return shop;
		}
		return null;
	}

	// return true, if good was successfully bought, else false
	public boolean handleBuy(Player player, ShopGood good, boolean closeInventory) {
		if (player == null || good == null) return false;

		UUID playerId = player.getUniqueId();
		Match match = plugin.matchManager.getMatch(player);
		if (match != null && match.isSurvivor(player)) {
			if (good.isEmpty()) {
				player.sendMessage(Translator.getString("GOOD_NOT_AVAILABLE"));
				return false;
			}
			// check rank:
			Rank rank = plugin.rankManager.getRank(playerId);
			boolean admin = (player.isOp() || player.hasPermission("paintball.admin"));
			if (good.getNeededRank() > rank.getRankIndex() && !(admin && plugin.ranksAdminBypassShop)) {
				Map<String, String> vars = new HashMap<String, String>();
				vars.put("rank", plugin.rankManager.getRankByIndex(good.getNeededRank()).getName());
				player.sendMessage(Translator.getString("GOOD_INSUFFICIENT_RANK", vars));
				return false;
			}

			if (player.getInventory().firstEmpty() == -1) {
				player.sendMessage(Translator.getString("INVENTORY_FULL"));
				return false;
			}

			PlayerStats stats = plugin.playerManager.getPlayerStats(playerId);
			// stats even exist for this player ?
			if (stats == null) {
				Map<String, String> vars = new HashMap<String, String>();
				vars.put("player", player.getName());
				player.sendMessage(Translator.getString("PLAYER_NOT_FOUND", vars));
				return false;
			}

			// item
			ItemStack item = good.getItemStack();
			int price = good.getPrice();
			int amount = item.getAmount();
			if (!plugin.happyhour) {
				int cash = stats.getStat(PlayerStat.MONEY);
				if (cash < price) {
					player.sendMessage(Translator.getString("NOT_ENOUGH_MONEY"));
					return false;
				}

				// reduce players money:
				stats.addStat(PlayerStat.MONEY, -price);
				stats.addStat(PlayerStat.MONEY_SPENT, price);
				stats.saveAsync();

				// let match stats know:
				match.onBuying(playerId, price);

				Map<GeneralStat, Integer> gStats = new HashMap<GeneralStat, Integer>();
				gStats.put(GeneralStat.MONEY_SPENT, price);
				plugin.statsManager.addGeneralStats(gStats);
			}

			// close item menu
			if (plugin.shopCloseMenuOnPurchase) player.closeInventory();

			// give item
			player.getInventory().addItem(item);
			Sounds.playEquipLoadout(player);
			player.updateInventory();

			Map<String, String> vars = new HashMap<String, String>();
			vars.put("amount", String.valueOf(amount));
			vars.put("good", good.getName());
			if (!plugin.happyhour) vars.put("price", String.valueOf(price));
			else vars.put("price", Translator.getString("FOR_FREE"));
			player.sendMessage(Translator.getString("YOU_BOUGHT", vars));

			// hand update
			if (plugin.airstrike || plugin.orbitalstrike) {
				plugin.weaponManager.onItemHeld(player, item);
			}

			return true;
		} else {
			player.sendMessage(Translator.getString("SHOP_ONLY_WHILE_PLAYING"));
			return false;
		}
	}
}
