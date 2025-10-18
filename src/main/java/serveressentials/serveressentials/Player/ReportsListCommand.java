package serveressentials.serveressentials.Player;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.PlayerMessages;

import java.io.File;
import java.util.Set;
import java.util.UUID;

public class ReportsListCommand implements CommandExecutor {

    private final ServerEssentials plugin;
    private final PlayerMessages messages;

    private final File file;
    private final FileConfiguration config;

    public ReportsListCommand(ServerEssentials plugin, PlayerMessages messages) {
        this.plugin = plugin;
        this.messages = messages;

        this.file = new File(plugin.getDataFolder() + File.separator + "storage", "reports.yml");
        this.config = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender.isOp() || sender.hasPermission("serveressentials.report.admin"))) {
            sender.sendMessage(messages.get("report.no-permission"));
            return true;
        }

        if (!config.contains("reports") || config.getConfigurationSection("reports").getKeys(false).isEmpty()) {
            sender.sendMessage(messages.get("reports.none"));
            return true;
        }

        sender.sendMessage(messages.get("reports.header"));

        Set<String> keys = config.getConfigurationSection("reports").getKeys(false);
        for (String id : keys) {
            String reporterUUID = config.getString("reports." + id + ".reporter");
            String targetUUID = config.getString("reports." + id + ".target");
            String reason = config.getString("reports." + id + ".reason");

            OfflinePlayer reporter = Bukkit.getOfflinePlayer(UUID.fromString(reporterUUID));
            OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(targetUUID));

            Component line = messages.get("reports.entry",
                    "{id}", id,
                    "{reporter}", reporter.getName() != null ? reporter.getName() : "Unknown",
                    "{target}", target.getName() != null ? target.getName() : "Unknown",
                    "{reason}", reason
            );
            sender.sendMessage(line);
        }

        sender.sendMessage(messages.get("reports.footer"));

        return true;
    }
}
