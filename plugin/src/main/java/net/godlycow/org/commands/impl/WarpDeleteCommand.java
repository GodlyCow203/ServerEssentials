package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandModule;
import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.language.LanguageManager;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.warp.WarpManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class WarpDeleteCommand extends CommandModule implements CommandExecutor {
    private final WarpManager warpManager;

    public WarpDeleteCommand(WarpManager warpManager,
                             PlayerLanguageManager langManager, CommandDataStorage commandStorage) {
        super(commandStorage, langManager);
        this.warpManager = warpManager;
    }

    @Override
    protected String getCommandName() {
        return "delwarp";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.only-players",
                    "<red>This command can only be used by players!"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(langManager.getMessageFor(player, "commands.delwarp.usage",
                    "<red>Usage: <yellow>/delwarp <name>"));
            return true;
        }

        if (!player.hasPermission("essc.command.delwarp")) {
            player.sendMessage(langManager.getMessageFor(player, "commands.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    LanguageManager.ComponentPlaceholder.of("{permission}", "essc.command.delwarp")));
            return true;
        }

        String warpName = args[0].toLowerCase();

        warpManager.exists(warpName).thenCompose(exists -> {
            if (!exists) {
                player.sendMessage(langManager.getMessageFor(player, "commands.delwarp.not-found",
                        "<red>Warp <yellow>{warp}</yellow> not found.",
                        LanguageManager.ComponentPlaceholder.of("{warp}", warpName)));
                return CompletableFuture.completedFuture(null);
            }
            return warpManager.getCreator(warpName).thenCompose(optCreator -> {
                UUID creator = optCreator.orElse(null);
                if (creator == null || (!creator.equals(player.getUniqueId()) &&
                        !player.hasPermission("essc.command.delwarp.others"))) {
                    player.sendMessage(langManager.getMessageFor(player, "commands.delwarp.no-permission",
                            "<red>You don't have permission to delete this warp!"));
                    return CompletableFuture.completedFuture(null);
                }
                return warpManager.removeWarp(warpName).thenRun(() -> {
                    player.sendMessage(langManager.getMessageFor(player, "commands.delwarp.success",
                            "<green>Deleted warp <yellow>{warp}</yellow>!",
                            LanguageManager.ComponentPlaceholder.of("{warp}", warpName)));
                });
            });
        });

        return true;
    }
}