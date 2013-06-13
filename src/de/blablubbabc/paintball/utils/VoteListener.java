package de.blablubbabc.paintball.utils;

import java.util.HashMap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

import de.blablubbabc.paintball.Paintball;


public class VoteListener implements Listener {
	
	private HashMap<String, Integer> boni = new HashMap<String, Integer>();
	
	public VoteListener() {
		boni.put("money", Paintball.instance.voteCash);
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
    public void onVotifierEvent(VotifierEvent event) {
        Vote vote = event.getVote();
	    String playerName = vote.getUsername();
	    if (playerName != null && !playerName.isEmpty()) {
	    	if (Paintball.instance.pm.exists(playerName)) {
	    		Paintball.instance.pm.addStatsAsync(playerName, boni);
	    	}
	    }
	    
    }
}
