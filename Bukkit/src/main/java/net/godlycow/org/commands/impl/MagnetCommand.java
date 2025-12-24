package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.MagnetConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.language.LanguageManager.ComponentPlaceholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public final class MagnetCommand implements CommandExecutor {
    private static final String PERMISSION_NODE = "serveressentials.command.magnet";
    private final PlayerLanguageManager langManager;
    private final MagnetConfig config;
    private final MagnetStorage storage;

    public MagnetCommand(PlayerLanguageManager langManager, MagnetConfig config, MagnetStorage storage) {
        this.langManager = langManager;
        this.config = config;
        this.storage = storage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.magnet.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION_NODE)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.magnet.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION_NODE)));
            return true;
        }

        boolean currentlyEnabled = storage.isEnabled(player);

        CompletableFuture<Void> future = currentlyEnabled
                ? storage.disableMagnet(player)
                : storage.enableMagnet(player);

        future.thenRun(() -> {
            boolean nowEnabled = storage.isEnabled(player);
            if (nowEnabled) {
                player.sendMessage(langManager.getMessageFor(player, "commands.magnet.toggle-on",
                        "<green>Magnet mode enabled. Items within {radius} blocks will be pulled to you.",
                        ComponentPlaceholder.of("{radius}", String.format("%.1f", config.radius))));
            } else {
                player.sendMessage(langManager.getMessageFor(player, "commands.magnet.toggle-off",
                        "<red>Magnet mode disabled."));
            }
        }).exceptionally(ex -> {
            player.sendMessage(langManager.getMessageFor(player, "commands.magnet.error",
                    "<red>Failed to toggle magnet: {error}",
                    ComponentPlaceholder.of("{error}", ex.getMessage())));
            return null;
        });

        return true;
    }
}