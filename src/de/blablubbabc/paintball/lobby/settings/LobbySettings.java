package de.blablubbabc.paintball.lobby.settings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.configuration.file.YamlConfiguration;

import de.blablubbabc.paintball.shop.Shop;

public class LobbySettings {

	private final String settingsName;
	private final YamlConfiguration config;
	
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
	public boolean ranksLobbyArmorEnabled;
	public boolean ranksChatPrefixEnabled;
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
	
	@SuppressWarnings("unchecked")
	public LobbySettings(String settingsName, File file, LobbySettings defSettings) {
		this.settingsName = settingsName;
		boolean isDefault = (defSettings == null);
		config = YamlConfiguration.loadConfiguration(file);
		
		// INITIALIZE DEFAULT CONFIG:
		if (isDefault) {
			for (LobbySetting defSetting : LobbySetting.values()) {
				setDefault(config, defSetting);
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
		ranksLobbyArmorEnabled = config.getBoolean(LobbySetting.RanksLobbyArmorEnabled.getPath(), isDefault ? (Boolean) LobbySetting.RanksLobbyArmorEnabled.getDefaultValue() : defSettings.ranksLobbyArmorEnabled);
		ranksChatPrefixEnabled = config.getBoolean(LobbySetting.RanksChatPrefixEnabled.getPath(), isDefault ? (Boolean) LobbySetting.RanksChatPrefixEnabled.getDefaultValue() : defSettings.ranksChatPrefixEnabled);
		ranksChatPrefixOnlyForPaintballers = config.getBoolean(LobbySetting.RanksChatPrefixOnlyForPaintballers.getPath(), isDefault ? (Boolean) LobbySetting.RanksChatPrefixOnlyForPaintballers.getDefaultValue() : defSettings.ranksChatPrefixOnlyForPaintballers);
		ranksAdminBypassShop = config.getBoolean(LobbySetting.RanksAdminBypassShop.getPath(), isDefault ? (Boolean) LobbySetting.RanksAdminBypassShop.getDefaultValue() : defSettings.ranksAdminBypassShop);

		// Match related settings:
		
		// damage
		falldamage = config.getBoolean(LobbySetting.FallDamage.getPath(), isDefault ? (Boolean) LobbySetting.FallDamage.getDefaultValue() : defSettings.falldamage);
		otherDamage = config.getBoolean(LobbySetting.OtherDamage.getPath(), isDefault ? (Boolean) LobbySetting.OtherDamage.getDefaultValue() : defSettings.otherDamage);
		
		// melee
		allowMelee = config.getBoolean(LobbySetting.AllowMelee.getPath(), isDefault ? (Boolean) LobbySetting.AllowMelee.getDefaultValue() : defSettings.allowMelee);
		meleeDamage = Math.max(0, config.getInt(LobbySetting.MeleeDamage.getPath(), isDefault ? (Integer) LobbySetting.MeleeDamage.getDefaultValue() : defSettings.meleeDamage));
		
		autoSpecLobby = config.getBoolean(LobbySetting.AutoSpecLobby.getPath(), isDefault ? (Boolean) LobbySetting.AutoSpecLobby.getDefaultValue() : defSettings.autoSpecLobby);
		
		// gamemodes have to trigger when to check for afk state or when to mark
		// player as non-afk
		afkDetectionEnabled = config.getBoolean(LobbySetting.AfkDetectionEnabled.getPath(), isDefault ? (Boolean) LobbySetting.AfkDetectionEnabled.getDefaultValue() : defSettings.afkDetectionEnabled);
		afkRadius = Math.max(1, config.getInt(LobbySetting.AfkRadius.getPath(), isDefault ? (Integer) LobbySetting.AfkRadius.getDefaultValue() : defSettings.afkRadius));
		afkRadiusSquared = afkRadius * afkRadius;
		afkCounter = Math.max(1, config.getInt(LobbySetting.AfkCounter.getPath(), isDefault ? (Integer) LobbySetting.AfkCounter.getDefaultValue() : defSettings.afkCounter));
		
		// whether shop shall be disable through out all games in this lobby
		shopEnabled = config.getBoolean(LobbySetting.ShopEnabled.getPath(), isDefault ? (Boolean) LobbySetting.ShopEnabled.getDefaultValue() : defSettings.shopEnabled);
		// the by default suggested shop for games in this lobby. 
		// Gamemode can decide if they want to use this.
		if (isDefault) {
			String shopName = config.getString(LobbySetting.ShopName.getPath(), (String) LobbySetting.ShopName.getDefaultValue());
			//shop = ShopManager.getShopByName(name);
		} else {
			shop = defSettings.shop;
		}
		
		
		
		// SAVE CONFIG (creates the file, if it didn't exist before)
		saveToFile(file);
	}
	
	public String getSettingsName() {
		return settingsName;
	}
	
	private void setDefault(YamlConfiguration config, LobbySetting setting) {
		if (config.get(setting.getPath()) == null) config.set(setting.getPath(), setting.getDefaultValue());
	}
	
	public void saveToFile(File file) {
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
