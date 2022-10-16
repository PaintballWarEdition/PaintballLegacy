/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.metrics;

import org.bstats.bukkit.Metrics;

import de.blablubbabc.paintball.Paintball;

/**
 * Plugin metrics powered by
 * <a href="https://bstats.org/plugin/bukkit/PaintballWar/16677">bStats<a/>.
 */
public class PluginMetrics {

	private static final int PLUGIN_ID = 16677;

	private final Paintball plugin;

	public PluginMetrics(Paintball plugin) {
		this.plugin = plugin;
	}

	public void onEnable() {
		if (plugin.metrics) {
			this.setupMetrics();
		}
	}

	public void onDisable() {
	}

	private void setupMetrics() {
		Metrics metrics = new Metrics(plugin, PLUGIN_ID);
		metrics.addCustomChart(new CurrentlyPlayingPlayersChart());
		metrics.addCustomChart(new TotalPlayersEverPlayedChart(plugin.playerManager));
	}
}
