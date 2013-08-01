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
	
	public synchronized void addTon(Note ton) {
		melody.add(ton);
		if(ton.getDelay() > maxDelay) maxDelay = ton.getDelay();
	}
	
	public synchronized void printNotes() {
		for(Note t : melody) {
			System.out.println(t.toString());
		}
	}
	
	public synchronized void play(final Plugin plugin, final Player p) {
		stop(plugin, p);
		playing.put(p.getName(), plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			long delay = 0;
			@Override
			public void run() {
				if(delay > maxDelay) {
					stop(plugin, p);
					return;
				}
				if(!p.isOnline() || p.isDead()) {
					stop(plugin, p);
					return;
				}
				for(Note ton : melody) {
					if(delay == ton.getDelay()) ton.play(p);
				}
				delay += 2;
			}
		}, 1L, 2L));
	}
	
	public synchronized void stop(Plugin plugin, Player p) {
		if(isPlaying(p)) {
			plugin.getServer().getScheduler().cancelTask(playing.get(p.getName()));
			playing.remove(p.getName());
		}
	}
	
	public synchronized boolean isPlaying(Player p) {
		return playing.containsKey(p.getName());
	}
}