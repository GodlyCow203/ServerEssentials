package net.lunark.io.commands;

import net.lunark.io.language.LanguageManager;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Stream;

public class LanguageCommand implements CommandExecutor, TabCompleter {
    private final LanguageManager languageManager;
    private final PlayerLanguageManager playerLanguageManager;

    public LanguageCommand(LanguageManager languageManager, PlayerLanguageManager playerLanguageManager) {
        this.languageManager = languageManager;
        this.playerLanguageManager = playerLanguageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(playerLanguageManager.getMessageFor(player, "language.usage",
                    "<yellow>Usage: /language <language>"));
            return true;
        }

        String targetLang = args[0].toLowerCase();
        if (!languageManager.hasLanguage(targetLang)) {
            player.sendMessage(playerLanguageManager.getMessageFor(player, "language.invalid",
                    "<red>Invalid language: <white>{lang}</white>",
                    LanguageManager.ComponentPlaceholder.of("{lang}", targetLang)));
            return true;
        }

        playerLanguageManager.setPlayerLanguage(player.getUniqueId(), targetLang);

        player.sendMessage(playerLanguageManager.getMessageFor(player, "language.changed",
                "<green>Language changed to: <white>{lang}</white>",
                LanguageManager.ComponentPlaceholder.of("{lang}", targetLang)));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return languageManager.getAvailableLanguages().stream()
                    .filter(lang -> lang.startsWith(input))
                    .toList();
        }
        return List.of();
    }
}