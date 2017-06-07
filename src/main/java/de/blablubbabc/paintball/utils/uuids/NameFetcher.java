/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
/**
 * Thanks to evilmidget38 for this class.
 */
package de.blablubbabc.paintball.utils.uuids;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import de.blablubbabc.paintball.utils.Log;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

public class NameFetcher implements Callable<Map<UUID, String>> {

	private static final String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
	private final JSONParser jsonParser = new JSONParser();
	private final List<UUID> uuids;

	public NameFetcher(List<UUID> uuids) {
		this.uuids = new ArrayList<UUID>(uuids);
	}

	@Override
	public Map<UUID, String> call() throws Exception {
		Map<UUID, String> uuidStringMap = new HashMap<UUID, String>();
		for (UUID uuid : uuids) {
			HttpURLConnection connection = (HttpURLConnection) new URL(PROFILE_URL + uuid.toString().replace("-", "")).openConnection();
			JSONObject response = null;
			try {
				response = (JSONObject) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
			} catch (Exception e) {
				Log.warning(e.getMessage());
			}
			if (response == null) continue;

			String name = (String) response.get("name");
			if (name == null) {
				continue;
			}
			String cause = (String) response.get("cause");
			String errorMessage = (String) response.get("errorMessage");
			if (cause != null && cause.length() > 0) {
				throw new IllegalStateException(errorMessage);
			}
			uuidStringMap.put(uuid, name);
		}
		return uuidStringMap;
	}
}