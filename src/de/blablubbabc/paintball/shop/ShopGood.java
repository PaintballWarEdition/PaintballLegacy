package de.blablubbabc.paintball.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.blablubbabc.paintball.extras.ItemManager;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;


public class ShopGood {
	 /* goodsDef.add("10-Balls-332-0-15");
		goodsDef.add("50-Balls-332-0-65");
		goodsDef.add("100-Balls-332-0-120");
		goodsDef.add("1-Grenade-344-0-20");
		goodsDef.add("1-Airstrike-280-0-100");
		goodsDef.add("1-Turret-86-0-200");
	 */
	private String name = "empty";
	private ItemStack itemstack = null;
	private int price = 0;
	private int neededRank = 0;
	private String slot = "empty";
	private boolean empty = false;
	
	private ItemStack icon;

	public ShopGood(String slot) {
		String[] split = slot.split("-");
		if (split.length == 5 || split.length == 6) {
			this.name = split[1];
			int amount = isInteger(split[0]);
			int id = isInteger(split[2]);
			short subid = isShort(isInteger(split[3]));
			this.price = isInteger(split[4]);
			
			if (split.length == 6) {
				this.neededRank = isInteger(split[5]);
				if (this.neededRank < 0) this.neededRank = 0;
			}
			
			if(amount <= 0 || id < 0 || subid < 0 || price < 0 || this.name == null || this.name.isEmpty()) {
				this.empty = true;
			} else {
				this.itemstack = ItemManager.setMeta(new ItemStack(id, amount, subid));
				
				Map<String, String> vars = new HashMap<String, String>();
				vars.put("amount", split[0]);
				vars.put("good", split[1]);
				vars.put("price", split[4]);
				this.slot = Translator.getString("SHOP_GOOD", vars);
			}
		} else {
			this.empty = true;
		}
		
		// ICON:
		// TOD translation support
		if(this.empty) {
			this.slot = Translator.getString("SHOP_EMPTY");
			this.icon = new ItemStack(Material.SIGN_POST);
			List<String> desc = new ArrayList<String>();
			desc.add(ChatColor.RED + this.slot);
			
			this.icon = Utils.setItemMeta(this.icon, name, desc);
			
		} else {
			this.icon = itemstack.clone();
			
			List<String> desc = new ArrayList<String>();
			desc.add(ChatColor.RED + "__Price: " + ChatColor.GREEN + price + "$");
			desc.add(ChatColor.RED + "__Item:");
			
			ItemMeta meta = itemstack.getItemMeta();
			if (meta.hasDisplayName()) {
				desc.add(ChatColor.GREEN + "- " + ChatColor.WHITE + itemstack.getAmount() + "x " + ChatColor.AQUA + meta.getDisplayName());
			} else {
				desc.add(ChatColor.GREEN + "- " + ChatColor.WHITE + itemstack.getAmount() + "x " 
						+ ChatColor.AQUA + itemstack.getType().toString() + " (" + itemstack.getTypeId() + ":" + itemstack.getDurability() + ")");
			}
			
			this.icon = Utils.setItemMeta(this.icon, name, desc);
		}
		
	}
	
	private int isInteger(String s) {
		try {
			Integer a = Integer.parseInt(s);
			return a;
		} catch(Exception e) {
			return 0;
		}
	}
	
	private short isShort(int i) {
		if(i > Short.MAX_VALUE) return Short.MAX_VALUE;
		if (i < Short.MIN_VALUE) return Short.MIN_VALUE;
		return (short) i;
	}
	
	public boolean isEmpty() {
		return this.empty;
	}
	
	public String getName() {
		return this.name;
	}
	
	public ItemStack getItemStack() {
		return this.itemstack.clone();
	}
	
	public String getSlot() {
		return this.slot;
	}
	
	public int getPrice() {
		return this.price;
	}

	public int getNeededRank() {
		return neededRank;
	}
	
	public ItemStack getIcon() {
		return icon;
	}
	
	
}