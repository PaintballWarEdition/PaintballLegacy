package de.blablubbabc.paintball.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Scanner;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;



public class Translator {
	public static boolean success = false;

	private static HashMap<String, String> translation;
	private static HashMap<String, String> def_language;
	private static boolean use_def = false;

	public Translator(Plugin plugin, String filename) {
		File path;
		File localisationFile;
		File def_file;
		
		Translator.use_def = false;
		Translator.success = false;
		Translator.translation = new HashMap<String, String>();
		Translator.def_language = new HashMap<String, String>();

		path = new File(plugin.getDataFolder().toString() + "/languages/");
		if (!path.exists())
			path.mkdirs();

		// write default language file:
		// Default language:
		def_file = new File(path + "/enUS.txt");
		InputStream in = null;
		OutputStream out = null;
		try {
			in = plugin.getResource("enUS.txt");
			if (in != null) {
				out = new FileOutputStream(def_file);
				byte[] buffer = new byte[10240];
				int len = in.read(buffer);
				while (len != -1) {
					out.write(buffer, 0, len);
					len = in.read(buffer);
				}
			} else {
				Log.warning("ERROR: Couldn't load the default language file from jar!");
				return;
			}
		} catch (Exception e) {
			Log.warning("ERROR: Couldn't write the default language file!");
			e.printStackTrace();
			return;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// get default language:
		Log.info("Loading the default language: " + def_file.getName());
		def_language = loadLanguage(def_file);
		if (def_language == null) {
			return;
		}

		// get translation:
		localisationFile = new File(path + "/" + filename + ".txt");
		if (!localisationFile.exists()) {
			Log.info("ERROR: Couldn't find the specified language file.");
			Log.warning("Using the default language now: " + def_file.getName());
			use_def = true;
		} else {
			if (!localisationFile.equals(def_file)) {
				Log.info("Loading the specified language now: "
						+ localisationFile.getName());
				translation = loadLanguage(localisationFile);
				if (translation == null) {
					Log.info("ERROR: Couldn't load the specified language file!");
					Log.info("Do you use the right translation?");
					Log.warning("Using the default language now: " + def_file.getName());
					use_def = true;
				} else {
					// length check
					if (translation.size() != def_language.size()) {
						Log.warning("WARNING: Size-Missmatch between the keys of the loaded and the default language file detected! (translation-default: "
								+ translation.size()
								+ "-"
								+ def_language.size() + " )");
						Log.info("Do you use the right translation?");
					}
					// keys missing?
					boolean key_missing = false;
					for (String s : def_language.keySet()) {
						if (!translation.containsKey(s)) {
							Log.info("ERROR: Key missing: " + s);
							key_missing = true;
						}
					}
					if (key_missing) {
						Log.warning("ERROR: There are keys missing in the loaded language-file!");
						Log.info("Do you use the right translation-version?");
						Log.warning("Using the default language now: "
								+ def_file.getName());
						use_def = true;
					}
				}
			} else {
				Log.info("Using the default language now: " + def_file.getName());
				use_def = true;
			}
		}
		Translator.success = true;
	}

	// GETTER:
	public static String getString(String key) {
		if (!success) {
			return "ERROR:could_not_load_language!";
		}
		String value;
		if (use_def)
			value = def_language.get(key.toUpperCase());
		else
			value = translation.get(key.toUpperCase());
		if (value == null) {
			return "ERROR:translation_is_missing!";
		} else {
			// colors
			value = ChatColor.translateAlternateColorCodes('&', value);
			return value;
		}
	}

	public static String getString(String key, HashMap<String, String> vars) {
		if (!success) {
			return "ERROR:couldn't load language!";
		}
		String value;
		if (use_def)
			value = def_language.get(key.toUpperCase());
		else
			value = translation.get(key.toUpperCase());
		if (value == null) {
			return "ERROR:translation_is_missing!";
		} else {
			// vars
			for (String v : vars.keySet()) {
				value = value.replace("{" + v + "}", vars.get(v));
			}
			// colors
			value = ChatColor.translateAlternateColorCodes('&', value);
			return value;
		}
	}

	public static HashMap<String, String> getTranslations() {
		if (!success) return null;
		if (use_def)
			return def_language;
		else
			return translation;
	}

	private HashMap<String, String> loadLanguage(File file) {
		HashMap<String, String> language = new HashMap<String, String>();
		Scanner scanner = null;
		try {
			scanner = new Scanner(file);
			// TEST
			int line_skipped = 0;
			int line = 0;
			while (scanner.hasNextLine()) {
				line++;
				String text = scanner.nextLine();
				// Spaces am anfang entfernen:
				while (text.startsWith(" ")) {
					text = text.substring(1);
				}
				// Leerzeile?
				if (text.isEmpty()) {
					line_skipped++;
					continue;
				}
				// comment-zeile?
				if (text.startsWith("#")) {
					line_skipped++;
					continue;
				}

				// get key and value
				int delimeter = text.indexOf('=');
				if (delimeter == -1) {
					Log.info("ERROR: No '=' found in line " + line);
					return null;
				}
				String key = text.substring(0, delimeter).replaceAll(" ", "")
						.toUpperCase();
				String value = text.substring(delimeter);
				// get correct value
				int start = value.indexOf('"');
				if (start == -1) {
					Log.info("ERROR: No '\"' found in line " + line);
					return null;
				}
				int end = value.lastIndexOf('"');
				if (end == start) {
					Log.info("ERROR: No second '\"' found in line " + line);
					return null;
				}
				// too many '"'?
				int gaense = 0;
				for (int i = 0; i < value.length(); i++) {
					if (value.charAt(i) == '"')
						gaense++;
				}
				if (gaense > 2) {
					Log.info("ERROR: Too many '\"' found in line " + line);
					return null;
				}
				value = value.substring(start + 1, end);
				// checks
				if (key.isEmpty()) {
					Log.info("ERROR: No key found in line " + line);
					break;
				}
				if (value.isEmpty()) {
					Log.info("ERROR: No value found in line " + line);
					return null;
				}
				// already existing?
				if (language.containsKey(key)) {
					Log.info("WARNING: Duplicate key: " + key);
				}
				// Add to translation map:
				language.put(key, value);
			}
			
			Log.info("Scanned lines: " + line + " | Skipped lines: " + line_skipped);
			return language;
		} catch (Exception e) {
			Log.info("ERROR: Couldn't load the specified language file.");
			e.printStackTrace();
			return null;
		} finally {
			if(scanner != null) scanner.close();
		}

	}
}