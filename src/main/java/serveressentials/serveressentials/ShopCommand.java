package serveressentials.serveressentials;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class ShopCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = getPrefix();

        // Reload command
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("serveressentials.shop.reload")) {
                sender.sendMessage(prefix + ChatColor.RED + "You do not have permission to reload the shop!");
                return true;
            }

            ShopConfigManager.reloadShopConfig();
            sender.sendMessage(prefix + ChatColor.GREEN + "Shop configuration reloaded!");
            return true;
        }

        // Only players can open shop GUI
        if (!(sender instanceof Player player)) {
            sender.sendMessage(prefix + ChatColor.RED + "Only players can open the shop GUI.");
            return true;
        }

        // Command: /shop
        if (args.length == 0) {
            player.sendMessage(prefix + ChatColor.GREEN + "Opening Shop Sections...");
            ShopGUIListener.openSectionSelector(player, 1);

            return true;
        }

        // Command: /shop <section> [page]
        String section = args[0];
        int page = 1;
        if (args.length >= 2) {
            try {
                page = Integer.parseInt(args[1]);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                player.sendMessage(prefix + ChatColor.RED + "Invalid page number.");
                return true;
            }
        }

        if (!ShopManager.getSections().contains(section)) {
            player.sendMessage(prefix + ChatColor.RED + "Section " + ChatColor.YELLOW + section + prefix + ChatColor.RED + " does not exist.");
            return true;
        }

        ShopGUIListener.openShopGUI(player, section, page);
        return true;
    }
}
