package net.godlycow.org.commands.impl;

import net.kyori.adventure.text.Component;
import net.godlycow.org.commands.config.BeezookaConfig;
import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.language.LanguageManager.ComponentPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public final class BeezookaCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "essc.command.beezooka";
    private final PlayerLanguageManager langManager;
    private final BeezookaConfig config;
    private final CommandDataStorage dataStorage;
    private final Plugin plugin;

    public BeezookaCommand(PlayerLanguageManager langManager, BeezookaConfig config, CommandDataStorage dataStorage, Plugin plugin) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Component message = langManager.getMessageFor(null, "commands.beezooka.only-player",
                    "<red>Only players can use this command!");
            sender.sendMessage(message.toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            Component message = langManager.getMessageFor(player, "commands.beezooka.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION));
            player.sendMessage(message);
            return true;
        }

        if (args.length > 1) {
            Component message = langManager.getMessageFor(player, "commands.beezooka.usage",
                    "<red>Usage: <white>/beezooka [velocity]");
            player.sendMessage(message);
            return true;
        }

        double velocity = config.getVelocity();

        if (args.length == 1) {
            try {
                velocity = Double.parseDouble(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }

        double finalVelocity = velocity;
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bee bee = (Bee) player.getWorld().spawnEntity(
                    player.getEyeLocation().add(player.getLocation().getDirection()),
                    org.bukkit.entity.EntityType.BEE
            );

            Vector direction = player.getLocation().getDirection().multiply(finalVelocity);
            bee.setVelocity(direction);
            bee.setAnger(0);

            Component nameComponent = langManager.getMessageFor(player, "commands.beezooka.name",
                    "<yellow>Beezooka Bee!");
            bee.customName(nameComponent);
            bee.setCustomNameVisible(true);
        });

        Component message = langManager.getMessageFor(player, "commands.beezooka.fired",
                "<green>Beezooka fired with velocity {velocity}!",
                ComponentPlaceholder.of("{velocity}", String.valueOf(velocity)));
        player.sendMessage(message);

        trackUsage(player.getUniqueId(), "fired", 1);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("1", "2", "3", "4", "5");
        }
        return List.of();
    }

    private void trackUsage(UUID playerId, String type, int value) {
        dataStorage.getState(playerId, "beezooka", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "beezooka", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "beezooka", "last_type", type);
            dataStorage.setState(playerId, "beezooka", "last_used", String.valueOf(System.currentTimeMillis()));
        });
    }
}