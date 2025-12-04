package net.lunark.io.Player;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import net.lunark.io.ServerEssentials;
import net.lunark.io.util.PlayerMessages;

public class RepairCommand implements CommandExecutor {

    private final PlayerMessages messages;

    public RepairCommand(ServerEssentials plugin) {
        this.messages = plugin.getPlayerMessages();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("Repair.only-players"));
            return true;
        }

        if (!player.hasPermission("serveressentials.repair")) {
            player.sendMessage(messages.get("Repair.no-permission"));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(messages.get("Repair.no-item"));
            return true;
        }

        if (item.hasItemMeta() && item.getItemMeta() instanceof Damageable damageable) {
            damageable.setDamage(0);
            item.setItemMeta(damageable);
            player.sendMessage(messages.get("Repair.success"));
        } else {
            player.sendMessage(messages.get("Repair.cannot-repair"));
        }

        return true;
    }
}
