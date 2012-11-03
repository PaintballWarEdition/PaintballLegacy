package me.blablubbabc.paintball;

import me.blablubbabc.insigns.Changer;
import me.blablubbabc.insigns.InSigns;
import org.bukkit.plugin.Plugin;

public class InSignsFeature {
	private Paintball plugin;
	
	public InSignsFeature(Plugin insignsPlugin, Paintball pbPlugin) {
		plugin = pbPlugin;
		
		InSigns insigns = (InSigns) insignsPlugin;
		insigns.addChanger(new Changer("[PB POINTS]", "pbpoints") {

			@Override
			public String getValue(String playerName) {
				if(!plugin.sql.isConnected()) return "-no connection-";
				else if(plugin.pm.exists(playerName)) return ""+plugin.pm.getStats(playerName).get("points");
				else return "-not found-";
			}

		});
	}

}
