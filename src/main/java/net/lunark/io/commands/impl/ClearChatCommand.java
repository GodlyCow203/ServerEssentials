package net.lunark.io.commands.impl;

import net.lunark.io.commands.config.ClearChatConfig;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import static net.lunark.io.language.LanguageManager.ComponentPlaceholder;

public final class ClearChatCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.clearchat";
    private final PlayerLanguageManager langManager;
    private final ClearChatConfig config;

    public ClearChatCommand(PlayerLanguageManager langManager, ClearChatConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            Player playerSender = (sender instanceof Player) ? (Player) sender : null;
            sender.sendMessage(langManager.getMessageFor(playerSender, "commands.clearchat.no-permission", "<red>You do not have permission to use this command!"));
            return true;
        }

        String clearedBy = (sender instanceof Player) ? ((Player) sender).getDisplayName() : "Console";

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (int i = 0; i < 150; i++) {
                player.sendMessage("");
            }
            player.sendMessage(langManager.getMessageFor(player, "commands.clearchat.cleared", "<gray>Chat has been cleared by <#ff0000>{player}</#ff0000>", ComponentPlaceholder.of("{player}", clearedBy)));
        }

        return true;
    }
}