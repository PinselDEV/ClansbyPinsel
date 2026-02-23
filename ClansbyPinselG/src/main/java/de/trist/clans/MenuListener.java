package de.trist.clans.gui;

import de.trist.clans.model.Clan;
import de.trist.clans.service.ClanManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class MenuListener implements Listener {

    private final ClanManager clanManager;

    public MenuListener(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        String title = e.getView().getTitle();
        if (title == null) return;

        // ===== Clan Menü =====
        if (title.equals(ClanMenu.TITLE)) {
            e.setCancelled(true);

            ItemStack current = e.getCurrentItem();
            if (current == null) return;

            Material type = current.getType();

            if (type == Material.BOOK) {
                player.closeInventory();
                player.performCommand("clan info");
                return;
            }

            if (type == Material.BARRIER) {
                player.closeInventory();
                player.performCommand("clan leave");
                return;
            }

            if (type == Material.PLAYER_HEAD) {
                player.closeInventory();
                MembersMenu.open(player, clanManager);
                return;
            }

            if (type == Material.PAPER) {
                InviteMenu.open(player, clanManager);
                return;
            }

            // Optional: Admin-Menü Button im ClanMenu (nur wenn du dort eins setzt)
            if (type == Material.REDSTONE) {
                if (!player.hasPermission("clan.admin")) {
                    player.sendMessage("§8[§bClan§8] §cDazu hast du keine Rechte.");
                    return;
                }
                player.closeInventory();
                ClanAdminMenu.open(player, clanManager);
                return;
            }

            return;
        }

        // ===== Members Menü =====
        if (title.equals(MembersMenu.TITLE)) {
            e.setCancelled(true);

            ItemStack clicked = e.getCurrentItem();
            if (clicked == null) return;

            Material type = clicked.getType();

            // Zurück
            if (type == Material.ARROW) {
                player.closeInventory();
                ClanMenu.open(player);
                return;
            }

            // Klick auf Member-Kopf => Kick (nur Owner)
            if (type == Material.PLAYER_HEAD) {
                var meta = clicked.getItemMeta();
                if (meta == null) return;

                List<String> lore = meta.getLore();
                if (lore == null) return;

                UUID target = null;
                for (String line : lore) {
                    if (line.startsWith("§8UUID: ")) {
                        try {
                            target = UUID.fromString(line.substring("§8UUID: ".length()));
                        } catch (IllegalArgumentException ignored) {}
                        break;
                    }
                }

                if (target == null) {
                    player.sendMessage("§8[§bClan§8] §cKonnte Member nicht erkennen.");
                    return;
                }

                String tag = clanManager.getClanTagOf(player.getUniqueId());
                if (tag == null) {
                    player.sendMessage("§8[§bClan§8] §cDu bist in keinem Clan.");
                    player.closeInventory();
                    return;
                }

                Clan clan = clanManager.getClanByTag(tag);
                if (clan == null) {
                    player.sendMessage("§8[§bClan§8] §cClan nicht gefunden.");
                    player.closeInventory();
                    return;
                }

                if (clan.getOwner() == null || !clan.getOwner().equals(player.getUniqueId())) {
                    player.sendMessage("§8[§bClan§8] §cNur der Owner kann Member kicken.");
                    return;
                }

                if (clan.getOwner().equals(target)) {
                    player.sendMessage("§8[§bClan§8] §cDu kannst den Owner nicht kicken.");
                    return;
                }

                if (player.getUniqueId().equals(target)) {
                    player.sendMessage("§8[§bClan§8] §cDu kannst dich nicht selbst kicken.");
                    return;
                }

                if (!clan.getMembers().contains(target)) {
                    player.sendMessage("§8[§bClan§8] §cDieser Spieler ist nicht (mehr) im Clan.");
                    MembersMenu.open(player, clanManager);
                    return;
                }

                clanManager.removeMember(target);
                player.sendMessage("§8[§bClan§8] §aMember wurde gekickt.");
                MembersMenu.open(player, clanManager);
                return;
            }

            return;
        }

        // ===== Invite Menü =====
        if (title.equals(InviteMenu.TITLE)) {
            e.setCancelled(true);

            ItemStack clicked = e.getCurrentItem();
            if (clicked == null) return;

            if (clicked.getType() == Material.ARROW) {
                player.closeInventory();
                ClanMenu.open(player);
                return;
            }

            if (clicked.getType() != Material.PLAYER_HEAD) return;

            var meta = clicked.getItemMeta();
            if (meta == null) return;

            List<String> lore = meta.getLore();
            if (lore == null) return;

            String name = null;
            for (String line : lore) {
                if (line.startsWith("§8NAME: ")) {
                    name = line.substring("§8NAME: ".length());
                    break;
                }
            }

            if (name == null || name.isEmpty()) return;

            player.closeInventory();
            player.performCommand("clan invite " + name);
            return;
        }

        // ===== Clan Admin Menü =====
        if (title.equals(ClanAdminMenu.TITLE)) {
            e.setCancelled(true);

            ItemStack clicked = e.getCurrentItem();
            if (clicked == null) return;

            Material type = clicked.getType();

            if (type == Material.ARROW) {
                player.closeInventory();
                ClanMenu.open(player);
                return;
            }

            if (!player.hasPermission("clan.admin")) {
                player.sendMessage("§8[§bClan§8] §cDazu hast du keine Rechte.");
                player.closeInventory();
                return;
            }

            if (type == Material.REDSTONE) {
                clanManager.reload();
                player.sendMessage("§8[§bClan§8] §aClans neu geladen.");
                player.closeInventory();
                return;
            }

            if (type == Material.WRITABLE_BOOK) {
                clanManager.save();
                player.sendMessage("§8[§bClan§8] §aClans gespeichert.");
                player.closeInventory();
                return;
            }

            return;
        }
    }
}