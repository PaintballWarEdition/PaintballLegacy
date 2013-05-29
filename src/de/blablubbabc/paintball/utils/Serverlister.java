package de.blablubbabc.paintball.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;

public class Serverlister {
	private final static String CONFIG_FILE = "plugins/Paintball/serverlist.yml";
	private final YamlConfiguration defcon;
	private final YamlConfiguration config;
	private final File configFile;
	//private static final int PING_INTERVAL = 10;//unused
	//CONFIG VALUES
	private final HashMap<String, String> vars;
	
	public Serverlister() {
		this.defcon = new YamlConfiguration();
		this.vars = new HashMap<String, String>();
		// load the config
        configFile = new File(CONFIG_FILE);
        config = YamlConfiguration.loadConfiguration(configFile);
		//WERTE MIT DEFAULTS
		setDefcon();
		//NEUE WERTE IN DIE CONFIG SCHREIBEN
		for(String node : defcon.getValues(true).keySet()) {
			//CONFIG
			if(config.get(node) == null) config.set(node, defcon.get(node));
		}
		//Korrektur:
		String serverid = config.getString("Server.Id");
		if(!isValid(serverid)) {
			config.set("Server.Id", defcon.get("Server.Id"));
		}
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void setDefcon() {
		//VALUES (confignode, default, var)
		addValue("Server.Id", UUID.randomUUID().toString(), "serverid");
		addValue("Server.List", true, "serverlist");
	}
	
	private void addValue(String node, Object def, String var) {
		//VALUES
		defcon.set(node, def);
		//ZUORDNUNG
		vars.put(var, node);
	}
	
	public Object get(String var) {
		return config.get(vars.get(var), defcon.get(vars.get(var)));
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
