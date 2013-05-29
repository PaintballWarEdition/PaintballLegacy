package de.blablubbabc.paintball.melodies;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Ton {
	private final Sound sound;
	private final long delay;
	private final float pitch;
	
	public Ton(Sound sound, int id, long delay) {
		this.sound = sound;
		this.delay = delay;
		this.pitch = getPitch(id);
	}
	
	public void play(final Plugin plugin, final Player p) {
		
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			
			@Override
			public void run() {
				if(p.isOnline()) {
					play(p);
				}
			}
		}, delay);
	}
	
	public void play(Player p) {
		Location loc = p.getEyeLocation();
		p.playSound(loc, sound, 1, pitch);
	}
	
	public long getDelay() {
		return delay;
	}
	
	public String toString() {
		return "NOTE:"+sound.toString()+";"+pitch+";"+delay;
	}
	
	private static float getPitch(int id) {
		switch (id) {
		case 0: return 0.5F;
		case 1: return 0.53F;
		case 2: return 0.56F;
		case 3: return 0.6F;
		case 4: return 0.63F;
		case 5: return 0.67F;
		case 6: return 0.7F;
		case 7: return 0.76F;
		case 8: return 0.8F;
		case 9: return 0.84F;
		case 10: return 0.9F;
		case 11: return 0.94F;
		case 12: return 1.0F;
		case 13: return 1.06F;
		case 14: return 1.12F;
		case 15: return 1.18F;
		case 16: return 1.26F;
		case 17: return 1.34F;
		case 18: return 1.42F;
		case 19: return 1.5F;
		case 20: return 1.6F;
		case 21: return 1.68F;
		case 22: return 1.78F;
		case 23: return 1.88F;
		case 24: return 2.0F;
		default: return 0.0F;
		}
	}
	
}