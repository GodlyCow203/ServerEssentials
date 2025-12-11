package net.lunark.io.daily;

import net.kyori.adventure.text.Component;

import net.lunark.io.language.LanguageManager;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class DailyCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final PlayerLanguageManager langManager;
    private final DailyStorage storage;
    private final DailyConfig config;
    private final DailyListener listener;

    public DailyCommand(JavaPlugin plugin, PlayerLanguageManager langManager,
                        DailyStorage storage, DailyConfig config, DailyListener listener) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.storage = storage;
        this.config = config;
        this.listener = listener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            String message = langManager.getMessageFor(null, "daily.command.only-player",
                    "<red>Only players can use this command!").toString();
            sender.sendMessage(message);
            return true;
        }

        if (!player.hasPermission("serveressentials.command.daily")) {
            Component message = langManager.getMessageFor(player, "daily.command.no-permission",
                    "<red>You need permission <yellow>{permission}</yellow>!",
                    LanguageManager.ComponentPlaceholder.of("{permission}", "serveressentials.daily"));
            player.sendMessage(message);
            return true;
        }

        listener.openRewardsGUI(player, 1);
        return true;
    }
}