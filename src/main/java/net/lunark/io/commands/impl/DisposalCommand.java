package net.lunark.io.commands.impl;

import net.lunark.io.commands.CommandDataStorage;
import net.lunark.io.commands.config.DisposalConfig;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

import static net.lunark.io.language.LanguageManager.ComponentPlaceholder;

public final class DisposalCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.disposal";

    private final PlayerLanguageManager langManager;
    private final DisposalConfig config;
    private final CommandDataStorage dataStorage;

    public DisposalCommand(PlayerLanguageManager langManager, DisposalConfig config, CommandDataStorage dataStorage) {
        this.langManager = langManager;
        this.config = config;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.disposal.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.disposal.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        String title = langManager.getMessageFor(player, "commands.disposal.inventory-title",
                "<red><bold>Disposal").toString();
        Inventory disposal = Bukkit.createInventory(null, 54, title);

        player.openInventory(disposal);
        player.sendMessage(langManager.getMessageFor(player, "commands.disposal.opened",
                "<green>Disposal opened. Place items here to delete them."));

        UUID playerId = player.getUniqueId();
        dataStorage.getState(playerId, "disposal", "usage_count").thenAccept(opt -> {
            int count = opt.map(Integer::parseInt).orElse(0);
            dataStorage.setState(playerId, "disposal", "usage_count", String.valueOf(count + 1));
            dataStorage.setState(playerId, "disposal", "last_used", String.valueOf(System.currentTimeMillis()));
        });

        return true;
    }
}