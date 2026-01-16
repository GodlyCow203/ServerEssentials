package net.godlycow.org.commands.impl;

import net.godlycow.org.daily.DailyConfig;
import net.godlycow.org.daily.trigger.DailyListener;
import net.godlycow.org.daily.storage.DailyStorage;
import net.kyori.adventure.text.Component;
import net.godlycow.org.language.LanguageManager;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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
            Component message = langManager.getMessageFor(null, "commands.daily.only-player",
                    "<#FF2424>Only players can use this command!");
            plugin.getServer().sendMessage(message);
            return true;
        }

        if (!player.hasPermission("essc.command.daily")) {
            Component message = langManager.getMessageFor(player, "commands.daily.no-permission",
                    "<#FF2424>You need permission <#c0f0ff>{permission}<#FF2424>!",
                    LanguageManager.ComponentPlaceholder.of("{permission}", "essc.command.daily"));
            player.sendMessage(message);
            return true;
        }

        listener.openRewardsGUI(player, 1);
        return true;
    }
}