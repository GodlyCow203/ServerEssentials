package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class RenameWarpCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getConfig().getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (args.length != 2) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Usage: /renamewarp <old> <new>");
            return true;
        }

        String oldWarp = args[0].toLowerCase();
        String newWarp = args[1].toLowerCase();

        if (!WarpManager.warpExists(oldWarp)) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Warp " + ChatColor.YELLOW + oldWarp + ChatColor.RED + " not found.");
            return true;
        }

        WarpManager.renameWarp(oldWarp, newWarp);
        player.sendMessage(getPrefix() + ChatColor.GREEN + "Warp renamed from " + ChatColor.YELLOW + oldWarp + ChatColor.GREEN + " to " + ChatColor.YELLOW + newWarp);
        return true;
    }
}
