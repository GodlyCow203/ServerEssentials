package serveressentials.serveressentials;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.WarpManager;

public class SetWarpCooldownCommand implements CommandExecutor {

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
            player.sendMessage(getPrefix() + ChatColor.RED + "Usage: /setwarpcooldown <warp> <seconds>");
            return true;
        }

        String warp = args[0].toLowerCase();
        int seconds;

        try {
            seconds = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Cooldown must be a number.");
            return true;
        }

        if (!WarpManager.warpExists(warp)) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Warp does not exist.");
            return true;
        }

        WarpManager.setWarpCooldown(warp, seconds);
        player.sendMessage(getPrefix() + ChatColor.GREEN + "Cooldown for warp " + ChatColor.YELLOW + warp + ChatColor.GREEN + " set to " + seconds + " seconds.");
        return true;
    }
}
