package me.blablubbabc.paintball;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportFix implements Listener {
	private Server server;
	private Paintball plugin;
	private final int TELEPORT_FIX_DELAY = 15; // ticks
	final int visibleDistance;
	
	public TeleportFix(Paintball plugin) {
		this.plugin = plugin;
		this.server = plugin.getServer();
		visibleDistance = server.getViewDistance() * 16;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		final Player player = event.getPlayer();
		if(plugin.teleportFix && Lobby.LOBBY.isMember(player)) {
			// Fix the visibility issue one tick later
			server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					// Refresh nearby clients
					final List<Player> nearby = getPlayersWithin(player, visibleDistance);
					// Hide every player
					updateEntities(player, nearby, false);
					// Then show them again
					server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						@Override
						public void run() {
							updateEntities(player, nearby, true);
						}
					}, 1);
				}
			}, TELEPORT_FIX_DELAY);
		}
	}

	private void updateEntities(Player tpedPlayer, List<Player> players, boolean visible) {
		// Hide or show every player to tpedPlayer
		// and hide or show tpedPlayer to every player.
		for (Player player : players) {
			if (visible) {
				tpedPlayer.showPlayer(player);
				player.showPlayer(tpedPlayer);
			} else {
				tpedPlayer.hidePlayer(player);
				player.hidePlayer(tpedPlayer);	
			}
		}
	}

	private List<Player> getPlayersWithin(Player player, int distance) {
		List<Player> res = new ArrayList<Player>();
		int d2 = distance * distance;
		for (Player p : server.getOnlinePlayers()) {
			if (p != player && p.getWorld() == player.getWorld() && p.getLocation().distanceSquared(player.getLocation()) <= d2) {
				res.add(p);
			}
		}
		return res;
	}
}