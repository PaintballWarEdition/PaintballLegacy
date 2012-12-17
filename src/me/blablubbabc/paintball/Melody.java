package me.blablubbabc.paintball;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Melody {
	private ArrayList<Ton> melody;
	private long maxDelay;
	private HashMap<String, Integer> playing;
	
	public Melody() {
		melody = new ArrayList<Ton>();
		playing = new HashMap<String, Integer>();
		maxDelay = 0;
	}
	
	public synchronized void addTon(Ton ton) {
		melody.add(ton);
	}
	
	public synchronized void printNotes() {
		for(Ton t : melody) {
			System.out.println(t.toString());
		}
	}
	
	public synchronized void play(final Plugin plugin, final Player p) {
		stop(plugin, p);
		playing.put(p.getName(), plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			long delay = 0;
			@Override
			public void run() {
				delay += 2;
				for(Ton ton : melody) {
					if(delay == ton.getDelay()) ton.play(p);
				}
				if(delay > maxDelay) {
					stop(plugin, p);
				}
			}
		}, 1L, 2L));
	}
	
	public synchronized void stop(Plugin plugin, Player p) {
		if(isPlaying(p)) {
			plugin.getServer().getScheduler().cancelTask(playing.get(p));
			playing.remove(p.getName());
		}
	}
	
	public synchronized boolean isPlaying(Player p) {
		return playing.containsKey(p.getName());
	}
}