package de.blablubbabc.paintball.lobby.settings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import de.blablubbabc.paintball.shop.Shop;

public class LobbySettings {

	// SETTINGS:
	// countdown seconds
	public int countdownSeconds;
	// delay
	public int countdownDelay;
	
	// when to start searching for a game
	public int minPlayers;
	// when to stop players from joining a team
	public int maxPlayers;
	
	// team selection
	public boolean onlyRandom;
	public boolean autoRandom;
	
	// command blocking
	public List<String> allowedCommands;
	public boolean blacklistEnabled;
	public boolean blacklistAdminOverride;
	public List<String> blacklistedCommandsRegex;
	
	// arena rotation
	public boolean matchRotationRandom;

	// match voting
	public boolean matchVotingEnabled;
	public int matchVotingNumberOfOptions;
	public boolean matchVotingRandomOption;
	public int matchVotingBroadcastOptionsAtCountdownTime;
	public int matchVotingEndAtCountdownTime;

	// ranks
	public boolean ranksLobbyArmor;
	public boolean ranksChatPrefix;
	public boolean ranksChatPrefixOnlyForPaintballers;
	public boolean ranksAdminBypassShop;

	// Match related settings:
	
	// damage
	public boolean falldamage;
	public boolean otherDamage;
	
	// melee
	public boolean allowMelee;
	public int meleeDamage;
	
	public boolean autoSpecLobby;
	
	// gamemodes have to trigger when to check for afk state or when to mark
	// player as non-afk
	public boolean afkDetectionEnabled;
	public int afkRadius;
	public int afkRadiusSquared;
	public int afkCounter;
	
	// whether shop shall be disable through out all games in this lobby
	public boolean shopEnabled;

	// the by default suggested shop for games in this lobby. Gamemode settings
	// can decide if they want to use this.
	public Shop shop;

	public LobbySettings(File file, LobbySettings defSettings) {
		boolean isDefault = (defSettings == null);
		
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		// INITIALIZE DEFAULT CONFIG:
		if (isDefault) {
			for (LobbySetting defSetting : LobbySetting.values()) {
				setDefault(config, defSetting);
			}
			
			// SAVE CONFIG
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		// READ SETTINGS
		
		// countdown seconds
		countdownSeconds = Math.max(0, config.getInt(LobbySetting.CountdownSeconds.getPath(), isDefault ? (Integer) LobbySetting.CountdownSeconds.getDefaultValue() : defSettings.countdownSeconds));
		// delay
		countdownDelay = Math.max(0, config.getInt(LobbySetting.CountdownSeconds.getPath(), isDefault ? (Integer) LobbySetting.CountdownDelaySeconds.getDefaultValue() : defSettings.countdownDelay));
		
		// when to start searching for a game
		minPlayers = Math.max(1, config.getInt(LobbySetting.MinPlayers.getPath(), isDefault ? (Integer) LobbySetting.MinPlayers.getDefaultValue() : defSettings.minPlayers));
		// when to stop players from joining a team
		maxPlayers = Math.max(1, config.getInt(LobbySetting.MaxPlayers.getPath(), isDefault ? (Integer) LobbySetting.MaxPlayers.getDefaultValue() : defSettings.maxPlayers));
		
		// team selection
		onlyRandom = config.getBoolean(LobbySetting.OnlyRandom.getPath(), isDefault ? (Boolean) LobbySetting.OnlyRandom.getDefaultValue() : defSettings.onlyRandom);
		autoRandom = config.getBoolean(LobbySetting.AutoRandom.getPath(), isDefault ? (Boolean) LobbySetting.AutoRandom.getDefaultValue() : defSettings.autoRandom);
		
		// command blocking
		allowedCommands = (List<String>) config.getList(LobbySetting.AllowedCommands.getPath(), isDefault ? (List<String>) LobbySetting.AllowedCommands.getDefaultValue() : defSettings.allowedCommands);
		
		blacklistEnabled = config.getBoolean(LobbySetting.BlacklistEnabled.getPath(), isDefault ? (Boolean) LobbySetting.BlacklistEnabled.getDefaultValue() : defSettings.blacklistEnabled);
		blacklistAdminOverride = config.getBoolean(LobbySetting.BlacklistAdminOverride.getPath(), isDefault ? (Boolean) LobbySetting.BlacklistAdminOverride.getDefaultValue() : defSettings.blacklistAdminOverride);
		
		if (isDefault) {
			List<String> blacklistedCommands = (List<String>) config.getList(LobbySetting.AllowedCommands.getPath(), (List<String>) LobbySetting.BlacklistedCommands.getDefaultValue());
			
			blacklistedCommandsRegex = new ArrayList<String>();
			for (String black : blacklistedCommands) {
				String[] split = black.split(" ");
				if (split.length == 0) continue;
				String regex = Pattern.quote(split[0]);
				for (int i = 1; i < split.length; i++) {
					String s = split[i];
					if(s.equals("{args}")) {
						regex += " \\S*";
					} else if(s.equals("{player}")) {
						regex += " {player}";
					} else {
						regex += Pattern.quote(" "+s);
					}
				}
				blacklistedCommandsRegex.add(regex);
			}
		} else {
			blacklistedCommandsRegex = defSettings.blacklistedCommandsRegex;
		}
		
		// arena rotation
		matchRotationRandom = config.getBoolean(LobbySetting.MatchRotationRandom.getPath(), isDefault ? (Boolean) LobbySetting.MatchRotationRandom.getDefaultValue() : defSettings.matchRotationRandom);

		// match voting
		matchVotingEnabled = config.getBoolean(LobbySetting.MatchVotingEnabled.getPath(), isDefault ? (Boolean) LobbySetting.MatchVotingEnabled.getDefaultValue() : defSettings.matchVotingEnabled);
		matchVotingNumberOfOptions = Math.max(2, config.getInt(LobbySetting.MatchVotingNumberOfOptions.getPath(), isDefault ? (Integer) LobbySetting.MatchVotingNumberOfOptions.getDefaultValue() : defSettings.matchVotingNumberOfOptions));
		matchVotingRandomOption = config.getBoolean(LobbySetting.MatchVotingRandomOption.getPath(), isDefault ? (Boolean) LobbySetting.MatchVotingRandomOption.getDefaultValue() : defSettings.matchVotingRandomOption);
		matchVotingBroadcastOptionsAtCountdownTime = config.getInt(LobbySetting.MatchVotingBroadcastTime.getPath(), isDefault ? (Integer) LobbySetting.MatchVotingBroadcastTime.getDefaultValue() : defSettings.matchVotingBroadcastOptionsAtCountdownTime);
		matchVotingEndAtCountdownTime = config.getInt(LobbySetting.MatchVotingEndVotingTime.getPath(), isDefault ? (Integer) LobbySetting.MatchVotingEndVotingTime.getDefaultValue() : defSettings.matchVotingEndAtCountdownTime);

		// ranks
		ranksLobbyArmor;
		ranksChatPrefix;
		ranksChatPrefixOnlyForPaintballers;
		ranksAdminBypassShop;

		// Match related settings:
		
		// damage
		falldamage;
		otherDamage;
		
		// melee
		allowMelee;
		meleeDamage;
		
		autoSpecLobby;
		
		// gamemodes have to trigger when to check for afk state or when to mark
		// player as non-afk
		afkDetectionEnabled;
		afkRadius;
		afkRadiusSquared;
		afkCounter;
		
		// whether shop shall be disable through out all games in this lobby
		shopEnabled;
		// the by default suggested shop for games in this lobby. 
		// Gamemode can decide if they want to use this.
		shop;
		
		
		
		
		
		
		
		
		
		
		
		
		
		
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
	public LobbySettings(File file) {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		for (LobbySetting defSetting : LobbySetting.values()) {
			setDefault(config, defSetting);
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
	
	
	private void setDefault(YamlConfiguration config, LobbySetting setting) {
		if (config.get(setting.getPath()) == null) config.set(setting.getPath(), setting.getDefaultValue());
	}
	
	
}
