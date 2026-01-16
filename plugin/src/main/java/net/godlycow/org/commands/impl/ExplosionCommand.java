package net.godlycow.org.commands.impl;

import net.kyori.adventure.text.Component;
import net.godlycow.org.commands.config.ExplosionConfig;
import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.language.LanguageManager.ComponentPlaceholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public final class ExplosionCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "essc.command.explosion";
    private final PlayerLanguageManager langManager;
    private final ExplosionConfig config;
    private final CommandDataStorage dataStorage;

    public ExplosionCommand(PlayerLanguageManager langManager, ExplosionConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Component message = langManager.getMessageFor(null, "commands.explosion.only-player",
                    "<red>Only players can use this command!");
            sender.sendMessage(message.toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            Component message = langManager.getMessageFor(player, "commands.explosion.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION));
            player.sendMessage(message);
            return true;
        }

        if (args.length < 1) {
            Component message = langManager.getMessageFor(player, "commands.explosion.no-radius",
                    "<yellow>Please specify a radius!");
            player.sendMessage(message);
            return true;
        }

        try {
            float radius = Float.parseFloat(args[0]);

            player.getWorld().createExplosion(player.getLocation(), radius);

            Component message = langManager.getMessageFor(player, "commands.explosion.created",
                    "<green>Explosion created with radius {radius}!",
                    ComponentPlaceholder.of("{radius}", args[0]));
            player.sendMessage(message);

            trackUsage(player.getUniqueId(), "explosion", 1);

        } catch (NumberFormatException e) {
            Component message = langManager.getMessageFor(player, "commands.explosion.invalid-radius",
                    "<red>Invalid radius: {input}",
                    ComponentPlaceholder.of("{input}", args[0]));
            player.sendMessage(message);
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
        }
        return List.of();
    }

    private void trackUsage(UUID playerId, String type, int value) {
        dataStorage.getState(playerId, "explosion", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "explosion", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "explosion", "last_type", type);
            dataStorage.setState(playerId, "explosion", "last_used", String.valueOf(System.currentTimeMillis()));
        });
    }
}