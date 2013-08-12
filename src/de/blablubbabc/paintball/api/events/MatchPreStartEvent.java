package de.blablubbabc.paintball.api.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.blablubbabc.paintball.api.arena.ArenaI;
import de.blablubbabc.paintball.api.gametype.GametypeI;
import de.blablubbabc.paintball.api.lobby.LobbyI;

public class MatchPreStartEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private boolean cancelled;
	
	private final LobbyI lobby;
	
	private ArenaI arena;
	private GametypeI gametype;
	private String gameConfig;
	
	public MatchPreStartEvent(LobbyI lobby, ArenaI arena, GametypeI gametype, String gameConfig) {
		this.lobby = lobby;
		this.arena = arena;
		this.gametype = gametype;
		this.gameConfig = gameConfig;
	}

	public LobbyI getLobby() {
		return lobby;
	}
	
	public ArenaI getArena() {
		return arena;
	}

	public void setArena(ArenaI arena) {
		if (arena == null) throw new IllegalArgumentException("Arena is null!");
		// check is Ready
		// check supports gametype
		this.arena = arena;
	}
	
	public String getGameConfig() {
		return gameConfig;
	}
	
	public GametypeI getGametype() {
		return gametype;
	}
	
	public void setGametypeAndConfig(GametypeI gametype, String gameConfig) {
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
