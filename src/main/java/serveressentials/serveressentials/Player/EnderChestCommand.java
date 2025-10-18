package serveressentials.serveressentials.Player;

import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.PlayerMessages;

public class EnderChestCommand implements CommandExecutor {

    private final PlayerMessages messages;

    public EnderChestCommand() {
        this.messages = ServerEssentials.getInstance().getPlayerMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("EnderChest.only-players"));
            return true;
        }

        if (!player.hasPermission("serveressentials.enderchest")) {
            player.sendMessage(messages.get("EnderChest.no-permission"));
            return true;
        }

        player.sendMessage(messages.get("EnderChest.opening"));
        player.openInventory(player.getEnderChest());

        return true;
    }
}
