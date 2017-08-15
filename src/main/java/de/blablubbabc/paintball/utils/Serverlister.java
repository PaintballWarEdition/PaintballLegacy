/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;

import de.blablubbabc.paintball.Paintball;

public class Serverlister {

	private static final String URL = "http://blablubbabc.de/paintball/serverlist/post.php";
	private final static String CONFIG_FILE = "plugins/Paintball/serverlist.yml";

	private final Paintball plugin;
	private final YamlConfiguration config;
	private final File configFile;

	// private static final int PING_INTERVAL = 10;//unused
	// CONFIG VALUES

	public Serverlister(Paintball plugin) {
		this.plugin = plugin;

		// load the config
		configFile = new File(CONFIG_FILE);
		config = YamlConfiguration.loadConfiguration(configFile);

		// setup defaults:
		Configuration defaults = new YamlConfiguration();
		defaults.set("Server.id", UUID.randomUUID().toString());
		defaults.set("Server.post enabled", true);
		defaults.set("Server.show in serverlist", true);

		// apply defaults for missing entries:
		for (Entry<String, Object> defaultValue : defaults.getValues(true).entrySet()) {
			String node = defaultValue.getKey();
			if (!config.isSet(node)) {
				config.set(node, defaultValue.getValue());
			}
		}

		// correct server-id:
		String serverid = config.getString("Server.id");
		if (!isValid(serverid)) {
			config.set("Server.id", defaults.get("Server.id"));
		}
		// save, creates defaults if missing:
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean isValid(String uuid) {
		if (uuid == null) return false;
		try {
			// we have to convert to object and back to string because the built in fromString does not have
			// good validation logic:
			UUID fromStringUUID = UUID.fromString(uuid);
			String toStringUUID = fromStringUUID.toString();
			return toStringUUID.equals(uuid);
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	public void post() {
		// post server:
		if (config.getBoolean("Server.post enabled", true)) {
			try {
				doPost();
			} catch (IOException e) {
				Log.info("--------- Serverlist ----------");
				Log.info("Wasn't able to post server: " + e.getMessage());
				Log.info("Maybe the master server is down or has bad latency.");
				Log.info("--------- ---------------- ----------");
			}
		}
	}

	private void doPost() throws IOException {
		// The plugin's description file containg all of the plugin data such as name, version, author, etc
		final PluginDescriptionFile description = plugin.getDescription();
		final String ip = InetAddress.getLocalHost().toString();

		// Construct the post data for version checking and server list
		final StringBuilder data = new StringBuilder();
		String serverid = config.getString("Server.Id", "unknown id");
		data.append(encode("serverid")).append('=').append(encode(serverid));
		// Server List Data
		encodeDataPair(data, "serverip", ip);
		encodeDataPair(data, "ip", plugin.getServer().getIp());
		encodeDataPair(data, "port", Integer.toString(plugin.getServer().getPort()));
		encodeDataPair(data, "name", plugin.getServer().getServerName());
		encodeDataPair(data, "motd", plugin.getServer().getMotd());
		encodeDataPair(data, "bukkitversion", plugin.getServer().getBukkitVersion());
		encodeDataPair(data, "slots", Integer.toString(plugin.getServer().getMaxPlayers()));
		encodeDataPair(data, "onlinemode", Boolean.toString(plugin.getServer().getOnlineMode()));
		encodeDataPair(data, "whitelist", Boolean.toString(plugin.getServer().hasWhitelist()));
		encodeDataPair(data, "pluginversion", description.getVersion());
		encodeDataPair(data, "nometrics", Boolean.toString(!plugin.metrics));

		int numberp = plugin.playerManager.getPlayersEverPlayedCount();
		int numberj = plugin.playerManager.getPlayerCount();

		encodeDataPair(data, "everplayed", Integer.toString(numberp));
		encodeDataPair(data, "everjoined", Integer.toString(numberj));
		encodeDataPair(data, "autolobby", Boolean.toString(plugin.autoLobby));
		encodeDataPair(data, "list", Boolean.toString(config.getBoolean("Server.show in serverlist")));

		// Create the url
		URL url = new URL(URL);

		// Connect to the website
		URLConnection connection;
		connection = url.openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(5000);

		// Write the data
		final OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
		writer.write(data.toString());
		writer.flush();

		// Now read the response
		final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		final String response = reader.readLine();

		// close resources
		writer.close();
		reader.close();

		if (response == null || response.startsWith("ERR")) {
			throw new IOException(response);
		} else if (response.startsWith("INFO ")) {
			if (response.length() > 5) {
				String infoMessage = response.substring(5);
				String[] lines = infoMessage.split("\\n");
				for (String line : lines) {
					Log.logColored(ChatColor.translateAlternateColorCodes('&', line));
				}
			}
		} else {
			// do nothing: version is now checked with the Updater class
		}
	}

	private static void encodeDataPair(final StringBuilder buffer, final String key, final String value) throws UnsupportedEncodingException {
		buffer.append('&').append(encode(key)).append('=').append(encode(value));
	}

	private static String encode(final String text) throws UnsupportedEncodingException {
		return URLEncoder.encode(text, "UTF-8");
	}
}
