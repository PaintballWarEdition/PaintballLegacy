package me.blablubbabc.paintball;

import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Melodie {
	private ArrayList<Ton> melodie;
	
	public Melodie() {
		melodie = new ArrayList<Ton>();
	}
	
	public synchronized void addTon(Ton ton) {
		melodie.add(ton);
	}
	
	public synchronized void play(final Plugin plugin, final Player p, final Location loc) {
		for(Ton ton : melodie) {
			ton.play(plugin, p, loc);
		}
	}
	
}
