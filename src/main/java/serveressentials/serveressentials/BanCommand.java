package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class BanCommand implements CommandExecutor, TabCompleter {

    private final BanManager banManager;

    public BanCommand(BanManager banManager) {
        this.banManager = banManager;
    }

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ban.use")) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "You don't have permission.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Usage: /ban <player> <time> <reason>");
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (target == null || target.getName() == null) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "That player has never joined the server.");
            return true;
        }

        UUID uuid = target.getUniqueId();
        String name = target.getName();

        long duration = parseTime(args[1]);
        if (duration == -2) {
            sender.sendMessage(getPrefix() + ChatColor.RED + "Invalid time format. Use s/m/h/d or 'perm'");
            return true;
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        String bannedBy = (sender instanceof Player) ? ((Player) sender).getName() : "Console";
        long until = (duration == -1) ? -1 : System.currentTimeMillis() + duration;

        banManager.banPlayer(uuid, name, reason, bannedBy, until);

        if (target.isOnline()) {
            ((Player) target).kickPlayer(banManager.getBanMessage(uuid));
        }

        sender.sendMessage(getPrefix() + ChatColor.GREEN + name + " has been banned.");
        return true;
    }

    private long parseTime(String time) {
        if (time.equalsIgnoreCase("perm")) return -1;
        try {
            long num = Long.parseLong(time.substring(0, time.length() - 1));
            char unit = time.charAt(time.length() - 1);
            return switch (unit) {
                case 's' -> num * 1000L;
                case 'm' -> num * 60 * 1000L;
                case 'h' -> num * 60 * 60 * 1000L;
                case 'd' -> num * 24 * 60 * 60 * 1000L;
                default -> -2;
            };
        } catch (Exception e) {
            return -2;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("ban.use")) return Collections.emptyList();

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> matches = new ArrayList<>();
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase().startsWith(input)) {
                    matches.add(online.getName());
                }
            }
            return matches;
        }

        if (args.length == 2) {
            List<String> timeOptions = Arrays.asList("30s", "1m", "5m", "1h", "1d", "perm");
            List<String> matches = new ArrayList<>();
            for (String option : timeOptions) {
                if (option.startsWith(args[1].toLowerCase())) {
                    matches.add(option);
                }
            }
            return matches;
        }

        if (args.length == 3) {
            List<String> reasons = Arrays.asList("Griefing", "Abuse", "Cheating", "Spamming");
            String input = args[2].toLowerCase();
            List<String> matches = new ArrayList<>();
            for (String reason : reasons) {
                if (reason.toLowerCase().startsWith(input)) {
                    matches.add(reason);
                }
            }
            return matches;
        }

        return Collections.emptyList();
    }
}
