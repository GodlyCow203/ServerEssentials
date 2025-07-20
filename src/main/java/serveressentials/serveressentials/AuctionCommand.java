package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AuctionCommand implements CommandExecutor, TabCompleter {
    private final AuctionManager auctionManager;

    public AuctionCommand(AuctionManager auctionManager) {
        this.auctionManager = auctionManager;
    }

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            new AuctionGUI(auctionManager).open(player, 0);
            return true;
        }

        if (args[0].equalsIgnoreCase("sell")) {
            if (args.length != 2) {
                player.sendMessage(getPrefix() + ChatColor.RED + "Usage: /ah sell <price>");
                return true;
            }

            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType().isAir()) {
                player.sendMessage(getPrefix() + ChatColor.RED + "Hold the item you want to sell.");
                return true;
            }

            try {
                double price = Double.parseDouble(args[1]);
                int id = AuctionItem.getNextId();

                // Clone the item BEFORE modifying meta
                ItemStack clone = item.clone();
                ItemMeta meta = clone.getItemMeta();
                if (meta != null) {
                    NamespacedKey key = new NamespacedKey(ServerEssentials.getInstance(), "auction-id");
                    meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, id);
                    clone.setItemMeta(meta);
                }

                auctionManager.addItem(new AuctionItem(player.getUniqueId(), clone, price, id));
                player.getInventory().setItemInMainHand(null);
                player.sendMessage(getPrefix() + ChatColor.GREEN + "Item listed for $" + price);
            } catch (NumberFormatException e) {
                player.sendMessage(getPrefix() + ChatColor.RED + "Invalid number format.");
            }

            return true;
        }

        if (args[0].equalsIgnoreCase("remove") && args.length == 2 && args[1].equalsIgnoreCase("all")) {
            auctionManager.removeItemsBySeller(player.getUniqueId());
            player.sendMessage(getPrefix() + ChatColor.YELLOW + "All your items have been removed from the auction house.");
            return true;
        }

        player.sendMessage(getPrefix() + ChatColor.RED + "Unknown command. Use /ah, /ah sell <price>, or /ah remove all.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if ("sell".startsWith(args[0].toLowerCase())) completions.add("sell");
            if ("remove".startsWith(args[0].toLowerCase())) completions.add("remove");
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("sell")) {
            // Suggest common price values
            List<String> prices = List.of("10", "50", "100", "250", "500");
            List<String> matches = new ArrayList<>();
            for (String price : prices) {
                if (price.startsWith(args[1])) {
                    matches.add(price);
                }
            }
            return matches;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            if ("all".startsWith(args[1].toLowerCase())) {
                return Collections.singletonList("all");
            }
        }

        return Collections.emptyList();
    }
}
