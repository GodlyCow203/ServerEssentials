package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.AdminChatConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class AdminChatCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.adminchat";
    private static final String COMMAND_NAME = "adminchat";
    private static final Set<UUID> toggledCache = ConcurrentHashMap.newKeySet();

    private final PlayerLanguageManager langManager;
    private final AdminChatConfig config;
    private final CommandDataStorage dataStorage;

    public AdminChatCommand(PlayerLanguageManager langManager, AdminChatConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.adminchat.only-players", "<red>Only players can use this command."));
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.adminchat.no-permission", "<red>You need permission <yellow>{permission}</yellow>!", ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        UUID playerId = player.getUniqueId();

        if (args.length == 0) {
            if (toggledCache.contains(playerId)) {
                toggledCache.remove(playerId);
                dataStorage.deleteState(playerId, COMMAND_NAME, "enabled");
                player.sendMessage(langManager.getMessageFor(player, "commands.adminchat.toggle", "<green>Admin chat mode: {state}", ComponentPlaceholder.of("{state}", "OFF")));
            } else {
                toggledCache.add(playerId);
                dataStorage.setState(playerId, COMMAND_NAME, "enabled", "true");
                player.sendMessage(langManager.getMessageFor(player, "commands.adminchat.toggle", "<green>Admin chat mode: {state}", ComponentPlaceholder.of("{state}", "ON")));
            }
        } else {
            sendAdminMessage(player, String.join(" ", args));
        }

        return true;
    }

    private void sendAdminMessage(Player sender, String message) {
        Component formatted = langManager.getMessageFor(sender, "commands.adminchat.format", "<dark_gray>[<red>AdminChat<dark_gray>] <yellow>{player} <gray>» <white>{message}", ComponentPlaceholder.of("{player}", sender.getName()), ComponentPlaceholder.of("{message}", message));
        Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission(PERMISSION)).forEach(p -> p.sendMessage(formatted));
        Bukkit.getConsoleSender().sendMessage(formatted);
    }

    public static boolean isInAdminChat(UUID playerId) {
        return toggledCache.contains(playerId);
    }

    public void loadPlayerState(UUID playerId) {
        dataStorage.getState(playerId, COMMAND_NAME, "enabled").thenAccept(opt -> {
            if (opt.isPresent() && "true".equals(opt.get())) {
                toggledCache.add(playerId);
            }
        });
    }

    public void unloadPlayerState(UUID playerId) {
        toggledCache.remove(playerId);
    }
}