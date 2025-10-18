package serveressentials.serveressentials.Player;

import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.PlayerMessages;

public class HatCommand implements CommandExecutor {

    private final PlayerMessages messages;

    public HatCommand() {
        this.messages = ServerEssentials.getInstance().getPlayerMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("Hat.only-players"));
            return true;
        }

        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getType().isAir()) {
            player.sendMessage(messages.get("Hat.no-item"));
            return true;
        }

        ItemStack helmet = player.getInventory().getHelmet();
        player.getInventory().setHelmet(handItem);
        player.getInventory().setItemInMainHand(helmet);

        player.sendMessage(messages.get("Hat.success"));
        return true;
    }
}
