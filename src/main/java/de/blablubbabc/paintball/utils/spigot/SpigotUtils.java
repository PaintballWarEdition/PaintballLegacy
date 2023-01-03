/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.utils.spigot;

import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

public final class SpigotUtils {

	private SpigotUtils() {
	}

	// Null if not yet checked:
	private static Boolean SPIGOT_AVAILABLE = null;

	public static boolean isSpigotAvailable() {
		// Check once and then cache the result:
		if (SPIGOT_AVAILABLE == null) {
			try {
				Class.forName("org.bukkit.Server$Spigot");
				SPIGOT_AVAILABLE = true;
			} catch (ClassNotFoundException e) {
				SPIGOT_AVAILABLE = false;
			}
		}
		assert SPIGOT_AVAILABLE != null;
		return SPIGOT_AVAILABLE;
	}

	public static void sendClickableText(CommandSender recipient, String command, String message) {
		if (isSpigotAvailable()) {
			Internal.sendClickableText(recipient, command, message);
		} else {
			// Fallback: Send non-clickable message.
			recipient.sendMessage(message);
		}
	}

	// A separate class that is only accessed if Spigot is present. Avoids class loading issues.
	private static final class Internal {

		private Internal() {
		}

		public static void sendClickableText(CommandSender recipient, String command, String message) {
			TextComponent component = new TextComponent(TextComponent.fromLegacyText(message));
			component.setClickEvent(new ClickEvent(Action.RUN_COMMAND, command));
			recipient.spigot().sendMessage(component);
		}
	}
}
