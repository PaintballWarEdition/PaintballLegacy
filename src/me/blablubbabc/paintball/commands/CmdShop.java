package me.blablubbabc.paintball.commands;

import java.util.LinkedList;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import me.blablubbabc.paintball.Paintball;

public class CmdShop {
	private Paintball plugin;
	
	private LinkedList<String> goods;
	
	public CmdShop(Paintball pl) {
		plugin = pl;
		goods = new LinkedList<String>();
		for(String s : plugin.shopGoods) {
			goods.add(s);
		}
	}
	
	public boolean command(CommandSender sender, String[] args) {
		if(sender instanceof Player) {
			if(!sender.isOp() && !sender.hasPermission("paintball.admin") && !sender.hasPermission("paintball.general")) {
				sender.sendMessage(plugin.t.getString("NO_PERMISSION"));
				return true;
			}
			Player player = (Player) sender;
			if(!plugin.shop) {
				player.sendMessage(plugin.gray + "Shop is inactive right now. :( See you later, alligator!");
				return true;
			}
			if(args.length == 1) {
				//Goods-List
				player.sendMessage(plugin.dark_green+"$$$$$"+plugin.yellow+""+ plugin.bold+" Paintball-Shop "+plugin.dark_green+"$$$$$");
				player.sendMessage("");
				int i = 1;
				for(String s : goods) {
					String msg = plugin.gold+String.valueOf(i)+plugin.gray+" : "+transformGood(s);
					if(player.hasPermission("paintball.shop.not"+String.valueOf(i)) && !player.isOp() && !player.hasPermission("paintball.admin")) msg = msg.concat(" "+plugin.red+"X");
					player.sendMessage(msg);
					i++;
				}
				player.sendMessage("");
				player.sendMessage(plugin.gold+"Buy with /pb shop [id]");
				return true;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			} else if(args.length == 2) {
				if(plugin.mm.getMatch(player) == null) {
					player.sendMessage(plugin.gray+"The paintball-shop is only available while playing.");
					return true;
				}
				if(isNumber(args[1]) != null && isNumber(args[1]) > 0 && isNumber(args[1]) <= goods.size()) {
					if(transformGood(goods.get(isNumber(args[1])-1)).equalsIgnoreCase("empty") || (player.hasPermission("paintball.shop.not"+String.valueOf(isNumber(args[1]))) && !player.isOp() && !player.hasPermission("paintball.admin")) ) {
						player.sendMessage(plugin.gray+"This good is not available.");
						return true;
					}
					String[] split = goods.get(isNumber(args[1])-1).split("-");
					int price = Integer.parseInt(split[2]);
					int amount = Integer.parseInt(split[0]);
					int cash = (Integer) plugin.pm.getStats(player.getName()).get("money");
					if(cash < price) {
						player.sendMessage(plugin.gray+"$ Not enough cash $ :(");
						return true;
					}
					if(player.getInventory().firstEmpty() == -1) {
						player.sendMessage(plugin.gray+"Your inventory is full!");
						return true;
					}
					//money
					plugin.pm.addMoney(player.getName(), -price);
					plugin.stats.addMoney(price);
					//items
					if(split[1].equalsIgnoreCase("ball") || split[1].equalsIgnoreCase("balls")) player.getInventory().addItem(new ItemStack(Material.SNOW_BALL, amount));
					if(split[1].equalsIgnoreCase("grenade") || split[1].equalsIgnoreCase("grenades")) player.getInventory().addItem(new ItemStack(Material.EGG, amount));
					if(split[1].equalsIgnoreCase("airstrike") || split[1].equalsIgnoreCase("airstrikes")) player.getInventory().addItem(new ItemStack(Material.STICK, amount));
					
					player.sendMessage(plugin.green+"You bought "+amount+" "+split[1]+" for "+price+" $");
					
					return true;
				} else {
					player.sendMessage(plugin.red+"Invalid ID");
					return true;
				}
			}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else {
			plugin.log("This command cannot be used in console.");
			return true;
		}
		return false;
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private String transformGood(String s) {
		String good = "";
		String[] split = s.split("-");
		if(split.length != 3) {
			return "empty";
		}
		if(isNumber(split[0]) == null || isNumber(split[2]) == null) {
			return "empty";
		}
		if(isNumber(split[0]) < 0 || isNumber(split[2]) < 0) {
			return "empty";
		}
		
		good += plugin.yellow+split[0]+" ";
		
		if(split[1].equalsIgnoreCase("ball") || split[1].equalsIgnoreCase("balls")) {
			if(isNumber(split[0]) == 1) {
				good += "Ball";
			} else {
				good += "Balls";
			}
		} else if(split[1].equalsIgnoreCase("grenade") || split[1].equalsIgnoreCase("grenades")) {
			if(isNumber(split[0]) == 1) {
				good +="Grenade";
			} else {
				good += "Grenades";
			}
			if(!plugin.grenades) {
				return "empty";
			}
		} else if(split[1].equalsIgnoreCase("airstrike") || split[1].equalsIgnoreCase("airstrikes")) {
			if(isNumber(split[0]) == 1) {
				good += "Airstrike";
			} else {
				good += "Airstrikes";
			}
			if(!plugin.airstrike) {
				return "empty";
			}
		} else {
			return "empty";
		}
		
		good += ": " +plugin.dark_green+ split[2] + "$";
		return good;
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
