package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class MagnetCommand implements CommandExecutor {

    private final Set<Player> activeMagnets = new HashSet<>();
    private final JavaPlugin plugin;

    public MagnetCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        startMagnetTask();
    }

    private void startMagnetTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : activeMagnets) {
                    player.getWorld().getNearbyEntities(player.getLocation(), 5, 5, 5).stream()
                            .filter(e -> e instanceof org.bukkit.entity.Item)
                            .forEach(e -> {
                                Vector direction = player.getLocation().toVector().subtract(e.getLocation().toVector()).normalize().multiply(0.5);
                                e.setVelocity(direction);
                            });
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (activeMagnets.contains(player)) {
            activeMagnets.remove(player);
            player.sendMessage(getPrefix() + ChatColor.RED + "Magnet OFF");
        } else {
            activeMagnets.add(player);
            player.sendMessage(getPrefix() + ChatColor.GREEN + "Magnet ON");
        }
        return true;
    }
}
