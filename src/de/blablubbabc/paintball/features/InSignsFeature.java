package de.blablubbabc.paintball.features;

import java.text.DecimalFormat;
import de.blablubbabc.insigns.Changer;
import de.blablubbabc.insigns.InSigns;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.utils.Translator;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class InSignsFeature {
	private final Paintball plugin;
	private final InSigns insigns;
	private final DecimalFormat format;
	
	public InSignsFeature(Plugin insignsPlugin, Paintball pbPlugin) {
		plugin = pbPlugin;
		insigns = (InSigns) insignsPlugin;
		format = new DecimalFormat("####.##");
		
		//Create changers for all player stats
		for(final String stat : plugin.sql.sqlPlayers.statsList) {
			String s = stat;
			if(s.equals("teamattacks")) s = "ta";
			else if(s.equals("hitquote")) s = "hq";
			else if(s.equals("airstrikes")) s = "as";
			else if(s.equals("money_spent")) s = "spent";
			
			insigns.addChanger(new Changer("[PB_"+s.toUpperCase()+"]", "paintball.admin") {

				@Override
				public String getValue(Player player, Location location) {
					String playerName = player.getName();
					if(!plugin.sql.isConnected()) return Translator.getString("NOT_CONNECTED");
					else if(plugin.pm.exists(playerName)) {
						if(stat.equals("hitquote") || stat.equals("kd")) {
							float statF = ((float)plugin.pm.getStats(playerName).get(stat)) / 100;
							return format.format(statF);
						} else return "" + plugin.pm.getStats(playerName).get(stat);
					}
					else return Translator.getString("NOT_FOUND");
				}

			});
			
			// rank changers:
			insigns.addChanger(new Changer("[PB_R_"+s.toUpperCase()+"]", "paintball.admin") {

				@Override
				public String getValue(Player player, Location location) {
					String playerName = player.getName();
					if (!plugin.sql.isConnected())
						return Translator.getString("NOT_CONNECTED");
					else if (plugin.pm.exists(playerName)) {
						return "" + plugin.stats.getRank(playerName, stat);
					} else
						return Translator.getString("NOT_FOUND");
				}

			});
		}
		
		// Create additional changer for default rank (points):
		insigns.addChanger(new Changer("[PB_RANK]", "paintball.admin") {

			@Override
			public String getValue(Player player, Location location) {
				String playerName = player.getName();
				if (!plugin.sql.isConnected())
					return Translator.getString("NOT_CONNECTED");
				else if (plugin.pm.exists(playerName)) {
					return "" + plugin.stats.getRank(playerName, "points");
				} else
					return Translator.getString("NOT_FOUND");
			}

		});
		
	}

}
