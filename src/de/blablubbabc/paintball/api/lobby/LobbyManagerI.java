package de.blablubbabc.paintball.api.lobby;

import java.io.File;
import java.util.List;

import org.bukkit.Location;

import de.blablubbabc.paintball.lobby.settings.LobbySettings;

public interface LobbyManagerI {

	/**
	 * Creates a new lobby with the given name. An already existing lobby with
	 * that name will be closed and replaced. A newly created lobby has no
	 * spawns at first. If no LobbySettings of the given name are loaded, it
	 * will try to load the settings with that name from the lobbies settings
	 * folder (and create a file if necessary) and default to the default
	 * LobbySettings.
	 * 
	 * @param lobbyName
	 *            the name of the lobby
	 * @param lobbySettingsName
	 *            the name of the settings to use for the lobby
	 * @param lobbySpawns
	 *            the lobby spawns
	 * @return the newly created lobby
	 */
	public LobbyI createNewLobby(String lobbyName, String lobbySettingsName, List<Location> lobbySpawns);

	/**
	 * Returns the lobby with this name, or null if no lobby with this name is
	 * loaded.
	 * 
	 * @param lobbyName
	 *            the name of the lobby
	 * @return the lobby, or null, if no lobby with this name is loaded
	 */
	public LobbyI getLobby(String lobbyName);

	/**
	 * Closes and then removes the lobby with the given name. Does not save the
	 * changes of the lobbies file on it's own.
	 * 
	 * @param lobbyName
	 *            the name of the lobby to remove
	 */
	public void removeLobby(String lobbyName);

	/**
	 * Saves the lobbies configuration file.
	 */
	public void saveLobbiesFile();

	/**
	 * Loads (and initializes if necessary) and returns the default
	 * LobbySettings, just like when loading other LobbySettings, with the
	 * difference that the file will be filled with missing default values.
	 * 
	 * @return the loaded LobbySettings
	 */
	public LobbySettings loadDefaultLobbySettings();

	/**
	 * Loads all LobbySettings from the lobby settings folder just like it would
	 * if loading them separately. Will load the default LobbySettings file
	 * first.
	 */
	public void loadAllLobbySettings();

	/**
	 * Loads and returns the LobbySettings with the given name from the lobbies
	 * settings folder. The returned LobbySettings defaults to the default
	 * settings. Already loaded LobbySettings with that name will be replaced
	 * with the newly loaded one. Lobbies using this old LobbySetting will not
	 * automatically be updated. This will create a new LobbySettings file in
	 * the lobbies setting folder if none existed before with this name.
	 * 
	 * @param settingsName
	 *            the name of the LobbySettings
	 * @return the loaded LobbySettings with that name
	 */
	public LobbySettings loadLobbySettings(String settingsName);

	/**
	 * Loads and returns the LobbySettings with the given name, loaded from the
	 * given file. The returned LobbySettings defaults to the default settings.
	 * If LobbySettings with that name are already loaded, they will be replaced
	 * with the newly loaded one. This will create the new LobbySettings file if
	 * it didn't exist before.
	 * 
	 * @param settingsName
	 *            the name of the LobbySettings
	 * @param file
	 *            the file to load from
	 * @return the loaded LobbySettings with that name
	 */
	public LobbySettings loadLobbySettings(String settingsName, File file);

	/**
	 * Returns the LobbySettings with the given name, or null if those are not
	 * loaded.
	 * 
	 * @param settingsName
	 *            the name of the LobbySettings
	 * @param file
	 * @return the LobbySettings with that name if loaded, else null
	 */
	public LobbySettings getLobbySettings(String settingsName);

	/**
	 * Returns the default LobbySettings.
	 * 
	 * @return the default LobbySettings
	 */
	public LobbySettings getDefaultLobbySettings();

	/**
	 * Returns the default Lobby.
	 * 
	 * @return the default Lobby
	 */
	public LobbyI getDefaultLobby();

	/**
	 * Returns the file path to the lobbies file.
	 * 
	 * @return the file path to the lobbies file
	 */
	public File getLobbiesFilePath();

	/**
	 * Returns the file path to the lobby settings folder.
	 * 
	 * @return the file path to the lobby settings folder
	 */
	public File getLobbySettingsFolder();

	/**
	 * Returns the path to the settings file with this name inside the lobby
	 * settings folder.
	 * 
	 * @param settingsName
	 *            the name of the LobbySettings / the file
	 * @return the path to the settings file with this name
	 */
	public File getLobbySettingsFilePath(String settingsName);
}
