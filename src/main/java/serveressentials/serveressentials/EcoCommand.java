package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EcoCommand implements CommandExecutor, TabCompleter {

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = getPrefix();

        if (args.length < 2) {
            sender.sendMessage(prefix + ChatColor.RED + "Usage: /eco <give|take|reset> <player> [amount]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(prefix + ChatColor.RED + "Player not found.");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                if (args.length < 3) return error(sender, "Usage: /eco give <player> <amount>", prefix);
                double giveAmount = parseAmount(args[2], sender, prefix);
                if (giveAmount < 0) return true;

                EconomyManager.addBalance(target, giveAmount);
                sender.sendMessage(prefix + ChatColor.GREEN + "Gave " + ChatColor.GOLD + "$" + giveAmount + ChatColor.GREEN + " to " + ChatColor.YELLOW + target.getName());
                break;

            case "take":
                if (args.length < 3) return error(sender, "Usage: /eco take <player> <amount>", prefix);
                double takeAmount = parseAmount(args[2], sender, prefix);
                if (takeAmount < 0) return true;

                EconomyManager.takeBalance(target, takeAmount);
                sender.sendMessage(prefix + ChatColor.GREEN + "Took " + ChatColor.GOLD + "$" + takeAmount + ChatColor.GREEN + " from " + ChatColor.YELLOW + target.getName());
                break;

            case "reset":
                EconomyManager.resetBalance(target);
                sender.sendMessage(prefix + ChatColor.GREEN + "Reset balance of " + ChatColor.YELLOW + target.getName());
                break;

            default:
                return error(sender, "Unknown subcommand: " + args[0], prefix);
        }

        return true;
    }

    private boolean error(CommandSender sender, String msg, String prefix) {
        sender.sendMessage(prefix + ChatColor.RED + msg);
        return true;
    }

    private double parseAmount(String input, CommandSender sender, String prefix) {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            sender.sendMessage(prefix + ChatColor.RED + "Invalid amount.");
            return -1;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Suggest subcommands
            List<String> subs = List.of("give", "take", "reset");
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();
            for (String sub : subs) {
                if (sub.startsWith(input)) {
                    completions.add(sub);
                }
            }
            return completions;
        }

        if (args.length == 2) {
            // Suggest online player names
            List<String> completions = new ArrayList<>();
            String input = args[1].toLowerCase();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(input)) {
                    completions.add(p.getName());
                }
            }
            return completions;
        }

        // No tab completion for amount
        return List.of();
    }
}
