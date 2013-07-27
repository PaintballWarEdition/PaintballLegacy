package de.blablubbabc.paintball.gadgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
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
			matchEntry = new MatchEntry();
			gadgets.put(match, matchEntry);
		}
		matchEntry.addMatchGadget(playerName, gadget);
		overallCounter++;
	}
	
	// returns true, if gadget was found and removed
	public boolean removeGadget(Match match, String playerName, Gadget gadget) {
		if (match == null || playerName == null || gadget == null) throw new IllegalArgumentException();
		MatchEntry matchEntry = gadgets.get(match);
		if (matchEntry != null) {
			if (matchEntry.removeMatchGadget(playerName, gadget)) {
				if (matchEntry.getMatchGadgetCount() == 0) gadgets.remove(match);
				overallCounter--;
				return true;
			}
		}
		return false;
	}
	
	public List<Gadget> getGadgets(Match match, String playerName) {
		if (match == null || playerName == null) throw new IllegalArgumentException();
		MatchEntry matchEntry = gadgets.get(match);
		
		return matchEntry != null ? matchEntry.getMatchGadgets(playerName) : new ArrayList<Gadget>();
	}
	
	// COMPARE TO ENTITY
	
	public boolean isGadget(Entity entity) {
		return getGadget(entity) != null;
	}
	
	public boolean isGadget(Entity entity, String playerName) {
		return getGadget(entity, playerName) != null;
	}
	
	public boolean isGadget(Entity entity, Match match, String playerName) {
		return getGadget(entity, match, playerName) != null;
	}
	
	public Gadget getGadget(Entity entity, Match match) {
		if (entity == null || match == null) throw new IllegalArgumentException();
		MatchEntry matchEntry = gadgets.get(match);
		if (matchEntry != null) {
			Gadget gadget = matchEntry.getGadget(entity);
			if (gadget != null) {
				return gadget;
			}
		}
		return null;
	}
	
	public Gadget getGadget(Entity entity) {
		if (entity == null) throw new IllegalArgumentException();
		for (MatchEntry matchEntry : gadgets.values()) {
			Gadget gadget = matchEntry.getGadget(entity);
			if (gadget != null) {
				return gadget;
			}
		}
		return null;
	}
	
	public Gadget getGadget(Entity entity, String playerName) {
		if (entity == null || playerName == null) throw new IllegalArgumentException();
		for (MatchEntry matchEntry : gadgets.values()) {
			Gadget gadget = matchEntry.getGadget(entity, playerName);
			if (gadget != null) {
				return gadget;
			}
		}
		return null;
	}
	
	public Gadget getGadget(Entity entity, Match match, String playerName) {
		if (entity == null || match == null || playerName == null) throw new IllegalArgumentException();
		MatchEntry matchEntry = gadgets.get(match);
		if (matchEntry != null) {
			Gadget gadget = matchEntry.getGadget(entity, playerName);
			if (gadget != null) {
				return gadget;
			}
		}
		return null;
	}
	
	// COMPARE TO LOCATION (BLOCK)
	
	public boolean isGadget(Location location) {
		return getGadget(location) != null;
	}
	
	public boolean isGadget(Location location, String playerName) {
		return getGadget(location, playerName) != null;
	}
	
	public boolean isGadget(Location location, Match match, String playerName) {
		return getGadget(location, match, playerName) != null;
	}
	
	public Gadget getGadget(Location location) {
		if (location == null) throw new IllegalArgumentException();
		for (MatchEntry matchEntry : gadgets.values()) {
			Gadget gadget = matchEntry.getGadget(location);
			if (gadget != null) {
				/*if (removeWhenFound) {
					if (matchEntry.getMatchGadgetCount() == 0) gadgets.remove(matchEntry.match);
					overallCounter--;
				}*/
				return gadget;
			}
		}
		return null;
	}
	
	public Gadget getGadget(Location location, String playerName) {
		if (location == null || playerName == null) throw new IllegalArgumentException();
		for (MatchEntry matchEntry : gadgets.values()) {
			Gadget gadget = matchEntry.getGadget(location, playerName);
			if (gadget != null) {
				return gadget;
			}
		}
		return null;
	}
	
	public Gadget getGadget(Location location, Match match) {
		if (location == null || match == null) throw new IllegalArgumentException();
		MatchEntry matchEntry = gadgets.get(match);
		if (matchEntry != null) {
			Gadget gadget = matchEntry.getGadget(location);
			if (gadget != null) {
				return gadget;
			}
		}
		return null;
	}
	
	public Gadget getGadget(Location location, Match match, String playerName) {
		if (location == null || match == null || playerName == null) throw new IllegalArgumentException();
		MatchEntry matchEntry = gadgets.get(match);
		if (matchEntry != null) {
			Gadget gadget = matchEntry.getGadget(location, playerName);
			if (gadget != null) {
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
		private int overallMatchCounter = 0;
		private Map<String, List<Gadget>> matchPlayerGadgets = new HashMap<String, List<Gadget>>();
		
		private MatchEntry() {
			
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
		
		// returns true, if gadget was found and removed
		private boolean removeMatchGadget(String playerName, Gadget gadget) {
			List<Gadget> playerGadgets = matchPlayerGadgets.get(playerName);
			if (playerGadgets != null) {
				if (playerGadgets.remove(gadget)) {
					if (playerGadgets.size() == 0) matchPlayerGadgets.remove(playerName);
					overallMatchCounter--;
					return true;
				}
			}
			return false;
		}
		
		private List<Gadget> getMatchGadgets(String playerName) {
			List<Gadget> playerGadgets = matchPlayerGadgets.get(playerName);
			if (playerGadgets == null) {
				playerGadgets = new ArrayList<Gadget>();
			}
			return playerGadgets;
		}
		
		// COMPARE TO ENTITY
		
		private Gadget getGadget(Entity entity) {
			for (Entry<String, List<Gadget>> playerEntry : matchPlayerGadgets.entrySet()) {
				List<Gadget> playerGadgets = playerEntry.getValue();
				for (Gadget gadget : playerGadgets) {
					if (gadget.isSimiliar(entity)) {
						return gadget;
					}
				}
			}
			return null;
		}
		
		private Gadget getGadget(Entity entity, String playerName) {
			List<Gadget> playerGadgets = matchPlayerGadgets.get(playerName);
			if (playerGadgets != null) {
				for (Gadget gadget : playerGadgets) {
					if (gadget.isSimiliar(entity)) {
						return gadget;
					}
				}
			}
			return null;
		}
		
		// COMPARE TO LOCATION (BLOCK)
		
		private Gadget getGadget(Location location) {
			for (Entry<String, List<Gadget>> playerEntry : matchPlayerGadgets.entrySet()) {
				List<Gadget> playerGadgets = playerEntry.getValue();
				for (Gadget gadget : playerGadgets) {
					if (gadget.isSimiliar(location)) {
						return gadget;
					}
				}
			}
			
			return null;
		}
		
		private Gadget getGadget(Location location, String playerName) {
			List<Gadget> playerGadgets = matchPlayerGadgets.get(playerName);
			if (playerGadgets != null) {
				for (Gadget gadget : playerGadgets) {
					if (gadget.isSimiliar(location)) {
						return gadget;
					}
				}
			}
			return null;
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
					gadget.dispose(false);
				}
			}
			overallMatchCounter -= gadgetsRemoved;
			return gadgetsRemoved;
		}
		
		private int cleanUp() {
			for (String playerName : matchPlayerGadgets.keySet()) {
				List<Gadget> playerGadgets = matchPlayerGadgets.get(playerName);
				for (Gadget gadget : playerGadgets) {
					gadget.dispose(false);
				}
			}
			matchPlayerGadgets = new HashMap<String, List<Gadget>>();
			int gadgetsRemoved = overallMatchCounter;
			overallMatchCounter = 0;
			return gadgetsRemoved;
		}
	}
	
}
