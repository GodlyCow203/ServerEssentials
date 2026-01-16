package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.DayConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class DayCommand implements CommandExecutor {
    private static final String PERMISSION = "essc.command.day";

    private final PlayerLanguageManager langManager;
    private final DayConfig config;

    public DayCommand(PlayerLanguageManager langManager, DayConfig config) {
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.day.only-player",
                    "<red>Only players can use this command.").toString());
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.day.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    ComponentPlaceholder.of("{permission}", PERMISSION)));
            return true;
        }

        player.getWorld().setTime(1000);
        player.sendMessage(langManager.getMessageFor(player, "commands.day.success",
                "<green>Time set to day in world <yellow>{world}</yellow>.",
                ComponentPlaceholder.of("{world}", player.getWorld().getName())));

        return true;
    }
}