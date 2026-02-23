package de.trist.clans.service;

import de.trist.clans.model.Clan;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ClanManager {

    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration cfg;

    private final Map<String, Clan> clansByTag = new HashMap<>();
    private final Map<UUID, String> playerClan = new HashMap<>();

    public ClanManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "clans.yml");
        reload();
    }

    public void reload() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException ignored) {}
        }
        cfg = YamlConfiguration.loadConfiguration(file);

        clansByTag.clear();
        playerClan.clear();

        var section = cfg.getConfigurationSection("clans");
        if (section == null) return;

        for (String tag : section.getKeys(false)) {
            String upTag = tag.toUpperCase(Locale.ROOT);

            String ownerStr = cfg.getString("clans." + tag + ".owner");
            if (ownerStr == null) continue;

            UUID owner = UUID.fromString(ownerStr);
            int slots = cfg.getInt("clans." + tag + ".slots", 5);

            Clan clan = new Clan(upTag, owner, slots);
            clan.getMembers().clear();

            List<String> members = cfg.getStringList("clans." + tag + ".members");
            for (String s : members) {
                UUID u = UUID.fromString(s);
                clan.getMembers().add(u);
                playerClan.put(u, upTag);
            }

            clansByTag.put(upTag, clan);
        }
    }

    public void save() {
        cfg.set("clans", null);

        for (Clan clan : clansByTag.values()) {
            String base = "clans." + clan.getTag().toUpperCase(Locale.ROOT);
            cfg.set(base + ".owner", clan.getOwner().toString());
            cfg.set(base + ".slots", clan.getMemberSlots());

            List<String> members = new ArrayList<>();
            for (UUID u : clan.getMembers()) members.add(u.toString());
            cfg.set(base + ".members", members);
        }

        try { cfg.save(file); }
        catch (IOException e) {
            plugin.getLogger().warning("Konnte clans.yml nicht speichern: " + e.getMessage());
        }
    }

    public boolean isInClan(UUID uuid) {
        return playerClan.containsKey(uuid);
    }

    public boolean clanExists(String tag) {
        return clansByTag.containsKey(tag.toUpperCase(Locale.ROOT));
    }

    public Clan createClan(String tag, UUID owner, int defaultSlots) {
        tag = tag.toUpperCase(Locale.ROOT);

        Clan clan = new Clan(tag, owner, defaultSlots);
        clansByTag.put(tag, clan);

        for (UUID u : clan.getMembers()) {
            playerClan.put(u, tag);
        }

        save();
        return clan;
    }

    // ===== Mitglieder hinzufügen =====

    public void addMemberToClan(String tag, UUID member) {
        tag = tag.toUpperCase(Locale.ROOT);
        Clan clan = clansByTag.get(tag);
        if (clan == null) return;

        clan.getMembers().add(member);
        playerClan.put(member, tag);
        save();
    }

    public int getMemberCount(String tag) {
        tag = tag.toUpperCase(Locale.ROOT);
        Clan clan = clansByTag.get(tag);
        if (clan == null) return 0;
        return clan.getMembers().size();
    }

    public int getSlots(String tag) {
        tag = tag.toUpperCase(Locale.ROOT);
        Clan clan = clansByTag.get(tag);
        if (clan == null) return 0;
        return clan.getMemberSlots();
    }

    public String getClanTagOf(UUID uuid) {
        return playerClan.get(uuid);
    }

    public Clan getClanByTag(String tag) {
        if (tag == null) return null;
        return clansByTag.get(tag.toUpperCase(Locale.ROOT));
    }

    public void removeMember(UUID member) {
        String tag = playerClan.get(member);
        if (tag == null) return;

        Clan clan = clansByTag.get(tag.toUpperCase(Locale.ROOT));
        if (clan == null) {
            playerClan.remove(member);
            return;
        }

        clan.getMembers().remove(member);
        playerClan.remove(member);
        save();
    }

    public void deleteClan(String tag) {
        tag = tag.toUpperCase(Locale.ROOT);
        Clan clan = clansByTag.remove(tag);
        if (clan == null) return;

        for (UUID u : clan.getMembers()) {
            playerClan.remove(u);
        }

        save();
    }public boolean isOwnerOfClan(UUID uuid, String tag) {
        Clan clan = getClanByTag(tag);
        return clan != null && clan.getOwner().equals(uuid);
    }

    public boolean isMemberOfClan(UUID uuid, String tag) {
        Clan clan = getClanByTag(tag);
        return clan != null && clan.getMembers().contains(uuid);
    }public boolean kickMember(String tag, UUID kicker, UUID target) {
        if (tag == null || kicker == null || target == null) return false;

        Clan clan = getClanByTag(tag);
        if (clan == null) return false;

        // Nur Owner darf kicken
        if (clan.getOwner() == null || !clan.getOwner().equals(kicker)) return false;

        // Owner darf nicht gekickt werden
        if (clan.getOwner().equals(target)) return false;

        // Target muss Member sein
        if (!clan.getMembers().contains(target)) return false;

        // Entfernen + speichern (nutzt deine bestehende Logik)
        removeMember(target);
        return true;
    }
}