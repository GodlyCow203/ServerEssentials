package net.lunark.io.commands.impl;

import net.lunark.io.commands.CommandDataStorage;
import net.lunark.io.commands.config.AnvilConfig;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

import static net.lunark.io.language.LanguageManager.ComponentPlaceholder;

public final class AnvilCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.anvil";

    private final PlayerLanguageManager langManager;
    private final AnvilConfig config;
    private final CommandDataStorage dataStorage;
    private final Plugin plugin;

    public AnvilCommand(PlayerLanguageManager langManager, AnvilConfig config, CommandDataStorage dataStorage, Plugin plugin) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.anvil.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.anvil.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        player.openAnvil(null, true);

        player.sendMessage(langManager.getMessageFor(player, "commands.anvil.opened",
                "<green>Anvil opened!"));

        UUID playerId = player.getUniqueId();
        dataStorage.getState(playerId, "anvil", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "anvil", "usage_count", String.valueOf(count + 1));
        });

        return true;
    }
}