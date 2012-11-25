package me.blablubbabc.paintball;

import java.util.HashMap;

import org.bukkit.Material;

public class ShopGood {
	/*goodsDef.add("10-Balls-332-15");
		goodsDef.add("50-Balls-332-65");
		goodsDef.add("100-Balls-332-120");
		goodsDef.add("1-Grenade-344-20");
		goodsDef.add("1-Airstrike-280-100");*/
	private Paintball plugin;
	private String name = "empty";
	private Integer amount = 0;
	private Material material = Material.AIR;
	private Integer id = 0;
	private Integer price = 0;
	private String slot = "empty";
	private boolean empty = false;

	public ShopGood(String slot, Paintball plugin) {
		this.plugin = plugin;
		String[] split = slot.split("-");
		if(split.length != 4) {
			this.empty = true;
		} else {
			this.name = split[1];
			this.amount = isNumber(split[0]);
			this.id = isNumber(split[2]);
			this.price = isNumber(split[3]);
			
			if(this.amount == null || this.id == null || this.price == null || this.name == null) {
				this.empty = true;
			}
			else if(amount < 0 || id < 0 || price < 0 || this.name.isEmpty()) {
				this.empty = true;
			}
			else {
				this.material = Material.getMaterial(this.id);
				if(this.material == null) {
					this.empty = true;
				} else {
					HashMap<String, String> vars = new HashMap<String, String>();
					vars.put("amount", split[0]);
					vars.put("good", split[1]);
					vars.put("price", split[3]);
					this.slot = this.plugin.t.getString("SHOP_GOOD", vars);;
				}
			}
		}
		if(this.empty) this.slot = this.plugin.t.getString("SHOP_EMPTY");
	}
	
	private Integer isNumber(String s) {
		try {
			int a = Integer.parseInt(s);
			return a;
		}catch(Exception e) {
			return null;
		}
	}
	
	public boolean isEmpty() {
		return this.empty;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Material getMaterial() {
		return this.material;
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
