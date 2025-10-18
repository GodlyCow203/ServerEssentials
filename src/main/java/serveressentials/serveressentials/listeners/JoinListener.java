package serveressentials.serveressentials.listeners;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import serveressentials.serveressentials.ServerEssentials;

public class JoinListener implements Listener {

    private final ServerEssentials plugin;

    public JoinListener(ServerEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFirstJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPlayedBefore()) {
            FileConfiguration config = plugin.getStarterMoneyConfig();
            int amount = config.getInt("starter-money", 1000);

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + player.getName() + " " + amount);
            player.sendMessage("§aWelcome! You received §e$" + amount + "§a for joining for the first time.");
        }
    }
}
