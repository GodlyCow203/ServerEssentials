package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.BurnConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class BurnCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.burn";
    private static final String PERMISSION_OTHERS = "serveressentials.command.burn.others";

    private final PlayerLanguageManager langManager;
    private final BurnConfig config;
    private final CommandDataStorage dataStorage;

    public BurnCommand(PlayerLanguageManager langManager, BurnConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Permission check
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.burn.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)).toString());
            return true;
        }

        // Usage check
        if (args.length < 1) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null, "commands.burn.usage",
                    "<red>Usage: <white>/burn <player> [seconds]"));
            return true;
        }

        // Find target player
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null, "commands.burn.player-not-found",
                    "<red>Player <yellow>{target}</yellow> not found!",
                    ComponentPlaceholder.of("{target}", args[0])).toString());
            return true;
        }

        // Can't burn yourself unless you have permission
        if (sender instanceof Player player && target.equals(player)) {
            if (!player.hasPermission(PERMISSION)) {
                player.sendMessage(langManager.getMessageFor(player, "commands.burn.no-permission",
                        "<red>You need permission <yellow>{permission}</yellow>!",
                        ComponentPlaceholder.of("{permission}", PERMISSION)));
                return true;
            }
        } else {
            // Burning others requires permissions
            if (!sender.hasPermission(PERMISSION_OTHERS)) {
                sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null, "commands.burn.no-permission-sub",
                        "<red>You need permission <yellow>{subpermission}</yellow>!",
                        ComponentPlaceholder.of("{subpermission}", PERMISSION_OTHERS)).toString());
                return true;
            }
        }

        // Check target game mode immunity
        if (target.getGameMode() == GameMode.CREATIVE || target.getGameMode() == GameMode.SPECTATOR) {
            sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null, "commands.burn.immune-gamemode",
                    "<red>{target} is in <yellow>{gamemode}</yellow> mode and is immune!",
                    ComponentPlaceholder.of("{target}", target.getName()),
                    ComponentPlaceholder.of("{gamemode}", target.getGameMode().name())).toString());
            return true;
        }

        // Parse duration
        int seconds = config.defaultDuration();
        if (args.length == 2) {
            try {
                seconds = Integer.parseInt(args[1]);
                if (seconds < 1 || seconds > config.maxDuration()) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(
                        langManager.getMessageFor(
                                sender instanceof Player ? (Player) sender : null,
                                "commands.burn.invalid-number",
                                "<red>Invalid! Use <yellow>1-{max}</yellow> seconds.",
                                ComponentPlaceholder.of("{max}", config.maxDuration())
                        )
                );
                return true;
            }

        }

        // Apply fire
        target.setFireTicks(seconds * 20);

        // Send messages
        if (sender instanceof Player attacker && target.equals(attacker)) {
            // Self-burn
            target.sendMessage(langManager.getMessageFor(target, "commands.burn.self-burned",
                    "<yellow>You set yourself on fire for <gold>{seconds} <yellow>seconds!",
                    ComponentPlaceholder.of("{seconds}", seconds)));
        } else {
            // Burn target
            target.sendMessage(langManager.getMessageFor(target, "commands.burn.target-burned",
                    "<red>You've been set on fire by <yellow>{attacker}</yellow>!",
                    ComponentPlaceholder.of("{attacker}", sender.getName())));

            sender.sendMessage(langManager.getMessageFor(sender instanceof Player ? (Player)sender : null, "commands.burn.sender-burned",
                    "<yellow>{target}</yellow><red> is now burning for <yellow>{seconds}</yellow> seconds!",
                    ComponentPlaceholder.of("{target}", target.getName()),
                    ComponentPlaceholder.of("{seconds}", seconds)));
        }

        // Track usage statistics (async)
        UUID executorId = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
        if (executorId != null) {
            trackUsage(executorId, target.getUniqueId(), seconds, target.equals(sender));
        }

        return true;
    }

    private void trackUsage(UUID executorId, UUID targetId, int seconds, boolean self) {
        dataStorage.getState(executorId, "burn", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(executorId, "burn", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(executorId, "burn", "last_target", targetId.toString());
            dataStorage.setState(executorId, "burn", "last_duration", String.valueOf(seconds));
            dataStorage.setState(executorId, "burn", "self_burn", String.valueOf(self));
        });
    }
}