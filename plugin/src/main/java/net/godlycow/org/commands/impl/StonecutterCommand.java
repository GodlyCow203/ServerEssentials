package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.CommandDataStorage;
import net.godlycow.org.commands.config.StonecutterConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class StonecutterCommand implements CommandExecutor {
    private static final String PERMISSION = "essc.command.stonecutter";

    private final PlayerLanguageManager langManager;
    private final StonecutterConfig config;
    private final CommandDataStorage dataStorage;
    private final Plugin plugin;

    public StonecutterCommand(PlayerLanguageManager langManager, StonecutterConfig config, CommandDataStorage dataStorage, Plugin plugin) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.stonecutter.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.stonecutter.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        player.openStonecutter(null, true);

        player.sendMessage(langManager.getMessageFor(player, "commands.stonecutter.opened",
                "<green>Stonecutter opened!"));

        UUID playerId = player.getUniqueId();
        dataStorage.getState(playerId, "stonecutter", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "stonecutter", "usage_count", String.valueOf(count + 1));
        });

        return true;
    }
}