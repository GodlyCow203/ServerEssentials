package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AltsCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final File altsFile;
    private final FileConfiguration altsConfig;

    public AltsCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.altsFile = new File(plugin.getDataFolder(), "alts.yml");
        this.altsConfig = YamlConfiguration.loadConfiguration(altsFile);
    }

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    // Log IP (call this from a listener in Main)
    public void logIP(Player player) {
        String ip = player.getAddress().getAddress().getHostAddress();
        List<String> names = altsConfig.getStringList("IPs." + ip);
        if (!names.contains(player.getName())) {
            names.add(player.getName());
            altsConfig.set("IPs." + ip, names);
            try {
                altsConfig.save(altsFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = getPrefix();

        if (args.length != 1) {
            sender.sendMessage(prefix + ChatColor.RED + "Usage: /alts <player>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore()) {
            sender.sendMessage(prefix + ChatColor.RED + "Player not found.");
            return true;
        }

        // Find the IP that the target player used
        if (altsConfig.getConfigurationSection("IPs") == null) {
            sender.sendMessage(prefix + ChatColor.RED + "No alt data found.");
            return true;
        }

        String targetIP = null;

        for (String ip : altsConfig.getConfigurationSection("IPs").getKeys(false)) {
            List<String> names = altsConfig.getStringList("IPs." + ip);
            if (names.contains(target.getName())) {
                targetIP = ip;
                break;
            }
        }

        if (targetIP == null) {
            sender.sendMessage(prefix + ChatColor.RED + "No alt accounts found for that player.");
            return true;
        }

        List<String> altAccounts = altsConfig.getStringList("IPs." + targetIP);

        sender.sendMessage(prefix + ChatColor.YELLOW + "Accounts that have joined from the same IP (" + targetIP + "):");
        for (String name : altAccounts) {
            sender.sendMessage(ChatColor.GRAY + "- " + name);
        }

        return true;
    }
}
