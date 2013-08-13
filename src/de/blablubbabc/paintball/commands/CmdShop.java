package de.blablubbabc.paintball.commands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.Rank;
import de.blablubbabc.paintball.shop.ShopGood;
import de.blablubbabc.paintball.utils.KeyValuePair;
import de.blablubbabc.paintball.utils.Log;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;


public class CmdShop {
	private Paintball plugin;
	
	public CmdShop(Paintball pl) {
		plugin = pl;
		
	}
	
	public boolean command(CommandSender sender, String[] args, boolean ignoreShopDisabled) {
		if (sender instanceof Player) {
			//PERMISSION CHECK
			if (!plugin.noPerms) {
				if (!sender.isOp() && !sender.hasPermission("paintball.admin") && !sender.hasPermission("paintball.general")) {
					sender.sendMessage(Translator.getString("NO_PERMISSION"));
					return true;
				}
			}
			
			Player player = (Player) sender;
			String playerName = player.getName();
			
			if (!plugin.shop && !ignoreShopDisabled) {
				player.sendMessage(Translator.getString("SHOP_INACTIVE"));
				return true;
			}
			if (args.length == 1) {
				//Goods-List
				player.sendMessage(Translator.getString("SHOP_HEADER"));
				player.sendMessage("");
				if (plugin.happyhour) player.sendMessage(Translator.getString("HAPPYHOUR"));
				Map<String, String> vars = new HashMap<String, String>();
				boolean admin = (player.isOp() || player.hasPermission("paintball.admin"));
				Rank rank = plugin.rankManager.getRank(playerName);
				String mark = Translator.getString("SHOP_INSUFFICIENT_RANK_MARK");
				ShopGood[] goods = plugin.shopManager.getGoods();
				for (int i = 0; i < goods.length; i++) {
					vars.put("id", String.valueOf(i+1));
					vars.put("good", goods[i].getSlot());
					String msg = Translator.getString("SHOP_ENTRY", vars);
					// check rank:
					if(goods[i].getNeededRank() > rank.getRankIndex() && !(admin && plugin.ranksAdminBypassShop)) msg = msg.concat(" " + mark);
					//if(player.hasPermission("paintball.shop.not"+String.valueOf(i)) && !admin) msg = msg.concat(" "+plugin.red+"X");
					player.sendMessage(msg);
				}
				player.sendMessage("");
				player.sendMessage(Translator.getString("SHOP_INSUFFICIENT_RANK_MARK_EXPLANATION", new KeyValuePair("mark", mark)));
				player.sendMessage(Translator.getString("SHOP_BUY"));
				plugin.statsManager.sendCash(player, player.getName());
				return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			} else if(args.length == 2) {
				//Kaufen in der lobby während match aber tot:
				Match match = plugin.matchManager.getMatch(player);
				if(match != null && match.isSurvivor(player)) {
					Integer id = Utils.parseInteger(args[1]);
					ShopGood[] goods = plugin.shopManager.getGoods();
					if(id != null && id > 0 && id <= goods.length) {
						ShopGood good = goods[id - 1];
						plugin.shopManager.handleBuy(player, good, false);
						return true;
					} else {
						player.sendMessage(Translator.getString("INVALID_ID"));
						return true;
					}
				} else {
					player.sendMessage(Translator.getString("SHOP_ONLY_WHILE_PLAYING"));
					return true;
				}
			}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else {
			Log.info(Translator.getString("COMMAND_NOT_AS_CONSOLE"));
			return true;
		}
		return false;
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
