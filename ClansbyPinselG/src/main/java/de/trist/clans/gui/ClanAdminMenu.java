package de.trist.clans.gui;

import de.trist.clans.service.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ClanAdminMenu {

    public static final String TITLE = "§cClan Admin";

    public static void open(Player viewer, ClanManager clanManager) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        // Filler
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fm = filler.getItemMeta();
        if (fm != null) {
            fm.setDisplayName(" ");
            filler.setItemMeta(fm);
        }
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);

        // Reload
        inv.setItem(11, named(Material.REDSTONE, "§cReload Clans", "§7Lädt clans.yml neu."));

        // Save
        inv.setItem(13, named(Material.WRITABLE_BOOK, "§aSave Clans", "§7Speichert clans.yml."));

        // Back
        inv.setItem(15, named(Material.ARROW, "§7Zurück", "§7Zurück ins Clan-Menü."));

        viewer.openInventory(inv);
    }

    private static ItemStack named(Material mat, String name, String lore1) {
        ItemStack it = new ItemStack(mat);
        ItemMeta im = it.getItemMeta();
        if (im != null) {
            im.setDisplayName(name);
            im.setLore(java.util.List.of(lore1));
            it.setItemMeta(im);
        }
        return it;
    }
}