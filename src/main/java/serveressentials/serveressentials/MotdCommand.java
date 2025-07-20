package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MotdCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public MotdCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        File motdFile = new File(plugin.getDataFolder(), "motd.yml");

        if (!motdFile.exists()) {
            if (plugin.getResource("motd.yml") != null) {
                plugin.saveResource("motd.yml", false);
                sender.sendMessage(getPrefix() + ChatColor.YELLOW + "Default motd.yml created.");
            } else {
                sender.sendMessage(getPrefix() + ChatColor.RED + "Error: motd.yml is missing from both plugin folder and JAR resources.");
                return true;
            }
        }

        FileConfiguration motdConfig = YamlConfiguration.loadConfiguration(motdFile);
        List<String> lines = motdConfig.getStringList("motd");

        if (lines == null || lines.isEmpty()) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Your motd.yml is empty or malformed.");
            return true;
        }

        sender.sendMessage(getPrefix() + ChatColor.GREEN + "MOTD reloaded from motd.yml and will be shown on ping.");
        return true;
    }

    public static String parseHexColor(String line) {
        Pattern hexPattern = Pattern.compile("<#([A-Fa-f0-9]{6})>");
        Matcher matcher = hexPattern.matcher(line);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hexCode.toCharArray()) {
                replacement.append("§").append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }

        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}
