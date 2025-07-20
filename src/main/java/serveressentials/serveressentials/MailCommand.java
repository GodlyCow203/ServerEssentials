package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

import java.util.*;

public class MailCommand implements CommandExecutor {

    private static final Map<UUID, List<String>> mailboxes = new HashMap<>();

    // Dynamic prefix getter
    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        UUID uuid = player.getUniqueId();

        if (args.length == 0 || args[0].equalsIgnoreCase("read")) {
            List<String> inbox = mailboxes.getOrDefault(uuid, new ArrayList<>());
            if (inbox.isEmpty()) {
                player.sendMessage(getPrefix() + ChatColor.GRAY + "You have no mail.");
            } else {
                player.sendMessage(getPrefix() + ChatColor.GOLD + "--- Your Mail ---");
                inbox.forEach(msg -> player.sendMessage(getPrefix() + ChatColor.WHITE + "- " + msg));
                mailboxes.put(uuid, new ArrayList<>()); // Clear after reading
            }
            return true;
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("send")) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            UUID targetUUID = target.getUniqueId();
            String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            mailboxes.computeIfAbsent(targetUUID, k -> new ArrayList<>())
                    .add(ChatColor.GRAY + "From " + player.getName() + ": " + ChatColor.WHITE + message);
            player.sendMessage(getPrefix() + ChatColor.GREEN + "Mail sent to " + target.getName() + "!");
            return true;
        }

        player.sendMessage(getPrefix() + ChatColor.RED + "Usage: /mail send <player> <message> or /mail read");
        return true;
    }
}
