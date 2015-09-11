package de.blablubbabc.paintball.features;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import de.blablubbabc.paintball.utils.Log;

public class VaultRewardsFeature {

	private Economy economy = null;
	//private Map<String, Double> sessions = new HashMap<String, Double>();
	
	public VaultRewardsFeature(Plugin vaultPlugin) {
		RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp != null) {
			economy = rsp.getProvider();
		}
	}
	
	public boolean isEconomyDetected() {
		return economy != null;
	}
	
	// returns current money during this session:
	/*public double givePlayerMoneyAfterSession(String playerName, double moneyToAdd) {
		if (economy == null || playerName == null) return 0.0;
		
		Double current = sessions.get(playerName);
		if (current == null) current = 0.0;
		if (moneyToAdd > 0) current += moneyToAdd;
		
		sessions.put(playerName, current);
		return current;
	}*/
	
	/*public double getSessionMoney(String playerName) {
		Double current = sessions.get(playerName);
		return current != null ? current : 0.0;
	}
	
	public boolean transferCurrentSession(String playerName) {
		Double current = sessions.remove(playerName);
		return current != null && givePlayerMoneyInstant(playerName, current);
	}*/
	
	// return true if successfull
	public boolean givePlayerMoneyInstant(String playerName, double moneyToAdd) {
		if (economy != null && playerName != null && moneyToAdd > 0) {
			EconomyResponse response = economy.depositPlayer(playerName, moneyToAdd);
			if (response.transactionSuccess()) {
				return true;
			} else {
				Log.warning("Depositing money to '" + playerName + "'s account via vault wasn't successfully: " + response.errorMessage);
				return false;
			}
		} else {
			return false;
		}
	}
}
