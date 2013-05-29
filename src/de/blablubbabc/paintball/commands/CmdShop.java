package de.blablubbabc.paintball.commands;

import java.util.HashMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.ShopGood;
import de.blablubbabc.paintball.extras.Airstrike;
import de.blablubbabc.paintball.extras.ItemManager;
import de.blablubbabc.paintball.extras.Orbitalstrike;
import de.blablubbabc.paintball.utils.Translator;


public class CmdShop {
	private Paintball plugin;
	private ShopGood[] goods;
	
	public CmdShop(Paintball pl) {
		plugin = pl;
		goods = new ShopGood[plugin.shopGoods.size()];
		for(int i = 0; i < plugin.shopGoods.size(); i++) {
			goods[i] = new ShopGood(plugin.shopGoods.get(i));
		}
		
	}
	
	@SuppressWarnings("deprecation")
	public boolean command(CommandSender sender, String[] args) {
		if(sender instanceof Player) {
			//PERMISSION CHECK
			if(!plugin.noPerms) {
				if(!sender.isOp() && !sender.hasPermission("paintball.admin") && !sender.hasPermission("paintball.general")) {
					sender.sendMessage(Translator.getString("NO_PERMISSION"));
					return true;
				}
			}
			
			Player player = (Player) sender;
			if(!plugin.shop) {
				player.sendMessage(Translator.getString("SHOP_INACTIVE"));
				return true;
			}
			if(args.length == 1) {
				//Goods-List
				player.sendMessage(Translator.getString("SHOP_HEADER"));
				player.sendMessage("");
				if(plugin.happyhour) player.sendMessage(Translator.getString("HAPPYHOUR"));
				HashMap<String, String> vars = new HashMap<String, String>();
				//boolean admin = (player.isOp() || player.hasPermission("paintball.admin"));
				for(int i = 0; i < goods.length; i++) {
					vars.put("id", String.valueOf(i+1));
					vars.put("good", goods[i].getSlot());
					String msg = Translator.getString("SHOP_ENTRY", vars);
					//if(player.hasPermission("paintball.shop.not"+String.valueOf(i)) && !admin) msg = msg.concat(" "+plugin.red+"X");
					player.sendMessage(msg);
				}
				player.sendMessage("");
				player.sendMessage(Translator.getString("SHOP_BUY"));
				plugin.stats.sendCash(player, player.getName());
				return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			} else if(args.length == 2) {
				//Kaufen in der lobby während match aber tot:
				Match match = plugin.mm.getMatch(player);
				if(match != null && match.isSurvivor(player)) {
					Integer id = isNumber(args[1]);
					if(id != null && id > 0 && id <= goods.length) {
						ShopGood good = goods[id-1];
						//if(good.isEmpty() || (player.hasPermission("paintball.shop.not"+String.valueOf(id)) && !player.isOp() && !player.hasPermission("paintball.admin")) ) {
						if (good.isEmpty()) {
							player.sendMessage(Translator.getString("GOOD_NOT_AVAILABLE"));
							return true;
						}
						int price = good.getPrice();
						int amount = good.getAmount();
						if(!plugin.happyhour) {
							int cash = (Integer) plugin.pm.getStats(player.getName()).get("money");
							if(cash < price) {
								player.sendMessage(Translator.getString("NOT_ENOUGH_MONEY"));
								return true;
							}
						}
						
						if(player.getInventory().firstEmpty() == -1) {
							player.sendMessage(Translator.getString("INVENTORY_FULL"));
							return true;
						}
						//money
						if(!plugin.happyhour) {
							HashMap<String, Integer> pStats = new HashMap<String, Integer>();
							pStats.put("money", -price);
							pStats.put("money_spent", price);
							plugin.pm.addStatsAsync(player.getName(), pStats);
							plugin.stats.addGeneralStats(pStats);
						}
						//item
						ItemStack item = good.getItemStack();
						player.getInventory().addItem(ItemManager.setMeta(item));
						player.updateInventory();
						
						HashMap<String, String> vars = new HashMap<String, String>();
						vars.put("amount", String.valueOf(amount));
						vars.put("good", good.getName());
						if(!plugin.happyhour) vars.put("price", String.valueOf(price));
						else vars.put("price", Translator.getString("FOR_FREE"));
						player.sendMessage(Translator.getString("YOU_BOUGHT", vars));
						
						//airstrike item in hand update
						if (plugin.airstrike) {
							Airstrike.handleItemInHand(player, item);
						}
						
						if (plugin.orbitalstrike) {
							Orbitalstrike.handleItemInHand(player, item);
						}
						
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
			plugin.log(Translator.getString("COMMAND_NOT_AS_CONSOLE"));
			return true;
		}
		return false;
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private Integer isNumber(String s) {
		try {
			int a = Integer.parseInt(s);
			return a;
		}catch(Exception e) {
			return null;
		}
	}
}
