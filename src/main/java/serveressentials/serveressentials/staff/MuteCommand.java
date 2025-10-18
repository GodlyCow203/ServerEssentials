package serveressentials.serveressentials.staff;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.MessagesManager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MuteCommand implements CommandExecutor {

    private static final Map<UUID, MuteData> mutedPlayers = new HashMap<>();
    private final MessagesManager messages;

    private final File muteFile;
    private final FileConfiguration muteConfig;

    public MuteCommand(ServerEssentials plugin) {
        this.messages = new MessagesManager(plugin);

        // Default messages
        messages.addDefault("mute.no-permission", "<red>You don't have permission.");
        messages.addDefault("mute.usage", "<yellow>Usage: /mute <player> <reason> [duration]");
        messages.addDefault("mute.not-found", "<red>Player not found.");
        messages.addDefault("mute.already-muted", "<red><player> is already muted for <reason>.");
        messages.addDefault("mute.success", "<yellow>You muted <green><player></green> <gray>for <reason> <yellow>(<duration>)</yellow>.");
        messages.addDefault("mute.notify", "<red>You have been muted! <gray>Reason: <reason> <yellow>(<duration>)</yellow>");
        messages.addDefault("unmute.notify", "<green>You have been unmuted!");

        // Setup mute storage file
        muteFile = new File(plugin.getDataFolder(), "storage/mutes.yml");
        if (!muteFile.exists()) {
            try {
                muteFile.getParentFile().mkdirs();
                muteFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        muteConfig = YamlConfiguration.loadConfiguration(muteFile);

        // Load existing mutes
        if (muteConfig.contains("mutes")) {
            for (String uuidString : muteConfig.getConfigurationSection("mutes").getKeys(false)) {
                String reason = muteConfig.getString("mutes." + uuidString + ".reason", "No reason");
                long expiresAt = muteConfig.getLong("mutes." + uuidString + ".expiresAt", -1);
                mutedPlayers.put(UUID.fromString(uuidString), new MuteData(reason, expiresAt));
            }
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<UUID, MuteData>> iterator = mutedPlayers.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<UUID, MuteData> entry = iterator.next();
                    MuteData data = entry.getValue();
                    if (data.expiresAt != -1 && System.currentTimeMillis() > data.expiresAt) {
                        UUID uuid = entry.getKey();
                        iterator.remove();

                        // Remove from file
                        muteConfig.set("mutes." + uuid, null);
                        saveMutes();

                        // Notify player
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null && player.isOnline()) {
                            player.sendMessage(messages.getMessageComponent("unmute.notify"));
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L * 60);
    }

    public static void unmute(UUID uuid, MessagesManager messages) {
        if (!mutedPlayers.containsKey(uuid)) return;

        mutedPlayers.remove(uuid);

        // Remove from storage
        File muteFile = new File(ServerEssentials.getInstance().getDataFolder(), "storage/mutes.yml");
        FileConfiguration muteConfig = YamlConfiguration.loadConfiguration(muteFile);
        muteConfig.set("mutes." + uuid, null);

        try {
            muteConfig.save(muteFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Notify player
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            player.sendMessage(messages.getMessageComponent("unmute.notify"));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("serveressentials.mute")) {
            sender.sendMessage(messages.getMessageComponent("mute.no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(messages.getMessageComponent("mute.usage"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(messages.getMessageComponent("mute.not-found"));
            return true;
        }

        String reason = args[1];
        String durationStr = args.length > 2 ? args[2] : "perm";
        long expiresAt = parseDuration(durationStr);

        if (mutedPlayers.containsKey(target.getUniqueId())) {
            MuteData data = mutedPlayers.get(target.getUniqueId());
            sender.sendMessage(messages.getMessageComponent(
                    "mute.already-muted",
                    "<player>", target.getName(),
                    "<reason>", data.reason
            ));
            return true;
        }

        mutedPlayers.put(target.getUniqueId(), new MuteData(reason, expiresAt));

        // Save to file
        muteConfig.set("mutes." + target.getUniqueId() + ".reason", reason);
        muteConfig.set("mutes." + target.getUniqueId() + ".expiresAt", expiresAt);
        saveMutes();

        // Notify staff
        sender.sendMessage(messages.getMessageComponent(
                "mute.success",
                "<player>", target.getName(),
                "<reason>", reason,
                "<duration>", durationStr
        ));

        // Notify player
        target.sendMessage(messages.getMessageComponent(
                "mute.notify",
                "<reason>", reason,
                "<duration>", durationStr
        ));

        return true;
    }

    private void saveMutes() {
        try {
            muteConfig.save(muteFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Check if a player is muted
    public static boolean isMuted(UUID uuid) {
        if (!mutedPlayers.containsKey(uuid)) return false;

        MuteData data = mutedPlayers.get(uuid);
        if (data.expiresAt != -1 && System.currentTimeMillis() > data.expiresAt) {
            mutedPlayers.remove(uuid);
            return false;
        }

        return true;
    }

    public static String getMuteReason(UUID uuid) {
        MuteData data = mutedPlayers.get(uuid);
        return data != null ? data.reason : "No reason";
    }



    // Helper method to parse duration strings like 10m, 2h, 1d
    private long parseDuration(String input) {
        if (input.equalsIgnoreCase("perm")) return -1;

        try {
            long time = Long.parseLong(input.substring(0, input.length() - 1));
            char unit = input.charAt(input.length() - 1);

            switch (unit) {
                case 's': return System.currentTimeMillis() + time * 1000L;
                case 'm': return System.currentTimeMillis() + time * 60 * 1000L;
                case 'h': return System.currentTimeMillis() + time * 60 * 60 * 1000L;
                case 'd': return System.currentTimeMillis() + time * 24 * 60 * 60 * 1000L;
                default: return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    // Internal class for mute data
    private static class MuteData {
        String reason;
        long expiresAt; // -1 = permanent

        public MuteData(String reason, long expiresAt) {
            this.reason = reason;
            this.expiresAt = expiresAt;
        }
    }

}
