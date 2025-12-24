package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.VanishConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin; // Add this import

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class VanishCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.vanish";
    private static final String COMMAND_NAME = "vanish";
    private static final Set<UUID> vanishedCache = ConcurrentHashMap.newKeySet();

    private final PlayerLanguageManager langManager;
    private final VanishConfig config;
    private final CommandDataStorage dataStorage;
    private final JavaPlugin plugin;

    public VanishCommand(JavaPlugin plugin, PlayerLanguageManager langManager, VanishConfig config, CommandDataStorage dataStorage) {
        this.plugin = plugin; // Store plugin instance
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.vanish.only-player", "<red>Only players can use this command!"));
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.vanish.no-permission", "<red>You need permission <yellow>{permission}</yellow>!", ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        UUID playerId = player.getUniqueId();

        if (vanishedCache.contains(playerId)) {
            vanishedCache.remove(playerId);
            dataStorage.deleteState(playerId, COMMAND_NAME, "enabled");
            Bukkit.getOnlinePlayers().forEach(p -> p.showPlayer(plugin, player)); // Use plugin instance
            player.sendMessage(langManager.getMessageFor(player, "commands.vanish.visible", "<green>You are now visible to all players."));
        } else {
            vanishedCache.add(playerId);
            dataStorage.setState(playerId, COMMAND_NAME, "enabled", "true");
            Bukkit.getOnlinePlayers().forEach(p -> p.hidePlayer(plugin, player)); // Use plugin instance
            player.sendMessage(langManager.getMessageFor(player, "commands.vanish.vanished", "<green>You are now vanished."));
        }

        return true;
    }

    public static boolean isVanished(UUID playerId) {
        return vanishedCache.contains(playerId);
    }

    public void loadPlayerState(UUID playerId) {
        dataStorage.getState(playerId, COMMAND_NAME, "enabled").thenAccept(opt -> {
            if (opt.isPresent() && "true".equals(opt.get())) {
                vanishedCache.add(playerId);
            }
        });
    }

    public void unloadPlayerState(UUID playerId) {
        vanishedCache.remove(playerId);
    }
}