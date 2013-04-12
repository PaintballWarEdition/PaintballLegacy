package me.blablubbabc.paintball.extras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.blablubbabc.paintball.Source;

import org.bukkit.entity.Snowball;

public class Ball {
	
	private static Map<String, ArrayList<Ball>> balls = new HashMap<String, ArrayList<Ball>>();
	
	public static void registerBall(Snowball snowball, String shooterName, Source source) {
		ArrayList<Ball> pballs = balls.get(shooterName);
		if (pballs == null) {
			pballs = new ArrayList<Ball>();
			balls.put(shooterName, pballs);
		}
		pballs.add(new Ball(snowball.getEntityId(), source));
	}
	
	public static Ball getBall(Snowball snowball, String shooterName, boolean remove) {
		ArrayList<Ball> pballs = balls.get(shooterName);
		if (pballs == null) return null;
		Integer id = snowball.getEntityId();
		Ball ball = getBallFromList(pballs, id);
		if (remove && ball != null) {
			if (balls.size() == 1) balls.remove(shooterName);
			else pballs.remove(ball);
		}
		return ball;
	}
	
	private static Ball getBallFromList(ArrayList<Ball> pballs, int id) {
		for (Ball ball : pballs) {
			if (ball.getId() == id) return ball;
		}
		return null;
	}
	
	private final int id;
	private final Source source;
	
	public Ball(int id, Source source) {
		this.id = id;
		this.source = source;
	}

	int getId() {
		return id;
	}

	public Source getSource() {
		return source;
	}
	
	
}
