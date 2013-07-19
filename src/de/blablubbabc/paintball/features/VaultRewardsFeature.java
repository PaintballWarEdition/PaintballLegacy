package de.blablubbabc.paintball.features;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import de.blablubbabc.paintball.utils.Log;

public class VaultRewardsFeature {

	private Economy economy = null;
	
	public VaultRewardsFeature(Plugin vaultPlugin) {
		RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp != null) {
			economy = rsp.getProvider();
		}
	}
	
	public boolean isEconomyDetected() {
		return economy != null;
	}
	
	// return true if successfull
	public boolean givePlayerMoney(String playerName, double moneyToAdd) {
		if (economy != null && playerName != null && moneyToAdd > 0) {
			EconomyResponse response = economy.depositPlayer(playerName, moneyToAdd);
			if (response.transactionSuccess()) {
				return true;
			} else {
				Log.warning("", false);
				return false;
			}
		} else {
			return false;
		}
	}
}
