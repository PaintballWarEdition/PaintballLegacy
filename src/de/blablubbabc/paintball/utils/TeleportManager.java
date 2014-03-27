package de.blablubbabc.paintball.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import de.blablubbabc.paintball.Lobby;
import de.blablubbabc.paintball.Paintball;

public class TeleportManager implements Listener {
	private static Set<String> teleportRequests = new HashSet<String>();

	// TELEPORT (ANTI-INVISIBLE) FIX
	private final int TELEPORT_FIX_DELAY = 15; // ticks

	public TeleportManager() {
		Bukkit.getPluginManager().registerEvents(this, Paintball.instance);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		final Player player = event.getPlayer();
		final String playerName = player.getName();

		if (teleportRequests.remove(playerName)) {
			// make sure that the teleport isn't blocked by something:
			event.setCancelled(false);
			if (Paintball.instance.teleportFix) {
				this.handleTeleportFix(player);
			}
		} else {
			// workaround: essentials tpaccept command would otherwise allow players to be teleported out of the match/lobby
			// they use TeleportCause.COMMAND, so we can detect this:
			if (event.getCause() == TeleportCause.COMMAND && Lobby.LOBBY.isMember(player)) {
				event.setCancelled(true);
				Log.debug("Cancelled teleport attempt of player '" + playerName + "'.");
			}
		}
	}

	//TODO Check if this currently is sending unneeded player update stuff on match end and start
	private void handleTeleportFix(final Player player) {
		// fix the visibility issue one tick later:
		Bukkit.getScheduler().runTaskLater(Paintball.instance, new Runnable() {
			@Override
			public void run() {
				if (!player.isOnline()) return;
				// refresh nearby players:
				final int viewDistance = Bukkit.getServer().getViewDistance() * 16;
				final List<Player> nearby = getNearbyPlayers(player, viewDistance);
				// hide every player:
				updateEntities(player, nearby, false);
				// then show them again:
				Bukkit.getScheduler().runTaskLater(Paintball.instance, new Runnable() {
					@Override
					public void run() {
						if (!player.isOnline()) return;
						updateEntities(player, nearby, true);
					}
				}, 1);
			}
		}, TELEPORT_FIX_DELAY);
	}

	private void updateEntities(final Player tpedPlayer, final List<Player> players, final boolean visible) {
		// hide or show every player to tpedPlayer
		// and hide or show tpedPlayer to every player.
		for (Player player : players) {
			if (!player.isOnline()) continue;
			if (visible) {
				tpedPlayer.showPlayer(player);
				player.showPlayer(tpedPlayer);
			} else {
				tpedPlayer.hidePlayer(player);
				player.hidePlayer(tpedPlayer);
			}
		}
	}

	private List<Player> getNearbyPlayers(final Player player, final int distance) {
		final List<Player> result = new ArrayList<Player>();
		final int d2 = distance * distance;
		final Location playerLocation = player.getLocation();
		final UUID worldId = playerLocation.getWorld().getUID();
		for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
			if (otherPlayer != player && otherPlayer.getWorld().getUID() == worldId && otherPlayer.getLocation().distanceSquared(playerLocation) <= d2) {
				result.add(otherPlayer);
			}
		}
		return result;
	}

	public static void teleport(final Player player, final Location location) {
		assert player != null && location != null;
		player.closeInventory();
		player.eject();
		player.leaveVehicle();

		// load the chunk if it isn't already loaded:
		// TODO check if this is needed, useful (if it helps with the 'falling through block' problem) or not
		Chunk chunk = location.getChunk();
		if (!chunk.isLoaded()) {
			// this shouldn't be reached as getChunk() above already loads the chunk..
			Log.debug("Chunk isn't loaded. Loading it now.");
			chunk.load();
		}

		final String playerName = player.getName();
		teleportRequests.add(playerName);

		if (!player.teleport(location)) {
			Log.debug("Teleport player '" + playerName + "' failed!");
		}
	}
}