package de.blablubbabc.paintball.features;

import java.text.DecimalFormat;
import java.util.UUID;

import de.blablubbabc.insigns.InSigns;
import de.blablubbabc.insigns.SimpleChanger;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.statistics.player.PlayerStat;
import de.blablubbabc.paintball.statistics.player.PlayerStats;
import de.blablubbabc.paintball.utils.Translator;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class InSignsFeature {

	private final Paintball plugin;
	@SuppressWarnings("unused")
	private final InSigns insigns;
	private final DecimalFormat format;

	public InSignsFeature(Plugin insignsPlugin, Paintball pbPlugin) {
		plugin = pbPlugin;
		insigns = (InSigns) insignsPlugin;
		format = new DecimalFormat("####.##");

		// Create changers for all player stats
		for (final PlayerStat stat : PlayerStat.values()) {
			final String key = stat.getKey();
			String s = key;
			if (s.equals("teamattacks")) s = "ta";
			else if (s.equals("hitquote")) s = "hq";
			else if (s.equals("airstrikes")) s = "as";
			else if (s.equals("money_spent")) s = "spent";

			new SimpleChanger(Paintball.instance, "[PB_" + s.toUpperCase() + "]", "paintball.admin") {

				@Override
				public String getValue(Player player, Location signLocation, String affectedLine) {
					UUID playerUUID = player.getUniqueId();
					if (!plugin.sql.isConnected()) {
						return Translator.getString("NOT_CONNECTED");
					} else {
						PlayerStats stats = plugin.playerManager.getPlayerStats(playerUUID);
						if (stats != null) {
							Integer statValue = stats.getStat(stat);
							if (stat == PlayerStat.HITQUOTE || stat == PlayerStat.KD) {
								float statF = statValue / 100F;
								return format.format(statF);
							} else return String.valueOf(statValue);
						} else {
							return Translator.getString("NOT_FOUND");
						}
					}
				}
			};

			// rank changers:
			new SimpleChanger(Paintball.instance, "[PB_R_" + s.toUpperCase() + "]", "paintball.admin") {

				@Override
				public String getValue(Player player, Location signLocation, String affectedLine) {
					UUID playerUUID = player.getUniqueId();
					if (!plugin.sql.isConnected()) {
						return Translator.getString("NOT_CONNECTED");
					} else if (plugin.playerManager.exists(playerUUID)) {
						return String.valueOf(plugin.statsManager.getRank(playerUUID, stat));
					} else {
						return Translator.getString("NOT_FOUND");
					}
				}
			};
		}

		// Create additional changer for default rank (points):
		new SimpleChanger(Paintball.instance, "[PB_RANK]", "paintball.admin") {

			@Override
			public String getValue(Player player, Location signLocation, String affectedLine) {
				UUID playerUUID = player.getUniqueId();
				if (!plugin.sql.isConnected()) return Translator.getString("NOT_CONNECTED");
				else if (plugin.playerManager.exists(playerUUID)) {
					return String.valueOf(plugin.statsManager.getRank(playerUUID, PlayerStat.POINTS));
				} else {
					return Translator.getString("NOT_FOUND");
				}
			}
		};
	}
}