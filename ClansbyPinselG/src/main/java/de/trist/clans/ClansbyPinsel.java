package de.trist.clans;

import de.trist.clans.command.ClanCommand;
import de.trist.clans.gui.MenuListener;
import de.trist.clans.hooks.VaultHook;
import de.trist.clans.service.ClanManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class ClansbyPinsel extends JavaPlugin {

    private ClanManager clanManager;
    private VaultHook vaultHook;
    private BukkitTask autosaveTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        int defaultSlots = getConfig().getInt("clan.default-member-slots", 5);

        clanManager = new ClanManager(this);

        // ===== Vault Hook =====
        vaultHook = new VaultHook();
        if (vaultHook.setup()) {
            getLogger().info("Vault Economy Hook aktiv.");
        } else {
            getLogger().warning("Vault Economy NICHT gefunden! Slot-Kauf deaktiviert.");
        }

        if (getCommand("clan") != null) {
            getCommand("clan").setExecutor(new ClanCommand(clanManager, defaultSlots, vaultHook));
        } else {
            getLogger().severe("FEHLER: Command 'clan' ist NULL! Prüfe plugin.yml -> commands: clan");
        }

        getServer().getPluginManager().registerEvents(new MenuListener(clanManager), this);

        // ===== PlaceholderAPI Hook =====
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new de.trist.clans.hooks.ClanPlaceholders(clanManager).register();
            getLogger().info("Clan PlaceholderAPI Hook aktiviert.");
        } else {
            getLogger().info("PlaceholderAPI nicht gefunden.");
        }

        // ===== Autosave (alle 5 Minuten) =====
        autosaveTask = getServer().getScheduler().runTaskTimer(this, () -> {
            try {
                clanManager.save();
                getLogger().info("Autosave: clans.yml gespeichert.");
            } catch (Exception ex) {
                getLogger().warning("Autosave Fehler: " + ex.getMessage());
            }
        }, 20L * 60L * 5L, 20L * 60L * 5L);

        getLogger().info("ClansbyPinsel aktiviert!");
    }

    @Override
    public void onDisable() {
        if (autosaveTask != null) autosaveTask.cancel();

        if (clanManager != null) {
            clanManager.save();
        }
        getLogger().info("ClansbyPinsel deaktiviert!");
    }
}
