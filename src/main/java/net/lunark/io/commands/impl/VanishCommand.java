package net.lunark.io.commands.impl;

import net.lunark.io.commands.CommandDataStorage;
import net.lunark.io.commands.config.VanishConfig;
import net.lunark.io.language.PlayerLanguageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static net.lunark.io.language.LanguageManager.ComponentPlaceholder;

public final class VanishCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.vanish";
    private static final String COMMAND_NAME = "vanish";
    private static final Set<UUID> vanishedCache = ConcurrentHashMap.newKeySet();

    private final PlayerLanguageManager langManager;
    private final VanishConfig config;
    private final CommandDataStorage dataStorage;

    public VanishCommand(PlayerLanguageManager langManager, VanishConfig config, CommandDataStorage dataStorage) {
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
            Bukkit.getOnlinePlayers().forEach(p -> p.showPlayer(Bukkit.getPluginManager().getPlugin("lunark-io"), player));
            player.sendMessage(langManager.getMessageFor(player, "commands.vanish.visible", "<green>You are now visible to all players."));
        } else {
            vanishedCache.add(playerId);
            dataStorage.setState(playerId, COMMAND_NAME, "enabled", "true");
            Bukkit.getOnlinePlayers().forEach(p -> p.hidePlayer(Bukkit.getPluginManager().getPlugin("lunark-io"), player));
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
    }}