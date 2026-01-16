package net.godlycow.org.commands.impl;

import net.kyori.adventure.text.Component;
import net.godlycow.org.commands.config.CanonConfig;
import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.language.LanguageManager.ComponentPlaceholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class CanonCommand implements CommandExecutor {

    private static final String PERMISSION = "essc.command.canon";
    private final PlayerLanguageManager langManager;
    private final CanonConfig config;
    private final CommandDataStorage dataStorage;

    public CanonCommand(PlayerLanguageManager langManager, CanonConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Component message = langManager.getMessageFor(null, "commands.canon.only-player",
                    "<red>Only players can use this command.");
            sender.sendMessage(message.toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            Component message = langManager.getMessageFor(player, "commands.canon.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION));
            player.sendMessage(message);
            return true;
        }

        if (args.length > 0) {
            Component message = langManager.getMessageFor(player, "commands.canon.usage",
                    "<red>Usage: <white>/canon");
            player.sendMessage(message);
            return true;
        }

        Vector velocity = player.getLocation().getDirection().multiply(config.getVelocity()).setY(2);
        player.setVelocity(velocity);

        Component message = langManager.getMessageFor(player, "commands.canon.success",
                "<green>Woosh! You were launched into the air!");
        player.sendMessage(message);

        trackUsage(player.getUniqueId(), "launch", 1);

        return true;
    }

    private void trackUsage(UUID playerId, String type, int value) {
        dataStorage.getState(playerId, "canon", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "canon", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "canon", "last_type", type);
            dataStorage.setState(playerId, "canon", "last_used", String.valueOf(System.currentTimeMillis()));
        });
    }
}