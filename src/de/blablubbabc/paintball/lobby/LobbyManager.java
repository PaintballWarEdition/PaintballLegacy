package de.blablubbabc.paintball.lobby;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;

import de.blablubbabc.paintball.FileManager;
import de.blablubbabc.paintball.lobby.settings.LobbySettings;

public class LobbyManager {
	public static LobbyManager instance = null;
	
	private LobbySettings defaultLobbySettings = null;
	private Map<String, Lobby> lobbies = new HashMap<String, Lobby>();

	public LobbyManager() {
		instance = this;
		
		// load defaultLobbySettings:
		defaultLobbySettings = new LobbySettings(getDefaultLobbySettingsFile(), null);
		
		// load lobbies and other LobbySettings:
		YamlConfiguration lobbiesConfig = YamlConfiguration.loadConfiguration(FileManager.getLobbiesFilePath());
		
	}
	
	public File getDefaultLobbySettingsFile() {
		//TODO
		return null;
		//return new File(FileManager.getLobbiesFolder(), Config.defaultLobbySettings);
	}
	
	public LobbySettings getDefaultLobbySettings() {
		return defaultLobbySettings;
	}
}
