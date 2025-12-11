package net.lunark.io.commands.impl;

import net.kyori.adventure.text.Component;
import net.lunark.io.commands.config.GlowConfig;
import net.lunark.io.commands.CommandDataStorage;
import net.lunark.io.language.PlayerLanguageManager;
import net.lunark.io.language.LanguageManager.ComponentPlaceholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class GlowCommand implements CommandExecutor {

    private static final String PERMISSION = "serveressentials.command.glow";
    private final PlayerLanguageManager langManager;
    private final GlowConfig config;
    private final CommandDataStorage dataStorage;

    public GlowCommand(PlayerLanguageManager langManager, GlowConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Component message = langManager.getMessageFor(null, "commands.glow.only-player", "Only players can use this command!");
            sender.sendMessage(message.toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            Component message = langManager.getMessageFor(player, "commands.glow.no-permission",
                    "You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION));
            player.sendMessage(message);
            return true;
        }

        if (args.length > 0) {
            Component message = langManager.getMessageFor(player, "commands.glow.usage", "Usage: /glow");
            player.sendMessage(message);
            return true;
        }

        if (player.isGlowing()) {
            player.setGlowing(false);
            Component message = langManager.getMessageFor(player, "commands.glow.disabled", "Glow disabled.");
            player.sendMessage(message);
        } else {
            player.setGlowing(true);
            Component message = langManager.getMessageFor(player, "commands.glow.enabled", "Glow enabled.");
            player.sendMessage(message);
        }

        trackUsage(player.getUniqueId(), "toggle", 1);

        return true;
    }

    private void trackUsage(UUID playerId, String type, int value) {
        dataStorage.getState(playerId, "glow", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "glow", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "glow", "last_type", type);
            dataStorage.setState(playerId, "glow", "last_used", String.valueOf(System.currentTimeMillis()));
        });
    }
}