package serveressentials.serveressentials.staff;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.MessagesManager;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class AltsCommand implements CommandExecutor {

    private final MessagesManager messages;

    public AltsCommand(ServerEssentials plugin) {
        this.messages = new MessagesManager(plugin);

        // Default messages for /alts (MiniMessage format)
        messages.addDefault("alts.usage", "<red>Usage: /alts <player>");
        messages.addDefault("alts.player-offline", "<red>Player must be online to check their IP.");
        messages.addDefault("alts.header", "<yellow>Players with the same IP as %player%:");
        messages.addDefault("alts.none", "<gray>- None found online.");
        messages.addDefault("alts.found", "<gray>- %player%");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length != 1) {
            sender.sendMessage(messages.getMessageComponent("alts.usage"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(messages.getMessageComponent("alts.player-offline"));
            return true;
        }

        InetAddress targetIP = target.getAddress().getAddress();
        List<String> matchedPlayers = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.equals(target)) continue;
            if (player.getAddress().getAddress().equals(targetIP)) {
                matchedPlayers.add(player.getName());
            }
        }

        // Send header with placeholder
        sender.sendMessage(messages.getMessageComponent("alts.header", "%player%", target.getName()));

        if (matchedPlayers.isEmpty()) {
            sender.sendMessage(messages.getMessageComponent("alts.none"));
        } else {
            for (String name : matchedPlayers) {
                sender.sendMessage(messages.getMessageComponent("alts.found", "%player%", name));
            }
        }

        return true;
    }
}
