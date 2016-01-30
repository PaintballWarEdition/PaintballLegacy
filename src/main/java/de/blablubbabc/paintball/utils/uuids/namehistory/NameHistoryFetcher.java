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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import de.blablubbabc.paintball.utils.Log;

public class NameHistoryFetcher implements Callable<Map<UUID, List<NameHistoryEntry>>> {

	private final static String BASE_URL = "https://api.mojang.com/user/profiles/";
	private final static String URL_SUFFIX = "/names";

	private final JSONParser jsonParser = new JSONParser();
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
			JSONArray response = null;
			try {
				response = (JSONArray) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
			} catch (Exception e) {
				Log.warning(e.getMessage());
			}
			if (response == null) continue;

			for (int i = 0; i < response.size(); i++) {
				JSONObject entry = (JSONObject) response.get(i);
				String name = (String) entry.get("name");
				if (name == null) {
					continue;
				}

				Date date = null;
				if (entry.containsKey("changedToAt")) {
					date = new Date(((Number) entry.get("changedToAt")).longValue());
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
