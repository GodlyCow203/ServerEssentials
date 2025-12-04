package net.lunark.io.Fun;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.lunark.io.ServerEssentials;
import net.lunark.io.util.FunMessages;

public class SwapCommand implements CommandExecutor {

    private final FunMessages messages;

    public SwapCommand(ServerEssentials plugin, FunMessages messages) {
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            Component msg = messages.get("Swap.only-players");
            sender.sendMessage(msg);
            return true;
        }

        if (args.length != 1) {
            Component usageMsg = messages.get("Swap.usage");
            player.sendMessage(usageMsg);
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || target.equals(player)) {
            Component notFoundMsg = messages.get("Swap.not-found", "{target}", args[0]);
            player.sendMessage(notFoundMsg);
            return true;
        }

        var loc1 = player.getLocation();
        var loc2 = target.getLocation();
        player.teleport(loc2);
        target.teleport(loc1);

        Component swappedMsgPlayer = messages.get("Swap.success", "{target}", target.getName());
        Component swappedMsgTarget = messages.get("Swap.target-notified", "{player}", player.getName());

        player.sendMessage(swappedMsgPlayer);
        target.sendMessage(swappedMsgTarget);

        return true;
    }
}
