package de.blablubbabc.paintball;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;

public class Log {
	private static Plugin plugin;
	private static ConsoleCommandSender consoleSender;
	private static List<String> warnings = new ArrayList<String>();
	
	public static void init(Plugin pl) {
		plugin = pl;
		consoleSender = plugin.getServer().getConsoleSender();
	}
	
	public static void log(String message) {
		System.out.println("["+plugin.getName()+"] "+message);
	}

	public static void logConsole(String message) {
		consoleSender.sendMessage(message);
	}
	
	public static void logWarning(String message) {
		log("Warning!" + message);
		warnings.add(message);
	}
	
	public static void printWarnings() {
		if (warnings.size() > 0) {
			for (String warning : warnings) {
				Log.logConsole(ChatColor.RED+" - " + warning);
			}
			Log.logConsole(" ");
			Log.logConsole(ChatColor.RED+" -> Check your complete log!");
			Log.logConsole(ChatColor.RED+" There might be additional information above.");
			warnings.clear();
		} else {
			Log.logConsole(ChatColor.GREEN+" No warnings found. :)");
		}
	}
	
	public static void printInfo() {
		Log.logConsole(" ");
		Log.logConsole(ChatColor.YELLOW+" **************************************************");
		Log.logConsole(ChatColor.YELLOW+" ----------------- PAINTBALL INFO -----------------");
		Log.logConsole(" ");
		Log.logConsole(ChatColor.RED+" License stuff:");
		Log.logConsole(ChatColor.GOLD+"   - Usage on own risk. I give no warranties for anything.");
		Log.logConsole(ChatColor.GOLD+"   - Do not modify. Use it as it is!");
		Log.logConsole(ChatColor.GOLD+"   - Do not redistribute/upload/use parts of it/copy/give away.");
		Log.logConsole(ChatColor.GOLD+"   - Do not use for commercial purposes!");
		Log.logConsole(ChatColor.GOLD+"     -> No benefits for paying players/donors!");
		Log.logConsole(ChatColor.GOLD+"     -> This also applies to any kind of add-on you are using");
		Log.logConsole(ChatColor.GOLD+"        related to this plugin!");
		Log.logConsole(" ");
		Log.logConsole(ChatColor.DARK_GREEN+" If you like this plugin: Give feedback and donate at");
		Log.logConsole(ChatColor.DARK_GREEN+" ->http://dev.bukkit.org/server-mods/paintball_pure_war/ ");
		Log.logConsole(" ");
		Log.logConsole(ChatColor.GREEN+" Thank you and good shooting!");
		Log.logConsole(ChatColor.GREEN+"   - blablubbabc");
		Log.logConsole(" ");
		Log.logConsole(ChatColor.YELLOW+" ---------------- Detected Problems ---------------");
		Log.logConsole(" ");
		printWarnings();
		Log.logConsole(" ");
		Log.logConsole(ChatColor.YELLOW+" **************************************************");
		Log.logConsole(" ");
	}
}
