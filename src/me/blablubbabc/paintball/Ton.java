package me.blablubbabc.paintball;

import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.Block;
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
	
	public void play(final Plugin plugin, final Player p) {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			
			@Override
			public void run() {
				Location loc = p.getLocation().add(0, -1, 0);
				Block block = loc.getBlock();
				Material mat = block.getType();
				byte data = block.getData();
				
				p.sendBlockChange(loc, Material.NOTE_BLOCK, (byte) 0);
				
				p.playNote(loc, instrument, note);
				p.sendBlockChange(loc, mat, data);
			}
		}, delay);
	}
}
