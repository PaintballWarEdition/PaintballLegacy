/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.joindelay;

import org.bukkit.Location;

public class JoinWaitRunnable implements Runnable {

	private final Runnable runAfterSuccessfullyWaiting;
	
	private boolean aborted = false;
	private final Location startLocation;
	
	public JoinWaitRunnable(Runnable runAfterSuccessfullyWaiting, Location startLocation) {
		this.runAfterSuccessfullyWaiting = runAfterSuccessfullyWaiting;
		this.startLocation = startLocation;
	}
	
	public void onAborted() {
		aborted = true;
	}
	
	public boolean didPlayerMove(Location newLocation) {
		if (startLocation != null && newLocation != null) {
			if (startLocation.getWorld().getName().equals(newLocation.getWorld().getName())) {
				if (startLocation.getBlockX() == newLocation.getBlockX() 
						&& startLocation.getBlockY() == newLocation.getBlockY() 
						&& startLocation.getBlockZ() == newLocation.getBlockZ()) {
					
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public void run() {
		// check if player aborted somehow the waiting phase:
		if (!aborted) {
			if (runAfterSuccessfullyWaiting != null) runAfterSuccessfullyWaiting.run();
		}
		
	}

}
