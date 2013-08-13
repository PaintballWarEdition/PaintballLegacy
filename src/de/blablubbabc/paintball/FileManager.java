package de.blablubbabc.paintball;

import java.io.File;
import java.io.FilenameFilter;

public class FileManager {
	
	public void init() {
		// create folder structure:
		//TODO
		
		
	}
	
	/**
	 * Non-deep search for config files (.yml ending).
	 * 
	 * @param folder
	 *            folder to search in
	 * @return array of config files
	 */
	public static File[] getConfigFilesInFolder(File folder) {
		if (folder == null || !folder.isDirectory())
			return new File[0];

		return folder.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".yml");
			}
		});
	}
	
	public static String removeExtension(String fileName) {
		if (fileName == null) {
	        return null;
	    }

		int index = fileName.lastIndexOf(".");

	    if (index == -1) {
	        return fileName;
	    } else {
	        return fileName.substring(0, index);
	    }
	}
	
	// FOLDERS:
	
	public static File getPaintballDataFolder() {
		return Paintball.instance.getDataFolder();
	}
	
	public static File getAddonsFolder() {
		return new File(getPaintballDataFolder(), "addons");
	}
	
	public static File getArenasFolder() {
		return new File(getPaintballDataFolder(), "arenas");
	}
	
	public static File getGamemodesFolder() {
		return new File(getPaintballDataFolder(), "gamemodes");
	}
	
	public static File getLanguagesFolder() {
		return new File(getPaintballDataFolder(), "languages");
	}
	
	public static File getLobbySettingsFolder() {
		return new File(getPaintballDataFolder(), "lobby settings");
	}
	
	public static File getShopsFolder() {
		return new File(getPaintballDataFolder(), "shops");
	}
	
	public static File getWeaponsFolder() {
		return new File(getPaintballDataFolder(), "weapons and gadgets");
	}
	
	// FILES and SUBFOLDERS:
	
	// /Paintball/...
	
	public static File getConfigFilePath() {
		return new File(getPaintballDataFolder(), "config.yml");
	}
	
	public static File getGamesFilePath() {
		return new File(getPaintballDataFolder(), "games.yml");
	}
	
	public static File getLobbiesFilePath() {
		return new File(getPaintballDataFolder(), "lobbies.yml");
	}
	
	public static File getRanksFilePath() {
		return new File(getPaintballDataFolder(), "ranks.yml");
	}
	
	public static File getServerlistFilePath() {
		return new File(getPaintballDataFolder(), "serverlist.yml");
	}
	
	public static File getSoundsFilePath() {
		return new File(getPaintballDataFolder(), "sounds.yml");
	}
	
	public static File getTeamsFilePath() {
		return new File(getPaintballDataFolder(), "teams.yml");
	}
	
}
