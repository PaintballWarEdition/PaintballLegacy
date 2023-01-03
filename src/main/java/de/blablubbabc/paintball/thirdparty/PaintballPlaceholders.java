package de.blablubbabc.paintball.thirdparty;

import org.bukkit.entity.Player;

import java.util.Map;

import de.blablubbabc.paintball.Lobby;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.Rank;
import de.blablubbabc.paintball.statistics.arena.ArenaStat;
import de.blablubbabc.paintball.statistics.general.GeneralStat;
import de.blablubbabc.paintball.statistics.player.PlayerStat;
import de.blablubbabc.paintball.statistics.player.PlayerStats;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PaintballPlaceholders extends PlaceholderExpansion {

	private final Paintball paintballPlugin;

	public PaintballPlaceholders(Paintball paintballPlugin) {
		this.paintballPlugin = paintballPlugin;
	}

	@Override
	public String getAuthor() {
		return paintballPlugin.getDescription().getAuthors().get(0);
	}

	@Override
	public String getIdentifier() {
		return paintballPlugin.getDescription().getName();
	}

	@Override
	public String getVersion() {
		// Extension version
		return "1.0";
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public String onPlaceholderRequest(Player player, String identifier) {
		String[] generalArgs = identifier.split("_", 2);
		if (generalArgs.length < 2) return null;

		if (generalArgs[0].equalsIgnoreCase("general")) {
			GeneralStat stat = GeneralStat.getFromKey(generalArgs[1]);
			if (stat == null) return null;

			return Integer.toString(paintballPlugin.statsManager.getGerneralStats().get(stat));
		} else if (generalArgs[0].equalsIgnoreCase("player")) {
			PlayerStat stat = PlayerStat.getFromKey(generalArgs[1]);
			if (stat == null) return null;

			PlayerStats stats = paintballPlugin.playerManager.getPlayerStats(player.getUniqueId());
			if (stats == null) return null;

			return Integer.toString(stats.getStat(stat));
		} else if (generalArgs[0].equalsIgnoreCase("arena")) {
			String[] arenaArgs = generalArgs[1].split(":", 2);
			Map<ArenaStat, Integer> arenaStats = paintballPlugin.arenaManager.getArenaStats(arenaArgs[0]);
			if (arenaStats.size() == 0) return null;

			ArenaStat stat = ArenaStat.getFromKey(arenaArgs[1]);
			if (stat == null) return null;

			return Integer.toString(arenaStats.get(stat));
		} else if (generalArgs[0].equalsIgnoreCase("misc")) {

			if (generalArgs[1].equalsIgnoreCase("rankprefix")) {
				Rank rank = paintballPlugin.rankManager.getRank(player.getUniqueId());
				if (rank == null) return null;

				return rank.getPrefix();
			} else if (generalArgs[1].equalsIgnoreCase("chatcolor")) {
				// Don't return null, because null doesn't replace the placeholder
				if (!Lobby.LOBBY.isMember(player)) return "";

				if (!Lobby.isPlaying(player) && !Lobby.isSpectating(player)) return "";

				return paintballPlugin.matchManager.getMatch(player).getTeamLobby(player).color().toString();
			}
		}
		return null;
	}
}
