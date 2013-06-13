package de.blablubbabc.paintball.utils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;

public class Serverlister {
	private final static String CONFIG_FILE = "plugins/Paintball/serverlist.yml";
	private final YamlConfiguration defaults;
	private final YamlConfiguration config;
	private final File configFile;
	//private static final int PING_INTERVAL = 10;//unused
	//CONFIG VALUES
	
	public Serverlister() {
		this.defaults = new YamlConfiguration();
		// load the config
        configFile = new File(CONFIG_FILE);
        config = YamlConfiguration.loadConfiguration(configFile);
		// init defaults:
		initDefaults();
		// set defaults:
		for(String node : defaults.getValues(true).keySet()) {
			//CONFIG
			if(config.get(node) == null) config.set(node, defaults.get(node));
		}
		//Korrektur:
		String serverid = config.getString("Server.Id");
		if(!isValid(serverid)) {
			config.set("Server.Id", defaults.get("Server.Id"));
		}
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void initDefaults() {
		defaults.set("Server.Id", UUID.randomUUID().toString());
		defaults.set("Server.List", true);
	}
	
	public Object get(String node) {
		return config.get(node, defaults.get(node));
	}
	
	
	private static boolean isValid(String uuid){
	    if( uuid == null) return false;
	    try {
	        // we have to convert to object and back to string because the built in fromString does not have 
	        // good validation logic.
	        UUID fromStringUUID = UUID.fromString(uuid);
	        String toStringUUID = fromStringUUID.toString();
	        return toStringUUID.equals(uuid);
	    } catch(IllegalArgumentException e) {
	        return false;
	    }
	}
}
