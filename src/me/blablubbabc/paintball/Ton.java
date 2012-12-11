package me.blablubbabc.paintball;

import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Note;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Ton {
	public final Instrument instrument;
	public final Note note;
	public final long delay;
	
	public Ton(Instrument instrument, Note note, long delay) {
		this.instrument = instrument;
		this.note = note;
		this.delay = delay;
	}
	
	public void play(final Plugin plugin, final Player p, final Location loc) {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			
			@Override
			public void run() {
				p.playNote(loc, instrument, note);
			}
		}, delay);
	}
}
