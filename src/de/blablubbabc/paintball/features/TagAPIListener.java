package de.blablubbabc.paintball.features;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;

import de.blablubbabc.paintball.Lobby;
import de.blablubbabc.paintball.Match;
import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.utils.Log;

public class TagAPIListener implements Listener {
	private Paintball plugin;

	public TagAPIListener(Paintball plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onTagRecieve(AsyncPlayerReceiveNameTagEvent event) {
		this.handleAsyncEvent(event);
	}
	
	private void handleAsyncEvent(final AsyncPlayerReceiveNameTagEvent event) {
		// getting a players name tag can depend on many dynamic, internal things, which are currently not thread safe (yet)
        if (!Thread.currentThread().equals(Paintball.instance.mainThread)) {
            final Future<Boolean> future = Bukkit.getScheduler().callSyncMethod(Paintball.instance, new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
					handleEventSync(event);
                    return true;
                }
            });
            final long start = System.currentTimeMillis();
            while (!future.isCancelled() && !future.isDone()) {
                if ((Paintball.instance.mainThread == null) || ((System.currentTimeMillis() - start) > 5000)) {
                    if (Paintball.instance.mainThread != null) {
                        Log.severe("Tag handling took too long (limit: 5000ms). Ignoring for " + event.getNamedPlayer().getName() + " as seen by " + event.getPlayer().getName());
                    }
                    return;
                }
                try {
                    Thread.sleep(5);
                } catch (final InterruptedException ex) {
                }
            }
            if (future.isCancelled()) {
                Log.debug("Sync task for tag of " + event.getNamedPlayer().getName() + " to " + event.getPlayer().getName() + " was cancelled. Skipping.");
                return;
            }
        } else {
        	handleEventSync(event);
        }
    }
	
	private void handleEventSync(final AsyncPlayerReceiveNameTagEvent event) {
		Player player = event.getPlayer();
		Match match = plugin.matchManager.getMatch(player);
		if (match != null && match.isSurvivor(player)) {
			Player target = event.getNamedPlayer();
			if (Lobby.isPlaying(player)) {
				if (plugin.tagsInvis) {
					// invisible tags:
					event.setTag("§§§§");
					
				} else if (plugin.tagsColor) {
					// colored tags:
					Match matchTar = plugin.matchManager.getMatch(target);
					if (match == matchTar && match.isSurvivor(target)) {
						//change colors according to team
						//target is red, blue or spec ?
						if(match.isRed(target)) {
							event.setTag(Lobby.RED.color() + target.getDisplayName());
						} else if(match.isBlue(target)) {
							event.setTag(Lobby.BLUE.color() + target.getDisplayName());
						} else if(match.isSpec(target)) {
							event.setTag(Lobby.SPECTATE.color() + target.getDisplayName());
						}
					} else {
						if(plugin.tagsRemainingInvis) {
							// invisible tags:
							event.setTag("§§§§");
						} else {
							event.setTag(ChatColor.WHITE + target.getDisplayName());
						}
					}
				}
			} else if (Lobby.isSpectating(player)) {
				if (plugin.tagsColor) {
					// colored tags for spectators, else normal tags/no changes:
					Match matchTar = plugin.matchManager.getMatch(target);
					if (match == matchTar && match.isSurvivor(target)) {
						//change colors according to team
						//target is red, blue or spec ?
						if (match.isRed(target)) {
							event.setTag(Lobby.RED.color() + target.getDisplayName());
						} else if (match.isBlue(target)) {
							event.setTag(Lobby.BLUE.color() + target.getDisplayName());
						} else if (match.isSpec(target)) {
							event.setTag(Lobby.SPECTATE.color() + target.getDisplayName());
						}
					} else {
						event.setTag(ChatColor.WHITE + target.getDisplayName());
					}
				}
			}
		}
	}
}