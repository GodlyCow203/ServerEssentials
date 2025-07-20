package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class RepairCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getConfig().getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (!player.hasPermission("serveressentials.repair")) {
            player.sendMessage(getPrefix() + ChatColor.RED + "You don't have permission.");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(getPrefix() + ChatColor.RED + "You're not holding an item.");
            return true;
        }

        // Using Damageable for newer versions instead of deprecated setDurability
        if (item.hasItemMeta() && item.getItemMeta() instanceof Damageable damageable) {
            damageable.setDamage(0);
            item.setItemMeta(damageable);
            player.sendMessage(getPrefix() + ChatColor.GREEN + "Item repaired!");
        } else {
            player.sendMessage(getPrefix() + ChatColor.RED + "This item cannot be repaired.");
        }
        return true;
    }
}
