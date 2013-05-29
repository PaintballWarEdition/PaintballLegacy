package de.blablubbabc.paintball.extras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;

import de.blablubbabc.paintball.Origin;


public class Ball {
	
	public final static ItemStack item = ItemManager.setMeta(new ItemStack(Material.SNOW_BALL));
	
	public static int count = 0;
	
	public static Map<String, ArrayList<Ball>> balls = new HashMap<String, ArrayList<Ball>>();
	
	public static Ball registerBall(Snowball entity, String shooterName, Origin source) {
		ArrayList<Ball> pballs = balls.get(shooterName);
		if (pballs == null) {
			pballs = new ArrayList<Ball>();
			balls.put(shooterName, pballs);
		}
		Ball ball = new Ball(entity, source);
		pballs.add(ball);
		count++;
		return ball;
	}
	
	public static Ball getBall(int id, String shooterName, boolean remove) {
		ArrayList<Ball> pballs = balls.get(shooterName);
		if (pballs == null) return null;
		Ball ball = getBallFromList(pballs, id);
		if (remove && ball != null) {
			if (pballs.remove(ball)) {
				count--;
				if (pballs.size() == 0) balls.remove(shooterName);
			}
		}
		return ball;
	}
	
	private static Ball getBallFromList(ArrayList<Ball> pballs, int id) {
		for (Ball ball : pballs) {
			if (ball.getId() == id) return ball;
		}
		return null;
	}
	
	public static void clear() {
		for (String playerName : balls.keySet()) {
			ArrayList<Ball> pballs = balls.get(playerName);
			for (Ball b : pballs) {
				b.remove();
			}
		}
		balls.clear();
		count = 0;
	}
	
	
	private final Snowball entity;
	private final Origin source;
	
	public Ball(Snowball entity, Origin source) {
		this.entity = entity;
		this.source = source;
	}

	public int getId() {
		return entity.getEntityId();
	}

	public Origin getSource() {
		return source;
	}
	
	void remove() {
		entity.remove();
	}
	
}
