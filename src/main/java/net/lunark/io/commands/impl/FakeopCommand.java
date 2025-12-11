package net.lunark.io.commands.impl;

import net.kyori.adventure.text.Component;
import net.lunark.io.commands.config.FakeopConfig;
import net.lunark.io.commands.CommandDataStorage;
import net.lunark.io.language.LanguageManager;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class FakeopCommand implements CommandExecutor {

    private static final String PERMISSION = "serveressentials.command.fakeop";
    private final PlayerLanguageManager langManager;
    private final FakeopConfig config;
    private final CommandDataStorage dataStorage;

    public FakeopCommand(PlayerLanguageManager langManager, FakeopConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            Component message = langManager.getMessageFor(sender instanceof Player ? (Player) sender : null,
                    "commands.fakeop.no-permission",
                    "You need permission {permission}!",
                    LanguageManager.ComponentPlaceholder.of("{permission}", PERMISSION));
            sender.sendMessage(message.toString());
            return true;
        }

        if (args.length != 1) {
            Component message = langManager.getMessageFor(sender instanceof Player ? (Player) sender : null,
                    "commands.fakeop.usage",
                    "Usage: /fakeop <player>");
            sender.sendMessage(message.toString());
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            Component message = langManager.getMessageFor(sender instanceof Player ? (Player) sender : null,
                    "commands.fakeop.target-not-found",
                    "Player not found.",
                    LanguageManager.ComponentPlaceholder.of("{target}", args[0]));
            sender.sendMessage(message.toString());
            return true;
        }

        Component fakeMessage = langManager.getMessageFor(null, "commands.fakeop.fake-message",
                "[Server: Made {target} a server operator]",
                LanguageManager.ComponentPlaceholder.of("{target}", target.getName()));

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(fakeMessage);
        }

        Component confirmMessage = langManager.getMessageFor(sender instanceof Player ? (Player) sender : null,
                "commands.fakeop.success",
                "Fake OP sent to {target}",
                LanguageManager.ComponentPlaceholder.of("{target}", target.getName()));
        sender.sendMessage(confirmMessage.toString());

        trackUsage(target.getUniqueId(), "fakeop", 1);

        return true;
    }

    private void trackUsage(UUID playerId, String type, int value) {
        dataStorage.getState(playerId, "fakeop", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "fakeop", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "fakeop", "last_type", type);
            dataStorage.setState(playerId, "fakeop", "last_used", String.valueOf(System.currentTimeMillis()));
        });
    }
}