package net.godlycow.org.commands.impl;

import net.godlycow.org.language.PlayerLanguageManager;
import net.godlycow.org.settings.SettingsConfig;
import net.godlycow.org.settings.SettingsGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class SettingsCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.settings";

    private final PlayerLanguageManager langManager;
    private final SettingsConfig config;
    private final SettingsGUI gui;

    public SettingsCommand(PlayerLanguageManager langManager, SettingsConfig config, SettingsGUI gui) {
        this.langManager = langManager;
        this.config = config;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.settings.only-player",
                    "<red>Only players can use this command!"));
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.settings.no-permission",
                    "<red>You don't have permission to use /settings!"));
            return true;
        }

        gui.open(player, 0);
        return true;
    }
}