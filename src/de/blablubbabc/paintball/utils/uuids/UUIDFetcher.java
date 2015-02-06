/**
 * Thanks to evilmidget38 for this class.
 */
package de.blablubbabc.paintball.utils.uuids;

import com.google.common.base.Charsets;

import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.utils.Log;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.Callable;

public class UUIDFetcher implements Callable<Map<String, UUID>> {

	private static int PROFILES_PER_REQUEST = 100;
	private static final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
	private final JSONParser jsonParser = new JSONParser();
	private final List<String> names;
	private final boolean rateLimiting;

	public UUIDFetcher(List<String> names, boolean rateLimiting) {
		this.names = names;
		this.rateLimiting = rateLimiting;
	}

	public UUIDFetcher(List<String> names) {
		this(names, true);
	}

	@Override
	public Map<String, UUID> call() throws Exception {
		Map<String, UUID> uuids = new HashMap<String, UUID>();

		// use available local uuids first:
		if (Paintball.instance.uuidUseLocalPlayerData) {
			Log.info("Searching player uuids in local server data ...");
			for (OfflinePlayer offlinePlayer : Bukkit.getServer().getOfflinePlayers()) {
				if (offlinePlayer.getName() != null && offlinePlayer.getUniqueId() != null) {
					uuids.put(offlinePlayer.getName(), offlinePlayer.getUniqueId());
				}
			}

			for (int i = 0; i < names.size(); i++) {
				String name = names.get(i);
				UUID uuid = uuids.get(name);
				if (uuid != null) {
					names.remove(i--);
				}
			}
		} else {
			Log.info("NOT searching player uuids in local server data.");
		}

		// take UUIDCollector into account:
		if (Paintball.instance.uuidUseUUIDCollector) {
			Log.info("Searching player uuids in UUIDCollector data ...");
			File uuidCollectorDir = new File(Paintball.instance.getDataFolder().getParentFile(), "UUIDCollector");
			if (uuidCollectorDir.exists()) {
				File uuidCollectorFile = new File(uuidCollectorDir, "config.yml");
				YamlConfiguration uuidCollectorConfig = YamlConfiguration.loadConfiguration(uuidCollectorFile);
				for (int i = 0; i < names.size(); i++) {
					String name = names.get(i);
					List<String> playerUUIDs = uuidCollectorConfig.getStringList(name);
					if (playerUUIDs == null || playerUUIDs.isEmpty()) continue;

					UUID uuid = null;
					try {
						uuid = UUID.fromString(playerUUIDs.get(0)); // prefer first uuid
					} catch (IllegalArgumentException e) {
						Log.warning("UUIDCollector data seems to contain invalid uuid data ('" + playerUUIDs.get(0) + "') for player '" + name + "'.");
					}

					if (uuid != null) {
						uuids.put(name, uuid);
						names.remove(i--);
					}
				}
			} else {
				Log.warning("Couldn't find UUIDCollector directory. Skipping that.");
			}
		} else {
			Log.info("Not using UUIDCollector.");
		}

		int remaining = names.size();
		int counter = 0;

		// for online mode, request remaining uuids from Mojang:
		if (Paintball.instance.uuidOnlineMode) {
			Log.info("Requesting remaining online mode uuids from Mojang ...");
			int requests = (int) Math.ceil(names.size() / PROFILES_PER_REQUEST);
			for (int i = 0; i < requests; i++) {
				boolean retry = false;
				JSONArray array = null;
				int uuidRequsts = 0;

				do {
					retry = false;
					array = null;
					HttpURLConnection connection = createConnection();
					List<String> sublist = names.subList(i * PROFILES_PER_REQUEST, Math.min((i + 1) * PROFILES_PER_REQUEST, names.size()));
					uuidRequsts = sublist.size();
					String body = JSONArray.toJSONString(sublist);
					writeBody(connection, body);

					try {
						array = (JSONArray) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
					} catch (Exception e) {
						// check if we have run into Mojang's rate limit:
						if (e.getMessage().contains("429")) {
							retry = true;
							// if this is our first attempt, the batch size must be too large..
							if (i == 0 && PROFILES_PER_REQUEST > 1) {
								Log.info("Batch size " + PROFILES_PER_REQUEST + " seems too large. Trying again with smaller batch size ...");
								PROFILES_PER_REQUEST = Math.max(PROFILES_PER_REQUEST - 5, 1);
							} else {
								// otherwise wait a short time and then retry:
								Log.info("We have run into Mojang's rate limit, because of sending uuid request to fast. Trying again in 30 seconds ...");
								Thread.sleep(30000);
							}
						} else {
							// possibly crucial error.. retrying anyways so the already fetched data isn't wasted in case this is a temporary issue:
							retry = true;
							Log.info("Error: " + e.getMessage() + ". Trying again in 30 seconds ...");
							Thread.sleep(30000);
							// throw e;
						}
					}
				} while (retry);

				for (Object profile : array) {
					JSONObject jsonProfile = (JSONObject) profile;
					String id = (String) jsonProfile.get("id");
					String name = (String) jsonProfile.get("name");
					UUID uuid = UUIDFetcher.getUUID(id);
					uuids.put(name, uuid);
				}
				counter += uuidRequsts;
				Log.info("Progress: " + counter + " / " + remaining);
				if (rateLimiting) {
					Thread.sleep(200L);
				}
			}
		} else {
			// for offline mode, generate remaining uuids:
			Log.info("Generating remaining offline mode uuids ...");
			for (int i = 0; i < names.size(); i++) {
				String name = names.get(i);
				UUID uuid = java.util.UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8));
				uuids.put(name, uuid);
			}
		}

		return uuids;
	}

	private static void writeBody(HttpURLConnection connection, String body) throws Exception {
		OutputStream stream = connection.getOutputStream();
		stream.write(body.getBytes());
		stream.flush();
		stream.close();
	}

	private static HttpURLConnection createConnection() throws Exception {
		URL url = new URL(PROFILE_URL);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setDoOutput(true);
		return connection;
	}

	private static UUID getUUID(String id) {
		return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
	}

	public static byte[] toBytes(UUID uuid) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
		byteBuffer.putLong(uuid.getMostSignificantBits());
		byteBuffer.putLong(uuid.getLeastSignificantBits());
		return byteBuffer.array();
	}

	public static UUID fromBytes(byte[] array) {
		if (array.length != 16) {
			throw new IllegalArgumentException("Illegal byte array length: " + array.length);
		}
		ByteBuffer byteBuffer = ByteBuffer.wrap(array);
		long mostSignificant = byteBuffer.getLong();
		long leastSignificant = byteBuffer.getLong();
		return new UUID(mostSignificant, leastSignificant);
	}
}
