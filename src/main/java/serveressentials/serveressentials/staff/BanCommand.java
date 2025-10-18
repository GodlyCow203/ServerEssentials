package serveressentials.serveressentials.staff;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import serveressentials.serveressentials.ServerEssentials;
import serveressentials.serveressentials.util.MessagesManager;

import java.util.*;

public class BanCommand implements CommandExecutor, TabCompleter {

    private final BanManager banManager;
    private final MessagesManager messages;
    private final BukkitAudiences adventure;

    public BanCommand(BanManager banManager, ServerEssentials plugin) {
        this.banManager = banManager;
        this.messages = new MessagesManager(plugin);
        this.adventure = BukkitAudiences.create(plugin);

        // Add defaults (MiniMessage)
        messages.addDefault("ban.no-permission", "<red>You don't have permission.");
        messages.addDefault("ban.usage", "<red>Usage: /ban <player> <time> <reason>");
        messages.addDefault("ban.never-joined", "<red>That player has never joined the server.");
        messages.addDefault("ban.invalid-time", "<red>Invalid time format. Use <yellow>s/m/h/d</yellow> or <yellow>perm</yellow>");
        messages.addDefault("ban.success", "<green><player> has been banned.");
        messages.addDefault("ban.kick-message", "<red>You have been banned!\n<gray>Reason: <reason>\n<gray>Banned by: <banner>\n<gray>Until: <until>");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("serveressentials.ban.use")) {
            send(sender, messages.getMessageComponent("ban.no-permission"));
            return true;
        }

        if (args.length < 3) {
            send(sender, messages.getMessageComponent("ban.usage"));
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (target == null || target.getName() == null) {
            send(sender, messages.getMessageComponent("ban.never-joined"));
            return true;
        }

        UUID uuid = target.getUniqueId();
        String name = target.getName();

        long duration = parseTime(args[1]);
        if (duration == -2) {
            send(sender, messages.getMessageComponent("ban.invalid-time"));
            return true;
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        String bannedBy = (sender instanceof Player) ? ((Player) sender).getName() : "Console";
        long until = (duration == -1) ? -1 : System.currentTimeMillis() + duration;

        banManager.banPlayer(uuid, name, reason, bannedBy, until);

        if (target.isOnline()) {
            Component kickMsg = messages.getMessageComponent("ban.kick-message",
                    "<player>", name,
                    "<reason>", reason,
                    "<banner>", bannedBy,
                    "<until>", (until == -1 ? "Forever" : new Date(until).toString())
            );
            ((Player) target).kick(kickMsg);
        }

        send(sender, messages.getMessageComponent("ban.success", "<player>", name));
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

    private void send(CommandSender sender, Component message) {
        adventure.sender(sender).sendMessage(message);
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
