/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;

import de.blablubbabc.paintball.Paintball;

public class Log {

	private static Logger logger;
	private static ConsoleCommandSender consoleSender;
	private static List<String> warnings = new ArrayList<String>();
	private static boolean logWarnings = false;

	public static void init(Plugin pl) {
		consoleSender = pl.getServer().getConsoleSender();
		logger = pl.getLogger();
	}

	public static void debug(String message) {
		if (Paintball.getInstance().debug) {
			log("[PB DEBUG] " + message);
		}
	}

	public static void logWarnings(boolean logWarnings) {
		Log.logWarnings = logWarnings;
	}

	public static boolean isLoggingWarnings() {
		return Log.logWarnings;
	}

	public static void log(String message) {
		System.out.println(message);
	}

	public static void info(String message) {
		info(message, false);
	}

	public static void info(String message, boolean addWarning) {
		logger.info(message);
		if (addWarning) addWarning(message);
	}

	public static void logColored(String message) {
		consoleSender.sendMessage(message);
	}

	public static void severe(String message) {
		severe(message, false);
	}

	public static void warning(String message) {
		warning(message, false);
	}

	public static void severe(String message, boolean warn) {
		logger.severe(message);
		if (warn) addWarning(message);
	}

	public static void warning(String message, boolean warn) {
		logger.warning(message);
		if (warn) addWarning(message);
	}

	public static void addWarning(String message) {
		if (Log.logWarnings) warnings.add(message);
	}

	public static void printCurrentWarnings() {
		if (warnings.size() > 0) {
			for (String warning : warnings) {
				Log.logColored(ChatColor.RED + " - " + warning);
			}
			Log.logColored(" ");
			Log.logColored(ChatColor.RED + " -> Check your complete log!");
			Log.logColored(ChatColor.RED + " There might be additional information above.");
			warnings.clear();
		} else {
			Log.logColored(ChatColor.GREEN + " No problems found. :)");
		}
	}

	public static void printInfo() {
		Log.logColored(" ");
		Log.logColored(ChatColor.YELLOW + " **************************************************");
		Log.logColored(ChatColor.YELLOW + " ----------------- PAINTBALL INFO -----------------");
		Log.logColored(" ");
		Log.logColored(ChatColor.RED + " License stuff:");
		Log.logColored(ChatColor.GOLD + "   - Usage on own risk. I give no warranties for anything.");
		Log.logColored(ChatColor.GOLD + "   - Do not modify. Use it as it is!");
		Log.logColored(ChatColor.GOLD + "   - Do not redistribute/upload/use parts of it/copy/give away.");
		Log.logColored(ChatColor.GOLD + "   - Do not use for commercial purposes!");
		Log.logColored(ChatColor.GOLD + "     -> No benefits for paying players/donors!");
		Log.logColored(ChatColor.GOLD + "     -> This also applies to any kind of add-on you are using");
		Log.logColored(ChatColor.GOLD + "        related to this plugin!");
		Log.logColored(" ");
		Log.logColored(ChatColor.DARK_GREEN + " If you like this plugin: Give feedback and donate at");
		Log.logColored(ChatColor.DARK_GREEN + " ->http://dev.bukkit.org/projects/paintball_pure_war/ ");
		Log.logColored(" ");
		Log.logColored(ChatColor.GREEN + " Thank you and good shooting!");
		Log.logColored(ChatColor.GREEN + "   - blablubbabc :)");
		Log.logColored(" ");
		Log.logColored(ChatColor.YELLOW + " ---------------- Detected Problems ---------------");
		Log.logColored(" ");
		printCurrentWarnings();
		Log.logColored(" ");
		Log.logColored(ChatColor.YELLOW + " **************************************************");
		Log.logColored(" ");
	}
}