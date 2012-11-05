package me.blablubbabc.paintball.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.blablubbabc.paintball.Match;
import me.blablubbabc.paintball.Paintball;

public class CmdShop {
	private Paintball plugin;
	private LinkedList<String> goods;
	private String empty;
	//ITEMS:
	ArrayList<String> ball;
	ArrayList<String> grenade;
	ArrayList<String> airstrike;
	
	
	public CmdShop(Paintball pl) {
		plugin = pl;
		goods = new LinkedList<String>();
		empty = plugin.t.getString("SHOP_EMPTY");
		//ITEMS:
		ball = new ArrayList<String>();
		ball.add("ball");ball.add("balls");ball.add(plugin.t.getString("BALL"));ball.add(plugin.t.getString("BALLS"));
		
		grenade = new ArrayList<String>();
		grenade.add("grenade");grenade.add("grenades");grenade.add(plugin.t.getString("GRENADE"));grenade.add(plugin.t.getString("GRENADES"));
		
		airstrike = new ArrayList<String>();
		airstrike.add("airstrike");airstrike.add("airstrikes");airstrike.add(plugin.t.getString("AIRSTRIKE"));airstrike.add(plugin.t.getString("AIRSTRIKES"));
		for(String s : plugin.shopGoods) {
			goods.add(s);
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
				int i = 1;
				HashMap<String, String> vars = new HashMap<String, String>();
				for(String s : goods) {
					vars.put("id", String.valueOf(i));
					vars.put("good", transformGood(s));
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
					if(isNumber(args[1]) != null && isNumber(args[1]) > 0 && isNumber(args[1]) <= goods.size()) {
						if(transformGood(goods.get(isNumber(args[1])-1)).equalsIgnoreCase(empty) || (player.hasPermission("paintball.shop.not"+String.valueOf(isNumber(args[1]))) && !player.isOp() && !player.hasPermission("paintball.admin")) ) {
							player.sendMessage(plugin.t.getString("GOOD_NOT_AVAILABLE"));
							return true;
						}
						String[] split = goods.get(isNumber(args[1])-1).split("-");
						int price = Integer.parseInt(split[2]);
						int amount = Integer.parseInt(split[0]);
						int cash = (Integer) plugin.pm.getStats(player.getName()).get("money");
						if(cash < price) {
							player.sendMessage(plugin.t.getString("NOT_ENOUGH_MONEY"));
							return true;
						}
						if(player.getInventory().firstEmpty() == -1) {
							player.sendMessage(plugin.t.getString("INVENTORY_FULL"));
							return true;
						}
						//money
						HashMap<String, Integer> pStats = new HashMap<String, Integer>();
						pStats.put("money", -price);
						pStats.put("money_spent", price);
						plugin.pm.addStats(player.getName(), pStats);
						plugin.stats.addGeneralStats(pStats);
						//items
						if(isItem(split[1], "ball")) player.getInventory().addItem(new ItemStack(Material.SNOW_BALL, amount));
						if(isItem(split[1], "grenade")) player.getInventory().addItem(new ItemStack(Material.EGG, amount));
						if(isItem(split[1], "airstrike")) player.getInventory().addItem(new ItemStack(Material.STICK, amount));
						
						HashMap<String, String> vars = new HashMap<String, String>();
						vars.put("amount", String.valueOf(amount));
						vars.put("good", split[1]);
						vars.put("price", String.valueOf(price));
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
	
	private boolean isItem(String source, String item) {
		if(item.equalsIgnoreCase("ball")) {	
			for(String s : ball) {
				if(source.equalsIgnoreCase(s)) return true;
			}
		}else if(item.equalsIgnoreCase("grenade")) {
			for(String s : grenade) {
				if(source.equalsIgnoreCase(s)) return true;
			}
		}else if(item.equalsIgnoreCase("airstrike")) {
			for(String s : airstrike) {
				if(source.equalsIgnoreCase(s)) return true;
			}
		}
		return false;
	}
	
	private String transformGood(String s) {
		HashMap<String, String> vars = new HashMap<String, String>();
		String slot = "";
		String[] split = s.split("-");
		if(split.length != 3) {
			return empty;
		}
		if(isNumber(split[0]) == null || isNumber(split[2]) == null) {
			return empty;
		}
		if(isNumber(split[0]) < 0 || isNumber(split[2]) < 0) {
			return empty;
		}
		
		vars.put("amount", split[0]);
		
		String good = "";
		if(isItem(split[1], "ball")) {
			if(isNumber(split[0]) == 1) {
				good = plugin.t.getString("BALL");
			} else {
				good = plugin.t.getString("BALLS");
			}
		} else if(isItem(split[1], "grenade")) {
			if(isNumber(split[0]) == 1) {
				good = plugin.t.getString("GRENADE");
			} else {
				good = plugin.t.getString("GRENADES");
			}
			if(!plugin.grenades) {
				return empty;
			}
		} else if(isItem(split[1], "airstrike")) {
			if(isNumber(split[0]) == 1) {
				good = plugin.t.getString("AIRSTRIKE");
			} else {
				good = plugin.t.getString("AIRSTRIKES");
			}
			if(!plugin.airstrike) {
				return empty;
			}
		} else {
			return empty;
		}
		vars.put("good", good);
		vars.put("price", split[2]);
		slot = plugin.t.getString("SHOP_GOOD", vars);
		return slot;
	}
	
	private Integer isNumber(String s) {
		try {
			int a = Integer.parseInt(s);
			return a;
		}catch(Exception e) {
			return null;
		}
	}
}
