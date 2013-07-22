package de.blablubbabc.paintball.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import de.blablubbabc.paintball.Paintball;

public class Translator {
	public static boolean success = false;
	
	private static Map<String, String> translation;

	public Translator(Plugin plugin, String filename) {
		Translator.success = false;
		Translator.translation = new HashMap<String, String>();
		
		File path;
		File localisationFile;
		File def_file;
		
		boolean use_def = false;
		Map<String, String> def_language = new HashMap<String, String>();

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
				Log.severe("Couldn't load the default language file from jar!", true);
				return;
			}
		} catch (Exception e) {
			Log.severe("Couldn't write the default language file!", true);
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
		def_language = loadLanguage(def_file, "utf-8");
		if (def_language == null) {
			return;
		}

		// get translation:
		localisationFile = new File(path + "/" + filename + ".txt");
		if (!localisationFile.exists()) {
			Log.warning("Couldn't find the specified language file.", true);
			Log.warning("Using the default language now: " + def_file.getName(), true);
			use_def = true;
		} else {
			if (!localisationFile.equals(def_file)) {
				Log.info("Loading the specified language now: "
						+ localisationFile.getName());
				translation = loadLanguage(localisationFile, Paintball.instance.languageFileEncoding);
				if (translation == null) {
					Log.warning("Couldn't load the specified language file!", true);
					Log.warning("Do you use the right translation?");
					Log.warning("Using the default language now: " + def_file.getName(), true);
					use_def = true;
				} else {
					// length check
					if (translation.size() != def_language.size()) {
						Log.warning("Size-Missmatch between the keys of the loaded and the default language file detected! (translation-default: "
								+ translation.size()
								+ "-"
								+ def_language.size() + " )", true);
						Log.warning("Do you use the right translation?");
					}
					// keys missing?
					boolean key_missing = false;
					for (String s : def_language.keySet()) {
						if (!translation.containsKey(s)) {
							Log.warning("Key missing: " + s);
							key_missing = true;
						}
					}
					if (key_missing) {
						Log.warning("There are keys missing in the loaded language-file!", true);
						Log.warning("Do you use the right translation-version?");
						Log.warning("Using the default language now: "
								+ def_file.getName(), true);
						use_def = true;
					}
				}
			} else {
				Log.info("Using the default language now: " + def_file.getName());
				use_def = true;
			}
		}
		Translator.success = true;
		
		// remove the not needed map:
		if (use_def) {
			translation = def_language;
		}
		
		def_language = null;
	}

	// GETTER:
	public static String getString(String key) {
		return getString(key, (KeyValuePair[]) null);
	}

	public static String getString(String key, Map<String, String> vars) {
		if (vars == null) return getString(key);
		
		KeyValuePair[] values = new KeyValuePair[vars.size()];
		// vars
		int i = 0;
		for (Entry<String, String> entry : vars.entrySet()) {
			values[i++] = new KeyValuePair(entry.getKey(), entry.getValue());
		}
		
		return getString(key, values);
	}
	
	public static String getString(String key, KeyValuePair... values) {
		if (!success) {
			return "ERROR:couldn't load language!";
		}
		
		String value = translation.get(key.toUpperCase());
		
		if (value == null) {
			return "ERROR:translation_is_missing!";
		} else {
			if (values != null && values.length > 0) {
				// key-value-pair replacements
				for (KeyValuePair entry : values) {
					if (entry != null) {
						value = value.replace("{" + entry.getKey() + "}", entry.getValue());
					}
				}
			}
			// colors should already be done, when loading the langauge file:
			//value = ChatColor.translateAlternateColorCodes('&', value);
			return value;
		}
	}

	public static Map<String, String> getTranslation() {
		if (!success) return null;
		return translation;
	}

	private Map<String, String> loadLanguage(File file, String encoding) {
		Map<String, String> language = new HashMap<String, String>();
		Scanner scanner = null;
		try {
			scanner = new Scanner(file, encoding);
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
					Log.warning("No '=' found in line " + line);
					return null;
				}
				String key = text.substring(0, delimeter).replaceAll(" ", "").toUpperCase();
				String value = text.substring(delimeter);
				// get correct value
				int start = value.indexOf('"');
				if (start == -1) {
					Log.warning("No '\"' found in line " + line);
					return null;
				}
				int end = value.lastIndexOf('"');
				if (end == start) {
					Log.warning("No second '\"' found in line " + line);
					return null;
				}
				// too many '"'?
				int gaense = 0;
				for (int i = 0; i < value.length(); i++) {
					if (value.charAt(i) == '"')
						gaense++;
				}
				if (gaense > 2) {
					Log.warning("Too many '\"' found in line " + line);
					return null;
				}
				value = value.substring(start + 1, end);
				// checks
				if (key.isEmpty()) {
					Log.warning("No key found in line " + line);
					break;
				}
				if (value.isEmpty()) {
					Log.warning("No value found in line " + line);
					return null;
				}
				// already existing?
				if (language.containsKey(key)) {
					Log.warning("Duplicate key: " + key);
				}
				// Add colored value to translation map:
				language.put(key, ChatColor.translateAlternateColorCodes('&', value));
			}
			
			Log.info("Scanned lines: " + line + " | Skipped lines: " + line_skipped);
			return language;
		} catch (Exception e) {
			Log.severe("Couldn't load the specified language file.");
			e.printStackTrace();
			return null;
		} finally {
			if(scanner != null) scanner.close();
		}

	}
}
