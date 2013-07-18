package de.blablubbabc.paintball;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class FragInformations {
	private final Player killer;
	private final Player target;
	private final Origin origin;
	
	private ChatColor killerColor = ChatColor.WHITE;
	private ChatColor targetColor = ChatColor.WHITE;
	
	private String preKiller = "";
	private String afterKiller = "";
	private String preTarget = "";
	private String afterTarget = "";
	
	public FragInformations(Player killer, Player target, Origin origin) {
		this(killer, target, origin, ChatColor.WHITE, ChatColor.WHITE);
	}
	
	public FragInformations(Player killer, Player target, Origin origin, ChatColor killerColor, ChatColor targetColor) {
		if (killer == null || target == null || origin == null) throw new IllegalArgumentException("Killer, Target or Origin is null!");
		this.killer = killer;
		this.target = target;
		this.origin = origin;
		
		this.killerColor = killerColor != null ? killerColor : ChatColor.WHITE;
		this.targetColor = targetColor != null ? targetColor : ChatColor.WHITE;
	}
	
	// GETTERS
	
	public Player getKiller() {
		return killer;
	}
	
	public Player getTarget() {
		return target;
	}
	
	public Origin getOrigin() {
		return origin;
	}
	
	public ChatColor getKillerColor() {
		return killerColor;
	}
	
	public ChatColor getTargetColor() {
		return targetColor;
	}
	
	public String getPreKiller() {
		return preKiller;
	}
	
	public String getPreTarget() {
		return preTarget;
	}
	
	public String getAfterKiller() {
		return afterKiller;
	}
	
	public String getAfterTarget() {
		return afterTarget;
	}
	
	
	// SETTERS
	
	public void setKillerColor(ChatColor killerColor) {
		this.killerColor = killerColor;
	}
	
	public void setTargetColor(ChatColor targetColor) {
		this.targetColor = targetColor;
	}
	
	public void setPreKiller(String preKiller) {
		this.preKiller = preKiller != null ? preKiller : "";
	}
	
	public void setPreTarget(String preTarget) {
		this.preTarget = preTarget != null ? preTarget : "";
	}
	
	public void setAfterKiller(String afterKiller) {
		this.afterKiller = afterKiller != null ? afterKiller : "";
	}
	
	public void setAfterTarget(String afterTarget) {
		this.afterTarget = afterTarget != null ? afterTarget : "";
	}
}
