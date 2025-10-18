package serveressentials.serveressentials.lobby;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LobbyCommand implements CommandExecutor {

    // Cooldowns
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LobbyMessages.get("only-players"));
            return true;
        }

        if (args.length == 0) {
            // /lobby -> teleport
            if (!player.hasPermission("serveressentials.lobby")) {
                player.sendMessage(LobbyMessages.get("no-permission"));
                return true;
            }

            if (!LobbyStorage.hasLobby(player.getWorld().getName())) {
                player.sendMessage(LobbyMessages.get("no-lobby"));
                return true;
            }

            // Cooldown check
            int cooldownTime = LobbyConfig.getCooldown();
            long now = System.currentTimeMillis();
            if (cooldowns.containsKey(player.getUniqueId())) {
                long last = cooldowns.get(player.getUniqueId());
                int secondsLeft = (int) ((last + cooldownTime * 1000 - now) / 1000);
                if (secondsLeft > 0) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("time", String.valueOf(secondsLeft));
                    player.sendMessage(LobbyMessages.getWithPlaceholders("cooldown-active", placeholders));
                    return true;
                }
            }

            cooldowns.put(player.getUniqueId(), now);

            Location lobby = LobbyStorage.getLobby(player.getWorld().getName());
            player.teleport(lobby);

            player.sendMessage(LobbyMessages.get("teleport-lobby"));
            return true;
        }

        // Subcommands
        String sub = args[0].toLowerCase();

        if (sub.equals("set")) {
            if (!player.hasPermission("serveressentials.lobby.set")) {
                player.sendMessage(LobbyMessages.get("no-permission"));
                return true;
            }
            if (args.length == 2 && args[1].equalsIgnoreCase("world")) {
                if (!LobbyConfig.isPerWorld()) {
                    player.sendMessage(LobbyMessages.get("no-permission"));
                    return true;
                }
                String worldName = player.getWorld().getName();
                LobbyStorage.setWorldLobby(worldName, player.getLocation());

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("world", worldName);
                player.sendMessage(LobbyMessages.getWithPlaceholders("world-set", placeholders));
                return true;
            } else {
                LobbyStorage.setLobby(player.getLocation());
                player.sendMessage(LobbyMessages.get("set-lobby"));
            }
        } else if (sub.equals("remove")) {
            if (!player.hasPermission("serveressentials.lobby.remove")) {
                player.sendMessage(LobbyMessages.get("no-permission"));
                return true;
            }
            if (args.length == 2 && args[1].equalsIgnoreCase("world") && LobbyConfig.isPerWorld()) {
                String worldName = player.getWorld().getName();
                LobbyStorage.removeLobby(worldName);
            } else {
                LobbyStorage.removeLobby(null);
            }
            player.sendMessage(LobbyMessages.get("removed-lobby"));
        } else if (sub.equals("world")) {
            if (!player.hasPermission("serveressentials.lobby.world")) {
                player.sendMessage(LobbyMessages.get("no-permission"));
                return true;
            }
            if (!LobbyConfig.isPerWorld()) {
                player.sendMessage(LobbyMessages.get("no-permission"));
                return true;
            }
            if (args.length < 2) {
                player.sendMessage(LobbyMessages.get("no-lobby"));
                return true;
            }
            String worldName = args[1];
            if (Bukkit.getWorld(worldName) == null) {
                player.sendMessage(LobbyMessages.get("no-lobby"));
                return true;
            }
            LobbyStorage.setWorldLobby(worldName, player.getLocation());
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("world", worldName);
            player.sendMessage(LobbyMessages.getWithPlaceholders("world-set", placeholders));
        } else {
            player.sendMessage(LobbyMessages.get("no-permission"));
        }

        return true;
    }
}
