package de.blablubbabc.paintball;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;

import de.blablubbabc.paintball.utils.Translator;

public class Origin {
	
	public Origin() {
		
	}
	
	public String getKillMessage(String killerName, String victimName, ChatColor killerColor, ChatColor victimColor, String feedColorCode) {
		// return default frag message:
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("killer", killerName);
		vars.put("killer_color", killerColor.toString());
		vars.put("target", victimName);
		vars.put("target_color", victimColor.toString());
		vars.put("feed_color", Paintball.instance.feeder.getFeedColor());
		
		return Translator.getString("WEAPON_FEED_DEFAULT", vars);
	}
	
}