package de.blablubbabc.paintball.lobby;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import de.blablubbabc.paintball.FileManager;
import de.blablubbabc.paintball.api.lobby.LobbyI;
import de.blablubbabc.paintball.api.lobby.LobbyManagerI;
import de.blablubbabc.paintball.lobby.settings.LobbySettings;
import de.blablubbabc.paintball.lobby.settings.old.Config;
import de.blablubbabc.paintball.utils.Log;
import de.blablubbabc.paintball.utils.Utils;

public class LobbyManager implements LobbyManagerI {
	private static final String NODE_LOBBIES_SECTION = "Lobbies";
	private static final String NODE_SETTINGS = "Settings";
	private static final String NODE_SPAWNS = "Spawns";

	public static LobbyManager instance = null;

	private final YamlConfiguration lobbiesConfig;

	private final Map<String, LobbySettings> lobbySettings = new HashMap<String, LobbySettings>();
	private final Map<String, Lobby> lobbies = new HashMap<String, Lobby>();

	public LobbyManager() {
		instance = this;

		// load all LobbySettings (and default settings first):
		loadAllLobbySettings();

		// load lobbies:
		lobbiesConfig = YamlConfiguration.loadConfiguration(getLobbiesFilePath());
		ConfigurationSection lobbiesSection = lobbiesConfig.getConfigurationSection(NODE_LOBBIES_SECTION);
		if (lobbiesSection != null) {
			Iterator<String> lobbyNames = lobbiesSection.getKeys(false).iterator();
			while (lobbyNames.hasNext()) {
				String lobbyName = lobbyNames.next();
				// section
				ConfigurationSection lobbySection = lobbiesSection.getConfigurationSection(lobbyName);
				if (lobbySection == null) {
					Log.warning("Wasn't able to load lobby '" + lobbyName + "': invalid ConfigurationSection.", true);
					lobbiesSection.set(lobbyName, null);
					continue;
				}
				// settings name
				String settingsName = lobbySection.getString(NODE_SETTINGS);
				if (settingsName == null || !getLobbySettingsFilePath(settingsName).exists()) {
					Log.warning("No settings found for lobby '" + lobbyName + "': using default settings now.", true);
					settingsName = Config.instance.defaultLobbySettingsName;
					lobbySection.set(NODE_SETTINGS, settingsName);
				}
				// settings
				LobbySettings settings = lobbySettings.get(settingsName);
				if (settings == null) {
					// load settings:
					settings = loadLobbySettings(settingsName);
				}
				// lobbyspawns
				List<Location> lobbySpawns = Utils.StringsToLocations(lobbySection.getStringList(NODE_SPAWNS));
				// lobby
				lobbies.put(lobbyName, new Lobby(lobbyName, settings, lobbySpawns));
			}
		}

		// create default lobby, if not exists:
		if (getDefaultLobby() == null) {
			Log.warning("Default lobby not found: creating it with default lobby settings.", true);
			createNewLobby(Config.instance.defaultLobbyName, Config.instance.defaultLobbySettingsName, new ArrayList<Location>());
		} else {
			// save lobbies file: only if not already saved by creating a new
			// default lobby
			saveLobbiesFile();
		}
	}

	@Override
	public LobbyI getLobby(String lobbyName) {
		Validate.notNull(lobbyName, "Invalid lobbyName given: null");

		return lobbies.get(lobbyName);
	}

	@Override
	public LobbyI createNewLobby(String lobbyName, String lobbySettingsName, List<Location> lobbySpawns) {
		Validate.notNull(lobbyName, "Invalid lobbyName given: null");
		Validate.notNull(lobbySettingsName, "Invalid lobbySettingsName given: null");
		LobbySettings lobbySettings = getLobbySettings(lobbySettingsName);
		Validate.notNull(lobbySettingsName, "Invalid lobbySettingsName given: settings '" + lobbySettingsName + "' not loaded");

		if (lobbySpawns == null)
			lobbySpawns = new ArrayList<Location>();

		// remove already exiting lobby with that name
		removeLobby(lobbyName);
		// create new lobby
		Lobby lobby = new Lobby(lobbyName, lobbySettings, lobbySpawns);
		lobbies.put(lobbyName, lobby);
		// add to lobbies file:
		lobbiesConfig.set(NODE_LOBBIES_SECTION + "." + lobbyName + "." + NODE_SETTINGS, lobbySettingsName);
		lobbiesConfig.set(NODE_LOBBIES_SECTION + "." + lobbyName + "." + NODE_SPAWNS, lobbySpawns);
		// save lobbies file:
		saveLobbiesFile();
		return lobby;
	}

	@Override
	public void removeLobby(String lobbyName) {
		Validate.notNull(lobbyName, "Invalid lobbySettingsName given: null");

		Lobby lobby = lobbies.remove(lobbyName);
		if (lobby != null)
			lobby.close();
		// remove from lobbies file:
		lobbiesConfig.set(NODE_LOBBIES_SECTION + "." + lobbyName, null);
	}

	@Override
	public void saveLobbiesFile() {
		File file = getLobbiesFilePath();
		try {
			lobbiesConfig.save(file);
		} catch (IOException e) {
			Log.severe("Wasn't able to save the lobbies.yml to " + file.getPath() + ": " + e.getMessage(), true);
			e.printStackTrace();
		}
	}

	@Override
	public LobbySettings loadDefaultLobbySettings() {
		return loadLobbySettings(Config.instance.defaultLobbySettingsName, getLobbySettingsFilePath(Config.instance.defaultLobbySettingsName), true);
	}

	@Override
	public void loadAllLobbySettings() {
		// load default LobbySettings:
		loadDefaultLobbySettings();

		// load all remaining LobbySettings from lobbies folder:
		for (File settingsFile : FileManager.getConfigFilesInFolder(FileManager.getLobbiesFolder())) {
			String lobbySettingsName = FileManager.removeExtension(settingsFile.getName());
			if (lobbySettingsName.isEmpty() || settingsFile.equals(Config.instance.defaultLobbySettingsName))
				continue;

			loadLobbySettings(lobbySettingsName, settingsFile);
		}
	}

	@Override
	public LobbySettings loadLobbySettings(String lobbySettingsName) {
		return loadLobbySettings(lobbySettingsName, getLobbySettingsFilePath(lobbySettingsName));
	}

	@Override
	public LobbySettings loadLobbySettings(String lobbySettingsName, File file) {
		return loadLobbySettings(lobbySettingsName, file, false);
	}

	private LobbySettings loadLobbySettings(String lobbySettingsName, File file, boolean isDefault) {
		Validate.notNull(lobbySettingsName, "Invalid lobbySettingsName given: null");
		Validate.notNull(file, "Invalid file given: null");

		// loads the settings and creates a new file if necessary
		LobbySettings settings = new LobbySettings(lobbySettingsName, file, isDefault ? null : getDefaultLobbySettings());
		lobbySettings.put(lobbySettingsName, settings);
		return settings;
	}

	@Override
	public LobbySettings getLobbySettings(String lobbySettingsName) {
		if (lobbySettingsName == null)
			return null;
		return lobbySettings.get(lobbySettingsName);
	}

	@Override
	public LobbySettings getDefaultLobbySettings() {
		return lobbySettings.get(Config.instance.defaultLobbySettingsName);
	}

	@Override
	public LobbyI getDefaultLobby() {
		return lobbies.get(Config.instance.defaultLobbyName);
	}

	@Override
	public File getLobbiesFilePath() {
		return FileManager.getLobbiesFilePath();
	}

	@Override
	public File getLobbySettingsFilePath(String lobbySettingsName) {
		Validate.notNull(lobbySettingsName, "Invalid lobbySettingsName given: null");

		return new File(FileManager.getLobbiesFolder(), lobbySettingsName + ".yml");
	}

}
