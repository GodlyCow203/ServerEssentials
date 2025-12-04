package net.lunark.io.Player;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import net.lunark.io.ServerEssentials;
import net.lunark.io.util.PlayerMessages;

import java.util.ArrayList;
import java.util.List;

public class PTimeCommand implements CommandExecutor, TabCompleter {

    private final PlayerMessages messages;

    public PTimeCommand(ServerEssentials plugin) {
        this.messages = plugin.getPlayerMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("PTime.only-players"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(messages.get("PTime.usage"));
            return true;
        }

        String option = args[0].toLowerCase();

        switch (option) {
            case "day" -> {
                if (!player.hasPermission("serveressentials.ptime.day")) {
                    player.sendMessage(messages.get("PTime.no-permission"));
                    return true;
                }
                player.setPlayerTime(1000, false);
            }
            case "night" -> {
                if (!player.hasPermission("serveressentials.ptime.night")) {
                    player.sendMessage(messages.get("PTime.no-permission"));
                    return true;
                }
                player.setPlayerTime(13000, false);
            }
            case "reset" -> {
                if (!player.hasPermission("serveressentials.ptime.reset")) {
                    player.sendMessage(messages.get("PTime.no-permission"));
                    return true;
                }
                player.resetPlayerTime();
            }
            default -> {
                player.sendMessage(messages.get("PTime.invalid-option"));
                return true;
            }
        }

        player.sendMessage(messages.get("PTime.success", "{option}", option.toUpperCase()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return List.of();

        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            if (player.hasPermission("serveressentials.ptime.day")) completions.add("day");
            if (player.hasPermission("serveressentials.ptime.night")) completions.add("night");
            if (player.hasPermission("serveressentials.ptime.reset")) completions.add("reset");
        }
        return completions;
    }
}
