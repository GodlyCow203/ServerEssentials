package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(getPrefix() + ChatColor.RED + "Only players can check their own balance.");
                return true;
            }

            double balance = EconomyManager.getBalance(player);
            player.sendMessage(getPrefix() + ChatColor.GREEN + "Your balance is: " + ChatColor.GOLD + "$" + String.format("%.2f", balance));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        double balance = EconomyManager.getBalance(target);
        sender.sendMessage(getPrefix() + ChatColor.GREEN + target.getName() + "'s balance is: " + ChatColor.GOLD + "$" + String.format("%.2f", balance));
        return true;
    }
}
