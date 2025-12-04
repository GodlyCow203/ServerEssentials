package net.lunark.io.staff;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import net.lunark.io.util.MessagesManager;

public class UnfreezeCommand implements CommandExecutor {

    private final MessagesManager messages;

    public UnfreezeCommand(MessagesManager messages) {
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Component msg = messages.get("staff.yml", "unfreeze.only-players");
            sender.sendMessage(msg);
            return true;
        }

        if (args.length != 1) {
            Component msg = messages.get("staff.yml", "unfreeze.usage");
            player.sendMessage(msg);
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            Component msg = messages.get("staff.yml", "unfreeze.not-found");
            player.sendMessage(msg);
            return true;
        }

        boolean wasFrozen = FreezeCommand.frozenPlayers.remove(target);
        if (!wasFrozen) {
            Component msg = messages.get("staff.yml", "unfreeze.not-frozen", "<player>", target.getName());
            player.sendMessage(msg);
            return true;
        }

        Component success = messages.get("staff.yml", "unfreeze.success", "<player>", target.getName());
        player.sendMessage(success);

        Component targetMsg = messages.get("staff.yml", "unfreeze.target");
        target.sendMessage(targetMsg);

        return true;
    }
}
