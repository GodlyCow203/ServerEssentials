package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.FeedConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class FeedCommand implements CommandExecutor {
    private static final String PERMISSION = "essc.command.feed";
    private static final String PERMISSION_OTHERS = "essc.command.feed.others";

    private final PlayerLanguageManager langManager;
    private final FeedConfig config;
    private final CommandDataStorage dataStorage;

    public FeedCommand(PlayerLanguageManager langManager, FeedConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.feed.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.feed.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        Player target;

        if (args.length == 0) {
            target = player;
        } else if (args.length == 1) {
            if (!player.hasPermission(PERMISSION_OTHERS)) {
                player.sendMessage(langManager.getMessageFor(player, "commands.feed.no-permission-others",
                        "<red>You need permission <yellow>{permission}</yellow>!",
                        ComponentPlaceholder.of("{permission}", PERMISSION_OTHERS)));
                return true;
            }

            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                player.sendMessage(langManager.getMessageFor(player, "commands.feed.player-not-found",
                        "<red>Player <yellow>{target}</yellow> not found.",
                        ComponentPlaceholder.of("{target}", args[0])));
                return true;
            }
        } else {
            player.sendMessage(langManager.getMessageFor(player, "commands.feed.usage",
                    "<red>Usage: <white>/feed [player]"));
            return true;
        }

        target.setFoodLevel(config.foodLevel());
        target.setSaturation(config.saturation());

        if (target.equals(player)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.feed.fed-self",
                    "<green>✓ You have been fed to full!"));
        } else {
            player.sendMessage(langManager.getMessageFor(player, "commands.feed.fed-other",
                    "<green>✓ You fed <yellow>{target}</yellow> to full!",
                    ComponentPlaceholder.of("{target}", target.getName())));

            target.sendMessage(langManager.getMessageFor(target, "commands.feed.fed-by-other",
                    "<green>✓ You were fed to full by <yellow>{sender}</yellow>!",
                    ComponentPlaceholder.of("{sender}", player.getName())));
        }

        UUID playerId = player.getUniqueId();
        dataStorage.getState(playerId, "feed", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "feed", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "feed", "last_type", target.equals(player) ? "self" : "other");
            dataStorage.setState(playerId, "feed", "last_target", target.getUniqueId().toString());
        });

        return true;
    }
}