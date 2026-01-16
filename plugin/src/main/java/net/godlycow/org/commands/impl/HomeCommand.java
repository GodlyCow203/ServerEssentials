package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.HomesConfig;
import net.godlycow.org.homes.gui.trigger.HomeGUIListener;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class HomeCommand implements CommandExecutor {
    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final HomeGUIListener guiListener;
    private final HomesConfig config;

    private static final String PERMISSION_BASE = "essc.command.homes";

    public HomeCommand(Plugin plugin, PlayerLanguageManager langManager,
                       HomeGUIListener guiListener, HomesConfig config) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.guiListener = guiListener;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "homes.only-player",
                    "<red>❌ Only players can use this command!"));
            return true;
        }

        if (!player.hasPermission(PERMISSION_BASE)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.no-permission",
                    "<red>❌ You need permission <yellow><permission></yellow>!",
                    net.godlycow.org.language.LanguageManager.ComponentPlaceholder.of("<permission>", PERMISSION_BASE)));
            return true;
        }

        if (config.isWorldDisabled(player.getWorld().getName())) {
            player.sendMessage(langManager.getMessageFor(player, "commands.homes.world-disabled",
                    "<red>❌ Home commands are disabled in this world!"));
            return true;
        }

        guiListener.openMainGUI(player);
        return true;
    }
}