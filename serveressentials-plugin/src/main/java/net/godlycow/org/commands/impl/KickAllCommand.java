package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.KickAllConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class KickAllCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.kickall";
    private final PlayerLanguageManager langManager;
    private final KickAllConfig config;

    public KickAllCommand(PlayerLanguageManager langManager, KickAllConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            Player playerSender = (sender instanceof Player) ? (Player) sender : null;
            sender.sendMessage(langManager.getMessageFor(playerSender, "commands.kickall.no-permission", "<red>You need permission <yellow>{permission}</yellow>!", ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.kickall.usage", "<red>Usage: /kickall <reason>"));
            return true;
        }

        String reason = String.join(" ", args);
        int kickedCount = 0;

        Component kickMessage = langManager.getMessageFor(null, "commands.kickall.kick-message", "<red>You have been kicked by <yellow>{admin}</yellow>!\n<gray>Reason: <white>{reason}", ComponentPlaceholder.of("{admin}", sender.getName()), ComponentPlaceholder.of("{reason}", reason));

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(sender)) {
                p.kick(kickMessage);
                kickedCount++;
            }
        }

        Component senderMessage = langManager.getMessageFor(null, "commands.kickall.sender-message", "<green>Successfully kicked <yellow>{count}</yellow> players.\n<gray>Reason: <white>{reason}", ComponentPlaceholder.of("{count}", String.valueOf(kickedCount)), ComponentPlaceholder.of("{reason}", reason));
        sender.sendMessage(senderMessage);
        return true;
    }
}