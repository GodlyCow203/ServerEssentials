package serveressentials.serveressentials.serverEssentials;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import serveressentials.serveressentials.Managers.ReloadManager;
import serveressentials.serveressentials.ServerEssentials;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

public class ServerEssentialsCommand implements CommandExecutor {

    private final Plugin plugin;
    private final String PREFIX = hex("#00E5FF") + "[ServerEssentials] §r";
    private final Map<String, Consumer<CommandSender>> subCommands = new HashMap<>();

    public ServerEssentialsCommand(Plugin plugin) {
        this.plugin = plugin;

        // Register subcommands
        subCommands.put("info", this::showInfo);
        subCommands.put("version", this::showVersion);
        subCommands.put("reload", this::reloadPlugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendMessage(sender, "#FF5555", "Unknown subcommand. Use /" + label + " <info|version|reload>");
            return true;
        }

        Consumer<CommandSender> sub = subCommands.get(args[0].toLowerCase());
        if (sub == null) {
            sendMessage(sender, "#FF5555", "Unknown subcommand. Use /" + label + " <info|version|reload>");
            return true;
        }

        sub.accept(sender);
        return true;
    }

    private void showInfo(CommandSender sender) {
        sendMessage(sender, "#00FF7F", "ServerEssentials: Modern All-in-One Minecraft Plugin");
        sendMessage(sender, "#AAAAAA", "• RTP GUI");
        sendMessage(sender, "#AAAAAA", "• Home System");
        sendMessage(sender, "#AAAAAA", "• TPA System");
        sendMessage(sender, "#AAAAAA", "• Auction House");
        sendMessage(sender, "#AAAAAA", "• Shop");
        sendMessage(sender, "#AAAAAA", "• Admin Commands");
        sendMessage(sender, "#AAAAAA", "• Ranks");
        sendMessage(sender, "#AAAAAA", "• Join/Leave Messages");
        sendMessage(sender, "#AAAAAA", "• Warp System");
        sendMessage(sender, "#AAAAAA", "• Coinflip System");
        sendMessage(sender, "#AAAAAA", "• Customizable Scoreboard");
    }

    private void showVersion(CommandSender sender) {
        String currentVersion = plugin.getDescription().getVersion();
        String latestVersion = getLatestVersionFromModrinth();

        sendMessage(sender, "#00E5FF", "Current Version: " + hex("#FFFFFF") + currentVersion);

        if (latestVersion != null) {
            sendMessage(sender, "#FFD700", "Latest on Modrinth: " + hex("#FFFFFF") + latestVersion);
            if (!currentVersion.equals(latestVersion)) {
                sendMessage(sender, "#FF5555", "Your version is outdated! Please update.");
            } else {
                sendMessage(sender, "#55FF55", "You are using the latest version.");
            }
        } else {
            sendMessage(sender, "#FF5555", "Could not check for updates.");
        }
    }

    /**
     * Fetch latest version dynamically from Modrinth API.
     */
    private String getLatestVersionFromModrinth() {
        try {
            URL url = new URL("https://api.modrinth.com/v2/project/serveressentials/version");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            if (conn.getResponseCode() != 200) return null;

            Scanner scanner = new Scanner(conn.getInputStream());
            StringBuilder json = new StringBuilder();
            while (scanner.hasNext()) json.append(scanner.nextLine());
            scanner.close();

            // Parse simple JSON manually to find version_number
            String result = json.toString();
            int idx = result.indexOf("\"version_number\":");
            if (idx != -1) {
                int start = result.indexOf('"', idx + 17) + 1;
                int end = result.indexOf('"', start);
                return result.substring(start, end);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Calls the ReloadManager to reload everything.
     */
    private void reloadPlugin(CommandSender sender) {
        if (plugin instanceof ServerEssentials) {
            try {
                ReloadManager.reloadAll(sender);
            } catch (Exception e) {
                sendMessage(sender, "#FF5555", "An error occurred while reloading. Check console for details.");
                e.printStackTrace();
                return;
            }
        } else {
            sendMessage(sender, "#FF5555", "Reload failed: plugin instance mismatch.");
            return;
        }

        sendMessage(sender, "#55FF55", "All configuration files and systems have been reloaded!");
    }

    // Utility: send colorful messages with hex support
    private void sendMessage(CommandSender sender, String hexColor, String message) {
        sender.sendMessage(PREFIX + hex(hexColor) + message);
    }

    /**
     * Converts a hex color string (like #FFAA00) into Minecraft's §x format.
     */
    private String hex(String hexColor) {
        hexColor = hexColor.replace("#", "");
        if (hexColor.length() != 6) return "§f"; // fallback white
        StringBuilder sb = new StringBuilder("§x");
        for (char c : hexColor.toCharArray()) {
            sb.append("§").append(c);
        }
        return sb.toString();
    }
}
