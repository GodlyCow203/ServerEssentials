package serveressentials.serveressentials;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardCommand implements CommandExecutor, TabCompleter {

    private final SimpleScoreboard plugin;

    public ScoreboardCommand(SimpleScoreboard plugin) {
        this.plugin = plugin;
    }

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = getPrefix();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(prefix + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length == 1) {
            String sub = args[0].toLowerCase();

            switch (sub) {
                case "reload":
                    plugin.reloadScoreboardConfig();
                    sender.sendMessage(prefix + ChatColor.GREEN + "Scoreboard config reloaded successfully!");
                    return true;

                case "enable":
                    if (SimpleScoreboard.isDisabled(player)) {
                        SimpleScoreboard.enable(player);
                        sender.sendMessage(prefix + ChatColor.GREEN + "Scoreboard enabled.");
                    } else {
                        sender.sendMessage(prefix + ChatColor.YELLOW + "Scoreboard is already enabled.");
                    }
                    return true;

                case "disable":
                    if (!SimpleScoreboard.isDisabled(player)) {
                        SimpleScoreboard.disable(player);
                        sender.sendMessage(prefix + ChatColor.RED + "Scoreboard disabled.");
                    } else {
                        sender.sendMessage(prefix + ChatColor.YELLOW + "Scoreboard is already disabled.");
                    }
                    return true;

                default:
                    break;
            }
        }

        sender.sendMessage(prefix + ChatColor.RED + "Usage: /scoreboard <reload|enable|disable>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            String input = args[0].toLowerCase();

            for (String option : List.of("reload", "enable", "disable")) {
                if (option.startsWith(input)) {
                    suggestions.add(option);
                }
            }
            return suggestions;
        }
        return List.of();
    }
}
