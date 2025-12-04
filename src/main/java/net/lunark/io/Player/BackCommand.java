package net.lunark.io.Player;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import net.lunark.io.Managers.BackManager;
import net.lunark.io.util.PlayerMessages;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BackCommand implements CommandExecutor, TabCompleter {

    private final PlayerMessages messages;

    public BackCommand(PlayerMessages messages) {
        this.messages = messages;

        messages.addDefault("Back.no-permission", "<red>You don't have permission to use this command.");
        messages.addDefault("Back.no-back", "<red>No previous location saved.");
        messages.addDefault("Back.invalid-back", "<red>Your last location is invalid.");
        messages.addDefault("Back.teleported-back", "<green>Teleported back to your previous location.");
        messages.addDefault("Back.lobby-not-set", "<red>Lobby location not set.");
        messages.addDefault("Back.teleported-lobby", "<green>Teleported to the lobby.");
        messages.addDefault("Back.no-death-location", "<red>No death location found.");
        messages.addDefault("Back.teleported-death", "<green>Teleported to your last death location.");
        messages.addDefault("Back.unknown-usage", "<red>Unknown command. Use: /back, /back lobby, /back death.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        UUID uuid = player.getUniqueId();

        if (args.length == 0) {
            if (!player.hasPermission("serveressentials.back.last")) {
                player.sendMessage(messages.get("Back.no-permission"));
                return true;
            }

            return teleportToBack(player, uuid);
        }

        switch (args[0].toLowerCase()) {
            case "lobby" -> {
                if (!player.hasPermission("serveressentials.back.lobby")) {
                    player.sendMessage(messages.get("Back.no-permission"));
                    return true;
                }
                Location lobbyLocation = getLobbyLocation();
                if (lobbyLocation == null) {
                    player.sendMessage(messages.get("Back.lobby-not-set"));
                    return true;
                }
                player.teleport(lobbyLocation);
                player.sendMessage(messages.get("Back.teleported-lobby"));
            }
            case "death" -> {
                if (!player.hasPermission("serveressentials.back.death")) {
                    player.sendMessage(messages.get("Back.no-permission"));
                    return true;
                }
                Location deathLocation = player.getLastDeathLocation();
                if (deathLocation == null) {
                    player.sendMessage(messages.get("Back.no-death-location"));
                    return true;
                }
                player.teleport(deathLocation);
                player.sendMessage(messages.get("Back.teleported-death"));
            }
            case "back" -> {
                if (!player.hasPermission("serveressentials.back.last")) {
                    player.sendMessage(messages.get("Back.no-permission"));
                    return true;
                }
                teleportToBack(player, uuid);
            }
            default -> player.sendMessage(messages.get("Back.unknown-usage"));
        }

        return true;
    }

    private boolean teleportToBack(Player player, UUID uuid) {
        if (!BackManager.hasBack(uuid)) {
            player.sendMessage(messages.get("Back.no-back"));
            return true;
        }
        Location backLocation = BackManager.getLastLocation(uuid);
        if (backLocation == null) {
            player.sendMessage(messages.get("Back.invalid-back"));
            return true;
        }
        player.teleport(backLocation);
        player.sendMessage(messages.get("Back.teleported-back"));
        BackManager.clearBack(uuid);
        return true;
    }

    private Location getLobbyLocation() {
        return new Location(Bukkit.getWorld("world"), 0, 100, 0); }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return new ArrayList<>();

        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            if (player.hasPermission("serveressentials.back.lobby")) completions.add("lobby");
            if (player.hasPermission("serveressentials.back.death")) completions.add("death");
            if (player.hasPermission("serveressentials.back.last")) completions.add("back");
        }
        return completions;
    }
}
