/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.commandsigns;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

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
		// not ignoring off-hand interactions at first, so breaking of command signs gets properly denied:
		Block block = event.getClickedBlock();
		if (block == null) return;
		Material type = block.getType();
		if (type != Material.WALL_SIGN && type != Material.SIGN_POST) return;

		BlockState state = block.getState();
		Sign sign = (Sign) state;
		String line1 = ChatColor.stripColor(sign.getLine(0));
		if (!line1.equalsIgnoreCase(plugin.commandSignIdentifier)) return;

		// allow breaking (do not cancel):
		Player player = event.getPlayer();
		if (player.isSneaking() && event.getAction() == Action.LEFT_CLICK_BLOCK && player.hasPermission("paintball.admin")) {
			return;
		}

		// canceling interaction will prevent sign breaking:
		event.setCancelled(true);

		// ignore off-hand interactions from this point on, so no commands get triggered for those:
		if (event.getHand() != EquipmentSlot.HAND) return;

		String command = ChatColor.stripColor(sign.getLine(1) + sign.getLine(2) + sign.getLine(3));
		// ignore command-signs which don't have any command on it
		if (command.isEmpty()) return;

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
			String eventCommand = "/" + command;
			PlayerCommandPreprocessEvent commandEvent = new PlayerCommandPreprocessEvent(player, eventCommand);
			plugin.getServer().getPluginManager().callEvent(commandEvent);
			if (!commandEvent.isCancelled()) {
				String resultCommand = commandEvent.getMessage();
				if (Paintball.getInstance().debug) {
					if (!resultCommand.equals(eventCommand)) {
						player.sendMessage("[PB DEBUG] PlayerCommandPreprocessEvent: Command '" + eventCommand + "' was changed to '" + resultCommand + "'");
					}
				}
				player.performCommand(resultCommand.substring(1));
			} else {
				if (Paintball.getInstance().debug) {
					player.sendMessage("[PB DEBUG] PlayerCommandPreprocessEvent was cancelled");
				}
			}
		}
	}
}
