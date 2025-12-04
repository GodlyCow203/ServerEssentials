package net.lunark.io.commands.impl;

import net.lunark.io.commands.config.WorldConfig;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.lunark.io.language.LanguageManager.ComponentPlaceholder;

public final class WorldCommand implements CommandExecutor {
    private static final String PERMISSION_NODE = "serveressentials.command.world";
    private final PlayerLanguageManager langManager;
    private final WorldConfig config;

    public WorldCommand(PlayerLanguageManager langManager, WorldConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.world.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION_NODE)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.world.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION_NODE)));
            return true;
        }

        if (args.length > 0) {
            player.sendMessage(langManager.getMessageFor(player, "commands.world.usage",
                    "<red>Usage: /world",
                    ComponentPlaceholder.of("{player}", player.getName())));
            return true;
        }

        player.sendMessage(langManager.getMessageFor(player, "commands.world.current",
                "<green>You are in world: <yellow>{world}",
                ComponentPlaceholder.of("{player}", player.getName()),
                ComponentPlaceholder.of("{world}", player.getWorld().getName())));

        return true;
    }
}