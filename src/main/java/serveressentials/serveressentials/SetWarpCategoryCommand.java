package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class SetWarpCategoryCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig()
                .getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Usage: /setwarpcategory <warp> <category>");
            return true;
        }

        String warpName = args[0].toLowerCase();
        String category = args[1];

        if (!WarpManager.warpExists(warpName)) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Warp " + warpName + " does not exist.");
            return true;
        }

        WarpManager.setWarpCategory(warpName, category);
        player.sendMessage(getPrefix() + ChatColor.GREEN + "Category for warp " + warpName + " set to " + ChatColor.YELLOW + category);
        return true;
    }
}
