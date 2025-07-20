package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class SessionCommand implements CommandExecutor {

    // Instead of static PREFIX, use a method to get dynamic prefix (replace with your own plugin prefix getter)
    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    private static final HashMap<UUID, Long> sessionStartTimes = new HashMap<>();

    // Called from your main plugin class when a player joins
    public static void startSession(Player player) {
        sessionStartTimes.put(player.getUniqueId(), System.currentTimeMillis());
    }

    // Optional cleanup on quit
    public static void endSession(Player player) {
        sessionStartTimes.remove(player.getUniqueId());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Long startTime = sessionStartTimes.get(player.getUniqueId());
        if (startTime == null) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Your session hasn't been tracked.");
            return true;
        }

        long durationMillis = System.currentTimeMillis() - startTime;
        long seconds = durationMillis / 1000 % 60;
        long minutes = durationMillis / (1000 * 60) % 60;
        long hours = durationMillis / (1000 * 60 * 60);

        String sessionTime = String.format("%02dh %02dm %02ds", hours, minutes, seconds);

        player.sendMessage(getPrefix() + ChatColor.GREEN + "Current session duration: " + ChatColor.YELLOW + sessionTime);
        return true;
    }
}
