package serveressentials.serveressentials.Player;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.listeners.KillTracker;
import serveressentials.serveressentials.util.PlayerMessages;

public class KillCommand implements CommandExecutor {

    private final KillTracker killTracker;
    private final PlayerMessages messages;

    public KillCommand(KillTracker killTracker, PlayerMessages messages) {
        this.killTracker = killTracker;
        this.messages = messages;

        // Add defaults under Kill section
        this.messages.addDefault("Kill.only-players", "<prefix><red>Only players can use this command.");
        this.messages.addDefault("Kill.kill-count", "<prefix><green>You have <gold>{kills} <green>player kills.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Component msg = messages.get("Kill.only-players");
            sender.sendMessage(msg);
            return true;
        }

        int kills = killTracker.getKills(player);
        Component msg = messages.get("Kill.kill-count", "{kills}", String.valueOf(kills));
        player.sendMessage(msg);
        return true;
    }
}
