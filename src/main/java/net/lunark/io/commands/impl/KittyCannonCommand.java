package net.lunark.io.commands.impl;

import net.kyori.adventure.text.Component;
import net.lunark.io.commands.config.KittyCannonConfig;
import net.lunark.io.commands.CommandDataStorage;
import net.lunark.io.language.PlayerLanguageManager;
import net.lunark.io.language.LanguageManager.ComponentPlaceholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public final class KittyCannonCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "serveressentials.command.kittycannon";
    private final PlayerLanguageManager langManager;
    private final KittyCannonConfig config;
    private final CommandDataStorage dataStorage;

    public KittyCannonCommand(PlayerLanguageManager langManager, KittyCannonConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Component message = langManager.getMessageFor(null, "commands.kittycannon.only-player",
                    "<red>Only players can use this command!");
            sender.sendMessage(message.toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            Component message = langManager.getMessageFor(player, "commands.kittycannon.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION));
            player.sendMessage(message);
            return true;
        }

        if (args.length > 1) {
            Component message = langManager.getMessageFor(player, "commands.kittycannon.usage",
                    "<red>Usage: <white>/kittycannon [velocity]");
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

        LivingEntity kitty = (LivingEntity) player.getWorld().spawnEntity(
                player.getEyeLocation().add(player.getLocation().getDirection()),
                EntityType.CAT
        );

        Vector direction = player.getLocation().getDirection().multiply(velocity);
        kitty.setVelocity(direction);

        Component nameComponent = langManager.getMessageFor(player, "commands.kittycannon.name",
                "<light_purple>Kitty Cannon!");
        kitty.customName(nameComponent);
        kitty.setCustomNameVisible(true);

        Component message = langManager.getMessageFor(player, "commands.kittycannon.fired",
                "<green>Kitty cannon fired with velocity {velocity}!",
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
        dataStorage.getState(playerId, "kittycannon", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "kittycannon", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "kittycannon", "last_type", type);
            dataStorage.setState(playerId, "kittycannon", "last_used", String.valueOf(System.currentTimeMillis()));
        });
    }
}