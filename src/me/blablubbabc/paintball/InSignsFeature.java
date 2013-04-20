package me.blablubbabc.paintball;

import java.text.DecimalFormat;
import de.blablubbabc.insigns.Changer;
import de.blablubbabc.insigns.InSigns;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class InSignsFeature {
	private final Paintball plugin;
	private final InSigns insigns;
	
	public InSignsFeature(Plugin insignsPlugin, Paintball pbPlugin) {
		plugin = pbPlugin;
		insigns = (InSigns) insignsPlugin;
		
		//Create changers for all player stats
		for(final String stat : plugin.sql.sqlPlayers.statsList) {
			String s = stat;
			if(s.equals("teamattacks")) s = "ta";
			if(s.equals("hitquote")) s = "hq";
			if(s.equals("airstrikes")) s = "as";
			if(s.equals("money_spent")) s = "spent";
			
			insigns.addChanger(new Changer("[PB_"+s.toUpperCase()+"]", "paintball.insigns."+s) {

				@Override
				public String getValue(Player player, Location location) {
					String playerName = player.getName();
					if(!plugin.sql.isConnected()) return plugin.t.getString("NOT_CONNECTED");
					else if(plugin.pm.exists(playerName)) {
						if(stat.equals("hitquote") || stat.equals("kd")) {
							DecimalFormat dec = new DecimalFormat("####.##");
							float statF = (float)(Integer)plugin.pm.getStats(playerName).get(stat) / 100;
							return dec.format(statF);
						} else return ""+plugin.pm.getStats(playerName).get(stat);
					}
					else return plugin.t.getString("NOT_FOUND");
				}

			});
		}
	}

}
