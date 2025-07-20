package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class RankCommand implements CommandExecutor, TabCompleter {

    private final RankManager rankManager;

    public RankCommand(RankManager rankManager) {
        this.rankManager = rankManager;
    }

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(getPrefix() + ChatColor.YELLOW + "Usage:");
            sender.sendMessage(getPrefix() + ChatColor.RED + "/rank reload");
            sender.sendMessage(getPrefix() + ChatColor.RED + "/rank list");
            sender.sendMessage(getPrefix() + ChatColor.RED + "/rank <player> <rank>");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            rankManager.reloadConfig();
            sender.sendMessage(getPrefix() + ChatColor.GREEN + "Rank config reloaded.");
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            sender.sendMessage(getPrefix() + ChatColor.GREEN + "Available ranks:");
            Set<String> ranks = rankManager.getRankConfig().getConfigurationSection("ranks").getKeys(false);
            for (String rankName : ranks) {
                sender.sendMessage(getPrefix() + ChatColor.DARK_GREEN + "- " + rankName);
            }
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Usage: /rank <player> <rank>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Player not found or not online.");
            return true;
        }

        Rank rank = rankManager.getRank(args[1].toUpperCase());
        if (rank == null) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Rank not found.");
            return true;
        }

        rankManager.setRank(target, rank);
        sender.sendMessage(getPrefix() + ChatColor.GREEN + "Set rank of " + target.getName() + " to " + rank.getPrefix());
        target.sendMessage(getPrefix() + ChatColor.GREEN + "Your rank has been set to " + rank.getPrefix());

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            List<String> subcommands = Arrays.asList("reload", "list");
            String input = args[0].toLowerCase();

            for (String sub : subcommands) {
                if (sub.startsWith(input)) {
                    completions.add(sub);
                }
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(input)) {
                    completions.add(player.getName());
                }
            }
            return completions;
        }

        if (args.length == 2) {
            List<String> completions = new ArrayList<>();
            String input = args[1].toLowerCase();

            if (rankManager.getRankConfig().isConfigurationSection("ranks")) {
                Set<String> rankKeys = rankManager.getRankConfig().getConfigurationSection("ranks").getKeys(false);
                for (String rankKey : rankKeys) {
                    if (rankKey.toLowerCase().startsWith(input)) {
                        completions.add(rankKey);
                    }
                }
            }
            return completions;
        }

        return Collections.emptyList();
    }
}
