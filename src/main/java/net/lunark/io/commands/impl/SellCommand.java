package net.lunark.io.commands.impl;

import net.lunark.io.commands.config.SellConfig;
import net.lunark.io.sellgui.SellGUIManager;
import net.lunark.io.language.PlayerLanguageManager;
import net.lunark.io.language.LanguageManager.ComponentPlaceholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SellCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.sell";
    private static final String COMMAND_NAME = "sell";

    private final PlayerLanguageManager langManager;
    private final SellConfig config;
    private final SellGUIManager guiManager;

    public SellCommand(PlayerLanguageManager langManager, SellConfig config, SellGUIManager guiManager) {
        this.langManager = langManager;
        this.config = config;
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null,
                    "commands." + COMMAND_NAME + ".only-player",
                    "<red>This command can only be used by players!").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player,
                    "commands." + COMMAND_NAME + ".no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (!config.enabled) {
            player.sendMessage(langManager.getMessageFor(player, "economy.sellgui.disabled",
                    "<red>The sell GUI is currently disabled."));
            return true;
        }

        guiManager.openSellGUI(player);
        return true;
    }
}