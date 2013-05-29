package de.blablubbabc.paintball.shop;

import java.util.HashMap;
import org.bukkit.inventory.ItemStack;

import de.blablubbabc.paintball.utils.Translator;

public class ShopGoodOld {
	private String name = "empty";
	private Integer amount = 0;
	private ItemStack itemstack = null;
	private Integer id = 0;
	private Short subid = 0;
	private Integer price = 0;
	private String slot = "empty";
	private boolean empty = false;

	public ShopGoodOld(String slot) {
		String[] split = slot.split("-");
		if(split.length != 5) {
			this.empty = true;
		} else {
			this.name = split[1];
			this.amount = isInteger(split[0]);
			this.id = isInteger(split[2]);
			this.subid = isShort(isInteger(split[3]).intValue());
			this.price = isInteger(split[4]);
			
			if(this.amount == null || this.id == null || this.subid == null || this.price == null || this.name == null) {
				this.empty = true;
			}
			else if(amount < 0 || id < 0 || subid < 0 || price < 0 || this.name.isEmpty()) {
				this.empty = true;
			} else {
				this.itemstack = new ItemStack(id, amount, subid);
				
				HashMap<String, String> vars = new HashMap<String, String>();
				vars.put("amount", split[0]);
				vars.put("good", split[1]);
				vars.put("price", split[4]);
				this.slot = Translator.getString("SHOP_GOOD", vars);
			}
		}
		if(this.empty) this.slot = Translator.getString("SHOP_EMPTY");
	}
	
	private Integer isInteger(String s) {
		try {
			Integer a = Integer.parseInt(s);
			return a;
		} catch(Exception e) {
			return null;
		}
	}
	
	private Short isShort(int i) {
		if(i > Short.MAX_VALUE || i < Short.MIN_VALUE) return null;
		else return (short) i;
	}
	
	public boolean isEmpty() {
		return this.empty;
	}
	
	public String getName() {
		return this.name;
	}
	
	public ItemStack getItemStack(boolean clone) {
		return (clone ? this.itemstack.clone() : this.itemstack);
	}
	
	public String getSlot() {
		return this.slot;
	}
	
	public Integer getPrice() {
		return this.price;
	}
	
	public Integer getAmount() {
		return this.amount;
	}
	
	public Integer getId() {
		return this.id;
	}
	
}
