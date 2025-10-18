package serveressentials.serveressentials.Player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.PlayerMessages;

public class DisposalCommand implements CommandExecutor {

    private final PlayerMessages messages;

    public DisposalCommand() {
        this.messages = ServerEssentials.getInstance().getPlayerMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("Disposal.only-players"));
            return true;
        }

        Inventory disposal = Bukkit.createInventory(null, 54, "Disposal");
        player.openInventory(disposal);
        player.sendMessage(messages.get("Disposal.opened"));

        return true;
    }
}
