package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BalanceTopCommand implements CommandExecutor {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<Map.Entry<UUID, Double>> topBalances = EconomyManager.getTopBalances(5);

        sender.sendMessage(getPrefix() + ChatColor.GOLD + "" + ChatColor.BOLD + "Top 5 Balances:");
        int rank = 1;
        for (Map.Entry<UUID, Double> entry : topBalances) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getKey());
            String name = player.getName() != null ? player.getName() : "Unknown";
            double balance = entry.getValue();
            sender.sendMessage(getPrefix() + ChatColor.YELLOW + "#" + rank + " " + ChatColor.WHITE + name + ": " + ChatColor.GREEN + "$" + String.format("%.2f", balance));
            rank++;
        }

        return true;
    }
}
