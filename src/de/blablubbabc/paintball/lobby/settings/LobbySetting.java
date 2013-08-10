package de.blablubbabc.paintball.lobby.settings;

import java.util.Arrays;

public enum LobbySetting {
	CountdownSeconds("Countdown.Seconds", 20),
	CountdownDelaySeconds("Countdown.Delay Seconds", 10),
	MinPlayers("Min Players", 2),
	MaxPlayers("Max Players", 100),
	OnlyRandom("Only Random", false),
	AutoRandom("Auto Random", false),
	
	AllowedCommands("AllowedCommands", Arrays.asList(
			"/list",
			"/login *",
			"/register *")),
	BlacklistEnabled("Blacklisted Commands.Enabled", false),
	BlacklistAdminOverride("Blacklisted Commands.Admin Override", true),
	BlacklistedCommands("Blacklisted Commands.Commands", Arrays.asList(
			"/tphere {player}",
			"/tp {args} {player}",
			"/tp {player} {args}")),
	
	MatchRotationRandom("Match Rotation.Random Rotation", true),
	// match voting:
	MatchVotingEnabled("Match Rotation.Match Voting.enabled", true),
	MatchVotingNumberOfOptions("Match Rotation.Match Voting.Number of Vote Options (at least 2)", 4),
	MatchVotingRandomOption("Match Rotation.Match Voting.Random Arena Option", true),
	MatchVotingBroadcastTime("Match Rotation.Match Voting.Broadcast Options again at Countdown Time", 15),
	MatchVotingEndVotingTime("Match Rotation.Match Voting.End Voting at Countdown Time", 5),
	
	// ranks
	RanksChatPrefix("Ranks.Chat Prefix.enabled", true),
	RanksChatPrefixOnlyForPaintballers("Ranks.Chat Prefix.Only visible for Paintballers", true),
	RanksLobbyArmor("Ranks.Lobby Armor", true),
	RanksAdminBypassShop("Ranks.Admins Bypass Shop Restrictions", false),
	
	// Match related settings:
	FallDamage("Overall Match Settings.Damage.Fall Damage", false),
	OtherDamage("Overall Match Settings.Damage.Other Damage", true),
	AllowMelee("Overall Match Settings.Melee.Enabled", true),
	MeleeDamage("Overall Match Settings.Melee.Damage", 5),
	AutoSpecLobby("Overall Match Settings.Auto Spec Lobby", true),
	AfkDetectionEnabled("Overall Match Settings.AfkDetection.Enabled", true),
	AfkRadius("Overall Match Settings.AfkDetection.Radius", 5),
	AfkCounter("Overall Match Settings.AfkDetection.Counter", 3),
	ColoredListnames("Overall Match Settings.ColoredListnames", true),
	ColoredChat("Overall Match Settings.ColoredChat", true),
	ShopEnabled("Overall Match Settings.Shop.Enabled", true),
	Shop("Overall Match Settings.Shop.Shop ID", "default");
	
	private String path;
	private Object defaultValue;
	
	LobbySetting(String path, Object defaultValue) {
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
