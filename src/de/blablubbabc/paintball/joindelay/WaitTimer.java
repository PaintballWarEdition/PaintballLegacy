package de.blablubbabc.paintball.joindelay;

import org.bukkit.entity.Player;

import de.blablubbabc.paintball.Paintball;


public class WaitTimer {

	private Paintball plugin;
	private int task = -1;
	private int time;
	private final JoinWaitRunnable waitRunnable;

	public WaitTimer(final Paintball plugin, final Player player, long preDelay, long delay, final int times, final JoinWaitRunnable waitRunnable) {
		this.plugin = plugin;
		this.waitRunnable = waitRunnable;
		time = times;
		task = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {

			@Override
			public void run() {
				// check if player moved:
				if (waitRunnable.didPlayerMove(player.getLocation())) {
					plugin.playerManager.abortingJoinWaiting(player);
				}
				
				time--;
				if (time < 1) {
					end();
					if (waitRunnable != null) waitRunnable.run();
				}
			}
		}, preDelay, delay).getTaskId();
	}

	public int getTime() {
		return time;
	}

	public boolean isRunning() {
		return task != -1
				&& (plugin.getServer().getScheduler().isCurrentlyRunning(task) || plugin
						.getServer().getScheduler().isQueued(task));
	}

	public void end() {
		if (isRunning()) {
			plugin.getServer().getScheduler().cancelTask(task);
			task = -1;
		}
	}
	
	public void onAbort() {
		if (waitRunnable != null) waitRunnable.onAborted();
		end();
	}

}
