/**
 * Copyright (c) blablubbabc <http://www.blablubbabc.de>
 * All rights reserved.
 */
package de.blablubbabc.paintball.gadgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import de.blablubbabc.paintball.Match;

public class GadgetManager {

	private Map<Match, MatchEntry> gadgets = new HashMap<>();
	private int overallCounter = 0;

	public GadgetManager() {
	}

	public int getOverallCount() {
		return overallCounter;
	}

	public int getMatchGadgetCount(Match match) {
		if (match == null) throw new IllegalArgumentException();
		MatchEntry matchEntry = gadgets.get(match);
		return matchEntry == null ? 0 : matchEntry.getMatchGadgetCount();
	}

	public int getPlayerGadgetCount(Match match, UUID playerId) {
		if (match == null || playerId == null) throw new IllegalArgumentException();
		MatchEntry matchEntry = gadgets.get(match);
		return matchEntry == null ? 0 : matchEntry.getMatchPlayerGadgetCount(playerId);
	}

	public void addGadget(Match match, UUID playerId, Gadget gadget) {
		if (match == null || playerId == null || gadget == null) throw new IllegalArgumentException();
		MatchEntry matchEntry = gadgets.get(match);
		if (matchEntry == null) {
			matchEntry = new MatchEntry();
			gadgets.put(match, matchEntry);
		}
		matchEntry.addMatchGadget(playerId, gadget);
		overallCounter++;
	}

	// returns true, if gadget was found and removed
	public boolean removeGadget(Match match, UUID playerId, Gadget gadget) {
		if (match == null || playerId == null || gadget == null) throw new IllegalArgumentException();
		MatchEntry matchEntry = gadgets.get(match);
		if (matchEntry != null) {
			if (matchEntry.removeMatchGadget(playerId, gadget)) {
				if (matchEntry.getMatchGadgetCount() == 0) gadgets.remove(match);
				overallCounter--;
				return true;
			}
		}
		return false;
	}

	public List<Gadget> getGadgets(Match match, UUID playerId) {
		if (match == null || playerId == null) throw new IllegalArgumentException();
		MatchEntry matchEntry = gadgets.get(match);

		return matchEntry != null ? matchEntry.getMatchGadgets(playerId) : new ArrayList<Gadget>();
	}

	// COMPARE TO ENTITY

	public boolean isGadget(Entity entity) {
		return getGadget(entity) != null;
	}

	public boolean isGadget(Entity entity, UUID playerId) {
		return getGadget(entity, playerId) != null;
	}

	public boolean isGadget(Entity entity, Match match, UUID playerId) {
		return getGadget(entity, match, playerId) != null;
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

	public Gadget getGadget(Entity entity, UUID playerId) {
		if (entity == null || playerId == null) throw new IllegalArgumentException();
		for (MatchEntry matchEntry : gadgets.values()) {
			Gadget gadget = matchEntry.getGadget(entity, playerId);
			if (gadget != null) {
				return gadget;
			}
		}
		return null;
	}

	public Gadget getGadget(Entity entity, Match match, UUID playerId) {
		if (entity == null || match == null || playerId == null) throw new IllegalArgumentException();
		MatchEntry matchEntry = gadgets.get(match);
		if (matchEntry != null) {
			Gadget gadget = matchEntry.getGadget(entity, playerId);
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

	public boolean isGadget(Location location, UUID playerId) {
		return getGadget(location, playerId) != null;
	}

	public boolean isGadget(Location location, Match match, UUID playerId) {
		return getGadget(location, match, playerId) != null;
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

	public Gadget getGadget(Location location, UUID playerId) {
		if (location == null || playerId == null) throw new IllegalArgumentException();
		for (MatchEntry matchEntry : gadgets.values()) {
			Gadget gadget = matchEntry.getGadget(location, playerId);
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

	public Gadget getGadget(Location location, Match match, UUID playerId) {
		if (location == null || match == null || playerId == null) throw new IllegalArgumentException();
		MatchEntry matchEntry = gadgets.get(match);
		if (matchEntry != null) {
			Gadget gadget = matchEntry.getGadget(location, playerId);
			if (gadget != null) {
				return gadget;
			}
		}
		return null;
	}

	public void cleanUp(Match match, UUID playerId) {
		if (match == null || playerId == null) throw new IllegalArgumentException();
		MatchEntry matchEntry = gadgets.get(match);
		if (matchEntry != null) {
			overallCounter -= matchEntry.cleanUp(playerId);
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
		private Map<UUID, List<Gadget>> matchPlayerGadgets = new HashMap<>();

		private MatchEntry() {

		}

		private void addMatchGadget(UUID playerId, Gadget gadget) {
			List<Gadget> playerGadgets = matchPlayerGadgets.get(playerId);
			if (playerGadgets == null) {
				playerGadgets = new ArrayList<Gadget>();
				matchPlayerGadgets.put(playerId, playerGadgets);
			}
			playerGadgets.add(gadget);
			overallMatchCounter++;
		}

		// returns true, if gadget was found and removed
		private boolean removeMatchGadget(UUID playerId, Gadget gadget) {
			List<Gadget> playerGadgets = matchPlayerGadgets.get(playerId);
			if (playerGadgets != null) {
				if (playerGadgets.remove(gadget)) {
					if (playerGadgets.size() == 0) matchPlayerGadgets.remove(playerId);
					overallMatchCounter--;
					return true;
				}
			}
			return false;
		}

		private List<Gadget> getMatchGadgets(UUID playerId) {
			List<Gadget> playerGadgets = matchPlayerGadgets.get(playerId);
			if (playerGadgets == null) {
				playerGadgets = new ArrayList<Gadget>();
			}
			return playerGadgets;
		}

		// COMPARE TO ENTITY

		private Gadget getGadget(Entity entity) {
			for (Entry<UUID, List<Gadget>> playerEntry : matchPlayerGadgets.entrySet()) {
				List<Gadget> playerGadgets = playerEntry.getValue();
				for (Gadget gadget : playerGadgets) {
					if (gadget.isSimiliar(entity)) {
						return gadget;
					}
				}
			}
			return null;
		}

		private Gadget getGadget(Entity entity, UUID playerId) {
			List<Gadget> playerGadgets = matchPlayerGadgets.get(playerId);
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
			for (Entry<UUID, List<Gadget>> playerEntry : matchPlayerGadgets.entrySet()) {
				List<Gadget> playerGadgets = playerEntry.getValue();
				for (Gadget gadget : playerGadgets) {
					if (gadget.isSimiliar(location)) {
						return gadget;
					}
				}
			}

			return null;
		}

		private Gadget getGadget(Location location, UUID playerId) {
			List<Gadget> playerGadgets = matchPlayerGadgets.get(playerId);
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

		private int getMatchPlayerGadgetCount(UUID playerId) {
			List<Gadget> playerGadgets = matchPlayerGadgets.get(playerId);
			return playerGadgets == null ? 0 : playerGadgets.size();
		}

		private int cleanUp(UUID playerId) {
			List<Gadget> playerGadgets = matchPlayerGadgets.remove(playerId);
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
			for (UUID playerId : matchPlayerGadgets.keySet()) {
				List<Gadget> playerGadgets = matchPlayerGadgets.get(playerId);
				for (Gadget gadget : playerGadgets) {
					gadget.dispose(false);
				}
			}
			matchPlayerGadgets = new HashMap<>();
			int gadgetsRemoved = overallMatchCounter;
			overallMatchCounter = 0;
			return gadgetsRemoved;
		}
	}

}
