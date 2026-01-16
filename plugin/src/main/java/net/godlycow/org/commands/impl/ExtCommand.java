package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.ExtConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class ExtCommand implements CommandExecutor {
    private static final String PERMISSION_NODE = "essc.command.ext";
    private static final String PERMISSION_OTHERS = "essc.command.ext.others";

    private final PlayerLanguageManager langManager;
    private final ExtConfig config;

    public ExtCommand(PlayerLanguageManager langManager, ExtConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.ext.only-player",
                    "<red>Only players can use this command!").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION_NODE)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.ext.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION_NODE)));
            return true;
        }

        Player target;
        boolean isSelf;

        if (args.length == 0) {
            target = player;
            isSelf = true;
        } else {
            if (!player.hasPermission(PERMISSION_OTHERS)) {
                player.sendMessage(langManager.getMessageFor(player, "commands.ext.no-permission-others",
                        "<red>You need permission <yellow>{permission}</yellow> to extinguish other players!",
                        ComponentPlaceholder.of("{permission}", PERMISSION_OTHERS)));
                return true;
            }

            target = Bukkit.getPlayerExact(args[0]);
            if (target == null || !target.isOnline()) {
                player.sendMessage(langManager.getMessageFor(player, "commands.ext.player-not-found",
                        "<red>Player not found!"));
                return true;
            }
            isSelf = false;
        }

        target.setFireTicks(0);

        target.sendMessage(langManager.getMessageFor(target, "commands.ext.self",
                "<green>You have been extinguished!"));

        if (!isSelf) {
            player.sendMessage(langManager.getMessageFor(player, "commands.ext.target",
                    "<green>You extinguished {target}!",
                    ComponentPlaceholder.of("{target}", target.getName())));
        }

        return true;
    }
}