package de.trist.clans.command;

import de.trist.clans.gui.ClanAdminMenu;
import de.trist.clans.gui.ClanMenu;
import de.trist.clans.hooks.VaultHook;
import de.trist.clans.model.Clan;
import de.trist.clans.service.ClanManager;
import de.trist.clans.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class ClanCommand implements CommandExecutor {

    private final ClanManager clanManager;
    private final VaultHook vaultHook;
    private final int defaultSlots;

    private final Map<UUID, Invite> invites = new HashMap<>();

    private static class Invite {
        String clanTag;
        long expiresAt;
    }

    public ClanCommand(ClanManager clanManager, int defaultSlots, VaultHook vaultHook) {
        this.clanManager = clanManager;
        this.defaultSlots = defaultSlots;
        this.vaultHook = vaultHook;
    }

    private void sendHelp(Player p) {
        Msg.sendRaw(p, "§8§m------------------------");
        Msg.send(p, "§fBefehle:");
        Msg.sendRaw(p, "§7/clan create <TAG> §8- §fClan erstellen");
        Msg.sendRaw(p, "§7/clan invite <Spieler> §8- §fSpieler einladen");
        Msg.sendRaw(p, "§7/clan accept §8- §fEinladung annehmen");
        Msg.sendRaw(p, "§7/clan info §8- §fClan Info");
        Msg.sendRaw(p, "§7/clan leave §8- §fClan verlassen");
        Msg.sendRaw(p, "§7/clan delete §8- §fClan löschen (Owner)");
        Msg.sendRaw(p, "§7/clan kick <Spieler> §8- §fMember kicken (Owner)");
        Msg.sendRaw(p, "§7/clan slot buy §8- §f1 Slot kaufen (2000€)");
        if (p.hasPermission("clan.admin")) {
            Msg.sendRaw(p, "§c/clan admin §8- §fAdmin Menü");
            Msg.sendRaw(p, "§c/clan admin delete <TAG> confirm §8- §fClan löschen (Admin)");
        }
        Msg.sendRaw(p, "§8§m------------------------");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            Msg.send(sender, "Nur Spieler können das nutzen.");
            return true;
        }

        if (args.length == 0) {
            ClanMenu.open(player);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("help")) {
            sendHelp(player);
            return true;
        }

        // ===== ADMIN =====
        if (sub.equals("admin")) {
            if (!player.hasPermission("clan.admin")) {
                Msg.send(player, "§cDazu hast du keine Rechte.");
                return true;
            }

            // /clan admin  -> Menü öffnen
            if (args.length == 1) {
                ClanAdminMenu.open(player, clanManager);
                return true;
            }

            // /clan admin delete <TAG> confirm
            if (args.length >= 3 && args[1].equalsIgnoreCase("delete")) {
                String tag = args[2].toUpperCase(Locale.ROOT);

                if (!clanManager.clanExists(tag)) {
                    Msg.send(player, "§cClan §e" + tag + " §cgibt es nicht.");
                    return true;
                }

                if (args.length < 4 || !args[3].equalsIgnoreCase("confirm")) {
                    Msg.send(player, "§cAchtung! Das löscht den Clan komplett.");
                    Msg.sendRaw(player, "§7Bestätige mit: §e/clan admin delete " + tag + " confirm");
                    return true;
                }

                clanManager.deleteClan(tag);
                Msg.send(player, "§aClan §e" + tag + " §awurde gelöscht.");
                return true;
            }

            Msg.send(player, "§cNutze: §e/clan admin §7oder §e/clan admin delete <TAG> confirm");
            return true;
        }

        // ===== SLOT BUY =====
        if (sub.equals("slot") && args.length >= 2 && args[1].equalsIgnoreCase("buy")) {

            String myTag = clanManager.getClanTagOf(player.getUniqueId());
            if (myTag == null) {
                Msg.send(player, "§cDu bist in keinem Clan.");
                return true;
            }

            Clan clan = clanManager.getClanByTag(myTag);
            if (clan == null) {
                Msg.send(player, "§cFehler: Clan nicht gefunden.");
                return true;
            }

            if (!clan.getOwner().equals(player.getUniqueId())) {
                Msg.send(player, "§cNur der Clan-Owner kann Slots kaufen.");
                return true;
            }

            int price = 2000;

            if (vaultHook == null || !vaultHook.isReady()) {
                Msg.send(player, "§cEconomy nicht verfügbar (Vault/Essentials?).");
                return true;
            }

            double bal = vaultHook.eco().getBalance(player);
            if (bal < price) {
                Msg.send(player, "§cDu brauchst §e" + price + "€ §cfür einen Slot.");
                return true;
            }

            var res = vaultHook.eco().withdrawPlayer(player, price);
            if (!res.transactionSuccess()) {
                Msg.send(player, "§cZahlung fehlgeschlagen.");
                return true;
            }

            clan.setMemberSlots(clan.getMemberSlots() + 1);
            clanManager.save();

            Msg.send(player, "§aDu hast 1 Slot gekauft. §7Neue Slots: §e" + clan.getMemberSlots());
            return true;
        }

        // ===== CREATE =====
        if (sub.equals("create")) {
            if (args.length < 2) {
                Msg.send(player, "Nutze: §f/clan create <TAG>");
                return true;
            }

            String tag = args[1].toUpperCase(Locale.ROOT);

            if (tag.length() < 2 || tag.length() > 6) {
                Msg.send(player, "§cTAG muss 2-6 Zeichen lang sein.");
                return true;
            }

            if (!tag.matches("[A-Z0-9]+")) {
                Msg.send(player, "§cTAG darf nur A-Z und 0-9 enthalten.");
                return true;
            }

            if (clanManager.isInClan(player.getUniqueId())) {
                Msg.send(player, "§cDu bist bereits in einem Clan.");
                return true;
            }

            if (clanManager.clanExists(tag)) {
                Msg.send(player, "§cDiesen Clan gibt es schon.");
                return true;
            }

            clanManager.createClan(tag, player.getUniqueId(), defaultSlots);
            Msg.send(player, "§aClan §e" + tag + " §aerstellt! §7Slots: §e" + defaultSlots);
            return true;
        }

        // ===== INVITE =====
        if (sub.equals("invite")) {
            if (args.length < 2) {
                Msg.send(player, "Nutze: §f/clan invite <Spieler>");
                return true;
            }

            String myTag = clanManager.getClanTagOf(player.getUniqueId());
            if (myTag == null) {
                Msg.send(player, "§cDu bist in keinem Clan.");
                return true;
            }

            int members = clanManager.getMemberCount(myTag);
            int slots = clanManager.getSlots(myTag);
            if (members >= slots) {
                Msg.send(player, "§cDein Clan ist voll. §7Slots: §e" + slots);
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                Msg.send(player, "§cSpieler nicht online.");
                return true;
            }

            if (target.getUniqueId().equals(player.getUniqueId())) {
                Msg.send(player, "§cDu kannst dich nicht selbst einladen.");
                return true;
            }

            if (clanManager.isInClan(target.getUniqueId())) {
                Msg.send(player, "§cDer Spieler ist bereits in einem Clan.");
                return true;
            }

            Invite inv = new Invite();
            inv.clanTag = myTag;
            inv.expiresAt = System.currentTimeMillis() + 60_000;
            invites.put(target.getUniqueId(), inv);

            Msg.send(player, "§aEinladung an §e" + target.getName() + " §agesendet.");
            Msg.send(target, "§aDu wurdest in Clan §e" + myTag + " §aeingeladen!");
            Msg.sendRaw(target, "§7Nutze §e/clan accept §7(60 Sekunden Zeit)");
            return true;
        }

        // ===== ACCEPT =====
        if (sub.equals("accept")) {
            Invite inv = invites.get(player.getUniqueId());
            if (inv == null) {
                Msg.send(player, "§cDu hast keine Einladung.");
                return true;
            }

            if (System.currentTimeMillis() > inv.expiresAt) {
                invites.remove(player.getUniqueId());
                Msg.send(player, "§cDie Einladung ist abgelaufen.");
                return true;
            }

            if (clanManager.isInClan(player.getUniqueId())) {
                invites.remove(player.getUniqueId());
                Msg.send(player, "§cDu bist bereits in einem Clan.");
                return true;
            }

            int members = clanManager.getMemberCount(inv.clanTag);
            int slots = clanManager.getSlots(inv.clanTag);
            if (members >= slots) {
                invites.remove(player.getUniqueId());
                Msg.send(player, "§cClan ist inzwischen voll.");
                return true;
            }

            clanManager.addMemberToClan(inv.clanTag, player.getUniqueId());
            invites.remove(player.getUniqueId());

            Msg.send(player, "§aDu bist Clan §e" + inv.clanTag + " §abeigetreten!");
            return true;
        }

        // ===== INFO =====
        if (sub.equals("info")) {
            String myTag = clanManager.getClanTagOf(player.getUniqueId());
            if (myTag == null) {
                Msg.send(player, "§cDu bist in keinem Clan.");
                return true;
            }

            Clan clan = clanManager.getClanByTag(myTag);
            if (clan == null) {
                Msg.send(player, "§cFehler: Clan nicht gefunden.");
                return true;
            }

            String ownerName = Bukkit.getOfflinePlayer(clan.getOwner()).getName();
            if (ownerName == null) ownerName = clan.getOwner().toString();

            int m = clan.getMembers().size();
            int s = clan.getMemberSlots();

            String memberNames = clan.getMembers().stream()
                    .map(u -> Bukkit.getOfflinePlayer(u).getName())
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("§7, §e"));

            Msg.sendRaw(player, "§8§m------------------------");
            Msg.sendRaw(player, Msg.PREFIX + "§fClan: §e" + clan.getTag());
            Msg.sendRaw(player, Msg.PREFIX + "§fOwner: §e" + ownerName);
            Msg.sendRaw(player, Msg.PREFIX + "§fSlots: §e" + m + "§7/§e" + s);
            Msg.sendRaw(player, Msg.PREFIX + "§fMitglieder: §e" + (memberNames.isEmpty() ? "Keine" : memberNames));
            Msg.sendRaw(player, "§8§m------------------------");
            return true;
        }

        // ===== KICK =====
        if (sub.equals("kick")) {
            if (args.length < 2) {
                Msg.send(player, "Nutze: §f/clan kick <Spieler>");
                return true;
            }

            String myTag = clanManager.getClanTagOf(player.getUniqueId());
            if (myTag == null) {
                Msg.send(player, "§cDu bist in keinem Clan.");
                return true;
            }

            Clan clan = clanManager.getClanByTag(myTag);
            if (clan == null) {
                Msg.send(player, "§cFehler: Clan nicht gefunden.");
                return true;
            }

            if (!clan.getOwner().equals(player.getUniqueId())) {
                Msg.send(player, "§cNur der Clan-Owner kann kicken.");
                return true;
            }

            Player targetOnline = Bukkit.getPlayerExact(args[1]);
            UUID targetUuid = (targetOnline != null)
                    ? targetOnline.getUniqueId()
                    : Bukkit.getOfflinePlayer(args[1]).getUniqueId();

            if (targetUuid.equals(player.getUniqueId())) {
                Msg.send(player, "§cDu kannst dich nicht selbst kicken.");
                return true;
            }

            if (!clan.getMembers().contains(targetUuid)) {
                Msg.send(player, "§cDieser Spieler ist nicht in deinem Clan.");
                return true;
            }

            clanManager.removeMember(targetUuid);
            Msg.send(player, "§aDu hast §e" + args[1] + " §aaus dem Clan gekickt.");

            if (targetOnline != null) {
                Msg.send(targetOnline, "§cDu wurdest aus Clan §e" + myTag + " §centfernt.");
            }
            return true;
        }

        // ===== LEAVE =====
        if (sub.equals("leave")) {
            String myTag = clanManager.getClanTagOf(player.getUniqueId());
            if (myTag == null) {
                Msg.send(player, "§cDu bist in keinem Clan.");
                return true;
            }

            Clan clan = clanManager.getClanByTag(myTag);
            if (clan == null) {
                Msg.send(player, "§cFehler: Clan nicht gefunden.");
                return true;
            }

            if (clan.getOwner().equals(player.getUniqueId())) {
                Msg.send(player, "§cDu bist der Owner. Nutze §e/clan delete §czum Löschen.");
                return true;
            }

            clanManager.removeMember(player.getUniqueId());
            Msg.send(player, "§aDu hast den Clan §e" + myTag + " §averlassen.");
            return true;
        }

        // ===== DELETE =====
        if (sub.equals("delete")) {
            String myTag = clanManager.getClanTagOf(player.getUniqueId());
            if (myTag == null) {
                Msg.send(player, "§cDu bist in keinem Clan.");
                return true;
            }

            Clan clan = clanManager.getClanByTag(myTag);
            if (clan == null) {
                Msg.send(player, "§cFehler: Clan nicht gefunden.");
                return true;
            }

            if (!clan.getOwner().equals(player.getUniqueId())) {
                Msg.send(player, "§cNur der Clan-Owner kann den Clan löschen.");
                return true;
            }

            clanManager.deleteClan(myTag);
            Msg.send(player, "§aClan §e" + myTag + " §awurde gelöscht.");
            return true;
        }

        Msg.send(player, "§cUnbekannt. Nutze §f/clan help");
        return true;
    }
}