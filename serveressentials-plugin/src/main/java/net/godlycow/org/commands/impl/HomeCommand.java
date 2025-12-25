package net.godlycow.org.commands.impl;

import net.godlycow.org.language.LanguageManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

    private static final String PERMISSION_BASE = "serveressentials.command.homes";
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacySection();


    public HomeCommand(Plugin plugin, PlayerLanguageManager langManager, HomeGUIListener guiListener) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.guiListener = guiListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(legacySerializer.serialize(
                    langManager.getMessageFor(null, "commands.homes.only-player", "<red>Only players can use this command!")
            ));
            return true;
        }

        if (!player.hasPermission(PERMISSION_BASE)) {
            player.sendMessage(legacySerializer.serialize(
                    langManager.getMessageFor(player, "commands.no-permission",
                            "<red>You need permission <yellow><permission></yellow>!",
                            LanguageManager.ComponentPlaceholder.of("<permission>", PERMISSION_BASE))
            ));
            return true;
        }

        guiListener.openMainGUI(player);
        return true;
    }
}