package de.blablubbabc.paintball.shop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import de.blablubbabc.paintball.utils.Log;
import de.blablubbabc.paintball.utils.Utils;

public class ShopManager {
	private static List<ShopItem> shopitems = new ArrayList<ShopItem>(); 
	private static List<Shop> shops = new ArrayList<Shop>();
	
	private static Shop newShop(String name, ShopGood[] goods) {
		Shop shop = new Shop(name, goods);
		shops.add(shop);
		return shop;
	}
	
	public static Shop getShop(String shopName) {
		for (Shop shop : shops) {
			if (Utils.equalsIgnoreColor(shop.getName(), shopName)) return shop;
		}
		return null;
	}
	
	public static Shop getShopByPlayer(String playerName) {
		for (Shop shop : shops) {
			if (shop.isShopping(playerName)) return shop;
		}
		return null;
	}
	
	//TODO call 'init()' onEnable: early
	public static void init() {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(Utils.getPath("shops.cfg")));
		loadShopItems(config);
		Shop.init();
	}
	
	@SuppressWarnings("unchecked")
	private static void loadShopItems(YamlConfiguration config) {
		ConfigurationSection cs = config.getConfigurationSection("ShopItems");
		ConfigurationSection css;
		int id;
		int subidInt;
		Short subid;
		List<String> desc;
		for (String name : cs.getKeys(false)) {
			if (getShopItem(name) == null) {
				css = cs.getConfigurationSection(name);
				desc = (List<String>) css.getList("Description");
				css = css.getConfigurationSection("Item");
				if (css == null) {
					Log.logWarning("ShopItem '" + name + "' is missing 'Item' information! Check the 'shops.cfg'!");
					continue;
				}
				id = css.getInt("ID");
				if (id <= 0) {
					Log.logWarning("ShopItem '" + name + "' has an invalid ID! Check the 'shops.cfg'!");
					continue;
				}
				subidInt = css.getInt("SubID");
				subid = Utils.isShort(subidInt);
				if (subid == null || subid < 0) {
					Log.logWarning("ShopItem '" + name + "' has an invalid SubID! Check the 'shops.cfg'!");
					continue;
				}
				
				shopitems.add(new ShopItem(name, id, subid, desc));
			} else {
				Log.logWarning("ShopItem '" + name + "' has duplicates! Check the 'shops.cfg'!");
				continue;
			}
		}
	}
	
	public static ShopItem getShopItem(String name) {
		for (ShopItem item : shopitems) {
			if (item.matches(name)) {
				return item;
			}
		}
		return null;
	}
	
}
