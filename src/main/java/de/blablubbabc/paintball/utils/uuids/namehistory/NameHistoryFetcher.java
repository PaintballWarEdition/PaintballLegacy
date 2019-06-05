/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.utils.uuids.namehistory;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.blablubbabc.paintball.utils.Log;

public class NameHistoryFetcher implements Callable<Map<UUID, List<NameHistoryEntry>>> {

	private final static String BASE_URL = "https://api.mojang.com/user/profiles/";
	private final static String URL_SUFFIX = "/names";

	private final JsonParser jsonParser = new JsonParser();
	private final List<UUID> uuids;

	public NameHistoryFetcher(List<UUID> uuids) {
		this.uuids = new ArrayList<UUID>(uuids);
	}

	@Override
	public Map<UUID, List<NameHistoryEntry>> call() throws Exception {
		Map<UUID, List<NameHistoryEntry>> nameHistories = new HashMap<UUID, List<NameHistoryEntry>>();
		for (UUID uuid : uuids) {
			List<NameHistoryEntry> nameHistory = new ArrayList<NameHistoryEntry>();

			HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + uuid.toString().replace("-", "") + URL_SUFFIX).openConnection();
			JsonArray response = null;
			try {
				response = jsonParser.parse(new InputStreamReader(connection.getInputStream())).getAsJsonArray();
			} catch (Exception e) {
				Log.warning(e.getMessage());
			}
			if (response == null) continue;

			for (int i = 0; i < response.size(); i++) {
				JsonObject entry = (JsonObject) response.get(i).getAsJsonObject();
				String name = entry.get("name").getAsString();
				if (name == null) {
					continue;
				}

				Date date = null;
				if (entry.has("changedToAt")) {
					date = new Date(entry.get("changedToAt").getAsLong());
				}
				nameHistory.add(new NameHistoryEntry(name, date));
			}

			if (!nameHistory.isEmpty()) {
				nameHistories.put(uuid, nameHistory);
			}
		}
		return nameHistories;
	}
}
