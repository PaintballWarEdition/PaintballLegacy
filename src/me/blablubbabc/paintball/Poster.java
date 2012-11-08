package me.blablubbabc.paintball;

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

public class Poster {
	private final Paintball plugin;

	private static final String URL = "http://blablubbabc.de/paintball/serverlist/post.php";
	private final String serverid;

	public Poster(Paintball plugin) {
		this.plugin = plugin;
		serverid = plugin.serverid;
		plugin.logBlank("--------- Checking version ----------");
		if(plugin.versioncheck) {
			try {
				post();
			} catch (IOException e) {
				plugin.log(e.getMessage());
			}
		}
		else {
			plugin.log("You denied version checking. :(");
			plugin.log("If you want to be informed about a new version of paintball");
			plugin.log("-> enable it in the config.");
		}
		plugin.logBlank("--------- ---------------- ----------");
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
		int numberp = 0;
		int numberj = 0;
		try {
			for(String name : plugin.pm.getAllPlayerNames()) {
				numberj++;
				if(plugin.pm.getStats(name).get("rounds") > 0) numberp++;
			}
		} catch (Exception e) {
			numberp = 0;
		}
		encodeDataPair(data, "everplayed", Integer.toString(numberp));
		encodeDataPair(data, "everjoined", Integer.toString(numberj));
		encodeDataPair(data, "autolobby", Boolean.toString(plugin.autoLobby));
		encodeDataPair(data, "list", Boolean.toString(plugin.serverlist));

		// Create the url
		URL url = new URL(URL);

		// Connect to the website
		URLConnection connection;
		connection = url.openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);

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

		if (response == null) {
			throw new IOException(response); 
		} else {
			//version check:
				if(!description.getVersion().equals(response)) {
					plugin.log("There is a new version of paintball available: "+response);
					plugin.log("Download at the bukkit dev page.");
				}else{
					plugin.log("You are running the latest version. :)");
				}
		}
	}

	private static void encodeDataPair(final StringBuilder buffer, final String key, final String value) throws UnsupportedEncodingException {
		buffer.append('&').append(encode(key)).append('=').append(encode(value));
	}

	private static String encode(final String text) throws UnsupportedEncodingException {
		return URLEncoder.encode(text, "UTF-8");
	}

}
