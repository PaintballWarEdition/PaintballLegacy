package de.blablubbabc.paintball.utils;


import org.bukkit.plugin.Plugin;

public class Timer {

	private Plugin plugin;
	private int task = -1;
	private int time;

	public Timer(Plugin plugin, long delay, final int times, final Runnable eachDelay,
			final Runnable send, final Runnable end) {
		this.plugin = plugin;
		time = times;
		task = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {

			@Override
			public void run() {
				eachDelay.run();

				if (time == times && time > 0) {
					send.run();
					time--;
					return;
				}
				if ((time % 30) == 0 && time >= 60) {
					send.run();
				}
				else if ((time % 15) == 0 && time > 20 && time < 60) {
					send.run();
				}
				else if ((time % 10) == 0 && time > 5 && time <= 20) {
					send.run();
				}
				else if (time < 6 && time > 0) {
					send.run();
				}
				time--;
				if (time < 1) {
					end();
					end.run();
				}
			}
		}, 0L, delay).getTaskId();
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

}
