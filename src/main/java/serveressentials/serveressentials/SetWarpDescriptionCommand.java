package serveressentials.serveressentials;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.WarpManager;

public class SetWarpDescriptionCommand implements CommandExecutor {

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

        if (args.length < 2) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Usage: /setwarpdesc <warp> <description...>");
            return true;
        }

        String warp = args[0].toLowerCase();
        String description = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        if (!WarpManager.warpExists(warp)) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Warp does not exist.");
            return true;
        }

        WarpManager.setWarpDescription(warp, description);
        player.sendMessage(getPrefix() + ChatColor.GREEN + "Description set for warp " + ChatColor.YELLOW + warp);
        return true;
    }
}
