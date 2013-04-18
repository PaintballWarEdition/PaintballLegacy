package me.blablubbabc.paintball.extras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Snowball;

import me.blablubbabc.paintball.Source;

public class Ball {
	//TEST
	public static int count = 0;
	
	public static Map<String, ArrayList<Ball>> balls = new HashMap<String, ArrayList<Ball>>();
	
	public static Ball registerBall(Snowball entity, String shooterName, Source source) {
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
	}
	
	
	private final Snowball entity;
	private final Source source;
	
	public Ball(Snowball entity, Source source) {
		this.entity = entity;
		this.source = source;
	}

	public int getId() {
		return entity.getEntityId();
	}

	public Source getSource() {
		return source;
	}
	
	public void remove() {
		entity.remove();
	}
	
}
