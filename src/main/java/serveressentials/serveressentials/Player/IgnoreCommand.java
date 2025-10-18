package serveressentials.serveressentials.Player;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.PlayerMessages;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class IgnoreCommand implements CommandExecutor, Listener {

    private final Map<UUID, Set<UUID>> ignoredPlayers = new HashMap<>();
    private final PlayerMessages messages;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private File file;
    private FileConfiguration config;

    public IgnoreCommand(PlayerMessages messages) {
        this.messages = messages;

        // Register defaults
        messages.addDefault("Ignore.Messages.PlayerOnly", "<red>Only players can use this command.");
        messages.addDefault("Ignore.Messages.Usage", "<red>Usage: /ignore <player>");
        messages.addDefault("Ignore.Messages.NotFound", "<red>Player not found!");
        messages.addDefault("Ignore.Messages.Self", "<red>You cannot ignore yourself!");
        messages.addDefault("Ignore.Messages.Add", "<green>You are now ignoring <yellow><player><green>.");
        messages.addDefault("Ignore.Messages.Remove", "<green>You are no longer ignoring <yellow><player><green>.");

        // Register events (for future integration like blocking chat, payments, etc.)
        Bukkit.getPluginManager().registerEvents(this, ServerEssentials.getInstance());

        // Load ignored players
        loadIgnoredPlayers();
    }

    private void loadIgnoredPlayers() {
        file = new File(ServerEssentials.getInstance().getDataFolder(), "storage/ignoredplayers.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            UUID playerUUID = UUID.fromString(key);
            Set<UUID> ignoredSet = new HashSet<>();
            for (String ignoredUUID : config.getStringList(key)) {
                ignoredSet.add(UUID.fromString(ignoredUUID));
            }
            ignoredPlayers.put(playerUUID, ignoredSet);
        }
    }

    private void saveIgnoredPlayers() {
        for (UUID playerUUID : ignoredPlayers.keySet()) {
            Set<UUID> ignoredSet = ignoredPlayers.get(playerUUID);
            Set<String> ignoredStrings = new HashSet<>();
            for (UUID ignoredUUID : ignoredSet) {
                ignoredStrings.add(ignoredUUID.toString());
            }
            config.set(playerUUID.toString(), new ArrayList<>(ignoredStrings));
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isIgnoring(Player player, Player target) {
        IgnoreCommand instance = ServerEssentials.getInstance().getIgnoreCommand(); // main needs getter
        Set<UUID> ignoredSet = instance.ignoredPlayers.get(player.getUniqueId());
        return ignoredSet != null && ignoredSet.contains(target.getUniqueId());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("Ignore.Messages.PlayerOnly"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(messages.get("Ignore.Messages.Usage"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(messages.get("Ignore.Messages.NotFound"));
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(messages.get("Ignore.Messages.Self"));
            return true;
        }

        Set<UUID> ignoredSet = ignoredPlayers.getOrDefault(player.getUniqueId(), new HashSet<>());

// When unignoring
        String removeMsg = messages.getConfig()
                .getString("Ignore.Messages.Remove", "<green>You are no longer ignoring <yellow><player><green>.")
                .replace("<player>", target.getName());
        player.sendMessage(miniMessage.deserialize(removeMsg));

// When ignoring
        String addMsg = messages.getConfig()
                .getString("Ignore.Messages.Add", "<green>You are now ignoring <yellow><player><green>.")
                .replace("<player>", target.getName());
        player.sendMessage(miniMessage.deserialize(addMsg));


        ignoredPlayers.put(player.getUniqueId(), ignoredSet);
        saveIgnoredPlayers();
        return true;
    }
}
