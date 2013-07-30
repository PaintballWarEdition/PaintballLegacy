package de.blablubbabc.paintball.lobby;

import java.io.File;

import org.bukkit.configuration.file.YamlConfiguration;

public class LobbyConfig {

	private final YamlConfiguration configuration;
	private final YamlConfiguration defaultConfiguration;
	
	public LobbyConfig(File file, YamlConfiguration defaultConfiguration) {
		this.defaultConfiguration = defaultConfiguration;
		this.configuration = YamlConfiguration.loadConfiguration(file);
	}
	
	public boolean contains(LobbySetting setting) {
		return configuration.contains(setting.getPath());
	}

	public Object get(LobbySetting setting) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object get(LobbySetting setting, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getBoolean(LobbySetting setting) {
		return configuration.getBoolean(setting.getPath(), defaultSettings.);
	}

	public boolean getBoolean(LobbySetting setting, boolean arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public List<Boolean> getBooleanList(LobbySetting setting) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Byte> getByteList(LobbySetting setting) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Character> getCharacterList(LobbySetting setting) {
		// TODO Auto-generated method stub
		return null;
	}

	public Color getColor(LobbySetting setting) {
		// TODO Auto-generated method stub
		return null;
	}

	public Color getColor(LobbySetting setting, Color arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public double getDouble(LobbySetting setting) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getDouble(LobbySetting setting, double arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<Double> getDoubleList(LobbySetting setting) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Float> getFloatList(LobbySetting setting) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getInt(LobbySetting setting) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getInt(LobbySetting setting, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<Integer> getIntegerList(LobbySetting setting) {
		// TODO Auto-generated method stub
		return null;
	}

	public ItemStack getItemStack(LobbySetting setting) {
		// TODO Auto-generated method stub
		return null;
	}

	public ItemStack getItemStack(LobbySetting setting, ItemStack arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<?> getList(LobbySetting setting) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<?> getList(LobbySetting setting, List<?> arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public long getLong(LobbySetting setting) {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getLong(LobbySetting setting, long arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<Long> getLongList(LobbySetting setting) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Map<?, ?>> getMapList(LobbySetting setting) {
		// TODO Auto-generated method stub
		return null;
	}

	public OfflinePlayer getOfflinePlayer(LobbySetting setting) {
		// TODO Auto-generated method stub
		return null;
	}

	public OfflinePlayer getOfflinePlayer(LobbySetting setting, OfflinePlayer arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Short> getShortList(LobbySetting setting) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getString(LobbySetting setting) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getString(LobbySetting setting, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getStringList(LobbySetting setting) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, Object> getValues(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector getVector(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector getVector(String arg0, Vector arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
