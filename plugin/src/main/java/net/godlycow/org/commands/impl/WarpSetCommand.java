package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandModule;
import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.language.LanguageManager;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.warp.WarpManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class WarpSetCommand extends CommandModule implements CommandExecutor {
    private final WarpManager warpManager;

    public WarpSetCommand(WarpManager warpManager,
                          PlayerLanguageManager langManager, CommandDataStorage commandStorage) {
        super(commandStorage, langManager);
        this.warpManager = warpManager;
    }

    @Override
    protected String getCommandName() {
        return "setwarp";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.only-players",
                    "<red>This command can only be used by players!"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(langManager.getMessageFor(player, "commands.setwarp.usage",
                    "<red>Usage: <yellow>/setwarp <name>"));
            return true;
        }

        if (!player.hasPermission("essc.command.setwarp")) {
            player.sendMessage(langManager.getMessageFor(player, "commands.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    LanguageManager.ComponentPlaceholder.of("{permission}", "essc.command.setwarp")));
            return true;
        }

        String warpName = args[0].toLowerCase();
        int maxWarps = warpManager.getMaxWarpsForPlayer(player);

        warpManager.getWarpCountByCreator(player.getUniqueId()).thenAccept(playerWarpCount -> {
            if (playerWarpCount >= maxWarps) {
                player.sendMessage(langManager.getMessageFor(player, "commands.setwarp.limit-reached",
                        "<red>You have reached your warp limit of <yellow>{limit}</yellow>!",
                        LanguageManager.ComponentPlaceholder.of("{limit}", String.valueOf(maxWarps))));
                return;
            }

            warpManager.exists(warpName).thenCompose(exists -> {
                if (!exists) {
                    return CompletableFuture.completedFuture(true);
                }
                return warpManager.getCreator(warpName).thenApply(optCreator -> {
                    if (optCreator.isEmpty() || !optCreator.get().equals(player.getUniqueId())) {
                        player.sendMessage(langManager.getMessageFor(player, "commands.setwarp.already-exists",
                                "<red>Warp <yellow>{warp}</yellow> already exists.",
                                LanguageManager.ComponentPlaceholder.of("{warp}", warpName)));
                        return false;
                    }
                    return true;
                });
            }).thenCompose(canCreate -> {
                if (!canCreate) return CompletableFuture.completedFuture(null);
                Location loc = player.getLocation();
                return warpManager.addWarp(warpName, loc, player.getUniqueId()).thenRun(() -> {
                    player.sendMessage(langManager.getMessageFor(player, "commands.setwarp.success",
                            "<green>Created warp <yellow>{warp}</yellow>!",
                            LanguageManager.ComponentPlaceholder.of("{warp}", warpName)));
                });
            });
        });

        return true;
    }
}