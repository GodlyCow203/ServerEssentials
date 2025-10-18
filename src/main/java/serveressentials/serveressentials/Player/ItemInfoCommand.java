package serveressentials.serveressentials.Player;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import serveressentials.serveressentials.util.PlayerMessages;

public class ItemInfoCommand implements CommandExecutor {

    private final PlayerMessages messages;

    public ItemInfoCommand(PlayerMessages messages) {
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("iteminfo.only-players"));
            return true;
        }

        if (!player.hasPermission("serveressentials.iteminfo")) {
            player.sendMessage(messages.get("iteminfo.no-permission"));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(messages.get("iteminfo.no-item"));
            return true;
        }

        player.sendMessage(messages.get("iteminfo.header"));
        player.sendMessage(messages.get("iteminfo.type", "<type>", item.getType().toString()));
        player.sendMessage(messages.get("iteminfo.amount", "<amount>", String.valueOf(item.getAmount())));

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {

            if (meta.hasDisplayName()) {
                player.sendMessage(messages.get("iteminfo.name", "<name>", meta.getDisplayName()));
            }

            if (meta.hasLore()) {
                player.sendMessage(messages.get("iteminfo.lore-header"));
                for (String line : meta.getLore()) {
                    player.sendMessage(messages.get("iteminfo.lore-line", "<line>", line));
                }
            }

            if (!meta.getEnchants().isEmpty()) {
                player.sendMessage(messages.get("iteminfo.enchants-header"));
                for (Enchantment enchant : meta.getEnchants().keySet()) {
                    int level = meta.getEnchantLevel(enchant);
                    player.sendMessage(messages.get("iteminfo.enchant-line",
                            "<enchant>", enchant.getKey().getKey(),
                            "<level>", String.valueOf(level)));
                }
            }

            if (!meta.getItemFlags().isEmpty()) {
                player.sendMessage(messages.get("iteminfo.flags-header"));
                for (ItemFlag flag : meta.getItemFlags()) {
                    player.sendMessage(messages.get("iteminfo.flag-line", "<flag>", flag.name()));
                }
            }
        }

        return true;
    }
}
