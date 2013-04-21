package me.blablubbabc.paintball.extras;

import me.blablubbabc.paintball.Paintball;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemManager {
	
	public static ItemStack setMeta(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		Material type = item.getType();
		// set meta:
		switch (type) {
		case SNOW_BALL:
			meta.setDisplayName(Paintball.instance.t.getString("WEAPON_PAINTBALL"));
			break;

		case EGG:
			meta.setDisplayName(Paintball.instance.t.getString("WEAPON_GRENADE"));
			break;
			
		case DIODE:
			meta.setDisplayName(Paintball.instance.t.getString("WEAPON_ROCKET"));
			break;

		case CARROT_STICK:
			meta.setDisplayName(Paintball.instance.t.getString("WEAPON_SNIPER"));
			break;

		case SPECKLED_MELON:
			meta.setDisplayName(Paintball.instance.t.getString("WEAPON_SHOTGUN"));
			break;

		case STICK:
			meta.setDisplayName(Paintball.instance.t.getString("WEAPON_AIRSTRIKE"));
			break;

		case FLOWER_POT_ITEM:
			meta.setDisplayName(Paintball.instance.t.getString("WEAPON_MINE"));
			break;

		case PUMPKIN:
			meta.setDisplayName(Paintball.instance.t.getString("WEAPON_TURRET"));
			break;

		case CHEST:
			meta.setDisplayName(Paintball.instance.t.getString("WEAPON_GIFT"));
			break;
			
		case STONE_AXE:
			meta.setDisplayName(Paintball.instance.t.getString("WEAPON_PUMPGUN"));
			break;
			
		case WOOL:
			byte data = item.getData().getData();
			if (data == DyeColor.RED.getWoolData()) {
				meta.setDisplayName(Paintball.instance.t.getString("TEAM_RED"));
			} else if (data == DyeColor.BLUE.getWoolData()) {
				meta.setDisplayName(Paintball.instance.t.getString("TEAM_BLUE"));
			} else if (data == DyeColor.YELLOW.getWoolData()) {
				meta.setDisplayName(Paintball.instance.t.getString("TEAM_SPECTATOR"));
			}
			break;

		default:
			break;
		}
		
		// give:
		item.setItemMeta(meta);
		return item;
	}
}
