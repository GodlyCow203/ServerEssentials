package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.GodConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class GodCommand implements CommandExecutor {

    private static final String PERMISSION = "serveressentials.command.god";
    private static final String COMMAND_NAME = "god";

    private final Set<UUID> godModeCache = ConcurrentHashMap.newKeySet();

    private final PlayerLanguageManager langManager;
    private final GodConfig config;
    private final CommandDataStorage dataStorage;

    public GodCommand(PlayerLanguageManager langManager, GodConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.god.only-player", "<red>Only players can use this command!"));
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.god.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        UUID playerId = player.getUniqueId();

        if (godModeCache.contains(playerId)) {
            godModeCache.remove(playerId);
            dataStorage.deleteState(playerId, COMMAND_NAME, "enabled");
            player.sendMessage(langManager.getMessageFor(player, "commands.god.disabled", "<red>God mode disabled."));
        } else {
            godModeCache.add(playerId);
            dataStorage.setState(playerId, COMMAND_NAME, "enabled", "true");
            player.sendMessage(langManager.getMessageFor(player, "commands.god.enabled", "<green>God mode enabled."));
        }

        return true;
    }

    public boolean isGodMode(UUID playerId) {
        return godModeCache.contains(playerId);
    }

    public void loadPlayerState(UUID playerId) {
        dataStorage.getState(playerId, COMMAND_NAME, "enabled").thenAccept(opt -> {
            if (opt.isPresent() && "true".equals(opt.get())) {
                godModeCache.add(playerId);
            }
        });
    }

    public void unloadPlayerState(UUID playerId) {
        godModeCache.remove(playerId);
    }
}
