package de.blablubbabc.paintball;

import java.io.File;

public class FileManager {
	
	public void init() {
		// create folder structure:
		//TODO
		
		
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
	
	public static File getLobbiesFolder() {
		return new File(getPaintballDataFolder(), "lobbies");
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
