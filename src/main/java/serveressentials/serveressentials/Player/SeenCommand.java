package serveressentials.serveressentials.Player;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.PlayerMessages;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SeenCommand implements CommandExecutor {

    private final PlayerMessages messages;

    public SeenCommand(ServerEssentials plugin) {
        this.messages = plugin.getPlayerMessages();

        // Add default messages if missing
        messages.addDefault("seen.usage", "<yellow>Usage: /seen <player>");
        messages.addDefault("seen.player-not-found", "<red>Player not found!");
        messages.addDefault("seen.result", "<green>{player} was last seen on <white>{lastSeen}");
        messages.addDefault("seen.never-joined", "<yellow>{player} has never joined the server.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(messages.get("seen.usage"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[0]);
        if (target == null) {
            sender.sendMessage(messages.get("seen.player-not-found"));
            return true;
        }

        String playerName = target.getName() != null ? target.getName() : args[0];

        if (target.getLastPlayed() == 0) {
            sender.sendMessage(messages.get("seen.never-joined", "{player}", playerName));
            return true;
        }

        String lastSeen = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(target.getLastPlayed()));
        sender.sendMessage(messages.get("seen.result",
                "{player}", playerName,
                "{lastSeen}", lastSeen
        ));

        return true;
    }
}
