package de.blablubbabc.paintball.extras;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.blablubbabc.paintball.utils.Translator;

public class ItemManager {
	
	public static void init() {
		
	}
	
	public static ItemStack setMeta(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		Material type = item.getType();
		// set meta:
		switch (type) {
		case SNOW_BALL:
			meta.setDisplayName(Translator.getString("WEAPON_PAINTBALL"));
			break;

		case EGG:
			meta.setDisplayName(Translator.getString("WEAPON_GRENADE"));
			break;
			
		case GHAST_TEAR:
			meta.setDisplayName(Translator.getString("WEAPON_FLASHBANG"));
			break;
			
		case SLIME_BALL:
			meta.setDisplayName(Translator.getString("WEAPON_GRENADE2"));
			break;
			
		case DIODE:
			meta.setDisplayName(Translator.getString("WEAPON_ROCKET"));
			break;

		case CARROT_STICK:
			meta.setDisplayName(Translator.getString("WEAPON_SNIPER"));
			break;

		case SPECKLED_MELON:
			meta.setDisplayName(Translator.getString("WEAPON_SHOTGUN"));
			break;

		case STICK:
			meta.setDisplayName(Translator.getString("WEAPON_AIRSTRIKE"));
			break;

		case BLAZE_ROD:
			meta.setDisplayName(Translator.getString("WEAPON_ORBITALSTRIKE"));
			break;

		case FLOWER_POT_ITEM:
			meta.setDisplayName(Translator.getString("WEAPON_MINE"));
			break;

		case PUMPKIN:
			meta.setDisplayName(Translator.getString("WEAPON_TURRET"));
			break;

		case CHEST:
			meta.setDisplayName(Translator.getString("WEAPON_GIFT"));
			break;
			
		case STONE_AXE:
			meta.setDisplayName(Translator.getString("WEAPON_PUMPGUN"));
			break;
			
		case WOOL:
			byte data = item.getData().getData();
			if (data == DyeColor.RED.getWoolData()) {
				meta.setDisplayName(Translator.getString("TEAM_RED"));
			} else if (data == DyeColor.BLUE.getWoolData()) {
				meta.setDisplayName(Translator.getString("TEAM_BLUE"));
			} else if (data == DyeColor.YELLOW.getWoolData()) {
				meta.setDisplayName(Translator.getString("TEAM_SPECTATOR"));
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
