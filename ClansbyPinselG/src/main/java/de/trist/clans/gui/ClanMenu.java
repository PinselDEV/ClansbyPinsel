package de.trist.clans.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ClanMenu {

    public static final String TITLE = "§bClan Menü";

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        ItemStack filler = item(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);

        inv.setItem(11, item(Material.BOOK, "§aClan Info"));
        inv.setItem(13, item(Material.PLAYER_HEAD, "§aMitglieder"));
        inv.setItem(15, item(Material.PAPER, "§aEinladen"));
        inv.setItem(22, item(Material.BARRIER, "§cClan verlassen"));

        player.openInventory(inv);
    }

    private static ItemStack item(Material mat, String name) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            it.setItemMeta(meta);
        }
        return it;
    }
}