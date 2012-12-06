package me.blablubbabc.paintball.commands;

import java.util.HashMap;
import java.util.LinkedList;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.blablubbabc.paintball.Match;
import me.blablubbabc.paintball.Paintball;
import me.blablubbabc.paintball.ShopGood;

public class CmdShop {
	private Paintball plugin;
	private LinkedList<ShopGood> goods;
	
	public CmdShop(Paintball pl) {
		plugin = pl;
		goods = new LinkedList<ShopGood>();
		for(String s : plugin.shopGoods) {
			goods.add(new ShopGood(s, pl));
		}
	}
	
	public boolean command(CommandSender sender, String[] args) {
		if(sender instanceof Player) {
			//PERMISSION CHECK
			if(!plugin.noPerms) {
				if(!sender.isOp() && !sender.hasPermission("paintball.admin") && !sender.hasPermission("paintball.general")) {
					sender.sendMessage(plugin.t.getString("NO_PERMISSION"));
					return true;
				}
			}
			// else anyone is allowed..
			
			Player player = (Player) sender;
			if(!plugin.shop) {
				player.sendMessage(plugin.t.getString("SHOP_INACTIVE"));
				return true;
			}
			if(args.length == 1) {
				//Goods-List
				player.sendMessage(plugin.t.getString("SHOP_HEADER"));
				player.sendMessage("");
				if(plugin.happyhour) player.sendMessage(plugin.t.getString("HAPPYHOUR"));
				int i = 1;
				HashMap<String, String> vars = new HashMap<String, String>();
				for(ShopGood good : goods) {
					vars.put("id", String.valueOf(i));
					vars.put("good", good.getSlot());
					String msg = plugin.t.getString("SHOP_ENTRY", vars);
					if(player.hasPermission("paintball.shop.not"+String.valueOf(i)) && !player.isOp() && !player.hasPermission("paintball.admin")) msg = msg.concat(" "+plugin.red+"X");
					player.sendMessage(msg);
					i++;
				}
				player.sendMessage("");
				player.sendMessage(plugin.t.getString("SHOP_BUY"));
				plugin.stats.sendCash(player, player.getName());
				return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			} else if(args.length == 2) {
				//Kaufen in der lobby während match aber tot:
				Match match = plugin.mm.getMatch(player);
				if(match != null && match.isSurvivor(player)) {
					Integer id = isNumber(args[1]);
					if(id != null && id > 0 && id <= goods.size()) {
						ShopGood good = goods.get(id-1);
						if(good.isEmpty() || (player.hasPermission("paintball.shop.not"+String.valueOf(id)) && !player.isOp() && !player.hasPermission("paintball.admin")) ) {
							player.sendMessage(plugin.t.getString("GOOD_NOT_AVAILABLE"));
							return true;
						}
						int price = good.getPrice();
						int amount = good.getAmount();
						if(!plugin.happyhour) {
							int cash = (Integer) plugin.pm.getStats(player.getName()).get("money");
							if(cash < price) {
								player.sendMessage(plugin.t.getString("NOT_ENOUGH_MONEY"));
								return true;
							}
						}
						
						if(player.getInventory().firstEmpty() == -1) {
							player.sendMessage(plugin.t.getString("INVENTORY_FULL"));
							return true;
						}
						//money
						if(!plugin.happyhour) {
							HashMap<String, Integer> pStats = new HashMap<String, Integer>();
							pStats.put("money", -price);
							pStats.put("money_spent", price);
							plugin.pm.addStats(player.getName(), pStats);
							plugin.stats.addGeneralStats(pStats);
						}
						//item
						player.getInventory().addItem(good.getItemStack());
						
						HashMap<String, String> vars = new HashMap<String, String>();
						vars.put("amount", String.valueOf(amount));
						vars.put("good", good.getName());
						if(!plugin.happyhour) vars.put("price", String.valueOf(price));
						else vars.put("price", plugin.t.getString("FOR_FREE"));
						player.sendMessage(plugin.t.getString("YOU_BOUGHT", vars));
						
						return true;
					} else {
						player.sendMessage(plugin.t.getString("INVALID_ID"));
						return true;
					}
				} else {
					player.sendMessage(plugin.t.getString("SHOP_ONLY_WHILE_PLAYING"));
					return true;
				}
			}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else {
			plugin.log(plugin.t.getString("COMMAND_NOT_AS_CONSOLE"));
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
