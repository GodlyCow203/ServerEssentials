package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandModule;
import net.godlycow.org.commands.CommandDataStorage;
import net.kyori.adventure.text.Component;
import net.godlycow.org.language.LanguageManager;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.warp.WarpManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpsCommand extends CommandModule implements CommandExecutor {
    private final WarpManager warpManager;

    public WarpsCommand(WarpManager warpManager,
                        PlayerLanguageManager langManager, CommandDataStorage commandStorage) {
        super(commandStorage, langManager);
        this.warpManager = warpManager;
    }

    @Override
    protected String getCommandName() {
        return "warps";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.only-players",
                    "<red>This command can only be used by players!"));
            return true;
        }

        if (!player.hasPermission("serveressentials.command.warps")) {
            player.sendMessage(langManager.getMessageFor(player, "commands.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    LanguageManager.ComponentPlaceholder.of("{permission}", "serveressentials.command.warps")));
            return true;
        }

        warpManager.getAllWarps().thenAccept(warps -> {
            if (warps.isEmpty()) {
                player.sendMessage(langManager.getMessageFor(player, "warps.empty",
                        "<yellow>No warps have been set!"));
                return;
            }

            player.sendMessage(langManager.getMessageFor(player, "warps.header",
                    "<gold>------[ Warps ]------"));

            warps.keySet().forEach(warp -> {
                Component line = langManager.getMessageFor(player, "warps.entry",
                        "<green>{warp}",
                        LanguageManager.ComponentPlaceholder.of("{warp}", warp));
                player.sendMessage(line);
            });

            player.sendMessage(langManager.getMessageFor(player, "warps.footer",
                    "<gold>-------------------"));
        });

        return true;
    }
}