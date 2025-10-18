package serveressentials.serveressentials.Player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.PlayerMessages;

import java.util.HashSet;
import java.util.Set;

public class MagnetCommand implements CommandExecutor {

    private final Set<Player> activeMagnets = new HashSet<>();
    private final JavaPlugin plugin;
    private final PlayerMessages messages;

    public MagnetCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.messages = ((ServerEssentials) plugin).getPlayerMessages();
        startMagnetTask();
    }

    /**
     * Start the repeating task that pulls items toward players with magnet enabled
     */
    private void startMagnetTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (activeMagnets.isEmpty()) return;

                for (Player player : new HashSet<>(activeMagnets)) {
                    if (!player.isOnline()) {
                        activeMagnets.remove(player);
                        continue;
                    }

                    player.getWorld().getNearbyEntities(player.getLocation(), 5, 5, 5).stream()
                            .filter(entity -> entity instanceof Item)
                            .map(entity -> (Item) entity)
                            .forEach(item -> {
                                Vector direction = player.getLocation().toVector()
                                        .subtract(item.getLocation().toVector())
                                        .normalize()
                                        .multiply(0.5);
                                item.setVelocity(direction);
                            });
                }
            }
        }.runTaskTimer(plugin, 0L, 10L); // every 10 ticks (0.5s)
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("Magnet.only-players"));
            return true;
        }

        if (activeMagnets.contains(player)) {
            activeMagnets.remove(player);
            player.sendMessage(messages.get("Magnet.off"));
        } else {
            activeMagnets.add(player);
            player.sendMessage(messages.get("Magnet.on"));
        }


        // Debug (remove once confirmed)
        Bukkit.getLogger().info("DEBUG Magnet.on = " + messages.getConfig().getString("Magnet.on"));

        return true;
    }
}
