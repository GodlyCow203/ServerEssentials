package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class EnderChestCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String prefix = getPrefix();

        if (!(sender instanceof Player player)) return false;

        if (!player.hasPermission("serveressentials.enderchest")) {
            player.sendMessage(prefix + ChatColor.RED + "You don't have permission.");
            return true;
        }

        player.sendMessage(prefix + ChatColor.GREEN + "Opening your Ender Chest...");
        player.openInventory(player.getEnderChest());
        return true;
    }
}
