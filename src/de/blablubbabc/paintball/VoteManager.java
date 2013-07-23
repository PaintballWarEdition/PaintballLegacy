package de.blablubbabc.paintball;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import de.blablubbabc.paintball.utils.KeyValuePair;
import de.blablubbabc.paintball.utils.Translator;
import de.blablubbabc.paintball.utils.Utils;

public class VoteManager {

	private final List<VoteOption> voteOptions = new ArrayList<VoteOption>();
	private final Map<String, VoteOption> playerVotes = new HashMap<String, VoteOption>();
	
	private boolean isOver = false;
	
	// numberOfOptions between 2 and 8 !
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
	
	public void endVoting() {
		isOver = true;
		Collections.sort(voteOptions);
	}
	
	public boolean isOver() {
		return isOver;
	}
	
	public void broadcastVoteOptions() {
		for (Player player : Lobby.LOBBY.getMembers()) {
			sendVoteOptions(player);
		}
	}
	
	public void sendVoteOptions(Player player) {
		Paintball.instance.feeder.text(player, Translator.getString("GAME_VOTE_HEADER"));
		
		int id = 0;
		KeyValuePair idPair = new KeyValuePair("id", String.valueOf(id));
		KeyValuePair votesPair = new KeyValuePair("votes", "0");
		
		// ranom option if needed:
		String randomOption = Translator.getString("GAME_VOTE_OPTION_RANDOM");
		
		for (VoteOption option : voteOptions) {
			idPair.setValue(String.valueOf(++id));
			votesPair.setValue(String.valueOf(option.getVotes()));
			String arenaName = option.getArena();
			
			Paintball.instance.feeder.text(player, Translator.getString("GAME_VOTE_OPTION", idPair, votesPair, new KeyValuePair("arena", arenaName != null ? arenaName : randomOption)));
			
		}
	}
	
	public void handleVote(Player player, int voteID) {
		if (!(voteID >= 1 && voteID <= voteOptions.size())) {
			player.sendMessage(Translator.getString("GAME_VOTE_NOT_VALID_ID", new KeyValuePair("max", String.valueOf(voteOptions.size()))));
			return;
		}
		
		String playerName = player.getName();
		handleVoteUndo(playerName);
		
		VoteOption vote = voteOptions.get(voteID - 1);
		vote.addVote();
		String arenaName = vote.getArena();
		
		playerVotes.put(playerName, vote);
		
		player.sendMessage(Translator.getString("GAME_VOTE_VOTED", new KeyValuePair("arena", arenaName != null ? arenaName : Translator.getString("GAME_VOTE_OPTION_RANDOM"))));
	}
	
	public void handleVoteUndo(String playerName) {
		VoteOption oldVote = playerVotes.get(playerName);
		if (oldVote != null) {
			oldVote.removeVote();
		}
	}
	
	public boolean didSomebodyVote() {
		return !playerVotes.isEmpty();
	}
	
	public String getHighestVotedArena() {
		List<VoteOption> sorted = voteOptions;
		if (!isOver) {
			sorted = new ArrayList<VoteManager.VoteOption>(voteOptions);
			Collections.sort(sorted);
		}
		
		VoteOption highestVoted = sorted.get(sorted.size() - 1);
		String arenaName = highestVoted.getArena();
		
		return arenaName != null ? arenaName : Translator.getString("GAME_VOTE_OPTION_RANDOM");
	}
	
	// returns the highest voted AND currently ready arena. If no arena is ready -> return null
	public String getVotedAndReadyArena() {
		if (!isOver) {
			endVoting();
		}
		
		Collections.sort(voteOptions);
		
		// get the currently ready arenas:
		List<String> allReady = Paintball.instance.arenaManager.getReadyArenas();
		
		// start at highest voted option:
		for (int i = voteOptions.size() - 1; i >= 0; i--) {
			VoteOption vote = voteOptions.get(i);
			String arenaName = vote.getArena();
			// random vote option:
			if (arenaName == null) {
				List<String> remaining = new ArrayList<String>(allReady);
				remaining.removeAll(getVoteAbleArenas());
				// pick random:
				if (remaining.size() > 0) {
					return remaining.remove(Utils.random.nextInt(remaining.size()));
				}
				// still no arena found? -> go on and check the other vote-able ones
				continue;
			} else {
				// check if arena is still ready:
				if (allReady.contains(arenaName)) {
					return arenaName;
				}
				// else -> keep searching:
				continue;
			}
		}
		
		// still no ready arena found? -> return null
		return null;
	}
	
	public List<String> getVoteAbleArenas() {
		List<String> arenas = new ArrayList<String>();
		for (VoteOption option : voteOptions) {
			String arenaName = option.getArena();
			if (arenaName != null) arenas.add(arenaName);
		}
		return arenas;
	}
	
	private class VoteOption implements Comparable<VoteOption> {
		
		private final String arenaName;
		private int votes = 0;
		
		private VoteOption(String arenaName) {
			this.arenaName = arenaName;
		}
		
		public void removeVote() {
			if (votes > 0) votes--;
		}
		
		public void addVote() {
			votes++;
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
