package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.RulesConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.rules.gui.RulesGUI;
import net.godlycow.org.rules.storage.RulesStorage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class RulesCommand implements CommandExecutor {
    private final PlayerLanguageManager langManager;
    private final RulesConfig config;
    private final RulesStorage storage;
    private final Plugin plugin;

    public RulesCommand(PlayerLanguageManager langManager, RulesConfig config, RulesStorage storage, Plugin plugin) {
        this.langManager = langManager;
        this.config = config;
        this.storage = storage;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.rules.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(config.permission())) {
            player.sendMessage(langManager.getMessageFor(player, "commands.rules.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", config.permission())));
            return true;
        }

        if (args.length == 0) {
            new RulesGUI(langManager, storage, config, this.plugin).showRules(player);
            return true;
        }

        String action = args[0].toLowerCase();
        switch (action) {
            case "accept" -> handleAccept(player);
            case "reload" -> handleReload(player);
            default -> showUsage(player);
        }

        return true;
    }

    private void handleAccept(Player player) {
        if (player.hasMetadata("accepting_rules")) {
            return;
        }
        player.setMetadata("accepting_rules", new org.bukkit.metadata.FixedMetadataValue(plugin, true));

        storage.getLatestVersion().thenCompose(version -> {
            if (version == 0) {
                player.sendMessage(langManager.getMessageFor(player, "commands.rules.no-rules",
                        "<red>No rules are currently configured."));
                return CompletableFuture.completedFuture(null);
            }

            return storage.hasAcceptedRules(player.getUniqueId()).thenCompose(accepted -> {
                if (accepted) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.rules.already-accepted",
                            "<yellow>You have already accepted the latest rules."));
                    return CompletableFuture.completedFuture(null);
                }

                return storage.acceptRules(player.getUniqueId(), version)
                        .thenAccept(v -> {
                            player.removeMetadata("accepting_rules", plugin);
                        });
            });
        }).exceptionally(ex -> {
            player.removeMetadata("accepting_rules", plugin);
            player.sendMessage(langManager.getMessageFor(player, "commands.rules.error",
                    "<red>An error occurred: {error}",
                    ComponentPlaceholder.of("{error}", ex.getMessage())));
            plugin.getLogger().warning("Error in /rules accept for " + player.getName() + ": " + ex.getMessage());
            return null;
        });
    }

    private void handleReload(Player player) {
        if (!player.hasPermission(config.reloadPermission())) {
            player.sendMessage(langManager.getMessageFor(player, "commands.rules.no-permission-sub",
                    "<red>You need permission <yellow>{subpermission}</yellow>!",
                    ComponentPlaceholder.of("{subpermission}", config.reloadPermission())));
            return;
        }

        storage.reloadRulesFromConfig().thenAccept(v -> {
            player.sendMessage(langManager.getMessageFor(player, "commands.rules.reload-success",
                    "<green>Rules reloaded successfully!"));
        }).exceptionally(ex -> {
            player.sendMessage(langManager.getMessageFor(player, "commands.rules.reload-failure",
                    "<red>Failed to reload rules: {error}",
                    ComponentPlaceholder.of("{error}", ex.getMessage())));
            return null;
        });
    }

    private void showUsage(Player player) {
        player.sendMessage(langManager.getMessageFor(player, "commands.rules.usage",
                "<yellow>Usage: <white>/rules [accept|reload]"));
    }
}