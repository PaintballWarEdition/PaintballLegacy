package de.blablubbabc.commandsigns;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import de.blablubbabc.paintball.Paintball;

public class CommandSignsListener implements Listener {
	
	private Paintball plugin;
	
	public CommandSignsListener(Paintball plugin) {
		this.plugin = plugin;
		if (plugin.commandSignEnabled) {
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}
	
	@EventHandler(ignoreCancelled = false)
	public void onSignClick(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		if (block != null) {
			BlockState state = block.getState();
			if (state instanceof Sign) {
				Sign sign = (Sign) state;
				String line1 = ChatColor.stripColor(sign.getLine(0));
				if (line1.equalsIgnoreCase(plugin.commandSignIdentifier)) {
					for (int i = 1; i < 4; i++) {
						String command = sign.getLine(i);
						if (!command.isEmpty()) {
							if (command.startsWith("/")) command = command.substring(1);
							Player player = event.getPlayer();
							player.performCommand(command);
							event.setCancelled(true);
							break;
						}
					}
				}
			}
		}
	}
	
}
