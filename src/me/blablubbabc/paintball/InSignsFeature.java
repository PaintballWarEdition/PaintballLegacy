package me.blablubbabc.paintball;

import me.blablubbabc.insigns.Changer;
import me.blablubbabc.insigns.InSigns;

import org.bukkit.plugin.Plugin;

public class InSignsFeature {
	Paintball plugin;
	
	public InSignsFeature(Plugin insignsPlugin, Paintball pbPlugin) {
		plugin = pbPlugin;
		
		InSigns insigns = (InSigns) insignsPlugin;
		insigns.addChanger(new Changer("[PB POINTS]") {

			@Override
			public String getValue(String playerName) {
				return ""+plugin.pm.getStats(playerName).get("points");
			}

		});
	}

}