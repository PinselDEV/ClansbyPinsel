package de.trist.clans.hooks;

import de.trist.clans.model.Clan;
import de.trist.clans.service.ClanManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ClanPlaceholders extends PlaceholderExpansion {

    private final ClanManager clanManager;

    public ClanPlaceholders(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "clan";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Trist";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        UUID uuid = player.getUniqueId();
        String tag = clanManager.getClanTagOf(uuid);

        // Spieler ist in keinem Clan
        if (tag == null) {
            return switch (params.toLowerCase()) {
                case "tag" -> "-";
                case "tag_brackets" -> "";
                case "members" -> "0";
                case "slots" -> "0";
                case "role" -> "-";
                default -> "";
            };
        }

        Clan clan = clanManager.getClanByTag(tag);
        if (clan == null) return "";

        return switch (params.toLowerCase()) {
            case "tag" -> clan.getTag();
            case "tag_brackets" -> "§8[§6" + clan.getTag() + "§8]";
            case "members" -> String.valueOf(clan.getMembers().size());
            case "slots" -> String.valueOf(clan.getMemberSlots());
            case "role" -> (clan.getOwner() != null && clan.getOwner().equals(uuid)) ? "Owner" : "Member";
            default -> "";
        };
    }
}