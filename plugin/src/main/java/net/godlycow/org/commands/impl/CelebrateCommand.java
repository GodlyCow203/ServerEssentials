package net.godlycow.org.commands.impl;

import net.kyori.adventure.text.Component;
import net.godlycow.org.commands.config.CelebrateConfig;
import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.language.LanguageManager.ComponentPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class CelebrateCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "serveressentials.command.celebrate";
    private static final String PERMISSION_OTHERS = "serveressentials.command.celebrate.others";
    private final PlayerLanguageManager langManager;
    private final CelebrateConfig config;
    private final CommandDataStorage dataStorage;
    private final Plugin plugin;

    public CelebrateCommand(PlayerLanguageManager langManager, CelebrateConfig config, CommandDataStorage dataStorage, Plugin plugin) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 2) {
            Component message = langManager.getMessageFor(sender instanceof Player ? (Player) sender : null, "commands.celebrate.usage", "Usage: /celebrate [player] [-s]");
            sender.sendMessage(message);
            return true;
        }

        boolean silent = false;
        String targetName = null;
        for (String arg : args) {
            if ("-s".equalsIgnoreCase(arg)) {
                silent = true;
            } else if (targetName == null) {
                targetName = arg;
            }
        }

        Player target;
        boolean isSelf = false;

        if (targetName == null) {
            if (!(sender instanceof Player player)) {
                Component message = langManager.getMessageFor(null, "commands.celebrate.only-player", "Only players can use this command!");
                sender.sendMessage(message);
                return true;
            }
            target = player;
            isSelf = true;
        } else {
            target = Bukkit.getPlayerExact(targetName);
            if (target == null || !target.isOnline()) {
                Component message = langManager.getMessageFor(sender instanceof Player ? (Player) sender : null, "commands.celebrate.target-not-found", "Player not found.", ComponentPlaceholder.of("{target}", targetName));
                sender.sendMessage(message);
                return true;
            }
        }

        if (isSelf) {
            if (!sender.hasPermission(PERMISSION)) {
                Component message = langManager.getMessageFor(target, "commands.celebrate.no-permission", "You need permission {permission}!", ComponentPlaceholder.of("{permission}", PERMISSION));
                target.sendMessage(message);
                return true;
            }
        } else {
            if (!sender.hasPermission(PERMISSION_OTHERS)) {
                Component message = langManager.getMessageFor(sender instanceof Player ? (Player) sender : null, "commands.celebrate.no-permission-others", "You need permission {permission}!", ComponentPlaceholder.of("{permission}", PERMISSION_OTHERS));
                sender.sendMessage(message);
                return true;
            }
        }

        Location loc = target.getLocation();
        World world = loc.getWorld();
        if (world == null) return true;

        FireworkEffect effect = config.getEffect();

        Bukkit.getScheduler().runTask(plugin, () -> {
            Firework fw = (Firework) world.spawnEntity(loc, EntityType.FIREWORK_ROCKET);
            FireworkMeta meta = fw.getFireworkMeta();
            meta.addEffect(effect);
            meta.setPower(1);
            fw.setFireworkMeta(meta);
        });

        if (!silent) {
            String targetNameForMessage = isSelf ? langManager.getMessageFor(target, "commands.celebrate.self-name", "yourself").toString() : target.getName();
            String messageKey = isSelf ? "commands.celebrate.message" : "commands.celebrate.message-other";
            Component message = langManager.getMessageFor(
                    sender instanceof Player ? (Player) sender : null,
                    messageKey,
                    "Celebration!",
                    ComponentPlaceholder.of("{target}", targetNameForMessage)
            );
            sender.sendMessage(message);
        }

        trackUsage(target.getUniqueId(), isSelf ? "self" : "other", 1);

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
        if (args.length == 2 && "-s".startsWith(args[1].toLowerCase())) {
            return List.of("-s");
        }
        return List.of();
    }

    private void trackUsage(UUID playerId, String type, int value) {
        dataStorage.getState(playerId, "celebrate", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "celebrate", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "celebrate", "last_type", type);
            dataStorage.setState(playerId, "celebrate", "last_used", String.valueOf(System.currentTimeMillis()));
        });
    }
}