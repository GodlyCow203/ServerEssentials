package net.lunark.io.commands.impl;

import net.lunark.io.commands.config.UptimeConfig;
import net.lunark.io.language.PlayerLanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import static net.lunark.io.language.LanguageManager.ComponentPlaceholder;

public final class UptimeCommand implements CommandExecutor {
    private static final String PERMISSION = "serveressentials.command.uptime";

    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final UptimeConfig config;
    private final long serverStartTime;

    public UptimeCommand(Plugin plugin, long serverStartTime, PlayerLanguageManager langManager, UptimeConfig config) {
        this.plugin = plugin;
        this.serverStartTime = serverStartTime;
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;

        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(langManager.getMessageFor(player, "commands.uptime.no-permission",
                    "<red>You do not have permission to use this command!"));
            return true;
        }

        long currentTime = System.currentTimeMillis();
        long uptimeMillis = currentTime - serverStartTime;

        long seconds = uptimeMillis / 1000 % 60;
        long minutes = uptimeMillis / (1000 * 60) % 60;
        long hours = uptimeMillis / (1000 * 60 * 60) % 24;
        long days = uptimeMillis / (1000 * 60 * 60 * 24);

        String uptimeStr = String.format("%d days, %d hours, %d minutes, %d seconds", days, hours, minutes, seconds);

        sender.sendMessage(langManager.getMessageFor(player, "commands.uptime.info",
                "<green>Server uptime: <white>{uptime}",
                ComponentPlaceholder.of("{uptime}", uptimeStr)));

        return true;
    }
}