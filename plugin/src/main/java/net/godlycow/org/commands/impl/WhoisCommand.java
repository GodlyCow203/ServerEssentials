package net.godlycow.org.commands.impl;

import net.kyori.adventure.text.Component;
import net.godlycow.org.commands.config.WhoisConfig;
import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.language.LanguageManager.ComponentPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class WhoisCommand implements CommandExecutor {

    private static final String PERMISSION = "essc.command.whois";
    private final PlayerLanguageManager langManager;
    private final WhoisConfig config;
    private final CommandDataStorage dataStorage;
    private final Plugin plugin;

    public WhoisCommand(PlayerLanguageManager langManager, WhoisConfig config, CommandDataStorage dataStorage, Plugin plugin) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player senderPlayer = sender instanceof Player ? (Player) sender : null;

        if (!sender.hasPermission(PERMISSION)) {
            Component message = langManager.getMessageFor(senderPlayer, "commands.whois.no-permission",
                    "You need permission {permission}!",
                    ComponentPlaceholder.of("{permission}", PERMISSION));
            sender.sendMessage(message.toString());
            return true;
        }

        if (args.length != 1) {
            Component message = langManager.getMessageFor(senderPlayer, "commands.whois.usage",
                    "Usage: /whois <player>");
            sender.sendMessage(message.toString());
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            Component message = langManager.getMessageFor(senderPlayer, "commands.whois.target-not-found",
                    "Player not found.",
                    ComponentPlaceholder.of("{target}", args[0]));
            sender.sendMessage(message.toString());
            return true;
        }

        Component header = langManager.getMessageFor(senderPlayer, "commands.whois.info-header",
                "Player info:");
        sender.sendMessage(header.toString());

        Component nameMsg = langManager.getMessageFor(senderPlayer, "commands.whois.info-name",
                "Name: {name}",
                ComponentPlaceholder.of("{name}", target.getName()));
        sender.sendMessage(nameMsg.toString());

        Component healthMsg = langManager.getMessageFor(senderPlayer, "commands.whois.info-health",
                "Health: {health}",
                ComponentPlaceholder.of("{health}", String.valueOf(target.getHealth())));
        sender.sendMessage(healthMsg.toString());

        Component locMsg = langManager.getMessageFor(senderPlayer, "commands.whois.info-location",
                "Location: {location}",
                ComponentPlaceholder.of("{location}", target.getLocation().toVector().toString()));
        sender.sendMessage(locMsg.toString());

        trackUsage(target.getUniqueId(), "lookup", 1);

        return true;
    }

    private void trackUsage(UUID playerId, String type, int value) {
        dataStorage.getState(playerId, "whois", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "whois", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "whois", "last_type", type);
            dataStorage.setState(playerId, "whois", "last_used", String.valueOf(System.currentTimeMillis()));
        });
    }
}