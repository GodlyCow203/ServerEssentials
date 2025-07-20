package serveressentials.serveressentials;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JailCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final Location jailLocation;

    // Store jailed players and optional release tasks
    private final Map<UUID, BukkitRunnable> releaseTasks = new HashMap<>();

    public JailCommand(JavaPlugin plugin, Location jailLocation) {
        this.plugin = plugin;
        this.jailLocation = jailLocation;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!sender.hasPermission("serveressentials.jail")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /jail <player> [time in minutes]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found or not online.");
            return true;
        }

        // Cancel any existing release task if re-jailing
        if (releaseTasks.containsKey(target.getUniqueId())) {
            releaseTasks.get(target.getUniqueId()).cancel();
            releaseTasks.remove(target.getUniqueId());
        }

        // Teleport to jail
        target.teleport(jailLocation);
        target.sendMessage(ChatColor.RED + "You have been jailed!");

        sender.sendMessage(ChatColor.GREEN + target.getName() + " has been jailed.");

        // If time given, schedule release
        if (args.length >= 2) {
            int minutes;
            try {
                minutes = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid time. Use minutes as a number.");
                return true;
            }

            BukkitRunnable releaseTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (target.isOnline()) {
                        target.sendMessage(ChatColor.GREEN + "You have been released from jail.");
                        // You can teleport them to spawn or last location here
                    }
                    releaseTasks.remove(target.getUniqueId());
                    this.cancel();
                }
            };
            releaseTask.runTaskLater(plugin, minutes * 60L * 20L);
            releaseTasks.put(target.getUniqueId(), releaseTask);

            sender.sendMessage(ChatColor.YELLOW + target.getName() + " will be released in " + minutes + " minute(s).");
        }

        return true;
    }
}
