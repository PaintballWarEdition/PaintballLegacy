/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.addons.melodies;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Melody {
	private ArrayList<Note> melody;
	private long maxDelay;
	private HashMap<String, Integer> playing;

	public Melody() {
		melody = new ArrayList<Note>();
		playing = new HashMap<String, Integer>();
		maxDelay = 0;
	}

	public void addNote(Note note) {
		melody.add(note);
		if (note.getDelay() > maxDelay) maxDelay = note.getDelay();
	}

	public void printNotes() {
		for (Note note : melody) {
			System.out.println(note.toString());
		}
	}

	public void play(final Plugin plugin, final Player p) {
		stop(plugin, p);
		playing.put(p.getName(), plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			long delay = 0;

			@Override
			public void run() {
				if (delay > maxDelay) {
					stop(plugin, p);
					return;
				}
				if (!p.isOnline() || p.isDead()) {
					stop(plugin, p);
					return;
				}
				for (Note note : melody) {
					if (delay == note.getDelay()) note.play(p);
				}
				delay += 2;
			}
		}, 1L, 2L));
	}

	public void stop(Plugin plugin, Player p) {
		if (isPlaying(p)) {
			plugin.getServer().getScheduler().cancelTask(playing.get(p.getName()));
			playing.remove(p.getName());
		}
	}

	public boolean isPlaying(Player p) {
		return playing.containsKey(p.getName());
	}
}