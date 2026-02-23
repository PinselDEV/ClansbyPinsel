package de.trist.clans.gui;

import de.trist.clans.model.Clan;
import de.trist.clans.service.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MembersMenu {

    public static final String TITLE = "§bClan Mitglieder";

    public static void open(Player viewer, ClanManager clanManager) {
        String tag = clanManager.getClanTagOf(viewer.getUniqueId());
        if (tag == null) {
            viewer.sendMessage("§8[§bClan§8] §cDu bist in keinem Clan.");
            return;
        }

        Clan clan = clanManager.getClanByTag(tag);
        if (clan == null) {
            viewer.sendMessage("§8[§bClan§8] §cClan nicht gefunden.");
            return;
        }

        int size = 54;
        Inventory inv = Bukkit.createInventory(null, size, TITLE);

        // ===== Filler =====
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        boolean isOwner = clan.getOwner() != null && clan.getOwner().equals(viewer.getUniqueId());

        // ===== Member-Köpfe =====
        int slot = 0;
        for (UUID uuid : clan.getMembers()) {
            if (slot >= 45) break; // oben 5 Reihen für Member

            OfflinePlayer off = Bukkit.getOfflinePlayer(uuid);

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta == null) continue;

            meta.setOwningPlayer(off);

            String name = (off.getName() != null) ? off.getName() : uuid.toString();
            boolean memberIsOwner = clan.getOwner() != null && uuid.equals(clan.getOwner());

            meta.setDisplayName((memberIsOwner ? "§6★ " : "§a") + name);

            List<String> lore = new ArrayList<>();
            lore.add("§7Rolle: " + (memberIsOwner ? "§6Owner" : "§fMember"));

            lore.add(" "); // spacer
            if (isOwner && !memberIsOwner) {
                lore.add("§cKlick zum Kicken");
            } else if (memberIsOwner) {
                lore.add("§7Das ist der Owner.");
            } else {
                lore.add("§7Keine Aktion verfügbar.");
            }

            // Wichtig: UUID speichern, damit wir im Listener wissen, wer geklickt wurde
            lore.add("§8UUID: " + uuid);

            meta.setLore(lore);
            head.setItemMeta(meta);

            inv.setItem(slot, head);
            slot++;
        }

        // ===== Zurück-Button =====
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