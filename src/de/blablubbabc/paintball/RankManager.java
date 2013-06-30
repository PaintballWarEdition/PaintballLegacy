package de.blablubbabc.paintball;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import de.blablubbabc.paintball.statistics.player.PlayerStat;
import de.blablubbabc.paintball.statistics.player.PlayerStats;
import de.blablubbabc.paintball.utils.Log;
import de.blablubbabc.paintball.utils.Utils;

public class RankManager {
	
	private List<Rank> ranks;
	
	public RankManager(File file) {
		// LOAD RANKS
		Log.info("Loading ranks..");
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		ConfigurationSection ranksSection = config.getConfigurationSection("Ranks");
		if (ranksSection == null || ranksSection.getKeys(false).size() == 0) {
			// DEFAULT RANKS
			initDefaultRanks();
		} else {
			ranks = new ArrayList<Rank>();
			// READ RANKS:
			for (String name : ranksSection.getKeys(false)) {
				ConfigurationSection rankSection = ranksSection.getConfigurationSection(name);
				if (rankSection == null) {
					Log.warning("Couldn't read rank section for rank: " + name, true);
					continue;
				}
				
				// NEEDED POINTS
				if (!rankSection.contains("Needed Points")) {
					Log.warning("'Needed Points' value missing for rank: " + name, true);
					continue;
				}
				int neededPoints = rankSection.getInt("Needed Points");
				
				// PREFIX
				String prefix = rankSection.getString("Prefix");
				if (prefix != null) ChatColor.translateAlternateColorCodes('&', prefix);
				
				// ARMOR
				ItemStack helmet = itemFromConfig(rankSection.getConfigurationSection("Helmet"));
				ItemStack chestplate = itemFromConfig(rankSection.getConfigurationSection("Chestplate"));
				ItemStack leggings = itemFromConfig(rankSection.getConfigurationSection("Leggings"));
				ItemStack boots = itemFromConfig(rankSection.getConfigurationSection("Boots"));
				
				// ADD RANK:
				Rank rank = new Rank(name, neededPoints, prefix, helmet, chestplate, leggings, boots);
				ranks.add(rank);
			}
			
			// NO VALID RANKS?
			if (ranks.size() == 0) initDefaultRanks();
		}
		
		// SORT RANK LIST:
		Collections.sort(ranks);
		// SET RANK INDEXES:
		for (int i = 0; i < ranks.size(); i++) {
			ranks.get(i).setRankIndex(i);
		}
		
		// WRITE RANKS BACK TO CONFIG:
		// reset ranks section first:
		config.set("Ranks", null);
		for (Rank rank : ranks) {
			String node = "Ranks." + rank.getName();
			config.set(node + ".Needed Points", rank.getNeededPoints());
			config.set(node + ".Prefix", rank.getPrefix());
			
			itemToConfig(config.createSection(node + ".Helmet"), rank.getHelmet());
			itemToConfig(config.createSection(node + ".Chestplate"), rank.getChestplate());
			itemToConfig(config.createSection(node + ".Leggings"), rank.getLeggings());
			itemToConfig(config.createSection(node + ".Boots"), rank.getBoots());
		}
		
		
		// SAVE FILE:
		try {
			config.save(file);
		} catch (IOException exception) {
			Log.severe("Unable to write to the rank configuration file at \"" + file.getPath() + "\"", true);
		}
		
		
	}
	
	private ItemStack itemFromConfig(ConfigurationSection section) {
		if (section == null) return null;
		
		String type = section.getString("Type");
		if (type == null) return null;
		Material material = Material.matchMaterial(type);
		if (material == null) return null;
		ItemStack item = new ItemStack(material);
		
		ConfigurationSection colorSection = section.getConfigurationSection("Color");
		if (colorSection != null) {
			int red = Math.max(0, Math.min(255, colorSection.getInt("Red", 0)));
			int green = Math.max(0, Math.min(255, colorSection.getInt("Green", 0)));
			int blue = Math.max(0, Math.min(255, colorSection.getInt("Blue", 0)));
			Color color = Color.fromRGB(red, green, blue);
			
			Utils.setLeatherArmorColor(item, color);
		}
		
		return item;
	}
	
	private ConfigurationSection itemToConfig(ConfigurationSection section, ItemStack item) {
		if (section == null || item == null) return null;
		
		Material type = item.getType();
		section.set("Type", type.name());
		if (type == Material.LEATHER_CHESTPLATE || type == Material.LEATHER_LEGGINGS || type == Material.LEATHER_BOOTS || type == Material.LEATHER_HELMET) {
			ConfigurationSection colorSection = section.createSection("Color");
			LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
			Color color = meta.getColor();
			colorSection.set("Red", color.getRed());
			colorSection.set("Green", color.getGreen());
			colorSection.set("Blue", color.getBlue());
		}
		
		return section;
		
	}
	
	private void initDefaultRanks() {
		Log.info("Initialize default ranks..");
		ranks = new ArrayList<Rank>();
		
		ranks.add(new Rank("Recruit", 0, ChatColor.GOLD + "[" + ChatColor.WHITE + "Recruit" + ChatColor.GOLD + "] " + ChatColor.RESET, null, Utils.setLeatherArmorColor(new ItemStack(Material.LEATHER_CHESTPLATE), Color.WHITE), null, null));
		ranks.add(new Rank("Private", 25, ChatColor.GOLD + "[" + ChatColor.GRAY + "Private" + ChatColor.GOLD + "] " + ChatColor.RESET, null, Utils.setLeatherArmorColor(new ItemStack(Material.LEATHER_CHESTPLATE), Color.SILVER), null, null));
		ranks.add(new Rank("Corporal", 100, ChatColor.GOLD + "[" + ChatColor.GRAY + "Corporal" + ChatColor.GOLD + "] " + ChatColor.RESET, null, new ItemStack(Material.LEATHER_CHESTPLATE), null, null));
		ranks.add(new Rank("Sergeant", 250, ChatColor.GOLD + "[" + ChatColor.YELLOW + "Sergeant" + ChatColor.GOLD + "] " + ChatColor.RESET, null, Utils.setLeatherArmorColor(new ItemStack(Material.LEATHER_CHESTPLATE), Color.YELLOW), null, null));
		ranks.add(new Rank("First Sergeant", 500, ChatColor.GOLD + "[" + ChatColor.LIGHT_PURPLE + "First Sergeant" + ChatColor.GOLD + "] " + ChatColor.RESET, null, Utils.setLeatherArmorColor(new ItemStack(Material.LEATHER_CHESTPLATE), Color.ORANGE), null, null));
		ranks.add(new Rank("Sergeant Major", 750, ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "Sergeant Major" + ChatColor.GOLD + "] " + ChatColor.RESET, null, Utils.setLeatherArmorColor(new ItemStack(Material.LEATHER_CHESTPLATE), Color.PURPLE), null, null));
		ranks.add(new Rank("Lieutenant", 1000, ChatColor.GOLD + "[" + ChatColor.GREEN + "Lieutenant" + ChatColor.GOLD + "] " + ChatColor.RESET, null, Utils.setLeatherArmorColor(new ItemStack(Material.LEATHER_CHESTPLATE), Color.GREEN), null, null));
		ranks.add(new Rank("Captain", 5000, ChatColor.GOLD + "[" + ChatColor.DARK_GREEN + "Captain" + ChatColor.GOLD + "] " + ChatColor.RESET, null, Utils.setLeatherArmorColor(new ItemStack(Material.LEATHER_CHESTPLATE), Color.TEAL), null, null));
		ranks.add(new Rank("Major", 10000, ChatColor.GOLD + "[" + ChatColor.DARK_AQUA + "Major" + ChatColor.GOLD + "] " + ChatColor.RESET, null, Utils.setLeatherArmorColor(new ItemStack(Material.LEATHER_CHESTPLATE), Color.BLACK), null, null));
		ranks.add(new Rank("Colonel", 15000, ChatColor.GOLD + "[" + ChatColor.BLACK + "Colonel" + ChatColor.GOLD + "] " + ChatColor.RESET, null, new ItemStack(Material.IRON_CHESTPLATE), null, null));
		ranks.add(new Rank("General", 25000, ChatColor.GOLD + "[" + ChatColor.RED + "General" + ChatColor.GOLD + "] " + ChatColor.RESET, null, new ItemStack(Material.GOLD_CHESTPLATE), null, null));
		ranks.add(new Rank("Commander", 35000, ChatColor.GOLD + "[" + ChatColor.DARK_RED + "Commander" + ChatColor.GOLD + "] " + ChatColor.RESET, null, new ItemStack(Material.DIAMOND_CHESTPLATE), null, null));
		ranks.add(new Rank("Master Chief", 50000, ChatColor.GOLD + "[" + ChatColor.DARK_RED + "Master Chief" + ChatColor.GOLD + "] " + ChatColor.RESET, null, 
				Utils.setLeatherArmorColor(new ItemStack(Material.LEATHER_CHESTPLATE), DyeColor.GREEN.getColor()), 
				Utils.setLeatherArmorColor(new ItemStack(Material.LEATHER_LEGGINGS), DyeColor.LIME.getColor()), 
				Utils.setLeatherArmorColor(new ItemStack(Material.LEATHER_BOOTS), DyeColor.GREEN.getColor())));
	}
	
	public Rank getRank(String playerName) {
		// init with lowest rank:
		Rank highest = ranks.get(0);
		
		PlayerStats stats = Paintball.instance.playerManager.getPlayerStats(playerName);
		// stats even exist for this player ?
		if (stats != null) {
			int points = stats.getStat(PlayerStat.POINTS);
			//get highest rank:
			for (Rank rank : ranks) {
				int needed = rank.getNeededPoints();
				if (needed <= points) {
					if (highest == null || needed > highest.getNeededPoints()) {
						highest = rank;
					}
				}
			}
		}

		// return highest found rank, or lowest possible rank:
		return highest;
	}
	
	public Rank getRankByName(String rankName) {
		for (Rank rank : ranks) {
			if (rank.getName().equals(rankName)) return rank;
		}
		return null;
	}
	
	public Rank getRankByIndex(int index) {
		index = Math.max(0, Math.min(index, ranks.size() - 1));
		return ranks.get(index);
	}
	
}
