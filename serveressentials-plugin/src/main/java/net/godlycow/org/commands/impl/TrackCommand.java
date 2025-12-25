package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.TrackConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class TrackCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.track";

    private final PlayerLanguageManager langManager;
    private final TrackConfig config;
    private final CommandDataStorage dataStorage;

    public TrackCommand(PlayerLanguageManager langManager, TrackConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.track.only-players",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.track.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(langManager.getMessageFor(player, "commands.track.usage",
                    "<red>Usage: <white>/track <player>"));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(langManager.getMessageFor(player, "commands.track.not-found",
                    "<red>Player <yellow>{target}</yellow> not found.",
                    ComponentPlaceholder.of("{target}", args[0])));
            return true;
        }

        // Set compass target
        Location targetLoc = target.getLocation();
        player.setCompassTarget(targetLoc);

        player.sendMessage(langManager.getMessageFor(player, "commands.track.success",
                "<green>Now tracking <yellow>{target}</yellow>. Compass updated.",
                ComponentPlaceholder.of("{target}", target.getName())));

        // Store tracking data (async)
        UUID playerId = player.getUniqueId();
        String trackingData = target.getUniqueId() + ":" + System.currentTimeMillis();
        dataStorage.setState(playerId, "track", "last_target", trackingData).thenAccept(v -> {
            // Also update usage count
            dataStorage.getState(playerId, "track", "usage_count").thenAccept(opt -> {
                int count = opt.map(Integer::parseInt).orElse(0);
                dataStorage.setState(playerId, "track", "usage_count", String.valueOf(count + 1));
            });
        });

        return true;
    }
}