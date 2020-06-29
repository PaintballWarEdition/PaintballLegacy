/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PluginUtils {

	private static final String CLASS_FILE_EXTENSION = ".class";

	private PluginUtils() {
	}

	// Returns true on (potentially partial) success and false on failure.
	public static boolean loadAllPluginClasses(File pluginJarFile, Predicate<String> filter) {
		//Log.debug("Loading all plugin classes...");
		try (ZipInputStream jar = new ZipInputStream(new FileInputStream(pluginJarFile))) {
			for (ZipEntry entry = jar.getNextEntry(); entry != null; entry = jar.getNextEntry()) {
				if (entry.isDirectory()) continue;
				String entryName = entry.getName();
				if (!entryName.endsWith(CLASS_FILE_EXTENSION)) continue;

				// Try to load the class:
				String className = entryName.substring(0, entryName.length() - CLASS_FILE_EXTENSION.length()).replace('/', '.');
				//Log.debug("  Loading: " + className);
				if (!filter.test(className)) {
					//Log.debug("    Skipped.");
					continue;
				}
				try {
					Class.forName(className);
				} catch (ClassNotFoundException e) {
					// Just in case.
					Log.warning("Could not load class '" + className + "':" + e.getMessage());
					// Continue loading any other remaining classes.
				}
			}
		} catch (IOException e) {
			Log.warning("Could not load plugin classes: " + e.getMessage());
			return false;
		}
		return true;
	}
}
