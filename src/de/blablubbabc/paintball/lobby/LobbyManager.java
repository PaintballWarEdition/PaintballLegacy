package de.blablubbabc.paintball.lobby;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import de.blablubbabc.paintball.FileManager;
import de.blablubbabc.paintball.api.lobby.LobbyI;
import de.blablubbabc.paintball.api.lobby.LobbyManagerI;
import de.blablubbabc.paintball.lobby.settings.LobbySettings;
import de.blablubbabc.paintball.lobby.settings.old.Config;
import de.blablubbabc.paintball.utils.Log;

public class LobbyManager implements LobbyManagerI {
	public static LobbyManager instance = null;
	
	private final YamlConfiguration lobbiesConfig;
	
	private final Map<String, LobbySettings> lobbySettings = new HashMap<String, LobbySettings>();
	private final Map<String, Lobby> lobbies = new HashMap<String, Lobby>();

	public LobbyManager() {
		instance = this;
		
		// load defaultLobbySettings:
		String defaultSettingsName = Config.instance.defaultLobbySettingsName;
		LobbySettings defaultLobbySettings = new LobbySettings(defaultSettingsName, getLobbySettingsFilePath(defaultSettingsName), null);
		lobbySettings.put(defaultSettingsName, defaultLobbySettings);
		
		// load lobbies and other LobbySettings:
		lobbiesConfig = YamlConfiguration.loadConfiguration(getLobbiesFilePath());
		ConfigurationSection lobbiesSection = lobbiesConfig.getConfigurationSection("Lobbies");
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
				String settingsName = lobbySection.getString("Settings Name");
				if (settingsName == null || !getLobbySettingsFilePath(settingsName).exists())  {
					Log.warning("No settings found for lobby '" + lobbyName + "': using default settings now.", true);
					settingsName = defaultSettingsName;
					lobbySection.set("Settings Name", settingsName);
				}
				// settings
				LobbySettings settings = lobbySettings.get(settingsName);
				if (settings == null) {
					// load settings:
					settings = loadLobbySettings(settingsName);
				}
				// lobby
				lobbies.put(lobbyName, new Lobby(lobbyName, settings));
			}
		}
		
		// create default lobby, if not exists:
		if (getDefaultLobby() == null) {
			Log.warning("Default lobby not found: creating it with default lobby settings.", true);
			createLobby(Config.instance.defaultLobbyName, Config.instance.defaultLobbySettingsName);
		} else {
			// save lobbies file: only if not already saved by creating a new default lobby
			saveLobbiesFile();
		}
	}
	
	@Override
	public LobbyI createLobby(String lobbyName, String lobbySettingsName) {
		Validate.notNull(lobbyName, "Invalid lobbyName given!");
		Validate.notNull(lobbySettingsName, "Invalid lobbySettingsName given!");
		
		Lobby lobby = new Lobby(lobbyName, getDefaultLobbySettings());
		lobbies.put(lobbyName, lobby);
		// add to lobbies file:
		lobbiesConfig.set("Lobbies." + lobbyName + ".Settings Name", lobbySettingsName);
		// save lobbies file:
		saveLobbiesFile();
		return lobby;
	}
	
	@Override
	public void removeLobby(String lobbyName) {
		Validate.notNull(lobbyName, "Invalid lobbyName given!");
		
		Lobby lobby = lobbies.remove(lobbyName);
		lobby.close();
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
	public LobbySettings loadLobbySettings(String lobbySettingsName) {
		Validate.notNull(lobbySettingsName, "Invalid lobbySettingsName given!");
		
		return loadLobbySettings(lobbySettingsName, getLobbySettingsFilePath(lobbySettingsName));
	}
	
	@Override
	public LobbySettings loadLobbySettings(String lobbySettingsName, File file) {
		Validate.notNull(lobbySettingsName, "Invalid lobbySettingsName given!");
		
		LobbySettings settings = new LobbySettings(lobbySettingsName, file, getDefaultLobbySettings());
		lobbySettings.put(lobbySettingsName, settings);
		return settings;
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
		Validate.notNull(lobbySettingsName, "Invalid lobbySettingsName given!");
		
		return new File(FileManager.getLobbiesFolder(), lobbySettingsName);
	}
}
