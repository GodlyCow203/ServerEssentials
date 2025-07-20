package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ReportCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;

    public ReportCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "reports.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "This command can only be used in-game.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Usage: /report <player> <reason>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || target.getName() == null) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Player not found.");
            return true;
        }

        String reason = String.join(" ", args).substring(args[0].length()).trim();
        String message = ChatColor.RED + "[REPORT] " + ChatColor.GRAY + player.getName() +
                " reported " + target.getName() + " for: " + reason;

        // Save report for all OPs (for future login notice)
        for (OfflinePlayer op : Bukkit.getOperators()) {
            UUID uuid = op.getUniqueId();
            String path = "reports." + uuid + ".messages";
            var messages = config.getStringList(path);
            messages.add(message);
            config.set(path, messages);
        }

        // Notify online OPs
        Bukkit.getOnlinePlayers().stream()
                .filter(Player::isOp)
                .forEach(op -> op.sendMessage(message));

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.sendMessage(getPrefix() + ChatColor.GREEN + "Your report has been submitted.");
        return true;
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public File getFile() {
        return file;
    }
}

