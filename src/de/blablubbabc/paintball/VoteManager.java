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
	
	public void broadcastVoteOptions() {
		for (Player player : Lobby.LOBBY.getMembers()) {
			sendVoteOptions(player);
		}
		Paintball.instance.feeder.textUntoggled(Translator.getString("GAME_VOTE_HEADER"));
		
		int id = 1;
		KeyValuePair idPair = new KeyValuePair("id", String.valueOf(id));
		
		for (VoteOption option : voteOptions) {
			String arenaName = option.getArena();
			if (arenaName != null) {
				Paintball.instance.feeder.textUntoggled(Translator.getString("GAME_VOTE_OPTION", idPair, new KeyValuePair("arena", arenaName)));
			} else {
				Paintball.instance.feeder.textUntoggled(Translator.getString("GAME_VOTE_OPTION_RANDOM", idPair));	
			}
			idPair.setValue(String.valueOf(++id));
		}
	}
	
	public void sendVoteOptions(Player player) {
		Paintball.instance.feeder.text(player, Translator.getString("GAME_VOTE_HEADER"));
		
		int id = 1;
		KeyValuePair idPair = new KeyValuePair("id", String.valueOf(id));
		
		for (VoteOption option : voteOptions) {
			String arenaName = option.getArena();
			if (arenaName != null) {
				Paintball.instance.feeder.text(player, Translator.getString("GAME_VOTE_OPTION", idPair, new KeyValuePair("arena", arenaName)));
			} else {
				Paintball.instance.feeder.text(player, Translator.getString("GAME_VOTE_OPTION_RANDOM", idPair));	
			}
			idPair.setValue(String.valueOf(++id));
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
	
	// returns the highest voted AND currently ready arena. If no arena is ready -> return null
	public String getVotedArena() {
		List<VoteOption> copy = new ArrayList<VoteOption>(voteOptions);
		Collections.sort(copy);
		
		// get the currently ready arenas:
		List<String> allReady = Paintball.instance.arenaManager.getReadyArenas();
		
		// start at highest voted option:
		for (int i = copy.size() - 1; i >= 0; i--) {
			VoteOption vote = copy.get(i);
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