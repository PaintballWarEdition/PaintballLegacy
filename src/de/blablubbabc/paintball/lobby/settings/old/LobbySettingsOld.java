package de.blablubbabc.paintball.lobby.settings.old;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import de.blablubbabc.paintball.gadgets.Gift;

public class LobbySettingsOld {
	public int countdownSeconds;
	public int countdownDelaySeconds;
	public int maxPlayers;
	public boolean coloredListnames;
	public boolean coloredChat;
	public boolean shopEnabled;
	public String shop;
	public List<String> allowedCommands;
	public List<String> blacklistedCommands;
	public boolean blacklistAdminOverride;
	public boolean saveInventory;
	public boolean onlyRandom;
	public boolean autoRandom;
	public boolean autoSpecLobby;
	public boolean afkDetectionEnabled;
	public int afkRadius;
	public int afkPoints;

	// gifts
	public boolean giftsEnabled;
	public List<Gift> gifts; // later inserted
	public double giftChanceFactor; //generated
	public boolean wishesEnabled;
	public String wishesText;
	public int wishesDelayMinutes;

	// lobby join checks
	public boolean checkInventory;
	public boolean checkGamemode;
	public boolean checkFlymode;
	public boolean checkBurningFallingDiving;
	public boolean checkHealth;
	public boolean checkFoodlevel;
	public boolean checkEffects;
	
	@SuppressWarnings("unchecked")
	public LobbySettingsOld(File file, LobbySettingsOld defSettings) {
		boolean isDefault = defSettings == null;
		
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		// INITIALIZE DEFAULT CONFIG:
		if (isDefault) {
			for (LobbySettingOld defSetting : LobbySettingOld.values()) {
				setDefault(config, defSetting);
			}
			
			// gifts
			if (config.get(LobbySettingOld.Gifts.getPath()) == null) {
				List<Gift> giftsDef = new ArrayList<Gift>();
				giftsDef.add(new Gift(332, (short)0, 50, 30.0, "Hope you have luck with these balls!"));
				giftsDef.add(new Gift(344, (short)0, 2, 15.0, "May these grenades be with you!"));
				giftsDef.add(new Gift(390, (short)0, 2, 15.0, "I knew you ever wanted to be a sneaky killer!"));
				giftsDef.add(new Gift(356, (short)0, 2, 15.0, "Heat them with these rocket launchers!"));
				giftsDef.add(new Gift(280, (short)0, 1, 15.0, "I knew you ever wanted to order a airstrike at least once!"));
				giftsDef.add(new Gift(54, (short)0, 2, 5.0, "I got some more gifts for you!"));
				giftsDef.add(new Gift(86, (short)0, 1, 3.0, "They survived the apocalypse? But the will not survive this!"));
				giftsDef.add(new Gift(0, (short)0, 0, 2.0, "You had no luck this time :("));
				
				for(Gift g : giftsDef) {
					config.set(LobbySettingOld.Gifts.getPath()+"."+giftsDef.indexOf(g)+".message", g.getMessage());
					config.set(LobbySettingOld.Gifts.getPath()+"."+giftsDef.indexOf(g)+".id", g.getItem(false).getTypeId());
					config.set(LobbySettingOld.Gifts.getPath()+"."+giftsDef.indexOf(g)+".subid", g.getItem(false).getDurability());
					config.set(LobbySettingOld.Gifts.getPath()+"."+giftsDef.indexOf(g)+".amount", g.getItem(false).getAmount());
					config.set(LobbySettingOld.Gifts.getPath()+"."+giftsDef.indexOf(g)+".chance", g.getChance());
				}
			}
			
			// SAVE CONFIG
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		// READ VALUES
		countdownSeconds = Math.max(0, config.getInt(LobbySettingOld.CountdownSeconds.getPath(), isDefault ? (Integer) LobbySettingOld.CountdownSeconds.getDefaultValue() : defSettings.countdownSeconds));
		countdownDelaySeconds = Math.max(config.getInt(LobbySettingOld.CountdownDelaySeconds.getPath(), (Integer) LobbySettingOld.CountdownDelaySeconds.getDefaultValue()), 0);
		maxPlayers = Math.max(config.getInt(LobbySettingOld.MaxPlayers.getPath(), (Integer) LobbySettingOld.MaxPlayers.getDefaultValue()), 2);
		coloredListnames = config.getBoolean(LobbySettingOld.ColoredListnames.getPath(), (Boolean) LobbySettingOld.ColoredListnames.getDefaultValue());
		coloredChat = config.getBoolean(LobbySettingOld.ColoredChat.getPath(), (Boolean) LobbySettingOld.ColoredChat.getDefaultValue());
		shopEnabled = config.getBoolean(LobbySettingOld.ShopEnabled.getPath(), (Boolean) LobbySettingOld.ShopEnabled.getDefaultValue());
		// shop
		shop = config.getString(LobbySettingOld.Shop.getPath(), (String) LobbySettingOld.Shop.getDefaultValue());
		
		allowedCommands = (List<String>) config.getList(LobbySettingOld.AllowedCommands.getPath(), (List<String>) LobbySettingOld.AllowedCommands.getDefaultValue());
		blacklistedCommands = (List<String>) config.getList(LobbySettingOld.BlacklistedCommands.getPath(), (List<String>) LobbySettingOld.BlacklistedCommands.getDefaultValue());
		blacklistAdminOverride = config.getBoolean(LobbySettingOld.BlacklistAdminOverride.getPath(), (Boolean) LobbySettingOld.BlacklistAdminOverride.getDefaultValue());
		onlyRandom = config.getBoolean(LobbySettingOld.OnlyRandom.getPath(), (Boolean) LobbySettingOld.OnlyRandom.getDefaultValue());
		autoRandom = config.getBoolean(LobbySettingOld.AutoRandom.getPath(), (Boolean) LobbySettingOld.AutoRandom.getDefaultValue());
		autoSpecLobby = config.getBoolean(LobbySettingOld.AutoSpecLobby.getPath(), (Boolean) LobbySettingOld.AutoSpecLobby.getDefaultValue());
		afkDetectionEnabled = config.getBoolean(LobbySettingOld.AfkDetectionEnabled.getPath(), (Boolean) LobbySettingOld.AfkDetectionEnabled.getDefaultValue());
		afkRadius = Math.max(config.getInt(LobbySettingOld.AfkRadius.getPath(), (Integer) LobbySettingOld.AfkRadius.getDefaultValue()), 1);
		afkPoints = Math.max(config.getInt(LobbySettingOld.AfkPoints.getPath(), (Integer) LobbySettingOld.AfkPoints.getDefaultValue()), 1);

		// GIFTS
		giftsEnabled = config.getBoolean(LobbySettingOld.GiftsEnabled.getPath(), (Boolean) LobbySettingOld.GiftsEnabled.getDefaultValue());

		gifts = new ArrayList<Gift>();
		
		ConfigurationSection giftsEntries = config.getConfigurationSection(LobbySettingOld.Gifts.getPath());
		double allChances = 0;
		for(String key : giftsEntries.getKeys(false)) {
			int id = Math.max(giftsEntries.getConfigurationSection(key).getInt("id", 0), 0);
			int subI = Math.max(giftsEntries.getConfigurationSection(key).getInt("subid", 0), 0);
			short sub = (subI > Short.MAX_VALUE ? Short.MAX_VALUE : (short) subI);
			int amount = Math.max(giftsEntries.getConfigurationSection(key).getInt("amount", 0), 0);
			double chance = Math.min(Math.max(giftsEntries.getConfigurationSection(key).getDouble("chance", 0.0), 0.0),100.0);
			allChances += chance;
			String message = giftsEntries.getConfigurationSection(key).getString("message", "Have fun with this!");
			gifts.add(new Gift(id, sub, amount, chance, message));
		}
		giftChanceFactor = (100/allChances);
		
		wishesEnabled = config.getBoolean(LobbySettingOld.WishesEnabled.getPath(), (Boolean) LobbySettingOld.WishesEnabled.getDefaultValue());
		wishesText = config.getString(LobbySettingOld.WishesText.getPath(), (String) LobbySettingOld.WishesText.getDefaultValue());
		wishesDelayMinutes = Math.max(config.getInt(LobbySettingOld.WishesDelayMinutes.getPath(), (Integer) LobbySettingOld.WishesDelayMinutes.getDefaultValue()), 0);

		// LOBBY JOIN CHECKS
		saveInventory = config.getBoolean(LobbySettingOld.SaveInventory.getPath(), (Boolean) LobbySettingOld.SaveInventory.getDefaultValue());
		checkInventory = config.getBoolean(LobbySettingOld.CheckInventory.getPath(), (Boolean) LobbySettingOld.CheckInventory.getDefaultValue());
		checkGamemode = config.getBoolean(LobbySettingOld.CheckGamemode.getPath(), (Boolean) LobbySettingOld.CheckGamemode.getDefaultValue());
		checkFlymode = config.getBoolean(LobbySettingOld.CheckFlymode.getPath(), (Boolean) LobbySettingOld.CheckFlymode.getDefaultValue());
		checkBurningFallingDiving = config.getBoolean(LobbySettingOld.CheckBurningFallingDiving.getPath(), (Boolean) LobbySettingOld.CheckBurningFallingDiving.getDefaultValue());
		checkHealth = config.getBoolean(LobbySettingOld.CheckHealth.getPath(), (Boolean) LobbySettingOld.CheckHealth.getDefaultValue());
		checkFoodlevel = config.getBoolean(LobbySettingOld.CheckFoodlevel.getPath(), (Boolean) LobbySettingOld.CheckFoodlevel.getDefaultValue());
		checkEffects = config.getBoolean(LobbySettingOld.CheckEffects.getPath(), (Boolean) LobbySettingOld.CheckEffects.getDefaultValue());
		
	}
	
	
	
	
	
	
	
	@SuppressWarnings("unchecked")
	public LobbySettingsOld(File file) {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		for (LobbySettingOld defSetting : LobbySettingOld.values()) {
			setDefault(config, defSetting);
		}
		//GIFTS
		if (config.get(LobbySettingOld.Gifts.getPath()) == null) {
			List<Gift> giftsDef = new ArrayList<Gift>();
			giftsDef.add(new Gift(332, (short)0, 50, 30.0, "Hope you have luck with these balls!"));
			giftsDef.add(new Gift(344, (short)0, 2, 15.0, "May these grenades be with you!"));
			giftsDef.add(new Gift(390, (short)0, 2, 15.0, "I knew you ever wanted to be a sneaky killer!"));
			giftsDef.add(new Gift(356, (short)0, 2, 15.0, "Heat them with these rocket launchers!"));
			giftsDef.add(new Gift(280, (short)0, 1, 15.0, "I knew you ever wanted to order a airstrike at least once!"));
			giftsDef.add(new Gift(54, (short)0, 2, 5.0, "I got some more gifts for you!"));
			giftsDef.add(new Gift(86, (short)0, 1, 3.0, "They survived the apocalypse? But the will not survive this!"));
			giftsDef.add(new Gift(0, (short)0, 0, 2.0, "You had no luck this time :("));
			
			for(Gift g : giftsDef) {
				config.set(LobbySettingOld.Gifts.getPath()+"."+giftsDef.indexOf(g)+".message", g.getMessage());
				config.set(LobbySettingOld.Gifts.getPath()+"."+giftsDef.indexOf(g)+".id", g.getItem(false).getTypeId());
				config.set(LobbySettingOld.Gifts.getPath()+"."+giftsDef.indexOf(g)+".subid", g.getItem(false).getDurability());
				config.set(LobbySettingOld.Gifts.getPath()+"."+giftsDef.indexOf(g)+".amount", g.getItem(false).getAmount());
				config.set(LobbySettingOld.Gifts.getPath()+"."+giftsDef.indexOf(g)+".chance", g.getChance());
			}
		}
		
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//VALUES
		countdownSeconds = Math.max(config.getInt(LobbySettingOld.CountdownSeconds.getPath(), (Integer) LobbySettingOld.CountdownSeconds.getDefaultValue()), 0);
		countdownDelaySeconds = Math.max(config.getInt(LobbySettingOld.CountdownDelaySeconds.getPath(), (Integer) LobbySettingOld.CountdownDelaySeconds.getDefaultValue()), 0);
		maxPlayers = Math.max(config.getInt(LobbySettingOld.MaxPlayers.getPath(), (Integer) LobbySettingOld.MaxPlayers.getDefaultValue()), 2);
		coloredListnames = config.getBoolean(LobbySettingOld.ColoredListnames.getPath(), (Boolean) LobbySettingOld.ColoredListnames.getDefaultValue());
		coloredChat = config.getBoolean(LobbySettingOld.ColoredChat.getPath(), (Boolean) LobbySettingOld.ColoredChat.getDefaultValue());
		shopEnabled = config.getBoolean(LobbySettingOld.ShopEnabled.getPath(), (Boolean) LobbySettingOld.ShopEnabled.getDefaultValue());
		// SHOP
		shop = config.getString(LobbySettingOld.Shop.getPath(), (String) LobbySettingOld.Shop.getDefaultValue());
		
		allowedCommands = (List<String>) config.getList(LobbySettingOld.AllowedCommands.getPath(), (List<String>) LobbySettingOld.AllowedCommands.getDefaultValue());
		blacklistedCommands = (List<String>) config.getList(LobbySettingOld.BlacklistedCommands.getPath(), (List<String>) LobbySettingOld.BlacklistedCommands.getDefaultValue());
		blacklistAdminOverride = config.getBoolean(LobbySettingOld.BlacklistAdminOverride.getPath(), (Boolean) LobbySettingOld.BlacklistAdminOverride.getDefaultValue());
		onlyRandom = config.getBoolean(LobbySettingOld.OnlyRandom.getPath(), (Boolean) LobbySettingOld.OnlyRandom.getDefaultValue());
		autoRandom = config.getBoolean(LobbySettingOld.AutoRandom.getPath(), (Boolean) LobbySettingOld.AutoRandom.getDefaultValue());
		autoSpecLobby = config.getBoolean(LobbySettingOld.AutoSpecLobby.getPath(), (Boolean) LobbySettingOld.AutoSpecLobby.getDefaultValue());
		afkDetectionEnabled = config.getBoolean(LobbySettingOld.AfkDetectionEnabled.getPath(), (Boolean) LobbySettingOld.AfkDetectionEnabled.getDefaultValue());
		afkRadius = Math.max(config.getInt(LobbySettingOld.AfkRadius.getPath(), (Integer) LobbySettingOld.AfkRadius.getDefaultValue()), 1);
		afkPoints = Math.max(config.getInt(LobbySettingOld.AfkPoints.getPath(), (Integer) LobbySettingOld.AfkPoints.getDefaultValue()), 1);

		// GIFTS
		giftsEnabled = config.getBoolean(LobbySettingOld.GiftsEnabled.getPath(), (Boolean) LobbySettingOld.GiftsEnabled.getDefaultValue());

		gifts = new ArrayList<Gift>();
		
		ConfigurationSection giftsEntries = config.getConfigurationSection(LobbySettingOld.Gifts.getPath());
		double allChances = 0;
		for(String key : giftsEntries.getKeys(false)) {
			int id = Math.max(giftsEntries.getConfigurationSection(key).getInt("id", 0), 0);
			int subI = Math.max(giftsEntries.getConfigurationSection(key).getInt("subid", 0), 0);
			short sub = (subI > Short.MAX_VALUE ? Short.MAX_VALUE : (short) subI);
			int amount = Math.max(giftsEntries.getConfigurationSection(key).getInt("amount", 0), 0);
			double chance = Math.min(Math.max(giftsEntries.getConfigurationSection(key).getDouble("chance", 0.0), 0.0),100.0);
			allChances += chance;
			String message = giftsEntries.getConfigurationSection(key).getString("message", "Have fun with this!");
			gifts.add(new Gift(id, sub, amount, chance, message));
		}
		giftChanceFactor = (100/allChances);
		
		wishesEnabled = config.getBoolean(LobbySettingOld.WishesEnabled.getPath(), (Boolean) LobbySettingOld.WishesEnabled.getDefaultValue());
		wishesText = config.getString(LobbySettingOld.WishesText.getPath(), (String) LobbySettingOld.WishesText.getDefaultValue());
		wishesDelayMinutes = Math.max(config.getInt(LobbySettingOld.WishesDelayMinutes.getPath(), (Integer) LobbySettingOld.WishesDelayMinutes.getDefaultValue()), 0);

		// LOBBY JOIN CHECKS
		saveInventory = getBoolean(config, LobbySettingOld.SaveInventory);
		saveInventory = config.getBoolean(LobbySettingOld.SaveInventory.getPath(), (Boolean) LobbySettingOld.SaveInventory.getDefaultValue());
		checkInventory = config.getBoolean(LobbySettingOld.CheckInventory.getPath(), (Boolean) LobbySettingOld.CheckInventory.getDefaultValue());
		checkGamemode = config.getBoolean(LobbySettingOld.CheckGamemode.getPath(), (Boolean) LobbySettingOld.CheckGamemode.getDefaultValue());
		checkFlymode = config.getBoolean(LobbySettingOld.CheckFlymode.getPath(), (Boolean) LobbySettingOld.CheckFlymode.getDefaultValue());
		checkBurningFallingDiving = config.getBoolean(LobbySettingOld.CheckBurningFallingDiving.getPath(), (Boolean) LobbySettingOld.CheckBurningFallingDiving.getDefaultValue());
		checkHealth = config.getBoolean(LobbySettingOld.CheckHealth.getPath(), (Boolean) LobbySettingOld.CheckHealth.getDefaultValue());
		checkFoodlevel = config.getBoolean(LobbySettingOld.CheckFoodlevel.getPath(), (Boolean) LobbySettingOld.CheckFoodlevel.getDefaultValue());
		checkEffects = config.getBoolean(LobbySettingOld.CheckEffects.getPath(), (Boolean) LobbySettingOld.CheckEffects.getDefaultValue());
		
	}
	
	private boolean getBoolean(YamlConfiguration config, LobbySettingOld setting) {
		return getBoolean(config, setting, (Boolean) setting.getDefaultValue());
	}
	
	private boolean getBoolean(YamlConfiguration config, LobbySettingOld setting, boolean defaultValue) {
		return config.getBoolean(setting.getPath(), defaultValue);
	}
	
	private int getInt(YamlConfiguration config, LobbySettingOld setting) {
		return getInt(config, setting, (Integer) setting.getDefaultValue());
	}
	
	private int getInt(YamlConfiguration config, LobbySettingOld setting, int defaultValue) {
		return config.getInt(setting.getPath(), defaultValue);
	}
	
	private String getString(YamlConfiguration config, LobbySettingOld setting) {
		return getString(config, setting, (String) setting.getDefaultValue());
	}
	
	private String getString(YamlConfiguration config, LobbySettingOld setting, String defaultValue) {
		return config.getString(setting.getPath(), defaultValue);
	}
	
	
	private void setDefault(YamlConfiguration config, LobbySettingOld setting) {
		if (config.get(setting.getPath()) == null) config.set(setting.getPath(), setting.getDefaultValue());
	}
	
}
