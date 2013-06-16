package de.blablubbabc.paintball.features;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.statistics.player.PlayerStat;


public class VoteListener implements Listener {
	
	private Map<PlayerStat, Integer> boni = new HashMap<PlayerStat, Integer>();
	
	public VoteListener() {
		boni.put(PlayerStat.MONEY, Paintball.instance.voteCash);
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
