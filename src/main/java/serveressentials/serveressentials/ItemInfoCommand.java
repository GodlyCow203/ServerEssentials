package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemInfoCommand implements CommandExecutor {

    // Dynamic prefix getter (replace with your plugin's prefix system)
    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("serveressentials.iteminfo")) {
            player.sendMessage(getPrefix() + ChatColor.RED + "You do not have permission to use /iteminfo.");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(getPrefix() + ChatColor.YELLOW + "You are not holding any item.");
            return true;
        }

        player.sendMessage(getPrefix() + ChatColor.AQUA + "Item Information:");
        player.sendMessage(ChatColor.GRAY + "Type: " + ChatColor.WHITE + item.getType());
        player.sendMessage(ChatColor.GRAY + "Amount: " + ChatColor.WHITE + item.getAmount());

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName()) {
                player.sendMessage(ChatColor.GRAY + "Name: " + ChatColor.WHITE + meta.getDisplayName());
            }

            if (meta.hasLore()) {
                player.sendMessage(ChatColor.GRAY + "Lore:");
                for (String line : meta.getLore()) {
                    player.sendMessage(ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + line);
                }
            }

            if (!meta.getEnchants().isEmpty()) {
                player.sendMessage(ChatColor.GRAY + "Enchantments:");
                for (Enchantment enchant : meta.getEnchants().keySet()) {
                    int level = meta.getEnchantLevel(enchant);
                    player.sendMessage(ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + enchant.getKey().getKey() + " " + level);
                }
            }

            if (!meta.getItemFlags().isEmpty()) {
                player.sendMessage(ChatColor.GRAY + "Item Flags:");
                for (ItemFlag flag : meta.getItemFlags()) {
                    player.sendMessage(ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + flag.name());
                }
            }
        }

        return true;
    }
}
