package de.blablubbabc.paintball.extras.weapons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Entity;

import de.blablubbabc.paintball.Match;

public class GadgetManager {

	private Map<Match, MatchEntry> gadgets = new HashMap<Match, MatchEntry>();
	private int overallCounter = 0;
	
	public GadgetManager() {
	}
	
	public int getOverallCount() {
		return overallCounter;
	}
	
	public int getMatchGadgetCount(Match match) {
		if (match == null ) throw new IllegalArgumentException();
		MatchEntry matchEntry = gadgets.get(match);
		return matchEntry == null ? 0 : matchEntry.getMatchGadgetCount();
	}
	
	public int getPlayerGadgetCount(Match match, String playerName) {
		if (match == null || playerName == null) throw new IllegalArgumentException();
		MatchEntry matchEntry = gadgets.get(match);
		return matchEntry == null ? 0 : matchEntry.getMatchPlayerGadgetCount(playerName);
	}
	
	public void addGadget(Match match, String playerName, Gadget gadget) {
		if (match == null || playerName == null || gadget == null) throw new IllegalArgumentException();
		MatchEntry matchEntry = gadgets.get(match);
		if (matchEntry == null) {
			matchEntry = new MatchEntry(match);
			gadgets.put(match, matchEntry);
		}
		matchEntry.addMatchGadget(playerName, gadget);
		overallCounter++;
	}
	
	public void removeGadget(Match match, String playerName, Gadget gadget) {
		if (match == null || playerName == null || gadget == null) throw new IllegalArgumentException();
		MatchEntry matchEntry = gadgets.get(match);
		if (matchEntry != null) {
			matchEntry.removeMatchGadget(playerName, gadget);
			if (matchEntry.getMatchGadgetCount() == 0) gadgets.remove(match);
			overallCounter--;
		}
	}
	
	public List<Gadget> getGadgets(Match match, String playerName) {
		if (match == null || playerName == null) throw new IllegalArgumentException();
		MatchEntry matchEntry = gadgets.get(match);
		
		return matchEntry != null ? matchEntry.getMatchGadgets(playerName) : new ArrayList<Gadget>();
	}
	
	public boolean isGadget(Entity entity) {
		return getGadget(entity, false) != null;
	}
	
	public Gadget getGadget(Entity entity, boolean removeWhenFound) {
		if (entity == null) throw new IllegalArgumentException();
		for (MatchEntry matchEntry : gadgets.values()) {
			Gadget gadget = matchEntry.getGadget(entity, removeWhenFound);
			if (gadget != null) {
				if (removeWhenFound) {
					if (matchEntry.getMatchGadgetCount() == 0) gadgets.remove(matchEntry.match);
					overallCounter--;
				}
				return gadget;
			}
		}
		return null;
	}
	
	public void cleanUp(Match match, String playerName) {
		if (match == null || playerName == null) throw new IllegalArgumentException();
		MatchEntry matchEntry = gadgets.get(match);
		if (matchEntry != null) {
			overallCounter -= matchEntry.cleanUp(playerName);
			if (matchEntry.getMatchGadgetCount() == 0) gadgets.remove(match);
		}
	}
	
	public void cleanUp(Match match) {
		if (match == null) throw new IllegalArgumentException();
		MatchEntry matchEntry = gadgets.get(match);
		if (matchEntry != null) {
			overallCounter -= matchEntry.cleanUp();
			gadgets.remove(match);
		}
	}
	
	
	private class MatchEntry {
		private final Match match;
		private int overallMatchCounter = 0;
		private Map<String, List<Gadget>> matchPlayerGadgets = new HashMap<String, List<Gadget>>();
		
		private MatchEntry(Match match) {
			this.match = match;
		}
		
		private void addMatchGadget(String playerName, Gadget gadget) {
			List<Gadget> playerGadgets = matchPlayerGadgets.get(playerName);
			if (playerGadgets == null) {
				playerGadgets = new ArrayList<Gadget>();
				matchPlayerGadgets.put(playerName, playerGadgets);
			}
			playerGadgets.add(gadget);
			overallMatchCounter++;
		}
		
		private void removeMatchGadget(String playerName, Gadget gadget) {
			List<Gadget> playerGadgets = matchPlayerGadgets.get(playerName);
			if (playerGadgets != null) {
				if (playerGadgets.remove(gadget)) {
					if (playerGadgets.size() == 0) matchPlayerGadgets.remove(playerName);
					overallMatchCounter--;
				}
			}
		}
		
		private List<Gadget> getMatchGadgets(String playerName) {
			List<Gadget> playerGadgets = matchPlayerGadgets.get(playerName);
			if (playerGadgets == null) {
				playerGadgets = new ArrayList<Gadget>();
			}
			return playerGadgets;
		}
		
		private Gadget getGadget(Entity entity, boolean removeWhenFound) {
			Gadget found = null;
			for (Entry<String, List<Gadget>> playerEntry : matchPlayerGadgets.entrySet()) {
				List<Gadget> playerGadgets = playerEntry.getValue();
				for (Gadget gadget : playerGadgets) {
					if (gadget.isSimiliar(entity)) {
						found = gadget;
						break;
					}
				}
				
				if (found != null) {
					if (playerGadgets.remove(found)) {
						if (playerGadgets.size() == 0) matchPlayerGadgets.remove(playerEntry.getKey());
						overallMatchCounter--;
					}
					break;
				}
			}
			
			return found;
		}
		
		private int getMatchGadgetCount() {
			return overallMatchCounter;
		}
		
		private int getMatchPlayerGadgetCount(String playerName) {
			List<Gadget> playerGadgets = matchPlayerGadgets.get(playerName);
			return playerGadgets == null ? 0 : playerGadgets.size();
		}
		
		private int cleanUp(String playerName) {
			List<Gadget> playerGadgets = matchPlayerGadgets.remove(playerName);
			int gadgetsRemoved = 0;
			if (playerGadgets != null) {
				gadgetsRemoved = playerGadgets.size();
				for (Gadget gadget : playerGadgets) {
					gadget.dispose(false, false);
				}
			}
			overallMatchCounter -= gadgetsRemoved;
			return gadgetsRemoved;
		}
		
		private int cleanUp() {
			for (String playerName : matchPlayerGadgets.keySet()) {
				List<Gadget> playerGadgets = matchPlayerGadgets.get(playerName);
				for (Gadget gadget : playerGadgets) {
					gadget.dispose(false, true);
				}
			}
			matchPlayerGadgets = new HashMap<String, List<Gadget>>();
			int gadgetsRemoved = overallMatchCounter;
			overallMatchCounter = 0;
			return gadgetsRemoved;
		}
	}
	
}
