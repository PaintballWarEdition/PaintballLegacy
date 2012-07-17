package me.blablubbabc.paintball;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Scanner;

import org.bukkit.plugin.java.JavaPlugin;

public class Translator {
	public boolean success;
	
	private JavaPlugin plugin;
	private File path;
	private File localisationFile;
	private File def_file;
	private HashMap<String, String> translation;
	
	public Translator(JavaPlugin plugin, String filename) {
		this.success = true;
		this.plugin = plugin;
		this.translation = new HashMap<String, String>();
		
		path = new File(plugin.getDataFolder().toString()+"/languages/");
		if(!path.exists()) path.mkdirs();
		
		//write default language file:
		//Default language:
		InputStream in = plugin.getResource("enUS.txt");
		def_file = new File(path+"enUS.txt");
	    if (in != null) {
	    	try {
		    	OutputStream out = new FileOutputStream(def_file);
		    	byte[] buffer = new byte[10240];
		    	int len = in.read(buffer);
		    	while (len != -1) {
		    	    out.write(buffer, 0, len);
		    	    len = in.read(buffer);
		    	}
	    	}catch(Exception e) {
	    		log("ERROR: Couldn't write the default language file!");
	    		e.printStackTrace();
		    	success = false;
		    	return;
	    	}
	    } else {
	    	log("ERROR: Couldn't load the default language file from jar!");
	    	success = false;
	    	return;
	    }
		
	    //loading language file:
		localisationFile = new File(path + filename + ".txt");
		if(!localisationFile.exists()) {
			log("ERROR: Couldn't find the specified language file.");
			log("Loading the default language now: "+def_file.getName());
			loadLanguage(def_file);
		} else {
			log("Loading the specified language now: "+localisationFile.getName());
			loadLanguage(localisationFile);
		}
		if(success) {
			log("Language file successfull loaded.");
		} else {
			log("ERROR: " +
					"Could not load the language file.");
		}
		
	}
	
	//GETTER:
	public String getString(String key) {
		String value = translation.get(key);
		if(value == null) {
			return "TRANSLATION_IS_MISSING";
		} else return value;
	}
	
	public HashMap<String, String> getTranslations() {
		return translation;
	}
	
	private void loadLanguage(File file) {
		try {
			Scanner scanner = new Scanner(file);
			int line = 0;
			while(scanner.hasNextLine()) {
				line++;
				//get key and value
				String text = scanner.nextLine();
				int delimeter = text.indexOf('=');
				if(delimeter == -1) {
					log("ERROR: No '=' found in line "+line);
					success = false;
					break;
				}
				String key = text.substring(0, delimeter).replaceAll(" ", "").toUpperCase();
				String value = text.substring(delimeter);
				//get correct value
				int start = value.indexOf('"');
				if(start == -1) {
					log("ERROR: No '\"' found in line "+line);
					success = false;
					break;
				}
				int end = value.lastIndexOf('"');
				if(end == start) {
					log("ERROR: No second '\"' found in line "+line);
					success = false;
					break;
				}
				value = value.substring(start+1, end);
				//checks
				if(key.isEmpty()) {
					log("ERROR: No key found in line "+line);
					success = false;
					break;
				}
				if(value.isEmpty()) {
					log("ERROR: No value found in line "+line);
					success = false;
					break;
				}
				//Add to translation map:
				translation.put(key, value);
				success = true;
			}
			
		} catch (FileNotFoundException e) {
			log("ERROR: Couldn't load the specified language file.");
			e.printStackTrace();
			success = false;
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private void log(String message) {
		System.out.println("["+plugin.toString()+"]"+message);
	}
}
