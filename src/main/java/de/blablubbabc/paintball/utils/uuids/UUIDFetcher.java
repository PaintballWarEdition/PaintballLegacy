/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
/**
 * Thanks to evilmidget38 for this class.
 */
package de.blablubbabc.paintball.utils.uuids;

import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.utils.Log;

public class UUIDFetcher {

	private static int PROFILES_PER_REQUEST = 100;
	private static final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
	private final JsonParser jsonParser = new JsonParser();
	private final Gson gson = new Gson();
	private final List<String> names;
	private final boolean rateLimiting;

	public UUIDFetcher(List<String> names, boolean rateLimiting) {
		this.names = new ArrayList<String>(names);
		this.rateLimiting = rateLimiting;
	}

	public UUIDFetcher(List<String> names) {
		this(names, true);
	}

	public Map<String, UUID> searchLocal() {
		Map<String, UUID> uuids = new HashMap<String, UUID>();

		// use available local uuids first:
		if (Paintball.getInstance().uuidUseLocalPlayerData) {
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
		if (Paintball.getInstance().uuidUseUUIDCollector) {
			Log.info("Searching player uuids in UUIDCollector data ...");
			File uuidCollectorDir = new File(Paintball.getInstance().getDataFolder().getParentFile(), "UUIDCollector");
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

		return uuids;
	}

	public Map<String, UUID> fetch() throws Exception {
		Map<String, UUID> uuids = new HashMap<String, UUID>();

		// for online mode, request remaining uuids from Mojang:
		if (Paintball.getInstance().uuidOnlineMode) {
			Log.info("Requesting remaining online mode uuids from Mojang ...");
			int requests = (int) Math.ceil(names.size() / PROFILES_PER_REQUEST);
			int counter = 0;
			for (int i = 0; i < requests; i++) {
				boolean retry = false;
				JsonArray array = null;
				int uuidRequsts = 0;

				do {
					retry = false;
					array = null;
					HttpURLConnection connection = createConnection();
					List<String> sublist = names.subList(i * PROFILES_PER_REQUEST, Math.min((i + 1) * PROFILES_PER_REQUEST, names.size()));
					uuidRequsts = sublist.size();

					String body = gson.toJson(sublist);
					writeBody(connection, body);

					try {
						array = jsonParser.parse(new InputStreamReader(connection.getInputStream())).getAsJsonArray();
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
							// possibly crucial error.. retrying anyways so the already fetched data
							// isn't wasted in
							// case this is only a temporary issue:
							retry = true;
							Log.info("Error: " + e.getMessage() + ". Trying again in 30 seconds ...");
							Thread.sleep(30000);
							// throw e;
						}
					}
				} while (retry);

				for (JsonElement profile : array) {
					JsonObject jsonProfile = profile.getAsJsonObject();
					String id = jsonProfile.get("id").getAsString();
					String name = jsonProfile.get("name").getAsString();
					UUID uuid = UUIDFetcher.getUUID(id);
					uuids.put(name, uuid);
				}

				// progress report:
				counter += uuidRequsts;
				Log.info("Progress: " + counter + " / " + names.size());

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
