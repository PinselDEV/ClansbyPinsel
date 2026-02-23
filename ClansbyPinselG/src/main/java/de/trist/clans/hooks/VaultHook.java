package de.trist.clans.hooks;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {

    private Economy economy;

    public boolean setup() {
        RegisteredServiceProvider<Economy> rsp =
                Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public boolean isReady() {
        return economy != null;
    }

    public Economy eco() {
        return economy;
    }
}