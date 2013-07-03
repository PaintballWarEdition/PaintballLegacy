package de.blablubbabc.paintball.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import org.bukkit.plugin.PluginDescriptionFile;

import de.blablubbabc.paintball.Paintball;

public class Poster {
	private final Paintball plugin;

	private static final String URL = "http://blablubbabc.de/paintball/serverlist/post.php";
	private final String serverid;

	public Poster(Paintball plugin) {
		this.plugin = plugin;
		serverid = (String) plugin.serverList.get("Server.Id");
		if(plugin.versioncheck) {
			try {
				post();
			} catch (IOException e) {
				Log.info("--------- Checking version ----------");
				Log.infoWarn("Wasn't able to check version: " + e.getMessage());
				Log.info("Maybe the update-check server is down or has bad latency.");
				Log.info("--------- ---------------- ----------");
			}
		} else {
			Log.info("--------- Checking version ----------");
			Log.info("You denied version checking. :(");
			Log.info("If you want to be informed about a new version of paintball");
			Log.info("-> enable it in the config.");
			Log.info("--------- ---------------- ----------");
		}
	}

	private void post() throws IOException {
		// The plugin's description file containg all of the plugin data such as name, version, author, etc
		final PluginDescriptionFile description = plugin.getDescription();
		final String ip = InetAddress.getLocalHost().toString();

		// Construct the post data for version checking and server list
		final StringBuilder data = new StringBuilder();
		data.append(encode("serverid")).append('=').append(encode(serverid));
		//Server List Data
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
		encodeDataPair(data, "nometrics", Boolean.toString(plugin.nometrics));
		
		int numberp = plugin.playerManager.getPlayersEverPlayedCount();
		int numberj = plugin.playerManager.getPlayerCount();
		
		encodeDataPair(data, "everplayed", Integer.toString(numberp));
		encodeDataPair(data, "everjoined", Integer.toString(numberj));
		encodeDataPair(data, "autolobby", Boolean.toString(plugin.autoLobby));
		encodeDataPair(data, "list", Boolean.toString((Boolean)plugin.serverList.get("Server.List")));

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
		} else {
			// version check:
			String version = description.getVersion();
			// higher or lower version ?
			String[] splitVersion = version.split("\\.");
			String[] splitResponse = response.split("\\.");
			int maxIndex = Math.max(splitVersion.length, splitResponse.length) - 1;
			int versionValue = calcVersion(splitVersion, maxIndex);
			int responseValue = calcVersion(splitResponse, maxIndex);
			
			
			Log.info("--------- Checking version ----------");
			if (responseValue > versionValue) {
				Paintball.instance.needsUpdate = true;
				Log.infoWarn("There is a new version of paintball available: " + response);
				Log.info("Download at the bukkit dev page.");
			} else if (versionValue > responseValue) {
				Log.info("You are running a newer version. :o");
			} else {
				Log.info("You are running the latest version. :)");
			}
			Log.info("--------- ---------------- ----------");
		}
	}
	
	private int calcVersion(String[] versionSplit, int highestIndex) {
		int result = 0;
		if (versionSplit != null) {
			for (int i = highestIndex; i >= 0; i--) {
				if (i < versionSplit.length) {
					try {
						result += Integer.valueOf(versionSplit[i]) * Math.pow(10, highestIndex - i);
					} catch (NumberFormatException e) {
						continue;
					}
				}
				// else add nothing
			}
		}
		return result;
	}

	private static void encodeDataPair(final StringBuilder buffer, final String key, final String value) throws UnsupportedEncodingException {
		buffer.append('&').append(encode(key)).append('=').append(encode(value));
	}

	private static String encode(final String text) throws UnsupportedEncodingException {
		return URLEncoder.encode(text, "UTF-8");
	}

}
