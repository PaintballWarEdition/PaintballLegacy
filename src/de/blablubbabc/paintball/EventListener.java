package de.blablubbabc.paintball;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import de.blablubbabc.paintball.gadgets.Gadget;
import de.blablubbabc.paintball.statistics.player.PlayerStat;
import de.blablubbabc.paintball.statistics.player.PlayerStats;
import de.blablubbabc.paintball.utils.Log;
import de.blablubbabc.paintball.utils.Sounds;
import de.blablubbabc.paintball.utils.Translator;

public class EventListener implements Listener {
	private Paintball plugin;
	private Origin meleeOrigin = new Origin() {
		
		@Override
		public String getKillMessage(FragInformations fragInfo) {
			return Translator.getString("WEAPON_FEED_MELEE", getDefaultVariablesMap(fragInfo));
		}
	};
	
	private long lastSignUpdate = 0;

	public EventListener(Paintball pl) {
		plugin = pl;
	}

	// /////////////////////////////////////////
	// EVENTS
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onSignCreate(SignChangeEvent event) {
		Player player = event.getPlayer();
		String l = ChatColor.stripColor(event.getLine(0));

		if (l.startsWith("[PB ")) {
			for (String key : PlayerStat.getKeys()) {
				if (key.equals("teamattacks"))
					key = "ta";
				else if (key.equals("hitquote"))
					key = "hq";
				else if (key.equals("airstrikes"))
					key = "as";
				else if (key.equals("money_spent"))
					key = "spent";

				if (l.equalsIgnoreCase("[PB " + key.toUpperCase() + "]") || l.equalsIgnoreCase("[PB R " + key.toUpperCase() + "]") || l.equalsIgnoreCase("[PB RANK]")) {
					if (!player.isOp() && !player.hasPermission("paintball.admin")) {
						event.setCancelled(true);
						player.sendMessage(Translator.getString("NO_PERMISSION"));
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onHangingEntityBreak(HangingBreakByEntityEvent event) {
		Entity remover = event.getRemover();
		if (remover instanceof Projectile) {
			Projectile projectile = (Projectile) remover;
			if (projectile.getShooter() instanceof Player) {
				remover = (Player) projectile.getShooter();
			}
		}
		
		if (remover instanceof Player) {
			if (Lobby.LOBBY.isMember((Player) remover)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onWorldChange(PlayerChangedWorldEvent event) {
		if (plugin.worldMode) {
			Player player = event.getPlayer();
			boolean fromPb = plugin.worldModeWorlds.contains(event.getFrom().getName());
			boolean toPb = plugin.worldModeWorlds.contains(event.getPlayer().getWorld().getName());
			if (!fromPb && toPb) {
				if (!Lobby.LOBBY.isMember(player)) {
					if (plugin.autoTeam) {
						plugin.playerManager.joinTeam(player, false, Lobby.RANDOM);
					} else {
						plugin.playerManager.joinLobbyPre(player, false, null);
					}
				}
			} else if (fromPb && !toPb) {
				plugin.playerManager.leaveLobby(player, true);
			}
		}
	}

	// denying all inventories (chests, furnaces, etc..) in the lobby
	@EventHandler(priority = EventPriority.NORMAL)
	public void onInventoryOpen(InventoryOpenEvent event) {
		Player player = (Player) event.getPlayer();
		if (Lobby.LOBBY.isMember(player)) {
			if (!event.getInventory().getName().equalsIgnoreCase(Translator.getString("SHOP_NAME"))) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onInteract(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		if (block != null) {
			BlockState state = block.getState();
			if (state instanceof Sign) {
				Sign sign = (Sign) state;
				String l = ChatColor.stripColor(sign.getLine(0));

				if (l.startsWith("[PB ")) {
					if (l.equalsIgnoreCase("[PB RANK]")) {
						changeSign(event.getPlayer().getName(), sign, "points", true);
					} else {
						for (String key : PlayerStat.getKeys()) {
							String s = key;
							if (s.equals("teamattacks"))
								s = "ta";
							else if (s.equals("hitquote"))
								s = "hq";
							else if (s.equals("airstrikes"))
								s = "as";
							else if (s.equals("money_spent"))
								s = "spent";

							if (l.equalsIgnoreCase("[PB " + s.toUpperCase() + "]")) {
								changeSign(event.getPlayer().getName(), sign, key, false);
								break;
							} else if (l.equalsIgnoreCase("[PB R " + s.toUpperCase() + "]")) {
								changeSign(event.getPlayer().getName(), sign, key, true);
								break;
							}
						}
					}
				}
			}
		}
	}

	private void changeSign(String player, Sign sign, String stat, boolean rank) {
		if ((System.currentTimeMillis() - lastSignUpdate) > (250)) {
			PlayerStat pStat = PlayerStat.getFromKey(stat);
			if (pStat != null) {
				Map<String, String> vars = new HashMap<String, String>();
				vars.put("player", player);
				PlayerStats stats = Paintball.instance.playerManager.getPlayerStats(player);
				// stats for this player even exist ?
				if (stats != null) {
					if (rank) {
						vars.put("value", String.valueOf(plugin.statsManager.getRank(player, pStat)));
					} else {
						if (pStat == PlayerStat.HITQUOTE|| pStat == PlayerStat.KD) {
							float statF = (float) stats.getStat(pStat) / 100;
							vars.put("value", Stats.decimalFormat.format(statF));
						} else {
							vars.put("value", String.valueOf(stats.getStat(pStat)));
						}
					}
				} else {
					vars.put("value", Translator.getString("NOT_FOUND"));
				}
				sign.setLine(1, Translator.getString("SIGN_LINE_TWO", vars));
				sign.setLine(2, Translator.getString("SIGN_LINE_THREE", vars));
				sign.setLine(3, Translator.getString("SIGN_LINE_FOUR", vars));
				sign.update();
				lastSignUpdate = System.currentTimeMillis();	
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void onPlayerHit(EntityDamageByEntityEvent event) {
		Player attacker = null;
		
		Entity damager = event.getDamager();
		
		if (damager instanceof Projectile) {
			Projectile projectile = (Projectile) damager;
			if (projectile.getShooter() instanceof Player) {
				attacker = (Player) projectile.getShooter();
			}
		} else if (damager instanceof Player) {
			attacker = (Player) damager;
		}
		
		if (attacker != null) {
			if (Lobby.LOBBY.isMember(attacker)) {
				Entity damagedEntity = event.getEntity();
				EntityType damagedType = damagedEntity.getType();
				// handle damage to hanging entities:
				if (damagedType == EntityType.PAINTING || damagedType == EntityType.ITEM_FRAME) {
					event.setCancelled(true);
					return;
				} else if (damagedType == EntityType.PLAYER) {
					Player target = (Player) event.getEntity();
					if (target == attacker) return;
					Match matchA = plugin.matchManager.getMatch(attacker);
					if (matchA == null) return;
					Match matchB = plugin.matchManager.getMatch(target);
					if (matchB == null || matchA != matchB) return;
					if (!matchA.isSpec(attacker) && !matchA.isSpec(target) && matchA.isSurvivor(attacker) && matchA.isSurvivor(target) && matchA.hasStarted()) {
						// damage cause?
						if (event.getCause() == DamageCause.PROJECTILE) {
							// Paintball hit?
							if (damager instanceof Snowball) {
								Gadget ball = plugin.weaponManager.getBallHandler().getBall(event.getDamager(), matchA, attacker.getName());
								if (ball != null) {
									matchA.onHitByBall(target, attacker, ball.getGadgetOrigin());
								}
							}
						} else if (plugin.allowMelee && event.getCause() == DamageCause.ENTITY_ATTACK) {
							if (matchA.enemys(target, attacker)) {
								if (target.getHealth() > plugin.meleeDamage) {
									target.setHealth(target.getHealth() - plugin.meleeDamage);
									Sounds.playMeleeHit(attacker, target);
								} else {
									matchA.frag(target, attacker, meleeOrigin);
								}
							}
						}
					}
				}
			}
		}
	}

	/*@EventHandler(priority = EventPriority.NORMAL)
	public void onFireballExplosion(ExplosionPrimeEvent event) {
		Entity entity = event.getEntity();
		if (entity != null && entity.getType() == EntityType.FIREBALL) {
			Fireball fireball = (Fireball) entity;
			if (fireball.getShooter() instanceof Player) {
				if (Rocket.getRocket(fireball, ((Player)fireball.getShooter()).getName(), false) != null) {
					event.setCancelled(true);
				}
			}
		}
	}*/

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerShoot(ProjectileLaunchEvent event) {
		if (event.getEntity().getShooter() instanceof Player) {
			Player player = (Player) event.getEntity().getShooter();
			if (Lobby.isPlaying(player)) {
				if (event.getEntity().getType() != EntityType.SPLASH_POTION) event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInventory(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			Player player = (Player) event.getWhoClicked();
			if (Lobby.LOBBY.isMember(player)) {
				if (event.getSlotType() != SlotType.CONTAINER && event.getSlotType() != SlotType.QUICKBAR && event.getSlotType() != SlotType.OUTSIDE) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerInteractPlayer(PlayerInteractEntityEvent event) {
		Player player = (Player) event.getPlayer();
		if (Lobby.LOBBY.isMember(player)) {
			Entity clickedEntity = event.getRightClicked();
			EntityType clickedType = clickedEntity.getType();
			if (clickedType == EntityType.ITEM_FRAME || clickedType == EntityType.PAINTING) {
				// no clicking of item frames:
				event.setCancelled(true);
			} else if (clickedType == EntityType.PLAYER) {
				// gifting others:
				if (!plugin.giftsEnabled) return;
				ItemStack itemInHand = player.getItemInHand();
				if (itemInHand != null && itemInHand.getType() == Material.CHEST) {
					Player receiver = (Player) event.getRightClicked();
					if (Lobby.getTeam(receiver) != null) {
						plugin.weaponManager.getGiftManager().transferGift(player, receiver);
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void onPlayerInteractHandleWeapons(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		String playerName = player.getName();
		if (player.getGameMode() == GameMode.CREATIVE) return;
		if (Lobby.LOBBY.isMember(player)) {
			Match match = plugin.matchManager.getMatch(player);
			if (match != null && Lobby.isPlaying(player) && match.isSurvivor(player)) {
				ItemStack item = event.getItem();
				if (item != null && item.getType() != Material.POTION) {
					event.setUseItemInHand(Result.DENY);
					
					// shop book:
					if (plugin.shop && item.isSimilar(plugin.shopManager.item)) {
						plugin.shopManager.getShopMenu().open(player);
						return;
					}
				}
				
				if (!match.hasStarted() || match.isJustRespawned(playerName)) return;
				
				// handle weapons and gadgets:
				plugin.weaponManager.onInteract(event, match);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onItemInHand(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();
		if (Lobby.LOBBY.isMember(player)) {
			plugin.weaponManager.onItemHeld(player, player.getInventory().getItem(event.getNewSlot()));
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onProjectileHit(ProjectileHitEvent event) {
		Projectile projectile = event.getEntity();
		if (projectile.getShooter() instanceof Player) {
			Player shooter = (Player) projectile.getShooter();
			Match match = plugin.matchManager.getMatch(shooter);
			
			if (match != null) {
				plugin.weaponManager.onProjectileHit(event, projectile, match, shooter);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player target = (Player) event.getEntity();
			Lobby team = Lobby.getTeam(target);
			if (team != null) {
				DamageCause cause = event.getCause();
				int fireTicks = 0;
				if (cause == DamageCause.FIRE_TICK) {
					fireTicks = target.getFireTicks();
					target.setFireTicks(0);
				}
				double damage = event.getDamage();
				event.setDamage(0);
				event.setCancelled(true);
				Match match = plugin.matchManager.getMatch(target);
				if (match != null && team != Lobby.SPECTATE && match.isSurvivor(target) && match.hasStarted()) {
					if ((plugin.falldamage && cause == DamageCause.FALL) || (plugin.otherDamage && cause != DamageCause.FALL && cause != DamageCause.ENTITY_ATTACK && cause != DamageCause.PROJECTILE)) {
						if (target.getHealth() <= damage) {
							match.death(target);
						} else {
							event.setDamage(damage);
							event.setCancelled(false);
							// fire ticks:
							if (cause == DamageCause.FIRE_TICK && plugin.otherDamage) {
								target.setFireTicks(fireTicks);
							}
							
							//heal armor
							PlayerInventory inventory = target.getInventory();
							ItemStack[] armor = inventory.getArmorContents();

							for (int i = 0; i < armor.length; i++) {
							    if(armor[i] != null) {
							        armor[i].setDurability((short) 0);
							    }
							}

							inventory.setArmorContents(armor);
							target.updateInventory();
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (Lobby.LOBBY.isMember(player)) {
			if (player.getGameMode() != GameMode.CREATIVE) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onPlayerPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		
		if (player.getGameMode() == GameMode.CREATIVE) return;
		
		if (Lobby.LOBBY.isMember(player)) {
			event.setCancelled(true);
			Match match = plugin.matchManager.getMatch(player);
			if (match != null && match.hasStarted() && match.isSurvivor(player)) {
				
				// handle weapons and gadgets:
				plugin.weaponManager.onBlockPlace(event, match);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerHunger(FoodLevelChangeEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if (Lobby.LOBBY.isMember(player)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerItemsI(PlayerPickupItemEvent event) {
		plugin.weaponManager.onItemPickup(event);
		if (event.isCancelled()) return;
		
		Player player = event.getPlayer();
		if (Lobby.LOBBY.isMember(player)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerItemsII(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		if (Lobby.LOBBY.isMember(player)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		// always allow pb commands:
		if (event.getMessage().startsWith("/pb")) {
			if (event.isCancelled()) event.setCancelled(false);
		} else {
			if (Lobby.LOBBY.isMember(event.getPlayer()) && (!plugin.allowedCommands.isEmpty() ? !isAllowedCommand(event.getMessage()) : true)) {
				if (!event.getPlayer().hasPermission("paintball.admin") && !event.getPlayer().isOp()) {
					event.getPlayer().sendMessage(Translator.getString("COMMAND_NOT_ALLOWED"));
					event.setCancelled(true);
				}
			} else if (plugin.checkBlacklist && isBlacklistedCommand(event.getMessage())) {
				if (!plugin.blacklistAdminOverride && !event.getPlayer().hasPermission("paintball.admin") && !event.getPlayer().isOp()) {
					event.getPlayer().sendMessage(Translator.getString("COMMAND_BLACKLISTED"));
					event.setCancelled(true);
				}
			}
		}
	}

	private boolean isBlacklistedCommand(String cmd) {
		Set<Player> players = Lobby.LOBBY.getMembers();
		List<String> playernames = new ArrayList<String>();
		for (Player p : players) {
			playernames.add(p.getName());
		}
		for (String regex : plugin.blacklistedCommandsRegex) {
			for (String name : playernames) {
				if (cmd.matches(regex.replace("{player}", Pattern.quote(name)))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isAllowedCommand(String cmd) {
		if (plugin.allowedCommands.contains(cmd))
			return true;
		String[] split = cmd.split(" ");
		String cmds = "";
		for (int i = 0; i < split.length; i++) {
			cmds += split[i];
			if (plugin.allowedCommands.contains(cmds) || plugin.allowedCommands.contains(cmds + " *"))
				return true;
			cmds += " ";
		}
		return false;
	}
	
	/*@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChatEarly(AsyncPlayerChatEvent event) {
		if (plugin.chatMessageColor) {
			final Player player = event.getPlayer();
			if (Lobby.LOBBY.isMember(player)) {
				ChatColor color = Lobby.LOBBY.color();
				if (Lobby.isPlaying(player) || Lobby.isSpectating(player)) {
					Match match = plugin.matchManager.getMatch(player);
					// colorize message:
					if (match.isRed(player))
						color = Lobby.RED.color();
					else if (match.isBlue(player))
						color = Lobby.BLUE.color();
					else if (match.isSpec(player))
						color = Lobby.SPECTATE.color();
				}
				if (plugin.chatMessageColor) event.setMessage(color + event.getMessage());
			}
		}
	}*/
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerChatLate(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		String playerName = player.getName();
		if (Lobby.LOBBY.isMember(player)) {
			// determine team color:
			
			ChatColor color = Lobby.LOBBY.color();
			if (Lobby.isPlaying(player) || Lobby.isSpectating(player)) {
				Match match = plugin.matchManager.getMatch(player);
				if (match.isRed(player)) {
					color = Lobby.RED.color();
				} else if (match.isBlue(player)) {
					color = Lobby.BLUE.color();
				} else if (match.isSpec(player)) {
					color = Lobby.SPECTATE.color();
				}
			}
			
			String messageToUse = event.getMessage();
			String formatToUse = plugin.chatReplaceFormat ? plugin.chatFormat : event.getFormat();
			
			if (plugin.chatMessageColor) {
				messageToUse = color + event.getMessage();
			}
			
			if (plugin.chatNameColor) {
				// change name color via format:
				String displayName = ChatColor.stripColor(player.getDisplayName());
				formatToUse = formatToUse.replaceFirst(displayName, color + displayName);
			}
			
			if (plugin.ranksChatPrefix ) {
				Rank rank = plugin.rankManager.getRank(playerName);
				String prefix = rank.getPrefix();
				if (prefix != null && !prefix.isEmpty()) {
					formatToUse = prefix + formatToUse;
				}
			}
			
			
			// chat feature onyl visible for paintballers:
			if (plugin.chatFeaturesOnlyForPaintballersVisible) {
				// send chat message to paintballers manually:
				String messageToSend = String.format(formatToUse, player.getDisplayName(), messageToUse);
				
				Set<Player> recipients = event.getRecipients();
				Iterator<Player> iter = recipients.iterator();
				while (iter.hasNext()) {
					Player recipient = iter.next();
					if (Lobby.LOBBY.isMember(player)) {
						iter.remove();
						recipient.sendMessage(messageToSend);
					}
				}
			} else {
				event.setFormat(formatToUse);
				event.setMessage(messageToUse);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = (Player) event.getEntity();
		if (plugin.playerManager.leaveLobby(player, true)) {
			// drops?
			event.setDroppedExp(0);
			event.setKeepLevel(false);
			event.getDrops().clear();
			Log.severe("WARNING: IllegalState! A player died while playing paintball. Report this to blablubbabc !");
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = (Player) event.getPlayer();
		plugin.playerManager.addPlayerAsync(player.getName());

		// now done by playerManager directly after player wass added to database..
		/*plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {

			@Override
			public void run() {
				if (plugin.autoLobby && plugin.autoTeam)
					plugin.commandManager.joinTeam(player, false, Lobby.RANDOM);
				else if (plugin.autoLobby)
					plugin.commandManager.joinLobbyPre(player, false, null);
			}
		}, 1L);*/

		// notify admins on update:
		if (plugin.needsUpdate && player.hasPermission("paintball.admin")) {
			player.sendMessage(ChatColor.DARK_PURPLE + "There is a new version of Paintball available! Check out the bukkit dev page: ");
			player.sendMessage(ChatColor.AQUA + "http://dev.bukkit.org/bukkit-mods/paintball_pure_war/");
		}
		
	}	/*
	 * @EventHandler(priority = EventPriority.HIGHEST) public void
	 * onPbCommands(PlayerCommandPreprocessEvent event) { Player player =
	 * event.getPlayer(); String[] m = event.getMessage().split(" "); // basic
	 * commands if (m[0].equalsIgnoreCase("/pb")) { if (m.length == 1) {
	 * plugin.cm.pbhelp(player); } else if (m[1].equalsIgnoreCase("help") ||
	 * m[1].equalsIgnoreCase("?")) { plugin.cm.pbhelp(player); } else if
	 * (m[1].equalsIgnoreCase("info")) { plugin.cm.pbinfo(player); } } }
	 */

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent event) {
		this.onPlayerDisconnect(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerKick(PlayerKickEvent event) {
		this.onPlayerDisconnect(event.getPlayer());
	}

	private void onPlayerDisconnect(Player player) {
		plugin.playerManager.leaveLobby(player, true);
	}

}
