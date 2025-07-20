package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class GlowCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        String prefix = getPrefix();

        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix + ChatColor.RED + "Only players can toggle glow.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("serveressentials.glow")) {
            player.sendMessage(prefix + ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (player.isGlowing()) {
            player.setGlowing(false);
            player.sendMessage(prefix + ChatColor.YELLOW + "Glow effect disabled.");
        } else {
            player.setGlowing(true);
            player.sendMessage(prefix + ChatColor.GREEN + "Glow effect enabled.");
        }

        return true;
    }
}
