package de.trist.clans.gui;

import de.trist.clans.service.ClanManager;
import de.trist.clans.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class InviteMenu {

    public static final String TITLE = "§bClan Einladen";

    public static void open(Player viewer, ClanManager clanManager) {
        String myTag = clanManager.getClanTagOf(viewer.getUniqueId());
        if (myTag == null) {
            Msg.send(viewer, "§cDu bist in keinem Clan.");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fm = filler.getItemMeta();
        if (fm != null) {
            fm.setDisplayName(" ");
            filler.setItemMeta(fm);
        }
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);

        int slot = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (slot >= 45) break;
            if (p.getUniqueId().equals(viewer.getUniqueId())) continue;
            if (clanManager.isInClan(p.getUniqueId())) continue;

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta == null) continue;

            meta.setOwningPlayer(p);
            meta.setDisplayName("§a" + p.getName());

            List<String> lore = new ArrayList<>();
            lore.add("§7Klick zum Einladen");
            lore.add("§8NAME: " + p.getName());
            meta.setLore(lore);

            head.setItemMeta(meta);

            inv.setItem(slot, head);
            slot++;
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bm = back.getItemMeta();
        if (bm != null) {
            bm.setDisplayName("§7Zurück");
            back.setItemMeta(bm);
        }
        inv.setItem(49, back);

        viewer.openInventory(inv);
    }
}