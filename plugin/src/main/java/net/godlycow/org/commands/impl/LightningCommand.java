package net.godlycow.org.commands.impl;

import net.kyori.adventure.text.Component;
import net.godlycow.org.commands.config.LightningConfig;
import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.language.LanguageManager.ComponentPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class LightningCommand implements CommandExecutor {

    private static final String PERMISSION = "essc.command.lightning";
    private final PlayerLanguageManager langManager;
    private final LightningConfig config;
    private final CommandDataStorage dataStorage;
    private final Plugin plugin;

    public LightningCommand(PlayerLanguageManager langManager, LightningConfig config, CommandDataStorage dataStorage, Plugin plugin) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 2) {
            Component message = langManager.getMessageFor(sender instanceof Player ? (Player) sender : null,
                    "commands.lightning.usage",
                    "Usage: /lightning [player] [-s]");
            sender.sendMessage(message.toString());
            return true;
        }

        List<String> argv = new ArrayList<>(List.of(args));
        boolean silent = argv.remove("-s");
        String targetName = argv.isEmpty() ? null : argv.get(0);

        Player target;
        boolean isSelf = false;

        if (targetName == null) {
            if (!(sender instanceof Player player)) {
                Component message = langManager.getMessageFor(null, "commands.lightning.only-player",
                        "Only players can use this command!");
                sender.sendMessage(message.toString());
                return true;
            }
            target = player;
            isSelf = true;
        } else {
            target = sender.getServer().getPlayer(targetName);
            if (target == null || !target.isOnline()) {
                Component message = langManager.getMessageFor(sender instanceof Player ? (Player) sender : null,
                        "commands.lightning.target-not-found",
                        "Player not found.",
                        ComponentPlaceholder.of("{target}", targetName));
                sender.sendMessage(message.toString());
                return true;
            }
        }

        if (!sender.hasPermission(PERMISSION)) {
            Component message = langManager.getMessageFor(sender instanceof Player ? (Player) sender : null,
                    "commands.lightning.no-permission",
                    "You need permission {permission}!",
                    ComponentPlaceholder.of("{permission}", PERMISSION));
            sender.sendMessage(message.toString());
            return true;
        }

        Location loc = target.getLocation();
        World world = loc.getWorld();
        if (world == null) return true;

        Bukkit.getScheduler().runTask(plugin, () -> world.strikeLightning(loc));

        if (!silent) {
            String messageKey = isSelf ? "commands.lightning.struck-self" : "commands.lightning.struck-other";
            Component message = langManager.getMessageFor(sender instanceof Player ? (Player) sender : null,
                    messageKey,
                    isSelf ? "Lightning struck!" : "Lightning struck {target}!",
                    ComponentPlaceholder.of("{target}", target.getName()));
            sender.sendMessage(message.toString());
        }

        trackUsage(target.getUniqueId(), isSelf ? "self" : "other", 1);

        return true;
    }

    private void trackUsage(UUID playerId, String type, int value) {
        dataStorage.getState(playerId, "lightning", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "lightning", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "lightning", "last_type", type);
            dataStorage.setState(playerId, "lightning", "last_used", String.valueOf(System.currentTimeMillis()));
        });
    }
}