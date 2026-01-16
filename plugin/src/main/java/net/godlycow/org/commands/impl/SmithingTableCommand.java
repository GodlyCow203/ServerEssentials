package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.SmithingTableConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class SmithingTableCommand implements CommandExecutor {
    private static final String PERMISSION = "essc.command.smithingtable";

    private final PlayerLanguageManager langManager;
    private final SmithingTableConfig config;

    public SmithingTableCommand(PlayerLanguageManager langManager, SmithingTableConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.smithingtable.only-player",
                    "<red>Only players can use this command."));
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.smithingtable.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        player.openSmithingTable(null, true);
        player.sendMessage(langManager.getMessageFor(player, "commands.smithingtable.success",
                "<green>Opened smithing table."));
        return true;
    }
}