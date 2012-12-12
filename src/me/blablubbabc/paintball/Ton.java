package me.blablubbabc.paintball;

import java.util.HashMap;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.block.NoteBlock;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Ton {
	private final Instrument instrument;
	private final Note note;
	private final long delay;
	
	private static HashMap<Player, HashMap<Location, Integer>> noteblocks = new HashMap<Player, HashMap<Location, Integer>>();
	private static HashMap<Location, Block> original = new HashMap<Location, Block>();
	
	public static synchronized void addNoteBlock(Player p, Location loc, Block block) {
		HashMap<Location, Integer> entry = noteblocks.get(p);
		if(entry == null) entry = new HashMap<Location, Integer>();
		Integer i = entry.get(loc);
		entry.put(loc, (i == null ? 0:i)+1);
		if(entry.get(loc) == 1) {
			original.put(loc, block);
		}
		
		noteblocks.put(p, entry);
	}
	public static synchronized void removeNoteBlock(Player p, Location loc) {
		HashMap<Location, Integer> entry = noteblocks.get(p);
		if(entry == null) return;
		Integer i = entry.get(loc);
		entry.put(loc, (i == null ? 1:i)-1);
		if(entry.get(loc) == 0) entry.remove(loc);
		if(entry.isEmpty()) {
			noteblocks.remove(p);
		} else {
			noteblocks.put(p, entry);
		}
	}
	public static synchronized boolean isNoteBlock(Player p, Location loc) {
		HashMap<Location, Integer> entry = noteblocks.get(p);
		if(entry == null) return false;
		return entry.containsKey(loc);
	}
	public synchronized Block getOriginalBlock(Location loc) {
		return original.get(loc);
	}
	public synchronized void removeOriginalBlock(Location loc) {
		original.remove(loc);
	}
	
	public Ton(Instrument instrument, Note note, long delay) {
		this.instrument = instrument;
		this.note = note;
		this.delay = delay+1;
	}
	
	public void play(final Plugin plugin, final Player p) {
		
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			
			@Override
			public void run() {
				
				final Location loc = p.getLocation().add(0, 5, 0);
				
				final Block block = loc.getBlock();
				addNoteBlock(p, loc, block);
				//final Material mat = block.getType();
				//final byte data = block.getData();
				block.setType(Material.NOTE_BLOCK);
				//p.sendBlockChange(loc, Material.NOTE_BLOCK, (byte) 1);
				
				
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					
					@Override
					public void run() {
						//NoteBlock n = (NoteBlock)block.getState();
						//n.play(instrument, note);
						p.playNote(loc, instrument, note);
						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							
							@Override
							public void run() {
								removeNoteBlock(p, loc);
								if(!isNoteBlock(p, loc)) {
									//p.sendBlockChange(loc, mat, data);
									Block b = getOriginalBlock(loc);
									block.setType(b.getType());
									block.setData(b.getData());
									removeOriginalBlock(loc);
								}
							}
						}, 20L);
					}
				}, 1L);
			}
		}, delay-1);
	}
}
