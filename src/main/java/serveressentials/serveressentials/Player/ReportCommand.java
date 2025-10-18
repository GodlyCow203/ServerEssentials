package serveressentials.serveressentials.Player;


import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.PlayerMessages;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ReportCommand implements CommandExecutor, TabExecutor {

    private final ServerEssentials plugin;
    private final PlayerMessages messages;

    private File file;
    private FileConfiguration config;

    public ReportCommand(ServerEssentials plugin, PlayerMessages messages) {
        this.plugin = plugin;
        this.messages = messages;
        loadReports();
    }

    private void loadReports() {
        file = new File(plugin.getDataFolder() + File.separator + "storage", "reports.yml");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    private void saveReports() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("report")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(messages.get("report.usage", "{usage}", "/report <player> <reason>"));
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
                player.sendMessage(messages.get("report.invalid-player", "{player}", args[0]));
                return true;
            }

            String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

            // Generate ID
            String id = UUID.randomUUID().toString().substring(0, 8);

            config.set("reports." + id + ".reporter", player.getUniqueId().toString());
            config.set("reports." + id + ".target", target.getUniqueId().toString());
            config.set("reports." + id + ".reason", reason);
            config.set("reports." + id + ".timestamp", System.currentTimeMillis());
            saveReports();

            player.sendMessage(messages.get("report.submitted", "{player}", target.getName(), "{reason}", reason));

            // Notify online staff
            Component notifyMsg = messages.get("report.notify", "{id}", id, "{reporter}", player.getName(), "{target}", target.getName(), "{reason}", reason);
            boolean notified = false;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.isOp() || p.hasPermission("serveressentials.report.admin")) {
                    p.sendMessage(notifyMsg);
                    notified = true;
                }
            }

            // Store pending notification if no staff online
            if (!notified) {
                config.set("pending." + id, true);
                saveReports();
            }

            return true;
        }

        if (command.getName().equalsIgnoreCase("reportclear")) {
            if (!(sender.isOp() || sender.hasPermission("serveressentials.report.admin"))) {
                sender.sendMessage(messages.get("report.no-permission"));
                return true;
            }

            if (args.length < 1) {
                sender.sendMessage(messages.get("reportclear.usage", "{usage}", "/reportclear <id>"));
                return true;
            }

            String id = args[0];
            if (config.contains("reports." + id)) {
                config.set("reports." + id, null);
                saveReports();
                sender.sendMessage(messages.get("reportclear.cleared", "{id}", id));
            } else {
                sender.sendMessage(messages.get("reportclear.invalid", "{id}", id));
            }

            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("report")) {
            if (args.length == 1) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            }
        }

        if (command.getName().equalsIgnoreCase("reportclear")) {
            if (args.length == 1) {
                if (config.contains("reports")) {
                    return new ArrayList<>(config.getConfigurationSection("reports").getKeys(false));
                }
            }
        }

        return Collections.emptyList();
    }

    public void handleJoin(Player player) {
        if (player.isOp() || player.hasPermission("serveressentials.report.admin")) {
            if (config.contains("pending")) {
                for (String id : config.getConfigurationSection("pending").getKeys(false)) {
                    String targetUUID = config.getString("reports." + id + ".target");
                    String reporterUUID = config.getString("reports." + id + ".reporter");
                    String reason = config.getString("reports." + id + ".reason");

                    OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(targetUUID));
                    OfflinePlayer reporter = Bukkit.getOfflinePlayer(UUID.fromString(reporterUUID));

                    player.sendMessage(messages.get("report.notify", "{id}", id, "{reporter}", reporter.getName(), "{target}", target.getName(), "{reason}", reason));
                }
                config.set("pending", null);
                saveReports();
            }
        }
    }
}
