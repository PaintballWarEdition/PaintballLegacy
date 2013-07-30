package de.blablubbabc.paintball.lobby;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import de.blablubbabc.paintball.gadgets.Gift;

public class LobbySettings {
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
	public LobbySettings(File file, LobbySettings defSettings) {
		boolean isDefault = defSettings == null;
		
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		// INITIALIZE DEFAULT CONFIG:
		if (isDefault) {
			for (LobbySetting defSetting : LobbySetting.values()) {
				setDefault(config, defSetting);
			}
			
			// gifts
			if (config.get(LobbySetting.Gifts.getPath()) == null) {
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
					config.set(LobbySetting.Gifts.getPath()+"."+giftsDef.indexOf(g)+".message", g.getMessage());
					config.set(LobbySetting.Gifts.getPath()+"."+giftsDef.indexOf(g)+".id", g.getItem(false).getTypeId());
					config.set(LobbySetting.Gifts.getPath()+"."+giftsDef.indexOf(g)+".subid", g.getItem(false).getDurability());
					config.set(LobbySetting.Gifts.getPath()+"."+giftsDef.indexOf(g)+".amount", g.getItem(false).getAmount());
					config.set(LobbySetting.Gifts.getPath()+"."+giftsDef.indexOf(g)+".chance", g.getChance());
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
		countdownSeconds = Math.max(0, config.getInt(LobbySetting.CountdownSeconds.getPath(), isDefault ? (Integer) LobbySetting.CountdownSeconds.getDefaultValue() : defSettings.countdownSeconds));
		countdownDelaySeconds = Math.max(config.getInt(LobbySetting.CountdownDelaySeconds.getPath(), (Integer) LobbySetting.CountdownDelaySeconds.getDefaultValue()), 0);
		maxPlayers = Math.max(config.getInt(LobbySetting.MaxPlayers.getPath(), (Integer) LobbySetting.MaxPlayers.getDefaultValue()), 2);
		coloredListnames = config.getBoolean(LobbySetting.ColoredListnames.getPath(), (Boolean) LobbySetting.ColoredListnames.getDefaultValue());
		coloredChat = config.getBoolean(LobbySetting.ColoredChat.getPath(), (Boolean) LobbySetting.ColoredChat.getDefaultValue());
		shopEnabled = config.getBoolean(LobbySetting.ShopEnabled.getPath(), (Boolean) LobbySetting.ShopEnabled.getDefaultValue());
		// shop
		shop = config.getString(LobbySetting.Shop.getPath(), (String) LobbySetting.Shop.getDefaultValue());
		
		allowedCommands = (List<String>) config.getList(LobbySetting.AllowedCommands.getPath(), (List<String>) LobbySetting.AllowedCommands.getDefaultValue());
		blacklistedCommands = (List<String>) config.getList(LobbySetting.BlacklistedCommands.getPath(), (List<String>) LobbySetting.BlacklistedCommands.getDefaultValue());
		blacklistAdminOverride = config.getBoolean(LobbySetting.BlacklistAdminOverride.getPath(), (Boolean) LobbySetting.BlacklistAdminOverride.getDefaultValue());
		onlyRandom = config.getBoolean(LobbySetting.OnlyRandom.getPath(), (Boolean) LobbySetting.OnlyRandom.getDefaultValue());
		autoRandom = config.getBoolean(LobbySetting.AutoRandom.getPath(), (Boolean) LobbySetting.AutoRandom.getDefaultValue());
		autoSpecLobby = config.getBoolean(LobbySetting.AutoSpecLobby.getPath(), (Boolean) LobbySetting.AutoSpecLobby.getDefaultValue());
		afkDetectionEnabled = config.getBoolean(LobbySetting.AfkDetectionEnabled.getPath(), (Boolean) LobbySetting.AfkDetectionEnabled.getDefaultValue());
		afkRadius = Math.max(config.getInt(LobbySetting.AfkRadius.getPath(), (Integer) LobbySetting.AfkRadius.getDefaultValue()), 1);
		afkPoints = Math.max(config.getInt(LobbySetting.AfkPoints.getPath(), (Integer) LobbySetting.AfkPoints.getDefaultValue()), 1);

		// GIFTS
		giftsEnabled = config.getBoolean(LobbySetting.GiftsEnabled.getPath(), (Boolean) LobbySetting.GiftsEnabled.getDefaultValue());

		gifts = new ArrayList<Gift>();
		
		ConfigurationSection giftsEntries = config.getConfigurationSection(LobbySetting.Gifts.getPath());
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
		
		wishesEnabled = config.getBoolean(LobbySetting.WishesEnabled.getPath(), (Boolean) LobbySetting.WishesEnabled.getDefaultValue());
		wishesText = config.getString(LobbySetting.WishesText.getPath(), (String) LobbySetting.WishesText.getDefaultValue());
		wishesDelayMinutes = Math.max(config.getInt(LobbySetting.WishesDelayMinutes.getPath(), (Integer) LobbySetting.WishesDelayMinutes.getDefaultValue()), 0);

		// LOBBY JOIN CHECKS
		saveInventory = config.getBoolean(LobbySetting.SaveInventory.getPath(), (Boolean) LobbySetting.SaveInventory.getDefaultValue());
		checkInventory = config.getBoolean(LobbySetting.CheckInventory.getPath(), (Boolean) LobbySetting.CheckInventory.getDefaultValue());
		checkGamemode = config.getBoolean(LobbySetting.CheckGamemode.getPath(), (Boolean) LobbySetting.CheckGamemode.getDefaultValue());
		checkFlymode = config.getBoolean(LobbySetting.CheckFlymode.getPath(), (Boolean) LobbySetting.CheckFlymode.getDefaultValue());
		checkBurningFallingDiving = config.getBoolean(LobbySetting.CheckBurningFallingDiving.getPath(), (Boolean) LobbySetting.CheckBurningFallingDiving.getDefaultValue());
		checkHealth = config.getBoolean(LobbySetting.CheckHealth.getPath(), (Boolean) LobbySetting.CheckHealth.getDefaultValue());
		checkFoodlevel = config.getBoolean(LobbySetting.CheckFoodlevel.getPath(), (Boolean) LobbySetting.CheckFoodlevel.getDefaultValue());
		checkEffects = config.getBoolean(LobbySetting.CheckEffects.getPath(), (Boolean) LobbySetting.CheckEffects.getDefaultValue());
		
	}
	
	
	
	
	
	
	
	@SuppressWarnings("unchecked")
	public LobbySettings(File file) {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		for (LobbySetting defSetting : LobbySetting.values()) {
			setDefault(config, defSetting);
		}
		//GIFTS
		if (config.get(LobbySetting.Gifts.getPath()) == null) {
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
				config.set(LobbySetting.Gifts.getPath()+"."+giftsDef.indexOf(g)+".message", g.getMessage());
				config.set(LobbySetting.Gifts.getPath()+"."+giftsDef.indexOf(g)+".id", g.getItem(false).getTypeId());
				config.set(LobbySetting.Gifts.getPath()+"."+giftsDef.indexOf(g)+".subid", g.getItem(false).getDurability());
				config.set(LobbySetting.Gifts.getPath()+"."+giftsDef.indexOf(g)+".amount", g.getItem(false).getAmount());
				config.set(LobbySetting.Gifts.getPath()+"."+giftsDef.indexOf(g)+".chance", g.getChance());
			}
		}
		
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//VALUES
		countdownSeconds = Math.max(config.getInt(LobbySetting.CountdownSeconds.getPath(), (Integer) LobbySetting.CountdownSeconds.getDefaultValue()), 0);
		countdownDelaySeconds = Math.max(config.getInt(LobbySetting.CountdownDelaySeconds.getPath(), (Integer) LobbySetting.CountdownDelaySeconds.getDefaultValue()), 0);
		maxPlayers = Math.max(config.getInt(LobbySetting.MaxPlayers.getPath(), (Integer) LobbySetting.MaxPlayers.getDefaultValue()), 2);
		coloredListnames = config.getBoolean(LobbySetting.ColoredListnames.getPath(), (Boolean) LobbySetting.ColoredListnames.getDefaultValue());
		coloredChat = config.getBoolean(LobbySetting.ColoredChat.getPath(), (Boolean) LobbySetting.ColoredChat.getDefaultValue());
		shopEnabled = config.getBoolean(LobbySetting.ShopEnabled.getPath(), (Boolean) LobbySetting.ShopEnabled.getDefaultValue());
		// SHOP
		shop = config.getString(LobbySetting.Shop.getPath(), (String) LobbySetting.Shop.getDefaultValue());
		
		allowedCommands = (List<String>) config.getList(LobbySetting.AllowedCommands.getPath(), (List<String>) LobbySetting.AllowedCommands.getDefaultValue());
		blacklistedCommands = (List<String>) config.getList(LobbySetting.BlacklistedCommands.getPath(), (List<String>) LobbySetting.BlacklistedCommands.getDefaultValue());
		blacklistAdminOverride = config.getBoolean(LobbySetting.BlacklistAdminOverride.getPath(), (Boolean) LobbySetting.BlacklistAdminOverride.getDefaultValue());
		onlyRandom = config.getBoolean(LobbySetting.OnlyRandom.getPath(), (Boolean) LobbySetting.OnlyRandom.getDefaultValue());
		autoRandom = config.getBoolean(LobbySetting.AutoRandom.getPath(), (Boolean) LobbySetting.AutoRandom.getDefaultValue());
		autoSpecLobby = config.getBoolean(LobbySetting.AutoSpecLobby.getPath(), (Boolean) LobbySetting.AutoSpecLobby.getDefaultValue());
		afkDetectionEnabled = config.getBoolean(LobbySetting.AfkDetectionEnabled.getPath(), (Boolean) LobbySetting.AfkDetectionEnabled.getDefaultValue());
		afkRadius = Math.max(config.getInt(LobbySetting.AfkRadius.getPath(), (Integer) LobbySetting.AfkRadius.getDefaultValue()), 1);
		afkPoints = Math.max(config.getInt(LobbySetting.AfkPoints.getPath(), (Integer) LobbySetting.AfkPoints.getDefaultValue()), 1);

		// GIFTS
		giftsEnabled = config.getBoolean(LobbySetting.GiftsEnabled.getPath(), (Boolean) LobbySetting.GiftsEnabled.getDefaultValue());

		gifts = new ArrayList<Gift>();
		
		ConfigurationSection giftsEntries = config.getConfigurationSection(LobbySetting.Gifts.getPath());
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
		
		wishesEnabled = config.getBoolean(LobbySetting.WishesEnabled.getPath(), (Boolean) LobbySetting.WishesEnabled.getDefaultValue());
		wishesText = config.getString(LobbySetting.WishesText.getPath(), (String) LobbySetting.WishesText.getDefaultValue());
		wishesDelayMinutes = Math.max(config.getInt(LobbySetting.WishesDelayMinutes.getPath(), (Integer) LobbySetting.WishesDelayMinutes.getDefaultValue()), 0);

		// LOBBY JOIN CHECKS
		saveInventory = config.getBoolean(LobbySetting.SaveInventory.getPath(), (Boolean) LobbySetting.SaveInventory.getDefaultValue());
		checkInventory = config.getBoolean(LobbySetting.CheckInventory.getPath(), (Boolean) LobbySetting.CheckInventory.getDefaultValue());
		checkGamemode = config.getBoolean(LobbySetting.CheckGamemode.getPath(), (Boolean) LobbySetting.CheckGamemode.getDefaultValue());
		checkFlymode = config.getBoolean(LobbySetting.CheckFlymode.getPath(), (Boolean) LobbySetting.CheckFlymode.getDefaultValue());
		checkBurningFallingDiving = config.getBoolean(LobbySetting.CheckBurningFallingDiving.getPath(), (Boolean) LobbySetting.CheckBurningFallingDiving.getDefaultValue());
		checkHealth = config.getBoolean(LobbySetting.CheckHealth.getPath(), (Boolean) LobbySetting.CheckHealth.getDefaultValue());
		checkFoodlevel = config.getBoolean(LobbySetting.CheckFoodlevel.getPath(), (Boolean) LobbySetting.CheckFoodlevel.getDefaultValue());
		checkEffects = config.getBoolean(LobbySetting.CheckEffects.getPath(), (Boolean) LobbySetting.CheckEffects.getDefaultValue());
		
	}
	
	private void setDefault(YamlConfiguration config, LobbySetting defSetting) {
		if (config.get(defSetting.getPath()) == null) config.set(defSetting.getPath(), defSetting.getDefaultValue());
	}
	
}
