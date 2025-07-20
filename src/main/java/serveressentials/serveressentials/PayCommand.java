package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class PayCommand implements CommandExecutor {

    // Dynamic prefix method
    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player senderPlayer)) return false;

        if (args.length != 2) {
            senderPlayer.sendMessage(getPrefix() + ChatColor.RED + "Usage: /pay <player> <amount>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (target == null || (!target.hasPlayedBefore() && !target.isOnline())) {
            senderPlayer.sendMessage(getPrefix() + ChatColor.RED + "Player not found.");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            senderPlayer.sendMessage(getPrefix() + ChatColor.RED + "Invalid amount.");
            return true;
        }

        if (amount <= 0) {
            senderPlayer.sendMessage(getPrefix() + ChatColor.RED + "Amount must be positive.");
            return true;
        }

        if (EconomyManager.getBalance(senderPlayer) < amount) {
            senderPlayer.sendMessage(getPrefix() + ChatColor.RED + "You don't have enough money.");
            return true;
        }

        EconomyManager.takeBalance(senderPlayer, amount);   // Withdraw from sender
        EconomyManager.addBalance(target, amount);          // Deposit to target

        senderPlayer.sendMessage(getPrefix() + ChatColor.GREEN + "Paid " + ChatColor.YELLOW + target.getName() + ChatColor.GREEN + " $" + amount);

        if (target.isOnline()) {
            ((Player) target).sendMessage(getPrefix() + ChatColor.GREEN + "You received " + ChatColor.YELLOW + "$" + amount + ChatColor.GREEN + " from " + ChatColor.YELLOW + senderPlayer.getName());
        }

        return true;
    }
}
