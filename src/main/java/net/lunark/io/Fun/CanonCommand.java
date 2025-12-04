package net.lunark.io.Fun;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import net.lunark.io.util.FunMessages;

public class CanonCommand implements CommandExecutor {

    private final FunMessages messages;

    public CanonCommand(FunMessages messages) {
        this.messages = messages;

        messages.addDefault("Canon.Messages.PlayerOnly", "<red>Only players can use this command.");
        messages.addDefault("Canon.Messages.Success", "<green>Woosh! You were launched into the air!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("Canon.Messages.PlayerOnly"));
            return true;
        }

        Vector velocity = player.getLocation().getDirection().multiply(2).setY(2);
        player.setVelocity(velocity);

        player.sendMessage(messages.get("Canon.Messages.Success"));
        return true;
    }
}
