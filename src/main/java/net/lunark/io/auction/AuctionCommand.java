package net.lunark.io.auction;

import net.lunark.io.ServerEssentials;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

public class AuctionCommand implements CommandExecutor {

    private final ServerEssentials plugin;

    public AuctionCommand(ServerEssentials plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getAuctionMessagesManager()
                    .getMessage("command.only-players"));
            return true;
        }

        if (!player.hasPermission("serveressentials.ah.use")) {
            player.sendMessage(plugin.getAuctionMessagesManager()
                    .getMessage("command.no-permission"));
            return true;
        }

        if (args.length == 0) {
            if (!player.hasPermission("serveressentials.ah.view")) {
                player.sendMessage(plugin.getAuctionMessagesManager()
                        .getMessage("command.no-permission"));
                return true;
            }
            plugin.getGuiManager().openAuctionGUI(player, 1);
            return true;
        }

        if (args[0].equalsIgnoreCase("my")) {
            if (!player.hasPermission("serveressentials.ah.my")) {
                player.sendMessage(plugin.getAuctionMessagesManager()
                        .getMessage("command.no-permission"));
                return true;
            }
            plugin.getGuiManager().openPlayerItemsGUI(player, 1);
            return true;
        }

        if (args[0].equalsIgnoreCase("sell")) {
            if (!player.hasPermission("serveressentials.ah.sell")) {
                player.sendMessage(plugin.getAuctionMessagesManager()
                        .getMessage("command.no-permission"));
                return true;
            }

            ItemStack handItem = player.getInventory().getItemInMainHand();
            if (handItem == null || handItem.getType().isAir()) {
                player.sendMessage(plugin.getAuctionMessagesManager()
                        .getMessage("command.sell.no-item"));
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(plugin.getAuctionMessagesManager()
                        .getMessage("command.sell.usage"));
                return true;
            }

            double price;
            try {
                price = Double.parseDouble(args[1]);
            } catch (NumberFormatException ex) {
                player.sendMessage(plugin.getAuctionMessagesManager()
                        .getMessage("command.sell.invalid-price"));
                return true;
            }

            int maxSell = plugin.getConfig().getInt("serveressentials.auction.max-sell-limit", 64);
            if (handItem.getAmount() > maxSell) {
                player.sendMessage(plugin.getAuctionMessagesManager()
                        .getMessage("command.sell.max-limit", "%max%", String.valueOf(maxSell)));
                return true;
            }

            int expirationDays = plugin.getConfig().getInt("serveressentials.auction.expiration-days", 7);
            long expirationTime = System.currentTimeMillis() + (expirationDays * 24L * 60L * 60L * 1000L);

            player.getInventory().setItemInMainHand(null);

            plugin.getAuctionManager().addAuctionItem(
                    new AuctionItem(player.getUniqueId(), handItem, price, expirationTime)
            );

            player.sendMessage(plugin.getAuctionMessagesManager()
                    .getMessage("command.sell.success", "%price%", String.valueOf(price)));
            return true;
        }

        player.sendMessage(plugin.getAuctionMessagesManager()
                .getMessage("command.usage"));
        return true;
    }
}
