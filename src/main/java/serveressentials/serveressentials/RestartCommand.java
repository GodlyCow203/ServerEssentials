package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class RestartCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public RestartCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /restart <minutes>");
            return true;
        }

        int minutes;
        try {
            minutes = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid number.");
            return true;
        }

        int seconds = minutes * 60;
        Bukkit.broadcastMessage(ChatColor.RED + "⚠ Server will restart in " + minutes + " minute(s).");

        new BukkitRunnable() {
            int remaining = seconds;

            @Override
            public void run() {
                if (remaining == 0) {
                    Bukkit.broadcastMessage(ChatColor.DARK_RED + "Restarting now...");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
                    cancel();
                    return;
                }

                if (remaining <= 60 && remaining % 10 == 0 || remaining <= 10) {
                    Bukkit.broadcastMessage(ChatColor.GOLD + "⏳ Restart in " + remaining + " seconds...");
                } else if (remaining % 60 == 0) {
                    Bukkit.broadcastMessage(ChatColor.YELLOW + "⏳ Restart in " + (remaining / 60) + " minute(s).");
                }

                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        return true;
    }
}
