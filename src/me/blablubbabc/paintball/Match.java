package me.blablubbabc.paintball;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Match {

	private Paintball plugin;
	private LinkedHashMap<Player, Integer> redT = new LinkedHashMap<Player, Integer>();
	private LinkedHashMap<Player, Integer> blueT = new LinkedHashMap<Player, Integer>();
	private LinkedHashMap<Player, Integer> shots = new LinkedHashMap<Player, Integer>();
	private LinkedHashMap<Player, Integer> kills = new LinkedHashMap<Player, Integer>();
	private LinkedHashMap<Player, Integer> hits = new LinkedHashMap<Player, Integer>();
	private LinkedHashMap<Player, Integer> teamattacks = new LinkedHashMap<Player, Integer>();
	private LinkedHashMap<Player, Integer> deaths = new LinkedHashMap<Player, Integer>();
	private Set<Player> spec;
	private ArrayList<Player> players;
	private ArrayList<Player> left;
	private String arena;
	private boolean matchOver;
	
	public Match(Paintball plugin, int lives, Set<Player> red, Set<Player> blue, Set<Player> spec, Set<Player> random, String arena) {
		this.plugin = plugin;
		this.arena = arena;
		this.players = new ArrayList<Player>();
		this.left = new ArrayList<Player>();
		this.matchOver = false;
        
		//TEAMS
		for(Player p : red) {
			this.redT.put(p, lives);
		}
		for(Player p : blue) {
			this.blueT.put(p, lives);
		}
		this.spec = spec;
		//randoms:
		List<Player> rand = new ArrayList<Player>();
		for(Player p : random) {
			rand.add(p);
		}
		Collections.shuffle(rand);
		for(Player p : rand) {
			if(this.blueT.size() < this.redT.size()){
				this.blueT.put(p, lives);
			}
			else if(this.redT.size() <= this.blueT.size()){
				this.redT.put(p, lives);
			}
		}
		//TELEPORTS
		ArrayList<LinkedHashMap<String, Object>> bluespawns = plugin.am.getBlueSpawns(arena);
		ArrayList<LinkedHashMap<String, Object>> redspawns = plugin.am.getRedSpawns(arena);
		ArrayList<LinkedHashMap<String, Object>> specspawns = plugin.am.getSpecSpawns(arena);

		//teleports+inv+messages:
		int spawn = 0;
		for(Player p : this.redT.keySet()) {
			//list
			players.add(p);
			//stats
			this.shots.put(p, 0);
			this.kills.put(p, 0);
			this.hits.put(p, 0);
			this.teamattacks.put(p, 0);
			this.deaths.put(p, 0);
			//teleport
			if(spawn > (redspawns.size()-1)) spawn = 0;
			p.teleport(plugin.transformLocation(redspawns.get(spawn)));
			spawn++;
			//inventory
			p.getInventory().setHelmet(Lobby.RED.helmet());
			if(plugin.balls > 0 ) p.getInventory().addItem(new ItemStack(Material.SNOW_BALL, plugin.balls));
			else if(plugin.balls == -1 ) p.getInventory().addItem(new ItemStack(Material.SNOW_BALL, 10));
			if(plugin.grenadeAmount > 0 ) p.getInventory().addItem(new ItemStack(Material.EGG, plugin.grenadeAmount));
			else if(plugin.grenadeAmount == -1 ) p.getInventory().addItem(new ItemStack(Material.EGG, 10));
			if(plugin.airstrikeAmount > 0 ) p.getInventory().addItem(new ItemStack(Material.STICK, plugin.airstrikeAmount));
			else if(plugin.airstrikeAmount == -1 ) p.getInventory().addItem(new ItemStack(Material.STICK, 10));
			//message
			p.sendMessage(plugin.aqua+"You are in Team "+Lobby.getTeam(getTeamName(p)).color()+getTeamName(p));
		}
		spawn = 0;
		for(Player p : this.blueT.keySet()) {
			//list
			players.add(p);
			//stats
			this.shots.put(p, 0);
			this.kills.put(p, 0);
			this.hits.put(p, 0);
			this.teamattacks.put(p, 0);
			this.deaths.put(p, 0);
			//teleport
			if(spawn > (bluespawns.size()-1)) spawn = 0;
			p.teleport(plugin.transformLocation(bluespawns.get(spawn)));
			spawn++;
			//inventory:
			p.getInventory().setHelmet(Lobby.BLUE.helmet());
			if(plugin.balls > 0 ) p.getInventory().addItem(new ItemStack(Material.SNOW_BALL, plugin.balls));
			else if(plugin.balls == -1 ) p.getInventory().addItem(new ItemStack(Material.SNOW_BALL, 10));
			if(plugin.grenadeAmount > 0 ) p.getInventory().addItem(new ItemStack(Material.EGG, plugin.grenadeAmount));
			else if(plugin.grenadeAmount == -1 ) p.getInventory().addItem(new ItemStack(Material.EGG, 10));
			if(plugin.airstrikeAmount > 0 ) p.getInventory().addItem(new ItemStack(Material.STICK, plugin.airstrikeAmount));
			else if(plugin.airstrikeAmount == -1 ) p.getInventory().addItem(new ItemStack(Material.STICK, 10));
			//message
			p.sendMessage(plugin.aqua+"You are in Team "+Lobby.getTeam(getTeamName(p)).color()+getTeamName(p));
		}
		spawn = 0;
		for(Player p : this.spec) {
			//list
			players.add(p);
			//teleport
			if(spawn > (specspawns.size()-1)) spawn = 0;
			p.teleport(plugin.transformLocation(specspawns.get(spawn)));
			spawn++;
			//inv
			p.getInventory().setHelmet(Lobby.SPECTATE.helmet());
			//message
			p.sendMessage(plugin.aqua+"You are "+Lobby.getTeam("spec").color()+"spectator!");
		}
		//colorchanges:
		changeAllColors();
		//TEST
		makeAllVisible();
		//TEST
		//lives + start!:
		if(lives == 1) plugin.nf.status("Everybody got " +plugin.green+1+ " life!"+plugin.light_purple+" And now FIGHT!" );
		else plugin.nf.status("Everybody got " +plugin.green+plugin.lives+ " lives!"+plugin.light_purple+" And now FIGHT!" );
	}
	
	//TESTING
	public void makeAllVisible() {
		for(Player pl : getAllPlayers()) {
			for(Player p : getAllPlayers()) {
				if(!p.equals(pl)) pl.showPlayer(p);
			}
		}
	}
	//TESTING
	public void changeAllColors() {
		for(Player p : redT.keySet()) {
			//chatnames
			String n = plugin.red+p.getName();
			if(n.length() > 16) n = (String) n.subSequence(0, n.length() - (n.length()-16));
			/*if(plugin.chatnames) {
				p.setDisplayName(n+white);
			}*/
			//listnames
			if(plugin.listnames) {
				p.setPlayerListName(n);
			}
		}
		for(Player p : blueT.keySet()) {
			//chatnames
			String n = plugin.blue+p.getName();
			if(n.length() > 16) n = (String) n.subSequence(0, n.length() - (n.length()-16));
			/*if(plugin.chatnames) {
				p.setDisplayName(n+white);
			}*/
			//listnames
			if(plugin.listnames) {
				p.setPlayerListName(n);
			}
		}
	}
	
	public void undoAllColors() {
		for(Player p : shots.keySet()) {
			/*if(plugin.chatnames) {
				p.setDisplayName(p.getName());
			}*/
			//listnames
			if(plugin.listnames) {
				if(!hasLeft(p)) p.setPlayerListName(null);
			}
		}
	}
	
	public int teamSizeRed() {
		return redT.size();
	}
	public int teamSizeBlue() {
		return blueT.size();
	}
	
	public String getArena() {
		return this.arena;
	}
	
	public int survivors(LinkedHashMap<Player, Integer> team) {
		int survivors = 0;
		for(Player p : team.keySet()) {
			if(team.get(p) > 0) {
				survivors++;
			}
		}
		return survivors;
	}
	public boolean hasLeft(Player player) {
		if(left.contains(player)) return true;
		else return false;
	}
	public boolean isSurvivor(Player player) {
		if(spec.contains(player)) return true;
		if(getTeam(player) != null) {
			if(getTeam(player).get(player) > 0) return true;
		}
		return false;
	}
	
	public String getTeamName(Player player) {
		if(redT.keySet().contains(player)) return "red";
		if(blueT.keySet().contains(player)) return "blue";
		return null;
	}
	public String getEnemyTeamName(Player player) {
		if(redT.keySet().contains(player)) return "blue";
		if(blueT.keySet().contains(player)) return "red";
		return null;
	}
	
	public boolean isSpec(Player player) {
		if(spec.contains(player)) return true;
		else return false;
	}
	public boolean isRed(Player player) {
		if(redT.keySet().contains(player)) return true;
		else return false;
	}
	public boolean isBlue(Player player) {
		if(blueT.keySet().contains(player)) return true;
		else return false;
	}
	
	public LinkedHashMap<Player, Integer> getTeam(Player player) {
		if(redT.keySet().contains(player)) return redT;
		if(blueT.keySet().contains(player)) return blueT;
		return null;
	}
	public LinkedHashMap<Player, Integer> getEnemyTeam(Player player) {
		if(redT.keySet().contains(player)) return blueT;
		if(blueT.keySet().contains(player)) return redT;
		return null;
	}
	
	public boolean inMatch(Player player) {
		if(redT.keySet().contains(player)) return true;
		if(blueT.keySet().contains(player)) return true;
		if(spec.contains(player)) return true;
		return false;
	}
	
	public boolean enemys(Player player1, Player player2) {
		if(redT.keySet().contains(player1) && blueT.keySet().contains(player2)) return true;
		if(redT.keySet().contains(player2) && blueT.keySet().contains(player1)) return true;
		return false;
	}
	
	public boolean friendly(Player player1, Player player2) {
		if(redT.keySet().contains(player1) && redT.keySet().contains(player2)) return true;
		if(blueT.keySet().contains(player1) && blueT.keySet().contains(player2)) return true;
		return false;
	}
	
	public void left(Player player) {
		//left
		left.add(player);
		//listname
		if(plugin.listnames) player.setPlayerListName(null);
		//team?
		if(getTeam(player) != null) {
			//0 leben aka tot
			getTeam(player).put(player, 0);
			//survivors?->endGame
			if(survivors(getTeam(player)) == 0) {
				matchOver = true;
				//unhideAll();
				undoAllColors();
				plugin.mm.gameEnd(this, getEnemyTeam(player).keySet(), getEnemyTeamName(player), getTeam(player).keySet(), getTeamName(player), spec, shots, hits, deaths, kills, teamattacks);
			}
		} else if(spec.contains(player)) spec.remove(player);
	}
	
	public ArrayList<Player> getAllPlayers() {
		//return players;
		ArrayList<Player> list = new ArrayList<Player>();
		Set<Player> players = shots.keySet();
		for(Player p : players) {
			list.add(p);
		}
		for(Player p : spec) {
			list.add(p);
		}
		return list;
	}
	//AKTIONS
	
	public void shot(Player player) {
		//add 1
		shots.put(player, (shots.get(player)+1));
	}
	
	public void hitSnow(Player target, Player shooter) {
		//math over already?
		if(matchOver) return;
		//target already dead?
		if(getTeam(target).get(target) <= 0) return;
		//Teams?
		if(enemys(target, shooter)) {
			//player not dead already?
			if(getTeam(target).get(target) > 0) {
				//-1 live
				getTeam(target).put(target, (getTeam(target).get(target) -1));
				//stats
				hits.put(shooter, (hits.get(shooter)+1));
				//dead?->frag
				//message:
				if(getTeam(target).get(target) <= 0) {
					frag(target, shooter);
				} else {
					shooter.sendMessage(plugin.green+"You hit!");
					target.sendMessage(plugin.red+"You were hit!");
				}
			}
		} else if(friendly(target, shooter)) {
			//message
			//-points
			teamattacks.put(shooter, (teamattacks.get(shooter)+1));
			shooter.sendMessage(plugin.red+"Teamattack! "+plugin.pointsPerTeamattack + " Points!");
		}
	}
	
	public void frag(final Player target, final Player killer) {
		//teleport lobby:
		final Match this2 = this;
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			
			@Override
			public void run() {
				target.teleport(plugin.transformLocation(plugin.getLobbySpawns().get(0)));
				plugin.clearInv(target);
				//feed
				killer.sendMessage(plugin.green+"You killed "+target.getName()+" ! +"+plugin.pointsPerKill + " Points and +"+plugin.cashPerKill+" Cash!");
				target.sendMessage(plugin.red+"You were killed by "+killer.getName()+" !");
				plugin.nf.feed(target, killer, this2);
				//points+cash+kill+death
				deaths.put(target, (deaths.get(target)+1));
				kills.put(killer, (kills.get(killer)+1));
				//survivors?->endGame
				if(survivors(getTeam(target)) == 0) {
					//unhideAll();
					matchOver = true;
					undoAllColors();
					plugin.mm.gameEnd(this2, getTeam(killer).keySet(), getTeamName(killer), getTeam(target).keySet(), getTeamName(target), spec, shots, hits, deaths, kills, teamattacks);
				}
				
			}
		}, 1L);

	}
	
	
	
}
