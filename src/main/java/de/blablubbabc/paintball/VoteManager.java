/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
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
	
	// numberOfOptions at least 2!
	public VoteManager(int numberOfOptions, boolean addRandomOption) {
		if (numberOfOptions < 2) numberOfOptions = 2;
		// init vote options:
		List<String> readyArenas = Paintball.getInstance().arenaManager.getReadyArenas();
		
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
			// are there even enough ready arenas to create a vote?
			if (readyArenas.size() >= 2) {
				// take all:
				for (String arenaName : readyArenas) {
					voteOptions.add(new VoteOption(arenaName));
				}
			}
			// else: voteOptions empty -> not valid
		}
		
	}
	
	public void endVoting() {
		if (isOver) throw new IllegalStateException("Voting is already over, but it's tried to end it again.");
		
		isOver = true;
		Collections.sort(voteOptions);
	}
	
	public boolean isOver() {
		return isOver;
	}
	
	// it will not be valid, if there are less than 2 VoteOptions to choose from
	public boolean isValid() {
		return voteOptions.size() >= 2;
	}
	
	public void broadcastVoteOptions() {
		if (isOver) throw new IllegalStateException("Voting is already over, but options shall be braodcasted.");
		
		for (Player player : Lobby.LOBBY.getMembers()) {
			sendVoteOptions(player);
		}
	}
	
	public void sendVoteOptions(Player player) {
		if (isOver) throw new IllegalStateException("Voting is already over, but optiosn shall be send to a player.");
		
		Paintball.getInstance().feeder.text(player, Translator.getString("GAME_VOTE_HEADER"));
		
		int id = 0;
		KeyValuePair idPair = new KeyValuePair("id", String.valueOf(id));
		KeyValuePair votesPair = new KeyValuePair("votes", "0");
		
		// ranom option if needed:
		String randomOption = Translator.getString("GAME_VOTE_OPTION_RANDOM");
		
		for (VoteOption option : voteOptions) {
			idPair.setValue(String.valueOf(++id));
			votesPair.setValue(String.valueOf(option.getVotes()));
			String arenaName = option.getArena();
			
			Paintball.getInstance().feeder.text(player, Translator.getString("GAME_VOTE_OPTION", idPair, votesPair, new KeyValuePair("arena", arenaName != null ? arenaName : randomOption)));
			
		}
	}
	
	public void handleVote(Player player, String votedArena) {
		if (isOver) throw new IllegalStateException("Voting is already over, but a vote shall be handled.");
		
		int id = getIdForArenaName(votedArena);
		if (id == -1) {
			player.sendMessage(Translator.getString("GAME_VOTE_INVALID_ARENA_NAME", new KeyValuePair("max", String.valueOf(voteOptions.size()))));
		} else {
			handleVote(player, id);
		}
	}
	
	// returns the first option with this arena name:
	private int getIdForArenaName(String arenaName) {
		if (arenaName != null) {
			for (int i = 0; i < voteOptions.size(); i++) {
				VoteOption option = voteOptions.get(i);
				String voteArena = option.getArena();
				
				if (voteArena == null) {
					if (arenaName.equalsIgnoreCase(Translator.getString("RANDOM")) || arenaName.equalsIgnoreCase("random")) {
						return i;
					}
				} else if (voteArena.equalsIgnoreCase(arenaName)) {
					return i;
				}
			}
		}
		
		return -1;
	}
	
	public void handleVote(Player player, int voteID) {
		if (isOver) throw new IllegalStateException("Voting is already over, but a vote shall be handled.");
		
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
		if (isOver) throw new IllegalStateException("Voting is already over, but a vote shall be a undone.");
		
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
	public String getVotedAndReadyArena(List<String> readyArenas) {
		if (!isOver) {
			endVoting();
		}
		
		Collections.sort(voteOptions);
		
		// start at highest voted option:
		for (int i = voteOptions.size() - 1; i >= 0; i--) {
			VoteOption vote = voteOptions.get(i);
			String arenaName = vote.getArena();
			// random vote option:
			if (arenaName == null) {
				List<String> remaining = new ArrayList<String>(readyArenas);
				remaining.removeAll(getVoteAbleArenas());
				// pick random:
				if (remaining.size() > 0) {
					return remaining.remove(Utils.random.nextInt(remaining.size()));
				}
				// still no arena found? -> go on and check the other vote-able ones
				continue;
			} else {
				// check if arena is still ready:
				if (readyArenas.contains(arenaName)) {
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
