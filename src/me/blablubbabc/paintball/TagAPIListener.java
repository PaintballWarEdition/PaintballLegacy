package me.blablubbabc.paintball;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.kitteh.tag.PlayerReceiveNameTagEvent;

public class TagAPIListener implements Listener {
	private Paintball plugin;

	public TagAPIListener(Paintball plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onTagRecieve(PlayerReceiveNameTagEvent event) {
		Player player = event.getPlayer();
		Match match = plugin.mm.getMatch(player);
		if (match != null && match.isSurvivor(player)) {
			Player target = event.getNamedPlayer();
			if(Lobby.isPlaying(player)) {
				if(plugin.tagsInvis) {
					// invisible tags:
					event.setTag("§§§§");
					
				} else if(plugin.tagsColor) {
					// colored tags:
					Match matchTar = plugin.mm.getMatch(target);
					if(match == matchTar && match.isSurvivor(target)) {
						//change colors according to team
						//target is red, blue or spec ?
						if(match.isRed(target)) {
							event.setTag(Lobby.RED.color()+target.getDisplayName());
						} else if(match.isBlue(target)) {
							event.setTag(Lobby.BLUE.color()+target.getDisplayName());
						} else if(match.isSpec(target)) {
							event.setTag(Lobby.SPECTATE.color()+target.getDisplayName());
						}
					} else {
						if(plugin.tagsRemainingInvis) {
							// invisible tags:
							event.setTag("§§§§");
						} else {
							event.setTag(ChatColor.WHITE+target.getDisplayName());
						}
					}
				}
			} else if(Lobby.isSpectating(player)) {
				if(plugin.tagsColor) {
					// colored tags for spectators, else normal tags/no changes:
					Match matchTar = plugin.mm.getMatch(target);
					if(match == matchTar && match.isSurvivor(target)) {
						//change colors according to team
						//target is red, blue or spec ?
						if(match.isRed(target)) {
							event.setTag(Lobby.RED.color()+target.getDisplayName());
						} else if(match.isBlue(target)) {
							event.setTag(Lobby.BLUE.color()+target.getDisplayName());
						} else if(match.isSpec(target)) {
							event.setTag(Lobby.SPECTATE.color()+target.getDisplayName());
						}
					} else {
						event.setTag(ChatColor.WHITE+target.getDisplayName());
					}
				}
			}
		}
	}

}
