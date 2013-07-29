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
					String command = ChatColor.stripColor(sign.getLine(1) + sign.getLine(2) + sign.getLine(3));
					if (!command.isEmpty()) {
						event.setCancelled(true);
						Player player = event.getPlayer();
						
						if (command.startsWith("/")) command = command.substring(1);
						
						// pb shop sign and normal shop disabled:
						if (command.startsWith("pb shop") && !plugin.shop && plugin.commandSignIgnoreShopDisabled) {
							String[] argsCmd = command.split(" ");
							String[] args = new String[argsCmd.length - 1];
							for (int j = 1; j < argsCmd.length; j++) {
								args[j - 1] = argsCmd[j];
							}
							// run shop command and ignore shop inactive:
							plugin.commandManager.cmdShop.command(player, args, true);
						} else {
							// perform command like normal:
							player.performCommand(command);
						}
					}
				}
			}
		}
	}
	
}
