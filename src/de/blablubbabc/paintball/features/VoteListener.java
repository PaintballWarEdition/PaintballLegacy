package de.blablubbabc.paintball.features;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

import de.blablubbabc.paintball.Paintball;
import de.blablubbabc.paintball.statistics.player.PlayerStat;
import de.blablubbabc.paintball.statistics.player.PlayerStats;


public class VoteListener implements Listener {
	
	public VoteListener() {
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
    public void onVotifierEvent(VotifierEvent event) {
        Vote vote = event.getVote();
	    String playerName = vote.getUsername();
	    if (playerName != null && !playerName.isEmpty()) {
	    	PlayerStats stats = Paintball.instance.pm.getPlayerStats(playerName);
	    	if (stats != null) {
	    		stats.addStat(PlayerStat.MONEY, Paintball.instance.voteCash);
	    		stats.saveAsync();
	    	}
	    }
	    
    }
}
