package de.blablubbabc.paintball;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import de.blablubbabc.paintball.utils.KeyValuePair;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class VoteManager {

	private final List<VoteOption> voteOptions = new ArrayList<VoteOption>();
	private Map<String, Integer> playerVotes = new HashMap<String, Integer>();
	
	
	public VoteManager(int numberOfOptions, boolean addRandomOption) {
		// init vote options:
		List<String> readyArenas = Paintball.instance.arenaManager.getReadyArenas();
		
		if (readyArenas.size() > numberOfOptions) {
			List<String> remaining = new ArrayList<String>(readyArenas);
			// pick random:
			for (int i = 0; i < numberOfOptions; i++) {
				String arenaName = remaining.remove(Utils.random.nextInt(remaining.size()));
				voteOptions.add(new VoteOption(arenaName));
			}
			// add random option:
			voteOptions.add(new VoteOption(null));
		} else {
			// take all:
			for (String arenaName : readyArenas) {
				voteOptions.add(new VoteOption(arenaName));
			}
		}
		
	}
	
	public void handleVote(Player player, int voteID) {
		if (player == null) return;
		if (!Lobby.LOBBY.isMember(player)) {
			player.sendMessage(Translator.getString("NOT_IN_LOBBY"));
			return;
		}
		if (!(voteID >= 1 && voteID <= voteOptions.size())) {
			player.sendMessage(Translator.getString("GAME_VOTE_NOT_VALID_ID", new KeyValuePair("max", String.valueOf(voteOptions.size()))));
			return;
		}
		String playerName = player.getName();
		Integer oldVote = playerVotes.get(playerName);
		
		//TODO ...
	}
	
	// returns the highest voted AND currently ready arena. If no arena is ready -> return null
	public String getArena() {
		
	}
	
	private class VoteOption implements Comparable<VoteOption> {
		
		private final String arenaName;
		private int votes = 0;
		
		private VoteOption(String arenaName) {
			this.arenaName = arenaName;
		}
		
		public String getArena() {
			return arenaName;
		}
		
		public int getVotes() {
			return votes;
		}

		@Override
		public int compareTo(VoteOption other) {
			if (other == null) {
				throw new IllegalArgumentException();
			}
			return this.getVotes() - other.getVotes();
		}
		
	}
}
