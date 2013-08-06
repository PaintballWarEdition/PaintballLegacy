package de.blablubbabc.paintball.api.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.blablubbabc.paintball.api.Arena;
import de.blablubbabc.paintball.api.Gametype;
import de.blablubbabc.paintball.api.Lobby;

public class MatchPreStartEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private boolean cancelled;
	
	private final Lobby lobby;
	
	private Arena arena;
	private Gametype gametype;
	private String gameConfig;
	
	public MatchPreStartEvent(Lobby lobby, Arena arena, Gametype gametype, String gameConfig) {
		this.lobby = lobby;
		this.arena = arena;
		this.gametype = gametype;
		this.gameConfig = gameConfig;
	}

	public Lobby getLobby() {
		return lobby;
	}
	
	public Arena getArena() {
		return arena;
	}

	public void setArena(Arena arena) {
		if (arena == null) throw new IllegalArgumentException("Arena is null!");
		// check is Ready
		// check supports gametype
		this.arena = arena;
	}
	
	public String getGameConfig() {
		return gameConfig;
	}
	
	public Gametype getGametype() {
		return gametype;
	}
	
	public void setGametypeAndConfig(Gametype gametype, String gameConfig) {
		if (gametype == null) throw new IllegalArgumentException("Gametype is null!");
		if (gameConfig == null || gameConfig.isEmpty()) throw new IllegalArgumentException("No GameConfig was given");
		// check: is supported by arena
		// check if gameCOnfig exists
		this.gametype = gametype;
		this.gameConfig = gameConfig;
	}
	
	@Override
	public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

}
