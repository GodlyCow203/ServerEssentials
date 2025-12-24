package net.godlycow.org.commands.impl;

import net.kyori.adventure.text.Component;
import net.godlycow.org.commands.config.FireballConfig;
import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.language.LanguageManager.ComponentPlaceholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class FireballCommand implements CommandExecutor {

    private static final String PERMISSION = "serveressentials.command.fireball";
    private final PlayerLanguageManager langManager;
    private final FireballConfig config;
    private final CommandDataStorage dataStorage;

    public FireballCommand(PlayerLanguageManager langManager, FireballConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Component message = langManager.getMessageFor(null, "commands.fireball.only-player", "Only players can use this command!");
            sender.sendMessage(message.toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            Component message = langManager.getMessageFor(player, "commands.fireball.no-permission", "You need permission {permission}!", ComponentPlaceholder.of("{permission}", PERMISSION));
            player.sendMessage(message);
            return true;
        }

        player.launchProjectile(Fireball.class);

        Component message = langManager.getMessageFor(player, "commands.fireball.launched", "Fireball launched!");
        player.sendMessage(message);

        trackUsage(player.getUniqueId(), "self", 1);

        return true;
    }

    private void trackUsage(UUID playerId, String type, int value) {
        dataStorage.getState(playerId, "fireball", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "fireball", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "fireball", "last_type", type);
            dataStorage.setState(playerId, "fireball", "last_used", String.valueOf(System.currentTimeMillis()));
        });
    }
}