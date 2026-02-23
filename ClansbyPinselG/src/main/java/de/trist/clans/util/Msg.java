package de.trist.clans.util;

import org.bukkit.command.CommandSender;

public class Msg {

    public static final String PREFIX = "§8[§bClan§8] §7";

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + message);
    }

    public static void sendRaw(CommandSender sender, String message) {
        sender.sendMessage(message);
    }
}