package net.godlycow.org.commands;

import net.godlycow.org.language.LanguageManager;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LanguageCommand implements CommandExecutor, TabCompleter {
    private final LanguageManager languageManager;
    private final PlayerLanguageManager playerLanguageManager;

    public LanguageCommand(LanguageManager languageManager, PlayerLanguageManager playerLanguageManager) {
        this.languageManager = languageManager;
        this.playerLanguageManager = playerLanguageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("essc.command.language.reload")) {
                sender.sendMessage("§cYou don't have permission to reload languages.");
                return true;
            }

            sender.sendMessage("Reloading language files...");
            languageManager.reloadLanguages();

            if (sender instanceof Player player) {
                player.sendMessage(playerLanguageManager.getMessageFor(player, "language.reload.complete",
                        "<green>✓ Successfully reloaded <white>{count}</white> languages!",
                        LanguageManager.ComponentPlaceholder.of("{count}",
                                String.valueOf(languageManager.getAvailableLanguages().size()))));
            } else {
                sender.sendMessage("§a✓ Successfully reloaded " +
                        languageManager.getAvailableLanguages().size() + " languages!");
            }
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cUsage: /language reload");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(playerLanguageManager.getMessageFor(player, "language.usage",
                    "<yellow>Usage: <white>/language <language></white> or <white>/language reload</white>"));
            return true;
        }

        String targetLang = args[0].toLowerCase();
        if (!languageManager.hasLanguage(targetLang)) {
            player.sendMessage(playerLanguageManager.getMessageFor(player, "language.invalid",
                    "<red>✗ Invalid language: <white>{lang}</white>",
                    LanguageManager.ComponentPlaceholder.of("{lang}", targetLang)));

            player.sendMessage(playerLanguageManager.getMessageFor(player, "language.available",
                    "<yellow>Available languages: <white>{langs}</white>",
                    LanguageManager.ComponentPlaceholder.of("{langs}",
                            String.join(", ", languageManager.getAvailableLanguages()))));
            return true;
        }

        playerLanguageManager.setPlayerLanguage(player.getUniqueId(), targetLang);

        player.sendMessage(playerLanguageManager.getMessageFor(player, "language.changed",
                "<green>✓ Language changed to: <white>{lang}</white>",
                LanguageManager.ComponentPlaceholder.of("{lang}", targetLang)));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();

            if (sender.hasPermission("godlycow.language.reload") && "reload".startsWith(input)) {
                completions.add("reload");
            }

            languageManager.getAvailableLanguages().stream()
                    .filter(lang -> lang.startsWith(input))
                    .forEach(completions::add);

            return completions;
        }
        return List.of();
    }
}