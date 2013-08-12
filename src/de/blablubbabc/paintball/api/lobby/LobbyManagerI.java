package de.blablubbabc.paintball.api.lobby;

import java.io.File;

import de.blablubbabc.paintball.lobby.settings.LobbySettings;

public interface LobbyManagerI {
	public LobbyI createLobby(String lobbyName, String lobbySettingsName);
	public void saveLobbiesFile();
	public LobbySettings loadLobbySettings(String settingsName);
	public LobbySettings loadLobbySettings(String settingsName, File file);
	public LobbySettings getDefaultLobbySettings();
	public LobbyI getDefaultLobby();
	public File getLobbiesFilePath();
	public File getLobbySettingsFilePath(String settingsName);
}
