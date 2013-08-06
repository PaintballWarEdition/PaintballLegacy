package de.blablubbabc.paintball.lobby;

import java.util.Arrays;

public enum LobbySettingOld {
	CountdownSeconds("Countdown.Seconds", 20),
	CountdownDelaySeconds("Countdown.DelaySeconds", 10),
	MaxPlayers("MaxPlayers", 1000),
	ColoredListnames("ColoredListnames", true),
	ColoredChat("ColoredChat", true),
	ShopEnabled("ShopEnabled", true),
	Shop("Shop", "Paintball Shop"),
	AllowedCommands("AllowedCommands", Arrays.asList(
			"/list",
			"/login *",
			"/register *")),
	BlacklistedCommands("BlacklistedCommands", Arrays.asList(
			"/tphere {player}",
			"/tp {args} {player}",
			"/tp {player} {args}")),
	BlacklistAdminOverride("BlacklistAdminOverride", true),
	OnlyRandom("OnlyRandom", false),
	AutoRandom("AutoRandom", false),
	AutoSpecLobby("AutoSpecLobby", true),
	AfkDetectionEnabled("AfkDetection.Enabled", true),
	AfkRadius("AfkDetection.Radius", 5),
	AfkPoints("AfkDetection.Points", 3),
	GiftsEnabled("Gifts.Enabled", true),
	Gifts("Gifts.Gifts", null),
	WishesEnabled("Gifts.Wishes.Enabled", true),
	WishesText("Gifts.Wishes.Text", "&cblablubbabc&5, &cAlphaX &5and &cthe server team &5are wishing you a lot of fun!"),
	WishesDelayMinutes("Gifts.Wishes.DelayMinutes", 60),
	SaveInventory("SaveInventory", true),
	CheckInventory("Check.Inventory", true),
	CheckGamemode("Check.Gamemode", true),
	CheckFlymode("Check.Flymode", true),
	CheckBurningFallingDiving("Check.Burning, Falling, Diving", true),
	CheckHealth("Check.Health", true),
	CheckFoodlevel("Check.Foodlevel", true),
	CheckEffects("Check.Effects", true);
	
	private String path;
	private Object defaultValue;
	
	LobbySettingOld(String path, Object defaultValue) {
		this.path = path;
		this.defaultValue = defaultValue;
	}

	public String getPath() {
		return path;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}
}
