package net.lunark.io.commands.impl;

import net.kyori.adventure.text.Component;
import net.lunark.io.commands.config.ThunderConfig;
import net.lunark.io.commands.CommandDataStorage;
import net.lunark.io.language.PlayerLanguageManager;
import net.lunark.io.language.LanguageManager.ComponentPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ThunderCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "serveressentials.command.thunder";
    private static final String PERMISSION_OTHERS = "serveressentials.command.thunder.others";
    private final PlayerLanguageManager langManager;
    private final ThunderConfig config;
    private final CommandDataStorage dataStorage;

    public ThunderCommand(PlayerLanguageManager langManager, ThunderConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            Component message = langManager.getMessageFor(null, "commands.thunder.only-player", "Only players can use this command!");
            sender.sendMessage(message.toString());
            return true;
        }

        Player target = player;
        boolean isSelf = true;

        if (args.length > 0) {
            Player found = Bukkit.getPlayerExact(args[0]);
            if (found != null) {
                target = found;
                isSelf = false;
            }
        }

        if (isSelf) {
            if (!player.hasPermission(PERMISSION)) {
                Component message = langManager.getMessageFor(player, "commands.thunder.no-permission", "You need permission {permission}!", ComponentPlaceholder.of("{permission}", PERMISSION));
                player.sendMessage(message);
                return true;
            }
        } else {
            if (!player.hasPermission(PERMISSION_OTHERS)) {
                Component message = langManager.getMessageFor(player, "commands.thunder.no-permission-others", "You need permission {permission}!", ComponentPlaceholder.of("{permission}", PERMISSION_OTHERS));
                player.sendMessage(message);
                return true;
            }
        }

        Location loc = target.getLocation();
        target.getWorld().strikeLightningEffect(loc);

        String targetName = isSelf ? langManager.getMessageFor(player, "commands.thunder.self-name", "yourself").toString() : target.getName();

        Component message = langManager.getMessageFor(player, "commands.thunder.struck", "Thunder effect summoned at {target}!", ComponentPlaceholder.of("{target}", targetName));
        player.sendMessage(message);

        trackUsage(player.getUniqueId(), isSelf ? "self" : "other", 1);

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private void trackUsage(UUID playerId, String type, int value) {
        dataStorage.getState(playerId, "thunder", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "thunder", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "thunder", "last_type", type);
            dataStorage.setState(playerId, "thunder", "last_used", String.valueOf(System.currentTimeMillis()));
        });
    }
}