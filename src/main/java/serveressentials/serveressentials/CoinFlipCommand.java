package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CoinFlipCommand implements CommandExecutor, TabCompleter {
    private final Random random = new Random();

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length != 1) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Usage: /coinflip <amount>");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Invalid amount.");
            return true;
        }

        if (amount <= 0) {
            player.sendMessage(getPrefix() + ChatColor.RED + "Amount must be greater than 0.");
            return true;
        }

        double balance = DailyRewardGUI.getBalance(player);
        if (balance < amount) {
            player.sendMessage(getPrefix() + ChatColor.RED + "You don't have enough money! Your balance: " + ChatColor.YELLOW + balance);
            return true;
        }

        boolean win = random.nextBoolean();
        if (win) {
            DailyRewardGUI.addBalance(player, amount);
            player.sendMessage(getPrefix() + ChatColor.GREEN + "You won the coin flip! +" + amount);
        } else {
            DailyRewardGUI.takeBalance(player, amount);
            player.sendMessage(getPrefix() + ChatColor.RED + "You lost the coin flip! -" + amount);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = Arrays.asList("10", "50", "100", "500", "1000");
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();

            for (String suggestion : suggestions) {
                if (suggestion.startsWith(input)) {
                    completions.add(suggestion);
                }
            }

            return completions;
        }
        return List.of();
    }
}
